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

	/**
	 * Name of the slot provided by the Alexa Skill Service
	 * equipName - equipment ID whose the user wants to check the CMH of
	 */
	public static final String EQUIP_NAME = "equipName";
	
	/**
	 * Information index in the response returned by API Call
	 * 0 - the date and time of the transaction
	 */
	public static final int TRANSACTION_TIME = 0;
	
	/**
	 * Information index in the response returned by API Call
	 * 1 - the type of the transaction
	 */
	public static final int TRANSACTION_TYPE = 1;
	
	/**
	 * Information index in the response returned by API Call
	 * 2 - the user related to the transaction
	 */
	public static final int USER = 2;
	
	/**
	 * Information index in the response returned by API Call
	 * 3 - the location where transaction happened
	 */
	public static final int JOBSITE = 3;

	/**
	 * Returns if the Alexa skill service's input matches with the intent this handler cover
	 *	
	 * @param input 
	 * @return whether the input's intent is LastTransactionIntent 
	 */
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("LastTransactionIntent"));
	}

	/**
	 * Build an according response based on the input request 
	 * Correct messages: X's last transaction is type Y at jobsite Z on transDate at transTime operated by User
	 * Incorrect messages: I'm not sure what the equipment's name is, please try again
	 * Reprompt message: Try again by asking something like what's the last transaction of bulldozer
	 * @Override
	 * @param input 
	 * @return a response builder with relevant message if successful from the API call 
	 * or a message indicating errors so the SDK sends the correct JSON object back to user.
	 */
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
						name + " may not exist or there isn't any trasanction of " +name +" within the past 3 months. please try again";
			}
			repromptText = "Anything else?";

		}//IF INPUTS HAVE ERROR
		else {
			//if it's the time
			speechText = "I'm not sure what the equipment's name is, please try again";
			repromptText = "Try again by asking something like what's the last transaction of bulldozer";

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
