package at.workflow.webdesk.tools;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import at.workflow.webdesk.tools.api.JSExecutionContext;

/**
 * @author fritzberger 2012-09-01
 * @author ggruber 30.09.2013
 */
public class WTestJSTools extends TestCase
{
	// START Gabriel's tests
	
	public void testScriptWithUpperCaseVariables() {
		
		String script = "if (Vorgesetzter!=null) { Vorgesetzter; } else { \"NOTHING\"; }";
		Map<String,Object> ctx = new HashMap<String,Object>();
		
		ctx.put("Vorgesetzter", null);
		assertEquals("NOTHING", JSTools.executeJS(script, ctx));
		
		ctx.put("Vorgesetzter", "Martin");
		assertEquals("Martin", JSTools.executeJS(script, ctx));
		
		ctx.put("vorgesetzter", "Hugo");	// must be case sensitive!
		assertEquals("Martin", JSTools.executeJS(script, ctx));
	}
	
	public void testSimpleScript() {
		
		String script = "myVariable + '_postfix'";
		Map<String,Object> ctx = new HashMap<String,Object>();
		
		ctx.put("myVariable", "Content");
		
		assertEquals(JSTools.executeJS(script, ctx), "Content_postfix");
	}
	
	public void testSimpleBooleanScript() {
		
		String script = "boolVar!=null";
		
		Map<String,Object> ctx = new HashMap<String,Object>();
		ctx.put("boolVar", null);
		
		assertEquals( JSTools.executeJS(script, ctx), false);
	}

	// END Gabriel's tests

	// START Fritz's tests
	
	public void testArithmeticExpression() {
		final Object result = JSTools.executeJS("1 + 2");
		assertTrue(result instanceof Number);
		assertEquals(3, ((Number) result).intValue());
	}
	
	public void testJavaFromJavaScript() {
		final String script = 
			"var myDate = Packages.at.workflow.webdesk.tools.date.DateTools.toDate(2012, 1, 1); "+
			"var weekDay = Packages.at.workflow.webdesk.tools.date.DateTools.getDateTimeField(myDate, Packages.java.util.Calendar.DAY_OF_WEEK); "+
			"'WeekDay-' + weekDay;";
		// scripting result is expected to be the *last expression* in this statement sequence!
		
		final Object result = JSTools.executeJS(script);
		assertEquals("WeekDay-1", result);
	}

	public void testArithmeticExpressionWithMap() {
		final Map<String, Object> context = new HashMap<String, Object>();
		context.put("x", 5);
		context.put("y", 7);
		
		final Object result = JSTools.executeJS("x + y", context);
		assertEquals(12, ((Number) result).intValue());
	}
	
	public void testArithmeticExpressionWithScriptingContext() {
		final Map<String, Object> predefinedContext = new HashMap<String, Object>();
		predefinedContext.put("x", 5);
		
		final JSExecutionContext context = JSTools.createContext(predefinedContext);
		context.putVariable("y", 7);
		
		Object result = JSTools.executeJS("x + y", context);
		assertEquals(12, ((Number) result).intValue());
	}
	
	/** Tests the reflection-on-demand ScriptingContext, using bean values and explicitly mapped values together. */
	public void testReflectionOnDemandScriptingContext() {
		// value provider for testing bean reflection
		class TestVariableValueHolder
		{
			@SuppressWarnings("unused")	// will be found by reflection for variable "result"
			public int getResult()	{
				return -30;
			}
			@SuppressWarnings("unused")	// will be found by reflection for variable "subtrahend"
			public int getSubtrahend()	{
				return -10;
			}
		}
		
		// provide bean values
		JSExecutionContext context = JSTools.createContext(new TestVariableValueHolder());
		
		// provide explicitly mapped values
		context.putVariable("x", 7);
		context.putVariable("y", 5);
		
		// execute calculation
		Object result = JSTools.executeJS("result + x * y - subtrahend", context);	// (-30) + 7 * 5 - (-10)
		
		// assert calculation result
		assertEquals(15, ((Number) result).intValue());
	}

	/** Tests using disallowed variable names. */
	public void testScriptingContextReservedNames() {
		JSExecutionContext context = JSTools.createContext();

		for (String illegalName : new String [] { "context", "Function" })	{
			try	{
				context.putVariable(illegalName, "some variable value");
				fail("Using illegal name "+illegalName+" was not thrown back!");
			}
			catch (IllegalArgumentException e)	{
				// is expected here
			}
		}
	}

	/** Tests a String substitution. */
	public void testStringSubstitution()	{
		JSExecutionContext context = JSTools.createContext();
		context.putVariable("world", "WORLD");
		context.putVariable("exclamation", "!");
		
		Object result = JSTools.executeJS("'Hello '+world+exclamation", context);
		
		assertEquals("Hello WORLD!", result);
	}

