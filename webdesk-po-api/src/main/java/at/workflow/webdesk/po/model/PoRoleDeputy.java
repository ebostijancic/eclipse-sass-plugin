package at.workflow.webdesk.po.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * Indicates a <code>PoRoleHolderPerson</code> object as a 
 * deputy (<code>deputyPerson</code>) of the <code>officeHolder</code>
 * for a given timerange. The timerange is defined via <code>validfrom</code>
 * and <code>validto</code>.
 *</p>
 *
 * @author hentner
 */
@SuppressWarnings("serial")
public class PoRoleDeputy extends PoHistorization {

	private String uid;
	private List<PoRoleHolderPerson> roleHolderPersons = new ArrayList<PoRoleHolderPerson>();
	private PoPerson officeHolder;
	
	/*
	 * These are softvalues, they are not persisted, as they can be extracted.
	 */
	private PoRole role;
	private PoPerson deputy;
	private boolean changed = false;
	private boolean deleted = false;
	
	/**
	 * @return true if the current element should be deleted.
	 */
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * The deputy is determined via the 
	 * first <code>PoRoleHolderPerson</code> in the <code>roleHolderPerson</code>
	 * collection. 
	 * 
	 * @return <code>PoPerson</code> object.
	 */
	public PoPerson getDeputy() {
		if (deputy != null)
			return deputy;
		if (getRoleHolderPersons().size() > 0)
			return deputy = roleHolderPersons.get(0).getPerson();
		return null;
	}

	public void setDeputy(PoPerson deputy) {
		this.deputy = deputy;
	}

	
	public boolean isValid() {
		if (getRoleHolderPersons() == null)
			return false;
		
		Iterator<PoRoleHolderPerson> i = this.getRoleHolderPersons().iterator();
		Date now = new Date();
		while (i.hasNext()) {
			if (i.next().getValidto().after(now))
				return true;
		}
		return false;
	}


	public PoRole getRole() {
		if (role != null)
			return role;
		if (roleHolderPersons.size() > 0)
			return getRoleHolderPersons().get(0).getRoleCompetenceBase().getRole();
		return null;
	}

	public void setRole(PoRole role) {
		this.role = role;
	}


	/**
	 * @return the officeholder. (a <code>PoPerson</code>) 
     */
	public PoPerson getOfficeHolder() {
		return officeHolder;
	}

	public void setOfficeHolder(PoPerson officeHolder) {
		this.officeHolder = officeHolder;
	}

	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}

	@Override
	public String getUID() {
		return uid;
	}


	/**
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects. These objects 
	 * represents all links to competences defined for this deputy.
	 */
	public List<PoRoleHolderPerson> getRoleHolderPersons() {
		return roleHolderPersons;
	}

	public void setRoleHolderPersons(List<PoRoleHolderPerson> roleHolderPersons) {
		this.roleHolderPersons = roleHolderPersons;
	}

	/**
	 * Adds the given <code>rhp</code> to the <code>List</code> of 
	 * <code>PoRoleHolderPerson</code> objects.
	 * 
	 * @param rhp a <code>PoRoleHolderPerson</code> object.
	 */
	public void addRoleHolderPerson(PoRoleHolderPerson rhp) {
		this.roleHolderPersons.add(rhp);
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}
