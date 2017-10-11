package uk.co.terminological.parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import uk.co.terminological.datatypes.Deferred;
import uk.co.terminological.parser.StateMachineException.HandledStateMachineException;

/**
 * Uses caching to consume the input of a state machine
 * @author rc538
 *
 * @param <X>
 */
public abstract class StateMachineIterator<X> extends StateMachineExecutor implements Iterator<Deferred<X,StateMachineException>> {

	Queue<Deferred<X,StateMachineException>> output = new LinkedList<>();
	
	public abstract State handle(Token token, Transition transition, State current, State end)
			throws HandledStateMachineException;

	public void push(X e) {
		output.add(Deferred.value(e));
	}
	
	public boolean hasNext() {
		try {
			while (output.isEmpty()) {
				this.execute();
			}
			return !output.isEmpty();
		} catch (StateMachineException.NoMoreInputException | StateMachineException.MachineStoppedException e) {
			return false;
		} catch (StateMachineException e) {
			// This will have been through the handler so represents only re-thrown cases of parser error. 
			output.add(Deferred.exception(e));
			return true;
		}
	}
	
	public Deferred<X,StateMachineException> next() {
		if (hasNext()) {
			return output.poll();
		} else {
			throw new NoSuchElementException();
		}
	}

}
