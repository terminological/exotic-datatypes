package uk.co.terminological.datatypes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class OneToManyMap<K, V> extends HashMap<K, Set<V>> {

	public OneToManyMap<K, V> putItem(K k,V v) {
		this.merge(
				k, 
				new HashSet<>(Arrays.asList(v)), 
				(s,vNew) -> {
					s.add(v);
					return s;
				});
		return this;
	}
	
	public Stream<Tuple<K,V>> stream() {
		return this.entrySet().stream().flatMap(ksv -> {
			K k = ksv.getKey();
			Stream<V> v = ksv.getValue().stream();
			return v.map(y -> Tuple.create(k, y));
		});
	}
	
	public Stream<V> stream(K k) {
		if (!this.containsKey(k)) return Stream.empty();
		return this.get(k).stream();
	}
	
	public Stream<V> stream(K k, Supplier<V> defaultValue) {
		if (!this.containsKey(k)) return Stream.empty();
		return this.get(k).stream();
	}
	
	public boolean removeItem(K k,V v) {
		if (this.containsKey(k)) {
			boolean removed = this.get(k).remove(v);
			if (this.get(k).isEmpty()) this.remove(k);
			return removed;
		} else {
			return false;
		}
	}
	
	public boolean contains(K k, V v) {
		return 
				this.containsKey(k) &&
				this.get(k).contains(v);
	}
	
	
}
