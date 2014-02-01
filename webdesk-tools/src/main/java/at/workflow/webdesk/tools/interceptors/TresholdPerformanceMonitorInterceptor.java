package at.workflow.webdesk.tools.interceptors;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.aop.interceptor.PerformanceMonitorInterceptor;
import org.springframework.util.StopWatch;

/**
 * Extends PerformanceMonitorInterceptor by being able to
 * set a treshold in ms.
 * 
 * @author ggruber
 *
 */
@SuppressWarnings("serial")
public class TresholdPerformanceMonitorInterceptor extends
		PerformanceMonitorInterceptor {

	private long treshold = 50;
	
	protected Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {
		String name = createInvocationTraceName(invocation);
		StopWatch stopWatch = new StopWatch(name);
		stopWatch.start(name);
		try {
			return invocation.proceed();
		}
		finally {
			stopWatch.stop();
			
			if (stopWatch.getTotalTimeMillis()>treshold)
				logger.trace(stopWatch.shortSummary());
		}
	}

	public void setTreshold(int treshold) {
		this.treshold = treshold;
	}
}
