package at.workflow.webdesk.po.impl.daos;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import at.workflow.webdesk.tools.pagination.PaginationCursor;
import at.workflow.webdesk.tools.pagination.PaginationDataProvider;
import at.workflow.webdesk.tools.pagination.impl.CriteriaPaginationCursor;

/**
 * The <b>Hibernate</b> Implementation of the <code>PaginationDataProvider</code> interface.
 * Uses Hibernate's Criteria functionality.
 * @see http://www.hibernate.org
 *  
 * @author hentner, ggruber
 */
public class PaginationDataProviderCriteriaImpl extends HibernateDaoSupport implements PaginationDataProvider {

	@Override
	public void navigateTo(PaginationCursor page, int index) {
		page.setActPage(index);
	}

	@Override
	public List<?> getElements(final PaginationCursor page) {
		return getElements(page, -1);
	}
	
	@Override
	public List<?> getElements(final PaginationCursor page, final int startElementIndex) {

		return getHibernateTemplate().execute(
				new HibernateCallback<List<?>>() {
					@Override
					public List<?> doInHibernate(Session session) throws HibernateException, SQLException {
						CriteriaPaginationCursor criteriaPage = (CriteriaPaginationCursor) page;
						Example example = Example.create(criteriaPage.getExampleObj()).ignoreCase().enableLike(MatchMode.ANYWHERE);
						Criteria criteria = session.createCriteria(criteriaPage.getSearchObj().getClass())
											.add(example);

						// how should we use those additional constraints ?????
						@SuppressWarnings("unchecked")
						Iterator<Object> addCriterias = criteriaPage.getAdditionalConstraints().keySet().iterator();
						while (addCriterias.hasNext()) {
							Object next = addCriterias.next();
							Object ex = criteriaPage.getAdditionalConstraints().get(next);
							Example exObj = Example.create(ex).ignoreCase().enableLike(MatchMode.ANYWHERE);
							criteria.createCriteria((String) next).add(exObj);
						}

						Map<String, String> hm = criteriaPage.getOrderObj();
						Iterator<String> i = hm.keySet().iterator();
						while (i.hasNext()) {
							String key = i.next();
							String direction = hm.get(key).toString();
							if (direction.equals("asc"))
								criteria.addOrder(Order.asc(key));
							if (direction.equals("desc"))
								criteria.addOrder(Order.desc(key));
						}

						final int firstResult = (startElementIndex >= 0)
								? startElementIndex
								: criteriaPage.getActPage() * criteriaPage.getPageSize() - criteriaPage.getPageSize();

						criteria.setFirstResult(startElementIndex);
						criteria.setMaxResults(criteriaPage.getPageSize());

						return criteria.list();
					}
				}
				);
	}

	@Override
	public long getTotalNumberOfElements(final PaginationCursor page) {
		return getHibernateTemplate().execute(
				new HibernateCallback<Integer>() {
					@Override
					public Integer doInHibernate(Session session) throws HibernateException, SQLException {
						CriteriaPaginationCursor criteriaPage = (CriteriaPaginationCursor) page;
						Example example = Example.create(criteriaPage.getExampleObj()).ignoreCase().enableLike(MatchMode.ANYWHERE);
						Criteria criteria = session.createCriteria(criteriaPage.getSearchObj().getClass())
											.setProjection(Projections.projectionList()
													.add(Projections.rowCount()))
											.add(example);
						Object res = criteria.uniqueResult();
						return (Integer) res;
					}
				}
				);
	}

}
