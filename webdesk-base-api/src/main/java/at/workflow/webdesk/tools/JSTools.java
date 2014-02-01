package at.workflow.webdesk.tools;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.tools.api.JSExecutionContext;

/**
 * Wrapper around JavaScript execution engines.
 * Use a reusable ScriptingContext for repetitive calls of the same script (is 3 times faster).
 * <p/>
 * sdzuban 13.11.12: switched to Java 1.6 JavaScript engine, was Mozilla Rhino before.
 * <p/>
 * fri_2013-10-01: it is now legal to use variables that are null, be it in String concatenations
 * 		(evaluates to "null") or arithmetic expressions (evaluates to zero).
 * 
 * @author ggruber
 * @author sdzuban
 * @author fritzberger 2012-12-14 added dynamic reflection scripting context.
 */
public class JSTools
{
	/**
	 * This is meant to be used together with <code>executeJS(String, ScriptingContext)</code>.
	 * Provides a ScriptingContext that contains variable values that will be reused for several scripts,
	 * all executed by the same engine.
	 * @return a reusable scripting context to be used for more than one script (caller keeps the context).
	 */
	public static JSExecutionContext createContext() {
		return createContext(null);
	}
	
	/**
	 * This is meant to be used together with <code>executeJS(String, ScriptingContext)</code>.
	 * Provides a ScriptingContext that contains variable values that will be reused for several scripts,
	 * all executed by the same engine.
	 * @param variableValues the variables as name/value pairs, can be null.
	 * @return a reusable scripting context to be used for more than one script (caller keeps the context).
	 */
	public static JSExecutionContext createContext(Map<String,?> variableValues) {
		return new ReflectionOnDemandContext(variableValues);
	}
	
	/**
	 * This is meant to be used together with <code>executeJS(String, ScriptingContext)</code>.
	 * Provides a ScriptingContext that will dynamically retrieve variable values from the given bean,
	 * using java.lang.reflection. Variable names must be identical with bean property names.
	 * @param variableValuesHoldingeBean the Java Object holding values for variables with same names as bean property names, can be null.
	 * @return a reusable scripting context to be used for more than one script (caller keeps the context).
	 */
	public static JSExecutionContext createContext(Object variableValuesHoldingeBean) {
		return new ReflectionOnDemandContext(variableValuesHoldingeBean);
	}
	
	/**
	 * Executes given JavaScript.
	 * @param javaScript the script source text to execute.
	 * @return the result of the script execution (whatever the scripting-engine returns).
	 */
	public static Object executeJS(String javaScript) {
        return executeJS(javaScript, (Map<String, ?>) null);
    }

	/**
	 * Executes given JavaScript with given variable values.
	 * @param javaScript the script source text to execute.
	 * @param variableValues the values for variables used in script source text.
	 * @return the result of the script execution (whatever the scripting-engine returns).
	 */
	public static Object executeJS(String javaScript, Map<String, ?> variableValues) {
		if (variableValues == null)
			variableValues = new HashMap<String,Object>();
		
		JSExecutionContext context = createContext(variableValues);
		
        context.putVariable("sourceObjMap", variableValues);
        // fri_2012-12-14: Gabriel: this seems to be a necessity for job/connector-scripts running on customer installations
        
        return executeJS(javaScript, context);
    }

	/**
	 * Executes given JavaScript with given variable values.
	 * The ScriptingContext MUST be one retrieved by createContext(),
	 * as this call reuses the JavaScript engine stored in it.
	 * @param javaScript the script source text to execute.
	 * @param variableValues the values for variables used in script source text.
	 * @return the result of the script execution (whatever the scripting-engine returns).
	 */
	public static Object executeJS(String javaScript, JSExecutionContext variableValues) {
        return ((ReflectionOnDemandContext) variableValues).executeScript(javaScript);
    }


	/**
	 * The return structure for cases where variable names would be "person.lastName",
	 * which does not work with JavaScript (violates syntax).
	 * This class contains a script where any "." has been replaced by a "_",
	 * and variable names, where any "." has been replaced by a "_", so that these
	 * two can be executed together.
	 */
	public static class DottedVariableNamesWorkaround
	{
		public final String javaScript;
		public final String [] variableNamesWithoutDots;
		
		DottedVariableNamesWorkaround(String javaScript, String [] variableNamesWithoutDots)	{
			this.javaScript = javaScript;
			this.variableNamesWithoutDots = variableNamesWithoutDots;
		}
	}
	
	/**
	 * See DottedVariableNamesWorkaround.
	 * The returned variable names (without dots) will be in same order as they input names were.
	 */
	public static DottedVariableNamesWorkaround dottedVariableNamesWorkaround(String javaScript, String [] variableNames)	{
		assert StringUtils.isNotEmpty(javaScript);
		
		if (variableNames == null || variableNames.length <= 0)
			return new DottedVariableNamesWorkaround(javaScript, variableNames);
		
		final String [] variableNamesWithoutDots = new String[variableNames.length];
		String javaScriptWithoutDots = javaScript;
		for (int i = 0; i < variableNames.length; i++)	{
			variableNamesWithoutDots[i] = replaceDotByUnderscore(variableNames[i]);
			javaScriptWithoutDots = javaScriptWithoutDots.replaceAll(variableNames[i], variableNamesWithoutDots[i]);
		}
		
		return new DottedVariableNamesWorkaround(javaScriptWithoutDots, variableNamesWithoutDots);
	}
	
