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
//***** Done integrate, need formatting *****//
public class LastTransactionHandler implements RequestHandler{

	public static final String EQUIP_NAME = "equipName";
	
	public static final int TRANSACTION_TIME = 0;
	public static final int TRANSACTION_TYPE = 1;
	public static final int USER = 2;
	public static final int JOBSITE = 3;

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
		boolean isValidInput = equipName.getValue() != null;

		//Set up response
		String speechText = "Empty";
		String repromptText = "Empty";
		boolean isAskResponse = false;


		if (isValidInput) {
			//Stringify all the slot 
			String name = equipName.getValue().toLowerCase();

			//****// CALL API //****// 	
			LatestTransaction apicall = new LatestTransaction();
			apicall.run(name);
			String[] result = apicall.basicTransactionInfo();
			
			//IF API RETURN POSITIVE MESSAGE 
			if (result.length != 0){
				//0: date  1: time  2: transactionType   3: pdaUsername  4: jobSite
				String date = result[0];
				String time = result[1];
				String type = result[2];
				String user = result[3];
				String jobSite = result[4];
				
				speechText = name + " last transaction is " + type + " at jobsite " + jobSite + " on " + date + " at " + time;
				//if there is no user available 
				if (user.contains("No"))
					speechText += " with no available username assigned";
				else {
					speechText += " operated by " + user;
				}
			}
			//IF API RETURN AN ERROR
			else {
				speechText = 
						"There's an error from request to the API with equipment or the equipment does not exist. please try again";
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
