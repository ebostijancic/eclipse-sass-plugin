package at.workflow.webdesk.tools.interceptors;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;

/**
 * This interceptor intercepts specific method calls and is able to retry the method
 * call x times, if a specific exception occurs. 
 * 
 * Can be useful to retry a transactional exception resulted from a deadlock, where
 * a retry has a chance to be successful.
 * 
 * @author ggruber
 *
 */
public class ExceptionInterceptor implements MethodInterceptor {

	protected Logger logger = Logger.getLogger(this.getClass());
	
	private int noOfRetries=5;
	private List<String> exceptionsToIntercept = new ArrayList<String>();
	private int millisToWaitAfterException=25;
	private List<String> methodsToIntercept = new ArrayList<String>();
	
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		
		if (methodsToIntercept.size()>0 && (methodsToIntercept.get(0).equals("*") || methodsToIntercept.contains(methodInvocation.getMethod().getName())))
			return doIntercept(methodInvocation);
		else
			return methodInvocation.proceed();
	}
	
	private Object doIntercept(MethodInvocation methodInvocation) throws Throwable {
		Object ret=null;
		try {
			ret = methodInvocation.proceed();
		} catch (Exception e) {
			// decide wheter to retry
			
			if (containsException(e, exceptionsToIntercept) && noOfRetries>0) {
				this.logger.info("caught exception with interceptor, retrying method...");
				ret = retryInvoke(methodInvocation);
			} else {
				// rethrow
				if (e instanceof RuntimeException) {
					throw new RuntimeException(e);
				}
				throw new Exception(e);
			}
			
		}
		// normal end
		return ret;
	}
	
	private boolean containsException(Throwable e, List<String> exceptionNames) {
		
		if (exceptionsToIntercept.contains(e.getClass().getName()))
			return true;
		
		while (e.getCause()!=null) {
			e=e.getCause();
			if (exceptionsToIntercept.contains(e.getClass().getName()))
				return true;
			
		}
		
		return false;
		
	}
	
	
	private Object retryInvoke(MethodInvocation methodInvocation) throws Throwable {
		Object ret = null;
		boolean successful=false;
		Exception lastEx=null;
		int i=0;
		while (i<noOfRetries && !successful) {
			try {
				i++;
				this.logger.info("retry invocation of method '" + methodInvocation.getMethod().getName() + "' " + i + ".time...");
				ret = methodInvocation.proceed();
				successful=true;
			} catch (Exception e) {
				
				// rethrow other exceptions
				if (!containsException(e, exceptionsToIntercept)) {
					if (e instanceof RuntimeException) {
						throw new RuntimeException(e);
					}
					throw new Exception(e);
				}
				
				// randomize timeout
				int timeout = RandomUtils.nextInt(millisToWaitAfterException);
				Thread.sleep(timeout);
				
				// remember last exception
				lastEx = e;
			}
		}
		if (successful) {
			// successfull return it!
			return ret;
		} else {
			// retried it n times, but it did not work :-(
			if (lastEx instanceof RuntimeException) {
				throw new RuntimeException(lastEx);
			}
			throw new Exception(lastEx);
		}
	}
	

	public void setNoOfRetries(int noOfRetries) {
		this.noOfRetries = noOfRetries;
	}

	public void setExceptionsToIntercept(List<String> exceptionsToIntercept) {
		this.exceptionsToIntercept = exceptionsToIntercept;
	}

	public void setMillisToWaitAfterException(int millisToWaitAfterException) {
		this.millisToWaitAfterException = millisToWaitAfterException;
	}

	public void setMethodsToIntercept(List<String> methodsToIntercept) {
		this.methodsToIntercept = methodsToIntercept;
	}

}
