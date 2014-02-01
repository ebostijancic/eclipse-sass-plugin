package at.workflow.webdesk.po.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;

/**
 * A menuItemActionLink is an menu item which refers to an action. 
 * 
 * @author ebostijancic 04.09.2012
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class PoMenuItemActionLink extends PoMenuItemBase {
	
	@ManyToOne
    @JoinColumn(name="ACTION_UID")
    @ForeignKey(name="FK_ITEM_ACTION")
	/** PoAction assigned to the link */
	private PoAction action;

	public PoAction getAction() {
		return action;
	}

	public void setAction(PoAction action) {
		this.action = action;
	}
}
