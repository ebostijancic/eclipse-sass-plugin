package at.workflow.webdesk.po.model;

import at.workflow.webdesk.po.ActionPermission;

/**
 * Base-Class for Permissions. provides attributes to link objects with Actions.<br>
 * A permission holds a ViewPermissionType which can have one of the following values: <br/>
 * a) <i>PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON</i><br>
 * means User can view only his own personal data referenced
 * in the corresponding action<br>
 * b) <i>PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT</i><br>
 * means User can view the personal data of his own
 * department referenced in the corresponding action<br>
 * c) <i>PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS</i><br>
 * means User can view the personal ata of his own department and the 
 * whole organisational subtree referenced in the corresponding action<br>
 * d) <i>PoConstants.VIEW_PERMISSION_TYPE_DUMMY_ROLE</i><br>
 * means that the View-Permissions map to the Target of a specific linked role. 
 * Has the implication that the Action MUST be linked via an PoAPermissionRole Object.
 * means the Users are the referenced Holders of the referenced Role, and the 
 * Viewpermissions are valid for the referenced Competencetargets per RoleHolder Object.<br>
 * e) <i>PoConstants.VIEW_PERMISSION_TYPE_NULL</i><br>
 * it us used for negative permissions, User cannot view anything 
 * 
 * Created on 16.06.2005
 * @author Gabriel Gruber, Harald Entner
 */
public class PoAPermissionBase extends PoHistorization implements ActionPermission {

    
	private static final long serialVersionUID = 1L;

	public void accept(PoAPermissionVisitor visitor) {
        // do nothing
    }
    
    private String uid;
    private int viewPermissionType;
    private PoAction action;
    private String type;
    private boolean negative;

    @Override
	public PoAction getAction() {
        return action;
    }

    public void setAction(PoAction action) {
        this.action = action;
    }

    public boolean isNegative() {
		return negative;
	}

	public void setNegative(boolean negative) {
		this.negative = negative;
	}

	/**
     * Discriminator - defined in the base class.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }

	/**
	 * @return Returns the viewPermissionType.
	 */
	public int getViewPermissionType() {
		return viewPermissionType;
	}
	/**
	 * @param viewPermissionType The viewPermissionType to set.
	 */
	public void setViewPermissionType(int viewPermissionType) {
		this.viewPermissionType = viewPermissionType;
	}
}
