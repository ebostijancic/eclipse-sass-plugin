package at.workflow.webdesk.po.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;

/**
 * A menuItemTemplateLink is an item which when placed inside 
 * a folder or tree (in case it's a top level item) will refer
 * to a tree template. It's possible to place different template
 * links inside a folder or tree refering to the same template tree. 
 * 
 * @author ebostijancic 07.09.2012
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class PoMenuItemTemplateLink extends PoMenuItemBase {

	@ManyToOne
	@JoinColumn(name="TREETEMPLATE_UID" )
	@ForeignKey( name="FK_ITEM_TREETEMPLATE" )
	/** reference to a tree template, which is expanded on the current position in the
	 * containing PoMenuTree. When deleting the link it's must not delete the template */
	private PoMenuTreeTemplate treeTemplate;

	public PoMenuTreeTemplate getTreeTemplate() {
		return treeTemplate;
	}

	public void setTreeTemplate(PoMenuTreeTemplate treeTemplate) {
		this.treeTemplate = treeTemplate;
	}
}
