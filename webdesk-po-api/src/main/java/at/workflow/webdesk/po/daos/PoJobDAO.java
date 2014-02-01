package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoJob;

/**
 * DAO for CRUD methods on PoJob Entity
 */
public interface PoJobDAO extends GenericDAO<PoJob>	{

	/**
     * @param includeConfigurableJobs defines whether to include configurable jobs or not.
	 * @return a list of all jobs
	 */
	public List<PoJob> loadAllJobs(boolean includeConfigurableJobs);
	
    /**
     * Returns a list of all configurable jobs.
     * 
     * @return a list of PoJob objects.
     */
    public List<PoJob> findAllConfigurableJobs();

    /**
     * @param name
     * @param jobType: an integer which defines wether the job is a config or not. 
     * See <a href="PoConstants.html">PoConstants</a> for more information.
     * @return a PoJob object.
     */
    public PoJob findJobByNameAndType(String name, int jobType);

    /**
     * 
     * Returns a list of active PoJob objects.
     * 
     * @return a list of PoJob objects.
     */
    public List<PoJob> findAllActiveJobs();

}
