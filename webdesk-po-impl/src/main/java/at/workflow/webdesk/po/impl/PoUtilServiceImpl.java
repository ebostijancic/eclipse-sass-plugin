package at.workflow.webdesk.po.impl;

import java.beans.Expression;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import at.workflow.webdesk.po.BusinessDayAdder;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.PoUtilService;
import at.workflow.webdesk.po.daos.PoQueryUtils;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.TemporaryAccessPermission;
import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.hibernate.SessionThreadDecorator;
import at.workflow.webdesk.tools.pagination.FilterAndSortPaginationCursor;
import at.workflow.webdesk.tools.pagination.PaginationCursor;
import at.workflow.webdesk.tools.pagination.PaginationDataProvider;
import at.workflow.webdesk.tools.pagination.impl.CriteriaPaginationCursor;
import at.workflow.webdesk.tools.pagination.impl.ListPaginationCursor;
import at.workflow.webdesk.tools.pagination.impl.QueryPaginationCursor;

/**
 * @author hentner
 * @author amalic
 * 
 * 
 *         TODO Mailservice auslagern, e.printStackTrace ... entfernen,
 *         PoRuntimeExceptions überdenken.
 * 
 *         FIXME: harald: remove dependencies to cocoon -> move out all GUI
 *         specific code
 * 
 */
public class PoUtilServiceImpl implements PoUtilService, ApplicationContextAware{

	protected final Logger logger = Logger.getLogger(this.getClass().getName());

	/*
	 * Paging DAO Implementations
	 */
	private PaginationDataProvider queryPageDAO;
	private PaginationDataProvider criteriaPageDAO;
	private PaginationDataProvider listPageDAO;

	private PoOrganisationService organisationService;

	private CacheManager cacheManager;

	private static final String TEMP_ACCESS_RIGHTS_CACHE_ID = "webdesk_temporary_accessrights_cache";
	
	private Cache tapCache;  
	
	private Map<String,String> tapLinkedKeyMap = Collections.synchronizedMap( new HashMap<String,String>() );
	
	private ApplicationContext applicationContext;


	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Map convertBeanToMap(Object o, List fieldNames) {
		Map m = new HashMap();
		Iterator fieldNamesI = fieldNames.iterator();
		while (fieldNamesI.hasNext()) {
			String fieldName = (String) fieldNamesI.next();
			Object res = fieldName;
			if (fieldName.indexOf("$") == -1) {

				String methodName = "";
				if (fieldName.indexOf(".") > -1) {
					StringTokenizer tok = new StringTokenizer(fieldName, ".");
					methodName = getMethodName(tok.nextToken(), "get");
					while (tok.hasMoreElements()) {
						methodName += "()."
								+ getMethodName(tok.nextToken(), "get");
					}
				} else
					methodName = getMethodName(fieldName, "get");

				Expression e = new Expression(o, methodName, null);
				try {
					e.execute();
					res = e.getValue();
				} catch (Exception e1) {
					throw new PoRuntimeException(e1.getMessage());
				}
			}
			m.put(fieldName, res);
		}
		return m;
	}

	@Override
	public String getMethodName(String fieldName, String prefix) {
		if (fieldName.length() < 1)
			throw new PoRuntimeException("FieldName is not valid. Length < 1");
		fieldName = prefix + fieldName.substring(0, 1).toUpperCase()
				+ fieldName.substring(1, fieldName.length());
		return fieldName;
	}


