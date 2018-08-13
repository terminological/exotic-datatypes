/**
 * 
 */
package uk.co.terminological.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


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
	
	public static <T> FluentList<T> create() {
		FluentList<T> out = new FluentList<T>();
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
	
	@SafeVarargs
	public static <T> FluentList<T> create(Collection<T>... args) {
		FluentList<T> out = new FluentList<T>();
		for (Collection<T> arg: args) {
			out.addAll(arg);
		}
		return out;
	}
	
	public static <T> FluentList<T> empty() {
		return new FluentList<T>();
	}
	
	public FluentList<S> and(S element) {
		this.add(element);
		return this;
	}
	
	public FluentList<S> append(List<S> element) {
		if (element != null) this.addAll(element);
		return this;
	}

	
}