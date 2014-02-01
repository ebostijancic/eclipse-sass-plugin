package at.workflow.webdesk.tools.cache;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import at.workflow.webdesk.tools.api.ClusterConfig;

/**
 * This Cache listener 'listens' for Cache events from other cluster nodes which change
 * Elements whose key starts with 'CacheCommunicationRequest_'
 * 
 * Processing will be done, if the elementKey starts with 'CacheCommunicationRequest_' and those
 * not end with the name of the current cluster node. (To avoid processing of ServiceCalls which have
 * been posted by this node)
 * 
 * If the element's value is of type <code>CacheCommunicationServiceCall</code> it will be extracted and
 * another check will be done upon the ServiceCalls's ID to be sure it is not contained in the list
 * of ServiceCalls which have been fired by the current cluster node.
 * 
 * After those checks, the Call will be processed. (Gathering the Spring Bean, discovering the right method
 * and calling it!) While calling a thread Local variable by Log4j will be marked (MDC 'CacheCommunicationRequestFromSameThread')
 * for being able to trace recursive calls within the same thread.
 * 
 * @author hentner, ggruber
 */
public class CacheCommunicationListener implements CacheEventListener, ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	
	private Logger logger = Logger.getLogger(this.getClass());

	private ClusterConfig clusterConfig;
	
	public void setClusterConfig(ClusterConfig clusterConfig) {
		this.clusterConfig = clusterConfig;
	}

	@Override
	public void notifyElementExpired(Ehcache cache, Element element) {
	}

	@Override
	public void notifyElementPut(Ehcache cache, Element element)
			throws CacheException {
		doAction(cache, element);
	}

	@Override
	public void notifyElementRemoved(Ehcache cache, Element element)
			throws CacheException {
	}

	@Override
	public void notifyElementUpdated(Ehcache cache, Element element)
			throws CacheException {
		doAction(cache,element);
	}

	private void doAction(Ehcache cache, Element element) {
		
		String elementKey = element.getKey().toString();
		String currentClusterNode = clusterConfig.getClusterNode();
		
		if (elementKey.startsWith(CacheCommunicationInterceptor.CACHECOMUREQ) && !elementKey.endsWith(currentClusterNode) &&
				element.getValue() instanceof CacheCommunicationServiceCall ) {
			
			logger.debug("[Node:" + currentClusterNode + "] Cache communication received");
			// this communication is meant for me
			CacheCommunicationServiceCall call = (CacheCommunicationServiceCall) element.getValue();
			
			// be sure this call was not done by ourselfs
			// doublecheck with set of ids saved inside a second cache.
			if (!CacheCommunicationHelper.getRequestedIds(cache, currentClusterNode).contains(call.getId())) {
				
				logger.info("[Node:" + currentClusterNode + "] " + call.getOriginatingNode() + " executed a Servicecall which has to be propagated. I'm following...");
				logger.info("[Node:" + currentClusterNode + "] call " + call.getBeanName() + "." + call.getMethodName());
				
				// mark current thread
				// to process service call
				MDC.put(CacheCommunicationInterceptor.MDC_CACHEATTR, true);
				
				CacheCommunicationHelper.addRequestedId(cache, currentClusterNode,call.getId());
				
				doServiceCall(call);
				
				// remove mark
				MDC.remove(CacheCommunicationInterceptor.MDC_CACHEATTR);
				
				logger.info("[Node:" + currentClusterNode + "] finished calling " + call.getBeanName() + "." + call.getMethodName());
				
			}
		}
		
	}
	
	private void doServiceCall(CacheCommunicationServiceCall call) {
		Object beanService = this.applicationContext.getBean(call.getBeanName());
		Class<?> serviceIfClass = at.workflow.webdesk.tools.ReflectionUtils.findServiceInterface(beanService);
		Class<?>[] paramTypes = at.workflow.webdesk.tools.ReflectionUtils.convertToParamTypes(call.getArgs());
		
		Method method = ReflectionUtils.findMethod(serviceIfClass, call.getMethodName(), paramTypes);
		
		if (method==null && paramTypes.length == 1 && paramTypes[0].getGenericInterfaces().length>0) {
		
			// find method with same signature
			// but parameter with interface 
			for (Type interf : paramTypes[0].getGenericInterfaces()) {
				if (interf instanceof Class<?>) {
					Class<?>[] newParamTypes = at.workflow.webdesk.tools.ReflectionUtils.convertToParamTypes(call.getArgs());
					newParamTypes[0] = (Class<?>) interf;
					method = ReflectionUtils.findMethod(serviceIfClass, call.getMethodName(), newParamTypes);
					if (method!=null)
						break;
				}
			}
			
			// find method with same signature
			// but parameter with class in type hierarchy (up) 
			while (method == null && paramTypes[0].getSuperclass()!=null) {
				paramTypes[0] = paramTypes[0].getSuperclass();
				method = ReflectionUtils.findMethod(serviceIfClass, call.getMethodName(), paramTypes);
				
				if (method == null) {
					for (Type interf : paramTypes[0].getGenericInterfaces()) {
						if (interf instanceof Class<?> || interf instanceof ParameterizedType) {
							Class<?>[] newParamTypes = at.workflow.webdesk.tools.ReflectionUtils.convertToParamTypes(call.getArgs());
							
							if (interf instanceof ParameterizedType) {
								newParamTypes[0] = (Class<?>)((ParameterizedType) interf).getRawType();
							} else {
								newParamTypes[0] = (Class<?>) interf;
							}
							method = ReflectionUtils.findMethod(serviceIfClass, call.getMethodName(), newParamTypes);
							if (method!=null)
								break;
						}
					}
				}
			}
		}

		
		if (method!=null) {
			try {
				ReflectionUtils.invokeMethod(method, beanService, call.getArgs());
			} catch (Exception e) {
				this.logger.warn("problems to call method with name '" + call.getMethodName() + "' in Service=" + call.getBeanName() , e);
			}
		} else {
			this.logger.warn("Could not find method with name '" + call.getMethodName() + "' in Service=" + call.getBeanName());
		}
	}

	@Override
	public void dispose() {
		
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException  {
		throw new CloneNotSupportedException();
	}

	@Override
	public void notifyElementEvicted(Ehcache arg0, Element arg1) {
		
	}

	@Override
	public void notifyRemoveAll(Ehcache arg0) {
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
