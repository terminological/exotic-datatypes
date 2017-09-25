package uk.co.terminological.parser;

interface State {
	
	public static Error error(String message) {return new Error(message);}
	public static Stopped stopped() {return new Stopped();}
	
	
	public class Error implements State {
		String message;
		public Error(String message) {
			this.message = message;
		}
		public String getMessage() {return message;}
	}
	
	public class Stopped implements State {}
	
}