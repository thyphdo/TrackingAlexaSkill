package com.main;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import com.example.handlers.CancelandStopIntentHandler;
import com.example.handlers.EquipInfoDemoHandler;
import com.example.handlers.HelpIntentHandler;
import com.example.handlers.LaunchRequestHandler;
import com.example.handlers.SessionEndedRequestHandler;

public class EquipChatStreamHandler extends SkillStreamHandler {

	private static Skill getSkill() {
		return Skills.standard()
				.addRequestHandlers(new EquipInfoDemoHandler(),new HelpIntentHandler(),new LaunchRequestHandler(),
						new SessionEndedRequestHandler(), new CancelandStopIntentHandler())
				.withSkillId("amzn1.ask.skill.fdba1d4c-26a1-47ca-897c-9190a7c76f8c")
				.build();
	}
	
	public EquipChatStreamHandler() {
		super(getSkill());
	}
}