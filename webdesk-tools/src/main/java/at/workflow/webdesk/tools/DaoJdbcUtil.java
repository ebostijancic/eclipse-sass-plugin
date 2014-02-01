package at.workflow.webdesk.tools;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import at.workflow.tools.DatabaseConstants;

/**
 * Direct access to persistence layer via Spring JDBC wrapper (see <code>JdbcTemplate</code>).
 * 
 * Created on 28.03.2006
 * @author hentner (Harald Entner)
 * @author fritzberger Oct 2010
 */
public class DaoJdbcUtil extends JdbcDaoSupport implements ApplicationContextAware{
    
	/** Identifier of the Shark data source Spring bean. */
	public static final String SHARK_DATASOURCE_SPRING_BEAN_NAME = "SharkDataSource";

	/** The name of the DataSource object pointing to Webdesk database (not a Spring bean id!), possible argument for execute/query. */
	public static final String DATASOURCE_WEBDESK = DatabaseConstants.DATASOURCE_WEBDESK;
	
	/** The name of the DataSource object pointing to Shark database (not a Spring bean id!), possible argument for execute/query. */
	public static final String DATASOURCE_SHARK = DatabaseConstants.DATASOURCE_SHARK;

	private String dialect;
    private DataSource sharkDataSource;
    private DataSource webdeskDataSource;
    private ApplicationContext applicationContext;
    
    /**
     * Executes the passed SQL statement, typically a DDL statement (create table, alter table, ...).
     * Mind that you must retrieve this object with Spring.getBean().
     * @param sqlString the SQL command to execute.
     * @param dataSource one of DATASOURCE_WEBDESK or DATASOURCE_SHARK,
     * 		the name of the DataSource object to use for the SQL statement.
     */
    public void execute(String sqlString, String dataSource) {
        setDataSource(dataSource);
        getJdbcTemplate().execute(sqlString);
    }
    
    /**
     * Executes the passed SQL statement, typically a DML statement (insert, update, delete).
     * Mind that you must retrieve this object with Spring.getBean().
     * @param sqlString the SQL statement to execute.
     * @param dataSource one of DATASOURCE_WEBDESK or DATASOURCE_SHARK,
     * 		the name of the DataSource object to use for the SQL statement.
     */
    public void update(String sqlString, String dataSource) {
        setDataSource(dataSource);
        getJdbcTemplate().update(sqlString);
    }
    
    /**
     * Executes the given SQL Select statement and returns a list of rows, where
     * each row is a map, where the key is the name of the column and the value
     * the actual column-value.
     * Mind that you must retrieve this object with Spring.getBean().
     * @param sqlString
     * @param dataSource (webdesk or shark)
     * @return List of Maps (each row is a map)
     */
	public List<Map<String, Object>> queryForList(String sqlString, String dataSource) {
        setDataSource(dataSource);
        return getJdbcTemplate().queryForList(sqlString);
    }
    
    
    /**
     * Calls queryForInt() with the passed SQL statement.
     * Mind that you must retrieve this object with Spring.getBean().
     * @param sqlString the SQL command to execute.
     * @param dataSource one of DATASOURCE_WEBDESK or DATASOURCE_SHARK,
     * 		the name of the DataSource object to use for the SQL statement.
     */
    public int queryForInt(String sqlString, String dataSource) {
        setDataSource(dataSource);
        return getJdbcTemplate().queryForInt(sqlString);
    }

    /**
     * @return one of "db2", "mysql, "mssql", "oracle", "hsql", see DatabaseConstants.
     */
    public String getDatabaseVendorAsString() {
		if (getDialect().equals("org.hibernate.dialect.SQLServerDialect"))
			return  DatabaseConstants.MSSQL;
		
		if (getDialect().equals("org.hibernate.dialect.MySQLDialect"))
			return DatabaseConstants.MYSQL;
		
		if (getDialect().equals("org.hibernate.dialect.DB2Dialect"))
			return DatabaseConstants.DB2;
		
		if (getDialect().equals("org.hibernate.dialect.OracleDialect"))
			return DatabaseConstants.ORACLE;
		
		if (getDialect().equals("org.hibernate.dialect.HSQLDialect"))
			return DatabaseConstants.HSQL;
		
		return "";
    }
    
    
    /** Spring injection method. */
    public void setSharkDataSource(DataSource sharkDataSource) {
        this.sharkDataSource = sharkDataSource;
    }

    /** Spring injection method. */
	public DataSource getSharkDataSource() {
		return sharkDataSource;
	}

    /** Spring injection method. */
    public void setWebdeskDataSource(DataSource webdeskDataSource) {
        this.webdeskDataSource = webdeskDataSource;
    }

    /** Spring injection method. */
	public DataSource getWebdeskDataSource() {
		return webdeskDataSource;
	}
    
    /** Spring injection method. */
	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

    /** Spring injection method. */
	public String getDialect() {
		return dialect;
	}

    private void setDataSource(String dataSource) {
        if (dataSource.equals(DATASOURCE_SHARK))	{
        	setSharkDataSource();
            setDataSource(sharkDataSource);
        }
        else if (dataSource.equals(DATASOURCE_WEBDESK))	{
        	assert webdeskDataSource != null : "Webdesk data source is null - application context has not been initialized properly!";
            setDataSource(webdeskDataSource);
        }
    }

	private void setSharkDataSource() {
		if (this.sharkDataSource == null && this.applicationContext.containsBean(SHARK_DATASOURCE_SPRING_BEAN_NAME)) {
			this.sharkDataSource = (DataSource) applicationContext.getBean(SHARK_DATASOURCE_SPRING_BEAN_NAME);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
    
}
