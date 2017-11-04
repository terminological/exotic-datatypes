package uk.co.terminological.parser;

public class ParserException extends Exception {

	public ParserException(String message, Exception e) {
		super(message,e);
	}

	public ParserException(String message) {
		super(message);
	}
	
	public ParserException() {}
}
