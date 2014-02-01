package at.workflow.webdesk.po.impl.daos;

import java.util.Date;
import java.util.List;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.daos.PoRoleHolderGroupDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoRoleHolderGroupDAOImpl extends GenericHibernateDAOImpl<PoRoleHolderGroup> implements
		PoRoleHolderGroupDAO {

	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4Person(PoRole role,
			PoPerson controlledPerson, Date date) {

		Object[] keyValues = { role, controlledPerson, date, date, date, date,
				date, date, date, date };
		return getHibernateTemplate().find(
				"select rhg from PoRoleHolderGroup as rhg "
						+ " join rhg.roleHolder as rh "
						+ " join rh.roleHolderPersons as rhp "
						+ " join rh.role as r "
						+ " where r=? and rh.competence4Person=?"
						+ " and rhg.validfrom<=? " + " and rhg.validto >? "
						+ " and r.validfrom <= ? " + " and r.validto > ? "
						+ " and rhp.validfrom <= ? " + " and rhp.validto > ? "
						+ " and rh.validfrom<=?" + " and rh.validto >? ",
				keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4AllF(PoRole role,
			PoGroup group, Date date) {
		Object[] keyValues = { role, group, date, date };
		return getHibernateTemplate().find(
				"select rhg from PoRoleCompetenceAll as rh "
						+ " join rh.roleHolderGroups as rhg "
						+ " where rh.role=? and rhg.group=?"
						+ " and rhg.validto >? " + " and rh.validto>?",
				keyValues);
	}

	public PoRoleHolderGroup findRoleHolderGroupWithCompetence4Person(
			PoRole role, PoGroup group, PoPerson controlledPerson, Date date) {

		Object[] keyValues = { role, group, controlledPerson, date, date, date,
				date };
		return (PoRoleHolderGroup) DataAccessUtils
				.uniqueResult(getHibernateTemplate().find(
						"select rhg from PoRoleCompetencePerson rhcp "
								+ " join rhcp.roleHolderGroups as rhg "
								+ " where rhcp.role=? and rhg.group=?"
								+ " and rhcp.competence4Person=? "
								+ " and rhg.validfrom<=? "
								+ " and rhg.validto >? "
								+ " and rhcp.validfrom<=? "
								+ " and rhcp.validto >?", keyValues));
	}

	

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroupF(PoPerson person, Date date) {
		Object[] keyValues = { person, date, date, date, date, date,
				new Integer(PoRole.NORMAL_ROLE) };
		return getHibernateTemplate().find(
				"select rhg from PoRoleHolderGroup as rhg "
						+ " join rhg.roleHolder as rh" + " join rh.role as r "
						+ " join rhg.group as g "
						+ " join g.personGroups as pg" + " where pg.person=? "
						+ " and rhg.validto >? " + " and rh.validto >? "
						+ " and r.validto >? " + " and g.validto >? "
						+ " and pg.validto >? " + " and r.roleType=?",
				keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroupAll(PoPerson person) {
		Object[] keyValues = { person, PoRole.NORMAL_ROLE };
		return getHibernateTemplate().find(
				"select rhg from PoRoleHolderGroup as rhg "
				+ " join rhg.roleHolder as rh" + " join rh.role as r "
				+ " join rhg.group as g "
				+ " join g.personGroups as pg" + " where pg.person=? "
				+ " and r.roleType=? order by rhg.validto desc",
				keyValues);
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroup(PoGroup group, Date date) {
		Object[] keyValues = { group, date, date };
		return getHibernateTemplate().find(
				"select distinct rhg from PoRoleHolderGroup as rhg "
						+ " where rhg.group=? " + " and rhg.validfrom <=? "
						+ " and rhg.validto >? ", keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroupF(PoRole role, Date date) {
		Object[] keyValues = { role, date, date };
		return getHibernateTemplate().find(
				"select distinct rhg from PoRoleHolderGroup as rhg "
						+ " join rhg.roleHolder as rh where rh.role = ? "
						+ " and rhg.validto > ? and rh.validto > ?", keyValues);
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroup(PoRole role, Date date) {
		Object[] keyValues = { role, date, date, date, date };
		return getHibernateTemplate().find(
				"select distinct rhg from PoRoleHolderGroup as rhg "
						+ " join rhg.roleHolder as rh where rh.role = ? "
						+ " and rhg.validto > ? and rhg.validfrom <= ? and rh.validto > ? and rh.validfrom <= ?", keyValues);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4Group(PoRole role,
			PoGroup group, Date date) {
		Object[] keyValues = { role, group, date, date, date, date };
		return getHibernateTemplate().find(
				"select rhg from PoRoleCompetenceGroup rhcg "
						+ " join rhcg.roleHolderGroups as rhg "
						+ " where rhcg.role=? and rhcg.competence4Group=?"
						+ " and rhg.validfrom<=? " + " and rhg.validto >? "
						+ " and rhcg.validfrom<=? " + " and rhcg.validto >? "
						+ " order by rhg.ranking ", keyValues);
	}
	

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4GroupF(PoRole role, PoGroup officeHolder, PoGroup target, Date date) {
		Object[] keyValues = { role, officeHolder,target, date, date, date};
		return getHibernateTemplate()
				.find(
						" select rhg from PoRoleCompetenceGroup as rhcg "
								+ " join rhcg.roleHolderGroups as rhg "
								+ " where rhcg.role=? and rhg.group=? "
								+ " and rhcg.competence4Group=? "
								+ " and rhg.validto >? "
								+ " and rhcg.validto >? "
								+ " and rhg.group.validto>?"
								+ " order by rhg.validfrom, rhg.ranking",
						keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4ClientF(PoRole role, PoGroup officeHolder, PoClient target, Date date) {
		Object[] keyValues = { role, officeHolder, target, date, date, date};
		return getHibernateTemplate()
				.find(
						" select rhg from PoRoleCompetenceClient as rhcg "
								+ " join rhcg.roleHolderGroups as rhg "
								+ " where rhcg.role=? and rhg.group=? "
								+ " and rhcg.competence4Client=? "
								+ " and rhg.validto >? "
								+ " and rhcg.validto >? "
								+ " and rhg.group.validto>?"
								+ " order by rhg.validfrom, rhg.ranking",
								keyValues);
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4PersonF(PoRole role, PoGroup officeHolder, PoPerson target, Date date) {
		Object[] keyValues = { role, officeHolder, target, date, date, date};
		return getHibernateTemplate()
				.find(
						" select rhg from PoRoleCompetencePerson as rhcp "
								+ " join rhcp.roleHolderGroups as rhg "
								+ " where rhcp.role=? and rhg.group=? "
								+ " and rhcp.competence4Person=? "
								+ " and rhg.validto >? "
								+ " and rhcp.validto >? "
								+ " and rhg.group.validto>? "
								+ " order by rhg.validto, rhg.ranking ",
						keyValues);
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findDistinctRoleHolderGroupsWithCompetence4Person(PoRole role,
			PoPerson controlledPerson, Date date) {

		// Groups with Competence over the person itself
		Object[] keyValues = { role, controlledPerson, date, date, date, date,
				date, date };
		List<PoRoleHolderGroup> l = getHibernateTemplate().find(
				" select distinct rhg from PoRoleCompetencePerson as rhcp "
						+ " join rhcp.roleHolderGroups as rhg "
						+ " join rhg.group as g "
						+ " where rhcp.role=? and rhcp.competence4Person=?"
						+ " and rhcp.validfrom<=? " + " and rhcp.validto >? "
						+ " and rhg.validfrom<=? " + " and rhg.validto >?"
						+ " and g.validfrom<=? " + " and g.validto >?"
						+ " order by rhg.ranking", keyValues);
		return l;
	}

	
		
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findDistinctRoleHolderGroupsWithCompetence4Group(PoRole role, PoGroup group, Date date) {	
		Object[] keys = { role, group, date, date, date, date, date, date };
		List<PoRoleHolderGroup> l_g = getHibernateTemplate().find(
				" select distinct rhg from PoRoleCompetenceGroup as rhcg "
						+ " join rhcg.roleHolderGroups as rhg "
						+ " join rhg.group as g "
						+ " where rhcg.role=? and rhcg.competence4Group=?"
						+ " and rhcg.validfrom<=? " + " and rhcg.validto >? "
						+ " and rhg.validfrom<=? " + " and rhg.validto >?"
						+ " and g.validfrom<=? " + " and g.validto >?"
						+ " order by rhg.ranking", keys);
		return l_g;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroupF(PoGroup group, Date date) {
		Object[] keyValues = { group, new Integer(PoRole.NORMAL_ROLE), date, date, date, };
		return getHibernateTemplate().find(
						"select distinct rhg from PoRoleHolderGroup as rhg "
								+ " join rhg.roleHolder as rh "
								+ " join rh.role as r"
								+ " where rhg.group = ? and r.roleType = ? "
								+ "   and rh.validto > ? and r.validto > ? and rhg.validto > ?",
						keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderGroup> findRoleHolderGroupAll(PoGroup group) {
		Object[] keyValues = { group, new Integer(PoRole.NORMAL_ROLE) };
		return getHibernateTemplate()
		.find(
				"select rhg from PoRoleHolderGroup as rhg "
				+ " join rhg.roleHolder as rh "
				+ " join rh.role as r"
				+ " where rhg.group=? and r.roleType=? "
				+ " order by rhg.roleHolder.role.name asc, rhg.validfrom desc",
				keyValues);
	}

	@Override
	protected Class<PoRoleHolderGroup> getEntityClass() {
		return PoRoleHolderGroup.class;
	}

}
