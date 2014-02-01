package at.workflow.webdesk.po.impl.daos;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.QueryBuilderHelper;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoActionDAO;
import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAPermissionClient;
import at.workflow.webdesk.po.model.PoAPermissionGroup;
import at.workflow.webdesk.po.model.PoAPermissionPerson;
import at.workflow.webdesk.po.model.PoAPermissionRole;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.tools.HistoricizingDAOImpl;

/**
 * The PoActionDAOImpl class implements the <b>Hibernate</b> access to everything that has to do
 * with <code>PoAction</code> objects stored in the database.<br>
 * This does not mean, that only <code>PoAction</code> objects are handled, 
 * but also <code>PoAPermission-</code> 
 * <code>Base</code>,
 * <code>Client</code>,
 * <code>Group</code>,
 * <code>Role</code>,
 * <code>Person</code> objects.<br/>
 * 
 * This is only a <b>data access object</b>, more functions concerning 
 * actions are found in the classes implementing the {@see at.workflow.webdesk.po.PoActionService}
 * <br/>
 * Created on 22.06.2005<br/>
 * created at: 08.01.2007<br/>
 * @author DI Harald Entner logged in as: hentner
 */
@SuppressWarnings("unchecked")
public class PoActionDAOImpl extends HistoricizingDAOImpl<PoAction> implements PoActionDAO {

	@Override
	protected Class<PoAction> getEntityClass() {
		return PoAction.class;
	}

	@Override
	public String generateCommaList(List<String> uids, boolean isString) {
		return QueryBuilderHelper.generateCommaList(uids, isString);
	}
	
	/**
	 * performs unique checks -PoAction
	 */
	@Override
	public void checkAction(PoAction action) {
		// getHibernateTemplate().evict(action);
		Map<String,Object> uniqueAttributes = new HashMap<String,Object>();
		uniqueAttributes.put("name", action.getName());
		uniqueAttributes.put("actionType", new Integer(action.getActionType()));
		if (isAttributeSetUnique(uniqueAttributes, action.getUID(), action.getValidity()) == false)
			throw new PoRuntimeException(PoRuntimeException.MESSAGE_DUPLICATE_ACTIONTYPE_NAME + ": "+action.getName()+" / "+action.getActionType());
	}

	@Override
	protected void beforeSave(PoAction entity)	{
		checkAction(entity);
	}
	
	@Override
	public List<PoAction> loadAllCurrentActions() {
		return getHibernateTemplate()
				.find(
						"select a from PoAction a left join a.module m" +
						" where not a.detached is true and (m is null or not m.detached is true) " +
						" and a.validto>=current_timestamp() and a.validfrom<=current_timestamp() order by a.name");
	}

	/***************************************************************************
	 * find Action Permissions (Client, Group, Person, Role)
	 * 
	 **************************************************************************/

	@Override
	public List<PoAPermissionRole> findActionPermissionRoleWithActionAndRole(
			PoAPermissionRole apr, Date validFrom, Date validTo) {
		Object[] keyValues = { apr.getAction(), apr.getRole(), validFrom,
				validTo };
		List<PoAPermissionRole> l = getHibernateTemplate()
				.find(
						"from PoAPermissionRole ar"
								+ " where ar.action=? and ar.role=? "
								+ " and ar.validto>? and ar.validfrom<=? order by ar.validto",
						keyValues);
		return l;
	}

	@Override
	public List<PoAPermissionGroup> findActionPermissionGroupWithActionAndGroup(
			PoAPermissionGroup apg, Date validFrom) {

		Object[] keyValues = { apg.getAction(), apg.getGroup(), validFrom , validFrom};
		List<PoAPermissionGroup> l = getHibernateTemplate().find(
				"from PoAPermissionGroup ag"
						+ " where ag.action=? and ag.group=? "
						+ " and ag.validfrom<? and ag.validto>?", keyValues);
		return l;
	}

	@Override
	public List<PoAPermissionPerson> findActionPermissionsOfPerson(PoPerson person, Date date) {
		Object[] keyValues = { person, date, date };
		return getHibernateTemplate().find(
				"from PoAPermissionPerson as pp " + " where pp.person = ?"
						+ " and pp.validfrom <= ? " + " and pp.validto > ?",
				keyValues);
	}

