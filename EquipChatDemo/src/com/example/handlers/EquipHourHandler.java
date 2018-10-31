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

public class EquipHourHandler implements RequestHandler {
	
	public static final String EQUIP_NAME = "equipName";
	public static final String START_TIME = "startTime";
	public static final String END_TIME = "endTime";

	//EquipHourIntent
	//Slot types: 
	//equipName - Amazon.SearchQuery 
	//startTime - Amazon.Date (yyyy-mm-dd)
	//endTime - Amazon.Date (yyyy-mm-dd)
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("EquipHourIntent"));
	}
	
	//return if start and end time have correct format
	//source: http://www.java2s.com/Tutorial/Java/0120__Development/CheckifaStringisavaliddate.htm
	public static boolean isValid(String start, String end) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    dateFormat.setLenient(false);
	    try {
	      dateFormat.parse(start.trim());
	      dateFormat.parse(end.trim());
	    } catch (ParseException e) {
	      return false;
	    }
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
		Slot equipNameSlot = slots.get(EQUIP_NAME);
		Slot startTimeSlot = slots.get(START_TIME);
		Slot endTimeSlot = slots.get(END_TIME);

		
		//Check for validity of inputs 
		boolean isValidInput = equipNameSlot != null && isValid(startTimeSlot.getValue(),endTimeSlot.getValue());
	
		//Set up response
		String speechText = "Empty";
		String repromptText = "Empty";
		boolean isAskResponse = false;
		
		
		if (isValidInput) {
			//Stringify all the slot 
			String equipName = equipNameSlot.getValue();
			String startTime = startTimeSlot.getValue();
			String endTime = endTimeSlot.getValue();

			//****// CALL API //****// 
			String CMH = "20";

			//IF API RETURN POSITIVE MESSAGE 
			if (CMH == "20"){
				speechText = equipName + " has been running for " + CMH + " hours. From " + startTime + " to " + endTime;
			}
			//IF API RETURN AN ERROR
			else {
				speechText = 
						"There's an error from request to the API with " + equipName + "start time: " 
								+ startTime + ", end time: " + endTime + ". please try again";
			}
			repromptText = "Anything else?";
		} //IF INPUTS HAVE ERROR
		else {
			//if it's the name
			if (equipNameSlot == null) {
				speechText = "I'm not sure what the equipment's name is, please try again";
			}
			
			if (isValid(startTimeSlot.getValue(),endTimeSlot.getValue())){
				speechText += " the input time are invalid";
			}
			isAskResponse = true;
			repromptText = "Anything else?";
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
