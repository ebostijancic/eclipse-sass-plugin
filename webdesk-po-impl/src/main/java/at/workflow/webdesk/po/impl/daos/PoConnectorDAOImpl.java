package at.workflow.webdesk.po.impl.daos;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.daos.PoConnectorDAO;
import at.workflow.webdesk.po.model.FlowDirection;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoConnectorLink;
import at.workflow.webdesk.po.model.PoFieldMapping;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * The <b>Hibernate</b> Implementation fo the <code>PoConnectorDAO</code> interface.
 * 
 * Created on 10.04.2006
 * @author hentner
 */
@SuppressWarnings("unchecked")
public class PoConnectorDAOImpl extends GenericHibernateDAOImpl<PoConnector>  implements PoConnectorDAO {

	@Override
	protected Class<PoConnector> getEntityClass() {
		return PoConnector.class;
	}
	
    @Override
	public PoConnectorLink getConnectorLink(String uid) {
        return (PoConnectorLink) getHibernateTemplate().load(PoConnectorLink.class,uid);
    }

    @Override
	public PoFieldMapping getFieldMapping(String uid) {
        return (PoFieldMapping) getHibernateTemplate().load(PoFieldMapping.class,uid);
    }

    @Override
	public List<PoFieldMapping> findFieldMappingsOfLink(PoConnectorLink link) {
        return getHibernateTemplate().find("from PoFieldMapping fm where fm.mapping=? order by fm.orderIndicator", link);
    }

    @Override
	public void saveConnectorLink(PoConnectorLink link) {
        getHibernateTemplate().saveOrUpdate(link);
    }

    @Override
	public void saveFieldMapping(PoFieldMapping field) {
        getHibernateTemplate().saveOrUpdate(field);
    }

    @Override
	public void deleteConnectorLink(PoConnectorLink link) {
        getHibernateTemplate().delete(link);
    }

    @Override
	public void deleteFieldMapping(PoFieldMapping field) {
        getHibernateTemplate().delete(field);
    }


    @Override
	public List<PoConnector> findConnectorByName(String name) {
        Object[] keys = {name};
        return getHibernateTemplate().find("from PoConnector where name=?", keys);
    }
    
    @Override
	public List<PoConnectorLink> loadAllConnectorLinks() {
    	return getHibernateTemplate().loadAll(PoConnectorLink.class);
    }

	@Override
	public PoConnector findConnectorByNameAndModule(String name, PoModule module) {
		Object[] keys = {name, module};
        return (PoConnector) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoConnector where name=? and module=?", keys));
	}

	@Override
	public List<PoConnector> findAllConfigurableConnectors() {
		return getHibernateTemplate().find("from PoConnector where configurable is true and not module.detached is true");
	}
	
	@Override
	public List<String> findAllUsableConnectorNames(boolean writeable) {
		Object[] keys = {new Boolean(writeable)};
		return getHibernateTemplate().find("select name from PoConnector where configurable=false and writeable=? and not module.detached is true",keys);
	}

	
	@Override
	public List<PoConnector> findAllUsableConnectors(boolean writeable) {
		Object[] keys = {new Boolean(writeable)};
		return getHibernateTemplate().find("from PoConnector where configurable=false and writeable=? and not module.detached is true", keys);
		
	}

	@Override
	public List<PoConnector> findAllUsableConnectors(FlowDirection[] flowDirections) {
		if (flowDirections == null || flowDirections.length == 0)
			return Collections.emptyList();
		String query = "from PoConnector where configurable=false and (flowDirection=?";
		for (int i = 2; i <= flowDirections.length; i++)
			query += " or flowDirection=?";
		Object[] keys = flowDirections;
		return getHibernateTemplate().find(query + ") and not module.detached is true", keys);
	}
	
	@Override
	public PoConnectorLink findConnectorLinkByName(String name) {
		Object[] keys = {name};
		return (PoConnectorLink) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoConnectorLink where name=?", keys));
	}
	
	@Override
	public List<PoConnectorLink> findConnectorLinksByDestinationName(String name) {
		Object[] keys = {name};
		return getHibernateTemplate().find("from PoConnectorLink where destConnector.name=?", keys);
	}
}
