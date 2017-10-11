package uk.co.terminological.parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uk.co.terminological.parser.DelimitedParserMachine.States;
import uk.co.terminological.parser.StateMachineException.HandledStateMachineException;

public class DelimitedParserExecutor extends StateMachineIterator<List<String>> {

	LinkedList<String> line = new LinkedList<>();
	StringBuilder field = new StringBuilder();
	
	@Override
	public State handle(Token token, Transition transition, State current, State end)
			throws HandledStateMachineException {
		
		if (end instanceof State.Error) {
			
			//throw error for strict
			log.trace("Ignored error: "+((State.Error) end).getMessage()); 
			return current;
			
		} else if (end instanceof DelimitedParserMachine.States) {
			
			switch ((DelimitedParserMachine.States) end) {
			
			case FIELD_TERMINATED:
				line.add(field.toString());
				field = new StringBuilder();
				break;
			case FILE_TERMINATED:
				if (
					!current.equals(States.FIELD_TERMINATED) && 
					!current.equals(States.LINE_TERMINATED)
				) {
					line.add(field.toString());
					field = new StringBuilder();
				}
				if (!current.equals(States.LINE_TERMINATED)) {
					this.push(line);
					line = new LinkedList<>();
				}
				break;
			case LINE_TERMINATED:
				if (
						!current.equals(States.LINE_TERMINATED)
				) {
					line.add(field.toString());
					field = new StringBuilder();
				}
				this.push(line);
				line = new LinkedList<>();
				break;
			case READING_ENCLOSED:
			case READING_UNENCLOSED:
				field.append(token.get());
				break;
			case ESCAPING_ENCLOSED_OR_UNENCLOSING_FIELD:
			case ESCAPING_ENCLOSED:
			case UNENCLOSING_FIELD:
			case ENCLOSING_FIELD:
				break;
			default:
				break;
				
			}
			
			return end;
		} else {
			throw new HandledStateMachineException("Unexpected token type: "+token.getClass());
		}
	}

	
	DelimitedParserExecutor(StateMachine machine, Iterator<Token> input) {
		this.setInput(input);
		this.setMachine(machine);
		
	}
	
	DelimitedParserExecutor(StateMachine machine, Iterator<Token> input, ErrorHandler handler ) {
		this.setInput(input);
		this.setMachine(machine);
		this.setErrorHandler(handler);
	}
}
