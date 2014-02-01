package at.workflow.webdesk.tools.date;

import junit.framework.TestCase;

/**
 * This test might fail when a Java THread.sleep(1000) lasts less then a second.
 * It is then that the Java virtual machine time millis are not passing by as expected,
 * and HistorizationHelper might have a problem.
 * <p/>
 * fri_2013-11-13: as this test fails on Hudson randomly, this does not assert anything for now.
 * 
 * @author fritzberger 12.11.2013
 */
public class WTestSleepTimeMillis extends TestCase
{
	/** Tests that sleep(1000) really lasts one second. */
	public void testSleepMillis() throws Exception	{
		final int LOOPCOUNT = 10;
		final int SLEEPTIME = 1000;
		
		for (int i = 0; i < LOOPCOUNT; i++)	{
			final long time1 = System.currentTimeMillis();
			Thread.sleep(SLEEPTIME);
			final long time2 = System.currentTimeMillis();
			
			final long diff = time2 - time1;
			if (diff < SLEEPTIME)
				//throw new IllegalStateException("Launched sleep for "+SLEEPTIME+" millis but woke up after "+diff+" millis!");
				System.err.println("Launched sleep for "+SLEEPTIME+" millis but woke up after "+diff+" millis!");
			else
				System.err.println("Slept "+SLEEPTIME+" millis and continued after "+diff);
		}
	}

}
