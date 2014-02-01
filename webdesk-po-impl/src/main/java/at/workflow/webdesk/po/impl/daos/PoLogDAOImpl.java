package at.workflow.webdesk.po.impl.daos;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import at.workflow.webdesk.po.daos.PoLogDAO;
import at.workflow.webdesk.po.model.PoLog;
import at.workflow.webdesk.po.model.PoLogDetail;
import at.workflow.webdesk.po.model.PoLogRequestParameter;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * DAO to read and write PoLog Entities
 * via Hibernate
 * 
 * @author ggruber
 */
public class PoLogDAOImpl extends GenericHibernateDAOImpl<PoLog> implements PoLogDAO {

	@Override
	protected Class<PoLog> getEntityClass() {
		return PoLog.class;
	}
	
	@Override
	public void deleteAllLogs() {
		getHibernateTemplate().execute(new HibernateCallback<Object>() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.createQuery("delete from PoLogDetailThrowable").executeUpdate();
				return null;
			}
		});
		getHibernateTemplate().execute(new HibernateCallback<Object>() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.createQuery("delete from PoLogDetail").executeUpdate();
				return null;
			}
		});
		getHibernateTemplate().execute(new HibernateCallback<Object>() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.createQuery("delete from PoLogRequestParameter").executeUpdate();
				return null;
			}
		});
		getHibernateTemplate().execute(new HibernateCallback<Object>() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.createQuery("delete from PoLog").executeUpdate();
				return null;
			}
		});
	}

	/**
	 * DELETE from PoLogDetailThrowable where LOGDETAIL_UID in (select LOGDETAIL_UID from PoLogDetail where LOG_UID in (select LOG_UID from PoLog where endTime < :date))
	 * DELETE from PoLogDetail where LOG_UID in (select LOG_UID from PoLog where endTime < :date)
	 * DELETE from PoLogRequestParameter where LOG_UID in (select LOG_UID from PoLog where endTime < :date)
	 * DELETE from PoLog where endTime < :date
	 */
	@Override
	public void deleteOlderXDays(int days) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -days);
		final Date date = cal.getTime();

		getHibernateTemplate().execute(new HibernateCallback<Object>() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.createQuery(
						"delete from PoLogDetailThrowable where logDetail in (from PoLogDetail where log in (from PoLog where beginTime < :date))")
						.setDate("date", date)
						.executeUpdate();
				return null;
			}
		});
		getHibernateTemplate().execute(new HibernateCallback<Object>() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.createQuery(
					"delete from PoLogDetail where log in (from PoLog where beginTime < :date)")
					.setDate("date", date)
					.executeUpdate();
				return null;
			}
		});
		getHibernateTemplate().execute(new HibernateCallback<Object>() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.createQuery(
						"delete from PoLogRequestParameter where log in (from PoLog where beginTime < :date)")
						.setDate("date", date)
						.executeUpdate();
				return null;
			}
		});
		getHibernateTemplate().execute(new HibernateCallback<Object>() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.createQuery(
					"delete from PoLog where beginTime < :date")
					.setDate("date", date)
					.executeUpdate();
				return null;
			}
		});
	}
	

	@Override
	@SuppressWarnings("unchecked")
	public List<PoLogDetail> findLogDetails(PoLog log) {
		Object[] keys = { log };
		return getHibernateTemplate().find(
				"select distinct ld from PoLogDetail ld left outer join fetch ld.logDetailThrowables where ld.log = ?",
				keys);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PoLog> findLogsInSameContinuation(PoLog log) {
		return getHibernateTemplate().find("from PoLog l where l.continuationId = ? and l.UID <> ? order by l.beginTime asc", new Object[] { log.getContinuationId(), log.getUID() } );
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PoLogRequestParameter> findLogRequestParameters(PoLog log) {
		return getHibernateTemplate().find("from PoLogRequestParameter where log=?", log);
	}
	
}
