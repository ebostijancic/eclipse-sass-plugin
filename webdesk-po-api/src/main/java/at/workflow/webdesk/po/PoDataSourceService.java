package at.workflow.webdesk.po;

import java.util.List;

import javax.sql.DataSource;

import at.workflow.tools.DatabaseConstants;
import at.workflow.webdesk.po.model.PoDataSourceDefinition;

/**
 * This service provides methods for manipulation of data source definitions
 * and for holding all opened data sources.
 * 
 * @author sdzuban 03.05.2012
 */
public interface PoDataSourceService {

	/** See DatabaseConstants.DATASOURCE_SHARK. */
	String SHARK = DatabaseConstants.DATASOURCE_SHARK;
	
	/** See DatabaseConstants.DATASOURCE_WEBDESK. */
	String WEBDESK = DatabaseConstants.DATASOURCE_WEBDESK;

	PoDataSourceDefinition getDataSourceDefinition(String uid);
	
	void saveDataSourceDefinition(PoDataSourceDefinition definition);
	
	void deleteDataSourceDefinition(PoDataSourceDefinition definition);
	
	List<PoDataSourceDefinition> loadAllDataSourceDefinitions();
	
	PoDataSourceDefinition findDataSourceDefinitionByName(String name);

	/**
	 * @return list of names of instantiated and not instantiated DataSources  
	 */
	List<String> loadAllDataSourceNames();
	
	/** 
	 * @return java datasource, if webdesk is passed, returns Webdesk Datasources
	 * if shark is passed, returns Shark Datasource.  If dataSourceName could not be found, returns null.
	 * 
	 */
	DataSource getDataSource(String dataSourceName);
	
	/** 
	 * This method creates temporary data source that is only returned to the caller of the method 
	 * and not inserted into the cache so any other reuse is not possible.
	 * Only data sources of data source definitions stored in the database can be reused.
	 * @return java datasource with given parameters or null if no connection was possible to establish
	 */
	DataSource getDataSource(String driverClassName, String url, String username, String password, int maxIdle, int maxActive);
	
	/**
	 * @param dataSourceDefinition
	 * @return true if it is possible to establish connection with the dataSourceDefinition
	 */
	boolean checkTheConnection(PoDataSourceDefinition dataSourceDefinition);
	
	/**
	 * Do not call this method, it is for initialization of the Shark data-source bean
	 * in WfStartupImpl only (which is not predefined in unit tests).
	 */
	void setSharkDataSource();
	
	/** extract the database vendor by analyzing the JDBC URL */
	String getDatabaseVendor(DataSource dataSource);
	
	/** try to extract the database/schema name out of the URL of the datasource, if possible.
	 * Only works reliable for MS-SQL, MySQL when defining a default schema in the JDBC URL */
	String guessDefaultSchema(DataSource dataSource);

}
