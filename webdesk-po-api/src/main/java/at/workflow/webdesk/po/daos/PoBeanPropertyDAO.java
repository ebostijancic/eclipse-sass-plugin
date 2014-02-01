package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoBeanProperty;

/**
 * @author ggruber
 *
 */
public interface PoBeanPropertyDAO extends GenericDAO<PoBeanProperty> {
    /**
     * @param bp
     * @return a PoBeanProperty object if and only if the className and the 
     * property name are the same. Null otherwise.
     */
    public PoBeanProperty findBeanPropertyByKey(PoBeanProperty bp);
    
    /**
     * @param beanName
     * @param property
     * @return a PoBeanProperty object if and only if the beanName and the 
     * property name are the same. Null otherwise.
     */
    public PoBeanProperty findBeanPropertyByKey(String beanName, String property);
   
	public List<PoBeanProperty> readBeanPropertiesForModule(String moduleUID);
	
	public List<PoBeanProperty> readBeanPropertiesForBean(String beanName);
	
	public List<PoBeanProperty> readBeanPropertiesForModuleAndBean(String moduleUID, String beanName);
    
	public List<String> readBeanNames();

	public List<String> readBeanNamesForModule(String moduleUID);
	
}
