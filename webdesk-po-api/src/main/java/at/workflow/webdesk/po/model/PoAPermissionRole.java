package at.workflow.webdesk.po.model;

/**
 * Maps an action to a PoRole for a specific timeframe. If an action is mapped this 
 * way, it reflects, that all roleholders of this PoRole (persons & grouops) are allowed
 * to use the linked action. (PoAction Object).<br> If the corresponding PoAction is
 * used to view personal data the range of this view permission is limited to the 
 * CompetenceTarget definition of the corresponding RoleHolder object. F.i. if 
 * Weiss Florian gets the PoAction "Journal" via the PoRole "manager" where he is
 * assigned as PoRoleCompetenceAll for the department W4711 it means he can view personaldata
 * (if the action supports it) of this department W4711.<br>
 * This permission type is also used to assign "special" permissions where no organisationl
 * structure can be used. In this case a "dummy role" is created for this purpose.
 * The user to link the action to, is assigned as roleholder and the users for which 
 * personal data should be allowed to view for that user are defined as competence targets
 * within the PoRoleCompetenceAll definition.
 * 
 * Created on 16.06.2005
 * @author Gabriel Gruber, Harald Entner
 */
@SuppressWarnings("serial")
public class PoAPermissionRole extends PoAPermissionBase {
    
	/** This flag indicates that the viewPermission of Groups is extended by its sub-groups. */ 
	private boolean inheritToChilds;
	private Boolean viewInheritToChilds;
    private PoRole role;

    /**
     * @return PoRole object of role the action is assigned to.
     */
    public PoRole getRole() {
        return role;
    }

	@Override
	public void accept(PoAPermissionVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * @param role: PoRole object to assign Action to. 
     */
    public void setRole(PoRole role) {
        this.role = role;
    }

    /**  
     * This flag indicates that the viewPermission of Groups is  extended by its sub-groups.
     * @hibernate.property
     */
    public boolean isInheritToChilds() {
        return inheritToChilds;
    }
    
    /**
     * Defines whether the owning group inherits its permissions to its child groups.
     * This only applies if the role holder (owner) of the <code>PoRole</code> is  a <code>PoGroup</code>
     */
    public void setInheritToChilds(boolean inheritToChilds) {
        this.inheritToChilds = inheritToChilds;
    }
    
    public boolean isViewInheritToChilds() {
        if (viewInheritToChilds == null)
            return false;
        return viewInheritToChilds.booleanValue();
    }

    public void setViewInheritToChilds(Boolean viewInheritToChilds) {
        this.viewInheritToChilds = viewInheritToChilds;
    }

}
