package at.workflow.webdesk.tools.hibernate;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheReplicator;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.hibernate.EhCacheProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Timestamper;

import at.workflow.webdesk.tools.cache.CustomEhCacheConfigurationFactory;
import at.workflow.webdesk.tools.config.PropertiesUtils;

public class CustomCacheProvider implements CacheProvider{

	/**
     * The Hibernate system property specifying the location of the ehcache configuration file name.
     * <p/
     * If not set, ehcache.xml will be looked for in the root of the classpath.
     * <p/>
     * If set to say ehcache-1.xml, ehcache-1.xml will be looked for in the root of the classpath.
     */
    public static final String NET_SF_EHCACHE_CONFIGURATION_RESOURCE_NAME = "net.sf.ehcache.configurationResourceName";

    private static final Log LOG = LogFactory.getLog(EhCacheProvider.class);
    
    private Logger logger = Logger.getLogger(this.getClass());

    private CacheManager manager;

	private boolean isDistributed=false;
    
	private Set<String> excludedFromReplication = new HashSet<String>();

    /**
     * Builds a Cache.
     * <p/>
     * Even though this method provides properties, they are not used.
     * Properties for EHCache are specified in the ehcache.xml file.
     * Configuration will be read from ehcache.xml for a cache declaration
     * where the name attribute matches the name parameter in this builder.
     *
     * @param name       the name of the cache. Must match a cache configured in ehcache.xml
     * @param properties not used
     * @return a newly built cache will be built and initialised
     * @throws org.hibernate.cache.CacheException
     *          inter alia, if a cache of the same name already exists
     */
    @Override
	public final Cache buildCache(String name, Properties properties) throws CacheException {
        try {
            net.sf.ehcache.Ehcache cache = manager.getCache(name);
            
            if (cache == null) {
                LOG.warn("Could not find a specific ehcache configuration for cache named [" + name + "]; using defaults.");
                manager.addCache(name);
                cache = manager.getCache(name);
                
                
                if (isDistributed && excludedFromReplication.contains(name)==true) {
                	logger.info("Cache=" + name + " is excluded from replication, going to remove RMI replicator.");
                	CacheEventListener cel = discoverRMIReplicator(cache);
                	if (cel!=null) {
                		boolean successful = cache.getCacheEventNotificationService().unregisterListener(cel);
                		if (successful)
                			logger.info("successfuly removed RMI replicator for cache=" + name);
                		else 
                			logger.info("Removing the RMI replicator for cache=" + name + " was not successful!");
                	}
                }
                
                
                CustomCacheProvider.LOG.debug("started EHCache region: " + name);
            }
            return new net.sf.ehcache.hibernate.EhCache(cache);
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }
    
    private CacheEventListener discoverRMIReplicator(net.sf.ehcache.Ehcache cache) {
    	
    	for (Object cel : cache.getCacheEventNotificationService().getCacheEventListeners()) {
    		CacheEventListener cacheEventListener = (CacheEventListener) cel; 
    		if ( cacheEventListener instanceof CacheReplicator ) {
    			return cacheEventListener;
    		}
    	}
    	return null;
    }

    /**
     * Returns the next timestamp.
     */
    @Override
	public final long nextTimestamp() {
        return Timestamper.next();
    }
    
   
    /**
     * Callback to perform any necessary initialization of the underlying cache implementation
     * during SessionFactory construction.
     * <p/>
     *
     * @param properties current configuration settings.
     */
    /* (non-Javadoc)
     * @see org.hibernate.cache.CacheProvider#start(java.util.Properties)
     */
    @Override
	public final void start(Properties properties) throws CacheException {
    	
    	// setting isDistributed!
    	PropertiesUtils propUtils = new PropertiesUtils();
		if ("true".equals(propUtils.getProperties().get("isDistributed")) || "true".equals(System.getProperty("isDistributed"))) {
    			isDistributed=true;
    	} else {
    		logger.warn("'isDistributed' is not defined in webdesk.properties. Using default [false]!");
    	}
		
		// get List of Cache names excluded from Replication
		excludedFromReplication = CustomEhCacheConfigurationFactory.getCachesExcludedFromReplication( propUtils.getWebdeskProperties() );
    	
        if (manager != null) {
            LOG.warn("Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() " +
                    " between repeated calls to buildSessionFactory. Using previously created EhCacheProvider." +
                    " If this behaviour is required, consider using SingletonEhCacheProvider.");
            return;
        }
        
        if (CacheManager.ALL_CACHE_MANAGERS.size()>0) {
        	this.manager = CacheManager.getInstance();
        	return;
        }
        
        try {
        	
        	CustomEhCacheConfigurationFactory cacheConfigFactory = new CustomEhCacheConfigurationFactory();
            manager = new CacheManager(cacheConfigFactory.buildConfiguration());
            
            try {
				Field field = manager.getClass().getDeclaredField("singleton");
				field.setAccessible(true);
				field.set(manager, manager);
				
			} catch (Exception e) {
				this.logger.error(e);
			}
            
            
        } catch (net.sf.ehcache.CacheException e) {
            if (e.getMessage().startsWith("Cannot parseConfiguration CacheManager. Attempt to create a new instance of " +
                    "CacheManager using the diskStorePath")) {
                throw new CacheException("Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() " +
                    " between repeated calls to buildSessionFactory. Consider using SingletonEhCacheProvider. Error from " +
                    " ehcache was: " + e.getMessage());
            }
			throw e;
        }
    }

    /**
     * Callback to perform any necessary cleanup of the underlying cache implementation
     * during SessionFactory.close().
     */
    @Override
	public final void stop() {
        if (manager != null) {
            manager.shutdown();
            manager = null;
        }
    }


    /**
     * Not sure what this is supposed to do.
     *
     * @return false to be safe
     */
    @Override
	public final boolean isMinimalPutsEnabledByDefault() {
        return false;
    }

	

}
