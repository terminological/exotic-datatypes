/**
 * 
 */
package uk.co.terminological.view;


/**
 * @author RCHALLEN
 *
 */
public class TreeLeaf<PARENT,TYPE> implements TreeNode<PARENT,TYPE,Object> {
	
	String name;
	TYPE value;
	TreeBranch<?,PARENT,TYPE> parent;
	
	@Override
	public TreeBranch<?,PARENT,TYPE> getParent() throws RootNodeException {
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#getValue()
	 */
	@Override
	public TYPE getValue() {
		return value;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public void setValue(TYPE value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#getChildren()
	 */
	@Override
	public Iterable<TreeNode<TYPE,Object,?>> getChildren() throws LeafNodeException {
		throw new LeafNodeException();
	}
	
	public static <NEWPARENT,NEW> TreeLeaf<NEWPARENT,NEW> create(String name, NEW value, TreeBranch<?, NEWPARENT, NEW> parent) {
		TreeLeaf<NEWPARENT,NEW> out = new TreeLeaf<NEWPARENT,NEW>();
		out.setValue(value);
		out.name = name;
		parent.attach(out);
		return out;
	}

	public static <NEWPARENT,NEW> TreeLeaf<NEWPARENT,NEW> createStub(String name, NEW value, Class<NEWPARENT> parentType) {
		if (value == null) throw new NullPointerException();
		TreeLeaf<NEWPARENT,NEW> out = new TreeLeaf<NEWPARENT,NEW>();
		out.setValue(value);
		out.name = name;
		out.parent = null;
		return out;
	}
	
	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#asBranch()
	 */
	@Override
	public TreeBranch<PARENT, TYPE, Object> asBranch() throws LeafNodeException {
		throw new LeafNodeException();
	}

	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#asLeaf()
	 */
	@Override
	public TreeLeaf<PARENT, TYPE> asLeaf() {
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <CHILD> TreeBranch<PARENT,TYPE,CHILD> convertToBranchSet(Class<CHILD> childType) {
		TreeBranch<PARENT,TYPE,CHILD> out = (TreeBranch<PARENT, TYPE, CHILD>) TreeBranch.createStub(name, value, parent.value.getClass(), childType, true);
		parent.swap(this, out);
		return out;
	}
	
	@SuppressWarnings("unchecked")
	public <CHILD> TreeBranch<PARENT,TYPE,CHILD> convertToBranchList(Class<CHILD> childType) {
		TreeBranch<PARENT,TYPE,CHILD> out = (TreeBranch<PARENT, TYPE, CHILD>) TreeBranch.createStub(name, value, parent.value.getClass(), childType, false);
		parent.swap(this, out);
		return out;
	}

	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#setParent(com.bmj.informatics.datatypes.TreeBranch)
	 */
	@Override
	public void setParent(TreeBranch<?, PARENT, TYPE> parent) {
		this.parent = parent;
	}
	
	public String toXml() {return "<"+(name == null ? "item" : name)+" value='"+value.toString()+"'/>\n";}

	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#count()
	 */
	@Override
	public int count() {
		return 1;
	}
}