	private static String replaceDotByUnderscore(String text)	{
		return text.replaceAll("\\.", "_");
	}
	
	
	/**
	 * Internal helper class that implements javax.script.Bindings and Webdesk JSExecutionContext.
	 * <p/>
	 * Additionally this serves following purposes:
	 * <ul>
	 * 	<li>read a bean property value (by reflection) on demand only (better performance)</li>
	 * 	<li>get around with some reserved words that the JDK engine puts initially ("context", ...)</li>
	 * 	<li>implement a clean treatment of <code>null</code> values (in relation with containsKey and get)</li>
	 * </ul>
	 */
	private static class ReflectionOnDemandContext extends SimpleBindings implements JSExecutionContext
	{
		/**
		 * Before calling get() with actual variable names from the script,
		 * the Java 1.6 JavaScritp engine calls get() with following words.
		 */
		private static final String [] ENGINE_RESERVED_VARIABLE_NAMES = new String []	{
			"context",
			"Function",
			"print",
			"println",
		};
		
		private static final boolean isEngineReservedVariableName(String variableName)	{
			for (String reservedWord : ENGINE_RESERVED_VARIABLE_NAMES)
				if (reservedWord.equals(variableName))
					return true;
			return false;
		}
		
		/** Used as null-value instead of Java null, to be able to distinguish between "undefined" and "null". */
		private static final Object NULL = new Object();

		
	    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
		private /*final*/ Object variableValuesHoldingeBean;
		
		
		/** For internal use only. */
		ReflectionOnDemandContext(Map<String, ?> parameters) {
			putVariables(parameters);
		}
		
		/** For internal use only. */
		ReflectionOnDemandContext(Object variableValuesHoldingeBean)	{
			putBean(variableValuesHoldingeBean);
		}
		
		
		/** Overridden to consider reflection containment. */
		@Override
		public boolean containsKey(Object variableName) {
			get(variableName);	// ensure variable is searched in bean and buffered, ...
			// ... but do not use get() result for a null-check, because NULL would be converted back to null
			return super.get(variableName) != null;
			// because null is never used as value (replaced by NULL), a variable exist when its value is not null
		}
		
		/**
		 * Overridden to try reflection when super does not contain the value.
		 * @param name the (String) name of the variable to get the value for.
		 */
		@Override
		public Object get(final Object name) {
			final Object value = super.get(name);
			
			final String variableName = (String) name;
			final boolean isNull = isNull(value);
			
			// if value has been found, or no bean is present, return the value
			if (isNull == false || variableValuesHoldingeBean == null || isEngineReservedVariableName(variableName))
				return isNull ? null : value;
				
			// no value has been found, or a bean is present, try reflection (and cache value)
			try	{
				final Object reflectiveValue = ReflectionUtils.invokeGetter(variableValuesHoldingeBean, variableName);
				put(variableName, reflectiveValue);
				return reflectiveValue;
			}
			catch (Exception e)	{
				//throw new RuntimeException("Problem when retrieving value of variable "+name+" by reflection: "+e.getMessage(), e);
				//
				// ggruber / fritzberger 2013-04-13:
				// in case of instantiations of java objects inside an expression like 'new java.util.Date()', the former
				// implementation was throwing an exception. This is however not acceptable. So we return null now, which seems to
				// solve our problems and also lets expressions like 'new java.util.Date()' work...
				
				return null;
			}
		}
		
		@Override
		public Object put(String name, Object value) {
			final Object result = super.put(name, isNull(value) ? NULL : value);
			return isNull(result) ? null : result;
		}
		
		// putAll() delegates to put(), no need to override this
		
		@Override
		public final void putVariable(String name, Object value) {
			if (name == null)
				throw new IllegalArgumentException("A variable name can not be null!");
				
			if (name.contains("."))
				throw new IllegalArgumentException("A variable name must not contain dots: "+name);
				
			if (isEngineReservedVariableName(name))
				throw new IllegalArgumentException("Can not accept '"+name+"' as variable, this is a reserved name for Java 1.6 script engine");
			
			put(name, value);
		}
		
		@Override
		public void setVariables(Map<String,?> variableValues) {
			removeAllVariables();
			putVariables(variableValues);
		}
		
		@Override
		public void setBean(Object bean) {
			removeAllVariables();
			putBean(bean);
		}
		
		
		/** Called from surrounding class. Starts the engine with given script and "this" as evaluation context. */
		Object executeScript(String script) {
			try {
				return engine.eval(script, this);
			}
			catch (Exception e) {
				throw new RuntimeException("Error computing value by executing script >"+script+"< was: "+e.getMessage(), e);
			}
		}
		
		
		private final void putVariables(Map<String,?> variableValues) {
			if (variableValues != null)
				for (Map.Entry<String,?> e : variableValues.entrySet())	// do not use putAll() because this wouldn't check for reserved names
					putVariable(e.getKey(), e.getValue());
		}
		
		private final void putBean(Object bean) {
			this.variableValuesHoldingeBean = bean;
		}
		
		private boolean isNull(Object value)	{
			return value == null || value == NULL;
		}
		
		private void removeAllVariables() {
			keySet().clear();
		}
	}

}
