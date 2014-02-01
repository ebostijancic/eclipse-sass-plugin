package at.workflow.webdesk.tools;

import org.apache.commons.lang.StringUtils;

/**
 * Contains naming conventions as implementations, made to be reused.
 * Avoid duplications and hard-coding of naming-conventions!
 * 
 * @author fritzberger 21.05.2013
 */
public final class NamingConventions {
	
	public final static String WEBDESK_NAMESPACE = "at.workflow.webdesk";
	/**
	 * Extracts the Webdesk module name from given class name, i.e. returns "Po" from "at.workflow.webdesk.po.model.PoGroup".
	 * @param className must not be null or empty, represents the fully qualified or simple class name of the class to extract module name from.
	 * @return the module name of given class name.
	 */
	public static String getModuleName(final String className) {
		if (StringUtils.isEmpty(className))
			throw new IllegalArgumentException("The given class name to extract module from is empty!");
		
		final String simpleClassName = TextUtils.getSimpleClassName(className);
		String moduleName = ""+simpleClassName.charAt(0);	// take first char, furthers while they are lower-case
		for (int i = 1; i < simpleClassName.length() && Character.isLowerCase(simpleClassName.charAt(i)); i++)
			moduleName += simpleClassName.charAt(i);

		if (moduleName.length() == simpleClassName.length())
			throw new IllegalArgumentException("Invalid class name, contains no module prefix: "+simpleClassName);
				
		return moduleName;
	}
	
	/**
	 * Drops any package names and the module name from given class name, i.e. returns "Person" for "at.workflow.webdesk.po.model.PoPerson".
	 * @param className must not be null or empty, represents the fully qualified or simple class name of the class to drop module name from.
	 * @return the simple class name of given class, without module prefix.
	 */
	public static String getSimpleClassNameWithoutModuleName(String className)	{
		if (StringUtils.isEmpty(className))
			throw new IllegalArgumentException("The given class name to extract moduel from is empty!");
		
		final String simpleClassName = TextUtils.getSimpleClassName(className);	// "PoPerson"
		final String moduleName = NamingConventions.getModuleName(simpleClassName);	// "Po"

		return simpleClassName.substring(moduleName.length());	// "Person"
	}
	
	/**
	 * Pass the classpath of any webdesk resource which belongs to a module like a actiondescriptor, actionconfig, flowscript file, 
	 * a template, definition, script or image and get back its module name. 
	 * @param classPath of the file, expected to start with "at/workflow/webdesk"
	 * @return the name of the module this resource belongs to
	 */
	public static String getModuleNameOfResource(String classPath) {
		
		final String wdNameSpaceWithSlashes = WEBDESK_NAMESPACE.replace(".", "/");
		
		if (classPath.startsWith( wdNameSpaceWithSlashes )) {
			String ret = classPath.substring( wdNameSpaceWithSlashes.length()+1 ); // assumes string of form at/workflow/webdesk/modulename/something/xx/xx
			if (ret.indexOf("/")>-1)
				return ret.substring(0, ret.indexOf("/"));
			
			return ret;
		}
		throw new IllegalStateException("Supplied classpath=" + classPath + " is not in the webdesk namespace!");
	}
	
	private NamingConventions()	{}	// do not instantiate
}
