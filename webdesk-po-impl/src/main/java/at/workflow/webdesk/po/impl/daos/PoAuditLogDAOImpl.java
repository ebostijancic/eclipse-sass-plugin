/*
 * Created on 22.06.2005
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.impl.daos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import at.workflow.webdesk.po.model.PoAuditLog;
import at.workflow.webdesk.po.model.PoAuditLogDefinition;

/**
 * @author hentner
 * 
 * TODO Refactoring is not finished. This class is not in use but will
 * be one time. Refactoring doesn't make sense until then.
 *
 */
public class PoAuditLogDAOImpl extends HibernateDaoSupport  {

	public void saveAuditLogDefinition(PoAuditLogDefinition auditLogDefinition) {
		getHibernateTemplate().save(auditLogDefinition);
	}

	public void deleteAndFlushAuditLogDefinition(PoAuditLogDefinition auditLogDefinition) {
		getHibernateTemplate().delete(auditLogDefinition);
	}

	public PoAuditLogDefinition getHistoryDef(String uid) {
		return (PoAuditLogDefinition) getHibernateTemplate().get(PoAuditLogDefinition.class,uid);
	}

	public Iterator getAuditLogDefinitionByObjectName(String objectName) {
		 Object[] keyValues = { objectName };
	     return getHibernateTemplate().find("select hd.attributeName from PoAuditLogDefinition as hd"
	     			+ " where hd.objectName= ?" 
	                , keyValues).iterator();
	}

	public Object getOriginal(Object o)   {
        
		StringBuffer query = new StringBuffer();
		Session s = null;
	    List l = null;
	    try {
			s = getHibernateTemplate().getSessionFactory().openSession();
			s.beginTransaction();
			Example example = Example.create(o).ignoreCase().enableLike(MatchMode.EXACT);
			l = s.createCriteria(o.getClass())
				.add(example).list();
	    } catch (HibernateException e) {
			e.printStackTrace();
		}
		finally {
	    	try {
	    		if (s!= null) 
	    			s.close();
			} catch (HibernateException e1) {
				e1.printStackTrace();
			}	    	
		}
		/*Iterator iter = getHibernateTemplate().find("select g.description from PoGroup g").iterator();
		while (iter.hasNext()) {
			System.out.println("description: " + iter.next().toString());
		}*/
	    if (l.size()!=1)
	    	return null;
	    Object original = l.get(0);
	    /*
	     * I called saveOrUpdate() or update() and got a NonUniqueObjectException!
	     * The session maintains a unique mapping between persistent identity and object instance, in order 
	     * to help you avoid data aliasing problems. You cannot simultaneously attach two objects with the 
	     * same class and identifier to the same session. The best way to avoid any possibility of this exception 
	     * is to always call saveOrUpdate() at the beginning of the transaction, when the session is empty.
	     * In Hibernate3, you can try using the merge() operation, which does not reattach the passed instance.
	     * 
	     */
	    return original;
	}
	
	public void logIfNotEqual(Object original, Object newObj, String method, String uid) {
		Method m;
		if (original!=null) {
			try {
				m = original.getClass().getMethod(method,null);
				Object res_orig = m.invoke(original,null);
				Object res_new  = m.invoke(newObj,null);
				if (!res_orig.equals(res_new)) {
					// save the old values
					String oName = original.getClass().toString().substring(6);
					this.log(res_orig.toString(), res_new.toString(),  method,uid, oName);
				}
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		} else 
			System.out.println("object is null. No logging.");
	}
	
	public void log(String originalValue, String newValue, String method,String uid, String objectName ) {
		PoAuditLog al = new PoAuditLog();
		al.setAttributeName(method);
		al.setFieldValue(originalValue);
		al.setLastModified(new Date());
		al.setModifiedBy("not implemented");
		al.setObjectName(objectName);
		al.setObjectUid(uid);
		getHibernateTemplate().save(al);
		
	}
	
	
		
	/* (non-Javadoc)
	 * @see at.workflow.webdesk.po.PoHistoryDAO#saveHistory(at.workflow.webdesk.po.model.PoHistory)
	 */
	public void saveHistory(PoAuditLog auditLog) {
		getHibernateTemplate().save(auditLog);
	}

	/* (non-Javadoc)
	 * @see at.workflow.webdesk.po.PoAuditLogDAO#deleteHistory(at.workflow.webdesk.po.model.PoAuditLog)
	 */
	public void deleteHistory(PoAuditLog auditLog) {
		getHibernateTemplate().delete(auditLog);
	}

	/* (non-Javadoc)
	 * @see at.workflow.webdesk.po.PoAuditLogDAO#getHistory(java.lang.String)
	 */
	public PoAuditLog getAuditLog(String uid) {
		return (PoAuditLog) getHibernateTemplate().load(PoAuditLog.class,uid);
	}	
}