	/** Tests null as variable values. */
	public void testNullVariableValues() throws Exception	{
		JSExecutionContext context = JSTools.createContext();
		context.putVariable("world", null);
		
		// it is legal to append a null to a String
		Object stringResult = JSTools.executeJS("'Hello '+world", context);
		assertEquals("Hello "+null, stringResult);
		
		// it is legal to calculate arithmetically with a variable that is null
		Object numberResult = JSTools.executeJS("123 + world", context);
		assertEquals(123.0, numberResult);
	}
	
	/** Tests whether a variable with a dotted name (like "person.firstName") could be used in a JavaScript context. */
	public void testDottedVariableNames() throws Exception	{
		final String HELLO_JOHN_SCRIPT = "'Hello '+person.firstName+'!'";

		try	{
			final JSExecutionContext context = JSTools.createContext();
			context.putVariable("person.firstName", "John");
			final Object stringResult = JSTools.executeJS(HELLO_JOHN_SCRIPT, context);
			assertEquals("Hello John!", stringResult);
			
			fail("JavaScript accepts variable names containing a dot? We could rewrite our workaround now, see WDHREXPERT-524");
		}
		catch (IllegalArgumentException e)	{
			if (e.getMessage() == null || e.getMessage().contains("must not contain dots") == false)
				throw e;
			
			// else: is expected here
		}
		
		final JSExecutionContext context2 = JSTools.createContext((Object) null);
		context2.putVariable("person", new John());
		final Object stringResult2 = JSTools.executeJS(HELLO_JOHN_SCRIPT, context2);
		assertEquals("Hello John!", stringResult2);
	}


	/** Test helper class. */
	public static class John
	{
		public final String firstName = "John";
	}
	
	
	public void testSameEngineWithDifferentVariableMaps() {
		final Map<String,Object> powersValues = new HashMap<String,Object>();
		powersValues.put("userName", "jpow");
		
		final JSExecutionContext context = JSTools.createContext(powersValues);
		assertEquals("jpow", JSTools.executeJS("userName", context));
		
		// check if changes show immediately after modification of the bean
		powersValues.put("userName", "jo");
		// the context performs value caching, so the old value remains
		assertEquals("jpow", JSTools.executeJS("userName", context));
		// must change all variables to see the new value
		context.setVariables(powersValues);
		assertEquals("jo", JSTools.executeJS("userName", context));
	}
	
	/** Tests if caching of engines work. */
	public void testSameEngineWithDifferentBeans() {
		final Object powersBean = new TestBean("John", "Powers", "jpow", "12345");
		
		final JSExecutionContext context = JSTools.createContext(powersBean);
		assertEquals("jpow", JSTools.executeJS("userName", context));
		assertEquals("John Powers (12345)", JSTools.executeJS("firstName+' '+lastName+' ('+employeeId+')'", context));
		
		final TestBean porterBean = new TestBean("Natalie", "Porter", "npor", "54321");
		context.setBean(porterBean);
		assertEquals("npor", JSTools.executeJS("userName", context));
		
		// check if changes show immediately after modification of the bean
		porterBean.setUserName("napo");
		porterBean.setFirstName("Natty");
		// the context performs value caching, so the old value remains if it has been called before
		assertEquals("npor", JSTools.executeJS("userName", context));
		// "firstName" has not been called before, so it immediately shows the new value
		assertEquals("Natty", JSTools.executeJS("firstName", context));
		// must change the whole bean to see the new value
		context.setBean(porterBean);
		assertEquals("napo", JSTools.executeJS("userName", context));
	}
	
	
	private static class TestBean
	{
		private String firstName;
		private String lastName;
		private String userName;
		private String employeeId;
		
		TestBean(String firstName, String lastName, String userName, String employeeId)	{
			this.firstName = firstName;
			this.lastName = lastName;
			this.userName = userName;
			this.employeeId = employeeId;
		}

		public String getFirstName() {
			return firstName;
		}
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public String getLastName() {
			return lastName;
		}
		public String getEmployeeId() {
			return employeeId;
		}
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
	}

	
	public void testDottedVariableNamesWorkaround()	{
		final String[] variableNames = new String [] { "person.firstName", "person.lastName", "employeeId" };
		final String javaScript = "person.firstName+'.'+person.firstName+' ('+employeeId+')'";
		final JSTools.DottedVariableNamesWorkaround adaption = JSTools.dottedVariableNamesWorkaround(javaScript, variableNames);
		
		assertEquals(variableNames.length, adaption.variableNamesWithoutDots.length);
		
		assertEquals("person_firstName+'.'+person_firstName+' ('+employeeId+')'", adaption.javaScript);
		
		assertEquals("person_firstName", adaption.variableNamesWithoutDots[0]);
		assertEquals("person_lastName", adaption.variableNamesWithoutDots[1]);
		assertEquals("employeeId", adaption.variableNamesWithoutDots[2]);
	}

	
	// END Fritz's tests
	
}