	@Override
	public int getCacheElementCount(String cacheIdentifier) {
		Cache c = cacheManager.getCache(cacheIdentifier);
		try {
			return c.getKeys().size();
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public long getCacheSize(String cacheIdentifier) {
		Cache c = cacheManager.getCache(cacheIdentifier);
		try {
			return c.calculateInMemorySize();
		} catch (Exception e) {
			return -1;
		}

	}

	/**
	 * The Initialisation Function of the utilservice
	 */
	public void init() {
		try {
			cacheManager = CacheManager.create();
			if (cacheManager.getCache(PoConstants.PERMISSIONCACHE) == null) {
				Cache permissionCache = new Cache(PoConstants.PERMISSIONCACHE,
						1000, false, false, 5000, 2000, false, 120);
				cacheManager.addCache(permissionCache);
			}
			if (cacheManager.getCache(PoConstants.QUERYCACHE) == null) {
				Cache queryCache = new Cache(PoConstants.QUERYCACHE, 10000,
						false, false, 5000, 2000, false, 120);
				cacheManager.addCache(queryCache);
			}
			if (cacheManager.getCache(PoConstants.MENUCACHE) == null) {

				Cache menuCache = new Cache(PoConstants.MENUCACHE, 10000,
						false, false, 5000, 2000, false, 120);
				cacheManager.addCache(menuCache);
			}
			
			if (cacheManager.getCache(TEMP_ACCESS_RIGHTS_CACHE_ID)==null) {
				// lives for 4 hours
				tapCache = new Cache(TEMP_ACCESS_RIGHTS_CACHE_ID, 1000, false, false, 14400, 0);
				cacheManager.addCache(tapCache);
			}


			if (cacheManager.getCache(PoConstants.LOCKCACHE) == null) {
				// this cache is never deleted and all entries are kept
				// TODO elements can be deleted when they are too old (will be
				// refreshed otherwise)
				Cache lockCache = new Cache(PoConstants.LOCKCACHE, 4000, true,
						true, 0, 0);
				cacheManager.addCache(lockCache);
			}

		} catch (CacheException e) {
			e.printStackTrace();
		}

	}

	private PoQueryUtils queryUtils;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.workflow.webdesk.po.PoUtilService#createQueryPage(java.lang.String,
	 * java.lang.String[], java.lang.Object[], int)
	 */
	@Override
	public FilterAndSortPaginationCursor createQueryPaginationCursor(String hqlOrQueryName, String[] queryParameters,
			Object[] parameterValues, int pageSize) {
		return createQueryPaginationCursor(hqlOrQueryName, queryParameters, parameterValues, pageSize, this.queryPageDAO);
	}
	
	@Override
	public FilterAndSortPaginationCursor createQueryPaginationCursor(String hqlOrQueryName, String[] queryParameters,
			Object[] parameterValues, int pageSize, PaginationDataProvider pageDAO) {
		QueryPaginationCursor page = new QueryPaginationCursor(hqlOrQueryName, queryParameters, parameterValues,
				pageSize);
		
		// alternate pageDAO
		page.setPageDAO(pageDAO);

		if (pageSize == -1) {
			page.setPageSize(new Long(page.getTotalNumberOfElements())
					.intValue());
		}
		return page;
	}

	@Override
	public CriteriaPaginationCursor createCriteriaPage(Object searchObj,
			Object filterObj, int pageSize) {
		CriteriaPaginationCursor page = new CriteriaPaginationCursor(searchObj, filterObj, pageSize);
		page.setPageDAO(criteriaPageDAO);
		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.workflow.webdesk.po.PoUtilService#createListPage(java.util.List,
	 * int)
	 */
	@Override
	public PaginationCursor createListPage(List<?> elements, int pageSize) {

		ListPaginationCursor page = new ListPaginationCursor(elements, pageSize);
		page.setPageDAO(this.listPageDAO);
		return page;
	}

	/**
	 * @param queryPageDAO
	 *            The queryPageDAO to set.
	 */
	public void setQueryPageDAO(PaginationDataProvider queryPageDAO) {
		this.queryPageDAO = queryPageDAO;
	}

	/**
	 * @param criteriaPageDAO
	 *            The criteriaPageDAO to set.
	 */
	public void setCriteriaPageDAO(PaginationDataProvider criteriaPageDAO) {
		this.criteriaPageDAO = criteriaPageDAO;
	}

	@Override
	public void testThrowException() {
		// test function for logging purposes

		try {
			// do something stupid
			int i = 0;
			int x = 4711;
			@SuppressWarnings("unused")
			int xx = x / i;

		} catch (Exception e) {
			this.logger.error("Exception happened!", e);
			throw new PoRuntimeException(e);
		}
	}

	@Override
	public String generateUID() {
		return this.queryUtils.generateUID();
	}

	@Override
	public List<String> getEmailAdresses(PoGroup sendToGroup) {
		List<String> emailAdresses = new ArrayList<String>();
		List<PoPerson> persons = this.organisationService.findPersonsOfGroup(
				sendToGroup, new Date());
		for (Iterator<PoPerson> iter = persons.iterator(); iter.hasNext();) {
			PoPerson person = iter.next();
			emailAdresses.add(person.getEmail());
		}
		return emailAdresses;
	}
	
	@Override
	public String generateTemporaryAccessPermission(String userName,
			String sourceActionURL, String targetActionURL) {
		
		String linkedKey = createLinkedKey(userName, sourceActionURL, targetActionURL);
		
		// if token exits -> reuse it!
		if (tapLinkedKeyMap.containsKey(linkedKey) && this.tapCache.isElementInMemory(tapLinkedKeyMap.get(linkedKey))) {
			String securityToken =  this.tapLinkedKeyMap.get(linkedKey);
			return securityToken;
		}
		
		TemporaryAccessPermission tap = new TemporaryAccessPermission();
		tap.setSecurityToken(this.generateUID());
		tap.setSourceActionUrl(sourceActionURL);
		tap.setTargetActionURL(targetActionURL);
		tap.setUserName(userName);
		
		this.tapCache.put(new net.sf.ehcache.Element(tap.getSecurityToken(), tap));
		this.tapLinkedKeyMap.put(linkedKey, tap.getSecurityToken());
		
		return tap.getSecurityToken();
	}

	@Override
	public boolean hasTemporaryAccess(String userName, String sourceActionURL,
			String targetActionURL, String securityToken) {
		
		String linkedKey = createLinkedKey(userName, sourceActionURL, targetActionURL);
		
		if (securityToken!=null && securityToken.equals(this.tapLinkedKeyMap.get(linkedKey)) &&
				this.tapCache.isElementInMemory(securityToken)) {
			
			Element cacheElem = this.tapCache.get(securityToken);
			if (cacheElem!=null && cacheElem.isExpired()) {
				this.tapCache.remove(securityToken);
				this.tapLinkedKeyMap.remove(linkedKey);
				return false;
			}
			TemporaryAccessPermission tap = (TemporaryAccessPermission) cacheElem.getValue();
			tap.setHits(tap.getHits()+1);
			
			return true;
		}
		
		return false;
	}
	
	private String createLinkedKey(String userName, String sourceActionURL, String targetActionURL) {
		return userName + "#" + sourceActionURL + ":" + targetActionURL;
	}

	public void setQueryUtils(PoQueryUtils queryUtils) {
		this.queryUtils = queryUtils;
	}

	@Override
	public void evictObject(Object obj) {
		this.queryUtils.evictObject(obj);
	}

	@Override
	public void refreshObject(Object obj) {
		this.queryUtils.refreshObject(obj);
	}

	public void setListPageDAO(PaginationDataProvider listPageDAO) {
		this.listPageDAO = listPageDAO;
	}

	public void setOrganisationService(PoOrganisationService organisationService) {
		this.organisationService = organisationService;
	}
	
	 @Override
	public Thread decorateWithSession(Thread thread) {
		 return new SessionThreadDecorator(thread, false);
	 }
	 
	 @Override
	public Thread decorateWithSession(Thread thread, boolean useCurrentRequestSession) {
		 return new SessionThreadDecorator(thread, true);
	 }

	
	private void initQueryPage(QueryPaginationCursor page, PaginationDataProvider pageDAO, int pageSize) {
		// alternate pageDAO
		page.setPageDAO(pageDAO);

		if (pageSize == -1) {
			page.setPageSize(new Long(page.getTotalNumberOfElements())
					.intValue());
		}
		
	}

	@Override
	public FilterAndSortPaginationCursor createQueryPaginationCursor(PositionalQuery query, int pageSize) {
		QueryPaginationCursor page = new QueryPaginationCursor(query, pageSize);
		initQueryPage(page, this.queryPageDAO, pageSize);
		return page;
	}

	@Override
	public FilterAndSortPaginationCursor createQueryPaginationCursor(NamedQuery query, int pageSize) {
		QueryPaginationCursor page = new QueryPaginationCursor(query, pageSize);
		initQueryPage(page, this.queryPageDAO, pageSize);
		return page;
	}


	@Override
	public Date addBusinessDays(Date date, int businessDays) {
		String exactAdderUsingHolidays = findAdderBean(applicationContext.getBeanNamesForType(BusinessDayAdder.class));
		
		if( StringUtils.isNotBlank(exactAdderUsingHolidays)) {
			BusinessDayAdder adder = (BusinessDayAdder) applicationContext.getBean( exactAdderUsingHolidays );
			return adder.addBusinessDays(date, businessDays);
		} else {
			return DateTools.addWeekDays(date, businessDays);
		}
	}
	
	private String findAdderBean(String[] beanNames) {
		for (String beanName : beanNames ) {
			if (beanName.startsWith(PoUtilService.class.getSimpleName())==false)
				return beanName;
		}
		return null;
	}

	@Override
	public Date getNextBusinessDay(Date from) {
		return addBusinessDays(from, 1);
	}


}
