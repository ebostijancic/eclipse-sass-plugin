package at.workflow.webdesk.po.impl.daos;

import java.util.Iterator;
import java.util.List;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.PoModuleUpdateService;
import at.workflow.webdesk.po.daos.PoJobDAO;
import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * Hibernate implementation of the PoJobDAO
 * 
 * @author hentner, fpovysil, ggruber, fritzberger
 */
public class PoJobDAOImpl extends GenericHibernateDAOImpl<PoJob> implements PoJobDAO 
{
	private PoModuleUpdateService moduleUpdateService;
	
	@Override
	protected Class<PoJob> getEntityClass() {
		return PoJob.class;
	}
    
	@SuppressWarnings("unchecked")
	public List<PoJob> loadAllJobs(boolean includeConfigurableJobs)	{
		Object[] keys = { new Boolean(includeConfigurableJobs) };
        if (includeConfigurableJobs)
            return (List<PoJob>) getHibernateTemplate().find("from PoJob");
        else
            return (List<PoJob>) getHibernateTemplate().find("from PoJob where configurable = ?",keys);
	}


    public PoJob findJobByNameAndType(String name, int type) {
        Object keys[]  ={ name , new Integer(type)};
        return (PoJob) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoJob j where j.name = ? and j.type = ?",keys));
    }
    
	@SuppressWarnings("unchecked")
	@Override
	protected void beforeSave(PoJob job) {
		if (job.getUID()==null || job.getUID().equals("")) {
            Object[] keys = {job.getName()};
            List<PoJob> l = getHibernateTemplate().find("from PoJob where name = ?",keys);
            if (l.size()>0) {
                PoJob existingJobWithSameName = ((PoJob) l.get(0));
                getHibernateTemplate().evict(existingJobWithSameName);
                job.setUID(existingJobWithSameName.getUID());                
            }
        }
	}

    @SuppressWarnings("unchecked")
	public List<PoJob> findAllConfigurableJobs() {
        Object[] keys = { new Boolean(true) };
        return this.getHibernateTemplate().find("from PoJob j where j.configurable = ?",keys);
    }

    @SuppressWarnings("unchecked")
	public List<PoJob> findAllActiveJobs() {
        Object[] keys ={new Boolean(true)};
        Iterator<String> i = moduleUpdateService.getModules().iterator();
        String modules = "";
        while (i.hasNext()) {
        	String module = (String) i.next();
        	modules+="'" + module + "'";
        	if (i.hasNext()) 
        		modules+=",";
        }
        return getHibernateTemplate().find("from PoJob where active = ? and module.name in (" + modules + ")", keys);
    }

	public void setModuleUpdateService(PoModuleUpdateService moduleUpdateService) {
		this.moduleUpdateService = moduleUpdateService;
	}

}
