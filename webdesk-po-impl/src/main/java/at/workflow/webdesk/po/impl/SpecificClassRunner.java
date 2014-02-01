package at.workflow.webdesk.po.impl;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * TODO: check whether this class is really needed, could not find any references on 27.11.2012
 * 
 */
public class SpecificClassRunner implements ApplicationContextAware {

	ApplicationContext appCtx = null;
	
	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		this.appCtx = arg0;
	}
	
	public Map getModuleSpecificActions() {
		Map m = null;
		try {
			m = this.appCtx.getBeansOfType(Class.forName("at.workflow.webdesk.po.SpecificAdminAction"));
		} catch (BeansException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return m;
	}
	

}
