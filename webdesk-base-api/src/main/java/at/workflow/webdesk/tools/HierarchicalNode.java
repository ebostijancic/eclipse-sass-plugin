package at.workflow.webdesk.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a node of an hierarchical tree.
 * Circular references are not recommended, as such could lead to endless loops.
 * This class does NOT exclude circular references!
 * 
 * @author fritzberger 07.12.2011
 * @param <E> "Entity", the entity-type to wrap as data of this tree node.
 * @param <C> "Children", the node-type to wrap as children of this tree node.
 */
@SuppressWarnings("serial")
public class HierarchicalNode<E,C> implements Serializable	{

	private E entity;
	private List<C> children = new ArrayList<C>();
	
	public HierarchicalNode(E entity) {
		this.entity = entity;
	}
	
	public E getEntity() {
		return entity;
	}
	
	public List<C> getChildren() {
		return children;
	}
	
	public void addChild(C node) {
		children.add(node);
	}
	
}
