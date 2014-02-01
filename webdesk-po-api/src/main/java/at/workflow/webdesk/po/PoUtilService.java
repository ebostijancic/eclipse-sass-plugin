package at.workflow.webdesk.po;

import java.util.Date;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.pagination.FilterAndSortPaginationCursor;
import at.workflow.webdesk.tools.pagination.PaginationCursor;
import at.workflow.webdesk.tools.pagination.PaginationDataProvider;
import at.workflow.webdesk.tools.pagination.impl.CriteriaPaginationCursor;

/**
 * @author ggruber
 * @author amalic
 * @author hentner
 *
 * <p>
 * This service provides utility functions, which does not fit into 
 * existing services. Think twice before adding functionality here,
 * sometimes it makes more sense to start with a new service instead.
 * </p>
 *
 *
 * @see at.workflow.webdesk.po.PoActionService
 * @see at.workflow.webdesk.po.PoOrganisationService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.PoModuleService
 * @see at.workflow.webdesk.po.PoFileService
 * @see at.workflow.webdesk.po.PoLogService
 * @see at.workflow.webdesk.po.PoLanguageService
 * @see at.workflow.webdesk.po.PoBeanPropertyService
 * @see at.workflow.webdesk.po.model.PoRole

 */
public interface PoUtilService extends BusinessDayAdder {
	
    
    public Map<String,Object> convertBeanToMap(Object o, List<String> fieldNames);


    /**
     * @param fieldName
     * @param prefix
     * @return returns a getter or setter method according to
     * the java pojo specification. 
     * 
     * e.g.:
     * 
     * fieldName = client, prefix = get
     * 
     * --> returns "getFieldName"
     */
    public String getMethodName(String fieldName, String prefix);
        
    
    /**
     * @param cacheId
     * @return the amount of Elements in the cache.
     */
    public int getCacheElementCount(String cacheId);
    
    
    /**
     * 
     * 
     * Gets the size of the memory store for this cache
     * Warning: This method can be very expensive to run. 
     * Allow approximately 1 second per 1MB of entries. 
     * Running this method could create liveness problems 
     * because the object lock is held for a long period #
     * 
     * @param cacheIdentifier the identifier of the cache
     * @return the size of the memory store for this cache.
     */
    public long getCacheSize(String cacheIdentifier);
    

    public FilterAndSortPaginationCursor createQueryPaginationCursor(PositionalQuery query, int pageSize);
    
    public FilterAndSortPaginationCursor createQueryPaginationCursor(NamedQuery query, int pageSize);

	/**
	 * Creates a QueryPaginationCursor object for the specified HQL/JPA expression or named Hibernate Query 
	 * and the passed queryParameters, values for the specified pageSize.
	 *  
	 * @param queryExprOrQueryName
	 * @param queryParameters
	 * @param parameterValues
	 * @param pageSize
	 * @return a PaginationCursor object
	 * 
	 * creates a PaginationCursor object, more exactly a QueryPaginationCursor Object, which extends
	 * the PaginationCursor.
	 */
	public FilterAndSortPaginationCursor createQueryPaginationCursor(String queryExprOrQueryName, String[] queryParameters,
			Object[] parameterValues, int pageSize);
	
	/**
	 * @param queryName
	 * @param queryParameters
	 * @param obj
	 * @param pageSize
	 * @param alternatePageDAO -> for other sessionfactories...
	 * @return a PaginationCursor object
	 * 
	 * creates a PaginationCursor object, more exactly a QueryPaginationCursor Object, which extends
	 * the PaginationCursor.
	 */
	public FilterAndSortPaginationCursor createQueryPaginationCursor(String queryName, String[] queryParameters,
			Object[] obj, int pageSize, PaginationDataProvider alternateDataProvider);

	/**
	 * @param searchObj
	 * @param exampleObj
	 * @param pageSize
	 * @return
	 * 
	 * creates a PaginationCursor object, more exactly a QueryPaginationCursor Object, which extends
	 * the PaginationCursor.
	 */
	public CriteriaPaginationCursor createCriteriaPage(Object searchObj,
			Object exampleObj, int pageSize);

	/**
	 * @param elements
	 * @param pageSize
	 * @return creates a PaginationCursor object, more exactly a ListPaginationCursor Object, which
	 *         extends the PaginationCursor.
	 */
	public PaginationCursor createListPage(List<?> elements, int pageSize);


    /**
     * generates a PoRuntimeException which orginiates out of a Division by Zero Exception
     */
    public void testThrowException();
    
    /**
     * get a 32 char UniversalID in Hexform
     * like Hibernate does in the UUID id function
     * 
     * @return String
     */
    public String generateUID();
    
	/**
	 * reassosiates the given object with the database (hibernate session)
	 * making the hibernate object a persistent object again
	 * 
	 * @param obj
	 */
	public void refreshObject(Object obj);
	
	/**
	 * evicts specified hibernate model object
	 * (takes it out of the session resulting in a detached object)
	 * 
	 * @param obj
	 */
	public void evictObject(Object obj);
	
	
	/**
	 * @param group
	 * @return a <code>List</code> of <code>String</code>'s, 
	 * representing the email adresses of the assigned <code>PoPerson</code>'s
	 * 
	 */
	public List<String> getEmailAdresses(PoGroup group);
	
	public String generateTemporaryAccessPermission(String userName, String sourceActionURL, String targetActionURL);
	
	public boolean hasTemporaryAccess(String userName, String sourceActionURL, String targetActionURL, String securityToken);
	
	public Thread decorateWithSession(Thread thread);
	
	public Thread decorateWithSession(Thread thread, boolean useCurrentRequestSession);
	

	
}