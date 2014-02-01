package at.workflow.webdesk.po.impl.daos;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import at.workflow.webdesk.tools.pagination.PaginationCursor;
import at.workflow.webdesk.tools.pagination.PaginationDataProvider;
import at.workflow.webdesk.tools.pagination.impl.QueryPaginationCursor;

/**
 * This is the Hibernate implementation of our "pagination" class which is
 * responsible for delivering the correct batches of records to the GUI client.
 * 
 * It is used extensively in the SimpleDataList XSLT together with HQL Queries in that case.
 * 
 * Created on 21.07.2005
 * @author hentner
 * @author ggruber
 */
public class PaginationDataProviderQueryImpl extends HibernateDaoSupport implements PaginationDataProvider {

	@Override
	public void navigateTo(PaginationCursor page, int pageIndex) {
		page.setActPage(pageIndex);
	}

	@Override
	public List<?> getElements(PaginationCursor page) {
		return getElements(page, -1);
	}
	
	private void assertQuery(QueryPaginationCursor queryPage) {
		if (queryPage.getHqlQuery().indexOf("from ") == -1)
			throw new IllegalArgumentException("Passed Query is not a valid HQL/JPA Query! Named Hibernate Queries are not supported! Query is: >"+queryPage.getHqlQuery()+"<");
	}

	@Override
	public List<?> getElements(final PaginationCursor page, final int startElementIndex) {
		return getHibernateTemplate().execute(new HibernateCallback<List<?>>() {
			final protected Logger logger = Logger.getLogger(getClass());

			@Override
			public List<?> doInHibernate(Session session)
					throws HibernateException, SQLException {
				
				QueryPaginationCursor queryPage = (QueryPaginationCursor) page;
				
				assertQuery(queryPage);

				Query query = session.createQuery(queryPage.getHqlQuery());
				
				if (logger.isDebugEnabled())
					logger.debug("query = " + query.getQueryString());

				int firstResult = (startElementIndex >= 0)
					? startElementIndex
					: queryPage.getActPage() * queryPage.getPageSize() - queryPage.getPageSize();
				
				if (firstResult < 0)
					firstResult = 0;

				query.setFirstResult(firstResult);
				query.setMaxResults(queryPage.getPageSize());
				query.setCacheable(queryPage.isCacheQueryResults());
				
				int i = 0;
				if (queryPage.getParameterValues() != null)
					while (i < queryPage.getParameterValues().length) {
						if (queryPage.getParameterNames()[i] != null
								&& query.getQueryString().indexOf(":" + queryPage.getParameterNames()[i]) > -1)
							setQueryParameterCollectionAware(query, queryPage.getParameterNames()[i], queryPage.getParameterValues()[i]);
						i++;
					}
				return query.list();
			}


		});
	}