	@Override
	public List<PoAPermissionPerson> findActionPermissionsOfPersonF(PoPerson person, Date date) {
		Object[] keyValues = { person, date };
		return getHibernateTemplate().find(
				"from PoAPermissionPerson as pp " + " where pp.person = ?"
						+ " and pp.validto > ?", keyValues);
	}

	@Override
	public List<PoAPermissionClient> findActionPermissionsOfClient(PoClient client, Date date) {
		Object[] keyValues = { client, date, date };
		return getHibernateTemplate()
				.find(
						"from PoAPermissionClient as pc "
								+ " where pc.client = ?"
								+ " and pc.validfrom<=? "
								+ " and pc.validto>? order by pc.action.name, pc.action.actionType asc",
						keyValues);
	}

	@Override
	public List<PoAPermissionClient> findActionPermissionsOfClientF(PoClient client, Date date) {
		Object[] keyValues = { client, date };
		return getHibernateTemplate()
				.find(
						"from PoAPermissionClient as pc where pc.client = ?"
								+ " and pc.validto > ? order by pc.action.name, pc.action.actionType asc",
						keyValues);
	}

	@Override
	public List<PoAPermissionRole> findActionPermissionsOfRole(PoRole role, Date date) {
		Object[] keyValues = { role, date, date };
		return getHibernateTemplate().find(
				"from PoAPermissionRole as pr " + " where pr.role = ?"
						+ " and pr.validfrom<=? " + " and pr.validto>?",
				keyValues);
	}

	@Override
	public List<PoAPermissionRole> findActionPermissionsOfRoleF(PoRole role, Date date) {
		Object[] keyValues = { role, date };
		return getHibernateTemplate().find(
				"from PoAPermissionRole as pr " + " where pr.role = ?"
						+ " and pr.validto > ? order by pr.action.name",
				keyValues);

	}

	/***************************************************************************
	 * find Actions of (Client, Group, Person, Role)
	 * 
	 * Consider: the following functions are not useful if you want to find all
	 * actions assigned to one of the given instances. Only directly linked
	 * actions are extracted. (Exception is findActionsOfGroup: also Parent
	 * groups are visited.)
	 * 
	 **************************************************************************/

	
	@Override
	public List<PoAction> findActionsOfPerson(PoPerson person, Date date) {
		Object[] keyValues = { person, date, date };
		List<PoAction> actions = getHibernateTemplate().find(
				"select pp.action from PoAPermissionPerson as pp "
				+ " where pp.person = ?" + " and pp.validfrom <= ? "
				+ " and pp.validto > ? and pp.negative <> true", keyValues);
		List<PoAction> negativeActions = getHibernateTemplate().find(
				"select pp.action from PoAPermissionPerson as pp "
				+ " where pp.person = ?" + " and pp.validfrom <= ? "
				+ " and pp.validto > ? and pp.negative = true", keyValues);
		actions.removeAll(negativeActions);
		return actions;
	}

	@Override
	public boolean isActionAssignedToPerson(PoPerson person, Date date,
			PoAction action) {
		Object[] keyValues = { person, action, date, date,
				new Integer(PoConstants.ACTION_TYPE_CONFIG) };
		return 0 < getHibernateTemplate()
				.find(
						"from PoAPermissionPerson as pp join pp.action as a where pp.person = ?"
								+ " and a = ? and pp.validfrom <= ? and pp.validto > ? and a.actionType=?",
						keyValues).size();

	}

	@Override
	public boolean isActionAssignedToRole(PoRole role, PoAction action,
			Date date) {
		Object[] keyValues = { role, action, date, date };
		return 0 < getHibernateTemplate().find(
				"from PoAPermissionRole as pr where pr.role = ? and pr.action=?"
						+ " and pr.validfrom<=? " + " and pr.validto>?",
				keyValues).size();
	}

	@Override
	public boolean isActionAssignedToClient(PoClient client, PoAction action,
			Date date) {
		Object[] keyValues = { client, action, date, date };
		return 0 < getHibernateTemplate().find(
				"from PoAPermissionClient as pc where pc.client = ?"
						+ " and pc.action =? and pc.validfrom<=? "
						+ " and pc.validto>?", keyValues).size();
	}

	@Override
	public List<PoAction> findDirectlyLinkedActions(PoGroup group, Date date) {
		Object[] keyValues = { group, date, date };
		return getHibernateTemplate().find(
				"select pg.action from PoAPermissionGroup as pg "
						+ " where pg.group = ?" + " and pg.validfrom<=? "
						+ " and pg.validto>?", keyValues);
	}

