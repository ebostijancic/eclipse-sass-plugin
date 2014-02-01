package at.workflow.webdesk.po.model;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.lang.time.DateUtils;
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
import at.workflow.webdesk.tools.api.User;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;
import at.workflow.webdesk.tools.model.annotations.UiCollectionHint;
import at.workflow.webdesk.tools.model.annotations.UiFieldHint;
import at.workflow.webdesk.tools.model.annotations.UiHint;

/**
 * Represents the Person entity inside the system.  A Person object has 2 functions: it acts as 
 * a user (if the property active is enabled) and an employee of the managed company, whose
 * data can be analyzed.
 * If the property activeUser is enabled the person defined here can login into the system 
 * with his username defined and depending on the actual authentication type choosen.
 * The person MUST be linked to a valid hierarchical group and MUST be linked to a client.
 * If the person is deleted, its property validTo will be set to now(), so subsequent queries
 * using the current_timestamp() function will not return the current person.
 * 
 * Mandatory properties are userName, firstName, lastName, client, at least one linked 
 * hierarchical group (which has to be linked to the same client as the person itself) and employeeId.
 * EmployeeId, taId and username have to be unique within the lifetime of the person and accross
 * the system.
 * 
 * @author ggruber
 */

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Indexed
public class PoPerson extends PoDayHistorization implements Comparable<PoPerson>, User, CompetenceTarget, PermissionHolder {
	
	/** Used for gender fieldof the person. */
	public enum Gender
	{
		MALE, FEMALE
	}
	
