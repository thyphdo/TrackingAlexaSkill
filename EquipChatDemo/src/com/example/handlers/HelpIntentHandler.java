package com.example.handlers;
import java.util.Optional;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import static com.amazon.ask.request.Predicates.intentName;
import com.amazon.ask.model.Response;

public class HelpIntentHandler implements RequestHandler {

	/**
	 * Returns if the Alexa skill service's input matches with the intent this handler cover
	 *	
	 * @param input 
	 * @return whether the input's intent is AMAZON.HelpIntent 
	 */
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AMAZON.HelpIntent"));
	}
	
	/**
	 * Build an according response based on the input request 
	 * Correct message: 
	 * I am here to tell you about the equipment's information. 
	 * Say Initiate EquipHour with a time period if you want to track down cumulative machine hour. 
	 * Say something like Tell me about longboard to know more about the machine.
	 * Quickly check the status of equipment with a question like is longboard available.
	 * Want to know about the job site? Just ask how many machines are there at josbsite Gettysburg.
	 * Please read the user manual to know more about other functionalities of this skill!
	 * @Override
	 * @param input 
	 * @return a response builder with list of example utterances for each of intents
	 */
	public Optional<Response> handle(HandlerInput input) {
		String speechText = "I am here to tell you about the equipment's information." + 
	" Say Initiate EquipHour with a time period if you want to track down cumulative machine hour."
	+ " Say something like Tell me about longboard to know more about the machine." +
	" Quickly check the status of equipment with a question like is longboard available."
	+ " Want to know about the job site? Just ask how many machines are there at josbsite Gettysburg." +
	 " Please read the user manual to know more about other functionalities of this skill!";
		return input.getResponseBuilder()
				.withSpeech(speechText)
				.withSimpleCard("EquipSession", speechText)
				.withReprompt(speechText)
				.build();
	}
}