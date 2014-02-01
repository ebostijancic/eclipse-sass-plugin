package at.workflow.webdesk.po.model;

import at.workflow.webdesk.tools.HierarchicalNode;

/**
 * A node of the hierarchical organization-tree (organigram).
 */
public class OrgTreeNode extends HierarchicalNode<PoGroup,OrgTreeNode> {
	
	public OrgTreeNode(PoGroup group) {
		super(group);
	}
	
}
