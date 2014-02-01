package at.workflow.webdesk.tools;

import at.workflow.webdesk.tools.api.SysAdminUserInfo;

/**
 * Holding global constants for Webdesk application.
 * 
 * FIXME: This constants conflict with the possibility to change username and password for the full system
 * 	administrator by setting the BeanProperties AuthenticationOptions.sysAdminUser and AuthenticationOptions.sysAdminPassword
 * 	So we should deprecate this class, instead the Spring Bean SysAdminUserInfo should be used!
 * 
 * @author fritzberger 19.08.2011
 * @deprecated use applicationContext.getBean("SysAdminUserInfo");
 * @see SysAdminUserInfo
 */
public interface WebdeskConstants {
	
	/** The name of the system-administrator login-user. */
	String SADMIN = "sadmin";
	
	/** The password of the system-administrator login-user. */
	String SADMIN_PASSWORD = "ksedbewfi";
	
}
