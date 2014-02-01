package at.workflow.webdesk.po;

/**
 *<p>
 * Error's, Failure's and Warnings 
 * all sections have their own co-domain. 
 * </p><p>
 * Also internal errors should be listed here, these are errors that
 * are not mentioned to the user, but are logged for subsequent
 * maintenance.
 * </p>
 * Created on 15.03.2005
 * @author ggruber
 */
public class PoRuntimeException extends RuntimeException {
    
	// Format PO{digit} - {a-zA-Z0-9}
	// Errors (1 - 100)
    public static final String ERROR_DELETING_GROUP_ROLEHOLDER_LINKS = "PO1 - Can't delete Group as it is referenced in a RoleHolder Object!";
    public static final String ERROR_DELETING_GROUP_ROLEHOLDER_COMPETENCE_LINKS = "PO2 - Can't delete group as it is referenced as competence target in a RoleHolder Object!";
    public static final String ERROR_DELETING_GROUP_CHILDGROUPS_EXIST = "PO3 - Can't delete Group as childgroups exist!";        
    public static final String ERROR_DELETING_GROUP_LINKEDPERSONS_EXIST = "PO4 - Can't delete Group as linked persons exist!";
    public static final String ERROR_SET_PARENT_NOT_YET_VALID = "PO5 - Parent is not yet valid!";
    public static final String ERROR_SET_PARENT_NOT_SO_LONG_VALID = "PO6 - Parent is not long enough valid!";
    public static final String ERROR_SET_CHILD_NOT_YET_VALID = "PO7 - Child is not yet valid!";
    public static final String ERROR_SET_CHILD_NOT_SO_LONG_VALID = "P08 - Child is not long enough valid";
    public static final String ERROR_MORE_THAN_ONE_ROLE_HOLDERS_EXISTS = "P09 - More than one Role Holder exists at the selected time.";
    public static final String ERROR_MORE_THAN_ONE_ROLE_HOLDERS_COMPETENCE_GROUP_EXISTS = "P10 - More than one Role Holder with competence for group exists at the selected time.";
    public static final String ERROR_MORE_THAN_ONE_ROLE_HOLDERS_COMPETENCE_PERSON_EXISTS = "P11 - More than one Role Holder with competence for person exists at the selected time.";
    public static final String ERROR_ROLE_HOLDER_DUPLICATE_ENTRY = "P12 - It is not allowed to insert. It seems that this is already done.";
    public static final String ERROR_GROUP_TOO_MANY_PARENTS= "P13 - Too many childs for parent.";
    public static final String ERROR_MORE_THAN_ONE_GROUP_ASSOCIATED_WITH_PERSON ="P14 - Group is already associated with person.";
    public static final String ERROR_LINK_CANNOT_BE_DELETED_NO_GROUP_ASSOCIATED = "P15 - Link to group can not be deleted. No old group exists.";
    public static final String ERROR_LINK_CANNOT_BE_DELETED_GROUP_ALREADY_ACTIVE ="P16 - Link can not be deleted because it is already active.";
    public static final String ERROR_LINK_CANNOT_BE_DELETED_LINK_ALREADY_ACTIVE ="P17 - Link can not be deleted because it is already active.";
    public static final String ERROR_LINK_CANNOT_BE_CHANGED_LINK_IS_NOT_IN_FUTURE="P18 - The validity of the link can not be changed. ";
    public static final String ERROR_DATES_CANNOT_BE_APPLIED ="P19 - The given dates can not be applied. Check if the valid to field is after the valid from and if all other objects are valid at the passed date. ";
    public static final String ERROR_NO_HIERARCHICAL_GROUP_EXISTS ="P20 - No hierarchical group exists for person!";
    public static final String ERROR_TRY_TO_DELETE_HIERARCHICAL_GROUP="P21 - Can not delete an assignment to a hierarchical group. Use a new assignment instead.";
    public static final String ERROR_TRY_TO_ASSIGN_ITSELF_AS_PARENT ="P22 - Can not assign itself as parent. ";
    public static final String ERROR_TRY_TO_ASSIGN_CHILD_AS_PARENT ="P23 - Parent is already a child of the given child.";
    public static final String ERROR_VIEW_PERMISSION_TYPE_NOT_VALID ="P24 - Only PoAPermissionRole Objects are allowed to have viewType = 3.";
    public static final String ERROR_TRY_TO_DELETE_DEFAULT_LANGUAGE ="P25 - It's not allowed to delete the default language.";
    
    public static final String ERROR_NO_MAILSERVER_SPECIFIED = "P26 - There's no mail-server spcified for this person";
    public static final String ERROR_NO_LASTNAME_SPECIFIED = "P28 - There's no last-name specified for this person";
    public static final String ERROR_NO_USERNAME_SPECIFIED = "P27 - There's no username (= ldap-name) specified for this person";
    
