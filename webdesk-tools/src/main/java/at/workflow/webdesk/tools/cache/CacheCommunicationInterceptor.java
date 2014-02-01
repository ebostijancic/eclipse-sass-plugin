package at.workflow.webdesk.tools.cache;

import java.util.Date;

import net.sf.ehcache.Cache;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.context.ApplicationContext;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.tools.api.ClusterConfig;

/**
 * This Cache interceptor acts as communication medium between
 * cluster nodes. 
 * 
 * It 'posts' a CacheCommunicationServiceCall instance into a special 
 * communcationCache. The cacheKey depends on the originating cluster
 * node, in order to have no interference with other cluster members posting
 * requests to the same 'communication wire'.
 * 
 * @author hentner
 */
public class CacheCommunicationInterceptor implements MethodInterceptor {
	public final static String CACHECOMUREQ = "CacheCommunicationRequest_";
	public final static String CACHECOMUREQIDS = "CacheCommunicationRequestedIds_";
	public final static String MDC_CACHEATTR = "CacheCommunicationRequestFromSameThread";


	private Logger logger = Logger.getLogger(this.getClass());

	private Cache communicationCache;

	private ClusterConfig clusterConfig;

	public void setClusterConfig(ClusterConfig clusterConfig) {
		this.clusterConfig = clusterConfig;
	}

	public void setCommunicationCache(Cache communicationCache) {
		this.communicationCache = communicationCache;
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {

		boolean callRunsInSameThread = (Boolean)MDC.get(MDC_CACHEATTR) == Boolean.TRUE;

		if (clusterConfig.isDistributed() && !callRunsInSameThread ) {
			// mark current thread
			// to process service call
			MDC.put(MDC_CACHEATTR, true);
		}


		Object ret = methodInvocation.proceed();

		if (clusterConfig.isDistributed() && !callRunsInSameThread )  {

			String currentClusterNode = clusterConfig.getClusterNode();
			logger.debug("Cache Communication starts on Node " + currentClusterNode);

			String id = new Long(new Date().getTime()).toString();
			String beanName = findServiceBeanNameForImplementingClass(methodInvocation.getMethod().getDeclaringClass());

			if (beanName!=null) {
				CacheCommunicationServiceCall call = new CacheCommunicationServiceCall(id, beanName , methodInvocation.getMethod().getName(), methodInvocation.getArguments(), currentClusterNode);

				logger.info("Send Cache-Communication Servicecall on Node " + currentClusterNode + " for Execution of " + call.getBeanName() + "." + call.getMethodName());

				CacheCommunicationHelper.postServiceCall(communicationCache, currentClusterNode, call);

			}

			// remove mark
			MDC.remove(MDC_CACHEATTR);
		}

		return ret;
	}


	private String findServiceBeanNameForImplementingClass(Class<?> clazz) {
		ApplicationContext appCtx = WebdeskApplicationContext.getApplicationContext();
		String[] beanNames = appCtx.getBeanNamesForType(clazz, false, false);
		for (String beanName : beanNames) {
			if (beanName.endsWith("Service") && !beanName.startsWith("Cached")) {
				return beanName;
			}
		}
		return null;
	}

}