	/** the unique identifier of this object */
	@Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name="PERSON_UID", length=32)	
    @DocumentId
	private String UID;

	/** the Persons client */
	@ManyToOne (fetch=FetchType.EAGER)
	@JoinColumn (name="CLIENT_UID", nullable = false)	// has been optional since 17.5.2009, but is required in all databases
	@ForeignKey (name="FK_PERSON_CLIENT")
	@UiFieldHint(update = false)
	private PoClient client;

	/** if true, the Person is an active Webdesk User	 */
	@Column(length=4)	
	private boolean activeUser = true;
	
	/** the username of the person, must be unique in complete system. Is used to login. */
	@Column(length=60, nullable=false)	
	@Field(index=Index.UN_TOKENIZED)
	private String userName;

	/** the internal (readable) employeeId of the person, must be unique in the complete system within its validity **/
	@Column(length=20)
	@Field(index=Index.UN_TOKENIZED)
	private String employeeId;

	/** first name of the Person **/
	@Column(length=80, nullable=false)
	@Field(index=Index.TOKENIZED)
	private String firstName;
	
	/** last name of the Person **/
	@Column(length=80, nullable=false)
	@Field(index=Index.TOKENIZED)
	private String lastName;
	
	/** internet-mail address of the person */
	@Field(index=Index.TOKENIZED)
	@UiFieldHint(isEmail = true)
	private String email;
	
	/** ID of the person inside the connected Time+Attendance system. **/
	@Column(length=20)
	private String taID;
	
	/** ISO-639 language- (or ISO-3166 country-) code the persons preferred language used in the applications GUI */
	@Column(length=10)
    @UiFieldHint(referenceValueBeanClass = PoLanguage.class, referenceValuePropertyName = "code")
	private String languageCode;
	
	/** List of RoleDeputy associations -> persons which have been assigned locally as deputy roleholders **/
	@OneToMany(mappedBy="officeHolder")	
	@UiHint(ignore = true)
	private List<PoRoleDeputy> deputies;
	
	/** defines whether a central or decentral defined deputy may approve/decline/edit workflow tasks in the name of this person */
	private boolean deputyMayApprove;
	
	/** defines whether the person wants to be informed in case of a new workflow task */
	private boolean mailNotificationOnAssignment;
	
	/** defines whether the person wants to be informed in case of a new workflow task, where the person is not the direct
	 * assignee, but a deputy **/
	@Column (name="MailNotifOnAssignAsDeputy")	
	private boolean mailNotificationOnAssignmentAsDeputy;

	/** should the current person be notified upon new workflow tasks via popup inside the application on startup **/  
	private Boolean popupNotificationOnStartup;
	
	/** List of PoPersonGroup associations */
	@OneToMany(
			/** cascade=CascadeType.ALL, replaced by handling in PoOrganisationService.
			 * reason: to allow processing of whole timeline as e.g. in HR module **/ 
			mappedBy="person")
	@UiCollectionHint(tab="Organisation")
	private Collection<PoPersonGroup> memberOfGroups = new HashSet<PoPersonGroup>();
	
	/** Collection of RoleCompetencePerson associations: where is the person a direct competence of a roleholder **/
	@OneToMany(cascade=CascadeType.ALL, mappedBy="competence4Person")	
	@UiHint(ignore = true)
	private Collection<PoRoleCompetencePerson> referencedAsCompetenceTarget = new HashSet<PoRoleCompetencePerson>();
	
	/** Collection of RoleHolderPerson associations: where is the person a roleholder **/
	@OneToMany(cascade=CascadeType.ALL, mappedBy="person")	
	@UiHint(ignore = true)
	private Collection<PoRoleHolderPerson> referencedAsRoleHolder = new HashSet<PoRoleHolderPerson>();
	
	/** Collection of PoAPermissionPerson associations: which actions are directly assigned **/
	@OneToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE}, mappedBy="person")	
	@UiHint(ignore = true)
	private Collection<PoAPermissionPerson> permissions = new HashSet<PoAPermissionPerson>();

	/** person's bank account information */
	@UiCollectionHint(tab = "Finanzen", detailPanel = true)
	@OneToMany(mappedBy="person") // no cascading, saved by historization processing
	private Collection<PoPersonBankAccount> bankAccounts = new HashSet<PoPersonBankAccount>();
	
	/** person's date of Birth */
	private Date dateOfBirth;
	

	/* +++ office contact information ++ */
	
	@Column(length=40)
    private String officePhoneNumber;
	
	@Column(length=20)
    private String officeFaxPhoneNumber;
    
    @Column(length=100)
    private String officeCity;
    
    @Column(length=100)
    @UiFieldHint(keyValueTypeId="PoCountry")
    private String officeCountry;
    
    @Column(length=20)
    private String officeZip;
    
    private String officeStreetAddress;
    
    private String cellPhoneNumber;
    
    private Gender gender;
    
    @Column(length=40)
	@UiFieldHint(keyValueTypeId = "HrTitle")	// TODO: this should be named PoTitle and the according KeyValueType should be (available also in HR) in PO
    private String title;
    
    /** person's image and thumbnail */
    @OneToOne (fetch=FetchType.EAGER)
	@JoinColumn (name="PERSONIMAGES_UID")
	@ForeignKey (name="FK_PERSON_PERSONIMAGES")    
    @UiHint(ignore = true)
    private PoPersonImages personImages;
    
	
    @Override
	public String getUID() {
		return UID;
	}

    @Override
	public void setUID(String uid) {
		this.UID = uid;
	}

	public PoClient getClient() {
		return this.client;
	}
	
	public void setClient(PoClient client) {
		this.client = client;
	}
	
	public boolean isActiveUser() {
		return this.activeUser;
	}
	
	public void setActiveUser(boolean active) {
		this.activeUser = active;
	}

	/**
	 * @return a <code>Set</code> of the assigned <code>PoPersonGroup</code>s.
	 * 		Mind that expired <code>PoPersonGroup</code> objects are returned as well. 
	 */
    public Collection<PoPersonGroup> getMemberOfGroups() {
        return memberOfGroups;
    }

    /** @return <code>true</code> if the <code>element</code> was successfully added, <code>false</code> otherwise. */
    public boolean addMemberOfGroup(PoPersonGroup element) {
    	element.setPerson(this);
        return memberOfGroups.add(element);
    }

    public void setMemberOfGroups(Collection<PoPersonGroup> groups) {
    	this.memberOfGroups = groups;
    }
    
    public void setReferencedAsCompetenceTarget(Collection<PoRoleCompetencePerson> referencedAsCompetenceTarget) {
		this.referencedAsCompetenceTarget = referencedAsCompetenceTarget;
	}
    
    /**
     * @return a <code>List</code> of <code>PoRoleDeputy</code> objects.
     * 		A given <code>PoRoleDeputy</code> is linked with another <code>PoPerson</code>,
     * 		which acts as deputy (only if it is still valid).
     */
	public List<PoRoleDeputy> getDeputies() {
		return deputies;
	}

	public void setDeputies(List<PoRoleDeputy> deputies) {
		this.deputies = deputies;
	}
	
	public void addDeputy(PoRoleDeputy deputy) {
		deputy.setDeputy(this);
		this.deputies.add(deputy);
	}
    
    public Collection<PoRoleCompetencePerson> getReferencedAsCompetenceTarget() {
        return referencedAsCompetenceTarget;
    }

    public boolean addReferencedAsCompetenceTarget(PoRoleCompetencePerson element) {
    	element.setCompetence4Person(this);
        return referencedAsCompetenceTarget.add(element);
    }

    public Collection<PoRoleHolderPerson> getReferencedAsRoleHolder() {
        return referencedAsRoleHolder;
    }

    public void setReferencedAsRoleHolder(Collection<PoRoleHolderPerson> value) {
        referencedAsRoleHolder = value;
    }

    /** @return <code>true</code> if the <code>element</code> was added, <code>false</code> otherwise. */
    public boolean addReferencedAsRoleHolder(PoRoleHolderPerson element) {
    	element.setPerson(this);
        return referencedAsRoleHolder.add(element);
    }

    public Collection<PoAPermissionPerson> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<PoAPermissionPerson> value) {
        permissions = value;
    }
   
    public boolean addPermission(PoAPermissionPerson element) {
    	element.setPerson(this);
        return permissions.add(element);
    }

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public void setFirstName(String name) {
		this.firstName = name;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date DateofBirth) {
		this.dateOfBirth = DateofBirth;
	}

    /** This is the Interflex-6020 identity of the person. */
	public String getTaID() {
		return taID;
	}

	public void setTaID(String taID) {
		this.taID = taID;
	}
    
    /** This is the organization-specific identity of the person. */
    public String getEmployeeId() {
        return employeeId;
    }

	public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

	@Override
    public String getUserName() {
        return userName;
    }
    
    @Override
	public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public boolean isDeputyMayApprove() {
 		return deputyMayApprove;
 	}
 	
    public void setDeputyMayApprove(boolean deputyMayApprove) {
 		this.deputyMayApprove = deputyMayApprove;
 	}
 	
 	public boolean isMailNotificationOnAssignment() {
 		return mailNotificationOnAssignment;
 	}
 	
 	public void setMailNotificationOnAssignment(
 			boolean mailNotificationOnAssignment) {
 		this.mailNotificationOnAssignment = mailNotificationOnAssignment;
 	}
 	
 	
 	public boolean isMailNotificationOnAssignmentAsDeputy() {
 		return mailNotificationOnAssignmentAsDeputy;
 	}
 	
 	public void setMailNotificationOnAssignmentAsDeputy(
 			boolean mailNotificationOnAssignmentAsDeputy) {
 		this.mailNotificationOnAssignmentAsDeputy = mailNotificationOnAssignmentAsDeputy;
 	}
    
    public String getOfficeCity() {
        return officeCity;
    }
    
	public void setOfficeCity(String officeCity) {
        this.officeCity = officeCity;
    }
	
    public String getOfficeCountry() {
        return officeCountry;
    }

	public void setOfficeCountry(String officeCountry) {
        this.officeCountry = officeCountry;
    }

    public String getOfficeFaxPhoneNumber() {
        return officeFaxPhoneNumber;
    }

	public void setOfficeFaxPhoneNumber(String officeFaxPhoneNumber) {
        this.officeFaxPhoneNumber = officeFaxPhoneNumber;
    }
    
    public String getOfficePhoneNumber() {
        return officePhoneNumber;
    }

	public void setOfficePhoneNumber(String officePhoneNumber) {
        this.officePhoneNumber = officePhoneNumber;
    }
    
    public String getOfficeZip() {
        return officeZip;
    }
	
    public void setOfficeZip(String officeZip) {
        this.officeZip = officeZip;
    }
    
    public String getOfficeStreetAddress() {
        return officeStreetAddress;
    }
    
    public void setOfficeStreetAddress(String officeStreetAddress) {
        this.officeStreetAddress = officeStreetAddress;
    }
    
    @Override
	public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
       
    public boolean getPopupNotificationOnStartup() {
        if (popupNotificationOnStartup==null)
            return true;
        return popupNotificationOnStartup.booleanValue();
    }

    public void setPopupNotificationOnStartup(Boolean popupNotificationOnStartup) {
         this.popupNotificationOnStartup = popupNotificationOnStartup;
    }
    
    @Override
    public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
    
    @Override
	@Transient
    public String getWorkflowId() {
    	return this.UID;
    }
    
	@Transient
	public String getFullName() {
		if (lastName == null && firstName == null)
			return "fn and ln not defined";
		return lastName + " " + firstName;
	}

    @Override
	public int compareTo(PoPerson o) {
        // LastName + Vorname ist das sortierkriterium, falls sortiert werden sollte!
        String compStr = o.getLastName() + o.getFirstName();
        return ((getLastName() + getFirstName()).compareTo(compStr));
    }
    
    /**
     * @return the currently assigned hierarchical OrgUnit (department) of the person.
     * 		If the person has been historicized (validto is in the past), this should return the
     * 		OrgUnit where the person was assigned just before it was historicized. 
     */
    @Transient
    public PoGroup getOrgUnit() {
    	for (PoPersonGroup personGroup : getMemberOfGroups()) {
    		if (HistorizationHelper.isValid(personGroup) && HistorizationHelper.isValid(personGroup.getGroup())
    				&& personGroup.getGroup().getOrgStructure().getOrgType() == PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY) {
    			return personGroup.getGroup();
    		}
    	}
    	
    	if (getValidto().before( DateTools.now()) ) {
    		
    		Date lastValidMoment = DateUtils.addSeconds(getValidto(), -1);
    		
    		// search for last OrgUnit
        	for (PoPersonGroup personGroup : getMemberOfGroups()) {
        		if (HistorizationHelper.isValid(personGroup, lastValidMoment) && HistorizationHelper.isValid(personGroup.getGroup(), lastValidMoment)
        				&& personGroup.getGroup().getOrgStructure().getOrgType() == PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY) {
        			return personGroup.getGroup();
        		}
        	}
    	}
    	
    	// this should never happen!
    	return null;
    }
    
    /** @return getFullName() as UI label. */
    @Override
	@Transient
    public String getLabel() {
    	return getFullName();
    }
    
    
	@Column(length=20)
	public String getCellPhoneNumber() {
		return cellPhoneNumber;
	}

	public void setCellPhoneNumber(String cellPhoneNumber) {
		this.cellPhoneNumber = cellPhoneNumber;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public PoPersonImages getPersonImages() {
		return this.personImages;
	}
	
	public void setPersonImages(PoPersonImages personImages) {
		this.personImages = personImages;
	}

	public Collection<PoPersonBankAccount> getBankAccounts() {
		return bankAccounts;
	}

	public void setBankAccounts(Collection<PoPersonBankAccount> bankAccounts) {
		this.bankAccounts = bankAccounts;
	}
	
	public void addBankAccount(PoPersonBankAccount bankAccount) {
		this.bankAccounts.add(bankAccount);
		bankAccount.setPerson(this);
	}
}
