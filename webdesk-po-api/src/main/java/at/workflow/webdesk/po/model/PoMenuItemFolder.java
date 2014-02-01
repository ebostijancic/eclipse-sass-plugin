package at.workflow.webdesk.po.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

/**
 * A PoMenuItemFolder is a container for PoMenuItemActionLinks and
 * PoMenuItemTemplateLinks. A folder can contain some children and
 * can be empty too. 
 * 
 * @author ebostijancic 07.09.2012
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PoMenuItemFolder extends PoMenuItemBase {

	/** name of the folder */
	private String i18nKey;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
	@ForeignKey(name = "FK_ITEM_PARENT")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@OrderBy("ranking")
	/** by deleting the folder, also child actionLinks, folders should be delete too */
	private List<PoMenuItemBase> childs = new ArrayList<PoMenuItemBase>();

	public String getI18nKey() {
		return i18nKey;
	}

	public void setI18nKey(String i18nKey) {
		this.i18nKey = i18nKey;
	}

	/** a method to add a child to the folder, an exception is throw if child is null. */
	public void addChild(PoMenuItemBase item) {
		if(item == null) {
			throw new IllegalArgumentException("Cannot add null as child!");
		}
		
		// special check for folders
		if(item instanceof PoMenuItemFolder)
			assertNewChildIsValid(item);
		
		item.setParent(this);
		this.childs.add(item);
		item.setRanking(this.childs.indexOf(item));
	}
	
	public List<PoMenuItemBase> getChilds() {
		//Collections.sort(childs);
		return childs;
	}

	public void setChilds(List<PoMenuItemBase> childs) {
		this.childs = childs;
	}

	/** 
	 * to ensure to add only valid children to the folder, this method is invoked before
	 * adding the child to the folder.
	 */
	private void assertNewChildIsValid(PoMenuItemBase child) {
		
		// this should not happen, 
		if(child == null) {
			throw new IllegalArgumentException("Cannot add a null folder as child!");
		}
		
		// check if it's already in the folder at the same level
		if(this.getChilds().contains(child)) {
			throw new IllegalArgumentException("Folder is already a child!");
		}
		
		// check if it's the same object.
		if(this.equals(child)) {
			throw new IllegalArgumentException("Cannot add self as a child!");
		}
		
		// check for the same object in the tree up to the top.
		PoMenuItemFolder parentFolder = (PoMenuItemFolder) this.getParent();
		while(parentFolder != null) {
			if(parentFolder.equals(child)) {
				throw new IllegalArgumentException("Cannot add a parent as child!");
			}
			parentFolder = (PoMenuItemFolder) parentFolder.getParent();
		}
	}
}
