package at.workflow.webdesk.po.impl.daos;

import java.io.Serializable;
import java.util.List;

import at.workflow.webdesk.po.daos.PoMenuDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoMenuItem;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;


/**
 * Created on 04.08.2005
 * @author hentner (Harald Entner)
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          wdDEV<br>
 * refactored at:    14.05.2007<br>
 * package:          at.workflow.webdesk.po.impl.daos<br>
 * compilation unit: PoMenuDAOImpl.java<br><br>
 */
public class PoMenuDAOImpl extends GenericHibernateDAOImpl<PoMenuItem> implements PoMenuDAO  {
    
	@Override
	protected Class<PoMenuItem> getEntityClass() {
		return PoMenuItem.class;
	}
	
	/** Overridden to call load(throws exception when uid does not exist) instead of get(). */
	@Override
	public final PoMenuItem get(Serializable uid) {
        return (PoMenuItem) getHibernateTemplate().load(getEntityClass(), uid);
	}
	
	 /* (non-Javadoc)
     * @see at.workflow.webdesk.po.impl.PoMenoDAO#findMenuItemByName(java.lang.String)
     */
    public PoMenuItem findMenuItemByName(String name) {
        Object[] keyValues = {name};
        List l = getHibernateTemplate().find("from PoMenuItem where name=?",keyValues);
        if (l!=null && l.size()==1) 
            return (PoMenuItem) l.get(0);
        else 
            return null;
    }
    
    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.impl.PoMenoDAO#findMenuItemsByClient(at.workflow.webdesk.po.model.PoClient)
     */
    public List<PoMenuItem> findMenuItemsByClient(PoClient client) {
        Object[] keyValues = {client};
        return getHibernateTemplate().find(
                    "from PoMenuItem mi where mi.client=? order by ranking",keyValues);
    }
    
    public boolean hasMenuTree(String clientId) {
        Object[] keyValues = {clientId};
        return getHibernateTemplate().find("from PoMenuItem mi where mi.client.UID=?",keyValues).size()>0;
    }
    
    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.impl.PoMenoDAO#findMenuItemsForAll()
     */
    public List<PoMenuItem> findMenuItemsForAll() {
        return getHibernateTemplate().find(
                "from PoMenuItem mi where mi.client=null order by ranking");
    }

	public List<PoMenuItem> findAllCurrentMenuItems() {
        return getHibernateTemplate().find("from PoMenuItem " +
        		" where validto>=current_timestamp and " +
        		" validfrom<=current_timestamp order by name");
	}

    public List<PoMenuItem> findMenuRootOfClient(String clientId) {
        Object[] keys = {clientId};
        return getHibernateTemplate().find("from PoMenuItem mi where mi.client.UID=? and mi.parent is null", keys);
    }
    


	public List<PoMenuItem> findMenuItemsWithAction(PoAction myAction) {
		Object[] keys = {myAction};
		return getHibernateTemplate().find("from PoMenuItem where action=?",keys);
	}
	public List<PoMenuItem> findTemplateMenuItemsByTemplateId(Integer templateId) {
		Object[] keyValues = {templateId};
	    return getHibernateTemplate().find(
	    		"from PoMenuItem mi where mi.client is null and mi.templateId=? order by ranking",keyValues);
	}
		
	public List<Object[]> findAllTemplateIds() {
	    return getHibernateTemplate().find(
	    		"select mi.templateId, mi.description from PoMenuItem mi where mi.client is null and mi.templateId is not null group by mi.templateId, mi.description order by mi.templateId");
	}

	public int getMaxTemplateId() {
	    List res = getHibernateTemplate().find(
		"select max(mi.templateId) as maxTemplateId from PoMenuItem mi where mi.client is null");
    	if ( res.get(0) == null )
    		return 0;
    	else {
    		Integer maxCount = (Integer) res.get(0);
    		return maxCount.intValue();
    	}	
	}
	
	public List<PoMenuItem> findTemplateLinks(Integer templateId) {
		Object[] keyValues = {templateId};
	    return getHibernateTemplate().find(
	    		"from PoMenuItem mi where mi.client is not null and mi.templateId=? ",keyValues);
		
	}
}
