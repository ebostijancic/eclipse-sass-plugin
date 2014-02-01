package at.workflow.webdesk.po.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.persistence.Transient;

/**
 * <p>
 * The Base class of competence classes of <code>PoRole</code>'s. When this class is used in the HQL,
 * all classes that extend this class are considered as well. 
 * </p>
 * These classes are 
 * <ul>	
 * 	<li>PoRoleCompetenceAll</li>
 *  <li>PoRoleCompetencePerson</li>
 *  <li>PoRoleCompetenceGroup</li>
 *  <li>PoRoleCompetenceClient</li>
 * </ul>
 * <p>
 * The role holders are linked with this class and implement the <code>PoRoleHolderLink</code> interface.
 * 
 * @author hentner, ggruber
 */
@SuppressWarnings("serial")
public class PoRoleCompetenceBase extends PoHistorization  {

	@Transient
	private List<PoRoleHolderLink> roleHolders = null;	// is not stored in database
	
	private String type;
	private String uid;
	private PoRole role;
	private List<PoRoleHolderPerson> roleHolderPersons = new ArrayList<PoRoleHolderPerson>();
	private List<PoRoleHolderDynamic> roleHolderDynamics = new ArrayList<PoRoleHolderDynamic>();
	private List<PoRoleHolderGroup> roleHolderGroups = new ArrayList<PoRoleHolderGroup>();
	
    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }
 
    public PoRole getRole() {
        return role;
    }

    public void setRole(PoRole role) {
        this.role = role;
    }
    
    /**
     * @return a <code>List</code> of <code>PoRoleHolderLink</code>
     * objects. The current date is used as validity constraint. 
     */
    public List<PoRoleHolderLink> getRoleHolders() {
    	return getRoleHolders(new Date());
    }
    
    /**
     * @return a <code>List</code> of <code>PoRoleHolderLink</code>
     * objects. The given <code>date</code> is used as validity constraint. 
     */
    public List<PoRoleHolderLink> getRoleHolders(Date date) {
    	if (roleHolders!=null)
    		return roleHolders;

    	roleHolders = new ArrayList<PoRoleHolderLink>(this.getRoleHolderPersons());
    	roleHolders.addAll(this.getRoleHolderDynamics());
    	roleHolders.addAll(this.getRoleHolderGroups());
    	
    	roleHolders = filterOutDeadLinks(roleHolders, date);
    	return roleHolders;
    }
    
    public List<PoRoleHolderPerson> getRoleHolderPersons() {
        return roleHolderPersons;
    }

    public void setRoleHolderPersons(List<PoRoleHolderPerson> value) {
        roleHolderPersons = value;
    }

    public boolean addRoleHolderPerson(PoRoleHolderPerson element) {
    	element.setRoleHolder(this);
        return roleHolderPersons.add(element);
    }

    public List<PoRoleHolderGroup> getRoleHolderGroups() {
        return roleHolderGroups;
    }

    public void setRoleHolderGroups(List<PoRoleHolderGroup> value) {
        roleHolderGroups = value;
    }

    public boolean addRoleHolderGroup(PoRoleHolderGroup element) {
    	element.setRoleHolder(this);
        return roleHolderGroups.add(element);
    }

    public List<PoRoleHolderDynamic> getRoleHolderDynamics() {
        return roleHolderDynamics;
    }

    public void setRoleHolderDynamics(List<PoRoleHolderDynamic> value) {
        roleHolderDynamics = value;
    }

    public boolean addRoleHolderDynamic(PoRoleHolderDynamic element) {
    	element.setRoleHolder(this);
        return roleHolderDynamics.add(element);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getName() {
    	return "ALL";
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
    	return getClass().getSimpleName()+": "+(getName() != null ? getName() : "")+"; role: "+role;
    }
    
    
    private List<PoRoleHolderLink> filterOutDeadLinks(List<PoRoleHolderLink> list, Date date) {
		Iterator<PoRoleHolderLink> i = list.iterator();
		HashSet<PoRoleHolderLink> res = new LinkedHashSet<PoRoleHolderLink>();
		while (i.hasNext()) {
			PoHistorization histObj = (PoHistorization) i.next();
			if (histObj.getValidto().after(date))
				res.add((PoRoleHolderLink) histObj);
		}
		List<PoRoleHolderLink> ret = new ArrayList<PoRoleHolderLink>();
		ret.addAll(res);
		return ret;
	}

}
