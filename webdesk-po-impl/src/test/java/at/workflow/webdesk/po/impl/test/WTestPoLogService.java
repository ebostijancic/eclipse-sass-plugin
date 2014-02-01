package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.PoLogService;
import at.workflow.webdesk.po.model.PoLog;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * TODO continue this test, it covers just three methods by the time.
 * 
 * @author fritzberger 25.11.2010
 */
public class WTestPoLogService extends AbstractTransactionalSpringHsqlDbTestCase {

	private static PoLogService logService;
	
	@Override
	protected void onSetUpBeforeDataGeneration() throws Exception {
		super.onSetUpBeforeDataGeneration();
		
		if (logService == null)
			logService = (PoLogService) getBean("PoLogService");
	}

	public void testLog() {
		final int TESTCOUNT = 10;
		PoLog log = null;
		for (int i = 0; i < TESTCOUNT; i++)	{
			log = new PoLog();
			log.setActionName("test action name "+i);
			log.setContinuationId("TestContinuationId");
			logService.saveLog(log);
		}
		assertEquals(TESTCOUNT - 1, logService.findLogsInSameContinuation(log).size());
		
		logService.deleteAllLogs();
		assertEquals(0, logService.findLogsInSameContinuation(log).size());
    }

}
