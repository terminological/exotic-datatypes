/**
 * 
 */
package com.bmj.informatics.datatypes;

import java.util.ArrayList;


/**
 * @author RCHALLEN
 *
 */
public class FluentList<S> extends ArrayList<S> {
	
	private static final long serialVersionUID = 1L;
	
	@SafeVarargs
	public static <T> FluentList<T> create(T... args) {
		FluentList<T> out = new FluentList<T>();
		for (T arg: args) {
			out.add(arg);
		}
		return out;
	}
	
	public static <T> FluentList<T> create(Class<T> type) {
		FluentList<T> out = new FluentList<T>();
		return out;
	}
	
	public static <T> FluentList<T> with(T arg) {
		FluentList<T> out = new FluentList<T>();
		out.add(arg);
		return out;
	}
	
	public FluentList<S> and(S element) {
		this.add(element);
		return this;
	}
}