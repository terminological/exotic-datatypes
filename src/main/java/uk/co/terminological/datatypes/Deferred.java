package uk.co.terminological.datatypes;

public class Deferred<X,E extends Exception> {

	E exception;
	X value;
	
	public Deferred(E error) {
		exception = error;
	}

	public Deferred(X value) {
		this.value=value;
	}

	public static <Y,D extends Exception> Deferred<Y,D> exception(D error) {
		return new Deferred<Y,D>(error); 
	}
	
	public static <Y,D extends Exception> Deferred<Y,D> value(Y value) {
		return new Deferred<Y,D>(value); 
	}
	
	public X get() throws E {
		if (exception != null) throw exception;
		return value;
	}

	@SuppressWarnings("unchecked")
	public <Y> Deferred<Y, E> map(Operation<X,Y,E> mapper) {
		try {
			return Deferred.value(mapper.apply(this.get()));
		} catch (Exception ex) {
			return Deferred.exception((E) ex);
		}
	}
	
	public String toString() {
		if (exception != null) return exception.toString();
		else return value.toString();
	}
	
	public static interface Operation<X,Y,E extends Exception> {
		public Y apply(X x) throws E;
	}
}
