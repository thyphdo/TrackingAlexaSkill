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
import com.amazon.ask.model.dialog.DelegateDirective;
import com.amazon.ask.model.dialog.ElicitSlotDirective;
import com.amazon.ask.response.ResponseBuilder;

public class EquipHourHandler implements RequestHandler {

	/**
	 * Name of the slot provided by the Alexa Skill Service
	 * equipName - equipment ID whose the user wants to check the CMH of
	 */
	public static final String EQUIP_NAME = "equipName";
	
	/**
	 * Name of the slot provided by the Alexa Skill Service
	 * startTime - start time of period where the user wants to check the CMH of the machine
	 */
	public static final String START_TIME = "startTime";
	
	/**
	 * Name of the slot provided by the Alexa Skill Service
	 * endTime - end time of period where the user wants to check the CMH of the machine
	 */
	public static final String END_TIME = "endTime";

	/**
	 * Returns if the Alexa skill service's input matches with the intent this handler cover
	 *	
	 * @param input 
	 * @return whether the input's intent is EquipHourIntent 
	 */
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("EquipHourIntent"));
	}

	/**
	 * Build an according response based on the input request. 
	 * 		If the dialogue's status is in progress: prompt for the equipment name 
	 * 		If the dialogue's status is completed: check the require slots and out the applicable responses
	 * 
	 * Correct messages: 1. Please provide an equipment name
	 * 					 2. X has been running for Y hours from startTime to endTime or X has not been working at all from 
	 * 						startTime to endTime
	 * Incorrect messages: Your equipment may not exist or have not been working from startTime to endTime
	 * Reprompt message: Please try again by saying generate EquipHour from a start time to an end time
	 *
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
				speechText = "Your equipment may not exist or have not been working from " + startTime + " to " + endTime;
				repromptText = "Please try again by saying something like generate EquipHour from yesterday to today";
			}
			//IF THE EQUIPMENT WAS WORKING 
			else if (CMH != 0){
				speechText = equipName + " has been running for " + CMH + " hours from " + startTime + " to " + endTime;
				repromptText = "Anything else?";

			}
			//IF API RETURN AN ERROR
			else {
				speechText = equipName + " was not working at all from " + startTime + " to " + endTime;
				repromptText = "Please try again by saying generate EquipHour from yesterday to today";
			}

			//Building the response
			ResponseBuilder responseBuilder = input.getResponseBuilder();

			responseBuilder.withSimpleCard("EquipSession", speechText)
			.withSpeech(speechText)
			.withShouldEndSession(false)
			.withReprompt(repromptText);

			return responseBuilder.build();
		}
	}

	/**
	 * Take in the startDate and endDate from the VUI, transform accordingly and 
	 * return the startDate and endDate in the format of "yyyy-MM-dd"
	 * @param String - start: start date
	 * 		  String - end: end date (maybe null if user use "since")
	 * @return The start and end date string representation of "yyyy-MM-dd"
	 */
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
	
	/**
	 * Returns if a date string representation is yyyy-MM-dd
	 * Source: http://www.java2s.com/Tutorial/Java/0120__Development/CheckifaStringisavaliddate.html
	 * @param String - date string representation from VUI 
	 * @return whether the date is of format yyyy-MM-dd
	 */
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

	/**
	 * Returns a correct year in the form of "yyyy-MM-dd" for string representation of the given date
	 * Handle the Years problem from Alexa's VUI (August 10th always yields August 10th year + 1). 
	 * If the user's utterance does not specify a year, Alexa defaults to the year of the current date.
	 * @param String - date string representation from VUI  
	 * @return The date string representation with current year
	 */
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

	/**
	 * Returns a correct form of date yyyy-MM-dd for string representation of the given date
	 * Handle the case "yyyy-Wxx", "yyyy-MM"
	 * @param String - date string representation from VUI 
	 * 		  boolean - isStart: whether the input is the startTime 
	 * @return The date string representation of "yyyy-MM-dd"
	 */
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
