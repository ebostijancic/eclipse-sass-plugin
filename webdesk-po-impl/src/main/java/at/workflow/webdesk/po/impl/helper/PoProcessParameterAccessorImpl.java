/**
 * 
 */
package at.workflow.webdesk.po.impl.helper;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoProcessParameterAccessor;
import at.workflow.webdesk.po.PoWfServiceDelegate;
import at.workflow.webdesk.po.model.PoActionParameter;
import at.workflow.webdesk.po.model.PoContextParameter;

/**
 * sdzuban 18.04.2011
 */
public class PoProcessParameterAccessorImpl implements PoProcessParameterAccessor {

	private ApplicationContext applicationContext; 
	
	// this is an indirect dependency on the WF Service
	@Override
	public Collection<PoActionParameter> getProcessActionParameters(String procDefId) {
	
		if (applicationContext == null)
			applicationContext = WebdeskApplicationContext.getApplicationContext();
		
		PoWfServiceDelegate wfService = null;
		if (applicationContext.containsBean("PoWfServiceDelegate"))
			wfService = (PoWfServiceDelegate) applicationContext.getBean("PoWfServiceDelegate");
		//	applicationContext.getBeansOfType(PoWfServiceDelegate.class);
		
		if (wfService != null && StringUtils.isBlank(procDefId)==false )
			return wfService.getProcessActionParameters(procDefId);
		else
			return Collections.emptyList();
	}

	// this is an indirect dependency on the WF Service
	@Override
	public Collection<PoContextParameter> getProcessContextParameters(String procDefId) {
		
		if (applicationContext == null)
			applicationContext = WebdeskApplicationContext.getApplicationContext();
		
		PoWfServiceDelegate wfService = null;
		if (applicationContext.containsBean("PoWfServiceDelegate"))
			wfService = (PoWfServiceDelegate) applicationContext.getBean("PoWfServiceDelegate");
		//	applicationContext.getBeansOfType(PoWfServiceDelegate.class);
		
		if (wfService != null)
			return wfService.getProcessContextParameters(procDefId);
		else
			return Collections.emptyList();
	}
	
}
