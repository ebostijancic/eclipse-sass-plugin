package at.workflow.webdesk.tools.cache;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import at.workflow.tools.ResourceHelper;
import at.workflow.webdesk.WebdeskEnvironment;
import at.workflow.webdesk.tools.config.PropertiesUtils;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;

/**
 * Helper class which builds a EhCacheManger configuration
 * with replication features if needed. 
 * 
 * EH Cache default cache-configuration (including replication features) an is loaded from webdesk.properties, if specified
 * (under the WDPROP_PREFIX webdesk.cacheReplication.)
 * or can also be set inside the 4 files inside ROOT classpath: defaultCacheConfiguration.properties, 
 * peerProviderFactory.properties and peerListenerFactory.properties and ehCacheListener.properties
 * if neither webdesk.properties NOR the 4 files exist, default values are taken from classpath
 * file at at/workflow/webdesk/tools/cache/defaultCacheReplicationConfig.properties
 * 
 * @author ggruber
 */
public class CustomEhCacheConfigurationFactory {

	private static final String PROPERTYNAME_PO_OPTIONS_CLUSTER_NODE = "PoOptions.clusterNode";
	
	private static final String WDPROP_PREFIX = "webdesk.cacheReplication.";
	private static final String WDPROP_NAME_EXCLUDEDCACHES = WDPROP_PREFIX + "excludedCaches";
	

	private static final Logger logger = Logger.getLogger(CustomEhCacheConfigurationFactory.class);

	private boolean isDistributed = false;
	private Properties webdeskProps;
	private Properties defaultProps;

	/** build the configuration for the webdesk ehcache CacheManager, adding
	 * RMI replication features to the preconfigured caches and the default cache
	 * if isDistributed is set to true inside webdesk.properties */
	public Configuration buildConfiguration() {

		// getting isDistributed property from webdesk.properties (or System properties)
		PropertiesUtils propUtils = new PropertiesUtils();
		webdeskProps = propUtils.getProperties();

		if ("true".equals(propUtils.getProperties().get("isDistributed")) || "true".equals(System.getProperty("isDistributed"))) {
			isDistributed = true;
		} else {
			logger.warn("'isDistributed' is not defined in webdesk.properties. Using default [false]!");
		}

		// Declare a new Configuration object
		Configuration configuration = new Configuration();


		DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
		diskStoreConfiguration.setPath(buildCacheDirectory());

		defaultProps = new Properties();
		try {
			defaultProps.load(new ClassPathResource("at/workflow/webdesk/tools/cache/defaultCacheReplicationConfig.properties").getInputStream());
		}
		catch (IOException ioe) {
			logger.error("Could not load configuration file default configuration file, " + ioe.getMessage());
		}
		Properties specialProps = new Properties();
		try {
			specialProps.load(new ClassPathResource("defaultCacheConfiguration.properties").getInputStream());
		}
		catch (IOException ioe) {
			// ignore
		}

		

		// create the default cache Configuration
		CacheConfiguration defaultCacheConfiguration = new CacheConfiguration();
		
		defaultCacheConfiguration.setDiskPersistent(getBoolean(getProperty("diskPersistence", WDPROP_PREFIX, specialProps), false));
		defaultCacheConfiguration.setDiskExpiryThreadIntervalSeconds(getInteger(getProperty("diskExpiryThreadIntervalSeconds", WDPROP_PREFIX, specialProps), 0));
		defaultCacheConfiguration.setEternal(getBoolean(getProperty("eternal", WDPROP_PREFIX, specialProps), false));
		defaultCacheConfiguration.setMaxElementsInMemory(getInteger(getProperty("maxElementsInMemory", WDPROP_PREFIX, specialProps), 100));
		defaultCacheConfiguration.setName("defaultCacheConfiguration");
		defaultCacheConfiguration.setOverflowToDisk(getBoolean(getProperty("overflowToDisk", WDPROP_PREFIX, specialProps), false));
		defaultCacheConfiguration.setTimeToIdleSeconds(getInteger(getProperty("timeToIdleSeconds", WDPROP_PREFIX, specialProps), 100));
		defaultCacheConfiguration.setTimeToLiveSeconds(getInteger(getProperty("timeToLiveSeconds", WDPROP_PREFIX, specialProps), 100));

		// add default disk store configuration
		configuration.addDiskStore( diskStoreConfiguration );

		if (isDistributed) {

			configuration.addCacheManagerPeerProviderFactory( createPeerProviderConfiguration() );

			configuration.addCacheManagerPeerListenerFactory( createPeerListenerConfiguration() );

			// add cache replication config to default cache!
			defaultCacheConfiguration.addCacheEventListenerFactory( createEventListenerFactoryConfiguration() );
		}

		configuration.addDefaultCache(defaultCacheConfiguration);
		
		
		Set<String> excludeFromReplication = getCachesExcludedFromReplication( webdeskProps );
		
		
		// look for ehcache.xml in at.workflow.webdesk.*.*mpl  (TODO: mpl = impl? why not take impl?)
		
		String cpPattern = "classpath*:/at/workflow/webdesk/*/*mpl/ehcache.xml";
		PathMatchingResourcePatternResolver pmResolver = new PathMatchingResourcePatternResolver(); 
		Resource[] ress;
		try {
			ress = pmResolver.getResources(cpPattern);
			for (int j = 0; j < ress.length; j++) {
				String cp = ResourceHelper.getClassPathOfResource(ress[j]);
				
				Resource res = new ClassPathResource(cp);
				Configuration config = ConfigurationFactory.parseConfiguration(res.getInputStream());
				
				for (Object key : config.getCacheConfigurations().keySet()) {
					CacheConfiguration cacheConfig = (CacheConfiguration) config.getCacheConfigurations().get(key);
					
					if (isDistributed && excludeFromReplication.contains( cacheConfig.getName() ) == false)
						cacheConfig.addCacheEventListenerFactory( createEventListenerFactoryConfiguration() );
					
					configuration.addCache(cacheConfig);
				}
			}
		}
		catch (IOException e) {
			logger.warn("Problems to locate files at classpath=" + cpPattern);
		}
		
		return configuration;
	}
	
