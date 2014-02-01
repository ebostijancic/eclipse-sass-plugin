package at.workflow.webdesk.test;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.MulticastKeepaliveHeartbeatSender;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.webdesk.tools.testing.TestingHelper;

public class TestCacheManagerClusterNode extends TestCase {

	Logger logger = Logger.getLogger(this.getClass());
	ClassPathXmlApplicationContext apctx;
	
	public void setUp() {
		// logging
		TestingHelper.configureLogging("at/workflow/webdesk/tools/cache/test/log4j.properties");
		
		logger.info("Startup of the applicationContext");
    	// setup Spring Beans
	    String[] xmlfile = {"at/workflow/webdesk/tools/cache/test/clusterConfigAppCtx.xml" };
		apctx = new ClassPathXmlApplicationContext(xmlfile, false);
		
		PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
		Properties props = new Properties();
		props.put("isDistributed", "true");
		ppc.setProperties(props);
		
		System.setProperty("isDistributed", "true");

		apctx.addBeanFactoryPostProcessor(ppc);
        apctx.refresh();
        
        MulticastKeepaliveHeartbeatSender.setHeartBeatInterval(1000);

	}
	
	public void tearDown() {
		apctx.stop();
	}
	
	public void testChangeCache() throws Exception {
		
		CacheManager cacheManager1 = (CacheManager) this.apctx.getBean("CacheManager1");
		//CacheManager cacheManager1 = new CacheManager(new ClassPathResource("at/workflow/webdesk/tools/cache/test/ehcache-direct-node.xml").getInputStream());
		
		CacheManager cacheManager2 = (CacheManager) this.apctx.getBean("CacheManager2");
		//CacheManager cacheManager2 = new CacheManager(new ClassPathResource("at/workflow/webdesk/tools/cache/test/ehcache-direct-node.xml").getInputStream());
		
		
        Cache cache1 = cacheManager1.getCache("cache1");
		cache1.put(new Element("key" + (new Date()).getTime(), "value"));
		
		Cache cache1OnCM2 = cacheManager2.getCache("cache1");
		cache1OnCM2.put(new Element("key" + (new Date()).getTime(), "value"));
		
		wait(10);
		
		//while (true) {
		
			CacheManagerPeerProvider provider = cacheManager1.getCacheManagerPeerProvider("RMI");
			List remotePeersOfCache1 = provider.listRemoteCachePeers(cacheManager1.getCache("cache1"));
			
			this.logger.info("Remote Peers of Cache1 on CacheManager 1=" + remotePeersOfCache1);
			
			provider = cacheManager2.getCacheManagerPeerProvider("RMI");
			remotePeersOfCache1 = provider.listRemoteCachePeers(cacheManager2.getCache("cache1"));
			
			this.logger.info("Remote Peers of Cache1 on Cachemanager 2=" + remotePeersOfCache1);
			
			wait(3);
		//}
		
		
	}
	
	protected void wait(int seconds) {
		try { 
			Thread.sleep(seconds * 1000);
		} catch (Exception e) {
			// nothing to do
		}
	}
}
