package at.workflow.webdesk.tools.testing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * This is the base class for unit-tests that want an automatic rollback of their
 * database test data that were inserted during any test-method. This helps keeping
 * unit tests independent of their execution order and other circumstances in which
 * they are carried out.
 * <p/>
 * After each testMethod() a rollback is performed, to remove all database contents
 * created or modified by that testMethod.
 * <p/>
 * <p/>
 * There are three transactions:
 * one before the testMethod, one during the testMethod, and one after the testMethod.
 * When overriding onSetUpBeforeDataGeneration() you are in the first transaction.
 * When overriding onSetUpAfterDataGeneration() you are in the second transaction.
 * When overriding onTearDownAfterTransaction() you are in the third transaction.
 * <p/>
 * Try to avoid overriding any of the onSetUp or onTearDown methods.
 * Try to generate all data (needed for the test) in the testMethod() itself,
 * e.g. by writing private createXxxData() methods. All these data will be rolled back
 * and are no problem for other unit tests.
 * <p/>
 * Override onSetUpBeforeDataGeneration() and use committed data only if absolutely inevitable.
 * Global data MUST be removed explicitly after each testMethod() by overriding
 * <code>onTearDownAfterTransaction</code>. 
 * <p/>
 * See also WTestSpringHsqlDbTestCaseTransactionalBehaviour.
 */
public abstract class AbstractTransactionalSpringHsqlDbTestCase extends AbstractTransactionalSpringContextTests {
	
	private static boolean dataImported = false;
	
	private boolean rollBackGeneratedDataOnTearDown = true;

	/**
	 * Call this with false when no rollback of data created
	 * by dataGenerators should happen after test methods.
	 */
	protected void setRollBackGeneratedDataOnTearDown(boolean rollBackGeneratedDataOnTearDown) {
		this.rollBackGeneratedDataOnTearDown = rollBackGeneratedDataOnTearDown;
	}

	/**
	 * See getConfigLocations().
	 * Override this method to specify additional Spring bean specs
	 * in addition to the testApplicationContext.xml residing in the
	 * package of the test class.
	 */
	protected String[] getAdditionalConfigLocations() {
		return new String[] {};
	}

	/**
	 * Specifies packageName + "/testApplicationContext.xml" as basic Spring application context.
	 * Use <code>getAdditionalConfigLocations()</code> to add application contexts.
	 * Be aware that every time you do this the Spring application context will be shutdown and
	 * created again, and the old context will be restarted afterwards.
	 */
	@Override
	protected final String[] getConfigLocations() {
		String pckName = getClass().getPackage().getName().replace('.', '/');
		String[] configLocations = { pckName + "/testApplicationContext.xml" };
		return merge(configLocations, getAdditionalConfigLocations());
	}
	
