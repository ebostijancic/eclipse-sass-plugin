package at.workflow.tools;

import org.springframework.aop.target.dynamic.AbstractRefreshableTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * The RefreshableTargetSource can be used when it should be possible to switch 
 * between two implementations of a common interface. The class path 
 * of the interface should be provided via <code>defaultClass</code>, as 
 * it is necessary to determine the class of the resulting object without 
 * generating it. This is especially a problem when a set of beans which implements 
 * a distinct class has to be filled up, because the type of every bean has to 
 * be determined then.
 * <p> 
 * The <code>beanId</code> is used to determine the implementing class. There 
 * is no validity check, so it is possible to set a beanId that does not even 
 * exist. 
 * 
 * @author hentner
 * @deprecated this is not used anymore since GW refactoring. TODO remove this class.
 */
public class RefreshableTargetSource extends AbstractRefreshableTargetSource implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private String beanId;
	private String defaultBeanId;
	private String generalInterface;

	public String getDefaultBeanId() {
		return defaultBeanId;
	}

	public void setDefaultBeanId(String defaultBeanId) {
		this.defaultBeanId = defaultBeanId;
	}

	/**
	 * It is mandatory that this property is set correctly, as otherwise exceptions will 
	 * occur during startup when the application context will not be ready to initialise 
	 * the bean correctly. (full path should be provided)
	 * 
	 * @param generalInterface the full path of the interface used 
	 */
	public void setGeneralInterface(String generalInterface) {
		this.generalInterface = generalInterface;
	}

	public String getBeanId() {
		return beanId;
	}

	public void setBeanId(String beanId) {
		this.beanId = beanId;

		// force refresh!
		setRefreshCheckDelay(10);
	}

	@Override
	public Class<?> getTargetClass() {
		if (generalInterface != null)
			try {
				return Class.forName(generalInterface);
			}
			catch (ClassNotFoundException e) {
				logger.error(e, e);
			}
		return null;
	}

	@Override
	protected Object freshTarget() {
		if (applicationContext == null)
			return null;
		
		if (beanId != null && !"".equals(beanId))
			return applicationContext.getBean(beanId);

		if (defaultBeanId != null && !"".equals(defaultBeanId))
			return applicationContext.getBean(defaultBeanId);

		return null;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
