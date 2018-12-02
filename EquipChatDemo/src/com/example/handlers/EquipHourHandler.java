package com.example.handlers;

import static com.amazon.ask.request.Predicates.intentName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.DialogState;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.model.SlotConfirmationStatus;
import com.amazon.ask.model.Directive;
import com.amazon.ask.model.dialog.DelegateDirective;
import com.amazon.ask.model.dialog.DelegateDirective.Builder;
import com.amazon.ask.model.dialog.ElicitSlotDirective;
import com.amazon.ask.response.ResponseBuilder;

//import com.amazon.speech.speechlet.Directive;
//import com.amazon.speech.speechlet.SpeechletResponse;
//import com.amazon.speech.speechlet.dialog.directives.DelegateDirective;
//import com.amazon.speech.speechlet.dialog.directives.DialogIntent;
//import com.amazon.speech.speechlet.dialog.directives.DialogSlot;


//***** Done dealing with dates, yet with equipName*****//

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
	//	public static void main(String[] arg) {
	//		
	//		
	//		//***Create new slots***
	//		Slot.Builder startSlotBuilder = Slot.builder().withName(START_TIME).withValue("11-01-2018");
	//		Slot startTimeSlot = startSlotBuilder.build();
	//		
	//		Slot.Builder endSlotBuilder = Slot.builder().withName(END_TIME).withValue("11-20-2018");
	//		Slot endTimeSlot = endSlotBuilder.build();
	//		
	//		Map<String,Slot> updateSlots = new HashMap<String,Slot>();  slots.put(START_TIME, startTimeSlot); slots.put(END_TIME, endTimeSlot);
	//		
	//		//***Create new intents***
	//		Intent.Builder intentBuilder = Intent.builder();
	//		Intent updateIntent = intentBuilder.withSlots(updateSlots).build();
	//
	//		//***Create new delegate directive***
	//		DelegateDirective.Builder ddBuilder = DelegateDirective.builder().withUpdatedIntent(updateIntent);
	//		DelegateDirective dd = ddBuilder.build();
	//		
	//		//***Create new response builder***
	//		ResponseBuilder responseBuilder = input.getResponseBuilder();
	//
	//		responseBuilder.addDirective(dd).withSimpleCard("EquipSession", "What equipment do you want know about")
	//		.withSpeech("What equipment do you want know about")
	//		.withShouldEndSession(false);
	//
	//
	//		responseBuilder.build();
	//	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		//Capture input as JSON file 
		Request request = input.getRequestEnvelope().getRequest();
		IntentRequest intentRequest = (IntentRequest) request;

		// The intentRequest variable here is the IntentRequest object sent to the skill.
		if (intentRequest.getDialogState() == DialogState.STARTED){
			// Pre-fill slots: update the intent object with slot values for which
			// you have defaults, then return Dialog.Delegate with this updated intent
			// in the updatedIntent property.

			//UPDATE TIME SLOTS 

			Intent intent = intentRequest.getIntent();
			//Get slots from intent object as a map
			Map<String, Slot> slots = intent.getSlots();

			// Get the input slots
			Slot startTimeSlot = slots.get(START_TIME);
			Slot endTimeSlot = slots.get(END_TIME);

			String startTime = startTimeSlot.getValue();
			String endTime = endTimeSlot.getValue(); //== null ? null : endTimeSlot.getValue();

			//***Pre Process Dates****
			String[] start_end = dateHandler(startTime,endTime);

			//***Create new slots***
			Slot.Builder startSlotBuilder = Slot.builder().withName(START_TIME).withValue(start_end[0]);
			startTimeSlot = startSlotBuilder.build();

			Slot.Builder endSlotBuilder = Slot.builder().withName(END_TIME).withValue(start_end[1]);
			endTimeSlot = endSlotBuilder.build();

			Slot.Builder nameSlotBuilder = Slot.builder().withName(EQUIP_NAME).withConfirmationStatus(SlotConfirmationStatus.NONE);
			Slot equipNameSlot = nameSlotBuilder.build();

			Map<String,Slot> updateSlots = new HashMap<String,Slot>();  
			updateSlots.put(START_TIME, startTimeSlot); updateSlots.put(END_TIME, endTimeSlot); updateSlots.put(EQUIP_NAME, equipNameSlot);

			//***Create new intents***
			Intent.Builder intentBuilder = Intent.builder();
			Intent updateIntent = intentBuilder.withSlots(updateSlots).withName("EquipHourIntent").build();

			//***Create new delegate directive***
			//			DelegateDirective.Builder ddBuilder = DelegateDirective.builder().withUpdatedIntent(updateIntent);
			//			DelegateDirective dd = ddBuilder.build();

			//***Create new elicit directive
			ElicitSlotDirective.Builder edBuilder = ElicitSlotDirective.builder().withSlotToElicit(EQUIP_NAME).withUpdatedIntent(updateIntent);
			ElicitSlotDirective ed = edBuilder.build();


			//***Create new response builder***
			ResponseBuilder responseBuilder = input.getResponseBuilder();

			responseBuilder.addDirective(ed).withSimpleCard("EquipSession", "What equipment do you want know about")
			.withSpeech("Please provide an equipment name")
			.withShouldEndSession(false);


			return responseBuilder.build();

		} else if (intentRequest.getDialogState() != DialogState.IN_PROGRESS){
			// return a Dialog.Delegate directive with no updatedIntent property.
			//Generate intent object
			Intent intent = intentRequest.getIntent();

			DelegateDirective.Builder ddBuilder = DelegateDirective.builder().withUpdatedIntent(intent);
			DelegateDirective dd = ddBuilder.build();

			ResponseBuilder responseBuilder = input.getResponseBuilder();

			responseBuilder.withSpeech("What equipment do you want know about")
			.withShouldEndSession(false).addDirective(dd);


			return responseBuilder.build();
		} else {
			// Dialog is now complete and all required slots should be filled,
			// so call your normal intent handler. 
			//Generate intent object
			Intent intent = intentRequest.getIntent();
			//Get slots from intent object as a map
			Map<String, Slot> slots = intent.getSlots();

			// Get the input slots
			Slot equipNameSlot = slots.get(EQUIP_NAME);
			Slot startTimeSlot = slots.get(START_TIME);
			Slot endTimeSlot = slots.get(END_TIME);

			//Set up response
			String speechText = "Empty";
			String repromptText = "Empty";
			boolean isAskResponse = false;

			//Stringify all the slot 
			String equipName = equipNameSlot.getValue();
			String startTime = startTimeSlot.getValue();
			String endTime = endTimeSlot.getValue();

			//**** if startTime is not provided => failed
			//****// CALL API //****// 
			EquipHour apicall = new EquipHour();
			apicall.run(equipName, startTime, endTime);
			int CMH = apicall.cmhChange();
			
			//IF API CANNOT FIND THE EQUIPMENT NAME
			if (CMH == -1) {
				speechText = "Your equipment may not exist or may have not been valid from " + startTime + " to " + endTime;
			}
			//IF THE EQUIPMENT WAS WORKING 
			else if (CMH != 0){
				speechText = equipName + " has been running for " + CMH + " hours from " + startTime + " to " + endTime;
			}
			//IF API RETURN AN ERROR
			else {
				speechText = equipName + " was not working at all from " + startTime + " to " + endTime;

			}
			repromptText = "Anything else?";


			//Building the response
			ResponseBuilder responseBuilder = input.getResponseBuilder();

			responseBuilder.withSimpleCard("EquipSession", speechText)
			.withSpeech(speechText)
			.withShouldEndSession(false);

			//			if (isAskResponse) {
			//				responseBuilder.withShouldEndSession(false)
			//				.withReprompt(repromptText);
			//			}
			return responseBuilder.build();
		}
	}


	//	public static void main (String[] argrs) {
	////		since last week (W45), since this week (W46), to this week (W46,false) 
	////		since last month, since this month, to this month
	//		String d = "2018-11"; 
	//		boolean isStart = false;
	//		System.out.println(convertDate(d,isStart));
	//		String[] s = dateHandler("2018-W45","2018-W46");
	//		System.out.println(s[0] + s[1]);
	//	}

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

	//	@Override
	//	public Optional<Response> handle(HandlerInput input) {
	//
	//		//Capture input as JSON file 
	//		Request request = input.getRequestEnvelope().getRequest();
	//		IntentRequest intentRequest = (IntentRequest) request;
	//		//Generate intent object
	//		Intent intent = intentRequest.getIntent();
	//		//Get slots from intent object as a map
	//		Map<String, Slot> slots = intent.getSlots();
	//
	//		// Get the input slots
	//		Slot equipNameSlot = slots.get(EQUIP_NAME);
	//		Slot startTimeSlot = slots.get(START_TIME);
	//		Slot endTimeSlot = slots.get(END_TIME);
	//
	//
	//		//Check for validity of inputs 
	//		boolean isValidInput = (startTimeSlot != null); //only startTime and equipName are mandatory
	//
	//		//Set up response
	//		String speechText = "Empty";
	//		String repromptText = "Empty";
	//		boolean isAskResponse = false;
	//
	//
	//		if (isValidInput) {
	//			//Stringify all the slot 
	//			//String equipName = equipNameSlot.getValue();
	//			String startTime = startTimeSlot.getValue();
	//			String endTime = endTimeSlot == null ? null : endTimeSlot.getValue();
	//
	//			//***Pre Process Dates****
	//			String[] start_end = dateHandler(startTime,endTime);
	//			
	//			//**** if startTime is not provided => failed
	//			//****// CALL API //****// 
	//			String CMH = "20";
	//
	//			//IF API RETURN POSITIVE MESSAGE 
	//			if (CMH == "20"){
	//				speechText = " X has been running for " + CMH + " hours from " + start_end[0] + " to " + start_end[1];
	//			}
	//			//IF API RETURN AN ERROR
	//			else {
	//				speechText = 
	//						"There's an error from request to the API with start time: " 
	//								+ start_end[0] + ", end time: " + start_end[1] + ". please try again";
	//			}
	//			repromptText = "Anything else?";
	//		} //IF INPUTS HAVE ERROR
	//		else {			
	//			speechText = "There is a problem with you time input, please try again";
	//			
	//			//			if (isValid(startTimeSlot.getValue(),endTimeSlot.getValue())){
	//			//				speechText += " the input time are invalid";
	//			//			}
	//			isAskResponse = true;
	//			repromptText = "Anything else?";
	//		}
	//
	//		//Building the response
	//		ResponseBuilder responseBuilder = input.getResponseBuilder();
	//
	//		responseBuilder.withSimpleCard("EquipSession", speechText)
	//		.withSpeech(speechText)
	//		.withShouldEndSession(false);
	//
	//		if (isAskResponse) {
	//			responseBuilder.withShouldEndSession(false)
	//			.withReprompt(repromptText);
	//		}
	//
	//		return responseBuilder.build();
	//	}

	//Test Case***** 
	//since last week  - since "yyyy-Wxx"   (start)
	//since yesterday/date - since "yyyy-mm-dd" (start)
	//since this week - since "yyyy-Wxx"  (start)
	//from yesterday to today - from "yyyy-mm-dd" to "yyyy-mm-dd" (start-end)
	//from last week to this week - from "yyyy-Wxx" to "yyyy-Wxx" (start-end)
	//from date to date  - from "yyyy-mm-dd" to "yyyy-mm-dd"
	//from this month to this month - from "yyyy-mm" to "yyyy-mm"

	//since last week (W45), since this week (W46), to this week (W46,false) 
	//since last month, since this month, to this month
}
