package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;

/**
 * Created on 07.01.2005
 * @author ggruber
 */
public interface PoOrgStructureDAO extends GenericDAO<PoOrgStructure> {
    
	public List<PoOrgStructure> findOrgStructureByName(PoClient client, String name);
	
	public PoOrgStructure getOrgHierarchy(PoClient client);
	
	public PoOrgStructure getOrgLocations(PoClient client);
	
	public PoOrgStructure getOrgCostCenters(PoClient client);
	
	public boolean isOrgStructureExistent(String name, String client_uid);

	/**
	 * Returns a list of <code>PoOrgStructure</code> objects with
	 * the given <code>client</code> assigned.
	 * 
	 * @param client
	 * @return a list of <code>PoOrgStructure</code> objects.
	 */
	public List<PoOrgStructure> findOrgStructuresOfClient(PoClient client);

	/**
	 * @param os
	 * @param date
	 * @return a <code>List </code> of <code>PoGroup</code> objects without 
	 * an active parent and with the <code>topLevel</code> flag set.
	 */
	public List<PoGroup> getTopLevelGroupsOfOrgStructure(PoOrgStructure os, Date date);
	
	/**
	 * @param os
	 * @param date
	 * @param topLevelFlagSet
	 * @return a <code>List </code> of <code>PoGroup</code> objects without 
	 * an active parent and with the <code>topLevel</code> flag equal to <code>topLevelFlagSet</code>.
	 */
	public List<PoGroup> getTopLevelGroupsOfOrgStructure(PoOrgStructure os, Date date, boolean topLevelFlagSet);
	
}