	@Override
	public boolean isDirectlyLinkedActionConfigs(PoGroup group,
			PoAction action, Date date) {
		Object[] keyValues = { group, action, date, date };
		return 0 < getHibernateTemplate()
				.find(
						"select pg.action from PoAPermissionGroup as pg join pg.action as a where pg.group = ?"
								+ " and pg.action=? and pg.validfrom<=? and pg.validto>?",
						keyValues).size();
	}

	@Override
	public boolean isDirectlyLinkedActionConfigParent(PoGroup group,
			PoAction action, Date date) {
		Object[] keyValues = { group, action, new Boolean(true), date, date };
		return 0 < getHibernateTemplate()
				.find(
						"select pg.action from PoAPermissionGroup as pg join pg.action as a where pg.group = ?"
								+ " and pg.action=? and pg.inheritToChilds=? and pg.validfrom<=? and pg.validto>?",
						keyValues).size();
	}

	@Override
	public List<PoAPermissionGroup> findDirectlyLinkedActionConfigPermissions(PoGroup group,
			PoAction action, Date date) {
		Object[] keyValues = { group, action, date, date,
				new Integer(PoConstants.ACTION_TYPE_CONFIG) };
		return getHibernateTemplate()
				.find(
						"select pg from PoAPermissionGroup as pg join pg.action as a join a.childs as c where pg.group = ?"
								+ " and c=? and and pg.validfrom<=? and pg.validto>? and a.actionType=?",
						keyValues);
	}

	@Override
	public List<PoAPermissionGroup> findDirectlyLinkedActionPermissions(PoGroup group, Date date) {
		Object[] keyValues = { group, date, date };
		return getHibernateTemplate().find(
				"from PoAPermissionGroup as pg " + " where pg.group = ?"
						+ " and pg.validfrom<=? " + " and pg.validto>?",
				keyValues);
	}

	@Override
	public List<PoAPermissionGroup> findDirectlyLinkedActionPermissionsF(PoGroup group, Date date) {
		Object[] keyValues = { group, date };
		return getHibernateTemplate().find(
				"from PoAPermissionGroup as pg " + " where pg.group = ?"
						+ " and pg.validto>?", keyValues);
	}

	@Override
	public List<PoAction> findActionsOfRole(PoRole role, Date date) {
		Object[] keyValues = { role, date, date };
		List<PoAction> actions = getHibernateTemplate().find(
				"select pr.action from PoAPermissionRole as pr "
						+ " where pr.role = ?" + " and pr.validfrom<=? "
						+ " and pr.validto>? and negative <> true", keyValues);
		List<PoAction> negativeActions = getHibernateTemplate().find(
				"select pr.action from PoAPermissionRole as pr "
				+ " where pr.role = ?" + " and pr.validfrom<=? "
				+ " and pr.validto>? and negative = true", keyValues);
		actions.removeAll(negativeActions);
		return actions;
	}

	@Override
	public List<PoAction> findActionsUniversallyAllowed() {
		return getHibernateTemplate().find(
				"from PoAction where universallyAllowed=true");
	}

	@Override
	public PoAPermissionBase getAPermission(String uid) {
		return (PoAPermissionBase) getHibernateTemplate().load(
				PoAPermissionBase.class, uid);
	}

	@Override
	public PoAPermissionRole getAPermissionRole(String uid) {
		return (PoAPermissionRole) getHibernateTemplate().load(
				PoAPermissionRole.class, uid);
	}
	
	@Override
	public void deleteAPermission(PoAPermissionBase actionPermission) {
		getHibernateTemplate().delete(actionPermission);
	}

	@Override
	public List<PoAction> findConfigsFromAction(PoAction action) {
		Object[] keyValues = { action, new Date(), new Date() };
		return getHibernateTemplate()
				.find(
						"from PoAction a where a.parent=? and a.validfrom<=? and a.validto>? order by a.ranking",
						keyValues);
	}
	
	

	
	@Override
	public List<PoAPermissionBase> findAllPermissionsForActionF(PoAction action, Date date) {
		Object[] keyValues = { action, date };
		List<PoAPermissionBase> l = getHibernateTemplate().find(
				"from PoAPermissionBase apb where "
						+ "apb.action = ? and apb.validto>=?", keyValues);
		return l;
	}


