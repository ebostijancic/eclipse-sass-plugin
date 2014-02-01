package at.workflow.tools.test;


import junit.framework.TestCase;

public class WTestCutDomain extends TestCase {

	public void testCutDomain() {
		
		String username="WORKFLOW\\ggruber";
		
        username = username.substring(username.indexOf("\\"),username.length());
        username = username.replaceAll("\\\\","");
        
        
        assertTrue("cutDomain does not work...", username.equals("ggruber"));

	}
}
