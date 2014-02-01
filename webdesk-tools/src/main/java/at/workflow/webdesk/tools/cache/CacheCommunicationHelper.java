package at.workflow.webdesk.tools.cache;

import java.util.HashSet;
import java.util.Set;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

public abstract class  CacheCommunicationHelper {

	
	@SuppressWarnings("unchecked")
	public static Set<String> getRequestedIds(Ehcache cache, String clusterNode) {
		
		Set<String> requestedIds = new HashSet<String>();
		Element cacheElement = getRequestedIdsCache(cache, clusterNode);
		if (cacheElement != null) {
			requestedIds = (Set<String>) cacheElement.getValue();
		}
		return requestedIds;
	}
	
	
	public static void addRequestedId(Ehcache cache, String clusterNode, String id) {
		Set<String> requestedIds = getRequestedIds(cache, clusterNode);
		requestedIds.add(id);
		String cacheKeyIds = CacheCommunicationInterceptor.CACHECOMUREQIDS + clusterNode;
		cache.put(new Element(cacheKeyIds, requestedIds));
	}
	
	public static Element getRequestedIdsCache(Ehcache cache, String clusterNode) {
		String cacheKeyIds = CacheCommunicationInterceptor.CACHECOMUREQIDS + clusterNode;
		return cache.get(cacheKeyIds);

	}
	
	public static CacheCommunicationServiceCall getExistingCallFromNode(Ehcache cache, String originatingClusterNode) {
		String cacheKey = CacheCommunicationInterceptor.CACHECOMUREQ + originatingClusterNode;
		
		Element elem = cache.get(cacheKey);
		
		if (elem!=null && elem.getValue() instanceof CacheCommunicationServiceCall)
			return (CacheCommunicationServiceCall) elem.getValue();
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static void postServiceCall(Ehcache cache, String originatingClusterNode, CacheCommunicationServiceCall call) {
		// put element into cache with unique key
		// send request over wire
		String cacheKey = CacheCommunicationInterceptor.CACHECOMUREQ + originatingClusterNode;
		cache.put(new Element(cacheKey, call));
		
		
		// send requested Ids over wire
		String cacheKeyIds = CacheCommunicationInterceptor.CACHECOMUREQIDS + originatingClusterNode;
		Set<String> requestedIds = new HashSet<String>();
		Element cacheElement = cache.get(cacheKeyIds);
		if (cacheElement != null) {
			requestedIds = (Set<String>) cacheElement.getValue();
		}
		requestedIds.add(call.getId());
		cache.put(new Element(cacheKeyIds, requestedIds,false, 5, 5));
	}
}
