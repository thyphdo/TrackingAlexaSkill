package com.example.handlers;

import static com.amazon.ask.request.Predicates.intentName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.joda.time.DateTime;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.response.ResponseBuilder;

public class TransactionsTotalHandler implements RequestHandler {
	
	public static final String START_TIME = "startTime";
	public static final String END_TIME = "endTime";
	public static final String DURATION_TIME = "duration";

	//TransactionsTotalIntent
	//Slot types: 
	//startTime - Amazon.Date (yyyy-mm-dd)
	//endTime - Amazon.Date (yyyy-mm-dd)
	//duration - Amazon.Date (yyyy-Wxx) (yyyy-mm)
	//Time can be yyyy-mm-dd; yyyy-Wxx (last week, this week); yyyy-mm (this month) 
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("TransactionsTotalIntent"));
	}
	
	//return if start and end time have correct format
	//source: http://www.java2s.com/Tutorial/Java/0120__Development/CheckifaStringisavaliddate.htm
	private static boolean isValid(String start, String end) {
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
	
	private static ArrayList<String> handleWeekMonthFormat(String time) {
		ArrayList<String> result = new ArrayList<String>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		//Get this week's order
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String week = cal.get(Calendar.WEEK_OF_YEAR) + "";

		//Handling yyyy-Wxx as this week (Monday of the week to today)
		if (time.split("-W")[1] == week) {
			//Converting from week to date
			LocalDate ld = LocalDate.parse(time+"-1", DateTimeFormatter.ISO_WEEK_DATE);
			String startDate1 = ld.toString();
			cal.add(Calendar.DATE, 0);
			String formatted = format.format(cal.getTime());
			String endDate1 = formatted;
			result.add(startDate1); result.add(endDate1);
		}
		//Handling yyyy-Wxx as last week (Monday of the week to Sunday of the week)
		else if ((time.split("-W")[1] != week)) {
			//Converting from week to date
			LocalDate ld = LocalDate.parse(time+"-1", DateTimeFormatter.ISO_WEEK_DATE);
			String startDate2 = ld.toString();
			
			Date predefined;
			Date end = Calendar.getInstance().getTime();
			try {
				predefined = format.parse(startDate2);
				end = new DateTime(predefined).plusDays(7).toDate();

			} catch (ParseException e) {
				e.printStackTrace();
			}
			String endDate2 = format.format(end);
			result.add(startDate2); result.add(endDate2);
		}
		//Handling yyyy-mm (this month) day is not provided 
		else if (time.split("-").length == 2) {
			String startDate3 = time + "-01";
			String endDate3 = "";
			if (time.contains("-02")) 
				endDate3 = time + "-28"; //DOUBLE CHECK
			else if(time.contains("-01") || time.contains("-03") || time.contains("-05")
					|| time.contains("-07") || time.contains("-08") || time.contains("-10") || time.contains("-12"))
			{
				endDate3 = time + "-31";
			}
			else {
				endDate3 = time + "-30";
			}
			result.add(startDate3); result.add(endDate3);
		}
		return result;
	}
	
	private ArrayList<String> getDateFromIntent(final Slot startSlot, Slot endSlot, Slot durationSlot) {

		ArrayList<String> result = new ArrayList<String>();
		//Check if durationSlot is provided or a pair of start-end time is provided
		if (startSlot == null && endSlot == null && durationSlot == null)
			return result;
		if (startSlot != null && endSlot != null) {
			String startTime = startSlot.getValue();
			String endTime = endSlot.getValue();
			if (isValid(startTime,endTime)) { //check if the format is yyyy-MM-dd
				result.add(startTime);
				result.add(endTime);
			} 
			//check if the format is from yyyy-Wxx/yyyy-MM to yyyy-Wxx/yyyy-MM
			//if the function return a date
			else if (handleWeekMonthFormat(startTime).size() != 0 && handleWeekMonthFormat(endTime).size() != 0 ) { 
				result.add(handleWeekMonthFormat(startTime).get(0));
				result.add(handleWeekMonthFormat(startTime).get(1));
			}
		}
		else if (durationSlot != null) {
			String durationTime = durationSlot.getValue();
			ArrayList<String> arr = handleWeekMonthFormat(durationTime);
			if ( arr.size() == 2) {
				result.add(arr.get(0));
				result.add(arr.get(1));
			}
		}
		return result;
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
		Slot startTimeSlot = slots.get(START_TIME);
		Slot endTimeSlot = slots.get(END_TIME);
		Slot durationSlot = slots.get(DURATION_TIME);

		//Check for validity of inputs, time processor return exactly two date 
		ArrayList<String> startendTime = getDateFromIntent(startTimeSlot,endTimeSlot,durationSlot);
		boolean isValidInput = startendTime.size() == 2;

		//Set up response
		String speechText = "Empty";
		String repromptText = "Empty";
		boolean isAskResponse = false;
		
		if (isValidInput) {
			//Stringify all the slot 
			String startTime = startendTime.get(0);
			String endTime = startendTime.get(1);

			//****// CALL API //****// 
			String total = "30";

			//IF API RETURN POSITIVE MESSAGE 
			if (total == "30"){
				speechText = "There have been " + total + " transactions from " + startTime + " to " + endTime;
			}
			//IF API RETURN AN ERROR
			else {
				speechText = 
						"There's an error from request to the API with start time: " 
								+ startTime + ", end time: " + endTime + ". please try again";
			}
		} //IF INPUTS HAVE ERROR
		else {
			//if it's the time
			if (isValid(startTimeSlot.getValue(),endTimeSlot.getValue())){
				speechText += " the input time are invalid";
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
