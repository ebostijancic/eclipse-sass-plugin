package at.workflow.webdesk.po.impl.test;

import java.util.HashMap;
import java.util.Map;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.web.context.support.StaticWebApplicationContext;

import at.workflow.webdesk.po.PoScriptingService;
import at.workflow.webdesk.po.impl.PoScriptingServiceImpl;

/**
 * Tests PoScriptingService.
 * Most of JavaScript service tests are in WTestJSTools, as the ServiceImpl delegates to that util-class.
 * 
 * @author fritzberger 20.12.2012
 */
public class WTestPoScriptingService extends AbstractDependencyInjectionSpringContextTests {

	public void testVelocityInitialContext() {
		
		final PoScriptingService service = new PoScriptingServiceImpl();
		((PoScriptingServiceImpl) service).setApplicationContext(new StaticWebApplicationContext());
		Map<String,Object> placeholderValues = new HashMap<String,Object>();

		String substituted = service.velocitySubstitution("$DateTools.simpleName", placeholderValues);
		assertEquals("DateTools", substituted);
		substituted = service.velocitySubstitution("$DateUtils.simpleName", placeholderValues);
		assertEquals("DateUtils", substituted);
		substituted = service.velocitySubstitution("$simpleObjectFactory.class.simpleName", placeholderValues);
		assertEquals("SimpleObjectFactory", substituted);
		
	}
	
	/** Tests a Velocity script. */
	public void testVelocityScript() throws Exception	{
		final PoScriptingService service = new PoScriptingServiceImpl();
		Map<String,Object> placeholderValues = new HashMap<String,Object>();
		placeholderValues.put("foo", new Foo());
		placeholderValues.put("number", 3);
		String template = "Hello $foo.getBar($number).getName() !";
		String substituted = service.velocitySubstitution(template, placeholderValues);
		assertEquals("Hello Bar 3 !", substituted);
	}
	
	/** Helper class for testVelocityScript(). MUST be public for Velocity to work!!! */
	public static class Foo
	{
		public Bar getBar(int number)	{
			return new Bar(number);
		}
	}
	
	/** Helper class for testVelocityScript(). MUST be public for Velocity to work!!! */
	public static class Bar
	{
		private int number;
		
		public Bar(int number) {
			this.number = number;
		}
		
		public String getName()	{
			return "Bar "+number;
		}
	}

}
