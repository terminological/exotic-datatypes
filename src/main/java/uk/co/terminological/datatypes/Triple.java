package uk.co.terminological.datatypes;

import java.io.Serializable;

/**
 * An immutable triple class.
 * @author terminological
 *
 * @param <S1>
 * @param <S2>
 * @param <S3>
 */
public class Triple<S1, S2, S3> implements Cloneable, Serializable {

	private static final long serialVersionUID = -1444212990006531851L;

	@SuppressWarnings("unchecked")
	public Triple<S1, S2, S3> clone() {try {
		return (Triple<S1, S2, S3>) super.clone();
	} catch (CloneNotSupportedException e) {
		throw new Error("This should not occur since we implement Cloneable");
	}}
	
	private S1 item1;
	private S2 item2;
	private S3 item3;

	public Triple(S1 item1, S2 item2, S3 item3) {
		put(item1,item2,item3);
	};
	
	private void put(S1 item1, S2 item2, S3 item3) {
		this.item1 = item1;
		this.item2 = item2;
		this.item3 = item3;
	}
	
	public S1 getFirst() {return item1;}
	public S2 getSecond() {return item2;}
	public S3 getThird() {return item3;}
	
	public static <X extends Object, Y extends Object, Z extends Object> Triple<X,Y,Z> create(X x,Y y, Z z) {
		return new Triple<X,Y,Z>(x,y,z);
	}
	
	public boolean firstEquals(Object value) {
		if ((value == null && this.getFirst() == null)) return true;
		else if ((value != null) && (this.getFirst() == null)) return false;
		if (this.getFirst().equals(value)) return true;
		else return false;
	}
	
	public boolean secondEquals(Object value) {
		if ((value == null && this.getSecond() == null)) return true;
		else if ((value != null) && (this.getSecond() == null)) return false;
		if (this.getSecond().equals(value)) return true;
		else return false;
	}
	
	public boolean thirdEquals(Object value) {
		if ((value == null && this.getThird() == null)) return true;
		else if ((value != null) && (this.getThird() == null)) return false;
		if (this.getThird().equals(value)) return true;
		else return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		try {
			Triple<S1,S2,S3> t = (Triple<S1,S2,S3>) o;
			return (
					firstEquals(t.getFirst()) && 
					secondEquals(t.getSecond()) &&
					thirdEquals(t.getThird())
					);
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	public int hashCode() { 
	    int hash = 1;
	    hash = hash * 31 + (getFirst() == null ? 0 : getFirst().hashCode());
	    hash = hash * 31 + (getSecond() == null ? 0 : getSecond().hashCode());
	    hash = hash * 31 + (getThird() == null ? 0 : getThird().hashCode());
	    return hash;
	}
	
	public String toString() {
		return "["+item1.toString()+":"+item2.toString()+":"+item3.toString()+"]";
	}
	
	public S1 entity() {return getFirst();}
	public S2 attribute() {return getSecond();}
	public S3 value() {return getThird();}
}


