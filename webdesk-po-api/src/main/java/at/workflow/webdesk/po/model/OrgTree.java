package at.workflow.webdesk.po.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrgTree implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private List<OrgTreeNode> rootNodes = new ArrayList<OrgTreeNode>();

	/**
	 * @return a <code>List</code> of <code>OrgTreeNode</code> objects.
	 */
	public List<OrgTreeNode> getRootNodes() {
		return rootNodes;
	}

	public void addRootNode(OrgTreeNode otn) {
		this.rootNodes.add(otn); 
	}

}