	@Override
	public long getTotalNumberOfElements(final PaginationCursor page) {
			return getHibernateTemplate().execute(
					new HibernateCallback<Long>() {
							@Override
							public Long doInHibernate(Session session) throws HibernateException, SQLException {
								QueryPaginationCursor queryPage = (QueryPaginationCursor) page;
								
								assertQuery(queryPage);
								
								Query query = session.createQuery(queryPage.getHqlQuery());

								StringBuffer sb = new StringBuffer();
								if (query.getQueryString().indexOf(".") != -1) {
									// hack ggruber
									// this was needed as the extraction of the alias did not work properly in some cases
									String alias = extractAlias(query.getQueryString());

									// alias can be null! (if none is present!)
									if (alias != null && alias.indexOf("%") == -1)
										sb.append("select count(distinct " + alias + ".UID)");	// TODO: for PO classes this should be ".uid" - Hibernate uses field names fopr query!
									else
										sb.append("select count(*) ");
								} else	{
									sb.append("select count(*) ");
								}

								String qs = query.getQueryString();
								if (query.getQueryString().indexOf("select") >= 0 && query.getQueryString().indexOf("select") < 10) {	// TODO comment what is 10
									sb.append(qs.substring(qs.indexOf(" from")));
								} else {
									sb.append(qs);
								}

								// fix by ggruber
								// remove order by clause in count() statement
								if (sb.indexOf("order by") >= 1) {
									sb.setLength(sb.indexOf("order by"));
								}

								String countSql = sb.toString();	// fri_2013-07-17: please read my comment at end of this file!
								if (countSql.indexOf("left join fetch ") > -1) {
									countSql = countSql.replaceAll(" left join fetch \\S+", "");
								}
								
								if (countSql.indexOf("join fetch ") > -1) {
									countSql = countSql.replaceAll(" join fetch \\S+", "");
								}

								String queryString = query.getQueryString();
								Object [] queryObjects = queryPage.getParameterValues();
								String [] queryParams = queryPage.getParameterNames();
								try {
									Query execQuery = session.createQuery(countSql);
									execQuery.setCacheable( queryPage.isCacheQueryResults() );
									setQueryParameters(queryString, queryObjects, queryParams, execQuery);
									return (Long) execQuery.uniqueResult();
								}
								catch (Exception e) {
									// in case count(*) did not work just try to execute the whole query and take the size...
									
									// fri_2013-04-30: talked with Gabriel about that: Generally this is not a good strategy, but it is too risky to change this part of Webdesk
									// See also http://intranet/intern/ifwd_mgm.nsf/0/26040E9CC91F7E19C1257B5D00356903?OpenDocument notes://asterix/intern/ifwd_mgm.nsf/0/26040E9CC91F7E19C1257B5D00356903?EditDocument
									
									// fri_2012-03-21 unfortunately this block also is executed when a Cocoon column's datatype (show*.xml, recordset) did not match,
									//		so it is dangerous to NOT report this exception!
									
									// fri_2013-04-25: the "distinct alias.UID" that was added above fails here! Error is:
									// 		org.hibernate.QueryException: could not resolve property: UID of: at.workflow.webdesk.hr.model.HrPerson
									// 		[select count(distinct hp.UID) from at.workflow.webdesk.hr.model.HrPerson hp join hp.person pp join pp.memberOfGroups pg where pp.validfrom <= current_timestamp() and pp.validto > current_timestamp() and pg.validfrom <= current_timestamp() and pg.validto > current_timestamp() and pg.group.validfrom <= current_timestamp() and pg.group.validto > current_timestamp() and pg.group.orgStructure.orgType = 1 ]
									
									// fri_2013-04-30: as this can be any exception, we write it to stderr for grateful notion of the developer
									e.printStackTrace();
									
									setQueryParameters(queryString, queryObjects, queryParams, query);	// TODO: fri_2013-04-25: at least cut off "order by" to gain performance!
									return new Long(query.list().size());
								}
							}

							private void setQueryParameters(String queryString, Object[] queryObjects, String[] queryParams, Query execQuery) {
								for (int i = 0; queryObjects != null && i < queryObjects.length; i++)
									if (queryParams[i] != null && queryString.indexOf(":" + queryParams[i]) >= 0)
										setQueryParameterCollectionAware(execQuery, queryParams[i], queryObjects[i]);
							}

							/** extracts alias of Object in from-clause if one is present */
							private String extractAlias(String queryString) {
								// calculate from clause
								String fromClause = queryString.substring(queryString.indexOf("from") + 5);

								// assume end of from clause in start of a) (left, right, outer or plain) join or b) where or c) group by or d) order by
								if (fromClause.indexOf("left") != -1) {
									fromClause = fromClause.substring(0, fromClause.indexOf("left")).trim();
								} else if (fromClause.indexOf("right") != -1) {
									fromClause = fromClause.substring(0, fromClause.indexOf("right")).trim();
								} else if (fromClause.indexOf("outer") != -1) {
									fromClause = fromClause.substring(0, fromClause.indexOf("outer")).trim();
								} else if (fromClause.indexOf("join") != -1) {
									fromClause = fromClause.substring(0, fromClause.indexOf("join")).trim();
								} else if (fromClause.indexOf("group by") != -1) {
									fromClause = fromClause.substring(0, fromClause.indexOf("group by")).trim();
								} else if (fromClause.indexOf("where") != -1) {
									fromClause = fromClause.substring(0, fromClause.indexOf("where")).trim();
								} else if (fromClause.indexOf("order by") != -1) {
									fromClause = fromClause.substring(0, fromClause.indexOf("order by")).trim();
								}

								// from the fromClause the alias is extracted if present!
								if (fromClause.lastIndexOf(" ") != -1 && fromClause.lastIndexOf(" ") != fromClause.length() - 1)
									return fromClause.substring(fromClause.lastIndexOf(" "), fromClause.length()).trim();
								
								return null;
							}	// end method extractAlias()
							
						}	// end new HibernateCallback
					);	// end execute
			
		
	}
	
	private void setQueryParameterCollectionAware(Query query, String parameterName, Object value) {
		PoGeneralDAOImpl.applyNamedParameterToQuery(query, parameterName, value);
	}

}

/*
	Ich habe das jetzt mit SQL und den neuen HR Testdaten ausprobiert:
	
	select
		distinct(p.username), p.firstName, p.lastName, p.person_uid, o.orgType
	from
		HrPerson hp, PoPerson p, PoPersonGroup pg, PoGroup g, PoOrgStructure o
	where
		hp.PERSON_UID = p.PERSON_UID
		and p.PERSON_UID = pg.PERSON_UID
		and g.GROUP_UID = pg.GROUP_UID
		and g.ORGSTRUCTURE_UID = o.ORGSTRUCTURE_UID
	
	liefert bei mir 6 Datensätze, aber
	
	select
		distinct(p.username)
	from
		HrPerson hp, PoPerson p, PoPersonGroup pg, PoGroup g, PoOrgStructure o
	where
		hp.PERSON_UID = p.PERSON_UID
		and p.PERSON_UID = pg.PERSON_UID
		and g.GROUP_UID = pg.GROUP_UID
		and g.ORGSTRUCTURE_UID = o.ORGSTRUCTURE_UID
	
	liefert nur 4 Datensätze!
	Möglicherweise hatten wir das schon mal: expliziter join notwendig wenn ... ?
*/