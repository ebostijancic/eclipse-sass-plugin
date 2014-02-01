package at.workflow.webdesk.po.impl.daos;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.QueryBuilderHelper;
import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoPersonDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.HistoricizingDAOImpl;

/**
 * Created on 05.01.2005<br/>
 * Refactored at: 08.06.2007<br/>
 * 
 * @author DI Harald Entner, logged in as: hentner
 */
public class PoPersonDAOImpl extends HistoricizingDAOImpl<PoPerson> implements PoPersonDAO {

	@Override
	protected Class<PoPerson> getEntityClass() {
		return PoPerson.class;
	}

	/**
	 * Database Help function. Due to the historization of various object, this
	 * can not be done with the aid of the database, thus it is controlled every
	 * time when save is called.
	 */
	@Override
	public void checkUser(PoPerson person) {
//		if (person.getUID() != null)
//			getHibernateTemplate().evict(person);
			// fri_2013-07-31: this has been here since 2008-09-24 without comment why this must happen here
		// sdz-18-12-2013 is no more necessary - only count of conflicting objects is read from db

		if (person.getValidto().getTime() > System.currentTimeMillis()) {	// check uniqueness only for persons that are not expired 
			if (isAttributeUnique("userName", person.getUserName(), person.getUID(), person.getValidity()) == false)
				throw new PoRuntimeException(PoRuntimeException.MESSAGE_DUPLICATE_USERNAME + ": " + person.getUserName());

			if (person.getTaID() != null)
				if (isAttributeUnique("taID", person.getTaID(), person.getUID(), person.getValidity()) == false)
					throw new PoRuntimeException(PoRuntimeException.MESSAGE_DUPLICATE_TAID + ": " + person.getTaID());

			if (person.getEmployeeId() != null)
				if (isAttributeUnique("employeeId", person.getEmployeeId(), person.getUID(), person.getValidity()) == false)
					throw new PoRuntimeException(PoRuntimeException.MESSAGE_DUPLICATE_EMPLOYEE_ID + ": " + person.getEmployeeId());
			
			// prevents persistence of person with images of other person
			if (person.getPersonImages() != null)
				if (isAttributeUnique("personImages", person.getPersonImages(), person.getUID(), person.getValidity()) == false)
					throw new PoRuntimeException(PoRuntimeException.MESSAGE_DUPLICATE_PERSON_IMAGES);
		}
	}

	@Override
	public PoPersonGroup getPersonGroup(String uid) {
		if (uid != null)
			return (PoPersonGroup) getHibernateTemplate().get(
					PoPersonGroup.class, uid);
		else
			return null;
	}

	/**
	 * returns the amount of groups that the person is assigned to and that are
	 * valid after validat
	 * and ends before validat
	 */
	@Override
	public List getPersonGroups(PoPerson person, PoOrgStructure orgStructure,
			Date validAt) {
		Object[] keyValues = { person, orgStructure, validAt, validAt, validAt,
				validAt };

		List res = getHibernateTemplate().find(
				"select pg from PoPersonGroup as pg " + " join pg.group as g "
						+ " where pg.person = ?" + " and g.orgStructure = ? "
						+ " and pg.validfrom<=? " + " and pg.validto>? "
						+ " and g.validfrom<=? " + " and g.validto > ? "
						+ " order by pg.validfrom", keyValues);
		return res;

	}

	@Override
	public PoPersonGroup getPersonGroupLink(PoPerson person, PoGroup group,
			Date validAt) {
		Object[] keyValues = { person, group, validAt, validAt };

		return (PoPersonGroup) DataAccessUtils
				.uniqueResult(getHibernateTemplate().find(
						"select pg from PoPersonGroup as pg"
								+ " where pg.person = ? " + " and pg.group = ?"
								+ " and pg.validfrom<=? "
								+ " and pg.validto>? ", keyValues));

	}

	@Override
	public List getFutureGroups(PoPerson person, PoOrgStructure orgStructure,
			Date validFrom) {
		Object[] keyValues = { person, orgStructure, validFrom };
		return getHibernateTemplate().find(
				"select pg from PoPersonGroup as pg " + " join pg.group as g "
						+ " where pg.person = ?" + " and g.orgStructure = ? "
						+ " and pg.validfrom >? " // neu eingefügt: 20.7 /
													// wegen Spezialfall
						+ " order by pg.validfrom", keyValues);
	}

	/**
	 * @see at.workflow.webdesk.po.PoPersonDAO#savePerson(at.workflow.webdesk.po.model.PoPerson)
	 */
	@Override
	public void save(PoPerson person, PoGroup group) {
		checkUser(person);
		getHibernateTemplate().save(person);
	}

