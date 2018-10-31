package com.example.handlers;

import static com.amazon.ask.request.Predicates.intentName;

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

public class LastTransactionHandler implements RequestHandler{

	public static final String EQUIP_NAME = "equipName";

	//LastTransactionIntent
	//Slot types: 
	//equipName - Amazon.SearchQuery 
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("LastTransactionIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		//Capture input as JSON file 
		Request request = input.getRequestEnvelope().getRequest();
		IntentRequest intentRequest = (IntentRequest) request;
		//Generate intent object
		Intent intent = intentRequest.getIntent();
		//Get slots from intent object as a map
		Map<String, Slot> slots = intent.getSlots();

		// Get the input slots
		Slot equipName = slots.get(EQUIP_NAME);

		//Check for validity of inputs 
		boolean isValidInput = equipName == null;

		//Set up response
		String speechText = "Empty";
		String repromptText = "Empty";
		boolean isAskResponse = false;


		if (isValidInput) {
			//Stringify all the slot 
			String name = equipName.getValue();

			//****// CALL API //****// 
			String type = "repair";
			String user = "Sophie";
			String jobSite = "Mechanicsburg";

			//IF API RETURN POSITIVE MESSAGE 
			if (type == "repair"){
				speechText = "Transaction type is " + type + ", operated by " + user + " at "+ jobSite;
			}
			//IF API RETURN AN ERROR
			else {
				speechText = 
						"There's an error from request to the API with equipment " 
								+ name + ". please try again";
			}
		}//IF INPUTS HAVE ERROR
		else {
			//if it's the time
			speechText = "I'm not sure what the equipment's name is, please try again";
			isAskResponse = true;
		}

		repromptText = "Anything else?";

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
