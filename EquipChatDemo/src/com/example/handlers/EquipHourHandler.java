package com.example.handlers;

import static com.amazon.ask.request.Predicates.intentName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
	public static void main (String[] argrs) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date = "2018-W45";
		//		SimpleDateFormat sdf = new ("MM dd yyyy");
		Calendar cal = Calendar.getInstance();
		//cal.set(Calendar.WEEK_OF_YEAR, 46);        
		//cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		System.out.println(cal.get(Calendar.WEEK_OF_YEAR)); // Java is cool
	}

	//To check if a given date is in correct form of "yyyy-MM-dd"
	public static boolean isValidFormatted(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(date.trim());
		} catch (ParseException e) {
			return false;
		}
		return true;
	}

	//handle the Years problem from Alexa's VUI
	//If the user's utterance does not specify a year, Alexa defaults to dates on or after the current date.
	public static String checkValidYear(String date) {
		//If succeed, double check if it's match the current year, fix if it's in the future 
		String[] dateSplit = date.split("-");
		//Get the current year 
		Calendar cal = Calendar.getInstance();
		int currentYear = cal.get(Calendar.YEAR);
		//Assign the year portion to the current year position (no matter if it's true or not)
		dateSplit[0] = currentYear + "";
		return String.join("-", dateSplit);
	}
	//since last week  - since "yyyy-Wxx"   (start)
		//since yesterday/date - since "yyyy-mm-dd" (start)
		//since this week - since "yyyy-Wxx"  (start)
		//from yesterday to today - from "yyyy-mm-dd" to "yyyy-mm-dd" (start-end)
		//from last week to this week - from "yyyy-Wxx" to "yyyy-Wxx" (start-end)
		//from date to date  - from "yyyy-mm-dd" to "yyyy-mm-dd"
		//from this month to this month - from "yyyy-mm" to "yyyy-mm"
	
	//To convert "yyyy-Wxx" or "yyyy-MM" into "yyyy-MM-dd"
	public static String convertDate(String date) {
		String[] dateSplit = date.split("-");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String converted = "";

		//***"yyyy-Wxx" format ***
		if (dateSplit[1].contains("W")) {
			int week =  Integer.parseInt(dateSplit[1].substring(1));
			Calendar cal = Calendar.getInstance();
			int thisWeek = cal.get(Calendar.WEEK_OF_YEAR);
			
			if (week == thisWeek) { //if "yyyy-Wxx" refers to the current week, take today
				converted = dateFormat.format(cal.getTime());
			}
			else { //if "yyyy-Wxx" refers to other week, take the monday of that week
				cal.set(Calendar.WEEK_OF_YEAR, week);        
				cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);  
				converted = dateFormat.format(cal.getTime()); 
			}
		} 
		//***"yyyy-MM" format ***
		else {
			
		}
		return "";
	}

	public static String[] dateHandler(String start, String end) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		String[] start_end = new String[2];

		//If "endDate" is not provided, "since___" is generated, today Date is assigned
		if (end == null) {
			end = dateFormat.format(cal.getTime());
		}
		//Check validity of start and end date
		if (!isValidFormatted(start)) {
		
		} else {
			start = checkValidYear(start);
		}
		
		if (!isValidFormatted(end)) {
			
		} else {
			end = checkValidYear(end);
		}

			//		String converted = "";
			//		//check if it's a "yyyy-mm" or "yyyy-Wxx"
			//		if (date.contains("W")) {
			//			//week format
			//			
			//		}

			return new String[0];
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
		boolean isValidInput = false;//equipNameSlot != null && isValid(startTimeSlot.getValue(),endTimeSlot.getValue());

		//Set up response
		String speechText = "Empty";
		String repromptText = "Empty";
		boolean isAskResponse = false;


		if (isValidInput) {
			//Stringify all the slot 
			String equipName = equipNameSlot.getValue();
			String startTime = startTimeSlot.getValue();
			String endTime = endTimeSlot.getValue();

			//**** if startTime is not provided => failed
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

			//			if (isValid(startTimeSlot.getValue(),endTimeSlot.getValue())){
			//				speechText += " the input time are invalid";
			//			}
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
