package uk.co.terminological.datatypes;

import java.util.function.Function;

/**
 * also see https://github.com/diffplug/durian/blob/v2.0/src/com/diffplug/common/base/Errors.java
 * @author terminological
 *
 */
public class StreamExceptions {

	@FunctionalInterface
	public static interface FunctionWithException<T, R, E extends Exception> {

		R apply(T t) throws E;
	}

	public static <T, R, E extends Exception> Function<T, R> rethrow(FunctionWithException<T, R, E> fe) {
		return arg -> {
			try {
				return fe.apply(arg);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	
	//TODO: Other functional interfaces.
}
