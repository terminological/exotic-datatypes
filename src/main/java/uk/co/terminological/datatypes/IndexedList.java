/**
 * 
 */
package uk.co.terminological.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


/**
 * @author RCHALLEN
 *
 */
public class IndexedList<INDEX,LIST> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	HashMap<INDEX,ArrayList<LIST>> data; 
	
	public boolean hasIndexKey(INDEX index) {
		return data.containsKey(index);
	}
	
	public IndexedList() {
		data = new HashMap<INDEX,ArrayList<LIST>>();
	}
	
	public static <I,L> IndexedList<I,L> create(Class<I> iclzz, Class<L> ilst) {
		IndexedList<I,L> out = new IndexedList<I,L>();
		return out;
	}
	
	public static <I,L> IndexedList<I,L> create(I iclzz, L ilst) {
		IndexedList<I,L> out = new IndexedList<I,L>();
		out.add(iclzz, ilst);
		return out;
	}
	
	public void add(INDEX index, LIST item) {
		if (!data.containsKey(index)) {
			data.put(index, new ArrayList<LIST>());
		}
		data.get(index).add(item);
	}
	
	public boolean contains(INDEX index) {
		return data.containsKey(index);
	}
	
	public Iterable<LIST> get(INDEX index) {
		return data.get(index);
	}
	
	public Iterable<LIST> getSafe(INDEX index) {
		if (index == null || get(index) == null) return new ArrayList<LIST>();
		else return get(index);
	}

	public Iterable<LIST> get(INDEX index, Comparator<LIST> compare) {
		ArrayList<LIST> tmp = data.get(index);
		Collections.sort(tmp,compare);
		return tmp;
	}
	
	public Iterable<LIST> getSafe(INDEX index, Comparator<LIST> compare) {
		if (index == null || get(index) == null) return new ArrayList<LIST>();
		else return get(index, compare);
	}
	
	public Iterable<INDEX> getIndexes() {
		return data.keySet();
	}
	
	public Iterable<INDEX> getIndexes(Comparator<INDEX> compare) {
		ArrayList<INDEX> indexes = new ArrayList<INDEX>(data.keySet());
		Collections.sort(indexes, compare);
		return indexes;
	}
	
	public Stream<LIST> stream() {
		return data.entrySet().stream().flatMap(kv -> kv.getValue().stream());
	}

	public void remove(INDEX index, LIST list) {
		if (hasIndexKey(index)) data.get(index).remove(list);
		if (data.get(index).isEmpty()) data.remove(index);
	}

	public Map<INDEX,LIST> maximumValues(Comparator<? super LIST> compare) {
		Map<INDEX,LIST> out = new HashMap<INDEX,LIST>();
		for (Map.Entry<INDEX,ArrayList<LIST>> entry: data.entrySet()) {
			Collections.sort(entry.getValue(), compare);
			out.put(entry.getKey(), entry.getValue().get(0));
			// value lists are never empty in this class. 
		}
		return out;
	}
}