	public static Set<String> getCachesExcludedFromReplication(Properties props) {
		Set<String> excludeFromReplication = new HashSet<String>();
		if (props.containsKey( WDPROP_NAME_EXCLUDEDCACHES )) {
			
			String caches = props.getProperty( WDPROP_NAME_EXCLUDEDCACHES );
			excludeFromReplication.addAll( Arrays.asList( StringUtils.split(caches, ',') ) );
			
		}
		return excludeFromReplication;
	}

	private CacheEventListenerFactoryConfiguration createEventListenerFactoryConfiguration() {
		CacheEventListenerFactoryConfiguration eventListenerFactoryConfiguration;
		eventListenerFactoryConfiguration = new CacheEventListenerFactoryConfiguration();
		eventListenerFactoryConfiguration.setClass( "net.sf.ehcache.distribution.RMICacheReplicatorFactory" );
		eventListenerFactoryConfiguration.setProperties(CacheListenerConfigHelper.getCacheListenerPropertiesAsString());
		return eventListenerFactoryConfiguration;
	}
	
	/** creates the ehCache peerlistener Configuration */
	private FactoryConfiguration createPeerListenerConfiguration() {
		Properties specialProps = new Properties();
		String prefix = "webdesk.cacheReplication.peerListener.";
		try {
			specialProps.load(new ClassPathResource("peerListenerFactory.properties").getInputStream());
		}
		catch (IOException e) {
			// specialprops not found
		}
		
		FactoryConfiguration peerListenerFactoryConfiguration = new FactoryConfiguration();
		peerListenerFactoryConfiguration.setClass((String) getProperty("class", prefix, specialProps));
		peerListenerFactoryConfiguration.setProperties((String) getProperty("properties", prefix, specialProps));
		return peerListenerFactoryConfiguration;
	}

