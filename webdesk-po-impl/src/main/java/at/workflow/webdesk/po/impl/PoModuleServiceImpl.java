/*
 * Created on 01.02.2006
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.impl;

import java.util.List;

import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.daos.PoModuleDAO;
import at.workflow.webdesk.po.model.PoModule;

public class PoModuleServiceImpl implements PoModuleService {
	
	private PoModuleDAO moduleDAO;

    @Override
	public PoModule getModuleByName(String moduleName) {
        return this.moduleDAO.getModuleByName(moduleName);
    }

    @Override
	public void saveModule(PoModule module) {
        this.moduleDAO.save(module);        
    }

	public void setModuleDAO(PoModuleDAO moduleDAO) {
		this.moduleDAO = moduleDAO;
	}

	@Override
	public PoModule getModule(String uid) {
		return this.moduleDAO.get(uid);
	}
	
	/**
	 * This function can be used determine a corresponding 
	 * <code>PoModule</code>, which is returned  
	 * if it is possible to extract the module key from 
	 * <code>className</code>, which should have the following 
	 * form: "*\/at/workflow/webdesk/PCKNAME/*", where * can be anything
	 * <p>
	 * A backslash \\ will be replaced with a normal slash "/"
	 * @param className
	 * @return a <code>PoModule</code> if it was able to determine it 
	 * 
	 */
	@Override
	public PoModule tryToExtractModuleFromClassName(String className) {
		if(className==null)
			return null;
		className = className.replaceAll("\\\\", "/");
		String indicator = "at/workflow/webdesk/";
		String indicator2= "at.workflow.webdesk.";
		if (className.indexOf(indicator)!=-1) {
			String key = className.substring(className.indexOf(indicator)
					+ indicator.length(), className.length());
			if (key.indexOf("/")!=-1) {
				key = key.substring(0, key.indexOf("/"));
				// the key should now contain the package name, eg. po, wf, ..
				return getModuleByName(key);
			}
		}
		if (className.indexOf(indicator2)!=-1) {
			String key = className.substring(className.indexOf(indicator2)
					+ indicator2.length(), className.length());
			if (key.indexOf(".")!=-1) {
				key = key.substring(0, key.indexOf("."));
				// the key should now contain the package name, eg. po, wf, ..
				return getModuleByName(key);
			}
		}
		return null;
	}

	@Override
	public List<PoModule> loadActiveModules() {
		return this.moduleDAO.loadActiveModules();
	}

	@Override
	public List<PoModule> loadAllModules() {
		return this.moduleDAO.loadAll();
	}

	@Override
	public PoModule tryToExtractModuleFromBeanName(String beanName) {
		
		for (PoModule module : this.loadAllModules()) {
			if (module.getName()!=null && beanName.toLowerCase().startsWith(module.getName().toLowerCase()))
				return module;
		}
		
		return null;
	}

    
}
