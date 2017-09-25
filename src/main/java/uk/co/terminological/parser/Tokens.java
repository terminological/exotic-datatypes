package uk.co.terminological.parser;

public class Tokens {

	public static class CharToken implements Token {
		char token;
		public CharToken(char in) {token = in;}
		
		public char[] get() {return new char[] {token};}
		public boolean matched(String s) {
			return s.toCharArray().length == 1 && s.toCharArray()[0] == token;
		}
		public String toString() {return "["+token+"]";}
	}
	
	public static class CharSequenceToken implements Token {
		char[] token;
		public CharSequenceToken(String s) {token = s.toCharArray();}
		
		public char[] get() { return token; }
		public boolean matched(String s) {return s.toCharArray() == token;}
		public String toString() {return "["+token.toString()+"]";}
	}
	
	public static class EOF implements Token {

		public char[] get() {return new char[] {};}
		public boolean matched(String s) {return false;}
		public String toString() {return "[EOF]\n\n";}

	};
	
	public static class EOL extends CharSequenceToken {
		public EOL(String s) {super(s);}
		public String toString() {return "[EOL]\n";}
	}
	
	
	public static class SEP extends CharSequenceToken {
		public SEP(String s) {super(s);}
		public String toString() {return "[__SEP__]";}
	}
	
	public static class ENC  extends CharSequenceToken {
		public ENC(String s) {super(s);}
		public String toString() {return "[ENC]";}
	}
	
	public static class ESC  extends CharSequenceToken {
		public ESC(String s) {super(s);}
		public String toString() {return "[ESC]";}
	}
	
	public static class ENC_ESC  extends CharSequenceToken {
		public ENC_ESC(String s) {super(s);}
		public String toString() {return "[ENC_ESC]";}
	}
}
