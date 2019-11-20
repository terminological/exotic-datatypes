package uk.co.terminological.datatypes;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlTransient;

/**
 * A Java 8 friendly Entity-Attribute-Value map backed on hash maps.
 * @author terminological
 *
 * @param <E>
 * @param <A>
 * @param <V>
 */
public class EavMap<E,A,V> implements Serializable  {

	private static final long serialVersionUID = 1L;
	LinkedHashMap<E,LinkedHashMap<A,V>> map;
	@XmlTransient int attributes;

	public EavMap() {
		map = new LinkedHashMap<E,LinkedHashMap<A,V>>();
		attributes = 20;
	}
	
	public EavMap(int entities,int attributes) {
		map = new LinkedHashMap<E,LinkedHashMap<A,V>>(entities, 0.99F);
		this.attributes = attributes;
	}
	
	public static <E1,A1,V1> EavMap<E1,A1,V1> create(E1 entity, A1 attribute, V1 value) {
		EavMap<E1,A1,V1> out = new EavMap<E1,A1,V1>();
		return out.add(entity,attribute,value);
	}
	
	public EavMap<E,A,V> add(E entity, A attribute, V value) {
		this.put(entity, attribute, value);
		return this;
	}
	
	public EavMap<E,A,V> add(E entity, Map<A,V> attributeValue) {
		this.map.put(entity, new LinkedHashMap<A,V>(attributeValue));
		return this;
	}
	
	public void clear() {
		map = new LinkedHashMap<E,LinkedHashMap<A,V>>();
	}

	public boolean containsEntity(E entity) {
		return map.containsKey(entity);
	}
	
	public boolean containsKey(E entity, A attribute) {
		if (map.containsKey(entity)) {
			return map.get(entity).containsKey(attribute);
		} else {return false;}
	}

	public boolean containsValue(V value) {
		for (LinkedHashMap<A,V> sub: map.values()) {
			if (sub.containsValue(value)) return true;
		}
		return false;
	}

	
	/**
	 * returns a value or null if either entity not found or entity attribute pair not found
	 * @param entity
	 * @param attribute
	 * @return
	 */
	public V get(E entity, A attribute) { 
		try {
			return map.get(entity).get(attribute);
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public Set<E> getEntitySet() {
		return map.keySet();
	}
	
	public Set<A> getAttributeSet(E entity) {
		return map.get(entity).keySet();
	}
	
	public Set<A> getAttributeSet() {
		LinkedHashSet<A> atts = new LinkedHashSet<A>();
		for (E entity: getEntitySet()) {
			atts.addAll(this.get(entity).keySet());
		}
		return atts;
	}
	
	public Set<V> getValueSet() {
		LinkedHashSet<V> vals = new LinkedHashSet<V>();
		for (E entity: getEntitySet()) {
			vals.addAll(this.get(entity).values());
		}
		return vals;
	}
	
	public Map<A,V> get(E entity) {
		if (map.containsKey(entity)) return map.get(entity);
		else return new LinkedHashMap<A,V>(); 
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public V put(E entity, A attribute, V value) {
		if (map.containsKey(entity)) {
			map.get(entity).put(attribute, value);
		} else {
			LinkedHashMap<A,V> tmp = new LinkedHashMap<A,V>(attributes,0.99F);
			tmp.put(attribute, value);
			map.put(entity, tmp);
		}
		return value;
	}

	
	public V remove(E entity, A attribute) {
		try {
		V value = map.get(entity).remove(attribute);
		if (map.get(entity).isEmpty()) map.remove(entity);
		return value;
		} catch (NullPointerException e) {return null;}
	}

	public int size() {
		int count = 0;
		for (LinkedHashMap<A, V> tmp: map.values()) {
			count += tmp.size();
		}
		return count;
	}

	public int numberEntities() {
		return map.size();
	}
	
	public Stream<Triple<E,A,V>> stream() {
		Stream<Entry<E,LinkedHashMap<A,V>>> tmp = map.entrySet().stream();
		return tmp.flatMap(x -> {
			E e = x.getKey();
			Stream<Entry<A,V>> submap = x.getValue().entrySet().stream();
			Stream<Triple<E,A,V>> out = submap.map(av -> Triple.create(e, av.getKey(), av.getValue()));
			return out;
		});			
	}
	
	public Stream<Tuple<E, Map<A,V>>> streamEntities() {
		return map.entrySet().stream().map(eav -> 
			Tuple.create(
					eav.getKey(),
					eav.getValue())
		);
	}
	
	public EavMap<A,E,V> transpose() {
		EavMap<A,E,V> out = new EavMap<A,E,V>();
		this.stream().forEach(eav -> {
			out.add(eav.attribute(), eav.entity(), eav.value());
		});
		return out;
	}

	public V getOrElse(E x, A y, V i) {
		V tmp = get(x,y);
		if (tmp == null) return i;
		return tmp;
	}

	public EavMap<E,A,V> addAll(EavMap<E,A,V> map2) {
		map2.streamEntities().forEach(t -> this.add(t.getFirst(), t.getSecond()));
		return this;
	}
	
	
}
