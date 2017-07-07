/**
 * 
 */
package com.bmj.informatics.datatypes;

import java.util.HashSet;
import java.util.Iterator;

/**
 * @author RCHALLEN
 *
 */
public class DistinctValuesIterator<T extends Object> implements IteratorCollector<T>  {

	private HashSet<T> store;
	private Iterator<T> storeIt;
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		if (storeIt==null) iterator();
		return storeIt.hasNext();
	}

	public static <S extends Object> DistinctValuesIterator<S> create(Iterable<S> it) {
		DistinctValuesIterator<S> out = new DistinctValuesIterator<S>();
		out.add(it);
		return out;
	}
	
	public static <S extends Object> DistinctValuesIterator<S> create(Iterator<S> it) {
		DistinctValuesIterator<S> out = new DistinctValuesIterator<S>();
		out.add(it);
		return out;
	}
	
	public DistinctValuesIterator() {
		this.store = new HashSet<T>();
	}
	
	public IteratorCollector<T> add(Iterable<? extends T> it) {
		for (T t: it) {
			store.add(t);
		}
		return this;
	}
	
	public IteratorCollector<T> add(Iterator<? extends T> it) {
		while (it.hasNext()) {
			T t = it.next();
			store.add(t);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public T next() {
		if (storeIt==null) iterator();
		return storeIt.next();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		if (storeIt==null) iterator();
		storeIt.remove();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		storeIt = store.iterator();
		return this;
	}

}
