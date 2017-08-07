/**
 * 
 */
package uk.co.terminological.view;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.co.terminological.view.TreeNode.BranchNodeException;
import uk.co.terminological.view.TreeNode.LeafNodeException;

/**
 * @author RCHALLEN
 *
 */
public interface TreeFormatter<OUT> {
	
	public void write(TreeNode<?,?,?> node);
	public void write(TreeBranch<?,?,?> branch);
	public void write(TreeLeaf<?,?> leaf);
	public OUT output();
	
	public static class DomFormatter implements TreeFormatter<Document> {
		Document dom;
		Node focus;
		
		public DomFormatter(Document dom) {
			this.dom = dom;
			this.focus = dom;
		}
		
		/* (non-Javadoc)
		 * @see com.bmj.informatics.datatypes.TreeFormatter#write(com.bmj.informatics.datatypes.TreeBranch)
		 */
		@Override
		public void write(TreeBranch<?, ?, ?> branch) {
			write((TreeNode<?,?,?>) branch);
			for (TreeNode<?,?,?> child: branch.getChildren()) {
				try {
					write(child.asBranch());
				} catch (LeafNodeException e) {
					try {
						write(child.asLeaf());
					} catch (BranchNodeException e1) {
						throw new RuntimeException(e1); //TODO:auto generated runtime exception
					}
				} 
			}
			focus = focus.getParentNode();
			
			
		}

		/* (non-Javadoc)
		 * @see com.bmj.informatics.datatypes.TreeFormatter#write(com.bmj.informatics.datatypes.TreeLeaf)
		 */
		@Override
		public void write(TreeLeaf<?, ?> leaf) {
			write((TreeNode<?,?,?>) leaf);
			focus = focus.getParentNode();
		}

		/* (non-Javadoc)
		 * @see com.bmj.informatics.datatypes.TreeFormatter#write(com.bmj.informatics.datatypes.TreeNode)
		 */
		@Override
		public void write(TreeNode<?, ?, ?> node) {
			Element el = dom.createElement(node.getName());
			focus.appendChild(el);
			el.setAttribute("value", node.getValue().toString());
			focus = el;
		}
		
		public Document output() {return dom;}
		
	}
}
