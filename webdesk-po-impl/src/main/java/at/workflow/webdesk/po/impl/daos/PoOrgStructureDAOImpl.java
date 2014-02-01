package at.workflow.webdesk.po.impl.daos;

import java.util.Date;
import java.util.List;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoOrgStructureDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * Created on 07.01.2005
 * @author ggruber
 * @author hentner
 */
public class PoOrgStructureDAOImpl extends GenericHibernateDAOImpl<PoOrgStructure> implements PoOrgStructureDAO {

	@Override
	protected Class<PoOrgStructure> getEntityClass() {
		return PoOrgStructure.class;
	}
	
    @Override
    protected void beforeDelete(PoOrgStructure orgStructure) {
        if(orgStructure.getGroups() != null && orgStructure.getGroups().size() > 0) {
            throw new PoRuntimeException("OrgStructure has still groups assigned");
        }
        orgStructure.getClient().getOrgStructures().remove(orgStructure);
    }
    
	@Override
	public List<PoOrgStructure> findOrgStructuresOfClient(PoClient client) {
		Object[] keyValues = {client};
		return (List<PoOrgStructure>) getHibernateTemplate().find("from PoOrgStructure where client=?",keyValues);
	}
 	
    /**
     * @see at.workflow.webdesk.po.PoOrgStructureDAO#findOrgStructureByName(at.workflow.webdesk.po.model.PoClient, java.lang.String)
     */
    @Override
	public List<PoOrgStructure> findOrgStructureByName(PoClient client, String name) {
        Object[] keyValues = { name, client };
        return getHibernateTemplate().find("from PoOrgStructure os " 
                + " where os.name = ? and os.client = ?", 
                keyValues);
    }

    /**
     * @see at.workflow.webdesk.po.PoOrgStructureDAO#getOrgHierarchy(at.workflow.webdesk.po.model.PoClient)
     */
    @Override
	public PoOrgStructure getOrgHierarchy(PoClient client) {
    	return getOrgStructure(client, PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
    }
    
    @Override
    public PoOrgStructure getOrgLocations(PoClient client) {
    	return getOrgStructure(client, PoOrgStructure.STRUCTURE_TYPE_LOCATIONS);
    }
    
    @Override
    public PoOrgStructure getOrgCostCenters(PoClient client) {
    	return getOrgStructure(client, PoOrgStructure.STRUCTURE_TYPE_COSTCENTERS);
    }
    
    private PoOrgStructure getOrgStructure(PoClient client, int orgType) {
    	
    	Object[] keyValues = { new Integer(orgType) , client };
    	
    	PoOrgStructure poos=(PoOrgStructure) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoOrgStructure os " 
    			+ " where os.orgType = ? and os.client = ?", 
    			keyValues));
    	return poos;
    }
    
 	@Override
	public boolean isOrgStructureExistent(String name, String client_uid) {
		Object[] keyValues={ name, client_uid };
		List l = getHibernateTemplate().find("select count(*) from PoOrgStructure where name=? and client_uid=?",keyValues);
		int i = ((Integer) l.get(0)).intValue();
		return (1==i);
	}

 	@Override
	public List getTopLevelGroupsOfOrgStructure(PoOrgStructure os, Date date) {
 		return getTopLevelGroupsOfOrgStructure(os, date, true);
 	}

	@Override
	public List getTopLevelGroupsOfOrgStructure(PoOrgStructure organisationStructure, Date date, boolean topLevelFlagSet) {
		Boolean topLevel = Boolean.valueOf(topLevelFlagSet);
		
		// include all valid groups which have no valid children
		Object[] keys3 = { topLevel, organisationStructure, date, date, date };
		List nonParentGroups = getHibernateTemplate().find("from PoGroup g where" +
				" g.topLevel = ? and g.orgStructure = ? and g.validfrom <= ? and g.validto > ?" + 
				" and g not in (select pg.parentGroup from PoParentGroup as pg where pg.validto > ?)",
			keys3);
		
		// include all groups which have children (valid or not) and which don't have any valid parents
		// or: include all valid parent-groups which are not a valid child-group
		Object[] nonChildGroupsParams = { topLevel, organisationStructure, date, date, date, date, organisationStructure, date, date, };
		List nonChildGroups = getHibernateTemplate().find("select distinct pg.parentGroup from PoParentGroup as pg where"+
				" pg.parentGroup.topLevel = ? and pg.parentGroup.orgStructure = ?"+
				" and pg.parentGroup.validfrom <= ? and pg.parentGroup.validto > ? and pg.validfrom <= ? and pg.validto > ?"+
				" and pg.parentGroup.UID not in"+
				"   (select pg2.childGroup.UID from PoParentGroup pg2 where"+
				"    pg.parentGroup.orgStructure = ? and pg2.validfrom <= ? and pg2.validto > ?)",
			nonChildGroupsParams);
		
		nonParentGroups.addAll(nonChildGroups);
		return nonParentGroups;
	}

}