	@Override
	public void update(PoPerson person) {
		checkUser(person);
		getHibernateTemplate().update(person);
	}

	/**
	 * @see at.workflow.webdesk.po.PoPersonDAO#findPersonByTaId(java.lang.String)
	 */
	@Override
	public PoPerson findPersonByTaId(String key, Date date) {
		Object[] keys = { key, date, date };
		PoPerson myPerson = (PoPerson) DataAccessUtils
				.uniqueResult(getHibernateTemplate()
						.find(
								"from at.workflow.webdesk.po.model.PoPerson p"
										+ " where p.taID = ? and p.validfrom<=? and p.validto>?",
								keys));
		return (myPerson);
	}

	/**
	 * @see at.workflow.webdesk.po.PoPersonDAO#findPersonByTaId(java.lang.String, java.util.Date, java.util.Date)
	 */
	@Override
	public PoPerson findPersonByTaId(String key, Date from, Date to) {
		Object[] keys = { key, to, from };
		List<PoPerson> persons = getHibernateTemplate()
				.find(
						"from at.workflow.webdesk.po.model.PoPerson p"
						+ " where p.taID = ? and p.validfrom<=? and p.validto>?"
						+ " order by p.validto desc",
						keys);
		PoPerson myPerson = persons == null || persons.size() < 1 ? null : persons.get(0);
		return (myPerson);
	}
	
	/**
	 * @see at.workflow.webdesk.po.PoPersonDAO#findPersonByEmployeeId(java.lang.String)
	 */
	@Override
	public PoPerson findPersonByEmployeeId(String key, Date date) {
		Object[] keys = { key, date, date };
		return (PoPerson) DataAccessUtils.uniqueResult(getHibernateTemplate()
				.find(
						"from at.workflow.webdesk.po.model.PoPerson p"
								+ " where p.employeeId = ? and p.validfrom<=? and p.validto>?", keys));
	}

