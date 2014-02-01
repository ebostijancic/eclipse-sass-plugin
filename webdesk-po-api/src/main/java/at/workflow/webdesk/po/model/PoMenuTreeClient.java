package at.workflow.webdesk.po.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;

/**
 * A PoMenuTreeClient describes an PoMenuTreeBase (tree) for
 * an specific PoClient. 
 * 
 * @author ebostijancic 07.09.2012
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class PoMenuTreeClient extends PoMenuTreeBase {
	
	@ManyToOne
	@JoinColumn(name="CLIENT_UID" )
	@ForeignKey( name="FK_MENUTREE_CLIENT" )
	/** client to which this Menu belongs */
	private PoClient client;

	public PoClient getClient() {
		return client;
	}

	public void setClient(PoClient client) {
		this.client = client;
	}
}
