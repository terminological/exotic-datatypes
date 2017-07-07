/**
 * 
 */
package uk.co.terminological.view;


/**
 * @author RCHALLEN
 *
 */
public interface TreeNode<PARENT,TYPE,CHILD> {

	public TreeBranch<?,PARENT,TYPE> getParent() throws RootNodeException;
	public void setParent(TreeBranch<?,PARENT,TYPE> parent);
	public Iterable<TreeNode<TYPE,CHILD,?>> getChildren() throws LeafNodeException;
	public TreeBranch<PARENT,TYPE,CHILD> asBranch() throws LeafNodeException;
	public TreeLeaf<PARENT,TYPE> asLeaf() throws BranchNodeException;
	public TYPE getValue();
	public String getName();
	public String toXml();
	public int count();
	
	public static class RootNodeException extends Exception {private static final long serialVersionUID = 1L;}	
	public static class LeafNodeException extends Exception {private static final long serialVersionUID = 1L;}
	public static class BranchNodeException extends Exception {private static final long serialVersionUID = 1L;}
	public static class ValueNotFoundException extends Exception {private static final long serialVersionUID = 1L;}
}
