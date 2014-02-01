package at.workflow.webdesk.po.impl.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.daos.PoRoleCompetenceDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceAll;
import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.po.model.PoRoleCompetenceClient;
import at.workflow.webdesk.po.model.PoRoleCompetenceGroup;
import at.workflow.webdesk.po.model.PoRoleCompetencePerson;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoRoleCompetenceDAOImpl extends GenericHibernateDAOImpl<PoRoleCompetenceBase> implements PoRoleCompetenceDAO {

	@Override
	public PoRoleCompetenceAll getRoleCompetenceAll(String uid) {
		return (PoRoleCompetenceAll) getHibernateTemplate().load(
				PoRoleCompetenceAll.class, uid);
	}
	
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetenceAll> loadAllRoleCompetenceAll() {
		return getHibernateTemplate().loadAll(PoRoleCompetenceAll.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetenceBase> findRoleCompetence(PoRole role) {
		Object[] keyValues = { role, new Date(), new Date() };
		return getHibernateTemplate().find(
				"from PoRoleCompetenceBase rh where rh.role=? and rh.validto>? and rh.validfrom<=?", keyValues);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetenceBase> findRoleCompetenceF(PoRole role, Date date) {
		Object[] keyValues = { role, date };
		return getHibernateTemplate()
				.find("from PoRoleCompetenceBase rh where rh.role=? and rh.validto>?",
						keyValues);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetenceAll> findRoleCompetenceAll(PoRole role, Date referenceDate) {
		Object[] keyValues = { role, referenceDate, referenceDate,
				referenceDate, referenceDate };
		List<PoRoleCompetenceAll> l = getHibernateTemplate()
					.find(
						"select rh from PoRoleCompetenceAll rh "
								+ " join rh.role as r "
								+ " where r=? and rh.validfrom <=? "
								+ " and rh.validto>? and r.validfrom<=? and r.validto>?",
						keyValues);
		return l;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetenceAll> findRoleCompetenceAllF(PoRole role, Date referenceDate) {
		Object[] keyValues = { role, referenceDate, referenceDate};
		List<PoRoleCompetenceAll> l = getHibernateTemplate()
				.find("select rca from PoRoleCompetenceAll rca "
								+ " join rca.role as r "
								+ " where r=? "
								+ " and rca.validto>? "  
								+ " and r.validto>?",
						keyValues);
		return l;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetencePerson> findRoleCompetencePersonWithCompetence4PersonF(PoRole role,
			PoPerson controlledPerson, Date referenceDate) {
		Object[] keyValues = { role, controlledPerson, referenceDate,
				referenceDate };
		return getHibernateTemplate().find(
				"select rhcp from PoRoleCompetencePerson as rhcp join rhcp.role as r  "
						+ " where r =? and rhcp.competence4Person=?"
						+ " and rhcp.validto >? "
						+ " and r.validto >? ",
				keyValues);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetencePerson> findRoleCompetencePerson(PoRole role) {

		Object[] keyValues = { role, new Date(), new Date() };
		return getHibernateTemplate().find(
				"from PoRoleCompetencePerson as rhcp " + " where rhcp.role=? "
						+ " and rhcp.validfrom<=? " + " and rhcp.validto >? ",
				keyValues);

	}
	
	public void saveRoleHolderBase(PoRoleCompetenceBase rhb) {
		getHibernateTemplate().saveOrUpdate(rhb);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetenceGroup> findRoleCompetenceGroupWithCompetence4GroupF(PoRole role, PoGroup controlledGroup, Date referenceDate) {
		if (referenceDate == null)
			referenceDate = new Date();
		Object[] keyValues = { role, controlledGroup, referenceDate,
				referenceDate };
		List<PoRoleCompetenceGroup> l = getHibernateTemplate().find(
				"from PoRoleCompetenceGroup as rhcg " + " where rhcg.role=? "
						+ " and rhcg.competence4Group=? "
						+ " and rhcg.competence4Group.validto >?"
						+ " and rhcg.validto >? ",
				keyValues);
		return l;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetenceClient> findRoleCompetenceClient(PoRole role) {
		
		Object[] keyValues = { role, new Date(), new Date() };
		return getHibernateTemplate().find(
				"from PoRoleCompetenceClient as rhcg " + " where rhcg.role=? "
						+ " and rhcg.validfrom<=? " + " and rhcg.validto >? ",
						keyValues);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetenceGroup> findRoleCompetenceGroup(PoRole role) {

		Object[] keyValues = { role, new Date(), new Date() };
		return getHibernateTemplate().find(
				"from PoRoleCompetenceGroup as rhcg " + " where rhcg.role=? "
						+ " and rhcg.validfrom<=? " + " and rhcg.validto >? ",
				keyValues);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleCompetenceClient> findRoleCompetenceClientWithCompetence4ClientF(PoRole role,
			PoClient client, Date referenceDate) {
		Object[] keyValues = { role, client, referenceDate, referenceDate };
		return getHibernateTemplate().find(
				"select rhcp from PoRoleCompetenceClient as rhcp join rhcp.role as r  "
						+ " where r =? and rhcp.competence4Client=?"
						+ " and rhcp.validto >? "
						+ " and r.validto >? ",
				keyValues);
	}

	@Override
	protected Class<PoRoleCompetenceBase> getEntityClass() {
		return PoRoleCompetenceBase.class;
	}

}
