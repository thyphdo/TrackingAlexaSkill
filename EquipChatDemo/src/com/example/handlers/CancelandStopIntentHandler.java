package com.example.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CancelandStopIntentHandler implements RequestHandler {
	
	/**
	 * Returns if the Alexa skill service's input matches with the intent this handler cover
	 *	
	 * @param input 
	 * @return whether the input's intent is AMAZON.StopIntent or AMAZON.CancelIntent
	 */
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.StopIntent").or(intentName("AMAZON.CancelIntent")));
    }

	/**
	 * Build an according response based on the input request 
	 * Message: Goodbye, have a nice day
	 * @Override
	 * @param input 
	 * @return a farewell messge when user wants to end EquipChat Demo.
	 */
    @Override
    public Optional<Response> handle(HandlerInput input) {
        return input.getResponseBuilder()
                .withSpeech("Goodbye, have a nice day!")
                .withSimpleCard("EquipSession", "Goodbye, have a nice day!")
                .build();
    }
}