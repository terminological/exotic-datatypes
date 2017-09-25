package uk.co.terminological.parser;

import uk.co.terminological.parser.Tokens.CharToken;
import uk.co.terminological.parser.Tokens.EOF;

/**
 * Matchers consume the characters or tokens given to it. 
 * If they partially match the internal rules for the pattern of the characters.
 * The matcher has an internal state returned by state():
 * <li> Starts in SLEEPING
 * <li> If matcher partially matches input:
 * (1) consume() will have returned true 
 * (2) matcher will be in IN_PROGRESS
 * (3) matcher will be waiting for more input before it can determine final state  
 * <li> If matcher cannot match input:
 * (1) consume() will have returned false
 * (2) it will be in FAIL state
 * <li> If matcher fully matched input sequence
 * (1) it will be in SUCCEEDED 
 * (2) consume() will have returned either true or false
 * <br> consume() will return false when the matcher has succeeded if the end condition of the matcher is based on 
 * a negative condition e.g. the regex for a positive integer [0-9][^0-9] which needs to be passed on to subsequent matches -
 * this is like a backtrack in regex. 
 */
interface Matcher extends Cloneable {

	
	
	public boolean consume(Object o) throws ClassCastException ;
	public Token token();
	public Match state();
	public Object clone();

	public static Matcher from(Token t) {
		return new OfToken(t);
	}

	public static enum Match {
		SUCCEEDED, 
		FAIL, 
		IN_PROGRESS
	}

	
	/**
	 * A simple matcher that executes an exact string match on a token
	 * @author rc538
	 *
	 */
	public static class OfToken implements Matcher {

		Token token;
		int pos = 0;
		Match state = null;

		OfToken(Token t) {
			token = t;
		}

		public Match state() {
			return state;
		}

		public boolean consume(Object o) {
			if (!(o instanceof Character)) {
				pos = 0;
				state = Match.FAIL;
				return false;
			}
			char c = ((Character) o).charValue();
			if (token.get().length > 0 && token.get()[pos] == c) {
				pos +=1;
				if (pos == token.get().length) {
					pos = 0;
					state = Match.SUCCEEDED;
				} else {
					state = Match.IN_PROGRESS;
				}
				return true;
			} else {
				pos = 0;
				state = Match.FAIL;
				return false;
			}
		}

		public Token token() {
			return token;
		}

		public Object clone() {
			return new OfToken(token);
		}


	}

	/**
	 * A very permissive matcher that matches any EOF token or single character.
	 * This is generally used to match any alternative sequence.
	 */
	public static class Default implements Matcher {

		Token t;
		Match state = null;
		Default() {}

		public Match state() {
			return state;
		}

		public boolean consume(Object o) {
			if (o instanceof EOF) {
				t = (EOF) o;
				state = Match.SUCCEEDED;
				return true;
			} else if (o instanceof Character) {
				t = new CharToken((Character) o);
				state = Match.SUCCEEDED;
				return true;
			} else {
				state = Match.FAIL;
				return false;
			}
		}

		public Token token() { return t;}
		
		public Object clone() {
			return new Default();
		}

	}
}