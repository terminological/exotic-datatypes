/**
 * 
 */
package com.bmj.informatics.datatypes;


/**
 * @author RCHALLEN
 *
 */
public class TreeRoot<TYPE, CHILD> extends TreeBranch<Object,TYPE, CHILD> {

	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#getParent()
	 */
	@Override
	public TreeBranch<?,Object,TYPE> getParent() throws RootNodeException {
		throw new RootNodeException();
	}

	public static <NEW> TreeRoot<NEW,Object> create(String name, NEW value) {
		return create(name,value,Object.class,true);
	}
	
	public static <NEW,NEWCHILD> TreeRoot<NEW,NEWCHILD> create(String name, NEW value, Class<NEWCHILD> childType, boolean uniqueValues) {
		TreeRoot<NEW,NEWCHILD> out = new TreeRoot<NEW,NEWCHILD>();
		out.setValue(value);
		out.name = name;
		out.unique = uniqueValues;
		return out;
	}
	
}
