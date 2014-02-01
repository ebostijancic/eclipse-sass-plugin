package at.workflow.webdesk.po.impl.daos;

import java.util.Date;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import at.workflow.webdesk.po.daos.PoRoleHolderDAO;
import at.workflow.webdesk.po.daos.PoRoleHolderGroupDAO;
import at.workflow.webdesk.po.daos.PoRoleHolderPersonDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.po.model.PoRoleHolderLink;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;

public class PoRoleHolderDAOImpl extends HibernateDaoSupport implements PoRoleHolderDAO {
	
	private PoRoleHolderGroupDAO roleHolderGroupDAO;
	private PoRoleHolderPersonDAO roleHolderPersonDAO;
	

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4Group(PoGroup controlledGroup, Date date) {
		
		Object[] keyValues = { controlledGroup, date, date, date, date };

		// directly connected relations (persons which have rights for the given
		// group)
		@SuppressWarnings("rawtypes")
		List res = getHibernateTemplate().find(
				"select rhp from PoRoleCompetenceGroup rhcg "
						+ " join rhcg.roleHolderPersons as rhp "
						+ " where rhcg.competence4Group=? "
						+ " and rhcg.validfrom <= ? and rhcg.validto > ?"
						+ " and rhp.validfrom<=? and rhp.validto>?", keyValues);

		// directly connected relations (groups which have rights for the group
		// - may not be a parentgroup
		res.addAll(getHibernateTemplate()
					.find(
							"select rhg from PoRoleCompetenceGroup rhcg "
									+ " join rhcg.roleHolderGroups as rhg "
									+ " where rhcg.competence4Group=? "
									+ " and rhcg.validfrom <= ? and rhcg.validto > ?"
									+ " and rhg.validfrom<=? and rhg.validto>?",
							keyValues));
		
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4Client(PoClient client, Date date) {
		Object[] keyValues = { client, date, date, date, date };
		
		// directly connected relations (persons which have rights for the given
		// group)
		@SuppressWarnings("rawtypes")
		List res = getHibernateTemplate().find(
				"select rhp from PoRoleCompetenceClient rhcg "
						+ " join rhcg.roleHolderPersons as rhp "
						+ " where rhcg.competence4Client=? "
						+ " and rhcg.validfrom <= ? and rhcg.validto > ?"
						+ " and rhp.validfrom<=? and rhp.validto>?", keyValues);
		
		// directly connected relations (groups which have rights for the group
		// - may not be a parentgroup
		res
		.addAll(getHibernateTemplate()
				.find(
						"select rhg from PoRoleCompetenceClient rhcg "
								+ " join rhcg.roleHolderGroups as rhg "
								+ " where rhcg.competence4Client=? "
								+ " and rhcg.validfrom <= ? and rhcg.validto > ?"
								+ " and rhg.validfrom<=? and rhg.validto>?",
								keyValues));
		return res;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4Person(PoPerson controlledPerson,
			Date date) {
		Object[] keyValues = { controlledPerson, date, date, date, date };

		// directly connected relations (persons which have rights for the given
		// person)

		@SuppressWarnings("rawtypes")
		List direct_rh_list = getHibernateTemplate().find(
				"select distinct rhp from PoRoleCompetencePerson rhcp "
						+ " join rhcp.roleHolderPersons as rhp "
						+ " where rhcp.competence4Person=? "
						+ " and rhcp.validfrom <= ? and rhcp.validto > ? "
						+ " and rhp.validfrom<=? and rhp.validto>?", keyValues);

		// directly connected relations (groups which have rights for the person
		// - may not be a parentgroup
		direct_rh_list
				.addAll(getHibernateTemplate()
						.find(
								"select distinct rhg from PoRoleCompetencePerson rhcp "
										+ " join rhcp.roleHolderGroups as rhg "
										+ " where rhcp.competence4Person=? "
										+ " and rhcp.validfrom <= ? and rhcp.validto > ? "
										+ " and rhg.validfrom<=? and rhg.validto>?",
								keyValues));
		return direct_rh_list;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4All(PoClient client, Date date) {

		// Roleholders which have rights over all
		Object[] keys = { client, date, date, date, date, date, date };
		@SuppressWarnings("rawtypes")
		List res = getHibernateTemplate().find(
				"select distinct rhp from PoRoleCompetenceAll rh "
						+ " join rh.roleHolderPersons as rhp"
						+ " join rh.role as r "
						+ " where (r.client = ? or r.client=null)"
						+ " and rhp.validfrom<=? and rhp.validto>? "
						+ " and r.validfrom<=? and r.validto>?"
						+ " and rh.validfrom<=? and rh.validto>?", keys);
		res.addAll(getHibernateTemplate().find(
				"select distinct rhg from PoRoleCompetenceAll rh "
						+ " join rh.roleHolderGroups as rhg"
						+ " join rh.role as r "
						+ " where (r.client = ? or r.client=null)"
						+ " and r.validfrom<=? and r.validto>? "
						+ " and rhg.validfrom<=? and rhg.validto>? "
						+ " and rh.validfrom<=? and rh.validto>?", keys));
		return res;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4Person(PoRole role, PoPerson controlledPerson, Date date) {
		
		@SuppressWarnings("rawtypes")
		List ret = this.roleHolderGroupDAO.findRoleHolderGroupWithCompetence4Person(role, controlledPerson, date);
		ret.addAll(this.roleHolderPersonDAO.findRoleHolderPersonWithCompetence4Person(role, controlledPerson, date));
		
		return ret;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4Group(PoRole role, PoGroup controlledGroup, Date date) {
		
		@SuppressWarnings("rawtypes")
		List ret = this.roleHolderGroupDAO.findRoleHolderGroupWithCompetence4Group(role, controlledGroup, date);
		ret.addAll(this.roleHolderPersonDAO.findRoleHolderPersonWithCompetence4Group(role, controlledGroup, date));
		
		return ret;
	}

	@Override
	public boolean hasPersonRoleAssigned(PoPerson p, PoRole r, Date date) {
		Object[] keyValues = { p, r, date, date, date, date };
		return getHibernateTemplate()
				.find(
						"from PoRoleHolderPerson rhp where "
								+ "rhp.person=? and rhp.roleHolder.role=? and rhp.roleHolder.role.validfrom<=? and "
								+ "rhp.roleHolder.role.validto>? and rhp.roleHolder.validfrom <=? and rhp.roleHolder.validto>?",
						keyValues).size() > 0;

	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean hasPersonRoleAssignedWithCompetence4All(PoPerson p,
			PoRole r, Date date) {
		Object[] keyValues = { p, r, date, date, date, date, date, date };
		List<PoRoleHolderPerson> ret = getHibernateTemplate()
				.find(
						"from PoRoleHolderPerson rhp where "
								+ "rhp.person=? and rhp.roleHolder.role=? and "
								+ "rhp.roleHolder.role.validfrom<=? and rhp.roleHolder.role.validto>? and "
								+ "rhp.validfrom <=? and rhp.validto>? and "
								+ "rhp.roleHolder.validfrom<=? and rhp.roleHolder.validto>?",
						keyValues);

		return ret.size() > 0;

	}

	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetenceBase> findRoleHolders(PoRole role, Date date) {
		Object[] keys = {role, date, date, date, date, date, date, date, date};
		
		return getHibernateTemplate().find(
				"select distinct rcb from PoRoleCompetenceBase  rcb " +
				"left outer join rcb.roleHolderPersons as rhp " + 
				"left outer join rcb.roleHolderGroups as rhg " +
				"left outer join rcb.roleHolderDynamics as rhd "+
				" where rcb.role=? and " +
				" rcb.validfrom<=? and rcb.validto>? and" +
				"(rhp.validfrom<=? and rhp.validto>? or " +
				"rhg.validfrom<=? and rhg.validto>?  or " +
				" rhd.validfrom <=? and rhd.validto>?)" 
				, keys);
	}



	public void setRoleHolderGroupDAO(PoRoleHolderGroupDAO roleHolderGroupDAO) {
		this.roleHolderGroupDAO = roleHolderGroupDAO;
	}

	public void setRoleHolderPersonDAO(PoRoleHolderPersonDAO roleHolderPersonDAO) {
		this.roleHolderPersonDAO = roleHolderPersonDAO;
	}

}
