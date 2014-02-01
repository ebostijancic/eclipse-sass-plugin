package at.workflow.webdesk.po.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import at.workflow.tools.DatabaseConstants;
import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoDataSourceService;
import at.workflow.webdesk.po.PoGeneralSqlService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.impl.daos.PoDataSourceDefinitionDAOImpl;
import at.workflow.webdesk.po.impl.util.DataSourceUtil;
import at.workflow.webdesk.po.impl.util.JdbcUrlParser;
import at.workflow.webdesk.po.model.PoDataSourceDefinition;
import at.workflow.webdesk.tools.DaoJdbcUtil;

/**
 * Service implementation for handling of PoDataSourceDefinitions and for 
 * holding data sources.
 * 
 * @author sdzuban 03.05.2012
 */
public class PoDataSourceServiceImpl implements PoDataSourceService {

	protected final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private static final String DATA_SOURCE_CLASS_NAME = "org.apache.tomcat.dbcp.dbcp.BasicDataSource";

	private PoDataSourceDefinitionDAOImpl dataSourceDao;
	
	private PoGeneralSqlService sqlService;
	
	private Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
	
	/** {@inheritDoc} */
	@Override
	public PoDataSourceDefinition getDataSourceDefinition(String uid) {
		return dataSourceDao.get(uid);
	}

	/** {@inheritDoc} */
	@Override
	public void saveDataSourceDefinition(PoDataSourceDefinition dataSource) {
		
		if (PoDataSourceService.SHARK.equals(dataSource.getName()) ||
				PoDataSourceService.WEBDESK.equals(dataSource.getName()))
			throw new PoRuntimeException(PoDataSourceService.SHARK + " and " + PoDataSourceService.WEBDESK + " are not allowed as names");
		dataSourceDao.save(dataSource);
		removeFromCaches(dataSource);
	}

	/**
	 * Returns pooled DataSource with given name.
	 * If there is no pooled one a new one is created and stored in pool. 
	 * {@inheritDoc} 
	 */
	@Override
	public DataSource getDataSource(String name) {
		
		if (dataSourceMap.containsKey(name))
			return dataSourceMap.get(name);
		
		PoDataSourceDefinition dataSourceDefinition = dataSourceDao.findDataSourceDefinitionByName(name);
		if (dataSourceDefinition == null)
			return null;
		DataSource newDataSource = getNewDataSource(dataSourceDefinition);
		dataSourceMap.put(name, newDataSource);
		return newDataSource;
	}

	/** {@inheritDoc} */
	@Override
	public void deleteDataSourceDefinition(PoDataSourceDefinition dataSource) {
		dataSourceDao.delete(dataSource);
		removeFromCaches(dataSource);
	}

	/** {@inheritDoc} */
	@Override
	public List<PoDataSourceDefinition> loadAllDataSourceDefinitions() {
		return dataSourceDao.loadAll();
	}

	/** {@inheritDoc} */
	@Override
	public List<String> loadAllDataSourceNames() {
		SortedSet<String> names = new TreeSet<String>(dataSourceMap.keySet());
		List<PoDataSourceDefinition> all = dataSourceDao.loadAll();
		for (PoDataSourceDefinition definition : all)
			names.add(definition.getName());
		List<String> nameList = new ArrayList<String>(names);
		return nameList;
	}

	/** {@inheritDoc} */
	@Override
	public PoDataSourceDefinition findDataSourceDefinitionByName(String name) {
		/** for webdesk and shark there are no definitions in database */
		if (WEBDESK.equals(name) || SHARK.equals(name)) {
			final DataSource dataSource = getDataSource(name);
			if (dataSource != null)
				return getDefinitionFromDataSource(dataSource, name);
			return null;
		}
		return dataSourceDao.findDataSourceDefinitionByName(name);
	}
	
	/** {@inheritDoc} */
	@Override
	public DataSource getDataSource(String driverClassName, String url, String username, String password, int maxIdle, int maxActive) {
		
		PoDataSourceDefinition definition = new PoDataSourceDefinition();
		definition.setDriver(driverClassName);
		definition.setUrl(url);
		definition.setUserName(username);
		definition.setPassword(password);
		definition.setMaxIdle(maxIdle);
		definition.setMaxActive(maxActive);
		
		DataSource dataSource = getNewDataSource(definition);
		if (checkTheConnection(dataSource))
			return dataSource;
		return null;
	}

	public void setDataSourceDao(PoDataSourceDefinitionDAOImpl dataSourceDao) {
		this.dataSourceDao = dataSourceDao;
	}

	/** Do not use. Spring XML noise. */
	public void setWebdesk(DataSource webdesk) {
		dataSourceMap.put(PoDataSourceService.WEBDESK, webdesk);
	}
	
