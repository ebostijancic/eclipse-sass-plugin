package at.workflow.webdesk.po.model;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Transient;

import at.workflow.webdesk.po.CompetenceTarget;
import at.workflow.webdesk.po.PermissionHolder;
import at.workflow.webdesk.tools.model.annotations.UiHints;


/**
 * Represents a client of the webdesk system. Could be a customer in a Saas/asp system or just
 * a part of the organizational tree. In most cases only one client will exist.
 * 
 * @author ggruber
 */

@UiHints(
	isSmallList = true,
	autoCompleteJavaScript = "name"
)

public class PoClient extends PoBase implements Comparable<PoClient>, CompetenceTarget, PermissionHolder {

	private static final long serialVersionUID = 1L;	// TODO: is this really needed?
	
	private String uid;
	private String shortName;
	private String name;
	private String description;
	
	// client prefixes for other entities
	private String groupShortNamePrefix; 
	private String personUserNamePrefix;
	private String personEmployeeIdPrefix;
	
	private Collection<PoPerson> persons = new HashSet<PoPerson>();
	private Collection<PoRole> roles = new HashSet<PoRole>();
	private Collection<PoGroup> groups = new HashSet<PoGroup>();
	private Collection<PoOrgStructure> structures = new HashSet<PoOrgStructure>();
	private Collection<PoAPermissionClient> permissions = new HashSet<PoAPermissionClient>();

	@Override
	public String getUID() {
		return uid;
	}

	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public String getGroupShortNamePrefix() {
		return groupShortNamePrefix;
	}

	public void setGroupShortNamePrefix(String groupShortNamePrefix) {
		this.groupShortNamePrefix = groupShortNamePrefix;
	}

	public String getPersonUserNamePrefix() {
		return personUserNamePrefix;
	}

	public void setPersonUserNamePrefix(String personUserNamePrefix) {
		this.personUserNamePrefix = personUserNamePrefix;
	}

	public String getPersonEmployeeIdPrefix() {
		return personEmployeeIdPrefix;
	}

	public void setPersonEmployeeIdPrefix(String personEmployeeIdPrefix) {
		this.personEmployeeIdPrefix = personEmployeeIdPrefix;
	}

	public Collection<PoRole> getRoles() {
        return roles;
    }

    public void setRoles(Collection<PoRole> roles) {
        this.roles = roles;
    }

    public boolean addRole(PoRole element) {
    	element.setClient(this);
        return roles.add(element);
    }

    public Collection<PoPerson> getPersons() {
        return persons;
    }

    public void setPersons(Collection<PoPerson> persons) {
        this.persons = persons;
    }

    public boolean addPerson(PoPerson element) {
    	element.setClient(this);
        return persons.add(element);
    }

    public Collection<PoOrgStructure> getOrgStructures() {
        return structures;
    }

    public void setOrgStructures(Collection<PoOrgStructure> value) {
        structures = value;
    }

    public boolean addOrgStructure(PoOrgStructure element) {
    	element.setClient(this);
        return structures.add(element);
    }

    public Collection<PoGroup> getGroups() {
        return groups;
    }

    public void setGroups(Collection<PoGroup> groups) {
        this.groups = groups;
    }

    public boolean addGroup(PoGroup element) {
    	element.setClient(this);
        return groups.add(element);
    }

    public Collection<PoAPermissionClient> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<PoAPermissionClient> permissions) {
        this.permissions = permissions;
    }

    public boolean addPermission(PoAPermissionClient element) {
    	element.setClient(this);
        return permissions.add(element);
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    
    /** @return getName() as UI label. */
    @Override
	@Transient
    public String getLabel() {
    	return getName();
    }
    
    @Override
	public String toString() {
        return "PoClient["+"shortName="+shortName+", name="+name+", description="+description+", uid="+uid+"]";
    }

	/** {@inheritDoc} */
	@Override
	public int compareTo(PoClient o) {
		if (o == null)
			return 1;
		if (name == null && o.getName() == null)
			return 0;
		if (name == null)
			return -1;
		if (o.getName() == null)
			return 1;
		return name.compareToIgnoreCase(o.getName());
	}

}
