package at.workflow.webdesk;

import java.util.Properties;
import javax.servlet.ServletContext;
import org.springframework.core.io.FileSystemResource;

/**
 * Global helper class which provides access to the realpath and the servlet
 * context of the underlying web application.
 * <p/>
 * Further it holds the application's version and buildnumber, retrieved from
 * Maven-generated <code>version.txt</code> in CLASSPATH.
 * 
 * @author ggruber
 */
public class WebdeskEnvironment {
	
	private static ServletContext servletContext;

	private static String version;
	private static String buildnumber;
	private static String buildtimestamp;
	
	public static ServletContext getServletContext() {
		return servletContext;
	}

	public static void setServletContext(ServletContext servletContext) {
		WebdeskEnvironment.servletContext = servletContext;
	}

	/** @return the directory where the webdesk-webapp is installed within its servlet-container. */
	public static String getRealPath() {
		if (servletContext == null)
			return null;
		
		String realPath= servletContext.getRealPath("/");
		
		realPath = realPath.replaceAll("\\\\","/");
		
        // Tomcat allways delivers an ending Slash, but other servlet containers do not
        // so this makes sure, it is handled the sameway allways.
        if (!realPath.endsWith("/"))
        	realPath = realPath + "/";
        
        return realPath;
	}

	/** @return the webdesk application's version (something like "3.3.0-RC4"). */
	public static String getApplicationVersion()	{
		ensureVersionProperties();
		return version;
	}
	
	/** @return the webdesk application's build-number (unique SVN revision number). */
	public static String getApplicationBuildnumber()	{
		ensureVersionProperties();
		return buildnumber;
	}
	
	/** @return the webdesk application's build-timestamp (date-string representing the build timestamp). */
	public static String getApplicationBuildtimestamp()	{
		ensureVersionProperties();
		return buildtimestamp;
	}
	
	/** @return the webdesk application's version with appenden build-number (separated by one space). */
	public static String getApplicationVersionAndBuildnumber()	{
		return getApplicationVersion()+" "+getApplicationBuildnumber();
	}
	
	
	private static void ensureVersionProperties()	{
		if (version != null)
			return;
			
		String realPath = WebdeskEnvironment.getRealPath();
		try {
			FileSystemResource res = new FileSystemResource(realPath + "/version.txt");
			
			Properties myProps = new Properties();
			myProps.load(res.getInputStream());
			
			version = (String) myProps.get("version");
			buildnumber = (String) myProps.get("buildnumber");
			buildtimestamp = (String) myProps.get("buildtimestamp");
			
		} catch (Exception e) {
			version = "~";
			buildnumber = "~";
		}
	}
}
