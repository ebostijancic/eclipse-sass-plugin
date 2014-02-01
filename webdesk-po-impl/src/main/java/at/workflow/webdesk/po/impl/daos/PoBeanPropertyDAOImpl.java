package at.workflow.webdesk.po.impl.daos;

import java.util.List;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.daos.PoBeanPropertyDAO;
import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          wdDEV<br>
 * created at:       11.05.2007<br>
 * package:          at.workflow.webdesk.po.impl.daos<br>
 * compilation unit: PoBeanPropertyDAOImpl.java<br><br>
 *
 * Implements the <code>PoBeanProperty</code> interface.
 * 
 * DataAccessObject for everything that has to do with Bean Properties.
 */
public class PoBeanPropertyDAOImpl extends GenericHibernateDAOImpl<PoBeanProperty> implements PoBeanPropertyDAO {

	@Override
	protected Class<PoBeanProperty> getEntityClass() {
		return PoBeanProperty.class;
	}

	public PoBeanProperty findBeanPropertyByKey(PoBeanProperty bp) {
		return this.findBeanPropertyByKey(bp.getBeanName(),bp.getPropertyName());
	}

	public PoBeanProperty findBeanPropertyByKey(String beanName, String property) {
		Object[] keys = {beanName, property};
		return (PoBeanProperty) DataAccessUtils.uniqueResult(getHibernateTemplate().find(
				"from PoBeanProperty pb where pb.beanName = ? and pb.propertyName = ?",keys));
	}

	@Override
	public List<PoBeanProperty> loadAll() {
		return getHibernateTemplate().find("select bp from PoBeanProperty bp left join bp.module m where not bp.detached is true order by m.name, beanName, propertyName");
	}

	public List<PoBeanProperty> readBeanPropertiesForModule(String moduleUID) {
		if (moduleUID == null) {
			return getHibernateTemplate().find("select bp from PoBeanProperty bp where bp.module is null and not bp.detached is true order by beanName, propertyName");
		} else {
			Object[] keys = {moduleUID};
			return getHibernateTemplate().find("select bp from PoBeanProperty bp where bp.module.UID = ? and not bp.detached is true order by beanName, propertyName", keys);
		}
	}
	
	public List<PoBeanProperty> readBeanPropertiesForBean(String beanName) {
		Object[] keys = {beanName};
		return getHibernateTemplate().find("select bp from PoBeanProperty bp where bp.beanName = ? order by propertyName", keys);
	}
	
	public List<PoBeanProperty> readBeanPropertiesForModuleAndBean(String moduleUID, String beanName) {
		Object[] keys = {moduleUID, beanName};
		return getHibernateTemplate().find("select bp from PoBeanProperty bp where bp.module.UID = ? and bp.beanName = ? order by propertyName", keys);
	}
	
	public List<String> readBeanNames() {
		return getHibernateTemplate().find("select distinct bp.beanName from PoBeanProperty bp where not bp.detached is true order by beanName");
	}
	
	public List<String> readBeanNamesForModule(String moduleUID) {
		if (moduleUID == null) {
			return getHibernateTemplate().find("select distinct bp.beanName from PoBeanProperty bp where bp.module is null and not bp.detached is true order by beanName");
		} else {
			Object[] keys = {moduleUID};
			return getHibernateTemplate().find("select distinct bp.beanName from PoBeanProperty bp where bp.module.UID = ? and not bp.detached is true order by beanName", keys);
		}
	}
	
}
