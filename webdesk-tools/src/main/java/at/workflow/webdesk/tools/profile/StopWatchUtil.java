package at.workflow.webdesk.tools.profile;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Performance measurement with Apache StopWatch and Logger - convenience methods.
 * 
 * <pre>
 * 	StopWatch watch = StopWatchUtil.startPerformanceWatch("started PLEASE SAY WHAT" , logger);
 * 	....;	// long lasting work
 * 	StopWatchUtil.performanceWatch(watch, "finished PLEASE SAY WHAT" , logger);
 * </pre>
 * 
 * @author fritzberger 26.02.2011
 * 
 * Added possibility to set Logging-Level also.
 * @author ggruber, 24.2.2012
 */
public final class StopWatchUtil {
	
	
	private static final Level DEFAULT_LEVEL = Level.TRACE;

	public static StopWatch startPerformanceWatch(String startMessage, Logger logger) {
		return StopWatchUtil.startPerformanceWatch(startMessage, logger, DEFAULT_LEVEL);
	}
	
	/**
	 * Outputs passed message to <code>logger</code> with passed level and starts stop watch.
	 */
	public static StopWatch startPerformanceWatch(String startMessage, Logger logger, Level level)	{
		StopWatch watch = new StopWatch();
		performanceState(watch, startMessage, logger, level);
		return watch;
	}
	
	
	public static void performanceWatch(StopWatch watch, String progressMessage, Logger logger) {
		StopWatchUtil.performanceWatch(watch, progressMessage, logger, DEFAULT_LEVEL);
	}
	
	/**
	 * Outputs passed message to <code>logger</code> with passed level and appends consumed millis since
	 * previous call (or startPerformanceWatch call). Restarts stop watch.
	 */
	public static void performanceWatch(StopWatch watch, String progressMessage, Logger logger, Level level)	{
		performanceState(watch, progressMessage+" needed millis: "+watch.getTime(), logger, level);
	}
	
	private static void performanceState(StopWatch watch, String message, Logger logger, Level level)	{
		if (level.equals( DEFAULT_LEVEL )) {
			logger.trace(message);
		} else if (level.equals( Level.DEBUG )) {
			logger.debug(message);
		} else if (level.equals( Level.INFO )) {
			logger.info(message);
		}
		watch.reset(); 
		watch.start();
	}

	private StopWatchUtil() {}	// do not instantiate

}
