package at.workflow.webdesk.po.model;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import at.workflow.webdesk.po.CompetenceTarget;
import at.workflow.webdesk.po.PermissionHolder;
import at.workflow.webdesk.tools.model.annotations.UiCollectionHint;
import at.workflow.webdesk.tools.model.annotations.UiFieldHint;
import at.workflow.webdesk.tools.model.annotations.UiHint;
import at.workflow.webdesk.tools.model.annotations.UiHints;
import at.workflow.webdesk.tools.model.annotations.UniqueInTime;

/**
 * Represents an OrgUnit, Costcenter or a loose Group. Its members are PoPerson objects and membership
 * can change over time.
 * 
 * Created on 10.03.2005
 * @author ggruber
 */

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Indexed
@UiHints(orderBy = "name, shortName")
public class PoGroup extends PoDayHistorization implements Comparable<PoGroup>, CompetenceTarget, PermissionHolder   {

	private static final long serialVersionUID = 1L;	// TODO: is this really needed?

	/** the unique identifier of this object */
	@Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name="GROUP_UID", length=32)	
    @DocumentId
    private String UID;
	
	@Column(length=20, nullable=false)
	@Field(index=Index.UN_TOKENIZED)
	@UniqueInTime
    private String shortName;
	
	@Column(nullable=false)
	@Field(index=Index.TOKENIZED)
    private String name;
	
	@Field(index=Index.TOKENIZED)
    private String description;
	
	@Column(name="isTopLevel")
    private boolean topLevel;
	
	/** the orgStructure the group belongs to */
	@ManyToOne (fetch=FetchType.EAGER, optional=false)
	@JoinColumn (name="ORGSTRUCTURE_UID")
	@ForeignKey (name="FK_GROUP_ORGSTRUCTURE")
	@UiHint(ignore = true)	// fri_2013-08-07: in HR editors the orgStructure always would be "hierarchical", so don't render this property
	// sdzuban in HR the orgStructure is not always hierarchical but is handled automatically by service layer based on selected client
    private PoOrgStructure orgStructure;
	
	@ManyToOne (fetch=FetchType.EAGER, optional=false)
	@JoinColumn (name="CLIENT_UID")
	@ForeignKey (name="FK_GROUP_CLIENT")
	@UiFieldHint(update = false)
    private PoClient client;
	
	
	@OneToMany(mappedBy="group")
    private Collection<PoPersonGroup> personGroups = new HashSet<PoPersonGroup>();
	
	// relations where this group is in childGroup attribute are relations to its parents 
	@UiHint(ignoreButNotIn = "orgunit")
	@UiCollectionHint(tab = "Hierarchie")
	@OneToMany(cascade=CascadeType.ALL, mappedBy="childGroup")
    private Collection<PoParentGroup> parentGroups = new HashSet<PoParentGroup>();
	
	// relations where this group is in parentGroup attribute are relations to its children
	@UiHint(ignoreButNotIn = "orgunit")
	@UiCollectionHint(tab = "Hierarchie")
	@OneToMany(cascade=CascadeType.ALL, mappedBy="parentGroup")
    private Collection<PoParentGroup> childGroups = new HashSet<PoParentGroup>();
	
	@UiHint(ignore=true)
	@OneToMany(cascade=CascadeType.ALL, mappedBy="group")
    private Collection<PoAPermissionGroup> permissions = new HashSet<PoAPermissionGroup>();
	
	@UiHint(ignore=true)
	@OneToMany(cascade=CascadeType.ALL, mappedBy="competence4Group")
    private Collection<PoRoleCompetenceGroup> referencedAsCompetenceTarget = new HashSet<PoRoleCompetenceGroup>();
	
	@UiHint(ignore=true)
	@OneToMany(cascade=CascadeType.ALL, mappedBy="group")
	private Collection<PoRoleHolderGroup> referencedAsRoleHolder = new HashSet<PoRoleHolderGroup>();

	
    @Override
	public String getUID() {
        return UID;
    }

    @Override
	public void setUID(String uid) {
        this.UID = uid;
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

    public Collection<PoPersonGroup> getPersonGroups() {
        return personGroups;
    }

    public void setPersonGroups(Collection<PoPersonGroup> value) {
        personGroups = value;
    }

    public boolean addPersonGroup(PoPersonGroup element) {
    	element.setGroup(this);
        return personGroups.add(element);
    }

    public PoOrgStructure getOrgStructure() {
        return orgStructure;
    }

    public void setOrgStructure(PoOrgStructure orgStructure) {
        this.orgStructure = orgStructure;
    }

    public PoClient getClient() {
        return client;
    }

    public void setClient(PoClient client) {
        this.client = client;
    }

    public Collection<PoParentGroup> getParentGroups() {
        return parentGroups;
    }

    public void setParentGroups(Collection<PoParentGroup> value) {
        parentGroups = value;
    }

    public boolean addParentGroup(PoParentGroup element) {
    	element.setChildGroup(this);
        return parentGroups.add(element);
    }

    public Collection<PoParentGroup> getChildGroups() {
        return childGroups;
    }

    public void setChildGroups(Collection<PoParentGroup> value) {
        childGroups = value;
    }

    public boolean addChildGroup(PoParentGroup element) {
    	element.setParentGroup(this);
        return childGroups.add(element);
    }

    public Collection<PoRoleCompetenceGroup> getReferencedAsCompetenceTarget() {
        return referencedAsCompetenceTarget;
    }

    public void setReferencedAsCompetenceTarget(Collection<PoRoleCompetenceGroup> value) {
        referencedAsCompetenceTarget = value;
    }

    public boolean addReferencedAsCompetenceTarget(PoRoleCompetenceGroup element) {
    	element.setCompetence4Group(this);
        return referencedAsCompetenceTarget.add(element);
    }

    public Collection<PoRoleHolderGroup> getReferencedAsRoleHolder() {
        return referencedAsRoleHolder;
    }

    public void setReferencedAsRoleHolder(Collection<PoRoleHolderGroup> value) {
        referencedAsRoleHolder = value;
    }

    public boolean addReferencedAsRoleHolder(PoRoleHolderGroup element) {
    	element.setGroup(this);
        return referencedAsRoleHolder.add(element);
    }

    public Collection<PoAPermissionGroup> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<PoAPermissionGroup> value) {
        permissions = value;
    }

    public boolean addPermission(PoAPermissionGroup element) {
    	element.setGroup(this);
        return permissions.add(element);
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public boolean isTopLevel() {
		return topLevel;
	}

	public void setTopLevel(boolean topLevel) {
		this.topLevel = topLevel;
	}
	
    /** @return getShortName() as UI label. */
    @Override
	@Transient
    public String getLabel() {
    	return getShortName();
    }
    
    /** Sort criteria are (1) shortName and (2) name. */
    @Override
	public int compareTo(PoGroup o) {
        final String groupName1 = getShortName()+getName();
        final String groupName2 = o.getShortName()+o.getName();
        return groupName1.compareTo(groupName2);
    }

}
