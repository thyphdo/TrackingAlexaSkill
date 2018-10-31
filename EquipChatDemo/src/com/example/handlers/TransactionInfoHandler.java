package com.example.handlers;

import static com.amazon.ask.request.Predicates.intentName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class TransactionInfoHandler implements RequestHandler {
	
	public static final String TIME_STAMP = "timeStamp";
	public static final String TRANSACTION_ID = "transactionID";

	//TransactionsInfoIntent
	//Slot types: 
	//timeStamp - Amazon.Date (yyyy-mm-dd)
	//transactionID - Amazon.Number (String)
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("TransactionInfoIntent"));
	}
	
	//return if start and end time have correct format
	//source: http://www.java2s.com/Tutorial/Java/0120__Development/CheckifaStringisavaliddate.htm
	public static boolean isValid(Slot time, Slot id) {
		//if there's a time, check time format
		if (time != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		    dateFormat.setLenient(false);
		    try {
		      dateFormat.parse(time.getValue().trim());
		    } catch (ParseException e) {
		      return false;
		    }
		}
		//if there's an id, check id format
		if (id != null) {
			if (!id.getValue().matches("\\d+"))
				return false;
		}
		
		//if both are null
		if (time == null && id == null)
			return false;
		return true;	
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
		Slot timeSlot = slots.get(TIME_STAMP);
		Slot transactionIDSlot = slots.get(TRANSACTION_ID);

		
		//Check for validity of inputs 
		boolean isValidInput = isValid(timeSlot,transactionIDSlot);
	
		//Set up response
		String speechText = "Empty";
		String repromptText = "Empty";
		boolean isAskResponse = false;
		
		//Transaction type is "Transaction type", operated by "PDAUsername" at "jobSite".
		if (isValidInput) {
			//Stringify all the slot if applicable (either time or id should be valid)
			String time = timeSlot == null ? "" : timeSlot.getValue();
			String id = transactionIDSlot == null ? "" : transactionIDSlot.getValue();

			//****// CALL API //****// 
			String type = "fuel";
			String user = "Michael";
			String jobSite = "Gettysburg";

			//IF API RETURN POSITIVE MESSAGE 
			if (type == "fuel"){
				speechText = "Transaction type is " + type + ", operated by " + user + " at "+ jobSite;
			}
			//IF API RETURN AN ERROR
			else {
				speechText = 
						"There's an error from request to the API with time: " 
								+ time + ", ID: " + id + ". please try again";
			}
		} //IF INPUTS HAVE ERROR
		else {
			//if none of 2 slots found
			if (timeSlot == null && transactionIDSlot == null) {
				speechText = "You haven't provided either transaction ID or a time period. Please try again";
			}
			//if either one have error
			else { 
				speechText = "You either provide an invalid ID or time period. Please try again";
			}
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
