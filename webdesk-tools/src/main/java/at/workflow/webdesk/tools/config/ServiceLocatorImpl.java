package at.workflow.webdesk.tools.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import at.workflow.webdesk.ServiceLocator;

/**
 * Use this class to lookup services in other modules without
 * having to implement ApplicationContextAware and with a little less boilerplate code.
 * The nice thing is also, that with this locator the target bean is hot swappable!
 * 
 * TODO: what means "the target bean is hot swappable"?
 * 
 * @author ggruber
 */
public class ServiceLocatorImpl implements ApplicationContextAware, ServiceLocator {

	private ApplicationContext applicationContext;
	private String beanName;
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public Object lookupService() {
		if (isServiceAvailable()) {
			return applicationContext.getBean(beanName);
		}
		return null;
	}
	
	public boolean isServiceAvailable() {
		return applicationContext.containsBean(beanName);
	}

	/** Spring setter, do not use. */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

}
