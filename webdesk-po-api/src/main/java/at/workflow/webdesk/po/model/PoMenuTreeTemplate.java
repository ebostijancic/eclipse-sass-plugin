package at.workflow.webdesk.po.model;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * a specialization of the PoMenuTreeBase tree which,
 * can have a name too. This tree will be referenced by
 * the PoMenuItemTemplateLink.
 * 
 * @author ebostijancic 07.09.2012
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("serial")
public class PoMenuTreeTemplate extends PoMenuTreeBase {
 
	/** name of the treeTemplate */
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
