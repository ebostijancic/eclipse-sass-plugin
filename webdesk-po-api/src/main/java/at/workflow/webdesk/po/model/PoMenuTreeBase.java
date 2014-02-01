package at.workflow.webdesk.po.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

/**
 * A PoMenuTreeBase describes the actual Tree
 * 
 * @author ebostijancic 07.09.2012
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("serial")
public class PoMenuTreeBase extends PoBase {

	@Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name="MENUTREE_UID", length=32)	
	/** unique id of menutree */
	private String UID;
	
	/** id of the application if its null, then the tree is for the webdesk itself. */
	private String applicationId;

	@OneToMany(mappedBy="menuTree")
	@Where(clause = PoMenuItemBase.PARENT_MENUITEM_UID_COLUMN_NAME + " is null")
	private List<PoMenuItemBase> topLevelItems = new ArrayList<PoMenuItemBase>();
	
	/**
	 * method used to add a new item to the top level item list.
	 * @param item
	 */
	public void addTopLevelItem(PoMenuItemBase item) {
		topLevelItems.add(item);
		item.setRanking(topLevelItems.indexOf(item));
		item.setMenuTree(this);
	}

	@Override
	public String getUID() {
		return UID;
	}

	@Override
	public void setUID(String uid) {
		this.UID = uid;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public List<PoMenuItemBase> getTopLevelItems() {
		return topLevelItems;
	}

	public void setTopLevelItems(List<PoMenuItemBase> topLevelItems) {
		this.topLevelItems = topLevelItems;
	}

}
