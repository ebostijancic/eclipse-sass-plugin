package at.workflow.webdesk.tools.config;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import at.workflow.webdesk.WebdeskEnvironment;

/**
 * Helperclass to run all initialization code which has to run
 * safely AFTER complete initialization of spring container was done.
 * This is the first <b>webdesk</b> code called when the web-application starts up.
 * It outputs
 * 
 * @author ggruber
 */
public class StartupListener implements ApplicationListener, ApplicationContextAware {

	private ApplicationContext appCtx;
	private String firstStartupBeanName;
	private boolean isStarted = false;
	
	@Override
	public void onApplicationEvent(ApplicationEvent arg0) {
		if (arg0 instanceof ContextRefreshedEvent && false == isStarted) {
			isStarted = true;
			
			logEnvironment();

			// now search for all Beans of type WebdeskStartup and call their start method
			@SuppressWarnings("unchecked")
			Map<String, WebdeskStartup> beanMap = appCtx.getBeansOfType(WebdeskStartup.class);
			
			// first start the bean to be started first by configuration
			if (beanMap.containsKey(firstStartupBeanName)) 
				beanMap.get(firstStartupBeanName).start();
				
			// then start others bean
			for (String key : beanMap.keySet()) {
				if (false == key.equals(firstStartupBeanName))
					beanMap.get(key).start();
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		this.appCtx = arg0;
	}

	public void setFirstStartupBeanName(String firstStartupBeanName) {
		this.firstStartupBeanName = firstStartupBeanName;
	}

	
	private void logEnvironment()	{
		System.err.println("Webdesk EWP starting up at "+new Date()+" with \"user.dir\" directory "+System.getProperty("user.dir"));
		System.err.println("Java Version: "+System.getProperty("java.version"));
		System.err.println("Operating System: "+System.getProperty("os.name")+", Version: "+System.getProperty("os.version")+", Architecture: "+System.getProperty("os.arch")+", File Encoding: "+System.getProperty("file.encoding"));
		System.err.println("User: "+System.getProperty("user.name")+", Language: "+System.getProperty("user.language")+", Locale Default: "+Locale.getDefault());
		System.err.println("Webdesk 'real' path in servlet container: " + WebdeskEnvironment.getRealPath());
		System.err.println("Temp Dir: "+System.getProperty("java.io.tmpdir"));
		System.err.println("Webdesk Version: " + WebdeskEnvironment.getApplicationVersion()+", Build: " + WebdeskEnvironment.getApplicationBuildnumber());
		NumberFormat format = NumberFormat.getIntegerInstance();
		System.err.println("Memory: max = " + format.format(Runtime.getRuntime().maxMemory() / 1024)+" MB, currently used (within max)= "+format.format(Runtime.getRuntime().totalMemory() / 1024)+" MB, free (within currently used) = "+format.format(Runtime.getRuntime().freeMemory() / 1024)+" MB");
		//System.err.println("Classpath: "+System.getProperty("java.class.path"));	// fri_2010-09-13 took this out because it is too long
	}

}
