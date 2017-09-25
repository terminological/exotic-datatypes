package uk.co.terminological.parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import uk.co.terminological.parser.DelimitedParser.States;
import uk.co.terminological.parser.StateMachineException.HandledStateMachineException;

public class DelimitedParserExecutor extends StateMachineExecutor {

	Queue<List<String>> lines = new LinkedList<>();
	LinkedList<String> line = new LinkedList<>();
	StringBuilder field = new StringBuilder();
	
	@Override
	public State handle(Token token, Transition transition, State current, State end)
			throws HandledStateMachineException {
		
		if (end instanceof State.Error) {
			
			//throw error for strict
			log.trace("Ignored error: "+((State.Error) end).getMessage()); 
			return current;
			
		} else if (end instanceof DelimitedParser.States) {
			
			switch ((DelimitedParser.States) current) {
			
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
					lines.add(line);
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
				lines.add(line);
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

	
	DelimitedParserExecutor() {
		//Iterator<Token> tokenProvider = lex.iterator(reader);
		
	}
	
	/**
	 * The readLine method parses the next line from the delimited stream.
	 * @return A list of strings representing the fields in a single line of the file, missing values will
	 * be represented as empty strings.
	 * @throws StateMachineException 
	 * 
	 */
	public List<String> readLine() throws StateMachineException { 
		while(lines.isEmpty()) {
			this.execute();
		}
		return lines.poll();
	}
	
	public Iterator<List<String>> readLines() {
		return new Iterator<List<String>>() {
			public boolean hasNext() {
				try {
					while (lines.isEmpty()) {
						DelimitedParserExecutor.this.execute();
					}
					return !lines.isEmpty();
				} catch (StateMachineException e) {
					return false;
				}
			}
			public List<String> next() {
				if (hasNext()) {
					return lines.poll();
				} else {
					throw new NoSuchElementException();
				}
			}
		};
	}

	/*
	public Stream<List<String>> stream() {
		return StreamSupport.stream(readLines().spliterator(),false);
	}
	 */
}
