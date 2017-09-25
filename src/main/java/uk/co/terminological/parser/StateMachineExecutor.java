package uk.co.terminological.parser;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.terminological.parser.StateMachineException.*;

public abstract class StateMachineExecutor {

	static Logger log = LoggerFactory.getLogger(StateMachineExecutor.class);
	
	StateMachine machine;
	State current;
	Iterator<Token> input;
	ErrorHandler errorHandler = new ErrorHandler() {
		@Override
		public void handle(Exception e) throws StateMachineException {
			log.error("Ignoring: "+e.getMessage());
			log.debug(trace());
			//throw new StateMachineException(trace(), e);
		}
	};
	
	public abstract State handle(Token token, Transition transition, State current, State end) throws HandledStateMachineException;
	/*if (transition.end instanceof State.Error) {
		throw new HandledStateMachineException(((State.Error) transition.end).getMessage());
	}
	
	*/
	
	public StateMachineExecutor setMachine(StateMachine machine) {
		this.machine = machine;
		this.current = machine.start;
		return this;
	}
	
	public StateMachineExecutor setInput(Iterator<Token> input) {
		this.input = input;
		return this;
	}
	
	public StateMachineExecutor setErrorHandler(ErrorHandler handler) {
		this.errorHandler = handler;
		return this;
	}
	
	public State execute() throws StateMachineException {
		if (current instanceof State.Stopped) throw new MachineStoppedException();
		if (!input.hasNext()) throw new NoMoreInputException();
		Token token = input.next();
		errorHandler.capture(current, token);
		try {
			Transition transition = machine.transitions.get(current).stream() //sometimes gets executed with FILE_TERMINATED
					.filter(t -> t.message.test(token))
					.findFirst() //this allows state machine to be defined in order.
					.orElseThrow(() -> new HandledStateMachineException("Parser received invalid sequence"));
			current = handle(token, transition, current, transition.end);
		} catch (HandledStateMachineException e) {
			errorHandler.handle(e);
		}
		return current;
	}
	
	
	
	/**
	 * The base class to handle errors
	 * @author rc538
	 *
	 */
	public abstract static class ErrorHandler {
		public abstract void handle(Exception e) throws StateMachineException;
		
		LinkedHashMap<Integer, String> debug = new LinkedHashMap<Integer, String>() {
		     @Override
		     protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
		        return this.size() > 20;   
		     }
		};
		Integer position = 0;
		
		public void capture(State s, Token t) {
			debug.put(position, s.toString()+" > "+t.toString()+" > ");
			position+=t.get().length;
		}
		
		public String trace() {
			return debug.entrySet().stream().map(e -> e.getKey() + ": "+e.getValue()).collect(Collectors.joining("\n"));
		}
	}

}
