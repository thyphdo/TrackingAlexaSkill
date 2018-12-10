package com.example.handlers;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.response.ResponseBuilder;

import static com.amazon.ask.request.Predicates.intentName;

//Source: https://github.com/alexa/alexa-skills-kit-sdk-for-java
public class EquipStatusDemoHandler implements RequestHandler {

	/**
	 * Name of the slot provided by the Alexa Skill Service
	 * equipName - equipment ID whose the user wants to check the information of 
	 */
	public static final String EQUIP_NAME = "equipName";

	/**
	 * Returns if the Alexa skill service's input matches with the intent this handler cover
	 *	
	 * @param input 
	 * @return whether the input's intent is EquipStatusIntent 
	 */
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("EquipStatusIntent"));
	}

	/**
	 * Build an according response based on the input request 
	 * Correct messages: Yes - if the machine is working 
	 * 					 No - if the machine is not working
	 * Incorrect messages: I'm not sure what the equipment's name is, please try again.
	 * Reprompt message: I don't think I get the equipment's name. You can ask me again by kindly asking
	 * 					tell me the information of equipment
	 * @Override
	 * @param input 
	 * @return a response builder with relevant message if successful from the API call 
	 * or a message indicating errors so the SDK sends the correct JSON object back to user.
	 */
	@Override
	public Optional<Response> handle(HandlerInput input) {

		Request request = input.getRequestEnvelope().getRequest();
		IntentRequest intentRequest = (IntentRequest) request;
		Intent intent = intentRequest.getIntent();
		Map<String, Slot> slots = intent.getSlots();

		// Get the jobSiteName slot
		Slot equipNameSlot = slots.get(EQUIP_NAME);

		String speechText, repromptText;
		boolean isAskResponse = false;

		// Check for equip's name and create output to user.
		if (equipNameSlot != null) {
			// Store the job site name in the Session and create response.
			String equipName = equipNameSlot.getValue().toLowerCase();
			
			//****// CALL API //****// 
			HttpGet info = new HttpGet();
			info.run(equipName);
			
			//If the equipment exists
			if (info.equipExists()){
				input.getAttributesManager().setSessionAttributes(Collections.singletonMap(EQUIP_NAME,(Object) equipName));
				
				// grab equipment status
				if(info.equipStatus().contains("true")) {
					speechText = "Yes";
				}
				else {
					speechText = "No";
				}
				repromptText =
						"You can ask me again by kindly saying, " + "tell me about the job site";
			}
			else {
				speechText =
						"Machine " + equipName + " is not valid, please try again";
				repromptText =
						"You can ask me again by kindly saying, " + "tell me about the job site";
			}

		} else {
			// Render an error since we don't know what the equipment's name is.
			speechText = 
//					"I'm not sure what the job site's name is, please try again";
					"I'm note sure what the equipement name is. It may be invalid. Please try again.";
			repromptText = 
//					"I don't think I get the job site's name. You can ask me again by kindly asking, "
//							+ "tell me about the job site";
					"You can ask me again by kindly saying something like is longboard available";
			isAskResponse = true;
		}

		//Building the response
		ResponseBuilder responseBuilder = input.getResponseBuilder();

		responseBuilder.withSimpleCard("EquipSession", speechText)
		.withSpeech(speechText)
		.withShouldEndSession(false);

		if (isAskResponse) {
			responseBuilder.withShouldEndSession(false)
			.withReprompt(repromptText);
		}

		return responseBuilder.build();
	}
}