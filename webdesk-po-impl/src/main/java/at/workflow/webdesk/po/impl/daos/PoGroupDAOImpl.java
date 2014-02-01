package at.workflow.webdesk.po.impl.daos;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.QueryBuilderHelper;
import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoGroupDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.HistoricizingDAOImpl;

/**
 * Data Access Object for PoGroup and PoParentGroup objects.
 * Implements PoGroupDAO Interface. 
 * 
 * @author hentner
 * @author ggruber
 */
public class PoGroupDAOImpl extends HistoricizingDAOImpl<PoGroup> implements PoGroupDAO {
	
	@Override
	protected Class<PoGroup> getEntityClass() {
		return PoGroup.class;
	}
	
    /**
     * @see at.workflow.webdesk.po.daos.PoGroupDAO#checkGroup(at.workflow.webdesk.po.model.PoGroup)
     */
    @Override
	public void checkGroup(PoGroup group) {
    	// evict the group -> otherwise two groups with the same id and session would exist
//        getHibernateTemplate().evict(group);
    	// sdz_18.12.2013 no more necessary as no groups are read in isAttributeSetUnique any more
        
        Map<String,Object> uniqueAttributes = new HashMap<String,Object>();
        uniqueAttributes.put("shortName",group.getShortName());
        uniqueAttributes.put("client", group.getClient());
        // the group has to be unique with the given client and the given shortname
        if (isAttributeSetUnique(uniqueAttributes, group.getUID(), group.getValidity()) == false)
            throw new PoRuntimeException(PoRuntimeException.MESSAGE_DUPLICATE_GROUP_CLIENT_SHORTNAME + ": " + group);
    }
    
    @Override
	protected void beforeSave(PoGroup e)	{
		checkGroup(e);
	}
	
    @Override
	public void saveParentGroup(PoParentGroup parentGroup) {
    	getHibernateTemplate().save(parentGroup);
    }
    
    @Override
	public PoParentGroup getParentGroup(String uid) {
    	return getHibernateTemplate().get(PoParentGroup.class,uid);
    }	
    	
    @SuppressWarnings("unchecked")
	@Override
	public List<PoParentGroup> findAllParentGroups(PoGroup group) {
    	Object[] keys = {group};
    	return getHibernateTemplate().find("from PoParentGroup pg where pg.childGroup=?",keys);
    }
    
    @Override
	public List<PoGroup> findGroupByName(String key, Date referenceDate) {
         	
        Object[] keyValues = { key, referenceDate, referenceDate };
        
        @SuppressWarnings("unchecked")
		List<PoGroup> l = getHibernateTemplate().find("from PoGroup g " 
                + " where g.name = ? and "
                + " g.validfrom <= ? and "
				+ " g.validto > ?",  
                keyValues);
        
        return l;
        
    }

