package uk.co.terminological.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A 0..* to 0..* map where key may have multiple values and values multiple keys
 * this is not a super efficient implementation!
 * @author terminological
 *
 * @param <K>
 * @param <V>
 */
public class CrossMap<K extends Object, V extends Object> implements Collection<Tuple<K,V>>, Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Tuple<String,String> header;
	private HashSet<Tuple<K,V>> data;
	private HashSet<V> nullKeys;
	private HashSet<K> nullValues;
	
	
	
	public CrossMap() {
		data = new HashSet<Tuple<K,V>>();
		nullKeys = new HashSet<V>();
		nullValues = new HashSet<K>();
		header = null;
	}
	
	public void setHeader(String s, String t) {header = new Tuple<String,String>(s,t);}
	public Tuple<String,String> getHeader() {return header;}
	
	public boolean add(Tuple<K, V> e) {return put(e.getFirst(),e.getSecond());}
	public boolean add(K k, V v) {return put(k,v);}
	public boolean addAll(Collection<? extends Tuple<K, V>> c) {return putAll(c);}
	public void clear() {
		data.clear();
		nullKeys.clear();
		nullValues.clear();}
	public boolean contains(Object o) {return data.contains(o);}
	public boolean containsAll(Collection<?> c) {return data.containsAll(c);}
	public boolean isEmpty() {return data.isEmpty();}
	public Iterator<Tuple<K, V>> iterator() {return data.iterator();}
	public boolean remove(Object o) {return data.remove(o);}
	public boolean removeAll(Collection<?> c) {return data.removeAll(c);}
	public boolean retainAll(Collection<?> c) {return data.retainAll(c);}
	public int size() {return data.size();}
	public Object[] toArray() {return data.toArray();}
	public <T> T[] toArray(T[] a) {return data.toArray(a);}
	
	public boolean containsKey(K key) {
		for (Tuple<K,V> entry: data) {
			if (entry.firstEquals(key) && null != entry.getSecond()) return true;
		}
		return false;
	}
	
	public boolean containsValue(V value) {
		for (Tuple<K,V> entry: data) {
			if (entry.secondEquals(value) && null != entry.getFirst()) return true;
		}
		return false;
	}

	public Collection<V> getValues(K key) {
		if (key == null) return nullKeys;
		ArrayList<V> out = new ArrayList<V>();
		for (Tuple<K,V> entry: data) {
			if (entry.firstEquals(key) && null != entry.getSecond()) out.add(entry.getSecond());
		}
		return out;
	}
	
	public Collection<K> getKeys(V value) {
		if (value == null) return nullValues;
		ArrayList<K> out = new ArrayList<K>();
		for (Tuple<K,V> entry: data) {
			if (entry.secondEquals(value) && null != entry.getFirst()) out.add(entry.getFirst());
		}
		return out;
	}
	
	public Set<K> keySet() {
		HashSet<K> out = new HashSet<K>();
		for (Tuple<K,V> entry: data) {
			out.add(entry.getFirst());
		}
		out.addAll(nullValues);
		return out;
	}
	
	public Set<V> valueSet() {
		HashSet<V> out = new HashSet<V>();
		for (Tuple<K,V> entry: data) {
			out.add(entry.getSecond());
		}
		out.addAll(nullKeys);
		return out;
	}
	
	public boolean put(K key, V value) {
		if (key == null && value == null) {
			return false;
		} else if (key == null) {
			nullKeys.add(value);
		} else if (value == null) {
			nullValues.add(key);
		} else {
			data.add(new Tuple<K,V>(key,value));
		}
		return true;
	}
	
	public boolean putAll(Collection<? extends Tuple<K, V>> xmap) {
		for (Tuple<K,V> entry: xmap) {
			if (!data.add((Tuple<K,V>) entry)) return false;;
		}
		return true;
	}
	
	public Collection<V> getMultipleValues(K key) throws NoMatchException {
		if (key == null) {
			if (nullKeys.isEmpty()) throw new NoMatchException();
			return nullKeys;
		}
		Collection<V> values = getValues(key);
		if (values.isEmpty()) throw new NoMatchException();
		return values;
	}
	
	public Collection<K> getMultipleKeys(V value) throws NoMatchException {
		if (value == null) {
			if (nullValues.isEmpty()) throw new NoMatchException();
			return nullValues;
		}
		Collection<K> keys = getKeys(value);
		if (keys.isEmpty()) throw new NoMatchException();
		return keys;
	}
	
	public V getSingleValue(K key) throws NoMatchException, MultipleMatchException {
		Collection<V> values = getMultipleValues(key);
		if (values.size() > 1) throw new MultipleMatchException();
		return values.iterator().next();
	}
	
	public K getSingleKey(V value) throws NoMatchException, MultipleMatchException {
		Collection<K> keys = getMultipleKeys(value);
		if (keys.size() > 1) throw new MultipleMatchException();
		return keys.iterator().next();
	}
}
