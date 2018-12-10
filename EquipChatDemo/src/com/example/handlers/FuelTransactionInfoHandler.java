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

public class FuelTransactionInfoHandler implements RequestHandler {

	/**
	 * Name of the slot provided by the Alexa Skill Service
	 * startTime - start time of period where the user wants to check the list of 
	 * available fuel transactions
	 */
	public static final String START_TIME = "startTime";
	
	/**
	 * Name of the slot provided by the Alexa Skill Service
	 * endTime - end time of period where the user wants to check the list of 
	 * available fuel transactions
	 */
	public static final String END_TIME = "endTime";	
	
	/**
	 * Name of the slot provided by the Alexa Skill Service
	 * transactionID - the numeric value of the transaction ID
	 */
	public static final String TRANSACTION_ID = "transactionID";

	/**
	 * Returns if the Alexa skill service's input matches with the intent this handler cover
	 *	
	 * @param input 
	 * @return whether the input's intent is FuelTransactionInfoIntent 
	 */
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("FuelTransactionInfoIntent"));
	}

	/**
	 * Build an according response based on the input request 
	 * Correct messages: 
	 * 	1. ID provided: This transaction belongs to equipment X. Fuel type is Y with a volume of Z (unit of measurement) at jobsite J
	 * 	2. Time period provided: There are X fuel transactions within the provided period: equipName has one of transType with Volume UOM at jobsite...
	 * 
	 * Incorrect messages: The ID or time period you provided is invalid. ID only contains number. Please try again.
	 * Reprompt message: Try again by prompting the correct ID or say something like "Give me information about the transaction from yesterday to today"
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
		//Generate intent object
		Intent intent = intentRequest.getIntent();
		//Get slots from intent object as a map
		Map<String, Slot> slots = intent.getSlots();

		// Get the input slots
		Slot transactionIDSlot = slots.get(TRANSACTION_ID);
		Slot startTimeSlot = slots.get(START_TIME);
		Slot endTimeSlot = slots.get(END_TIME);

		//Check for validity of inputs, intent must have transaction ID or start time
		boolean isValidInput = (transactionIDSlot.getValue() != null || startTimeSlot.getValue() != null);

		//Set up response
		String speechText = "Empty";
		String repromptText = "Empty";
		boolean isAskResponse = false;

		if (isValidInput) {
			//Decide which slot user provides (ID or time)

			//***ID***
			if (transactionIDSlot.getValue() != null) {
				int id = Integer.parseInt(transactionIDSlot.getValue());

				//****// CALL API //****// 
				FuelTransactions apicall = new FuelTransactions();
				apicall.run(id+"");
				String[] results = apicall.fuelInfo();

				//IF API RETURN AN ERROR
				if (results.length == 0) {
					speechText = 
							"There's an error from request to the API with ID " + id + " or there is no"
									+ "transaction under this ID. please try again";
				}
				else {
					String name = results[0];
					String type = results[1];
					String volume = results[2];
					String uom = results[3];
					String jobsite = results[4];

					//IF API RETURN POSITIVE MESSAGE 	
					speechText = "This transaction belongs to equipment " + name + ". Fuel type is " + type + " with a volume of " + volume + " " + uom + " at jobsite " +jobsite;
				}
			} 
			//***TIME***
			else {
				String startTime = startTimeSlot.getValue();
				String endTime = endTimeSlot.getValue(); //== null ? null : endTimeSlot.getValue();

				//***Pre Process Dates****
				String[] start_end = dateHandler(startTime,endTime);

				//**** CALL API ****// 
				FuelTransactions apicall = new FuelTransactions();
				apicall.run(start_end[0],start_end[1]);
				String[] results = apicall.listOfFuelInfo();

				//IF API RETURN AN ERROR
				if (results.length == 0) {
					speechText = 
							"There's an error from request to the API with start time " + start_end[0]+ " and end time "
									+ start_end[1] + " please try again";
				}//IF API RETURN POSITIVE MESSAGE 	
				else {
					String numTransaction = results.length + "";
					speechText = "There are " + numTransaction + " fuel transactions within the provided period. ";
					for (String trans : results) {
						String[] transInfo = trans.split("\\s+"); //0: name, 1: transaction type, 2: volume, 3: uom, 4: jobsite
						speechText += transInfo[0] + " has one of " + transInfo[1] + " with " + transInfo[2] 
								+ " " + transInfo[3]  + " at job site " + transInfo[4] + ". ";
					}
				}
			}
		} //IF INPUTS HAVE ERROR
		else {
			speechText = "The ID or time period you provided is invalid. ID only contains number. Please try again.";
		    repromptText = "Try again by prompting the correct ID or say something like give me information about the transaction from yesterday to today";
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
	//		String[] start_end = dateHandler("2018-01-01","2018-11-20");
	//		System.out.println(start_end[0] + "\t"+ start_end[1]);
	//	}
}
