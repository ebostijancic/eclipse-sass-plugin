package at.workflow.webdesk.tools.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.tools.BeanReflectUtil;
import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * The basic unit test for DAO tests.
 * Implements the CRUD test method by calling insert, update, delete and loadAll (upon the DAO).
 * Provides the infrastructure for dynamically created test data and ignored fields.
 * 
 * @author sdzuban
 * @author fritzberger, Oct 2010 (completely rewritten)
 * 
 * @param <B> the database POJO to test.
 */
public abstract class AbstractCrudTestCase<B extends PersistentObject> extends AbstractTransactionalSpringHsqlDbTestCase {

	private static final Logger logger = Logger.getLogger(AbstractCrudTestCase.class);
	
	private Class<B> beanClass;
	private List<String> ignoreFields;
	private GenericDAO<B> dao;
	
	/** Ensures that the DAO is there. */
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		assert beanClass != null : "Bean class must be set before onSetUpAfterDataGeneration() !!!";
		setDao(getDaoName(beanClass));
	}

	/**
	 * The public JUnit test method.
	 * This tests insert/update/delete/load methods for the configured bean class.
	 * The according methods are found by reflection.
	 */
	public void testCRUD() throws Exception {
		assertNotNull("Bean class not provided", beanClass);
		if (dao == null)
			setDao(getDaoName(beanClass));

		final B bean = instantiateBean();
		
		// initialize the bean, e.g. set UID where it is not autogenerated
		initializeBean(bean);
		
		// first insert a bean to persistence
		provideInsertData(bean);
		insertTest(bean);
		
		// then update that bean
		provideUpdateData(bean);
		updateTest(bean);
		
		// load all bean from persistence
		loadAllTest();
		
		// delete the inserted bean
		deleteTest(bean);
	}
	
	/**
	 * This implementation does nothing.
	 * It is to be overridden by derivations that need to initialize the bean 
	 * before storing it to db, e.g. setting uid to bean without autogenerated uid.
	 * @param bean the bean about to be tested with DAO layer.
	 */
	protected void initializeBean(B bean)	{
	}
	
	
	/**
	 * This implementation does nothing.
	 * It is to be overridden by derivations that need to put test data for insert-operation into the test bean.
	 * @param bean the bean about to be tested with DAO layer.
	 */
	protected void provideInsertData(B bean)	{
	}
	
	/**
	 * This implementation does nothing.
	 * It is to be overridden by derivations that need to put test data for update-operation into the test bean.
	 * @param bean the bean about to be tested with DAO layer.
	 */
	protected void provideUpdateData(B bean) {
	}

	/** @return the bean class to be used in this test case. */
	protected final Class<B> getBeanClass()	{
		return beanClass;
	}
	
	/**
	 * Sets a bean class to be used in this test case.
	 * This MUST be called on TestCase.setUp() to initialize the bean class and the DAO.
	 */
	protected final void setBeanClass(Class<B> testCaseBeanClass) {
		logger.info("Initializing the bean to "+testCaseBeanClass);
		this.beanClass = testCaseBeanClass;
	}
	
	/**
	 * Sets the DAO with the passed name retrieved from Spring application-context.
	 * This is an optional call. When not called, the DAO name will be derived from the
	 * convention <i>simpleBeanClassName + "DAO"</i>.
	 */
	@SuppressWarnings("unchecked")
	protected final void setDao(String daoName) {
		dao = (GenericDAO<B>) applicationContext.getBean(daoName);
		assertNotNull("DAO must not be null, override getDaoName() when bean name does not match DAO name!", getDao());
	}

	/**
	 * Adds the passed names of Java fields (properties) to the ignored names in this test.
	 * Mind that "serialVersionUID" needs not to be added because this is ignored by default.
	 * @param fieldNames the names of the Java fields (properties) that should not be touched.
	 * @deprecated there are no more fields that have to be ignored when using <code>AbstractRandomdataProvidingTestCase</code>
	 */
	protected final void addIgnoreJavaFields(String[] fieldNames) {
		for (String s : fieldNames)
			addIgnoreJavaField(s);
	}

	/**
	 * Adds the passed name of a Java field (property) to the ignored names in this test.
	 * Mind that "serialVersionUID" needs not to be added because this is ignored by default.
	 * @param fieldNames the names of the Java fields (properties) that should not be touched.
	 * @deprecated there are no more fields that have to be ignored when using <code>AbstractRandomdataProvidingTestCase</code>
	 */
	protected final void addIgnoreJavaField(String fieldName) {
		ensureIgnoreList().add(fieldName);
	}
	
	/** @return true if the Java field of passed name is ignored by this test. */
	protected final boolean isIgnored(String fieldName)	{
		return ensureIgnoreList().contains(fieldName);
	}

	/** @return the List of properties in passed class, excluding UID field. */
	protected final List<BeanReflectUtil.BeanProperty> properties(Class<?> clazz)	{
		return properties(clazz, false);
	}
	
	/**
	 * For cases when the bean name does not match the DAO name, override this explicitly.
	 * @param clazz the bean class (persistence POJO) the DAO works for.
	 * @return the name of the DAO for the passed persistence POJO.
	 */
	protected String getDaoName(Class<?> clazz)	{
		assert clazz != null : "Can not find the DAO for a null POJO-class!";
		return clazz.getSimpleName()+"DAO";
	}
	
	/**
	 * Override this for another session factory than applicationContext.getBean("sessionFactory").
	 */
	protected String getSessionFactoryBeanName()	{
		return "sessionFactory";
	}
	
	
	
	/**
	 * The insert test creates a new bean, fills it with values, saves it,
	 * reads in the bean again and checks if all values have been persisted.
	 */
	private void insertTest(final B bean) throws Exception {
		final boolean checkUid = false;
		Map<String,Object> insertValues = getPropertyValues(bean, checkUid);

		logger.info("Insertion starts");
		save(bean);

		final String uid = bean.getUID();
		assertNotNull(uid);
		assertFalse("".equals(uid));
		
		final Object dbBean = readBeanFromDb(bean);
		assertNotNull("Null object returned from DAO after INSERT!", dbBean);
		checkProperties(dbBean, insertValues, checkUid);
	}
		
	/**
	 * The update test changes the persistent bean, saves the changes,
	 * reads in the bean again and checks if all values have been persisted.
	 */
	private void updateTest(final B bean) throws Exception {
		final boolean checkUid = true;
		Map<String,Object> updateValues = getPropertyValues(bean, checkUid);

		logger.info("Update starts");
		save(bean);

		final Object dbBean = readBeanFromDb(bean);
		assertNotNull("Null object returned from DAO after UPDATE!", dbBean);
		checkProperties(dbBean, updateValues, checkUid);
	}
	
	/**
	 * The load test creates a second bean, saves it, reads in all persistent
	 * beans and checks if there were two beans now.
	 */
	private void loadAllTest() throws Exception {
		// insert another bean
		B bean2 = instantiateBean();
		initializeBean(bean2);
		provideInsertData(bean2);
		save(bean2);
		
		// now there must be 2 records in database
		final int resultSize = loadAllTestResultSize();
		@SuppressWarnings("unchecked")
		Collection<B> objects = getDao().loadAll();
		assertEquals("Wrong number of beans read with loadAll()", resultSize, objects.size());
		
		for (Object object : objects) {
			assertNotNull("Null object returned from DAO", object);
			assertEquals("Wrong bean class returned from DAO", beanClass, object.getClass());
		}
	}
	
	/**
	 * Override this to return another load-all result size than 2.
	 * @return the number of objects a load-all call returns.
	 */
	protected int loadAllTestResultSize()	{
		return 2;
	}

	/**
	 * The delete test deletes the first created persistent bean,
	 * tries to read in the bean again and checks if null is returned.
	 */
	@SuppressWarnings("unchecked")
	protected final void deleteTest(B bean) throws Exception {
		logger.info("Deletion starts");
		getDao().delete(bean);
		// TODO interchanging following two lines would lead to failing test in cases where
		// records were read with hibernate.load (not hibernate.get) - review the flush-method!
		assertNull("Bean was not deleted", readBeanFromDb(bean));
		flushToPersistence();
	}
	
	
	@SuppressWarnings("unchecked")
	private void save(B bean)	{
		getDao().save(bean);
		flushToPersistence();
	}
	
	/** This flush() is enough to trigger database constraints. */
	protected final void flushToPersistence()	{
		//commitToDatabaseAndRestartTransaction();	// would cause exceptions because there is no transaction manager
		
		SessionFactory sf = (SessionFactory) applicationContext.getBean(getSessionFactoryBeanName());
		SessionFactoryUtils.getSession(sf, false).flush();
	}
	
	private List<String> ensureIgnoreList() {
		if (ignoreFields == null)	{
			ignoreFields = new ArrayList<String>();
			ignoreFields.add("serialVersionUID");
		}
		return ignoreFields;
	}
	
	/** @return the POJO with the UID that is in passed bean. */
	protected final Object readBeanFromDb(B bean) throws Exception	{
		return readBeanFromDb(bean.getUID());
	}
	
	protected final Object readBeanFromDb(String uid) throws Exception	{
		return readBeanFromDb(getDao(), uid);
	}
	
	@SuppressWarnings("rawtypes")
	protected final GenericDAO getDao()	{
		return dao;
	}
	
	@SuppressWarnings("rawtypes")
	protected final Object readBeanFromDb(GenericDAO dao, String uid) throws Exception	{
		try	{
			return dao.get(uid);
		}
		catch (HibernateObjectRetrievalFailureException e)	{
			if (e.getCause() == null || e.getCause().getClass() != ObjectNotFoundException.class)
				throw e;
			
			// ignore ObjectNotFoundException because some getters do not conform to get() specification
			// (= not throwing exception but returning null for non-existing UID)
			// TODO these non-conforming classes are
			//     PoFileDAOImpl
			//     PoMenuDAOImpl
			//     PoSystemMessageDAOImpl
			logger.warn("DAO get() method threw an exception, this does not conform to get() specification: "+dao.getClass(), e.getCause());
			return null;
		}
	}
	
	private  B instantiateBean() throws InstantiationException, IllegalAccessException	{
		return beanClass.newInstance();
	}

	private final List<BeanReflectUtil.BeanProperty> properties(boolean includeUid)	{
		return properties(getBeanClass(), includeUid);
	}
	
	private final List<BeanReflectUtil.BeanProperty> properties(Class<?> clazz, boolean includeUid)	{
		return BeanReflectUtil.properties(
					clazz,
					includeUid ? null : new String [] { "UID" },
					null);
	}
	
	private void checkProperties(Object bean, Map<String, Object> values, boolean includeUid) throws Exception {
		for (BeanReflectUtil.BeanProperty property : properties(includeUid))	{
			if (isIgnored(property.propertyName))
				continue;
			
			assertTrue("No value supplied for property "+property.propertyName, values.containsKey(property.propertyName));
			
			Object result = property.getter.invoke(bean, new Object[0]);
			Object value = values.get(property.propertyName);
			if (result instanceof Collection && value instanceof Collection && result.getClass() != value.getClass() &&
					result instanceof Map == false && value instanceof Map == false)	{
				value = new ArrayList<Object>((Collection<?>) value);
				result = new ArrayList<Object>((Collection<?>) result);
			}
			
			assertEquals(
				"Persistent value differs from test value for property '"+property.propertyName+"'",
				value, result);
		}
	}

	private Map<String,Object> getPropertyValues(Object bean, boolean includeUid) throws Exception {
		Map<String, Object> saver = new HashMap<String, Object>();
		for (BeanReflectUtil.BeanProperty property : properties(includeUid))	{
			if (isIgnored(property.propertyName))
				continue;
			
			final Object value = property.getter.invoke(bean, new Object[0]);
			saver.put(property.propertyName, value);
		}
		return saver;
	}

}