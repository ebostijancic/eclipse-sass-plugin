package at.workflow.webdesk.po.model;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Transient;

import at.workflow.webdesk.po.PermissionHolder;

/**
 * Represents a Role in the webdesk system, which can be assigned to either a Person, or a Group.
 * There are also general roles, where EVERYBODY is a member. And there are Dynamic Roles where
 * the members are different to the competence targets.
 * 
 * Competence Targets define for WHOM someone is a role member. Typically this defines the 
 * competence of a manager, which leads a specific orgunit.
 * 
 * Created on 10.03.2005
 * @author ggruber
 */
public class PoRole extends PoHistorization implements PermissionHolder {

	private static final long serialVersionUID = 1L;	// TODO: is this really needed?
	
	public static final int NORMAL_ROLE = 1;
    public static final int DUMMY_ROLE = 2;
    
	
	// FIXME: rename this to searchdirection 
	// as it is no inheritance actually !!!!
	private int directionOfInheritance;
	private int levelsToSearch;
	private int maxLevelToSearchUp;
	private Integer maxRoleHoldersToReturn;
	private PoClient client;
    private String uid;
    private String name;
    private String description;
    private Collection<PoRoleCompetenceBase> roleHolders = new HashSet<PoRoleCompetenceBase>();
    private Integer orgType;
    private Collection<PoAPermissionRole> permissions = new HashSet<PoAPermissionRole>();
    private int roleType = NORMAL_ROLE;
    private String participantId;
    private boolean doNotAllowSelfApproval;    
    private boolean doNotAllowApprovalByDeputy;    
    private boolean includeHierarchicalGroup;
    private boolean hideFromLocalAdmin;

