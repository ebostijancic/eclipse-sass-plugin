package at.workflow.webdesk.po.impl.daos;

import java.util.List;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.daos.PoJobTriggerDAO;
import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.po.model.PoJobTrigger;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;


/**
 * Hibernate Implementation of PoJobTriggerDAO
 * 
 * @author fpovysil, hentner, ggruber, fritzberger
 */
public class PoJobTriggerDAOImpl extends GenericHibernateDAOImpl<PoJobTrigger> implements PoJobTriggerDAO 
{
	@Override
	protected Class<PoJobTrigger> getEntityClass() {
		return PoJobTrigger.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void beforeSave(PoJobTrigger trigger) {
        if (trigger.getUID()==null || trigger.getUID().equals("")) {
            Object[] keys = {trigger.getName()};
            List<PoJobTrigger> l = getHibernateTemplate().find("from PoJobTrigger where name = ?",keys);
            if (l.size()>0) {
                PoJobTrigger existingJobTriggerWithSameName = ((PoJobTrigger) l.get(0));
                getHibernateTemplate().evict(existingJobTriggerWithSameName);
                trigger.setUID(existingJobTriggerWithSameName.getUID());                
            }
        }
	}
	
    public PoJobTrigger findJobTriggerByNameAndJob(String name, PoJob job) {
        Object[] keys = {name, job};
        return (PoJobTrigger) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoJobTrigger jt where jt.name = ? and jt.job=?", keys));
    }

    @SuppressWarnings("unchecked")
	public List<PoJobTrigger> findActiveTriggersOfJob(PoJob job) {
        Object[] keys = {job, new Boolean(true)};
        return (List<PoJobTrigger>) getHibernateTemplate().find("from PoJobTrigger as jt where jt.job = ? and jt.active = ?",keys);
    }
}
