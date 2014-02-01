package at.workflow.webdesk.tools;

import junit.framework.TestCase;

/**
 * Tests and specifies Webdesk naming conventions.
 * 
 * @author fritzberger 2013-05-21
 */
public class WTestNamingConventions extends TestCase
{
	public void testModuleName() {
		final String className = "at.workflow.webdesk.po.model.PoPerson";
		final String moduleName = NamingConventions.getModuleName(className);
		assertEquals("Po", moduleName);
	}

}