    @Override
	public String getUID() {
        return uid;
    }
    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }

    public boolean isIncludeHierarchicalGroup() {
		return includeHierarchicalGroup;
	}
	public void setIncludeHierarchicalGroup(boolean includeHierarchicalGroup) {
		this.includeHierarchicalGroup = includeHierarchicalGroup;
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

    public PoClient getClient() {
        return client;
    }
    public void setClient(PoClient client) {
        this.client = client;
    }
    
    /**
     * @return unique collection of role competences.
     */
    public Collection<PoRoleCompetenceBase> getRoleHolders() {
        return roleHolders;
    }
    public void setRoleHolders(Collection<PoRoleCompetenceBase> roleHolders) {
        this.roleHolders = roleHolders;
    }
    public boolean addRoleHolder(PoRoleCompetenceBase element) {
    	element.setRole(this);
        return roleHolders.add(element);
    }

    /**
     * @return one of PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY,
     * 	STRUCTURE_TYPE_COSTCENTERS, STRUCTURE_TYPE_PROJECTHIERARCHY, STRUCTURE_TYPE_CUSTOMGROUPS.
     */
    public int getOrgType() {
        return (orgType == null) ? 0 : orgType.intValue();
    }
    /**
     * If a role is assigned via a loose group,
     * the orgType defines if the direction 
     * of inheritance can be considered. 
     * 
     * i think this doesn't make sense in that
     * place, this should be handled at another position
     * 
     * TODO better comments needed!
     * 
     * @param orgType one of PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY,
     * 	STRUCTURE_TYPE_COSTCENTERS, STRUCTURE_TYPE_PROJECTHIERARCHY, STRUCTURE_TYPE_CUSTOMGROUPS.
     */
    public void setOrgType(Integer orgType) {
        this.orgType = orgType;
    }

    /**
     * @return unique collection of role permissions.
     */
    public Collection<PoAPermissionRole> getPermissions() {
        return permissions;
    }
    public void setPermissions(Collection<PoAPermissionRole> permissions) {
        this.permissions = permissions;
    }
    public boolean addPermission(PoAPermissionRole element) {
    	element.setRole(this);
        return permissions.add(element);
    }

    /**
     * @return one of NORMAL_ROLE, DUMMY_ROLE or SYSTEM_ROLE.
     */
    public int getRoleType() {
        return roleType;
    }
    public void setRoleType(int roleType) {
        this.roleType = roleType;
    }

    /**
     * @return Returns the ParticipantId.
     */
    public String getParticipantId() {
        return participantId;
    }
    /**
     * @param wfParticipantId The participantId to set.
     */
    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }
    
    /**
     * @return true when the role holder is not allowed to approve his own requests.
     */
    public boolean isDoNotAllowSelfApproval() {
        return doNotAllowSelfApproval;
    }
    /**
     * @param doNotAllowSelfApproval true when the role holder is not allowed to approve his own requests.
     */
    public void setDoNotAllowSelfApproval(boolean doNotAllowSelfApproval) {
        this.doNotAllowSelfApproval = doNotAllowSelfApproval;
    }
    
    /**
     * @param directionOfInheritance one of PoConstants.SEARCH_DIRECTION_UP, PoConstants.SEARCH_DIRECTION_DOWN and PoConstants.SEARCH_DIRECTION_NONE
     */
    public void setDirectionOfInheritance(int directionOfInheritance) {
        this.directionOfInheritance = directionOfInheritance;
    }
    public int getDirectionOfInheritance() {
        return directionOfInheritance;
    }

    /**
     * @return the count of levels the authority search is allowed to search upwards in organigram.
     */
	public int getLevelsToSearch() {
		return levelsToSearch;
	}
    /**
     * @param levelsToSearch the count of levels the authority search is allowed to search upwards in organigram.
     */
	public void setLevelsToSearch(int levelsToSearch) {
		this.levelsToSearch = levelsToSearch;
	}

    /**
     * @return the number of the level the authority upwards-search is allowed to search to, inclusively, in organigram.
     * 		Top level has number 1, below is 2-n.
     * 		E.g. if maximum is 3, all levels up to inclusively 3 can be searched.
     */
	public int getMaxLevelToSearchUp() {
		return maxLevelToSearchUp;
	}
    /**
     * @param maxLevelToSearchUp the number of the level the authority upwards-search is allowed to search to, inclusively, in organigram.
     * 		Top level has number 1, below is 2-n.
     * 		E.g. if maximum is 3, all levels up to inclusively 3 can be searched.
     */
	public void setMaxLevelToSearchUp(int maxLevelToSearchUp) {
		this.maxLevelToSearchUp = maxLevelToSearchUp;
	}
	
	/**
	 * @return true when deputy of the role holder should not be able to approve requests of the role holder.
	 */
	public boolean isDoNotAllowApprovalByDeputy() {
		return doNotAllowApprovalByDeputy;
	}
	/**
	 * @param doNotAllowApprovalByDeputy true when deputy of the role holder should not be able to approve requests of the role holder.
	 */
	public void setDoNotAllowApprovalByDeputy(boolean doNotAllowApprovalByDeputy) {
		this.doNotAllowApprovalByDeputy = doNotAllowApprovalByDeputy;
	}

	/**
	 * @return the maximum number of results the authority search should return (PoRoleService.findAuthority).
	 */
	public Integer getMaxRoleHoldersToReturn() {
		return maxRoleHoldersToReturn;
	}
	/**
	 * @param maxRoleHoldersToReturn the maximum number of results the authority search should return (PoRoleService.findAuthority).
	 */
	public void setMaxRoleHoldersToReturn(Integer maxRoleHoldersToReturn) {
		this.maxRoleHoldersToReturn = maxRoleHoldersToReturn;
	}
    
    /** @return getName() as UI label. */
    @Transient
    public String getLabel() {
    	return getName();
    }
    
    @Override
    public String toString() {
        return "PoRole["+"name="+name+", orgType="+orgType+", uid="+uid+"]";
    }
	public boolean isHideFromLocalAdmin() {
		return hideFromLocalAdmin;
	}
	public void setHideFromLocalAdmin(boolean hideFromLocalAdmin) {
		this.hideFromLocalAdmin = hideFromLocalAdmin;
	}
    
}