	@Override
	public PoAction findActionByNameAndType(String name, int type) {
		Object[] keyValues = { name, new Integer(type), new Date() };
		PoAction action = (PoAction) DataAccessUtils
				.uniqueResult(getHibernateTemplate()
						.find(
								"from PoAction a where a.name=? and a.actionType=? and a.validto>?",
								keyValues));
		return action;
	}

	@Override
	public List<PoAPermissionRole> findAPRoleWithActionAndPerson(PoPerson person, Date date,
			PoAction action) {
		
		Object[] keyValues = { person, date, date,
				new Integer(PoRole.NORMAL_ROLE), action, date, date};
		Object[] keyValues2 = { date, date,
				new Integer(PoRole.NORMAL_ROLE), action, date, date};
		List<PoAPermissionRole> res = getHibernateTemplate()
				.find(
						"select apr from PoAPermissionRole as apr "
								+ " join apr.role as r "
								+ " join r.roleHolders as rh "
								+ " join rh.roleHolderPersons as rhp "
								+ " where rhp.person=?  and rhp.validfrom<=? "
								+ " and rhp.validto >?  and r.roleType=? and apr.action=? and "
								+ "apr.validfrom<=? and apr.validto>?",
						keyValues);
		
		
		res.addAll(getHibernateTemplate()
				.find(
						"select apr from PoAPermissionRole as apr "
								+ " join apr.role as r "
								+ " join r.roleHolders as rh "
								+ " join rh.roleHolderDynamics as rhd "
								+ " where rhd.validfrom<=? and rhd.validto >?  " + 
								" and r.roleType=? and apr.action=? and "
								+ "apr.validfrom<=? and apr.validto>?",
						keyValues2)
		);
		return res;
		
	}

	@Override
	public List<PoAPermissionRole> findAPRoleWithActionAndGroup(PoGroup group, Date date,
			PoAction action) {
		Object[] keyValues = { group, date, date,
				new Integer(PoRole.NORMAL_ROLE), action, date, date };
		return getHibernateTemplate()
				.find(
						"select apr from PoAPermissionRole as apr "
								+ " join apr.role as r "
								+ " join r.roleHolders as rh "
								+ " join rh.roleHolderGroups as rhg "
								+ " where rhg.group=?  and rhg.validfrom<=? "
								+ " and rhg.validto >?  and r.roleType=? and apr.action=? and "
								+ "apr.validfrom<=? and apr.validto>?",
						keyValues);
	}

	@Override
	public void saveAPermission(PoAPermissionBase permission) {
		getHibernateTemplate().saveOrUpdate(permission);
	}

	@Override
	public List<PoAction> findActionByProcessDefId(String procDefId) {
		Object[] keyValues = { procDefId };
		return getHibernateTemplate().find(
				"from PoAction as a " + " where a.processDefId = ?"
						+ " and a.validfrom <= current_timestamp() "
						+ " and a.validto > current_timestamp()", keyValues);
	}

	@Override
	public List<PoAction> findAllActions(Date date) {
		Object[] keys = { date, date };
		return getHibernateTemplate().find(
				"select a from PoAction a left join a.module m" +
				" where not a.detached is true and (m is null or not m.detached is true) " +
				" and a.validto>=? and a.validfrom<=? order by a.name",keys);
	}

	@Override
	public List<PoAction> findAllUniversallyAllowedActions(Date date) {
		Object[] keys = { date };
		return getHibernateTemplate().find("from PoAction a " +
				"where universallyAllowed = true and a.validto>?",
				keys);
	}
	
	@Override
	public List<String> findActionNamesOfModule(String module) {
		Object[] keys = { module, new Integer(PoConstants.ACTION_TYPE_ACTION) };
		return getHibernateTemplate().find(
				"select a.name from PoAction a where "
						+ "a.actionFolder=? and a.actionType=?", keys);
	}

	@Override
	public List<PoAction> findConfigs() {
		Object[] keys = { new Integer(PoConstants.ACTION_TYPE_CONFIG) };
		return getHibernateTemplate().find("from PoAction where  actionType=?",
				keys);
	}

