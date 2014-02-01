package at.workflow.webdesk.po.model;

/**
 * <p>
 * Link Interface
 * provides possibility to navigate to Roleholder
 * for Objects PoRoleHolderPerson and PoRoleHolderGroup
 * <p>
 * Consider that there is one special case, when the 
 * <code>PoRoleHolderLink</code> object is of type 
 * <code>PoRoleHolderDynamic</code> due to the inexisting
 * <code>uid</code>.
 * 
 * @author ggruber
 * @author hentner
 * 
 * 
 */
public interface PoRoleHolderLink {
    
	static final String PERSON = "Person";
	static final String GROUP = "Group";
	static final String DYNAMIC = "Dynamic";
	
	/**
	 * @return
	 * @deprecated use getRoleCompetenceBase instead.
	 */
	PoRoleCompetenceBase getRoleHolder();
	
	/**
	 * @return a <code>PoRoleCompetenceBase</code> instance.
	 */
	PoRoleCompetenceBase getRoleCompetenceBase();
	
    
    /**
     * @return the <code>uid</code> of the <code>PoRoleHolderPerson</code> or
     * <code>PoRoleHolderGroup</code> object. If the implementation is 
     * of type <code>PoRoleHolderDynamic</code> the <code>roleHolderType</code> attribute
     * is returned. (which is internally stored as <code>int</code>.)
     * 
     */
    String getRoleHolderObjectUID();
}
