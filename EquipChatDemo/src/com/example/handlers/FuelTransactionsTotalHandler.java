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

//*****Done integrating *****//

public class FuelTransactionsTotalHandler implements RequestHandler {
	
	public static final String START_TIME = "startTime";
	public static final String END_TIME = "endTime";

	//TransactionsTotalIntent
	//Slot types: 
	//startTime - Amazon.Date (yyyy-mm-dd)
	//endTime - Amazon.Date (yyyy-mm-dd)
	//Time can be yyyy-mm-dd; yyyy-Wxx (last week, this week); yyyy-mm (this month) 
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("FuelTransactionsTotalIntent"));
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

		//Check for validity of inputs, time processor return exactly two date 
		boolean isValidInput = (startTimeSlot != null);  //only startTime and equipName are mandatory

		//Set up response
		String speechText = "Empty";
		String repromptText = "Empty";
		boolean isAskResponse = false;
		
		if (isValidInput) {
			//Stringify all the slot 
			//String equipName = equipNameSlot.getValue();
			String startTime = startTimeSlot.getValue();
			String endTime = endTimeSlot == null ? null : endTimeSlot.getValue();

			//***Pre Process Dates****
			String[] start_end = dateHandler(startTime,endTime);

			//****// CALL API //****// 
			numFuelTransactions apicall = new numFuelTransactions();
			apicall.run(start_end[0],start_end[1]);
			String total = apicall.totalFuelTransactions() + "";

			//IF API RETURN POSITIVE MESSAGE 
			if (!total.equals("")){
				speechText = "There have been " + total + " transactions from " + start_end[0] + " to " + start_end[1];
			}
			//IF API RETURN AN ERROR
			else {
				speechText = 
						"There's an error from request to the API with start time: " 
								+ startTime + ", end time: " + endTime + ". please try again";
			}
		} //IF INPUTS HAVE ERROR
		else {
			speechText = "We cannot record a valid start time. Try again by saying"
					+ " how many fuel transaction are there from start time to end time.";
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
	
	//To check if a given date is in correct form of "yyyy-MM-dd"
		//source: http://www.java2s.com/Tutorial/Java/0120__Development/CheckifaStringisavaliddate.htm
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

		//To convert "yyyy-Wxx" or "yyyy-MM" into "yyyy-MM-dd"
		public static String convertDate(String date, boolean isStart) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			String converted = "";

			//***"yyyy-Wxx" format ***
			if (date.contains("W")) {
				int week =  Integer.parseInt(date.substring(6));
				int thisWeek = cal.get(Calendar.WEEK_OF_YEAR);

				if (week == thisWeek && !isStart) { 
					//handle case "to this week"
					converted = dateFormat.format(cal.getTime());
				}
				else { 
					//hand cases: from last week, since last week, from this week, since this week
					cal.set(Calendar.WEEK_OF_YEAR, week);     
					cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);  
					converted = dateFormat.format(cal.getTime()); 
				}
			} 
			//***"yyyy-MM" format ***
			else {
				int month = Integer.parseInt(date.substring(5));
				//handle cases: from month or since month
				if (isStart) {
					cal.set(Calendar.MONTH, month-1); //Set the month        
					cal.set(Calendar.DAY_OF_MONTH, 1);	//Set the date to very beginning date		
				}
				//handle cases: to month
				else {
					cal.set(Calendar.MONTH, month-1); //Set the month   
					int lastDay = cal.getActualMaximum(Calendar.DATE); //get the last day of that month (30-31-28)
					cal.set(Calendar.DAY_OF_MONTH, lastDay);
				}
				converted = dateFormat.format(cal.getTime());
			}
			return converted;
		}

		public static String[] dateHandler(String start, String end) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();

			//If "endDate" is not provided, "since___" is generated, today Date is assigned
			if (end == null) {
				end = dateFormat.format(cal.getTime());
			}
			//Check validity of start and end date yyyy-MM-dd
			if (!isValidFormatted(start)) {
				//convert if not in valid format
				start = convertDate(start,true);
			} else {
				start = checkValidYear(start);
			}
		
			if (!isValidFormatted(end)) {
				end = convertDate(end,false);
			} else {
				end = checkValidYear(end);
			}

			return new String[] {start, end};
		}
}
