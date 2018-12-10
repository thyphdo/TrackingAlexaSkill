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

public class EquipCMHDemoHandler implements RequestHandler {
	
	/**
	 * Name of the slot provided by the Alexa Skill Service
	 * equipName - equipment ID
	 */
	public static final String EQUIP_NAME = "equipName";

	/**
	 * Returns if the Alexa skill service's input matches with the intent this handler cover
	 *	
	 * @param input 
	 * @return whether the input's intent is EquipCMHIntent 
	 */
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("EquipCMHIntent"));
	}

	/**
	 * Build an according response based on the input request 
	 * Correct message: Machine X has been running for Y hours
	 * Error message: Machine X is not valid, please try again
	 * Reprompt message: I don't think I get the equipment's name. You can ask me again by kindly saying "Tell me about the CMH of X"
	 * 
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

		// Get the equipName slot
		Slot equipNameSlot = slots.get(EQUIP_NAME);

		String speechText, repromptText;
		boolean isAskResponse = false;

		// Check for equip's name and create output to user.
		if (equipNameSlot != null) {
			// Store the equip name in the Session and create response.
			String equipName = equipNameSlot.getValue().toLowerCase();
			
			//****// CALL API //****// 
			HttpGet info = new HttpGet();
			info.run(equipName);

			//If the equipment exists 
			if (info.equipExists()){
				String CMH = info.equipCMH();
				speechText =
						"Machine " + equipName + " has been running for " + CMH + " hours";

				input.getAttributesManager().setSessionAttributes(Collections.singletonMap(EQUIP_NAME,(Object) equipName));

				repromptText =
						"You can ask me again by kindly saying, " + "tell me about the machine's information";
			}
			else {
				speechText =
						"Machine " + equipName + " is not valid, please try again";
				repromptText =
						"You can ask me again by kindly saying, " + "tell me about the machine's information";
			}
		} 
		else {
			// Render an error since we don't know what the equipment's name is.
			speechText = 
					"I'm not sure what the equipment's name is, please try again";
			repromptText = 
					"I don't think I get the equipment's name. You can ask me again by kindly asking something like, "
							+ "what's longboard's information ";
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