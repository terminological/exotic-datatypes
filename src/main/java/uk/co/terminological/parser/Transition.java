package uk.co.terminological.parser;

import java.util.function.Predicate;

class Transition {
	
	State end;
	Predicate<Token> message;
	
	static Transition from(Predicate<Token> message, State end) {
		Transition out = new Transition();
		out.end = end;
		out.message = message;
		return out;
	}

}