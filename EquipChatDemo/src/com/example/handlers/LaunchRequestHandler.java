package com.example.handlers;

import java.util.Optional;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import static com.amazon.ask.request.Predicates.requestType;

public class LaunchRequestHandler implements RequestHandler  {

	/**
	 * Returns if the Alexa skill service's input matches with the intent this handler cover
	 *	
	 * @param input 
	 * @return whether the input's intent is LaunchRequest 
	 */
	public boolean canHandle(HandlerInput input) {
		return input.matches(requestType(LaunchRequest.class));
	}

	/**
	 * Build an according response based on the input request 
	 * Message: Welcome to EquipChat, Let me help you to track and capture your equipment
	 * @Override
	 * @param input 
	 * @return a welcome message when user launch EquipChat Demo.
	 */
	public Optional<Response> handle(HandlerInput input) {
		String speechText = "Welcome to EquipChat, Let me help you to track and capture your equipment";
		return input.getResponseBuilder()
				.withSpeech(speechText)
				.withSimpleCard("EquipSession", speechText)
				.withReprompt(speechText)
				.build();
	}
}