	/**
	 * @see at.workflow.webdesk.po.PoPersonDAO#isPersonActualMemberOfGroup(at.workflow.webdesk.po.model.PoPerson,
	 *      at.workflow.webdesk.po.model.PoGroup)
	 */
	@Override
	public boolean isPersonActualMemberOfGroup(PoPerson person, PoGroup group) {
		Date now = new java.util.Date();
		Object[] keyValues = { person, group, now, now };

		PoPersonGroup pgLink = (PoPersonGroup) DataAccessUtils
				.uniqueResult(getHibernateTemplate()
						.find(
								"from PoPersonGroup pg where "
										+ " pg.person=? and pg.group=? and pg.validfrom<=? and pg.validto>? ",
								keyValues));
		if (pgLink == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void deletePersonGroupLink(PoPersonGroup personGroup) {
		getHibernateTemplate().delete(personGroup);
	}

	@Override
	public List findPersonsLinkedGroups(PoPerson person, int orgStructureType,
			Date effectiveDate) {
		List l = null;
		if (orgStructureType != -1) {
			Object[] keyValues = { person, new Integer(orgStructureType),
					effectiveDate, effectiveDate, effectiveDate, effectiveDate };
			l = (getHibernateTemplate()
					.find("select g from PoPersonGroup as pg "
							+ " join pg.group as g join g.orgStructure as os"
							+ " where pg.person = ?"
							+ " and (os.orgType=? or os.orgType=-1) "
							+ " and pg.validfrom<=? and pg.validto>? "
							+ " and g.validfrom<=? and g.validto>?  order by os.orgType, g.shortName", keyValues));
		} else {
			Object[] keyValues = { person, effectiveDate, effectiveDate,
					effectiveDate, effectiveDate };
			l = (getHibernateTemplate()
					.find("select pg.group from PoPersonGroup as pg "
							+ " where pg.person = ?"
							+ " and pg.validfrom<=? and pg.validto>? "
							+ " and pg.group.validfrom<=? and pg.group.validto>? "
							+ " order by pg.group.orgStructure.orgType, pg.group.shortName", keyValues));
		}
		return l;
	}

	@Override
	public List findPersonGroups(PoPerson person, Date date, int poOrgStructure) {
		if (poOrgStructure > 0) {

			Object[] keyValues = { person, date, date, date, date,
					new Integer(poOrgStructure) };
			return getHibernateTemplate().find(
					"select pg from PoPersonGroup as pg "
							+ " join pg.group as g"
							+ " join g.orgStructure as os"
							+ " where pg.person = ?" + " and pg.validfrom<=? "
							+ " and pg.validto>? " + " and g.validfrom <=? "
							+ " and g.validto >? " + " and os.orgType=?"
							+ " order by pg.validfrom", keyValues);
		} else {
			Object[] keyValues = { person, date, date, date, date };
			return getHibernateTemplate().find(
					"select pg from PoPersonGroup as pg "
							+ " join pg.group as g " + " where pg.person = ?"
							+ " and pg.validfrom<=? " + " and pg.validto>? "
							+ " and g.validfrom <=? " + " and g.validto > ? ",
					keyValues);
		}
	}

	@Override
	public List findPersonGroupsF(PoPerson person, Date date, int orgType) {
		if (orgType > 0) {

			Object[] keyValues = { person, date, date, new Integer(orgType) };
			return getHibernateTemplate().find(
					"select pg from PoPersonGroup as pg "
							+ " join pg.group as g"
							+ " join g.orgStructure as os"
							+ " where pg.person = ?" + " and pg.validto>? "
							+ " and g.validto>? "
							+ " and os.orgType=? order by pg.person.lastName",
					keyValues);
		} else {
			Object[] keyValues = { person, date, date };
			return getHibernateTemplate().find(
					"select pg from PoPersonGroup as pg "
							+ " join pg.group as g " + " where pg.person = ?"
							+ " and pg.validto>? "
							+ " and g.validto>? order by pg.person.lastName",
					keyValues);
		}
	}

	@Override
	public List<PoPersonGroup> findPersonGroups(PoPerson person, Date date,
			PoOrgStructure orgStructure) {

		Object[] keyValues = { person, date, date, date, date, orgStructure };
		return getHibernateTemplate().find(
				"select pg from PoPersonGroup as pg " + " join pg.group as g"
						+ " join g.orgStructure as os" + " where pg.person = ?"
						+ " and pg.validfrom <= ?" + " and pg.validto>? "
						+ " and g.validfrom <=?" + " and g.validto >? "
						+ " and os=?", keyValues);
	}

	@Override
	public List<PoPersonGroup> findPersonGroupsF(PoPerson person, Date date,
			PoOrgStructure poOrgStructure) {

		Object[] keyValues = { person, date, date, poOrgStructure };
		return getHibernateTemplate().find(
				"select pg from PoPersonGroup as pg " + " join pg.group as g"
						+ " join g.orgStructure as os" + " where pg.person = ?"
						+ " and pg.validto>? " + " and g.validto > ? "
						+ " and os=? order by pg.person.lastName asc", keyValues);
	}

	@Override
	public List<PoPersonGroup> findPersonGroupsAll(PoPerson person,
			PoOrgStructure poOrgStructure) {

		Object[] keyValues = { person, poOrgStructure };
		return getHibernateTemplate().find(
				"select pg from PoPersonGroup as pg " + " join pg.group as g"
						+ " join g.orgStructure as os" + " where pg.person = ?"
						+ " and os=? order by pg.validto desc", keyValues);
	}

	@Override
	public List<PoPersonGroup> findPersonGroupsAll(PoPerson person) {
		
		Object[] keyValues = { person };
		return getHibernateTemplate().find(
				"select pg from PoPersonGroup as pg "
						+ " where pg.person = ?"
						+ " order by pg.validto desc", keyValues);
	}
	
	@Override
	public List findActions(PoPerson person, Date date) {
		Object[] keyValues = { person, date, date };
		return getHibernateTemplate().find(
				"select pp.action from PoPermissionPerson as pp "
						+ " where pp.person = ?" + " and pp.validfrom<=? "
						+ " and pp.validto>?", keyValues);
	}

	@Override
	public List<PoPerson> findAllPersons(Date date) {
		return getHibernateTemplate().find("from PoPerson where validfrom<=? and " +
				" validto>? order by lastname, firstname", new Object[] { date, date });
	}

	@Override
	public List<PoPerson> findAllPersons(Date date, boolean activeUser) {
		return getHibernateTemplate().find("from PoPerson where validfrom<=? and " +
				" validto>? and activeUser = ? order by lastname, firstname",
				new Object[] { date, date, activeUser });
	}

	@Override
	public PoPersonGroup findPersonGroupObject(PoPerson person, PoGroup group, Date effectiveDate) {
		Date now = new java.util.Date();
		Object[] keyValues = { person, group, now, now };
		return (PoPersonGroup) DataAccessUtils.uniqueResult(getHibernateTemplate().find(
				"from PoPersonGroup pgl where pgl.person=? and pgl.group=?"
						+ "  and pgl.validfrom<=? and pgl.validto>? ",
				keyValues));

	}

	@Override
	public PoPersonGroup getGroupAfter(PoPersonGroup personGroup) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(personGroup.getValidto());
		Object[] keyValues = {
				personGroup.getPerson(),
				new Integer(personGroup.getGroup().getOrgStructure()
						.getOrgType()), gc.getTime(), gc.getTime() };

		return (PoPersonGroup) DataAccessUtils
				.uniqueResult(getHibernateTemplate()
						.find(
								"select pg from PoPersonGroup as pg "
										+ " join pg.group as g join g.orgStructure as os"
										+ " where pg.person = ?"
										+ " and os.orgType=? "
										+ " and pg.validfrom<=? "
										+ " and pg.validto>? ", keyValues));
	}

	@Override
	public PoPersonGroup getGroupBefore(PoPersonGroup personGroup) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(personGroup.getValidfrom());
		gc.add(Calendar.SECOND, -1);
		Object[] keyValues = {
				personGroup.getPerson(),
				new Integer(personGroup.getGroup().getOrgStructure()
						.getOrgType()), gc.getTime(), gc.getTime(),
				gc.getTime(), gc.getTime() };

		return (PoPersonGroup) DataAccessUtils
				.uniqueResult(getHibernateTemplate()
						.find(
								"select pg from PoPersonGroup as pg "
										+ " join pg.group as g join g.orgStructure as os"
										+ " where pg.person = ?"
										+ " and os.orgType=? "
										+ " and pg.validfrom<=? "
										+ " and pg.validto>? "
										+ " and g.validfrom <= ? "
										+ " and g.validto > ? ", keyValues));
	}