	/** Called before the transaction for current test method execution starts. */
	@Override
	protected final void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		
		// custom handler
		onSetUpBeforeDataGeneration();
	}

	/** Shortcut for <code>applicationContext.getBean(name)</code>. */
	protected final Object getBean(String name)	{
		return applicationContext.getBean(name);
	}
	
	/** Overridden to ensure that the static global WebdeskApplicationContext holds this test's application context. */
	@Override
	protected void onSetUp() throws Exception {
		assertNotNull("Makes no sense to set a null applicationContext into global context!", applicationContext);
		WebdeskApplicationContext.setApplicationContext(applicationContext);
		// ... for easier migration of old testcases!
		
		super.onSetUp();
	}
	
	/**
	 * Called before the transaction for current test method execution starts.
	 * Override this method to provide custom setup code.
	 * note that data written to the database will *NOT* be rolled back upon
	 * completion of each testcase
	 * 
	 * @throws Exception
	 */
	protected void onSetUpBeforeDataGeneration() throws Exception {
	}

	/** Called after the transaction for current test method execution was started. */
	@Override
	protected final void onSetUpInTransaction() throws Exception {
		
		// must clear caches, else Hibernate 2nd level cache might read objects that are not in database!
		CacheManager cacheManager = (CacheManager) applicationContext.getBean("CacheManager");
		cacheManager.clearAll();
		
		if (logger.isDebugEnabled())
			logger.debug("onSetUpInTransaction()");
		
		DataGenerator[] dataGenerators = getDataGenerators();
		
		if (dataGenerators != null && dataGenerators.length > 0 && (rollBackGeneratedDataOnTearDown || ! dataImported)) {
			logger.info("importing data from "+dataGenerators.length+" generators ...");
			
			dataImported = true;
			
			for (int i = 0; i < dataGenerators.length; i++) {
				dataGenerators[i].create(applicationContext);
			}
			
			if (false == rollBackGeneratedDataOnTearDown)
				commitToDatabaseAndRestartTransaction();
			else
				logger.info("Pending rollback of transaction, rollBackGeneratedDataOnTearDown = "+rollBackGeneratedDataOnTearDown);
		}
		
		super.onSetUpInTransaction();
		
		// custom handler
		onSetUpAfterDataGeneration();
	}
	
	/**
	 * This is called by onSetup() only when transactionManager != null and transactionDefinition != null.
	 * Called after the transaction for current test method execution was started,
	 * and after data were read from XML DataGenerators.
	 * Override this method to add further data.
	 * Note that data written to the database will be rolled back upon
	 * completion of each testcase!
	 */
	protected void onSetUpAfterDataGeneration() throws Exception {
	}

	/**
	 * This is called by onSetup() only when transactionManager != null and transactionDefinition != null.
	 * Always returns null. Override this to provide DataGenerators that create test data in database.
	 * @return the array of DataGenerator loaders that are to be processed for this test case.
	 */
	protected DataGenerator[] getDataGenerators() {
		return null;
	}
	
	/** Overridden to use an in-memory HSQL database. */
	@Override
	protected final void prepareApplicationContext(GenericApplicationContext context) {
		logger.info("prepareApplicationContext, context = "+context);
		
    	// init logger
		TestingHelper.configureLogging();
        
        Properties props = getTestDatabaseConnectionProperties();
		
		PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
		
		ppc.setProperties(props);
		ppc.setIgnoreUnresolvablePlaceholders(true);
		
		context.addBeanFactoryPostProcessor(ppc);
		
		super.prepareApplicationContext(context);
	}

	/**
	 * This implementation delegates to static TestingHelper.provideTestHsqlDb().
	 * To be overridden for CAREFUL use of this unit-test with other databases.
	 * Mind that the build-server (Hudson) launching this test will have only HSQL-DB!
	 * Expected return is something like:
	 * <pre>
		Properties properties = new Properties();

		properties.setProperty(Environment.DRIVER, "org.hsqldb.jdbcDriver");
		properties.setProperty(Environment.URL, "jdbc:hsqldb:file:"+dir+"/webdesk");
		properties.setProperty(Environment.USER, "sa");
		properties.setProperty(Environment.PASS, "");
		properties.setProperty(Environment.DIALECT, "org.hibernate.dialect.HSQLDialect");

		properties.setProperty("shark.connection.driver_class", "org.hsqldb.jdbcDriver");
		properties.setProperty("shark.connection.url", "jdbc:hsqldb:file:"+dir+"/shark");
		properties.setProperty("shark.connection.username", "sa");
		properties.setProperty("shark.connection.password", "");
		properties.setProperty("shark.db_loader_job", "hsql");

		properties.setProperty("webdesk.isDistributed", "false");
		properties.setProperty(WEBDESK_LICENCE_CHECK_DISABLED, "true");
        
		return properties;
	 * </pre>
	 * @return the properties to use for hibernate database access.
	 */
	protected Properties getTestDatabaseConnectionProperties() {
		return TestingHelper.provideTestHsqlDb();
	}

	/**
	 * This is to be used CAREFULLY as return from getTestDatabaseConnectionProperties()
	 * for unit-tests that should run with other databases.
	 * @return the database connection that is in your local webdesk.properties file!
	 */
	protected final Properties getLocalDeveloperDatabaseConnectionProperties() {
		final String localWebdeskProperties = "../../webdesk-webapp/src/main/resources/webdesk.properties";
		try {
			Properties properties = new Properties();
			InputStream in = new FileInputStream(localWebdeskProperties);
			properties.load(in);
			return properties;
		}
		catch (IOException e) {
			throw new RuntimeException("Loading "+localWebdeskProperties+" failed: "+e);
		}
	}

	/**
	 * This would commit currently launched SQL statements (be sure to flush Hibernate before!)
	 * and start another transaction (which then would be rollback'ed at end of test method).
	 * <p/>
	 * Never use this method inside a WTest* unit test
	 * as we assume all data is rolled back upon completion!!!
	 */
	protected void commitToDatabaseAndRestartTransaction() {
		logger.info("Committing transaction, rollBackGeneratedDataOnTearDown = "+rollBackGeneratedDataOnTearDown);
		// make sure hibernate does not fool us...
		setComplete();
		endTransaction();
		startNewTransaction();
	}


	/** Overridden to call <code>setAutowireMode(AUTOWIRE_BY_NAME)</code>. */
	@Override
	protected void prepareTestInstance() throws Exception {
		setAutowireMode(AUTOWIRE_BY_NAME);
		super.prepareTestInstance();
	}

	private String[] merge(String[] arg1, String[] arg2)	{
		Collection<String> ret = new ArrayList<String>();
		ret.addAll(Arrays.asList(arg1));
		ret.addAll(Arrays.asList(arg2));
		return ret.toArray(new String[ret.size()]);
	}

	/**
	 * This was needed to fix assertions that were thrown because a removed node
	 * either was not found when looking for historicized nodes, or was found when
	 * looking for valid nodes.
	 * Mind that e.g. MySql database stores no millis.
	 * @return waits a second and return the current system date then.
	 */
	protected Date waitASecond() {
		final int SLEEP_MILLIS = 1000;	// have a sleep for MySql
		final long targetTime = System.currentTimeMillis() + SLEEP_MILLIS;
		
		try { Thread.sleep(SLEEP_MILLIS); } catch (InterruptedException e) {}
		
		// wait until System.currentTimeMillis() is bigger than target time
		while (targetTime > System.currentTimeMillis())
			try { Thread.sleep(1); } catch (InterruptedException e) {}
		
		return DateTools.now();
	}

}
