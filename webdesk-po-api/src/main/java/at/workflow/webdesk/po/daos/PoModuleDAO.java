package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoModule;

public interface PoModuleDAO extends GenericDAO<PoModule> {
    /**
     * @param moduleName  the name of the module
     * @return a PoModule with the given name, null otherwise.
     */
    public PoModule getModuleByName(String moduleName);
    
    public List<PoModule> loadActiveModules();
    
    
	
}