	@Override
	public PoOrgStructure getOrgStructure(PoPerson person) {
		Object[] keyValues = { person,
				new Integer(PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY),
				new Date(), new Date() };

		PoOrgStructure os = (PoOrgStructure) DataAccessUtils
				.uniqueResult(getHibernateTemplate()
						.find(
								"select os from"
										+ " PoOrgStructure  os "
										+ " join os.groups as g join g.personGroups as pg "
										+ " where pg.person = ? "
										+ " and os.orgType = ? "
										+ " and pg.validfrom<=?"
										+ " and pg.validto> ?", keyValues));
		return os;
	}

	@Override
	public List findPersonsOfClientF(PoClient client, Date date) {
		Object[] keyValues = { client, date };
		return getHibernateTemplate().find(
				"select p from PoPerson as p " + " join p.client as c "
						+ " where c=? and p.validto>? order by p.lastName",
				keyValues);
	}

	/**
	 * @see at.workflow.webdesk.po.PoPersonDAO#findPersonByUserName(java.lang.String)
	 */
	@Override
	public PoPerson findPersonByUserName(String key) {
		Date now = new Date();
		Object[] keyValues = { key, now, now };
		PoPerson myPerson = (PoPerson) DataAccessUtils.uniqueResult(
				getHibernateTemplate().find(
						"select p from PoPerson as p where p.userName = ? and p.validfrom <= ? and p.validto > ?",
						keyValues));
		return myPerson;
	}

	@Override
	public PoPerson findPersonByEmail(String email) {
		Date now = new Date();
		Object[] keyValues = { email, now, now };
		return (PoPerson) DataAccessUtils.uniqueResult(
				getHibernateTemplate().find(
						"from PoPerson p where p.email = ? and p.validfrom <= ? and p.validto > ?",
						keyValues));
	}

	/**
	 * @param workflowId
	 * @return PoPerson (the Person with ghe given workflowId)
	 */
	@Override
	public PoPerson findPersonByWorkflowId(String workflowId) {
		return this.get(workflowId);
	}

	@Override
	public List<PoPerson> findPersonsOfClient(PoClient client, Date validAt) {
		Object[] keyValues = { client, validAt, validAt };
		return getHibernateTemplate()
				.find(
						"select p from PoPerson as p "
								+ " join p.client as c "
								+ " where c=? and p.validfrom<=? and p.validto>? order by p.lastName",
						keyValues);
	}

	@Override
	public List<PoPerson> findPersonsOfClient(PoClient client, Date from, Date to) {
		Object[] keyValues = { client, to, from };
		return getHibernateTemplate()
		.find(
				"select p from PoPerson as p "
				+ " join p.client as c "
				+ " where c=? and p.validfrom<=? and p.validto>? order by p.lastName",
				keyValues);
	}
	
	@Override
	public void savePersonGroup(PoPersonGroup personGroup) {
		this.getHibernateTemplate().saveOrUpdate(personGroup);
	}

