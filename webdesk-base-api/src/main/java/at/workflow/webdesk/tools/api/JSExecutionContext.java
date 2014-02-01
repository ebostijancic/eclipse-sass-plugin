package at.workflow.webdesk.tools.api;

import java.util.Map;

/**
 * A JavaScript execution context that provides performance tuning
 * by reusing a JavaScript engine.
 * 
 * @author sdzuban 26.03.2012
 * @author fritzberger 14.12.2012 refactorings, JavaDoc.
 * @author fritzberger 14.01.2014 removed unused methods, improved JavaDoc, renamed class (was ScriptingContext).
 */
public interface JSExecutionContext
{
	/**
	 * Adds a script variable and its value to this execution context,
	 * to be called before the execution of the script.
	 * @param name the script variable name under which the value should be stored in this context.
	 * @param value the value for the script variable.
	 */
	void putVariable(String name, Object value);

	/**
	 * Removes all currently set variables and sets given ones.
	 * @param variableValues a Map holding key = variable name (never null) and value = variable value (can be null).
	 */
	void setVariables(Map<String,?> variableValues);
	
	/**
	 * Removes all currently set variables and sets given bean as reflective variable source.
	 * @param bean the Object that serves as variable source, whereby any variable is interpreted as bean property.
	 */
	void setBean(Object bean);

}
