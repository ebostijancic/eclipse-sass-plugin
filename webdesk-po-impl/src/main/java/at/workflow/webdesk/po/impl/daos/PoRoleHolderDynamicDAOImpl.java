package at.workflow.webdesk.po.impl.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.daos.PoRoleHolderDynamicDAO;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderDynamic;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoRoleHolderDynamicDAOImpl extends GenericHibernateDAOImpl<PoRoleHolderDynamic> implements PoRoleHolderDynamicDAO {
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderDynamic> findRoleHolderDynamicF(PoRole role, Date date) {
		Object[] keys = {role, date};
		return getHibernateTemplate().find(
				"from PoRoleHolderDynamic rhd where rhd.roleCompetenceBase.role=?" + 
				" and rhd.validto>?", keys);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4All(Date date) {
		Object[] keyValues = {date, date};
		return  getHibernateTemplate().find(
				"select rhd from PoRoleCompetenceAll rhca "
						+ " join rhca.roleHolderDynamics as rhd "
						+ " where rhd.validfrom <= ? and rhd.validto > ?", 
						keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4Group(
			PoGroup controlledGroup, Date date) {
		
		Object[] keyValues = {controlledGroup, date, date, };
		return getHibernateTemplate().find(
				"select rhd from PoRoleCompetenceGroup rhcg "
						+ " join rhcg.roleHolderDynamics as rhd "
						+ " where rhcg.competence4Group=? "
						+ " and rhd.validfrom<=? and rhd.validto>?", keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4Person(
			PoPerson controlledPerson, Date date) {
		Object[] keyValues = {controlledPerson, date, date};
		return getHibernateTemplate().find(
				"select rhd from PoRoleCompetencePerson rhcp "
						+ " join rhcp.roleHolderDynamics as rhd "
						+ " where rhcp.competence4Person=? "
						+ " and rhd.validfrom<=? and rhd.validto>?", keyValues);
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderDynamic> findRoleHolderDynamic(Date date) {
		Object[] keys = {date, date};
		return getHibernateTemplate().find(
				"from PoRoleHolderDynamic where validfrom<=? and validto>?", keys);
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4All(PoRole role, Date date) {
		Object[] keyValues = {role, date, date};
		return getHibernateTemplate().find(
				"select rhd from PoRoleCompetenceAll rhca "
						+ " join rhca.roleHolderDynamics as rhd "
						+ " where rhca.role=? and rhd.validfrom <= ? and rhd.validto > ?", 
						keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4Group(PoRole role,
			PoGroup controlledGroup, Date date) {
		Object[] keyValues = {role, controlledGroup, date, date, };
		return getHibernateTemplate().find(
				"select rhd from PoRoleCompetenceGroup rhcg "
						+ " inner join rhcg.roleHolderDynamics as rhd "
						+ " where rhcg.role=? and rhcg.competence4Group=? "
						+ " and rhd.validfrom<=? and rhd.validto>?", keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4Person(PoRole role,
			PoPerson controlledPerson, Date date) {
		Object[] keyValues = {role, controlledPerson, date, date};
		return getHibernateTemplate().find(
				"select rhd from PoRoleCompetencePerson rhcp "
						+ " join rhcp.roleHolderDynamics as rhd "
						+ " where rhcp.role=? and rhcp.competence4Person=? "
						+ " and rhd.validfrom<=? and rhd.validto>?", keyValues);
	}

	@Override
	protected Class<PoRoleHolderDynamic> getEntityClass() {
		return PoRoleHolderDynamic.class;
	}



}
