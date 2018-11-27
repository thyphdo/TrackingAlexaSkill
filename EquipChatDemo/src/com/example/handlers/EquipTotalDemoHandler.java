package com.example.handlers;
import java.util.Collections;
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

import static com.amazon.ask.request.Predicates.intentName;

//Source: https://github.com/alexa/alexa-skills-kit-sdk-for-java
public class EquipTotalDemoHandler implements RequestHandler {

	public static final String JOBSITE_NAME = "jobSiteName";

	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("EquipTotalDemoHandler"));
	}

	public Optional<Response> handle(HandlerInput input) {

		Request request = input.getRequestEnvelope().getRequest();
		IntentRequest intentRequest = (IntentRequest) request;
		Intent intent = intentRequest.getIntent();
		Map<String, Slot> slots = intent.getSlots();

		// Get the jobSiteName slot
		Slot jobSiteSlot = slots.get(JOBSITE_NAME);

		String speechText, repromptText;
		boolean isAskResponse = false;

		// Check for job sites name and create output to user.
		if (jobSiteSlot != null) {
			// Store the job site name in the Session and create response.
			String jobSiteName = jobSiteSlot.getValue().toLowerCase();
			NumOnJobsite info = new NumOnJobsite();
			info.run(jobSiteName);

			//If the equipment exists (long board) 
			if (info.jobsiteExists()){
				//Test
				String totalMachines = info.numOnSite();

				input.getAttributesManager().setSessionAttributes(Collections.singletonMap(JOBSITE_NAME,(Object) jobSiteName));
				
				if(totalMachines.equals("0")) {
					speechText =
							"There are no machines at " + jobSiteName;
				}
				else if(totalMachines.equals("1")) {
					speechText =
							"There is " + totalMachines + " machine at " + jobSiteName;
				}
				else {
					speechText =
							"There are " + totalMachines + " machines at " + jobSiteName;
				}
				

				repromptText =
						"You can ask me again by kindly saying, " + "tell me about the job site";
			}
			else {
				speechText =
						"Job Site " + jobSiteName + " is not valid, please try again";
				repromptText =
						"You can ask me again by kindly saying, " + "tell me about the job site";
			}

		} else {
			// Render an error since we don't know what the equipment's name is.
			speechText = 
					"I'm not sure what the job site's name is, please try again";
			repromptText = 
					"I don't think I get the job site's name. You can ask me again by kindly asking, "
							+ "tell me about the job site";
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