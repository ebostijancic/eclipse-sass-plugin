package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.po.model.PoJobTrigger;

/**
 * Created on 13.10.2005
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          webdesk3<br>
 * created at:       14.03.2006<br>
 * package:          at.workflow.webdesk.po<br>
 * compilation unit: PoJobTriggerDAO.java<br><br>
 *
 *
 */
public interface PoJobTriggerDAO extends GenericDAO<PoJobTrigger>	{

	/**
     * Returns a PoJobTrigger object if a trigger was found, null otherwise.
	 * @param name the name of the trigger.
	 * @return a PoJobTrigger object if a trigger with the given name 
     * was found, null otherwise.
	 */
	public PoJobTrigger findJobTriggerByNameAndJob(String name, PoJob job);
	
    /**
     * Returns a list of active PoJobTrigger objects that are assigned 
     * to the given PoJob object. 
     * 
     * 
     * @param job
     * @return a list of PoJobTrigger objects.
     */
    public List<PoJobTrigger> findActiveTriggersOfJob(PoJob job);

    
}
