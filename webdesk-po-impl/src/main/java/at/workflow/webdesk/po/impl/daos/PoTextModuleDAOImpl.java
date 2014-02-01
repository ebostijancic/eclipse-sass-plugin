/*
 * Created on 22.06.2005
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.impl.daos;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import at.workflow.webdesk.po.daos.PoTextModuleDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoTextModule;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          webdesk3<br>
 * created at:       02.10.2006<br>
 * package:          at.workflow.webdesk.po.impl.daos<br>
 * compilation unit: PoTextModuleDAOImpl.java<br><br>
 *
 *
 */
public class PoTextModuleDAOImpl extends GenericHibernateDAOImpl<PoTextModule> implements PoTextModuleDAO {

	public List<PoTextModule> findTextModules(PoLanguage language) {
		Object[] keyValues = { language};
	    return getHibernateTemplate().find("select tm from PoTextModule as tm left join tm.module where (tm.module is null or not tm.module.detached is true)"
			+ " and tm.language=?",keyValues
		);	
	}
    
    public List<PoTextModule> findCommonTextModules(PoLanguage language) {
        Object[] keyValues = { language};
        return getHibernateTemplate().find("select tm from PoTextModule as tm left join tm.module where tm.action is null and "
            + " tm.language=? and (tm.module is null or not tm.module.detached is true) order by tm.name",keyValues
        );  
    }

	/* (non-Javadoc)
	 * @see at.workflow.webdesk.po.PoTextModuleDAO#findChildTextModules(at.workflow.webdesk.po.model.PoTextModule, java.util.Date)
	 */
	public List<PoTextModule> findChildTextModules(PoTextModule parent, Date date) {
		 Object[] keyValues = { parent };
	     return getHibernateTemplate().find("select tm from PoTextModule as tm "
	     			+ " where tm.parent= ?" 
					, keyValues);
     }
	
	public List<PoTextModule> findTextModuleByName(String name) {
		Object[] keyValues = {name};
		return getHibernateTemplate().find("from PoTextModule tm" +
				" where tm.name = ? ",keyValues);
		
	}
	
	/* (non-Javadoc)
	 * @see at.workflow.webdesk.po.PoTextModuleDAO#getParentTextModule(at.workflow.webdesk.po.model.PoTextModule, java.util.Date)
	 */
	public PoTextModule getParentTextModule(PoTextModule child, Date date) {
		 Object[] keyValues = { child};
	     return (PoTextModule) DataAccessUtils.uniqueResult(getHibernateTemplate().find("select tm from PoTextModule as tm "
	     			+ " where tm.parent= ?" 
					, keyValues));
	}
	
	public void setParentTextModule(PoTextModule parent, PoTextModule child, Date validFrom, Date validTo) {
		parent.addChild(child);
		child.setParent(parent);
		getHibernateTemplate().update(parent);
		getHibernateTemplate().update(child);
	}

	/* (non-Javadoc)
	 * @see at.workflow.webdesk.po.PoTextModuleDAO#findTextModulesForAction(at.workflow.webdesk.po.model.PoAction)
	 */
	public List<PoTextModule> findTextModulesForAction(PoAction action,String languageKey) {
		Object[] keyValues = {action, languageKey};
		if (action!=null) 
			return getHibernateTemplate().find("from PoTextModule tm where tm.action = ?" +
                " and tm.language.code=? ",keyValues );
		else
			return getHibernateTemplate().find("from PoTextModule tm where tm.action is ?" +
	                " and tm.language.code=?",keyValues );
	}

    public List<PoTextModule> findTextModuleLikeNameAndLanguage(String folder, String name, PoLanguage lang) {
        Object[] keyValues = {folder+"_"+name+"%",lang};
        return getHibernateTemplate().find("select t from PoTextModule t left join t.module where t.name like ? and t.language =? and t.action is not null and (t.module is null or not t.module.detached is true)", keyValues);
    }
    
    public PoTextModule findParentTextModule(PoTextModule textModule) {
    	PoTextModule parent = textModule.getParent();
    	while (parent!=null && parent.getParent()!=null) {
    		parent = parent.getParent();
    	}
   		return parent;
    }

	public PoTextModule findTextModuleByNameAndLanguage(String name, final PoLanguage lang) {
		return this.findTextModuleByNameAndLanguage(name, lang.getUID());
	}

	public PoTextModule findTextModuleByNameAndLanguage(final String name, final String uid) {
		try {
			Object[] keyValues = {name, uid};
			List<PoTextModule> l = getHibernateTemplate().find("from PoTextModule tm" +
					" where tm.name = ? and tm.language.UID = ?",keyValues);
			return (PoTextModule) (l.size()>0 ? l.get(0) : null);
				
		} catch (Exception e) {
			logger.error("Could not determine textmodule with name '" + name + "' and language '" + uid +"'");
			logger.error(e,e);
			return null;
		}
	}
	
    public List<PoTextModule> findStandardTextModules() {
    	return  getHibernateTemplate().find("select tm from PoTextModule as tm left join tm.module"
    			+ " join tm.language as l where l.defaultLanguage=true and (tm.module is null or not tm.module.detached is true)");
    }

    public void allowUpdateOnVersionChangeForAllTextModules() {
        getHibernateTemplate().execute( 
                new HibernateCallback() {
                    public Object doInHibernate(Session session) throws HibernateException, SQLException{
                        String hqlUpdate = "update PoTextModule set allowUpdateOnVersionChange=1";
                     return new Integer(session.createQuery( hqlUpdate )
                             .executeUpdate());
                    }                   
                }   
        );
    }

    public void disallowUpdateOnVersionChangeForAllTextModules() {
        getHibernateTemplate().execute( 
                new HibernateCallback() {
                    public Object doInHibernate(Session session) throws HibernateException, SQLException{
                        String hqlUpdate = "update PoTextModule set allowUpdateOnVersionChange=0";
                     return new Integer(session.createQuery( hqlUpdate )
                             .executeUpdate());
                    }                   
                }   
        );
    }


	public List<PoTextModule> findTextModules(PoLanguage myLang, Date dateOfCreation) {
		Object[] keyValues = { myLang, dateOfCreation };
	    return getHibernateTemplate().find("from PoTextModule as tm where "
			+ " tm.language=? and tm.lastModified >?",keyValues);
	}

	@Override
	protected Class<PoTextModule> getEntityClass() {
		return PoTextModule.class;
	}



}
