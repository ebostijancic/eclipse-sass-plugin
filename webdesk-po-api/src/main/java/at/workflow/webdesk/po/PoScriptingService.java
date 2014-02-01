package at.workflow.webdesk.po;

import java.util.Map;

import at.workflow.webdesk.tools.api.JSExecutionContext;

/**
 * Scripting languages facade.
 * 
 * @author ggruber
 */
public interface PoScriptingService {

	// Java-Script language
	
	/**
	 * Use this for variable values that stay the same for different scripts,
	 * which should all be executed in the same scripting engine instance (for performance reasons).
	 * @param variableValues that do not change and will be used with same engine but different scripts.
	 * @return a JavaScript variable-value map that also contains a scripting engine instance.
	 */
	public JSExecutionContext createContext(Map<String, ?> variableValues);
	
	/**
	 * Use this for variable values that should be retrieved and cached locally from given bean,
	 * whereby variable names map to bean property names. All calls with this context will be executed in same scripting engine.
	 * @param variableValuesHoldingeBean the Java Object holding values for variables with same names as bean property names.
	 * @return a reusable scripting context to be used for more than one script (caller keeps the context).
	 */
	public JSExecutionContext createContext(Object variableValuesHoldingBean);
	
	/**
	 * Executes given JavaScript source and returns the execution result.
	 * Mind that in JavaScript variables are NOT referenced using '$' prefix!
	 * @param javaScript the JavaScript source text to execute.
	 * @param variableValuesContext an execution context retrieved by createContext(variableValues).
	 * @return Object which is returned.
	 */
	public Object executeJS(String javaScript, JSExecutionContext variableValuesContext);

	/**
	 * Executes given JavaScript source and returns the execution result.
	 * Mind that in JavaScript variables are NOT referenced using '$' prefix!
	 * The "appCtx" variable holding a JSWrapper to access getBean() of ApplicationContext
	 * is available! See WDPTM-517.
	 * 
	 * @param javaScript the JavaScript source text to execute.
	 * @param variableValuesMap contains parameter values for script execution.
	 */
	public Object executeJS(String javaScript, Map<String, Object> variableValuesMap);


	// Velocity templating language
	
	/**
	 * Substitutes the passed text using Velocity template-engine.
	 * Mind that in Velocity variables are referenced using '$' prefix!
	 * The following built-in variables are available: 
	 * <ul>
	 * <li>"appCtx" (a JSWrapper over the spring applicationContext in order to call getBean()), see WDPTM-517,</li>
	 * <li>"DateTools" (holding Webdesk class DateTools),</li>
	 * <li>"DateUtils" (holding apache-commons class DateUtils),</li>
	 * <li>"StringUtils" (holding apache-commons class StringUtils),</li>
	 * <li>"simpleObjectFactory" (holding an instance of SimpleObjectFactory to be able to instantiate objects in Velocity ...)</li>
	 * </ul> 
	 * 
	 * @param template the text containing place-holders.
	 * @param placeholderValues the map containing real values for place-holders.
	 * @return the substituted text.
	 */
	public String velocitySubstitution(String template, Map<String,Object> placeholderValues);

}
