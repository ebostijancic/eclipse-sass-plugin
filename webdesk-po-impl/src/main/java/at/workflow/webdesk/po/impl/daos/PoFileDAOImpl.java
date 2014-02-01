package at.workflow.webdesk.po.impl.daos;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.Configurable;
import at.workflow.webdesk.po.daos.PoFileDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * Created on 26.08.2005
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          wdDEV<br>
 * created at:       08.06.2007<br>
 * package:          at.workflow.webdesk.po.impl.daos<br>
 * compilation unit: PoFileDAOImpl.java<br><br>
 */
public class PoFileDAOImpl extends GenericHibernateDAOImpl<PoFile> implements PoFileDAO {
	
	static final String CONNECTOR = "connector";
	static final String JOB = "job";
	static final String ACTION = "action";
   
	@Override
	protected Class<PoFile> getEntityClass() {
		return PoFile.class;
	}
	
	@Override
	protected void beforeSave(PoFile file) {
		file.setSize(file.getContent().length);
	}
	
	/**
	 * TODO this method throws an ObjectNotFoundException when the object was deleted.
	 * This does not conform to get() specification.
	 */
	@Override
	public PoFile get(Serializable uid) {
	    PoFile file = getHibernateTemplate().load(PoFile.class, uid);
	    return file;
	}
	
	@Override
	public PoFile getFile(PoAction action, int type, Date date) {
		Object[] keyValues = {action,new Integer(type), date, date};
		return (PoFile) DataAccessUtils.uniqueResult(getHibernateTemplate().find("select f from PoFile f " +
				" join f.action as a where a = ? and a.type=? and a.validfrom<=? and a.validto>?",keyValues));
	}
	
	private String getReferenceFieldPath(Configurable configurable) {
		String fieldName = "";
		if (configurable instanceof PoAction)
			fieldName = ACTION;
		else if (configurable instanceof PoJob)
			fieldName = JOB;
		else if (configurable instanceof PoConnector)
			fieldName = CONNECTOR;
		else
			throw new IllegalStateException("Configurable is neither a Job, Connector or Action!");
		return fieldName;
	}
	
    @Override
	public PoFile getFileOfConfigurable(Configurable configurable) {
    	
    	Object[] keyValues = {configurable.getUID()};
        List res = getHibernateTemplate().find("from PoFile f " +
                "where f." + getReferenceFieldPath(configurable) + ".UID=?  order by versionNumber desc",keyValues);
        PoFile file = null;
        if (res.size()>0) 
            file = (PoFile) res.get(0);
        return file;
    }
    
	@Override
	public PoFile getFile(PoAction action ,int type) {
	    Object[] keyValues = {action,new Integer(type)};
	    List res = getHibernateTemplate().find("from PoFile f " +
	    		"where f.action=? and f.type=? order by versionNumber desc",keyValues);
	    PoFile file = null;
	    if (res.size()>0) 
	        file = (PoFile) res.get(0);
	    return file;
	}
	
    @Override
	public String getFileIdPerPath(String relPath) {
        Object[] keyValues = {relPath};
        List res = getHibernateTemplate().find("from PoFile f where  " +
        		" f.path=? order by versionNumber desc",keyValues);
        if (res.size()>0) {
            PoFile f = (PoFile) res.get(0);
            return f.getFileId();
        }
        else 
            return "";
    }
    
    @Override
	public PoFile getFileWithVersionAndFileId(String id, int highestVersion) {
        Object[] keyValues2 = {id,new Integer(highestVersion)};
        List res = getHibernateTemplate().find("select f from PoFile f where  " +
                " f.fileId=? and f.versionNumber=?",keyValues2);
        if (res.size()>0) 
            return (PoFile) res.get(0);
        else
            return null;
    }

    @Override
	public List findFileWherePathLike(String constraint) {
        Object[] keys = {"%"+constraint+"%"};
        List l = getHibernateTemplate().find("from PoFile f where f.path like ? order by f.path" ,keys);
        return l;
    }
    
    @Override
	public List findFileWherePathLikeAndMaxVersion(String constraint) {
        Object[] keys = {"%"+constraint+"%"};
        return getHibernateTemplate().find("from PoFile f where f.path like ? "+ 
        		" and f.versionNumber in " +
        			"(select max(versionNumber) from PoFile where fileId=f.fileId) order by f.path",keys);
    }
    
	@Override
	public List findFilesOfActionOrderByVersion(PoAction action) {
		Object[] keyValues = {action};
		return getHibernateTemplate().find("from PoFile f where f.action=? order by f.versionNumber desc",keyValues);
	}

	@Override
	public List findFilesWithFileId(String uid) {
		Object[] keyValues = {uid};
		return getHibernateTemplate().find(
				"from PoFile f where f.fileId=? order by versionNumber desc",keyValues);
	}

	@Override
	public int getHighestVersion(String fileId) {
		Object[] keyValues = { fileId };
		List l = getHibernateTemplate().find(
				"select max(f.versionNumber) from PoFile f where fileId = ?",
				keyValues);
		int versionNumber = 1;
		if (l != null) {
			try {
				String s = l.get(0).toString();
				versionNumber = new Integer(s).intValue();
			} catch (Exception e) {

			}
		}
		return versionNumber;
		
	}

	@Override
	public PoFile getFileWithHighestVersion(String fileId) {
		return getFileWithVersionAndFileId(fileId, getHighestVersion(fileId));
	}

	@Override
	public List<String> loadAllFileIds() {
		return getHibernateTemplate().find("select f.UID from PoFile");
	}

}