	@Override
	public List<PoAction> findAllCurrentConfigs() {
		Object[] keys = { new Integer(PoConstants.ACTION_TYPE_CONFIG) };
		
		return getHibernateTemplate().find(
				"select a from PoAction a left join a.module m " 
						+ " where a.actionType=? "
						+ " and not a.detached is true and (m is null or not m.detached is true) "
						+ " and a.validto>=current_timestamp and "
						+ " a.validfrom<=current_timestamp order by a.name", keys);
	}

	@Override
	public List<PoAPermissionClient> findActionPermissionClientWithActionAndClientF(PoAction action,
			PoClient client, Date date) {
		Object[] keyValues = { action, client, date };
		return getHibernateTemplate().find(
				"from PoAPermissionClient ac"
						+ " where ac.action=? and ac.client=? "
						+ " and ac.validto>=?", keyValues);

	}

	@Override
	public List<PoAPermissionPerson> findActionPermissionPersonWithActionAndPersonF(PoAction action,
			PoPerson person, Date date) {
		Object[] keyValues = { action, person, date };
		List<PoAPermissionPerson> l = getHibernateTemplate().find(
				"from PoAPermissionPerson ap"
						+ " where ap.action=? and ap.person=? "
						+ " and ap.validto>?", keyValues);
		return l;

	}

	@Override
	public List<PoAPermissionClient> findActionPermissionClientWithActionAndClient(PoAction action,
			PoClient client, Date date) {
		Object[] keyValues = { action, client, date, date };
		List<PoAPermissionClient> l = getHibernateTemplate()
				.find(
						"from PoAPermissionClient apc where "
								+ " apc.action=? and apc.client=? and apc.validfrom<=? and apc.validto>?",
						keyValues);
		return l;
	}

	@Override
	public List<PoAPermissionPerson> findActionPermissionPersonWithActionAndPerson(PoAction action,
			PoPerson person, Date date) {
		Object[] keyValues = { action, person, date, date };
		List<PoAPermissionPerson> l = getHibernateTemplate()
				.find(
						"from PoAPermissionPerson app where "
								+ " app.action=? and app.person = ? and app.validfrom<=? and app.validto>?",
						keyValues);
		return l;
	}

	@Override
	public List<PoAPermissionGroup> findActionPermissionGroupWithActionAndGroup(PoAction action,
			PoGroup group, Date date) {
		Object[] keyValues = { action, group, date, date };
		List<PoAPermissionGroup> l = getHibernateTemplate()
				.find(
						"from PoAPermissionGroup apg where "
								+ " apg.action=? and apg.group= ? and apg.validfrom<=? and apg.validto>?",
						keyValues);
		return l;
	}

	@Override
	public List<PoAPermissionGroup> findActionPermissionGroupWithActionAndGroupAndInheritToChilds(
			PoAction action, PoGroup group, Date date, boolean inheritToChilds) {
		Object[] keyValues = { action, group, new Boolean(inheritToChilds), date, date,
				 };
		List<PoAPermissionGroup> l = getHibernateTemplate()
				.find(
						"from PoAPermissionGroup apg where "
								+ " apg.action=? and apg.group= ? and apg.inheritToChilds=? and apg.validfrom<=? and apg.validto>?",
						keyValues);
		return l;
	}

	@Override
	public List<PoAPermissionRole> findActionPermissionRoleWithActionAndGroup(PoAction action,
			PoGroup group, Date date) {
		Object[] keyValues = { action, group, date, date };
		List<PoAPermissionRole> l = getHibernateTemplate()
				.find(
						"select distinct apr from PoAPermissionRole apr join apr.role r join r.roleHolders rh join rh.roleHolderGroups as rhg where"
								+ " apr.action=? and rhg.group= ? and apr.validfrom<=? and apr.validto>?",
						keyValues);
		return l;

	}

	@Override
	public List<PoAPermissionRole> findActionPermissionRoleWithActionAndPersonAndRoleType(
			PoAction action, PoPerson person, Date date, int roleType) {
		Object[] keyValues = { action, date, date, person, date, date,
				new Integer(roleType) };
		List<PoAPermissionRole> persons = getHibernateTemplate()
				.find(
						"select apr from PoAPermissionRole apr "
								+ " where apr.action = ? and apr.role.validfrom <=? and apr.role.validto>? and "
								+ "apr.role.UID in (select r.UID from PoRoleHolderPerson rhp join rhp.roleHolder.role r where "
								+ " rhp.person = ? and rhp.validfrom<=? and rhp.validto>? and r.roleType=?)",
						keyValues);
		return persons;
	}

