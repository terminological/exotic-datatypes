package uk.co.terminological.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

class StateMachine {
	
	State start;
	Map<State,List<Transition>> transitions = new HashMap<State,List<Transition>>();
	
	
	public static StateMachine inState(State state) {
		StateMachine out = new StateMachine();
		out.start = state;
		return out;
	}
	
	/** Adds a transition configuration
	 * 
	 * @param in - the initial state
	 * @param message - the machine consumes messages in the form of Tokens
	 * @param end
	 * @return
	 */
	public StateMachine withTransition(State in, Predicate<Token> message, State end) {
		if (transitions.get(in) == null) transitions.put(in, new ArrayList<>());
		transitions.get(in).add(Transition.from(message, end));
		return this;
	}
	
	
	
	
}