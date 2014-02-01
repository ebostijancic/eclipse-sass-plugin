package at.workflow.webdesk.po.impl.daos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.daos.PoPasswordDAO;
import at.workflow.webdesk.po.model.PoPassword;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * Adds finders to basic DAO functionality.
 * 
 * @author fritzberger 21.10.2010
 */
public class PoPasswordDAOImpl extends GenericHibernateDAOImpl<PoPassword> implements PoPasswordDAO {

	/** @return <code>PoPassword.class</code>. */
	@Override
	protected Class<PoPassword> getEntityClass() {
		return PoPassword.class;
	}

	/** Implements PoPasswordDAO. */
	@Override
	public PoPassword findCurrentPassword(PoPerson person) {
		List<PoPassword> result = findNewestPasswords(person, 1);
		return result != null && result.size() == 1 ? (PoPassword) result.get(0) : null;
	}

	/** Implements PoPasswordDAO. */
	@Override
	@SuppressWarnings("unchecked")
	public List<PoPassword> findAllPasswords(PoPerson person) {
		final String query = from()+" pw where pw.person = ?";
		final Object [] params = new Object [] { person };
		return getHibernateTemplate().find(query, params);
	}

	/** Implements PoPasswordDAO. */
	@Override
	public List<PoPassword> findNewestPasswords(PoPerson person, int numberOfPasswords) {
		List<PoPassword> list = findAllPasswords(person);
		
		// sort by valid-from date, descending (newest first)
		Collections.sort(list, new Comparator<PoPassword>()	{
			@Override
			public int compare(PoPassword p1, PoPassword p2) {
				Date d1 = p1.getValidfrom();
				Date d2 = p2.getValidfrom();
				return d1.equals(d2) ? 0 : d1.after(d2) ? -1 : +1;
			}
		});
		
		// return only numberOfPasswords items
		List<PoPassword> latest = new ArrayList<PoPassword>();
		for (PoPassword p : list)	{
			if (latest.size() < numberOfPasswords)
				latest.add(p);
			else
				break;
		}
		
		return latest;
	}

}
