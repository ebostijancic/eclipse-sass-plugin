package at.workflow.webdesk.tools.testing;

import net.sf.ehcache.CacheManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This class should have been named <b>AbstractNonTransactionalSpringHsqlDbTestCase</b>.
 * It provides no transactions at all. This is like JDBC access with a database
 * running in auto-commit mode (HsqlDb does, that's where this class name comes from).
 * When calling a service from here, the service will create exactly one transaction per call.
 * <p/>
 * This is the base class for unit-tests that want every database statement result to
 * stay persistent, without the need to commit any transaction. Mind that Hibernate buffers
 * database statements and executes them only when it becomes necessary (e.g. a query is launched).
 * So do not expect e.g. a <code>pojo.setName(name)</code> to be immediately in database
 * when using this test case.
 * <p/>
 * After each test-method an explicit cleanup must be performed by overriding
 * <code>clearUsedTables()</code>. This will be executed on every test-method's setUp()
 * and tearDown(), in tearDown() secured via a "try - finally" clause (is guaranteed to be called).
 */
public abstract class AbstractAutoCommitSpringHsqlDbTestCase extends AbstractTransactionalSpringHsqlDbTestCase {

	private SessionFactory sessionFactory;
	private Session session;
	
    /** Sub-classes MUST implement this to clean the database tables they have been using. */
    protected abstract void clearUsedTables();

    /**
     * Override this and return true if you want to create a Hibernate session and keep it open
     * during a test method, this would enable lazy loading of references to other persistent objects.
     */
	protected boolean createAndKeepOpenPersistenceSession() {
		return false;
	}
    
    /** Override this to use another SessionFactory bean name than "sessionFactory". */
    protected String getSessionFactoryBeanName() {
    	return "sessionFactory";
    }
    
	/** Overridden to prevent abuse: always throws exception. */
	@Override
	protected final void commitToDatabaseAndRestartTransaction() {
		throw new UnsupportedOperationException("This is a unit test that does not support transactions!");
	}
	
	@Override
	protected final void startNewTransaction() throws TransactionException {
		throw new UnsupportedOperationException("This is a unit test that does not support transactions!");
	}

	@Override
	protected final void endTransaction() {
		throw new UnsupportedOperationException("This is a unit test that does not support transactions!");
	}
	
	/**
	 * Call this when keepPersistenceSessionOpen() returns true to close old session and open a new one.
	 * This is for provoking LazyInitializationException.
	 * TODO does not work, no LazyInitializationException.
	 */ 
	protected void restartSession()	{
		closeSession();
		openSession();
	}
	
	/**
	 * Overridden to clear relevant tables and caches.
	 * When 'Hibernate-session should be kept open' it creates a Session.
	 */
    @Override
    protected void onSetUp() throws Exception {
    	super.onSetUp();
    	
		clearUsedTablesAndAllCaches();
		
		if (createAndKeepOpenPersistenceSession()) {
			openSession();
		}
    }

    /**
     * Overridden to prevent transactional behaviour be calling <code>preventTransaction()</code>
     * (implemented like Spring recommends).
     */
    @Override
    public final void runBare() throws Throwable {
    	preventTransaction();
    	super.runBare();
    }
    
    /**
     * Overridden to be final as overriding this makes no sense, it will NOT BE CALLED.
     * Please override onSetUp(). 
     */
    @Override
    protected final void onSetUpBeforeDataGeneration() throws Exception {
    	throw new RuntimeException("onSetUpBeforeDataGeneration has been made final to not be called on a AbstractAutoCommitSpringHsqlDbTestCase. If you see this exception, it is time to further refactor AbstractAutoCommitSpringHsqlDbTestCase, maybe by not deriving it from AbstractTransactionalSpringHsqlDbTestCase");
    }
    
    /**
     * Overridden to be final as overriding this makes no sense, it will NOT BE CALLED.
     * Please override onSetUp(). 
     */
	@Override
	protected final void onSetUpAfterDataGeneration() throws Exception {
    	throw new RuntimeException("onSetUpAfterDataGeneration has been made final to not be called on a AbstractAutoCommitSpringHsqlDbTestCase. If you see this exception, it is time to further refactor AbstractAutoCommitSpringHsqlDbTestCase, maybe by not deriving it from AbstractTransactionalSpringHsqlDbTestCase");
	}
	
    /**
     * Overridden to be final as overriding this makes no sense, it will NOT BE CALLED.
     * Please override onSetUp(). 
     */
	@Override
	protected final DataGenerator[] getDataGenerators() {
    	throw new RuntimeException("getDataGenerators has been made final to not be called on a AbstractAutoCommitSpringHsqlDbTestCase. If you see this exception, it is time to further refactor AbstractAutoCommitSpringHsqlDbTestCase, maybe by not deriving it from AbstractTransactionalSpringHsqlDbTestCase");
	}
	
	/**
	 * Overridden to clear relevant tables and caches.
	 * When 'Hibernate-session should be kept open' it now closes the session.
	 */
    @Override
    protected final void onTearDown() throws Exception {
    	// remove all created data to be sure next tests can run with initial-only database data!
    	clearUsedTablesAndAllCaches();
    	
    	if (createAndKeepOpenPersistenceSession()) {
        	closeSession();
    	}
    }

	private void openSession() {
		closeSession();	// is safe against nulls
		
		sessionFactory = (SessionFactory) getBean(getSessionFactoryBeanName()); 
		session = SessionFactoryUtils.getSession(sessionFactory, true); 
		try {
			TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
		} catch (Exception e) {
			logger.warn("Could not bind session to thread. If run with a testsuite this is not an error", e);
		}
	}
	
	private void closeSession() {
		if (session != null)	{
			session.close();
			session = null;
		}		
		if (sessionFactory != null)	{
			TransactionSynchronizationManager.unbindResource(sessionFactory);
			sessionFactory = null;
		}
	}
    
	private void clearUsedTablesAndAllCaches() {
    	clearUsedTables();
		TestingHelper.clearAllCaches((CacheManager) getBean("CacheManager"));
    }

}
