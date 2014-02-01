package at.workflow.webdesk.po.impl.daos;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoRoleDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;
import at.workflow.webdesk.tools.HistoricizingDAOImpl;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * The DataAccessObject for <code>PoRole</code> objects.
 * This is the Hibernate Implementation.
 * <p/>
 * Refactoring Notes (8.06.2007)
 * <br/>
 * + PoRoleHolder -> PoRoleCompetenceAll <br/> 
 * + PoRoleHolderCompetencePerson -> PoRoleCompetencePerson <br/> 
 * + PoRoleHolderCompetenceGroup -> PoRoleCompetenceGroup <br/> 
 * + PoRoleHolderBase -> PoRoleCompetenceBase <br/>
 * <br/>
 * Created on 25.04.2005
 * 
 * @author ggruber
 * @author hentner
 * @author fflotzinger
 * @author sdzuban
 */
public class PoRoleDAOImpl extends HistoricizingDAOImpl<PoRole> implements PoRoleDAO {

	@Override
	protected Class<PoRole> getEntityClass() {
		return PoRole.class;
	}

	/**
	 * <b> Uniqueness Constraint</b><br/> ************************* <br/>
	 * <br/> a role has to be unique<br/> <br/> + with its name and client<br/> +
	 * with its participantId and client<br/> <br/> For more information check
	 * the abstract definition of this function<br/> <br/>
	 */
	private void checkRole(PoRole role) {
		getHibernateTemplate().evict(role);
		
		final Map<String, Object> uniqueAttributes = new HashMap<String, Object>();
		uniqueAttributes.put("name", role.getName());
		uniqueAttributes.put("client", role.getClient());
		if (isAttributeSetUnique(uniqueAttributes, role.getUID(), role.getValidity()) == false)
			throw new PoRuntimeException(PoRuntimeException.MESSAGE_DUPLICATE_ROLE_NAME_AND_CLIENT);
		
		uniqueAttributes.clear();
		uniqueAttributes.put("participantId", role.getParticipantId());
		uniqueAttributes.put("client", role.getClient());
		if (isAttributeSetUnique(uniqueAttributes, role.getUID(), role.getValidity()) == false)
			throw new PoRuntimeException(PoRuntimeException.MESSAGE_DUPLICATE_ROLE_PARTICIPANT_AND_CLIENT);
	}

	public List<PoRole> loadAllRoles() {
		return getHibernateTemplate().loadAll(PoRole.class);
	}