	/** Do not use. {@inheritDoc} */
	@Override
	public void setSharkDataSource() {
		dataSourceMap.put(PoDataSourceService.SHARK, (DataSource) WebdeskApplicationContext.getBean(DaoJdbcUtil.SHARK_DATASOURCE_SPRING_BEAN_NAME));
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkTheConnection(PoDataSourceDefinition dataSourceDefinition) {
		
		DataSource dataSource = getNewDataSource(dataSourceDefinition);
		try {
			Connection conn = dataSource.getConnection();
			conn.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

//	-------------------------- PRIVATE METHODS -------------------------------------
	

	private void removeFromCaches(PoDataSourceDefinition dataSourceDef) {

		sqlService.removeFromCache(getDataSource(dataSourceDef.getName()));
		dataSourceMap.remove(dataSourceDef.getName());
	}

	private DataSource getNewDataSource(PoDataSourceDefinition dataSourceDefinition) {
		
		DataSource newDataSource = null;
		try {
			newDataSource = instantiateNewDataSource(dataSourceDefinition);
		} catch (Exception e) {
			logger.error("Exception while creating new Data Source: " + e, e);
			throw new PoRuntimeException("Exception while creating new Data Source: " + e, e);
		}
		
		return newDataSource;
	}

	private DataSource instantiateNewDataSource(PoDataSourceDefinition dataSourceDefinition) throws Exception {
		
		Class<?> dataSourceClass = Class.forName(DATA_SOURCE_CLASS_NAME);
		Constructor<?> constructor = dataSourceClass.getConstructor();
		
		DataSource newDataSource = (DataSource) constructor.newInstance();
		
		Method setDriverClassName = dataSourceClass.getDeclaredMethod("setDriverClassName", String.class);
		setDriverClassName.invoke(newDataSource, dataSourceDefinition.getDriver());
		
		Method setUrl = dataSourceClass.getDeclaredMethod("setUrl", String.class);
		setUrl.invoke(newDataSource, dataSourceDefinition.getUrl());
		
		Method setUsername = dataSourceClass.getDeclaredMethod("setUsername", String.class);
		setUsername.invoke(newDataSource, dataSourceDefinition.getUserName());
		
		Method setPassword = dataSourceClass.getDeclaredMethod("setPassword", String.class);
		setPassword.invoke(newDataSource, dataSourceDefinition.getPassword());
		
		Method setMaxActive = dataSourceClass.getDeclaredMethod("setMaxActive", int.class);
		setMaxActive.invoke(newDataSource, dataSourceDefinition.getMaxActive());
		
		Method setMaxIdle = dataSourceClass.getDeclaredMethod("setMaxIdle", int.class);
		setMaxIdle.invoke(newDataSource, dataSourceDefinition.getMaxIdle());
		
		return newDataSource;
	}

	private PoDataSourceDefinition getDefinitionFromDataSource(DataSource dataSource, String name) {
		try {
			PoDataSourceDefinition definition = DataSourceUtil.getDefinitionFromDataSource(dataSource);
			definition.setName(name);
			return definition;
		}
		catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	// checks that the connection is real
	private boolean checkTheConnection(DataSource dataSource) {
		try {
			Connection conn = dataSource.getConnection();
			conn.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void setSqlService(PoGeneralSqlService sqlService) {
		this.sqlService = sqlService;
	}

	@Override
	public String getDatabaseVendor(DataSource dataSource) {
		
		PoDataSourceDefinition dataSourceDef = DataSourceUtil.getDefinitionFromDataSource(dataSource);
		return DataSourceUtil.extractDatabaseVendorFromUrl( dataSourceDef.getUrl() );
	}

	@Override
	public String guessDefaultSchema(DataSource dataSource) {
		
		PoDataSourceDefinition dataSourceDef = DataSourceUtil.getDefinitionFromDataSource(dataSource);
		if (dataSourceDef.getUrl()!=null) {
		
			String vendor = DataSourceUtil.extractDatabaseVendorFromUrl( dataSourceDef.getUrl() );
			if (DatabaseConstants.MYSQL.equals( vendor )) {
				return parseNameFromUrl(dataSourceDef);
			} else if ( DatabaseConstants.MSSQL.equals( vendor )) {
				return parseNameFromUrl(dataSourceDef);
			} else if ( DatabaseConstants.ORACLE.equals( vendor )) {
				return dataSourceDef.getUserName();
			}
			
		}
		return null;
	}

	private String parseNameFromUrl(PoDataSourceDefinition dataSourceDef) {
		JdbcUrlParser parser = new JdbcUrlParser( dataSourceDef.getUrl() );
		parser.parse();
		return parser.getDatabaseName();
	}
	
}
