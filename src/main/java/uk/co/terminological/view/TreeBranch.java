/**
 * 
 */
package uk.co.terminological.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @author RCHALLEN
 *
 */
public class TreeBranch<PARENT, TYPE, CHILD> implements TreeNode<PARENT, TYPE,CHILD> {

	String name;
	TYPE value;
	TreeBranch<?,PARENT,TYPE> parent;
	ArrayList<TreeNode<TYPE,CHILD,?>> children;
	HashMap<CHILD,TreeNode<TYPE,CHILD,?>> index;
	boolean unique = false;

	protected TreeBranch() {
		children = new ArrayList<TreeNode<TYPE,CHILD,?>>();
		index = new HashMap<CHILD,TreeNode<TYPE,CHILD,?>>();
	}

	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#getChildren()
	 */
	@Override
	public Iterable<TreeNode<TYPE,CHILD,?>> getChildren() {
		return children;
	}

	public void sortByName() {
		Collections.sort(children, new Comparator<TreeNode<?,?,?>>() {
			@Override
			public int compare(TreeNode<?, ?, ?> arg0, TreeNode<?, ?, ?> arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		try {
			for (TreeNode<?, ?, ?> arg0: getChildren()) {
				arg0.asBranch().sortByName();
			}
		} catch (LeafNodeException e) {
			//do nothing
		}
	}

	public void sortByNameAndValue() {
		Collections.sort(children, new Comparator<TreeNode<?,?,?>>() {
			@Override
			public int compare(TreeNode<?, ?, ?> arg0, TreeNode<?, ?, ?> arg1) {
				if (arg0.getName().compareTo(arg1.getName()) == 0) 
					return arg0.getValue().toString().compareTo(arg1.getValue().toString());
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		try {
			for (TreeNode<?, ?, ?> arg0: getChildren()) {
				arg0.asBranch().sortByNameAndValue();
			}
		} catch (LeafNodeException e) {
			//do nothing
		}
	}
	
	public void sortByNameAndFreq() {
		Collections.sort(children, new Comparator<TreeNode<?,?,?>>() {
			@Override
			public int compare(TreeNode<?, ?, ?> arg0, TreeNode<?, ?, ?> arg1) {
				if (arg0.getName().compareTo(arg1.getName()) == 0) 
					return arg1.count()-arg0.count();
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		try {
			for (TreeNode<?, ?, ?> arg0: getChildren()) {
				arg0.asBranch().sortByNameAndFreq();
			}
		} catch (LeafNodeException e) {
			//do nothing
		}
	}
	
	public String getName() {return name;} 

	@Override
	public TreeBranch<?,PARENT,TYPE> getParent() throws RootNodeException {
		if (parent != null) return parent;
		throw new RootNodeException();
	}

	public void setParent(TreeBranch<?,PARENT,TYPE> parent) {
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#getValue()
	 */
	@Override
	public TYPE getValue() {
		return value;
	}

	public void setValue(TYPE value) {
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public TreeBranch<PARENT, TYPE, CHILD> withLeafCounter(String name, CHILD child) {
		try {
			((TreeLeafCounter<TYPE,CHILD>) getChildByValue(name, child).asLeaf()).add();
			return this;
		} catch (ValueNotFoundException e) {
			attach((TreeNode<TYPE, CHILD, Object>) TreeLeafCounter.createStub(name,child, this.getValue().getClass()));
			return this;
		} catch (BranchNodeException e) {
			throw new RuntimeException(e);
		}
	}

	
	
	@SuppressWarnings("unchecked")
	public TreeBranch<PARENT, TYPE, CHILD> withLeaf(String name, CHILD child) {
		attach((TreeNode<TYPE, CHILD, Object>) TreeLeaf.createStub(name,child, this.getValue().getClass()));
		return this;
	}

	public TreeBranch<TYPE, CHILD, Object> withSimpleList(String name, CHILD value) {
		return withBranchList(name,value,Object.class);
	}

	public TreeBranch<TYPE, CHILD, Object> withSimpleSet(String name, CHILD value) {
		return withBranchSet(name,value,Object.class);
	}
	
	public TreeBranch<TYPE, CHILD, Object> withNamedType(CHILD type) {
		return withBranchSet(type.getClass().getSimpleName(),type,Object.class);
	}
	
	public TreeBranch<TYPE, CHILD, ?> getNamedType(CHILD type) throws ValueNotFoundException {
		return (TreeBranch<TYPE, CHILD, ?>) getChildByValue(type.getClass().getSimpleName(),type);
	}
	
	@SuppressWarnings("unchecked")
	public <GRANDCHILD> TreeBranch<TYPE, CHILD, GRANDCHILD> withBranchList(String name, CHILD value, Class<GRANDCHILD> subtype) {
		TreeBranch<TYPE, CHILD, GRANDCHILD> out = (TreeBranch<TYPE, CHILD, GRANDCHILD>) TreeBranch.createStub(name, value, this.getValue().getClass(), subtype, false);  
		attach(out);
		return out;
	}

	@SuppressWarnings("unchecked")
	public <GRANDCHILD> TreeBranch<TYPE, CHILD, GRANDCHILD> withBranchSet(String name, CHILD value, Class<GRANDCHILD> subtype) {
		try {
			return (TreeBranch<TYPE, CHILD, GRANDCHILD>) getChildByValue(name,value);
		} catch (ValueNotFoundException e) {
			TreeBranch<TYPE, CHILD, GRANDCHILD> out = (TreeBranch<TYPE, CHILD, GRANDCHILD>) TreeBranch.createStub(name, value, this.getValue().getClass(), subtype, true);  
			attach(out);
			return out;
		}
	}

	@SuppressWarnings("unchecked")
	public <DESCENDENT> TreeNode<?,DESCENDENT,?> getDescendentByValue(String name, DESCENDENT value) throws ValueNotFoundException {
		try {
			return (TreeNode<?, DESCENDENT, ?>) getChildByValue(name, (CHILD) value);
		} catch (Exception e) {
			for (TreeNode<TYPE, CHILD, ?> child: this.getChildren()) {
				try {
					child.asBranch().getDescendentByValue(name, value);
				} catch (LeafNodeException e1) {
					// don't process leaves
				}
			}
		}
		throw new ValueNotFoundException();
	}
	
	public TreeNode<TYPE, CHILD, ?> getChildByValue(String name, CHILD value) throws ValueNotFoundException {
		if (unique && index.containsKey(value)) {
			TreeNode<TYPE, CHILD, ?> child = index.get(value);
			try {
				if (child.getName() == null && name == null) return child;
				if (child.getName().equals(name)) return child;
				throw new ValueNotFoundException();
			} catch (NullPointerException e) {
				throw new ValueNotFoundException();
			}
		} else {
			for (TreeNode<TYPE, CHILD, ?> child: getChildren()) {
				if (child.getValue().equals(value)) {
					try {
						if (child.getName() == null && name == null) return child;
						if (child.getName().equals(name)) return child;
						// carry on
					} catch (NullPointerException e) {
						// carry on
					}
				}
			}
		}
		throw new ValueNotFoundException();
	}

	public <GRANDCHILD> TreeBranch<PARENT, TYPE, CHILD> attach(TreeNode<TYPE, CHILD, GRANDCHILD> branch) {
		branch.setParent(this);
		if (unique && index.containsKey(branch.getValue())) {
			TreeNode<TYPE, CHILD, ?> oldBranch = index.get(branch.getValue());
			swap(oldBranch, branch);
		} else {
			children.add(branch);
			if (unique) index.put(branch.getValue(), branch);
		}
		return this;
	}

	public <GRANDCHILD> TreeBranch<PARENT, TYPE, CHILD> detach(TreeNode<TYPE, CHILD, GRANDCHILD> branch) {
		branch.setParent(null);
		children.remove(branch);
		if (unique) index.remove(branch.getValue());
		return this;
	}

	public <GRANDCHILD> TreeBranch<PARENT, TYPE, CHILD> swap(TreeNode<TYPE, CHILD, GRANDCHILD> oldBranch, TreeNode<TYPE, CHILD, ?> newBranch) {
		oldBranch.setParent(null);
		newBranch.setParent(this);
		children.add(children.indexOf(oldBranch), newBranch);
		if (unique) index.put(newBranch.getValue(), newBranch);
		return this;
	}

	public static <NEWPARENT, NEW, NEWCHILD> TreeBranch<NEWPARENT, NEW, NEWCHILD> createStub(String name, NEW value, Class<NEWPARENT> parentType, Class<NEWCHILD> childType, boolean uniqueValues) {
		if (value == null) throw new NullPointerException();
		TreeBranch<NEWPARENT,NEW, NEWCHILD> out = new TreeBranch<NEWPARENT,NEW, NEWCHILD>();
		out.name = name;
		out.value = value;
		out.parent = null;
		out.unique = uniqueValues;
		return out;
	}

	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#asBranch()
	 */
	@Override
	public TreeBranch<PARENT, TYPE, CHILD> asBranch() {
		return this;
	}

	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#asLeaf()
	 */
	@Override
	public TreeLeaf<PARENT, TYPE> asLeaf() throws BranchNodeException {
		throw new BranchNodeException();
	}

	public String toXml() {
		StringBuilder out = new StringBuilder("<"+(name == null ? "item" : name)+" value='"+value.toString()+"' count='"+count()+"'>");
		for (TreeNode<?,?,?> child: this.getChildren()) {
			out.append(child.toXml());
		}
		out.append("</"+(name == null ? "item" : name)+">");
		return out.toString();
	}

	/* (non-Javadoc)
	 * @see com.bmj.informatics.datatypes.TreeNode#count()
	 */
	@Override
	public int count() {
		int count = 0;
		for (TreeNode<TYPE, CHILD, ?> child: getChildren()) {
			count += child.count();
		}
		return count;
	}
}
