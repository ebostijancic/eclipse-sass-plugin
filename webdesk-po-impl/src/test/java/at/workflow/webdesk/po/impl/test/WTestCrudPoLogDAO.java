package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.daos.PoLogDAO;
import at.workflow.webdesk.po.model.PoLog;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoLogDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoLog> {

	/**
	 * As it seems that log entries are written by other unit tests,
	 * it depends on the order of test if there are entries or not.
	 * Remove them all here, cleanup for this test.
	 */
	@Override
	protected void onSetUpBeforeDataGeneration() throws Exception {
		PoLogDAO logDao = (PoLogDAO) getBean("PoLogDAO");
		logDao.deleteAllLogs();
		super.onSetUpBeforeDataGeneration();
	}
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoLog.class);
		super.onSetUpAfterDataGeneration();
	}
	
	/** Overridden to return the session factory name for the logging DAO. */
	@Override
	protected String getSessionFactoryBeanName()	{
		return "logSessionFactory";
	}

}