    public static final String ERROR_TRY_TO_ASSIGN_A_TOP_LEVEL_GROUP = "P38 - Cannot assign a top-level-group to another group";
    
    public static final String ERROR_GROUP_ALREADY_ASSIGNED_TO_PERSON = "P29 -  Group already assigned to person.";
    public static final String ERROR_GROUP_WITH_NAME_ALREADY_DEFINED = "P30 - Group with given name already defined.";
    public static final String ERROR_CLIENT_HAS_ALREADY_HIERARCHICAL_OS= "P31 - Client has already a hierarchical OrgStructure.";
    public static final String ERROR_DELETE_NOT_ALLOWED="P32 - Group can not be deleted";
    public static final String ERROR_FOLDER_HAS_SUBFOLDERS="P33 - Erasure of folder is not allowed, as long as actions or sub-folders exist.";
    public static final String ERROR_ACTION_HAS_NO_FILES="P34 - Config (Action) has no corresponding files.";
    public static final String JOBCONFIG_WITHOUT_PARENT = "P35 - Can't save configurable Job without knowledge of the parent job";
    public static final String ERROR_CANNOT_COPY_A_TREE_TO_THE_SAME_CLIENT = "P36 - Can't copy a tree from one client to the same client.";
    public static final String ERROR_NO_SHORTNAME_SPECIFIED = "P37 - There*s no valid shortname specified for this group";
    // Failures (101 - 200)
    public static final String FAILURE_SET_PARENT = "PO101 - More than one groups interfere with parent.";
    // Warnings (201 - 300)
    public static final String WARNING_DELETE_ROLE = "PO201 - Do you really want to delete all role holders and action permissions?";
    
    
    // Messages (301 - )
    
    public static final String MESSAGE_DUPLICATE_PERSON_IMAGES ="The images belong to another user.";
    public static final String MESSAGE_DUPLICATE_USERNAME ="User with given Username already exists.";
    public static final String MESSAGE_DUPLICATE_TAID = "User with given TaId already exists.";
    public static final String MESSAGE_DUPLICATE_EMPLOYEE_ID = "User with given Employee Id already exists.";
    public static final String MESSAGE_DUPLICATE_LDAP = "User with given LDAP Name already exists.";
    public static final String MESSAGE_DUPLICATE_GROUP_CLIENT_NAME = "Group with given tuple client and name already exists";
    public static final String MESSAGE_DUPLICATE_GROUP_CLIENT_SHORTNAME = "Group with given tuple client and shortname already exists";
    public static final String MESSAGE_DUPLICATE_ACTIONTYPE_NAME = "Action with given tuple name and type already exists";
    public static final String MESSAGE_DUPLICATE_TEXTMODULE_NAME_AND_LANG = "Textmodule with given name and language already exits.";
    public static final String MESSAGE_DUPLICATE_ROLE_NAME_AND_CLIENT = "Role with given name and client already exists.";
    public static final String MESSAGE_DUPLICATE_ROLE_PARTICIPANT_AND_CLIENT = "Role with given participant-id and client already exists.";
    public static final String TRIGGER_ALREADY_LINKED_WITH_JOB = "Trigger is already linked with a Job";
    

    public static final String ERROR_PASSWORDQUALITY_MIN_LENGTH = "Minmimale Passwort-Länge ist {0}.";
    public static final String ERROR_PASSWORDQUALITY_UPPER_LOWER_CHARS = "Es müssen Groß- und Kleinbuchstaben vorkommen.";
    public static final String ERROR_PASSWORDQUALITY_MIN_DIGITS = "Das Passwort muß mindestens {0} Ziffern enthalten.";
    public static final String ERROR_PASSWORDQUALITY_MIN_SPECIALCHARS = "Das Passwort muß mindestens {0} Sonderzeichen enthalten.";
    public static final String ERROR_PASSWORDQUALITY_NUMBER_OF_DIFFERING_LATEST = "Das Passwort muß sich von den letzten {0} Passwörtern unterscheiden.";
    
    public static final String MAIL_ADDRESS_NOT_AVAILABLE_FOR_PASSWORD_RESET = "Keine E-Mail Addresse ist verfügbar für die Passwort-Rücksetzungs-Nachricht!";
    
    
    public PoRuntimeException() {
        super();
    }

    public PoRuntimeException(String message) {
        super(message);
    }

    public PoRuntimeException(Throwable e) {
        super(e);
        super.setStackTrace(e.getStackTrace());
    }

    public PoRuntimeException(String s, Throwable e) {
        super(s, e);
        super.setStackTrace(e.getStackTrace());
    }
}
