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
public interface PoAPermissionVisitor {
    public void visit(PoAPermissionRole apr);
    public void visit(PoAPermissionClient apc);
    public void visit(PoAPermissionPerson app);
    public void visit(PoAPermissionGroup apg);
    
    public String getWhichClassVisited();
    
    public PoAPermissionBase getVisitedObject();
}
