package at.workflow.webdesk.po.impl.daos;

import java.util.List;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.daos.PoModuleDAO;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          wdDEV<br>
 * refactored at:    14.05.2007<br>
 * package:          at.workflow.webdesk.po.impl.daos<br>
 * compilation unit: PoModuleDAOImpl.java<br><br>
 *
 *
 */
public class PoModuleDAOImpl extends GenericHibernateDAOImpl<PoModule> implements PoModuleDAO {

	@Override
	protected Class<PoModule> getEntityClass() {
		return PoModule.class;
	}
	
	public PoModule getModuleByName(String moduleName) {
        Object[] keys = {moduleName};
        return (PoModule) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoModule m where m.name=?",keys));
	}

	public List<PoModule> loadActiveModules() {
		return getHibernateTemplate().find("from PoModule m where m.detached=?", new Boolean(false));
	}

}