	@Override
	public PoGroup getPersonsHierarchicalGroup(PoPerson person, Date date) {
		if (logger.isDebugEnabled())
			this.logger.debug("try to get persons hierarchicalGroup for Person= " + person);
		return (PoGroup) DataAccessUtils.uniqueResult(findPersonsLinkedGroups(person,
				PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY, date));
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> findPersonUidsOfClient(String uidOfClient, Date date) {
		Object[] keyValues = { uidOfClient, date, date };
		return getHibernateTemplate()
				.find(
						"select p.UID from PoPerson as p "
								+ " join p.client as c "
								+ " where c.UID=? and p.validfrom<=? and p.validto>? order by p.lastName",
						keyValues);
	}

	@Override
	public List<PoPerson> findPersonsWithFilter(List<String> uids, List<String> names, Date date) {
		Object[] keys = { date, date };
		String addSql = getNamesQueryString(names, null);
		String uidString = QueryBuilderHelper.generateCommaList(uids, true);

		String sql = "from PoPerson where validFrom<=? and validto>? and UID in (" + uidString + ")";
		if (!addSql.equals(""))
			sql += "and (" + addSql + ")";

		return getHibernateTemplate().find(sql, keys);
	}

	@Override
	public List<PoPerson> findPersonsWithFilter(List<String> uids, List<String> names, Date from, Date to) {
		Object[] keys = { to, from };
		String addSql = getNamesQueryString(names, null);
		String uidString = QueryBuilderHelper.generateCommaList(uids, true);

		String sql = "from PoPerson where validFrom <= ? and validto >= ? and UID in (" + uidString + ")";
		if (!addSql.equals(""))
			sql += "and (" + addSql + ")";

		return getHibernateTemplate().find(sql, keys);
	}

	@Override
	public List<PoPerson> findPersonsWithViewPermission(Map viewPermissions, List names, Date date) {
		String addSql = getNamesQueryString(names, "p");
		Object[] keys = { date, date, date, date };
		String query = "select distinct p from PoPersonGroup as pg join pg.person as p where " +
				" p.validfrom <=? and p.validto >? and pg.validfrom<=? and pg.validto>? and " +
					(names.size() > 0 ? "(" + addSql + ") and " : "") +
					" (p.UID in (" + QueryBuilderHelper.generateCommaList((List) viewPermissions.get(PoActionPermissionService.PERSONS), true) + " ) " +
					" or pg.group.UID in (" + QueryBuilderHelper.generateCommaList((List) viewPermissions.get(PoActionPermissionService.GROUPS), true) + ") " +
					"or p.client.UID in (" + QueryBuilderHelper.generateCommaList((List) viewPermissions.get(PoActionPermissionService.CLIENTS), true) + "))" +
					" order by p.lastName";
		return getHibernateTemplate().find(query, keys);

	}

	@Override
	public List<PoPerson> findPersonsWithViewPermission(Map viewPermissions, List names, Date from, Date to) {
		String addSql = getNamesQueryString(names, "p");
		Object[] keys = { to, from, to, from };
		String query = "select distinct p from PoPersonGroup as pg join pg.person as p where " +
				" p.validfrom <= ? and p.validto > ? and pg.validfrom <= ? and pg.validto > ? and " +
				(names.size() > 0 ? "(" + addSql + ") and " : "") +
				" (p.UID in (" + QueryBuilderHelper.generateCommaList((List) viewPermissions.get(PoActionPermissionService.PERSONS), true) + " ) " +
				" or pg.group.UID in (" + QueryBuilderHelper.generateCommaList((List) viewPermissions.get(PoActionPermissionService.GROUPS), true) + ") " +
				"or p.client.UID in (" + QueryBuilderHelper.generateCommaList((List) viewPermissions.get(PoActionPermissionService.CLIENTS), true) + "))" +
				" order by p.lastName";
		return getHibernateTemplate().find(query, keys);

	}

	private String getNamesQueryString(List names, String alias) {
		String sql = "";
		if (alias != null)
			alias = alias + ".";
		else
			alias = "";
		if (names != null) {
			Iterator nI = names.iterator();
			while (nI.hasNext()) {
				String name = (String) nI.next();
				sql += " " + alias + "firstName like '%" + name + "%' or " + alias + "lastName like '%" + name + "%'";
				if (nI.hasNext())
					sql += " or ";
			}
		}
		return sql;
	}

	@Override
	public List findPersonGroupsF(PoPerson person, PoGroup group, Date validFrom) {
		Object[] kv = { person, group, validFrom, validFrom, validFrom };
		return getHibernateTemplate().find("from PoPersonGroup pg where " +
				" pg.person=? and pg.group=? and pg.validto>? and pg.group.validto>?" +
				" and pg.person.validto>? ", kv);
	}

	@Override
	public void refresh(Object obj) {
		getHibernateTemplate().refresh(obj);
	}
}
