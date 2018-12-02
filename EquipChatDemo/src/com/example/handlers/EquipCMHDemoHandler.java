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
public class EquipCMHDemoHandler implements RequestHandler {

	public static void main(String[] args) {
//		HttpGet info = new HttpGet();
//		info.run("bulldozer");
//		String CMH = info.equipCMH();
//		String jobSite = info.getJobSite();
//		System.out.println(CMH + jobSite);
//		
//		HttpGet info2 = new HttpGet();
//		info2.run("shortboard");
//		String CMH2 = info2.equipCMH();
//		String jobSite2 = info2.getJobSite();
//		
//		System.out.println(CMH2 + jobSite2);
	}
	
	public static final String EQUIP_NAME = "equipName";

	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("EquipCMHIntent"));
	}

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
					"I don't think I get the equipment's name. You can ask me again by kindly asking, "
							+ "what's the machine's information ";
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