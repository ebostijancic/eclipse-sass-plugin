package at.workflow.webdesk.tools.cache;

import java.io.IOException;
import java.util.Properties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.distribution.RMICacheReplicatorFactory;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.cocoon.configuration.Settings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.util.Assert;

import at.workflow.webdesk.tools.api.ClusterConfig;

/**
 * Extension of the EhCacheFactoryBean from Spring Framework
 * decorates cache with Cache replication features if enabled!
 * 
 * @author ggruber, hentner
 */
public class CustomEhCacheFactoryBean extends EhCacheFactoryBean {

	private static final Log logger = LogFactory.getLog(CustomEhCacheFactoryBean.class);

	private Boolean isDistributed=null;
	
	private Settings settings;
	
	private CacheManager cacheManager;
	
	/** the name of the cache. Note that "default" is a reserved name for the defaultCache. */
	private String cacheName;

	/** the maximum number of elements in memory, before they are evicted */
	private int maxElementsInMemory = 10000;

	/** the maximum number of Elements to allow on the disk. 0 means unlimited. */
	private int maxElementsOnDisk = 10000000;

	/** one of LRU, LFU and FIFO. Optionally null, in which case it will be set to LRU. */
	private MemoryStoreEvictionPolicy memoryStoreEvictionPolicy = MemoryStoreEvictionPolicy.LRU;

	/** whether to use the disk store */
	private boolean overflowToDisk = true;

	/** whether the elements in the cache are eternal, i.e. never expire */
	private boolean eternal = false;

	/** the default amount of time (in seconds) to live for an element from its creation date */
	private int timeToLive = 120;

	/** the default amount of time (in seconds) to live for an element from its last accessed or modified date */
	private int timeToIdle = 120;

	/** whether to persist the cache to disk between JVM restarts */
	private boolean diskPersistent = false;

	/** how often to run the disk store expiry thread. A large number of 120 seconds plus is recommended */
	private int diskExpiryThreadIntervalSeconds = 120;

	private String beanName;

	private Ehcache cache;


	/**
	 * Set a CacheManager from which to retrieve a named Cache instance.
	 * By default, <code>CacheManager.getInstance()</code> will be called.
	 * <p>Note that in particular for persistent caches, it is advisable to
	 * properly handle the shutdown of the CacheManager: Set up a separate
	 * EhCacheManagerFactoryBean and pass a reference to this bean property.
	 * <p>A separate EhCacheManagerFactoryBean is also necessary for loading
	 * EHCache configuration from a non-default config location.
	 * @see EhCacheManagerFactoryBean
	 * @see net.sf.ehcache.CacheManager#getInstance
	 */
	@Override
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
	 * Set a name for which to retrieve or create a cache instance.
	 * Default is the bean name of this EhCacheFactoryBean.
	 */
	@Override
	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	/**
	 * Specify the maximum number of cached objects in memory.
	 * Default is 10000 elements.
	 */
	@Override
	public void setMaxElementsInMemory(int maxElementsInMemory) {
		this.maxElementsInMemory = maxElementsInMemory;
	}

	/**
	 * Specify the maximum number of cached objects on disk.
	 * Default is 10000000 elements.
	 */
	@Override
	public void setMaxElementsOnDisk(int maxElementsOnDisk) {
		this.maxElementsOnDisk = maxElementsOnDisk;
	}

	/**
	 * Set the memory style eviction policy for this cache.
	 * Supported values are "LRU", "LFU" and "FIFO", according to the
	 * constants defined in EHCache's MemoryStoreEvictionPolicy class.
	 * Default is "LRU".
	 */
	@Override
	public void setMemoryStoreEvictionPolicy(MemoryStoreEvictionPolicy memoryStoreEvictionPolicy) {
		Assert.notNull(memoryStoreEvictionPolicy, "memoryStoreEvictionPolicy must not be null");
		this.memoryStoreEvictionPolicy = memoryStoreEvictionPolicy;
	}

	/**
	 * Set whether elements can overflow to disk when the in-memory cache
	 * has reached the maximum size limit. Default is "true".
	 */
	@Override
	public void setOverflowToDisk(boolean overflowToDisk) {
		this.overflowToDisk = overflowToDisk;
	}

