package at.workflow.webdesk.tools.cache;

import java.io.IOException;
import java.lang.reflect.Field;

import net.sf.ehcache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.CacheException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * fork of EhCacheManagerFactoryBean from the spring framework
 * 
 * instantiates a new EhCache Manager and adds configuration for
 * cache replication if isDistributed is set inside webdesk.properties
 * 
 * If an instance of EhCacheManager already exists, returns that instance!
 * 
 * @author ggruber
 *
 */
public class CustomEhCacheManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {
	
	protected final Log logger = LogFactory.getLog(getClass());
	private String cacheManagerName;
	private CacheManager cacheManager;
	private boolean enforceSingleton = true;


	/**
	 * Set the name of the EHCache CacheManager (if a specific name is desired).
	 * @see net.sf.ehcache.CacheManager#setName(String)
	 */
	public void setCacheManagerName(String cacheManagerName) {
		this.cacheManagerName = cacheManagerName;
	}

	@Override
	public void afterPropertiesSet() throws IOException, CacheException {
		
	   if (CacheManager.ALL_CACHE_MANAGERS.size()>0 && enforceSingleton) {
		   this.cacheManager = CacheManager.getInstance();
	   	   return;
	   }
		
	   logger.info("Initializing EHCache CacheManager");	
        
	    CustomEhCacheConfigurationFactory cacheConfigFactory = new CustomEhCacheConfigurationFactory();
	    
        try {
        	this.cacheManager = new CacheManager(cacheConfigFactory.buildConfiguration());
        	
        	if (enforceSingleton) {
				Field field = cacheManager.getClass().getDeclaredField("singleton");
				field.setAccessible(true);
				field.set(cacheManager, cacheManager);
        	}
			
		} catch (Exception e) {
			logger.error(e,e);
			throw new RuntimeException(e);
		}
		
		if (this.cacheManagerName != null) {
			this.cacheManager.setName(this.cacheManagerName);
		}
	}
	
	@Override
	public Object getObject() {
		return this.cacheManager;
	}

	@Override
	public Class<?> getObjectType() {
		return (this.cacheManager != null ? this.cacheManager.getClass() : CacheManager.class);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}


	@Override
	public void destroy() {
		logger.info("Shutting down EHCache CacheManager");
		this.cacheManager.shutdown();
	}

	public void setEnforceSingleton(boolean enforceSingleton) {
		this.enforceSingleton = enforceSingleton;
	}


}