	@Override
	public List<PoAPermissionRole> findActionPermissionRoleWithActionAndGroupAndRoleType(
			PoAction action, PoGroup hierarchicalGroup, Date date, int roleType) {
		Object[] keyValues = { action, date, date, hierarchicalGroup, date,
				date, new Integer(roleType) };
		List<PoAPermissionRole> groups = getHibernateTemplate()
				.find(
						"select apr from PoAPermissionRole apr "
								+ " where apr.action = ? and apr.role.validfrom <=? and apr.role.validto>? and "
								+ "apr.role.UID in (select r.UID from PoRoleHolderGroup rhg join rhg.roleHolder.role r where "
								+ " rhg.group = ? and rhg.validfrom<=? and rhg.validto>? and r.roleType=?)",
						keyValues);
		return groups;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List findByQuery(String query) {
		return getHibernateTemplate().find(query);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List findByQueryAndNamedParameters(String query, String[] paramNames, Object[] values) {
		return getHibernateTemplate().findByNamedParam(query, paramNames, values);
	}
	
	
	
	/* NOT USED FUNCTIONS _ MAYBE KICKOUT 
	 * 
	 * -> This functions should move to 
	 * OrgDAO when neccessary
	 * 
	 * 
	 * */

	@Override
	public List<PoGroup> findGroupsFromAction(PoAction action, Date date) {
		Object[] keyValues = { action, date, date, date, date };
		List<PoGroup> groups = getHibernateTemplate().find(
				"select pg.group from PoPermissionGroup as pg "
						+ " join pg.group as g " + " where pg.action = ?"
						+ " and pg.validfrom<=? " + " and pg.validto>?"
						+ " and g.validfrom <=? " + " and g.validto >? "
						+ " and negative <> true",
				keyValues);
		List<PoGroup> negativeGroups = getHibernateTemplate().find(
				"select pg.group from PoPermissionGroup as pg "
				+ " join pg.group as g " + " where pg.action = ?"
				+ " and pg.validfrom<=? " + " and pg.validto>?"
				+ " and g.validfrom <=? " + " and g.validto >? "
				+ " and negative = true",
				keyValues);
		groups.removeAll(negativeGroups);
		return groups;
	}

	@Override
	public List<PoPerson> findPersonsFromAction(PoAction action, Date date) {
		Object[] keyValues = { action, date, date, date, date };
		return getHibernateTemplate().find(
				"select pp.person from PoPermissionPerson as pp "
						+ " join pp.person as p " + " where pp.action = ?"
						+ " pp.validfrom<=? " + " and pp.validto>? "
						+ " and p.validfrom<=? " + " and p.validto > ? ",
				keyValues);

	}

	@Override
	public List<PoRole> findRolesFromAction(PoAction action, Date date) {
		Object[] keyValues = { action, date, date, date, date };
		return getHibernateTemplate().find(
				"select r from PoPermissionRole as pr" + " join pr.role as r "
						+ " join pr.Action as a " + " where pr.action = ?"
						+ " pr.validfrom<=? " + " and pr.validto>? "
						+ " and r.validfrom <=?" + " and r.validto>? ",
				keyValues);
	}

	@Override
	public List<String> findInUseProcessDefinitions() {
		Object[] keys = {new Integer(PoConstants.ACTION_TYPE_PROCESS), new Date(), new Date()};
		return getHibernateTemplate().find("select distinct processDefId from " +
				"PoAction where processDefId is not null and actionType=? " + 
				" and validfrom<=? and validto>?", keys);
	}

	@Override
	public List<PoAction> findActionsThatAllowAction(PoAction action, Date date) {
		Object[] keys = {action, date, date};
		return getHibernateTemplate().find("from PoAction where " +
				" allowsAction=? and validfrom<=? and validto>?" , keys);
	}

	@Override
	public List<PoAction> findAllActionsOfType(Date date, int actionType) {
		Object[] keys = { actionType, date, date };
		return getHibernateTemplate().find(
				"select a from PoAction a left join a.module m" +
				" where a.actionType=? and not a.detached is true and (m is null or not m.detached is true) " +
				" and a.validto>=? and a.validfrom<=? order by a.name",keys);
	}

}