	/**
	 * Set whether elements are considered as eternal. If "true", timeouts
	 * are ignored and the element is never expired. Default is "false".
	 */
	@Override
	public void setEternal(boolean eternal) {
		this.eternal = eternal;
	}

	/**
	 * Set t he time in seconds to live for an element before it expires,
	 * i.e. the maximum time between creation time and when an element expires.
	 * It is only used if the element is not eternal. Default is 120 seconds.
	 */
	@Override
	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	/**
	 * Set the time in seconds to idle for an element before it expires, that is,
	 * the maximum amount of time between accesses before an element expires.
	 * This is only used if the element is not eternal. Default is 120 seconds.
	 */
	@Override
	public void setTimeToIdle(int timeToIdle) {
		this.timeToIdle = timeToIdle;
	}

	/**
	 * Set whether the disk store persists between restarts of the Virtual Machine.
	 * The default is "false".
	 */
	@Override
	public void setDiskPersistent(boolean diskPersistent) {
		this.diskPersistent = diskPersistent;
	}

	/**
	 * Set the number of seconds between runs of the disk expiry thread.
	 * The default is 120 seconds.
	 */
	@Override
	public void setDiskExpiryThreadIntervalSeconds(int diskExpiryThreadIntervalSeconds) {
		this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds;
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public void afterPropertiesSet() throws CacheException, IOException {
		// If no CacheManager given, fetch the default.
		if (cacheManager == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Using default EHCache CacheManager for cache region '" + this.cacheName + "'");
			}
			cacheManager = CacheManager.getInstance();
		}

		// If no cache name given, use bean name as cache name.
		if (cacheName == null) {
			cacheName = beanName;
		}
		
		// set isDistributed
		if (isDistributed == null) {
			if (settings != null)
				isDistributed = Boolean.valueOf(settings.getProperty(ClusterConfig.DISTRIBUTED, "false"));
			else
			    isDistributed = "true".equals(System.getProperty(ClusterConfig.DISTRIBUTED));
		}

		// Fetch cache region: If none with the given name exists,
		// create one on the fly.
		Ehcache rawCache = null;
		if (cacheManager.cacheExists(cacheName)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Using existing EHCache cache region '" + cacheName + "'");
			}
			rawCache = cacheManager.getEhcache(cacheName);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating new EHCache cache region '" + cacheName + "'");
			}
			rawCache = createCache();
			if (isDistributed)
				addDistributionBehavior(rawCache);
			
			cacheManager.addCache(rawCache);
		}

		// Decorate cache if necessary.
		Ehcache decoratedCache = decorateCache(rawCache);
		if (decoratedCache != rawCache) {
			cacheManager.replaceCacheWithDecoratedCache(rawCache, decoratedCache);
		}
		
		cache = decoratedCache;
	}

	/**
	 * Create a raw Cache object based on the configuration of this FactoryBean.
	 */
	protected Cache createCache() {
		return new Cache(
				cacheName, maxElementsInMemory, memoryStoreEvictionPolicy,
				overflowToDisk, null, eternal, timeToLive, timeToIdle,
				diskPersistent, diskExpiryThreadIntervalSeconds, null, null, maxElementsOnDisk);
	}


	@Override
	public Ehcache getObject() {
		return cache;
	}

	@Override
	public Class<? extends Ehcache> getObjectType() { 
		return (cache != null ? cache.getClass() : Ehcache.class);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	protected void addDistributionBehavior(Ehcache cache) {
		CacheEventListenerFactory celf = new RMICacheReplicatorFactory();
		Properties props = getCacheListenerProperties();
		CacheEventListener cacheEventListener = celf.createCacheEventListener(props);
        cache.getCacheEventNotificationService().registerListener(cacheEventListener);
	}
	

	public void setIsDistributed(Boolean isDistributed) {
		this.isDistributed = isDistributed;
	}
	
	public void setSettings(Settings settings) {
		this.settings = settings;
	}
	
	/**
	 * tries to get cacheReplication Configs first from defaults, then 
	 * from webdesk.properties (from keys starting with webdesk.cacheReplication.)
	 * and then from the optional classpath file 'ehCacheListener.properties)
	 * 
	 * @return Properties to use
	 */
	private Properties getCacheListenerProperties() {
		return CacheListenerConfigHelper.getCacheListenerProperties();
	}

}
