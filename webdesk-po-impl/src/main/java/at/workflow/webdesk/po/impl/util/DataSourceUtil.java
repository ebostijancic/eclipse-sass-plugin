package at.workflow.webdesk.po.impl.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import javax.sql.DataSource;

import at.workflow.tools.DatabaseConstants;
import at.workflow.webdesk.po.model.PoDataSourceDefinition;

public final class DataSourceUtil {

	private static final String CLASSNAME_HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
	private static final String CLASSNAME_DB2_DRIVER = "com.ibm.db2.jcc.DB2Driver";
	private static final String CLASSNAME_ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
	private static final String CLASSNAME_MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	private static final String CLASSNAME_MSSQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static final String CLASSNAME_MSSQL_LEGACY_DRIVER = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
	private static final String CLASSNAME_MSSQL_JTDS_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
	
	private static final String JDBC_URL_KEYWORD_HSQLDB = ":hsqldb";
	private static final String JDBC_URL_KEYWORD_JTDS_SQLSERVER = "jtds:sqlserver";
	private static final String JDBC_URL_KEYWORD_DB2 = ":db2";
	private static final String JDBC_URL_KEYWORD_ORACLE = ":oracle";
	private static final String JDBC_URL_KEYWORD_MYSQL = ":mysql";
	private static final String JDBC_URL_KEYWORD_SQLSERVER = "jdbc:sqlserver";
	private static final String JDBC_URL_KEYWORD_MICROSOFT_SQLSERVER = "jdbc:microsoft:sqlserver";


	private static Field getAccessibleField(Object object, String fieldName) {
		assert fieldName != null;
		Class<?> classe = object.getClass();
		Field result = null;
		do {
			for (final Field field : classe.getDeclaredFields()) {
				if (fieldName.equals(field.getName())) {
					result = field;
					break;
				}
			}
			classe = classe.getSuperclass();
		} while (result == null && classe != null);

		result.setAccessible(true);
		return result;
	}
	
	private static boolean isJavaMelodyProxy(DataSource dataSource) {
		return Proxy.isProxyClass(dataSource.getClass())
			&& Proxy.getInvocationHandler(dataSource).getClass().getName()
			.equals("net.bull.javamelody.JdbcWrapper$DelegatingInvocationHandler"); 	
	}
	
	/**
	 * @return a PoDataSourceDefinition built from given DataSource,
	 * 		containing driver, URL, user, password, maxActive, maxIdle.
	 * 
	 * @author sdzuban 03.05.2012
	 */
	public static PoDataSourceDefinition getDefinitionFromDataSource(DataSource dataSource) {
		
		DataSource dsToInspect = dataSource;
		try {
			final PoDataSourceDefinition definition = new PoDataSourceDefinition();
			
			// hack to find out if the datasource is proxyed by the javamelody proxy 
			// JDBCWrapper
			if ( isJavaMelodyProxy(dsToInspect)) {
				dsToInspect = unwrapDataSource(dataSource);
			}
			final Class<?> clazz = dsToInspect.getClass();
			
			definition.setDriver((String) clazz.getDeclaredMethod("getDriverClassName").invoke(dsToInspect));
			definition.setUrl((String) clazz.getDeclaredMethod("getUrl").invoke(dsToInspect));
			definition.setUserName((String) clazz.getDeclaredMethod("getUsername").invoke(dsToInspect));
			definition.setPassword((String) clazz.getDeclaredMethod("getPassword").invoke(dsToInspect));
			definition.setMaxActive((Integer) clazz.getDeclaredMethod("getMaxActive").invoke(dsToInspect));
			definition.setMaxIdle((Integer) clazz.getDeclaredMethod("getMaxIdle").invoke(dsToInspect));
			
			return definition;
		}
		catch (Exception e) {
			final String message = "Failed reading connection properties from DataSource "+dsToInspect+": "+e;
			//throw new PoRuntimeException(message, e);
			return null;
		}
	}

	private static DataSource unwrapDataSource(DataSource dataSource) throws IllegalAccessException {
		DataSource dsToInspect;
		InvocationHandler ih = Proxy.getInvocationHandler(dataSource);
		Object delegate = getAccessibleField(Proxy.getInvocationHandler(dataSource), "delegate").get(ih);
		dsToInspect = (DataSource) getAccessibleField(delegate, "val$dataSource").get(delegate);
		return dsToInspect;
	}
	
	/**
	 * Simple mapper from url to driver class name
	 * @return driver class name
	 */
	public static String extractDriverClassNameFromUrl(String url) {
		
		if (url == null || "".equals(url.trim()))
			return null;
		else if (url.contains( JDBC_URL_KEYWORD_MICROSOFT_SQLSERVER ))
			return CLASSNAME_MSSQL_LEGACY_DRIVER;
		else if (url.contains( JDBC_URL_KEYWORD_SQLSERVER ))
			return CLASSNAME_MSSQL_DRIVER;
		else if (url.contains( JDBC_URL_KEYWORD_MYSQL ))
			return CLASSNAME_MYSQL_DRIVER;
		else if (url.contains( JDBC_URL_KEYWORD_ORACLE ))
			return CLASSNAME_ORACLE_DRIVER;
		else if (url.contains( JDBC_URL_KEYWORD_DB2 ))
			return CLASSNAME_DB2_DRIVER;
		else if (url.contains( JDBC_URL_KEYWORD_JTDS_SQLSERVER ))
			return CLASSNAME_MSSQL_JTDS_DRIVER;
		else if (url.contains( JDBC_URL_KEYWORD_HSQLDB ))
			return CLASSNAME_HSQLDB_DRIVER;
		else
			return null;
	}
	
	/** extract a string describing the database vendor out of the jdbc connection url,
	 * returns one of those: DatabaseConstants.(MYSQL|MSSQL|DB2|ORACLE|HSQL) */
	public static String extractDatabaseVendorFromUrl(String url) {
		if (url == null || "".equals(url.trim()))
			return null;
		else if (url.contains( JDBC_URL_KEYWORD_MICROSOFT_SQLSERVER ) || 
				 url.contains( JDBC_URL_KEYWORD_SQLSERVER ) ||
				 url.contains( JDBC_URL_KEYWORD_JTDS_SQLSERVER ))
			return DatabaseConstants.MSSQL;
		else if (url.contains( JDBC_URL_KEYWORD_MYSQL ))
			return DatabaseConstants.MYSQL;
		else if (url.contains( JDBC_URL_KEYWORD_ORACLE ))
			return DatabaseConstants.ORACLE;
		else if (url.contains( JDBC_URL_KEYWORD_DB2 ))
			return DatabaseConstants.DB2;
		else if (url.contains( JDBC_URL_KEYWORD_HSQLDB ))
			return DatabaseConstants.HSQL;
		else
			return null;
	}

	
	private DataSourceUtil() {}	// do not instantiate
}
