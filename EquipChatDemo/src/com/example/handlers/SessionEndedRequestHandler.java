package com.example.handlers;

import java.util.Optional;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.SessionEndedRequest;
import static com.amazon.ask.request.Predicates.requestType;
public class SessionEndedRequestHandler implements RequestHandler  {

	/**
	 * Returns if the Alexa skill service's input matches with the intent this handler cover
	 *	
	 * @param input 
	 * @return whether the input's intent is SessionEndedRequest 
	 */
	public boolean canHandle(HandlerInput input) {
		return input.matches(requestType(SessionEndedRequest.class));
	}
	
	/**
	 * End the skill section
	 * @Override
	 * @param input 
	 * @return an ending response
	 */
	public Optional<Response> handle(HandlerInput input) {
		return input.getResponseBuilder().build();
	}
}