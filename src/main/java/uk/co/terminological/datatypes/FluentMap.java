/**
 * 
 */
package uk.co.terminological.datatypes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author RCHALLEN
 *
 */
public class FluentMap<K,V> extends HashMap<K,V> implements Iterable<Tuple<K,V>>, Map<K,V> {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static <K1,V1> FluentMap<K1,V1> create(Class<K1> keyClass,Class<V1> valueClass) {
		FluentMap<K1,V1> out = new FluentMap<K1,V1>();
		return out;
	}
	
	public static <K1,V1> FluentMap<K1,V1> with(K1 key,V1 value) {
		FluentMap<K1,V1> out = new FluentMap<K1,V1>();
		out.put(key, value);
		return out;
	}
	
	public FluentMap<K,V> and(K key,V value) {
		this.put(key, value);
		return this;
	}

	public static <K1,V1> FluentMap<K1,V1> create(HashMap<K1,V1> map) {
		FluentMap<K1,V1> out = new FluentMap<K1,V1>();
		out.putAll(map);
		return out;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Tuple<K, V>> iterator() {
		final Iterator<Entry<K, V>> setIt = this.entrySet().iterator();
		return new Iterator<Tuple<K, V>>() {

			@Override
			public boolean hasNext() {
				return setIt.hasNext();
			}

			@Override
			public Tuple<K, V> next() {
				Entry<K,V> tmp = setIt.next();
				return Tuple.create(tmp.getKey(), tmp.getValue());
			}

			@Override
			public void remove() {
				setIt.remove();
			}
			
		};
	}
	
}
