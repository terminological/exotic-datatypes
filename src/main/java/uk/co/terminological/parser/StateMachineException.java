package uk.co.terminological.parser;

public class StateMachineException extends Exception {
	public StateMachineException(String message, Exception e) {
		super(message,e);
	}
	public StateMachineException(String message) {
		super(message);
	}
	public StateMachineException() {}
	
	/**
	 * thrown by the DelimitedParser.readLine() method when an error in the csv file is encountered.
	 * The parser is relatively strict and will not tolerate spurious white space outside quoted fields for example.
	 * @author terminological
	 *
	 */
	public static class HandledStateMachineException extends StateMachineException {
		public HandledStateMachineException() {super();}
		public HandledStateMachineException(String message) {
			super(message);
		}
	}
	
	/**
	 * thrown by the DelimitedParser.readLine() method when the end of the file is reached
	 * @author terminological
	 *
	 */
	public static class MachineStoppedException extends StateMachineException {}
	
	/**
	 * thrown by the DelimitedParser.readLine() method when the end of the file is reached
	 * @author terminological
	 *
	 */
	public static class NoMoreInputException extends StateMachineException {}
}