package at.workflow.webdesk.po.model;

import java.util.Collection;
import java.util.HashSet;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.tools.model.annotations.UiHints;

/**
 * Represents an organizational structure.
 * 
 * @author ggruber
 */
@UiHints(
	isSmallList = true,
	autoCompleteJavaScript = "name + ' (' + client.name + ')' "
)

public class PoOrgStructure extends PoBase	{

	/** Possible value for orgType property. */
    public static final int STRUCTURE_TYPE_ORGANISATION_HIERARCHY = PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY;
	/** Possible value for orgType property. */
    public static final int STRUCTURE_TYPE_COSTCENTERS = PoConstants.STRUCTURE_TYPE_COSTCENTERS;
	/** Possible value for orgType property. */
    public static final int STRUCTURE_TYPE_PROJECTHIERARCHY = PoConstants.STRUCTURE_TYPE_PROJECTHIERARCHY;
	/** Possible value for orgType property. */
    public static final int STRUCTURE_TYPE_CUSTOMGROUPS = PoConstants.STRUCTURE_TYPE_CUSTOMGROUPS;
	/** Possible value for orgType property. */
    public static final int STRUCTURE_TYPE_LOCATIONS = PoConstants.STRUCTURE_TYPE_LOCATIONS;

	private String uid;
	private String name;
	private String description;
    private PoClient client;										// link to the client
    private int orgType;											// is a generalization option of the next 2 props (org-hierarchy => hierarchy+onlySingleGMS, costcenter => non-hierarchy...)
    																// custom groups does not define hierarchy or onlySingleGMS
    private boolean hierarchy;										// if it is hierarchical, links from groups to child and parent groups exist
    private boolean allowOnlySingleGroupMembership;					// if true, one person can be only member of a single group of this orgstructure at a time
    
    private Collection<PoGroup> groups = new HashSet<PoGroup>();	// Set of groups which belong to this orgStructure
    
    
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

    public boolean isHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(boolean hierarchy) {
        this.hierarchy = hierarchy;
        
        // TODO: isn't setOrgType(PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY) missing here when true is given?
    }

    public PoClient getClient() {
        return client;
    }

    public void setClient(PoClient client) {
        this.client = client;
    }

    public Collection<PoGroup> getGroups() {
        return groups;
    }

    public void setGroups(Collection<PoGroup> values) {
        groups = values;
    }

    public boolean addGroup(PoGroup element) {
    	element.setOrgStructure(this);
        return groups.add(element);
    }

    /** One of PoOrgStructure.PoOrgStructure.STRUCTURE_TYPE_* values. */
    public int getOrgType() {
        return orgType;
    }

    public void setOrgType(int orgType) {
        this.orgType = orgType;
        
        if (orgType == PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY) {
        	this.hierarchy = true;
        }
    }
    
    public boolean isAllowOnlySingleGroupMembership() {
    	return allowOnlySingleGroupMembership;
    }
    
    public void setAllowOnlySingleGroupMembership(boolean allowOnlySingleGroupMembership) {
    	this.allowOnlySingleGroupMembership = allowOnlySingleGroupMembership;
    }
    
    
    @Override
	public String toString() {
        String ret = "PoOrgStructure[" + 
            ", name=" + this.name +
            ", description=" + this.description + 
            ", orgType=" + this.orgType +
            ", uid=" + this.uid + "]";
        return ret;
    }


    /**
     * @param orgType the organizational type to be turned into a i18n-key.
     * @return the i18n-key belonging to given <i>PoOrgSTructure.orgType</i>.
     */
	public static String orgTypeToString(int orgType)	{
		if (orgType == STRUCTURE_TYPE_ORGANISATION_HIERARCHY) {
			return "po_hierarchical_organisation";
		} else if (orgType == STRUCTURE_TYPE_COSTCENTERS) {
			return "po_costcenter";
		} else if (orgType == STRUCTURE_TYPE_PROJECTHIERARCHY) {
			return "po_projectgroups";
		} else if (orgType == STRUCTURE_TYPE_CUSTOMGROUPS) {
			return "po_unfixed_group";
		} else if (orgType == STRUCTURE_TYPE_LOCATIONS) {
			return "po_locations";
		}
		throw new IllegalArgumentException("Unknown orgType (please implement it): "+orgType);
	}

}
