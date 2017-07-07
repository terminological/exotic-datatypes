/**
 * 
 */
package com.bmj.informatics.datatypes;


/**
 * @author RCHALLEN
 *
 */
public class TreeLeafCounter<PARENT,TYPE> extends TreeLeaf<PARENT,TYPE> {

	int counter;
	
	public TreeLeafCounter<PARENT,TYPE> add() {counter += 1; return this;}
	
	public static <NEWPARENT,NEW> TreeLeafCounter<NEWPARENT,NEW> createStub(String name, NEW value, Class<NEWPARENT> parentType) {
		if (value == null) throw new NullPointerException();
		TreeLeafCounter<NEWPARENT,NEW> out = new TreeLeafCounter<NEWPARENT,NEW>();
		out.setValue(value);
		out.name = name;
		out.parent = null;
		out.counter = 1;
		return out;
	}
	
	public String toXml() {return "<"+(name == null ? "item" : name)+" value='"+value.toString()+"' count='"+counter+"'/>";}
	
	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#count()
	 */
	@Override
	public int count() {
		return counter;
	}
}
