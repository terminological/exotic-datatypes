package uk.co.terminological.parser;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.terminological.parser.StateMachineException.*;

/**
 * Classes that overide state machine executor are responsible for either exposing their output as a stream or collating their  
 * @author rc538
 *
 */
public abstract class StateMachineExecutor {

	static Logger log = LoggerFactory.getLogger(StateMachineExecutor.class);
	
	StateMachine machine;
	State current;
	Iterator<Token> input;
	ErrorHandler errorHandler = ErrorHandler.DEBUG;
	
	/**
	 * Implementations of this method define how the state machine will emit its results based on possible transitions.
	 * The implementation may for example use the various start and end States and the Token to 
	 * either populate a cache as part of a stream implementation, or this may function more like a builder of an output object
	 * 
	 * @param token
	 * @param transition
	 * @param current
	 * @param end
	 * @return
	 * @throws HandledStateMachineException - Errors will be processed by the StateMachineExecutor.ErrorHandler.handle() method
	 */
	public abstract State handle(Token token, Transition transition, State current, State end) throws HandledStateMachineException;
	
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
	
	/**
	 * Implementations must call this method to trigger the processing of a single input of the state machine.
	 * This will result in a new state and a call to StateMachineExecutor.handle(). This can be used in a loop to read in the whole
	 * of the input and the calls to StateMachineExecutor.handle() can be used to collate the result object. 
	 * Alternatively a streaming implementation will cache the result somewhere when StateMachineExecutor.handle() is called and use a 
	 * hasNext() and next() method to call this StateMachineExecutor.execute() method.  
	 * @return
	 * @throws StateMachineException
	 */
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
	
	
	public static interface ErrorHandler {
		/**
		 * Called on the event of a StateMachineException
		 * @param e
		 * @throws StateMachineException
		 */
		public void handle(HandledStateMachineException e) throws StateMachineException;
		/**
		 * Called once before every attempted state transition, with the starting state of the machine and the token that initiated it.
		 * A minimal implementation might just log this, count the number of times (hence the position) or do nothing with it.  
		 * @param s - The State before the execution
		 * @param t - the input Token
		 */
		public void capture(State s, Token t);
	
	
	
	/**
	 * The base class to handle errors
	 * @author rc538
	 *
	 */
	public static ErrorHandler DEBUG = new ErrorHandler() {
		
		public void handle(HandledStateMachineException e) throws StateMachineException {
			log.error("Ignoring: "+e.getMessage());
			log.debug(trace());
		}
		
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
	};
	
	public static ErrorHandler STRICT = new ErrorHandler() {

		@Override
		public void handle(HandledStateMachineException e) throws StateMachineException {
			throw e;
		}

		public void capture(State s, Token t) {}
		
	};
	
	}

}
