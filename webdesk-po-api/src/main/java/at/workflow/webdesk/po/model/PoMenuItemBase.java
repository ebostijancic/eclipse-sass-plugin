package at.workflow.webdesk.po.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;

/**
 * this class describes an generic menu item
 * which can be either a PoMenuItemActionLink or PoMenuItemFolder or PoMenuItemTemplateLink. 
 * 
 * @author ebostijancic 04.09.2012
 */
@SuppressWarnings("serial")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class PoMenuItemBase extends PoBase implements Comparable<PoMenuItemBase> {
	public static final String PARENT_MENUITEM_UID_COLUMN_NAME = "PARENT_MENUITEM_UID";
	
	@Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name="MENUITEM_UID", length=32)	
	/** unique id of item */
	private String UID;
	
	/** ranking for sorting of siblings */
	private int ranking;
	
	@ManyToOne
	@JoinColumn(name = PARENT_MENUITEM_UID_COLUMN_NAME)
	@ForeignKey(name = "FK_ITEM_PARENT")
	/** parent menu item of the current menuitem or null, if it is a root item */
	private PoMenuItemFolder parent;
	
	@ManyToOne
	@JoinColumn(name="MENUTREE_UID" )
	@ForeignKey(name = "FK_ITEM_MENUTREE")
	/** parent menutree of the current menuitem  */
	private PoMenuTreeBase menuTree;

	@Override
	public String getUID() {
		return UID;
	}

	@Override
	public void setUID(String uid) {
		this.UID = uid;
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public PoMenuItemFolder getParent() {
		return parent;
	}

	public void setParent(PoMenuItemFolder parent) {
		this.parent = parent;
	}

	public PoMenuTreeBase getMenuTree() {
		return menuTree;
	}

	public void setMenuTree(PoMenuTreeBase menuTree) {
		this.menuTree = menuTree;
	}

	/**
	 * special implementation of compareTo as
	 * two rankings cannot have the same value.
	 */
	@Override
	public int compareTo(PoMenuItemBase o) {
		if(this.ranking >= o.getRanking()) {
			return 1;
		}
		
		if(this.ranking < o.getRanking()) {
			return -1;
		}
		if(this.UID.equals(o.getUID())) {
			return 0;
		}
		return 1;
	}
}
