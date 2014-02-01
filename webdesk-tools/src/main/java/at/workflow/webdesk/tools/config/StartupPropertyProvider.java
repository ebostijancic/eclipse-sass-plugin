package at.workflow.webdesk.tools.config;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.cocoon.configuration.PropertyProvider;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.impl.RunningModeHelper;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Environment;
import org.springframework.web.context.ServletContextAware;

/**
 * This class is responsible for changing the datasources properties 2 hsqldb 
 * if runningmode is testing or if the webdesk (or system) property
 * webdesk.useEmbeddedDatabase is set to true. 
 * When runningmode=test, the hsqldb will be cleaned before! The hsqldb will
 * be stored inside the java.io.tmpdir in the following structure:
 * shark: ${java.io.tmpdir}/db/shark
 * webdesk: ${java.io.tmpdir}/db/webdesk
 * 
 * if an optional parameter 'webdesk.embeddedDatabasePath' exists, this will
 * be used to set the path to hsqldb.
 * 
 * @FIXME: not fully tested yet --> additional parameter (webdesk.embeddedDatabasePath) 
 * handling open
 * 
 * @author ggruber
 */
public class StartupPropertyProvider implements PropertyProvider, ServletContextAware {

	public static final String WEBDESK_LICENCE_CHECK_DISABLED = "webdesk.licenceCheckDisabled";

	private static final Logger log = Logger.getLogger(StartupPropertyProvider.class);

	/**
	 * @return HSQL-DB properties to be used with in-process mode, for test or production.
	 */
	public static Properties getHsqlDbProperties(String dir) {
		if (!dir.endsWith("/"))
			dir = dir + "/";

		Properties properties = new Properties();

		properties.setProperty(Environment.DIALECT, "org.hibernate.dialect.HSQLDialect");
		
		properties.setProperty(Environment.DRIVER, "org.hsqldb.jdbcDriver");
		properties.setProperty(Environment.URL, "jdbc:hsqldb:file:"+dir+"/webdesk");
		properties.setProperty(Environment.USER, "sa");
		properties.setProperty(Environment.PASS, "");

		properties.setProperty("shark.connection.driver_class", "org.hsqldb.jdbcDriver");
		properties.setProperty("shark.connection.url", "jdbc:hsqldb:file:"+dir+"/shark");
		properties.setProperty("shark.connection.username", "sa");
		properties.setProperty("shark.connection.password", "");
		properties.setProperty("shark.db_loader_job", "hsql");

		properties.setProperty("webdesk.isDistributed", "false");
		properties.setProperty(WEBDESK_LICENCE_CHECK_DISABLED, "true");
        
		return properties;
	}

	/**
	 * Cleans all files from passed directory.
	 * Changes the directory name to a random one if this did not succeed.
	 * Throws an Error if the directory still exists then.
	 * @return the name of the empty temporary directory to establish HSQL-DB.
	 */
	public static String cleanupHsqlWorkingDirectory(final String dir) {
		System.out.println("+++++++ DELETING HSQLDB DIR (runtime context) +++++++");
		File hsqlDbDirectory = new File(dir);
		try {
			FileUtils.deleteDirectory(hsqlDbDirectory);
		}
		catch (IOException e) {
			log.warn("Error when cleaning up HSQL database directory: "+e.getMessage());
		}

		int count = 0;
		while (hsqlDbDirectory.exists() && count < 10)	{
			Thread.yield();	// allow other threads to execute
			count++;
			hsqlDbDirectory = new File(dir+System.currentTimeMillis());	// append unique string to directory name
		}
		
		if (hsqlDbDirectory.exists()) {
			String msg = "Couldn't delete temporary HSQLDB directory: "+dir;
			log.error(msg);
			throw new Error(msg);
			// fri_2010-10-07:
			// need an error here *not* to be caught by cocoon AbstractSettingsBeanFactoryPostProcessor.createSettings()
			// just the logger.warn("Unable to get properties from provider.") would appear on console
			// the effect would be that webdesk connects to any existing local MySqlDB, which is not wanted in this case!
		}
		
		hsqlDbDirectory.mkdir();
		System.out.println("HSQL-DB will reside at " + hsqlDbDirectory.getAbsolutePath());
		return hsqlDbDirectory.getAbsolutePath();
	}

	/**
	 * @return a temporary directory name ${java.io.tempdir}/db, might already exist.
	 */
	public static String getHsqlWorkingDirectory() {
		String dir = System.getProperty("java.io.tmpdir");
		dir = dir.replaceAll("\\\\", "/");
		if (dir.endsWith("/") == false)
			dir += "/";
		return dir+"db";
	}


	private Properties propsToOverride;
	private ServletContext servletContext;
	private String realPath;
	private boolean hsqlDbDeleted = false;

	private void generatePropsToOverride() {
		String dir = getHsqlWorkingDirectory();

		// get realpath
		if (servletContext != null && servletContext.getRealPath("/") != null) {
			realPath = servletContext.getRealPath("/").replaceAll("\\\\", "/");
			if (!realPath.endsWith("/"))
				realPath += "/";

			// if we are not production mode -> check whether database exists...
			if (RunningModeHelper.determineRunningMode("prod").endsWith("prod")) {
				// check for WEB-INF/work/db directory	
				File dirFile = new File(realPath + "WEB-INF/work/db");
				if (dirFile.exists()) {
					// when a previous installation used this path, reuse it !!!!
					dir = realPath + "WEB-INF/work/db";
				}
			}
		}

		if (RunningModeHelper.determineRunningMode("test").endsWith("test") && hsqlDbDeleted == false) {
			dir = cleanupHsqlWorkingDirectory(dir);
			hsqlDbDeleted = true;
		}

		propsToOverride = getHsqlDbProperties(dir);
	}

	/** Implements PropertiesProvider. */
	@Override
	public Properties getProperties(Settings settings, String runningMode, String path) {
		if (settings.getRunningMode().endsWith("test") ||
				settings.getProperty("webdesk.useEmbeddedDatabase") != null && settings.getProperty("webdesk.useEmbeddedDatabase").equals("true") ||
				System.getProperty("webdesk.useEmbeddedDatabase") != null && System.getProperty("webdesk.useEmbeddedDatabase").equals("true")) {

			generatePropsToOverride();

			if (settings.getProperty("webdesk.embeddedDatabasePath") != null) {
				adjustDbPathInProperties(settings);
			}

			return this.propsToOverride;
		}
		return null;
	}

	private void adjustDbPathInProperties(Settings settings) {
		// TODO
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		servletContext = this.servletContext;
	}

}
