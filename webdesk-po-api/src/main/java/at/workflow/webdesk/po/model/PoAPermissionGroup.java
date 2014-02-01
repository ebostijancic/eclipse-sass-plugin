/*
 * Created on 16.06.2005
 *
 */
package at.workflow.webdesk.po.model;

/**
 * Maps an action to a PoGroup for a specific timeframe. If an action is mapped this 
 * way, it reflects, that all members of this PoGroup (persons) are allowed
 * to use the linked action. (PoAction Object)<br><br>
 * If the attribute InheritToChilds is set, it means that not only direct members of 
 * the corrsponding group, but also all members of linked subgroups are allowed to 
 * use the referenced PoAction.
 * 
 * @author Gabriel Gruber, Harald Entner
 */

public class PoAPermissionGroup extends PoAPermissionBase {

    
	private static final long serialVersionUID = 1L;

	@Override
	public void accept(PoAPermissionVisitor visitor) {
        visitor.visit(this);
    }
    
    private PoGroup group;
    private boolean inheritToChilds;

    /**
     * @return PoGroup which has the permission on the referenced PoAction
     *  
     */
    public PoGroup getGroup() {
        return group;
    }

    public void setGroup(PoGroup group) {
        this.group = group;
    }


    public boolean isInheritToChilds() {
        return inheritToChilds;
    }

    public void setInheritToChilds(boolean inheritToChilds) {
        this.inheritToChilds = inheritToChilds;
    }

}