	public List<PoRole> loadAllRoles(PoClient client) {
		return this.loadAllRoles(client, new Date());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRole> loadAllRoles(Date referenceDate) {
		Object[] keyValues = { referenceDate, referenceDate, new Integer(PoRole.NORMAL_ROLE) };
		return getHibernateTemplate().find(
				"from PoRole r " + " where r.validfrom <= ? and r.validto > ? and r.roleType=? order by name",
				keyValues);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRole> loadAllRoles(PoClient client, Date referenceDate) {
		Object[] keyValues = { client, referenceDate, referenceDate,
				new Integer(PoRole.NORMAL_ROLE) };
		return getHibernateTemplate().find(
				"from PoRole r " + " where r.client = ? and r.validfrom<= ? "
						+ " and r.validto>? and r.roleType=?", keyValues);
	}

	@Override
	protected void beforeSave(PoRole role)	{
		checkRole(role);
	}
	
	/** Overridden to load "from PoRole order by name asc". */
	@SuppressWarnings("unchecked")
	@Override
	public List<PoRole> loadAll() {
		return getHibernateTemplate().find("from PoRole order by name asc");
	}

	@Override
	@SuppressWarnings("unchecked")
	public PoRole findRoleByNameAndNullClient(String roleName) {
		final Date now = DateTools.now();
		Object[] criteria = { roleName, now, now };
		return (PoRole) DataAccessUtils.uniqueResult(getHibernateTemplate().find(
			"from PoRole r where r.name = ? and r.client is null and r.validfrom <= ? and r.validto > ?",
			criteria));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoRole> findRoleByName(String name, Date referenceDate) {
		Object[] keyValues = { name, referenceDate, referenceDate };
		return getHibernateTemplate().find(
			"from PoRole r where r.name = ? and r.validfrom <= ? and r.validto > ?",
			keyValues);
	}

	@Override
	@SuppressWarnings("unchecked")
	public PoRole findRoleByName(String name, PoClient client, Date referenceDate) {
		Object[] keyValues = { name, client, referenceDate, referenceDate };
		return (PoRole) DataAccessUtils.uniqueResult(getHibernateTemplate().find(
			"from PoRole r where r.name = ? and  r.client = ? and r.validfrom <= ? and r.validto > ?",
			keyValues));
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoPerson> findPersonsWithCompetence4Group(PoRole role, PoGroup controlledGroup, Date date) {
		Object[] keyValues = { role, controlledGroup, date, date, date, date, date, date };
		return getHibernateTemplate().find(
						" select p from PoRoleCompetenceGroup as rhcg "
								+ " join rhcg.roleHolderPersons as rhp "
								+ " join rhp.person as p "
								+ " where rhcg.role=? and rhcg.competence4Group=?"
								+ " and rhcg.validfrom<=? and rhcg.validto >? "
								+ " and rhp.validfrom<=? and rhp.validto>? "
								+ " and p.validfrom<=? and p.validto>? order by rhp.ranking ",
						keyValues);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoPerson> findPersonsWithCompetence4Person(PoRole role, PoPerson controlledPerson, Date date) {

		Object[] keyValues = { role, controlledPerson, date, date, date, date, date, date };
		List<PoRoleHolderPerson> l_rhp_persons = getHibernateTemplate().find(
				" select distinct rhp from PoRoleCompetencePerson as rhcp "
						+ " join rhcp.roleHolderPersons as rhp "
						+ " join rhp.person as p "
						+ " where rhcp.role=? and rhcp.competence4Person=?"
						+ " and rhcp.validfrom<=? " + " and rhcp.validto >? "
						+ " and rhp.validfrom<=? " + " and rhp.validto >? "
						+ " and p.validfrom<=? " + " and p.validto >? "
						+ " order by rhp.ranking", keyValues);
		List<PoPerson> result = new ArrayList<PoPerson>();
		for (PoRoleHolderPerson rhp : l_rhp_persons) {
			result.add(rhp.getPerson());
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoPerson> findPersonsWithCompetence4Client(PoRole role, PoClient client, Date date) {
		
		Object[] keyValues = { role, client, date, date, date, date, date, date };
		List<PoRoleHolderPerson> l_rhp_persons = getHibernateTemplate().find(
				" select distinct rhp from PoRoleCompetenceClient as rhcp "
						+ " join rhcp.roleHolderPersons as rhp "
						+ " join rhp.person as p "
						+ " where rhcp.role=? and rhcp.competence4Client=?"
						+ " and rhcp.validfrom<=? " + " and rhcp.validto >? "
						+ " and rhp.validfrom<=? " + " and rhp.validto >? "
						+ " and p.validfrom<=? " + " and p.validto >? "
						+ " order by rhp.ranking", keyValues);
		List<PoPerson> result = new ArrayList<PoPerson>();
		for (PoRoleHolderPerson rhp : l_rhp_persons) {
			result.add(rhp.getPerson());
		}
		return result;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoGroup> findGroupsWithCompetence4Group(PoRole role,
			PoGroup controlledGroup, Date date) {
		Object[] keyValues = { role, controlledGroup, date, date, date, date, date, date };
		return getHibernateTemplate().find(
				" select g from PoRoleCompetenceGroup as rhcg "
						+ " join rhcg.roleHolderGroups as rhg "
						+ " join rhg.group as g "
						+ " where rhcg.role=? and rhcg.competence4Group=?"
						+ " and rhcg.validfrom<=? and rhcg.validto >? "
						+ " and g.validfrom<=? and g.validto >? "
						+ " and rhg.validfrom<=? and rhg.validto >? ",
				keyValues);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoGroup> findGroupsWithCompetence4Client(PoRole role,
			PoClient client, Date date) {
		Object[] keyValues = { role, client, date, date, date, date, date, date };
		return getHibernateTemplate().find(
				" select g from PoRoleCompetenceClient as rhcg "
						+ " join rhcg.roleHolderGroups as rhg "
						+ " join rhg.group as g "
						+ " where rhcg.role=? and rhcg.competence4Client=?"
						+ " and rhcg.validfrom<=? and rhcg.validto >? "
						+ " and g.validfrom<=? and g.validto >? "
						+ " and rhg.validfrom<=? and rhg.validto >? ",
						keyValues);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoRole> findRolesOfPerson(PoPerson person, Date date) {
		Object[] keyValues = { person, date, date, date, date };
		// get all roles that are directly connected through
		// PoRoleHolderPerson objects
		List<PoRole> res = getHibernateTemplate().find(
				"select distinct rhb.role from PoRoleCompetenceBase as rhb "
						+ " join rhb.roleHolderPersons as rhp "
						+ " where rhp.person=? "
						+ " and rhp.validfrom<=? and rhp.validto >? "
						+ " and rhb.validfrom<=? and rhb.validto >? ",
				keyValues);
		Object[] kV = { person, date, date, date, date, date, date, date, date };
		// get all roles that are directly connected to a group which
		// is directly connected to the given person
		List<PoRole> rolesFromGroup = getHibernateTemplate().find(
				"select distinct rhb.role from PoRoleCompetenceBase as rhb "
						+ "join rhb.roleHolderGroups as rhg "
						+ "join rhg.group as g " + "join g.personGroups as pg "
						+ "where pg.person = ? "
						+ "and g.validfrom<=? and g.validto > ? "
						+ "and pg.validfrom<=? and pg.validto > ? "
						+ "and rhg.validfrom<=? and rhg.validto>? "
						+ "and rhb.validfrom<=? and rhb.validto>? ", kV);
		
		rolesFromGroup.removeAll(res);
		res.addAll(rolesFromGroup);
		return res;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoPerson> findCompetencePersonsOfPerson(PoPerson person, PoRole role,
			Date date) {
		List<PoPerson> res = new ArrayList<PoPerson>();
		Object[] keyValues = { person, role, date, date, date, date };
		// get all roles that are directly connected through
		// PoRoleHolderPerson objects
		res = getHibernateTemplate().find(
				"select rhcp.competence4Person from PoRoleCompetencePerson as rhcp "
						+ " join rhcp.roleHolderPersons as rhp "
						+ " where rhp.person=? " + " and rhcp.role=? "
						+ " and rhp.validfrom<=? and rhp.validto >? "
						+ " and rhcp.validfrom<=? and rhcp.validto>? ",
				keyValues);
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoGroup> findCompetenceGroupsOfPerson(PoPerson person, PoRole role,
			Date date) {
		List<PoGroup> res = new ArrayList<PoGroup>();
		Object[] keyValues = { person, role, date, date, date, date };
		// get all roles that are directly connected through
		// PoRoleHolderPerson objects
		res = getHibernateTemplate().find(
				"select rhcg.competence4Group from PoRoleCompetenceGroup as rhcg "
						+ " join rhcg.roleHolderPersons as rhp "
						+ " where rhp.person=? " + " and rhcg.role=? "
						+ " and rhp.validfrom<=? and rhp.validto >? "
						+ " and rhcg.validfrom<=? and rhcg.validto>? ",
				keyValues);
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoGroup> findCompetenceGroupsOfGroup(PoGroup group, PoRole role,
			Date date) {
		List<PoGroup> res = new ArrayList<PoGroup>();
		Object[] keyValues = { group, role, date, date, date, date };
		// get all roles that are directly connected through
		// PoRoleHolderPerson objects
		res = getHibernateTemplate().find(
				"select rhcg.competence4Group from PoRoleCompetenceGroup as rhcg "
						+ " join rhcg.roleHolderGroups as rhg "
						+ " where rhg.group=? " + " and rhcg.role=? "
						+ " and rhg.validfrom<=? and rhg.validto >? "
						+ " and rhcg.validfrom<=? and rhcg.validto>? ",
				keyValues);
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoPerson> findCompetencePersonsOfGroup(PoGroup group, PoRole role,
			Date date) {
		List<PoPerson> res = new ArrayList<PoPerson>();
		Object[] keyValues = { group, role, date, date, date, date };
		// get all roles that are directly connected through
		// PoRoleHolderPerson objects
		res = getHibernateTemplate().find(
				"select rhcp.competence4Person from PoRoleCompetencePerson as rhcp "
						+ " join rhcp.roleHolderGroups as rhg "
						+ " where rhg.group=? " + " and rhcp.role=? "
						+ " and rhg.validfrom<=? and rhg.validto >? "
						+ " and rhcp.validfrom<=? and rhcp.validto>? ",
				keyValues);
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoClient> findCompetenceClientsOfPerson(PoPerson person, PoRole role, Date date) {
		List<PoClient> res = new ArrayList<PoClient>();
		Object[] keyValues = { person, role, date, date, date, date };
		// get all roles that are directly connected through
		// PoRoleHolderPerson objects
		res = getHibernateTemplate().find(
				"select rhcp.competence4Client from PoRoleCompetenceClient as rhcp "
						+ " join rhcp.roleHolderPersons as rhg "
						+ " where rhg.person=? " + " and rhcp.role=? "
						+ " and rhg.validfrom<=? and rhg.validto >? "
						+ " and rhcp.validfrom<=? and rhcp.validto>? ",
						keyValues);
		return res;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoClient> findCompetenceClientsOfGroup(PoGroup group, PoRole role, Date date) {
		List<PoClient> res = new ArrayList<PoClient>();
		Object[] keyValues = { group, role, date, date, date, date };
		// get all roles that are directly connected through
		// PoRoleHolderPerson objects
		res = getHibernateTemplate().find(
				"select rhcp.competence4Client from PoRoleCompetenceClient as rhcp "
						+ " join rhcp.roleHolderGroups as rhg "
						+ " where rhg.group=? " + " and rhcp.role=? "
						+ " and rhg.validfrom<=? and rhg.validto >? "
						+ " and rhcp.validfrom<=? and rhcp.validto>? ",
						keyValues);
		return res;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoPerson> findPersonsWithRoleAndCompetence4All(PoRole role, Date date) {
		Object[] keyValues = { role, date, date, date, date, date, date };
		return getHibernateTemplate().find(
				"select p from PoRoleCompetenceAll as rh"
						+ " join rh.roleHolderPersons as rhp "
						+ " join rhp.person as p " + " where rh.role = ? "
						+ " and rhp.validfrom<=? and rhp.validto >?"
						+ " and rh.validfrom<=? and rh.validto >?"
						+ " and p.validfrom<=? and p.validto >?"
						+ " order by rhp.ranking", keyValues);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoGroup> findGroupsWithRoleAndCompetence4All(PoRole role, Date date) {
		Object[] keyValues = { role, date, date, date, date, date, date };
		return getHibernateTemplate().find(
				"select g from PoRoleCompetenceAll as rh"
						+ " join rh.roleHolderGroups as rhg "
						+ " join rhg.group as g " + " where rh.role = ? "
						+ " and rhg.validfrom<=? and rhg.validto >?"
						+ " and rh.validfrom<=? and rh.validto >?"
						+ " and g.validfrom<=? and g.validto >?"
						+ " order by rhg.ranking", keyValues);
	}

	@SuppressWarnings("unchecked")
	public List<PoAction> findActions(PoRole role, Date date) {
		Object[] keyValues = { role, date, date, date, date };
		return getHibernateTemplate().find(
				"select a from PoPermissionRole as pr "
						+ " join pr.action as a " + " where pr.role= ?"
						+ " and pr.validfrom<=? and pr.validto>?"
						+ " and a.validfrom<=? and a.validto>?", keyValues);
	}

	@Override
	public PoRole findRoleByParticipantId(String key, Date effectiveDate) {
		Object[] keyValues = { key, effectiveDate, effectiveDate };
		return (PoRole) DataAccessUtils
				.uniqueResult(getHibernateTemplate()
						.find(
								"from PoRole r "
										+ " where r.participantId = ? and r.validfrom<= ?"
										+ " and r.validto>?", keyValues));
	}

	@Override
	public PoRole findRoleByParticipantId(String key, PoClient client,
			Date effectiveDate) {
		Object[] keyValues = { client, key, effectiveDate, effectiveDate };
		return (PoRole) DataAccessUtils
				.uniqueResult(getHibernateTemplate()
						.find(
								"from PoRole r "
										+ " where r.client=? "
										+ " and r.participantId = ? and r.validfrom<= ?"
										+ " and r.validto>?", keyValues));
	}

	@Override
	public Object findPerformerOfDummyRole(PoRole role, Date date) {
		Object[] keyValues = { role, date, date, date, date, date, date, date,
				date };
		Object ret = DataAccessUtils.uniqueResult(getHibernateTemplate().find(
				"select distinct p from PoRoleHolderPerson rhp "
						+ " join rhp.roleHolder as rh" + " join rh.role as r"
						+ " join rhp.person as p" + " where r=? "
						+ " and r.validfrom<? and r.validto>?"
						+ " and rhp.validfrom<? and rhp.validto>?"
						+ " and p.validfrom<? and p.validto>?"
						+ " and rh.validfrom<? and rh.validto>?", keyValues));

		if (ret == null) // than the performer must be group!
			ret = DataAccessUtils.uniqueResult(getHibernateTemplate()
					.find(
							"select distinct g from PoRoleHolderGroup rhg "
									+ " join rhg.roleHolder as rh"
									+ " join rh.role as r"
									+ " join rhg.group as g" + " where r=? "
									+ " and r.validfrom<? and r.validto>?"
									+ " and rhg.validfrom<? and rhg.validto>?"
									+ " and g.validfrom<? and g.validto>?"
									+ " and rh.validfrom<? and rh.validto>?",
							keyValues));
		

		return ret;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRole> findRolesForClient(PoClient client, Date date) {
		Object[] kv = {client, date, date, new Integer(PoRole.NORMAL_ROLE)};
		return getHibernateTemplate().find("from PoRole r where (r.client is null or r.client=?) "
				+"and r.validfrom <=? and r.validto>? and r.roleType=? order by r.name",kv);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRole> findDirectlyLinkedRolesOfPerson(PoPerson person, Date date) {
		List<PoRole> res = new ArrayList<PoRole>();
		Object[] keyValues = { person, new Integer(PoRole.NORMAL_ROLE), date, date, date, date};
		// get all roles that are directly connected through
		// PoRoleHolderPerson objects
		res = getHibernateTemplate().find(
				"select distinct rhb.role from PoRoleCompetenceBase as rhb "
						+ " join rhb.roleHolderPersons as rhp "
						+ " where rhp.person=?  and rhb.role.roleType=?"
						+ " and rhp.validfrom<=? and rhp.validto >? "
						+ " and rhb.validfrom<=? and rhb.validto >? ",
				keyValues);
		return res;
	}


	@Override
	@SuppressWarnings("unchecked")
	public List<PoRole> findAllActiveRoles() {
		Object[] keys = {new Date(), new Date(), new Integer(PoRole.NORMAL_ROLE)};
		return getHibernateTemplate().find("from PoRole r where r.validfrom <=? and r.validto>?" +
				" and r.roleType=?", keys);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRole> findRoles(PoPerson person, Date date) {
        Object[] keyValues = { person, date, date, date, date, date, date };
        List<PoRole> l = getHibernateTemplate().find(
                "select distinct r from PoRoleHolderPerson as rhp "
                        + " join rhp.roleHolder as rh join rh.role as r "
                        + " where rhp.person = ?" 
                        + " and rhp.validfrom <= ? and rhp.validto > ?"  
                        + " and r.validfrom <= ? and r.validto > ? " 
                        + " and rh.validfrom <= ? and rh.validto > ? ", keyValues);
        return l;
    }

    @Override
	@SuppressWarnings("unchecked")
	public List<PoRole> findRoles(PoGroup group, Date date) {
		 Object[] keyValues = { group, date, date,date, date,date, date};
	     return getHibernateTemplate().find("select r from PoRoleHolderGroup as rhg "
	     			+ " join rhg.roleHolder as rh"
	     			+ " join rh.role as r "
					+ " where rhg.group = ? " 
	            + " and rhg.validfrom<=? "
	            + " and rhg.validto > ? "
					+ " and rh.validfrom<=? "
					+ " and rh.validto > ? "
					+ " and r.validfrom <= ? "
					+ " and r.validto > ?"
					, keyValues);	
	}
    
    @Override
	@SuppressWarnings("unchecked")
	public List<PoRole> findRolesF(PoGroup group, Date date) {
		 Object[] keyValues = { group, date, date,date};
	     return getHibernateTemplate().find("select distinct r from PoRoleHolderGroup as rhg "
	     			+ " join rhg.roleHolder as rh"
	     			+ " join rh.role as r "
					+ " where rhg.group = ? " 
					+ " and rhg.validto > ? "
					+ " and rh.validto > ? "
					+ " and r.validto > ?"
					, keyValues);	
	}

}
