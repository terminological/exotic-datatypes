package uk.co.terminological.parser;

import java.util.function.Predicate;

/**
 * The Token stores both the 
 * @author rc538
 *
 */
interface Token {
	
	char[] get();
	boolean matched(String s);

	static Predicate<Token> isType(Class<? extends Token> subtype) {
		return new Predicate<Token>() {
			@Override
			public boolean test(Token t) {
				return subtype.isInstance(t);
			}
		};
	}
	
	@SafeVarargs
	static Predicate<Token> isOneOf(Class<? extends Token>... subtypes) {
		return new Predicate<Token>() {
			@Override
			public boolean test(Token t) {
				for (Class<? extends Token> subtype: subtypes) {
					if (subtype.isInstance(t)) return true;
				}
				return false;
			}
		};
	}
}