	/** creates the ehCache peerProvicer Configuration */
	private FactoryConfiguration createPeerProviderConfiguration() {
		String prefix;
		FactoryConfiguration peerProviderFactoryConfiguration;
		peerProviderFactoryConfiguration = new FactoryConfiguration();
		Properties specialPeerProviderProps = new Properties();
		prefix = "webdesk.cacheReplication.peerProvider.";

		try {
			specialPeerProviderProps.load(new ClassPathResource("peerProviderFactory.properties").getInputStream());
		}
		catch (IOException e) {
			// specialprops not found
		}
		peerProviderFactoryConfiguration.setClass((String) getProperty("class", prefix, specialPeerProviderProps));
		peerProviderFactoryConfiguration.setProperties((String) getProperty("properties", prefix, specialPeerProviderProps));
		return peerProviderFactoryConfiguration;
	}

	/** builds a unique cachedirectory for the current webapplication */
	private String buildCacheDirectory() {

		String webappContextString = getWebappContextString();
		assert webappContextString != null : "need a name to append to local cache directory";
		
		String nodeName = webdeskProps.getProperty( PROPERTYNAME_PO_OPTIONS_CLUSTER_NODE );

		String cachePath = System.getProperty("java.io.tmpdir") + "/caches" + ( StringUtils.isEmpty(nodeName)? "" : "_" + nodeName) + webappContextString;

		if (webappContextString.length() <= 0) { // is a unit test environment, remove any cached data
			cleanCacheDir(cachePath);
		}

		try {
			FileUtils.forceMkdir(new File(cachePath));
		}
		catch (IOException e1) {
			// ensure dir is present
			logger.warn("problems while making dir=" + cachePath, e1);
		}

		return cachePath;
	}

	private void cleanCacheDir(String cachePath) {
		try {
			FileUtils.deleteDirectory(new File(cachePath));
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
			// can not delete directory - this is not a condition under which a unit test can run
		}
	}

	/**
	 * This is to avoid two Webdesk installations in same Tomcat to have the same cache files.
	 * @return "" when this is an unit test environment,
	 * 		or a webapp-specific name when this is a servlet environment.
	 */
	private String getWebappContextString() {
		if (WebdeskEnvironment.getServletContext() != null) {
			// for production environment return the a unique directory per webapplication
			
			// when servlet context provides a name, return it
			if (WebdeskEnvironment.getServletContext().getServletContextName() != null)	{
				return WebdeskEnvironment.getServletContext().getServletContextName().replaceFirst("/", "_");
			}

			// try servletContext.getRealPath("/")
			String realPath = WebdeskEnvironment.getRealPath();
			if (realPath != null) {
				// take last portion of real path
				return "_" + realPath.split("/")[realPath.split("/").length - 1];
			}

			// just ensure uniqueness
			return "_" + hashCode();
		}

		// for testcases return no additional context
		return "";
	}

	private Object getProperty(String key, String prefix, Properties specialProps) {
		Object ret = doGetProperty(key, prefix, specialProps);
		if (logger.isDebugEnabled() && ret != null) {
			logger.debug(key + " = " + ret);
		}
		return ret;
	}

	/** retrieves the specified property from either the special properties, the
	 * webdesk.properties or the default properties. (in this order) */
	private Object doGetProperty(String key, String prefix, Properties specialProps) {

		if (specialProps != null && specialProps.containsKey(key)) {
			return specialProps.get(key);
		}
		if (webdeskProps != null && webdeskProps.containsKey(prefix + key)) {
			return webdeskProps.get(prefix + key);
		}
		return defaultProps.get(prefix + key);

	}

	private int getInteger(Object object, int defValue) {
		try {
			return new Integer((String) object).intValue();
		}
		catch (Exception e) {
			logger.debug("Could not convert " + object + " to integer");
			return defValue;
		}
	}

	private boolean getBoolean(Object object, boolean defValue) {
		try {
			return Boolean.valueOf((String) object);
		}
		catch (Exception e) {
			logger.debug("Could not convert " + object + " to boolean");
			return defValue;
		}
	}

}
