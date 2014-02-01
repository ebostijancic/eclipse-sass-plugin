package at.workflow.webdesk.po.impl.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.daos.PoRoleDeputyDAO;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleDeputy;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoRoleDeputyDAOImpl extends GenericHibernateDAOImpl<PoRoleDeputy> implements PoRoleDeputyDAO {

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleDeputy> findRoleDeputiesOfPerson(PoPerson person, Date date) {
		Object[] kV = {person, date};
		return getHibernateTemplate().find("from PoRoleDeputy where officeHolder=? and validto>=?", kV);
	}
	
	@Override
	public List<PoRoleDeputy> findRoleDeputiesOfPersonF(PoPerson person, Date date) {
		return findRoleDeputiesOfPerson(person, date);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleDeputy> findRoleDeputiesOfPerson(PoPerson person, PoRole role, Date date) {
		Object[] kv = {role, person, date, date, date, date};
		return getHibernateTemplate().find("select dp from PoRoleDeputy dp join dp.roleHolderPersons as rhp " +
				" join rhp.roleHolder as rh where " +
				" rh.role =? and dp.officeHolder=? and " +
				" dp.validfrom<=? and dp.validto>? " +
				" and rh.role.validfrom<=? and rh.role.validto>?", kv);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoRoleDeputy> findRoleDeputiesOfPersonF(PoPerson person, PoRole role, Date date) {
		Object[] kv = {role, person, date, date};
		return getHibernateTemplate().find("select distinct dp from PoRoleDeputy dp join dp.roleHolderPersons as rhp " +
				" join rhp.roleHolder as rh where " +
				" rh.role =? and dp.officeHolder=? and " +
				" dp.validto>? and " +
				" rh.role.validto>? ", kv);
	}

	@Override
	protected Class<PoRoleDeputy> getEntityClass() {
		return PoRoleDeputy.class;
	}


}
