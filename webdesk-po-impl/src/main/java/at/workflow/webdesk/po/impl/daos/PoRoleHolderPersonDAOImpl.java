package at.workflow.webdesk.po.impl.daos;

import java.util.Date;
import java.util.List;

import org.springframework.dao.support.DataAccessUtils;
import at.workflow.webdesk.po.daos.PoRoleHolderPersonDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoRoleHolderPersonDAOImpl extends GenericHibernateDAOImpl<PoRoleHolderPerson> implements
		PoRoleHolderPersonDAO {
	
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Client(PoRole role,
			PoClient client, Date date) {
		
		Object[] keyValues = { role, client, date, date, date, date, date, date };
		return getHibernateTemplate().find(
				" select rhp from PoRoleCompetenceClient as rhcg "
						+ " join rhcg.roleHolderPersons as rhp "
						+ " where rhcg.role=? and rhcg.competence4Client=?"
						+ " and rhp.validfrom<=? and rhp.validto >? "
						+ " and rhcg.validfrom<=? and rhcg.validto >? "
						+ " and rhp.person.validfrom<=? and rhp.person.validto>? "
						+ " order by rhp.ranking",
						keyValues);
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Group(PoRole role,
			PoGroup controlledGroup, Date date) {

		Object[] keyValues = { role, controlledGroup, date, date, date, date,
				date, date };
		return getHibernateTemplate().find(
				" select rhp from PoRoleCompetenceGroup as rhcg "
						+ " join rhcg.roleHolderPersons as rhp "
						+ " where rhcg.role=? and rhcg.competence4Group=?"
						+ " and rhp.validfrom<=? and rhp.validto >? "
						+ " and rhcg.validfrom<=? and rhcg.validto >? "
						+ " and rhp.person.validfrom<=? and rhp.person.validto>? "
						+ " order by rhp.ranking",
				keyValues);
	}

	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Person(
			PoRole role, PoPerson person, PoPerson controlledPerson, Date date,
			boolean deputy) {
		Object[] keyValues = { role, person, controlledPerson, date, date,
				date, date, date};
		if (deputy) 
			return (PoRoleHolderPerson) DataAccessUtils
					.uniqueResult(getHibernateTemplate()
							.find(
									" select rhp from PoRoleCompetencePerson as rhcp "
											+ " join rhcp.roleHolderPersons as rhp "
											+ " where rhcp.role=? and rhp.person=? and rhcp.competence4Person=?"
											+ " and rhp.validfrom<=? and rhp.validto >? "
											+ " and rhcp.validto >? "
											+ " and rhp.person.validfrom<=? and rhp.person.validto>? " +
													"and rhp.deputy is not null order by rhp.ranking" ,
									keyValues));
		else
			return (PoRoleHolderPerson) DataAccessUtils
					.uniqueResult(getHibernateTemplate()
							.find(
									" select rhp from PoRoleCompetencePerson as rhcp "
											+ " join rhcp.roleHolderPersons as rhp "
											+ " where rhcp.role=? and rhp.person=? and rhcp.competence4Person=?"
											+ " and rhp.validfrom<=? and rhp.validto >? "
											+ " and rhcp.validto >? "
											+ " and rhp.person.validfrom<=? and rhp.person.validto>? " +
													"and rhp.deputy is null order by rhp.ranking",
									keyValues));
	}	

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Person(PoRole role,
			PoPerson person, Date date) {
		Object[] keyValues = { role, person, date, date, date, date, date, date };
		return getHibernateTemplate()
				.find(
						" select rhp from PoRoleCompetencePerson as rhcp "
								+ " join rhcp.roleHolderPersons as rhp "
								+ " where rhcp.role=? and rhcp.competence4Person=? "
								+ " and rhp.validfrom<=? and rhp.validto >? "
								+ " and rhcp.validfrom<=? and rhcp.validto >? "
								+ " and rhp.person.validfrom<=? and rhp.person.validto>?"
								+ "  order by rhp.ranking"
								,
						keyValues);
	}

	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Group(
			PoRole role, PoPerson person, PoGroup controlledGroup, Date date, boolean deputy) {
		Object[] keyValues = { role, person, controlledGroup, date, date,
				date, date, date };
		
		
		if (deputy) 
			return (PoRoleHolderPerson) DataAccessUtils
					.uniqueResult(getHibernateTemplate()
							.find(
									" select rhp from PoRoleCompetenceGroup as rhcg "
											+ " join rhcg.roleHolderPersons as rhp "
											+ " where rhcg.role=? and rhp.person=? and rhcg.competence4Group=?"
											+ " and rhp.validfrom<=? and rhp.validto >? "
											+ " and rhcg.validto >? "
											+ " and rhp.person.validfrom<=? and rhp.person.validto>? "
											+ " and rhp.deputy is not null",
									keyValues));
		else
			return (PoRoleHolderPerson) DataAccessUtils
					.uniqueResult(getHibernateTemplate()
							.find(
									" select rhp from PoRoleCompetenceGroup as rhcg "
											+ " join rhcg.roleHolderPersons as rhp "
											+ " where rhcg.role=? and rhp.person=? and rhcg.competence4Group=?"
											+ " and rhp.validfrom<=? and rhp.validto >? "
											+ " and rhcg.validto >? "
											+ " and rhp.person.validfrom<=? and rhp.person.validto>? "
											+ " and rhp.deputy is null",
									keyValues));
	}

	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Client(
			PoRole role, PoPerson person, PoClient client, Date date) {
		Object[] keyValues = { role, person, client, date, date, date, date, date };
		
		return (PoRoleHolderPerson) DataAccessUtils
				.uniqueResult(getHibernateTemplate()
						.find(
								" select rhp from PoRoleCompetenceClient as rhcg "
										+ " join rhcg.roleHolderPersons as rhp "
										+ " where rhcg.role=? and rhp.person=? and rhcg.competence4Client=?"
										+ " and rhp.validfrom<=? and rhp.validto >? "
										+ " and rhcg.validto >? "
										+ " and rhp.person.validfrom<=? and rhp.person.validto>? ",
										keyValues));
	}
	
	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4All(
			PoRole role, PoPerson person, Date date, boolean deputy) {
		Object[] keyValues = { role, person, date, date, date, date, date };
		if (deputy)
		return (PoRoleHolderPerson) DataAccessUtils
				.uniqueResult(getHibernateTemplate()
						.find(
								" select rhp from PoRoleCompetenceAll as rh "
										+ " join rh.roleHolderPersons as rhp "
										+ " where rh.role=? and rhp.person=? "
										+ " and rhp.validfrom<=? and rhp.validto >? "
										+ " and rh.validto >? and  rhp.person.validfrom<=? and rhp.person.validto>?" +
												" and rhp.deputy is not null",
								keyValues));
		else
			return (PoRoleHolderPerson) DataAccessUtils
				.uniqueResult(getHibernateTemplate()
						.find(
								" select rhp from PoRoleCompetenceAll as rh "
										+ " join rh.roleHolderPersons as rhp "
										+ " where rh.role=? and rhp.person=? "
										+ " and rhp.validfrom<=? and rhp.validto >? "
										+ " and rh.validto >? and  rhp.person.validfrom<=? and rhp.person.validto>? " +
												" and rhp.deputy is null",
								keyValues));
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4AllF(PoRole role,
			PoPerson person, Date date) {
		Object[] keyValues = { role, person, date, date , date};
		return getHibernateTemplate().find(
				" select rhp from PoRoleCompetenceAll as rh "
						+ " join rh.roleHolderPersons as rhp "
						+ " where rh.role=? and rhp.person=? "
						+ " and rhp.validto >? " + " and rh.validto >? and "
						+ " rhp.person.validto>? order by rhp.validfrom asc",
				keyValues);
	}


	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPerson(PoPerson person, Date date) {
		Object[] keyValues = { person, date, date, date, date, date, date,
				new Integer(PoRole.NORMAL_ROLE) };
		return getHibernateTemplate().find(
				"select rhp from PoRoleHolderPerson as rhp "
						+ " join rhp.roleHolder as rh join rh.role as r "
						+ " where rhp.person=? "
						+ " and rhp.validfrom<=? and rhp.validto >? "
						+ " and rh.validfrom<=? and rh.validto >? "
						+ " and r.validfrom<=? and r.validto >? "
						+ " and r.roleType=?", keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonF(PoPerson person, Date date) {
		Object[] keyValues = { person, date, date, date,
				new Integer(PoRole.NORMAL_ROLE) };
		List<PoRoleHolderPerson> l = getHibernateTemplate().find(
				"select rhp from PoRoleHolderPerson as rhp "
						+ " join rhp.roleHolder as rh " + " join rh.role as r "
						+ " where rhp.person=? " + " and rhp.validto >? "
						+ " and rh.validto >? " + " and r.validto >? "
						+ " and r.roleType=? order by r.name", keyValues);
		return l;
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonAll(PoPerson person) {
		Object[] keyValues = { person, new Integer(PoRole.NORMAL_ROLE) };
		List<PoRoleHolderPerson> l = getHibernateTemplate().find(
				"select rhp from PoRoleHolderPerson as rhp "
				+ " join rhp.roleHolder as rh " + " join rh.role as r "
				+ " where rhp.person=? " 
				+ " and r.roleType=? order by rhp.validto desc", keyValues);
		return l;
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonF(PoRole role, Date date) {
		Object[] keyValues = { role, date, date, date, date,
				new Integer(PoRole.NORMAL_ROLE) };
		return getHibernateTemplate().find(
				"select rhp from PoRoleHolderPerson as rhp "
						+ " join rhp.roleHolder as rh" + " join rh.role as r"
						+ " join rhp.person as p" + " where r = ? "
						+ " and rhp.validto>? " + " and rh.validto>?"
						+ " and p.validto>?" + " and r.validto>?"
						+ " and r.roleType=? order by p.lastName, rhp.ranking",
				keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPerson(PoRole role, Date date) {
		Object[] keyValues = { role, date, date, date, date, date, date, date,
				date, new Integer(PoRole.NORMAL_ROLE) };
		return getHibernateTemplate().find(
				"select rhp from PoRoleHolderPerson as rhp "
						+ " join rhp.roleHolder as rh" + " join rh.role as r"
						+ " join rhp.person as p" + " where r = ? "
						+ " and rhp.validfrom<? and rhp.validto>? "
						+ " and rh.validfrom<? and rh.validto>?"
						+ " and p.validfrom<? and p.validto>?"
						+ " and r.validfrom<? and r.validto>?"
						+ " and r.roleType=? order by p.lastName, rhp.ranking",
				keyValues);
	}
	

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4GroupF(PoRole role, PoPerson officeHolder, PoGroup target, Date date) {
		Object[] keyValues = { role, officeHolder, target,  date, date, date};
		return getHibernateTemplate()
				.find(
						" select rhp from PoRoleCompetenceGroup as rhcg "
								+ " join rhcg.roleHolderPersons as rhp "
								+ " where rhcg.role=? and rhp.person=? "
								+ " and rhcg.competence4Group=? "
								+ " and rhp.validto >? "
								+ " and rhcg.validto >? "
								+ " and rhp.person.validto>?"
								+ " order by rhp.validfrom, rhp.ranking",
						keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4ClientF(PoRole role, PoPerson officeHolder, PoClient target, Date date) {
		Object[] keyValues = { role, officeHolder, target,  date, date, date};
		return getHibernateTemplate()
				.find(
						" select rhp from PoRoleCompetenceClient as rhcg "
								+ " join rhcg.roleHolderPersons as rhp "
								+ " where rhcg.role=? and rhp.person=? "
								+ " and rhcg.competence4Client=? "
								+ " and rhp.validto >? "
								+ " and rhcg.validto >? "
								+ " and rhp.person.validto>?"
								+ " order by rhp.validfrom, rhp.ranking",
								keyValues);
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4PersonF(PoRole role, PoPerson officeHolder, PoPerson target, Date date) {
		Object[] keyValues = { role, officeHolder, target,  date, date, date};
		return getHibernateTemplate()
				.find(
						" select rhp from PoRoleCompetencePerson as rhcp "
								+ " join rhcp.roleHolderPersons as rhp "
								+ " where rhcp.role=? and rhp.person=? "
								+ " and rhcp.competence4Person=? "
								+ " and rhp.validto >? "
								+ " and rhcp.validto >? "
								+ " and rhp.person.validto>?"
								+ " order by rhp.validfrom, rhp.ranking",
						keyValues);
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4GroupF(PoRole role, PoPerson officeHolder, Date date) {
		Object[] keyValues = { role, officeHolder, date, date, date};
		return getHibernateTemplate()
				.find(
						" select rhp from PoRoleCompetenceGroup as rhcg "
								+ " join rhcg.roleHolderPersons as rhp "
								+ " where rhcg.role=? and rhp.person=? "
								+ " and rhp.validto >? "
								+ " and rhcg.validto >? "
								+ " and rhp.person.validto>?"
								+ " order by rhp.validfrom, rhp.ranking",
						keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4PersonF(PoRole role, PoPerson officeHolder, Date date) {
		Object[] keyValues = { role, officeHolder, date, date, date};
		return getHibernateTemplate()
				.find(
						" select rhp from PoRoleCompetencePerson as rhcp "
								+ " join rhcp.roleHolderPersons as rhp "
								+ " where rhcp.role=? and rhp.person=? "
								+ " and rhp.validto >? "
								+ " and rhcp.validto >? "
								+ " and rhp.person.validto>?" 
								+ " order by rhp.validfrom, rhp.ranking",
						keyValues);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderPerson> findRoleHolderPersonsWithRoleF(PoRole role, Date date) {
		Object[] keyValues = { role, date, date, date };
		return getHibernateTemplate().find(
				"select rhp from PoRoleCompetenceBase as rh"
						+ " join rh.roleHolderPersons as rhp "
						+ " join rhp.person as p " + " where rh.role = ? "
						+ " and rhp.validto >?" + " and rh.validto >?"
						+ " and p.validto >?" + " order by rhp.ranking",
				keyValues);
	}


	@Override
	protected Class<PoRoleHolderPerson> getEntityClass() {
		return PoRoleHolderPerson.class;
	}
	

}
