/*
 * Created on 19.09.2005
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.model;

/**
 * @author hentner
 *
 */
public class PoAPermissionAdapter implements PoAPermissionVisitor{
    
    String whichClassVisited;
    PoAPermissionBase apb;
    
    
    @Override
	public String getWhichClassVisited() {
        return whichClassVisited;
    }
    public void setWhichClassVisited(String whichClassVisited) {
        this.whichClassVisited = whichClassVisited;
    }
    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.model.PoAPermissionVisitor#visit(at.workflow.webdesk.po.model.PoAPermissionRole)
     */
    @Override
	public void visit(PoAPermissionRole apr) {
        apb = apr;
        whichClassVisited = "AR";
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.model.PoAPermissionVisitor#visit(at.workflow.webdesk.po.model.PoAPermissionClient)
     */
    @Override
	public void visit(PoAPermissionClient apc) {
        apb = apc;
        whichClassVisited = "AC";
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.model.PoAPermissionVisitor#visit(at.workflow.webdesk.po.model.PoAPermissionPerson)
     */
    @Override
	public void visit(PoAPermissionPerson app) {
        apb = app;
        whichClassVisited = "AP";
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.model.PoAPermissionVisitor#visit(at.workflow.webdesk.po.model.PoAPermissionGroup)
     */
    @Override
	public void visit(PoAPermissionGroup apg) {
        apb = apg;
        whichClassVisited="AG";
    }
    
    @Override
	public PoAPermissionBase getVisitedObject() {
        return apb;
    }
}