    @SuppressWarnings("unchecked")
	@Override
	public PoGroup findGroupByShortName(String name, PoClient client, Date referenceDate) {
        
        if (client==null) { // this part is deprecated -> can be deleted when findGroupByShortName(name,referenceDate) is removed.
            Object[] keyValues = { name, referenceDate, referenceDate };
            return (PoGroup) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoGroup g " 
                    + " where g.shortName = ? and "
                    + " g.validfrom <= ? and "
                    + " g.validto > ?",  
                    keyValues));
        }
        
        Object[] keyValues = { name, client, referenceDate, referenceDate };
        return (PoGroup) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoGroup g " 
                + " where g.shortName=? "
                + " and g.client= ? "  
                + " and g.validfrom<=?"
                + " and g.validto> ?",
                keyValues));
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public PoGroup findGroupByName(String name, PoClient client, Date referenceDate) {
        
        Object[] keyValues = { name, client, referenceDate, referenceDate };
        
        return (PoGroup) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoGroup g " 
                + " where g.name=? "
				+ " and g.client= ? "  
				+ " and g.validfrom<=?"
				+ " and g.validto> ?",
                keyValues));
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<PoGroup> findGroupsWithFilter(List<String> uids, List<String> names, Date date) {
		Object[] keys = {date, date};
		String addSql = getNamesQueryStringForGroup(names, null);
		String uidString = QueryBuilderHelper.generateCommaList(uids, true);
		
		String sql ="from PoGroup where validFrom<=? and validto>? and UID in (" + uidString + ")";
		if (!addSql.equals(""))
			sql += "and (" + addSql + ")";
		
		return getHibernateTemplate().find(sql, keys); 
	}
    
	@SuppressWarnings("unchecked")
	@Override
	public List<PoGroup> findGroupsWithFilter(List<String> uids, List<String> names, Date from, Date to) {
		Object[] keys = {to, from};
		String addSql = getNamesQueryStringForGroup(names, null);
		String uidString = QueryBuilderHelper.generateCommaList(uids, true);
		
		String sql ="from PoGroup where validFrom <= ? and validto >= ? and UID in (" + uidString + ")";
		if (!addSql.equals(""))
			sql += "and (" + addSql + ")";
		
		return getHibernateTemplate().find(sql, keys); 
	}
	
	private String getNamesQueryStringForGroup(List<String> names, String alias) {
		String sql="";
		if (alias!=null)
			alias = alias+".";
		else
			alias="";
		if (names!=null) {
			Iterator<String> nI = names.iterator();
			while (nI.hasNext()) {
				String name = nI.next();
				sql+=" " + alias + "shortName like '%"+name+"%' or " + alias +"name like '%"+ name + "%'" ;
				if (nI.hasNext())
					sql+=" or ";
			}
		}
		return sql;
	}
	
    @SuppressWarnings("unchecked")
	@Override
	public List<PoGroup> findGroupsOfOrgStructureF(PoOrgStructure orgStructure, Date date) {
        Object[] keyValues = { orgStructure, date};

        return getHibernateTemplate().find("from PoGroup g " 
                + " where g.orgStructure = ? "  
				+ " and g.validto> ? order by g.shortName",
                keyValues);
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public List<PoGroup> findGroupsOfOrgStructure(PoOrgStructure orgStructure, Date date) {
        Object[] keyValues = { orgStructure, date, date};

        return getHibernateTemplate().find("from PoGroup g " 
                + " where g.orgStructure = ? "  
                + " and g.validfrom<= ? and g.validto>? order by g.shortName",
                keyValues);
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public PoParentGroup getParentGroup(PoGroup group, Date referenceDate) {
        Object[] keyValues = { group, referenceDate, referenceDate , referenceDate, referenceDate};
        List<PoParentGroup> res = getHibernateTemplate().find("select pg from PoParentGroup pg " 
        		+ " join pg.childGroup as cg "
                + " where cg = ? and "
                + " pg.validfrom <= ? and pg.validto > ? " 
				+ " and cg.validfrom <= ? and cg.validto > ? ",  
                keyValues);
        if (res.size()==1)
        	return res.get(0);
        else
        	return null;
	}
    

    @SuppressWarnings("unchecked")
	@Override
	public List<PoGroup> findCurrentGroups() {
       return getHibernateTemplate().find("from PoGroup where validfrom<current_timestamp() and validto>current_timestamp() order by shortName asc");
    }
    

    @SuppressWarnings("unchecked")
	@Override
	public List<PoParentGroup> findParentGroupsWithDate(PoGroup group, Date validAt) {
    	if (validAt== null) validAt = new Date();
    	Object[] keyValues = { group, validAt, validAt,validAt, validAt};
		// hole alle aktuellen PoParentGroup Links 
		return getHibernateTemplate().find("select pg" +
		" from PoParentGroup pg where " +
		  " pg.childGroup = ? and pg.validfrom<=? and pg.validto>?" + 
		  " and pg.parentGroup.validfrom<=? and pg.parentGroup.validto>?",
		              keyValues);    	
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public List<PoParentGroup> findParentGroupsWithTimeOverlap(PoGroup child, Date validFrom, Date validTo) {
    	Object[] keyValues = {child, validFrom, validTo};
	    return getHibernateTemplate().find(
	    		"from PoParentGroup pg " +
	            "where pg.childGroup=? and " +
	            "pg.validto>? and pg.validfrom<=?", 
				 keyValues);
    }


    @Override
	public List<PoParentGroup> findChildGroups(PoGroup group) {
        return findChildGroups(group, null);
    }
    
    @Override
	@SuppressWarnings({ "cast", "unchecked" })
	public List<PoParentGroup> findChildGroups(PoGroup group, Date effectiveDate) {
    	return (List<PoParentGroup>) findChildGroupsImpl(group, effectiveDate, "select pg", "pg.parentGroup = ?", "order by pg.ranking");
    }
    
    @SuppressWarnings("rawtypes")	// group can be a PoGroup object, or just a String UID of a group
	private List findChildGroupsImpl(Object group, Date effectiveDate, String querySelect, String queryWhere, String queryOrderBy)	{
    	if (effectiveDate == null)
    		effectiveDate = new Date();
        Object[] keyValues = { group, effectiveDate, effectiveDate , effectiveDate, effectiveDate, effectiveDate, effectiveDate };
        return getHibernateTemplate().find(
        	querySelect+
	    		" from PoParentGroup pg where" +
	        	"   "+queryWhere+" and pg.validfrom <= ? and pg.validto > ?" +
	        	"   and pg.childGroup.validfrom <= ? and pg.childGroup.validto > ?" +
	        	"   and pg.parentGroup.validfrom <= ? and pg.parentGroup.validto > ? "+
	        	queryOrderBy,
        	keyValues);
    }

    @Override
	@SuppressWarnings("unchecked")
	public List<PoParentGroup> findChildGroupsF(PoGroup group, Date effectiveDate) {
    	if (effectiveDate == null)
    		effectiveDate = new Date();
        Object[] keyValues = { group, effectiveDate, effectiveDate };
        return getHibernateTemplate().find(
        	"select pg" +
        		" from PoParentGroup pg join pg.parentGroup as g join pg.childGroup as cg"+
        		" where g = ? and pg.validto > ? and cg.validto > ?"+
        		" order by pg.ranking, cg.shortName asc",
        	    // changed by ggruber to get correct sorting within the orgtree
        	    // it was reported that this sql makes problems on mssql server
        	    // however it worked with  bks-gleitzeit mssql server and
        	    // webdesk3 sql server!!!
        	keyValues);
    }


    @Override
	@SuppressWarnings("unchecked")
	public List<PoParentGroup> findChildGroupsAll(PoGroup group) {
    	Object[] keyValues = { group };
    	// hole alle aktuellen PoParentGroup Links 
    	return getHibernateTemplate().find("select pg" +
    			" from PoParentGroup pg join pg.childGroup as cg where " +
    			" pg.parentGroup = ? order by cg.shortName asc, pg.validfrom desc ",
    			keyValues);
    }
    
    
    @SuppressWarnings("unchecked")
	@Override
	public List<PoPerson> findPersonsOfGroupF(PoGroup group, Date validAt) {
    	if (logger.isDebugEnabled())
    		logger.debug("findPersonsFromGroupF: group:" +group + " date:" + validAt);
    	Object[] keyValues = {group,validAt,validAt,validAt};
    	return getHibernateTemplate().find("select p from PoPerson as p" +
    			" join p.memberOfGroups as pg join pg.group as g " +
    			" where g=? and pg.validto>? and g.validto>? and p.validto>? order by p.lastName asc, p.firstName asc",keyValues);
    }

    @SuppressWarnings("unchecked")
	@Override
	public List<PoPerson> findPersonsOfGroup(PoGroup group, Date validAt) {
    	Object[] keyValues = {group,validAt,validAt,validAt,validAt,validAt,validAt};
    	return getHibernateTemplate().find("select p from PoPerson as p" +
    			" join p.memberOfGroups as pg join pg.group as g " +
    			" where g=? and pg.validfrom<=? and pg.validto>? and p.validfrom<=? and p.validto>?" +
                " and g.validfrom<=? and g.validto>? order by p.lastName asc, p.firstName asc",keyValues);
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public List<PoPerson> findPersonsOfGroup(PoGroup group, Date from, Date to) {
    	Object[] keyValues = {group,to,from,to,from,to,from};
    	return getHibernateTemplate().find("select distinct p from PoPerson as p" +
    			" join p.memberOfGroups as pg join pg.group as g " +
    			" where g=? and pg.validfrom<=? and pg.validto>? and p.validfrom<=? and p.validto>?" +
    			" and g.validfrom<=? and g.validto>? order by p.lastName asc, p.firstName asc",keyValues);
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public List<PoAction> findActions(PoGroup group, Date date) {
		 Object[] keyValues = { group, date, date};
	     return getHibernateTemplate().find("select a from PoPermissionGroup as pg "
	     			+ " join pg.action as a "
	                + " where pg.group= ?" 
	                + " pg.validfrom<=? "
	                + " and pg.validto>?"
					, keyValues);
	}


	@Override
	public List<PoParentGroup> loadAllParentGroups() {
		return getHibernateTemplate().loadAll(PoParentGroup.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PoParentGroup> findParentGroupsAll(PoGroup group) {
		
		Object[] keyValues = { group };

		return getHibernateTemplate().find("select pg" +
        		" from PoParentGroup pg join pg.childGroup as cg where " +
                " cg = ? order by pg.validfrom desc", keyValues);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PoGroup> findGroupsFromClientF(PoClient client, Date date) {
		Object[] keyValues = {client, date};
		return getHibernateTemplate().find("select g from PoGroup as g " +
				" join g.client as c " +
				" where c=? and g.validto>? order by g.shortName",keyValues);
		
	}

    @Override
	public List<PoGroup> findGroupsWithoutParent(PoOrgStructure orgStructure, Date date) {
        
        Object[] keys = {date, date,orgStructure,orgStructure,date,date, date, date};
        @SuppressWarnings("unchecked")
		List<PoGroup> l =  getHibernateTemplate().find("from PoGroup g where g.validfrom<=? and g.validto>? and g.orgStructure=? and g.UID not in (select distinct g.UID from PoParentGroup pg join pg.childGroup g where " +
        		" g.orgStructure=? and g.validfrom<=? and g.validto>? and pg.validfrom<=? and pg.validto>?)  ",keys);
        return l;
    }

    @SuppressWarnings("unchecked")
	@Override
	public List<PoPersonGroup> findPersonGroupsF(PoGroup group, Date date) {
        Object[] keyValues = { group, date };
        return getHibernateTemplate().find(
                "select pg from PoPersonGroup as pg " + " where pg.group=? "
                        + " and pg.validto >? order by pg.person.lastName ", keyValues);
    }
    
    
    @SuppressWarnings("unchecked")
	@Override
	public List<PoPersonGroup> findPersonGroupsAll(PoGroup group) {
    	Object[] keyValues = { group };
    	return getHibernateTemplate().find(
    			"select pg from PoPersonGroup as pg " + " where pg.group=? "
    			+ " order by pg.person.lastName asc, pg.validfrom desc ", keyValues);
    }
    
    
    @SuppressWarnings("unchecked")
	@Override
	public List<PoGroup> findGroupWithOrgStructureAndPerson(PoPerson person, PoOrgStructure os, Date date) {
        Object[] keyValues = { os, person, date, date, date , date};

        return getHibernateTemplate().find("select g from PoPersonGroup pg join pg.group as g  " 
                + " where g.orgStructure = ? and pg.person = ? and pg.validfrom <= ? and pg.validto > ? "  
                + " and g.validfrom <= ? and g.validto>? order by g.shortName",
                keyValues);
    }
    
    @Override
	public void deleteParentGroup(PoParentGroup pg) {
    	
    	// remove links from the LIVE linked Pogroups
    	pg.getChildGroup().getParentGroups().remove(pg);
    	pg.getParentGroup().getChildGroups().remove(pg);
    	
    	getHibernateTemplate().delete(pg);
    }

	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> findPersonUidsOfGroup(String uidOfGroup,  Date date) {
			Object[] keyValues = {uidOfGroup,date,date,date,date,date,date};
	    	return getHibernateTemplate().find("select p.UID from PoPerson as p" +
	    			" join p.memberOfGroups as pg join pg.group as g " +
	    			" where g.UID = ? and pg.validfrom <= ? and pg.validto > ? and p.validfrom <= ? and p.validto > ?" +
	                " and g.validfrom <= ? and g.validto > ? " +
	                " order by p.lastName, p.firstName ",keyValues);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PoPerson> findPersonsOfGroup(PoGroup group, List<String> names, Date date ) {
		Object[] keyValues = {date,date,date,date,date,date};
		Iterator<String> nI = names.iterator();
		String addSql = "";
		while (nI.hasNext()) {
			String name = nI.next();
			name = name.replaceAll("\\*", "%");
			addSql+=" firstName like '"+name+"' or lastName like '"+ name + "'" ;
		}
		
		String query = "select p from PoPerson as p" +
    			" join p.memberOfGroups as pg join pg.group as g " +
    			" where pg.validfrom<=? and pg.validto>? and p.validfrom<=? and p.validto>?" +
                " and g.validfrom <= ? and g.validto > ? ";
		if (!addSql.equals(""))
			query += " and (" + addSql+")";
		query += " order by p.lastName, p.firstName ";
		return getHibernateTemplate().find(query,keyValues);
		
	}


	@Override
	@SuppressWarnings("unchecked")
	public List<PoParentGroup> findParentGroups(PoGroup parent, PoGroup child) {
		Object[]keys = {parent, child};
		return getHibernateTemplate().find("from PoParentGroup where " +
				" parentGroup=? and childGroup=?", keys);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> findGroupUidsOfClient(String uidOfClient, Date date) {
		Object[] keyValues = { uidOfClient, date, date };
		return getHibernateTemplate().find(
						"select g.UID from PoGroup as g "
								+ " join g.client as c "
								+ " where c.UID=? and g.validfrom<=? and g.validto>? order by g.shortName",
						keyValues);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoGroup> resolveViewPermissionsToGroups(Map<String, List<String>> viewPermissions, Date date) {
		
		String query = "from PoGroup g where " +
				"g.validfrom<=:argDate and g.validto>:argDate ";

		query += " and (g.UID in (:argGroupUids) or g.client.UID in (:argClientUids))";
		
		String[] keys = { "argDate", "argGroupUids", "argClientUids" };
		
		List<String> groupUIDs = viewPermissions.get(PoActionPermissionService.GROUPS);
		List<String> clientUIDs = viewPermissions.get(PoActionPermissionService.CLIENTS);
		
		Object[] values = { date, groupUIDs, clientUIDs };

		return getHibernateTemplate().findByNamedParam(query, keys, values);
	}
}
