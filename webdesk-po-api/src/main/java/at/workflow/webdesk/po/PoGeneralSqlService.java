package at.workflow.webdesk.po;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import at.workflow.webdesk.tools.PaginableQuery;


/**
 * This service provides methods 
 * to access, create, modify and delete database records.
 * <p/>
 * The database is specified indirectly as DataSource.
 * 
 * @author sdzuban
 * @author fritzberger 2012-12-17 added JDBC-based getConstraintNames().
 */
public interface PoGeneralSqlService {

	/** 
	 * This is the universal generic method for all three currently defined Query classes.
	 * It processes queries without parameters, with positional parameters, with named parameters.
	 * Returned Maps are CaseInsensitiveMaps, i.e. the case of the key is ignored.
	 * It is same as selectRecords with different signature.
	 * ALIASING:
	 * Table aliases: In the result maps table aliases are ignored and only column names are used solely as keys.
	 * Column aliases: in the result maps column aliases are used as keys.
	 * @return List of Maps term -> value.
	 */
	List<Map<String, ?>> select(DataSource dataSource, PaginableQuery query);
	
	/**
	 * TODO: this method only exists due to the incompatibility of
	 * List<Map<String, Object>> (QueryReportTreeTableGenerator) with List<Map<String, ?>> (Connector).
	 * <p/>
	 * This is the universal generic method for all three currently defined Query classes.
	 * It processes queries without parameters, with positional parameters, with named parameters.
	 * Returned Maps are CaseInsensitiveMaps, i.e. the case of the key is ignored.
	 * It is same as select with different signature.
	 * ALIASING:
	 * Table aliases: In the result maps table aliases are ignored and only column names are used solely as keys.
	 * Column aliases: in the result maps column aliases are used as keys.
	 * @return List of Maps term -> value as Object.
	 */
	List<Map<String, Object>> selectRecords(DataSource dataSource, PaginableQuery query);
	
	/**
	 * This is the method for executing DDL statements like create table, drop table etc.
	 * @param dataSource the database instance where to launch the statement.
	 * @param sqlStatement e.g. create Table, drop table
	 */
	void execute(DataSource dataSource, String sqlStatement);
	
	/**
	 * This is the universal method for insert, update and delete (prepared) statement processing.
	 * @param dataSource the database instance where to launch the statement.
	 * @param statementWithParameters an SQL statement with positional or named parameters,
	 * 		and the values for these parameters.
	 * @return the number of rows affected
	 */
	int execute(DataSource dataSource, PaginableQuery statementWithParameters);
	
	/**
	 * This is the method for inserting one row of named values into table
	 * Warning! The keys in the namedValues Map must be same as column names.
	 * Not-null and unique constraints must be obeyed 
	 * @return the number of rows affected by statement
	 */
	int insert(DataSource dataSource, String tableName, Map<String, Object> namedValues);
	
	/**
	 * This is the method for updating one row of named values in the table
	 * Warning! The keys in the namedValues Map must be same as column names.
	 * Unique constraints must be obeyed 
	 * @return the number of rows affected by statement
	 */
	int update(DataSource dataSource, String tableName, String primaryKeyName, Map<String, Object> namedValues);
	
	/**
	 * This is the method for inserting or updating of one row of named values.
	 * Warning! The keys in the namedValues Map must be same as column names.
	 * Unique constraints must be obeyed 
	 * @return the number of rows affected by statement
	 */
	int insertOrUpdate(DataSource dataSource, String tableName, String primaryKeyName, Map<String, Object> namedValues);
	
	/**
	 * This is the method for deleting of one row 
	 * @return the number of rows affected by statement
	 */
	int delete(DataSource dataSource, String tableName, String primaryKeyName, Object primaryKeyValue);
	
	/**
	 * This is the universal method for insert, update and delete statement batch processing.
	 * @return array of the number of rows affected by each statement
	 */
	int[] batchExecute(DataSource dataSource, String[] sqls);
	
	/**
	 * This is the universal method for insert, update and delete prepared statement batch processing.
	 * @param parameterValues for each of list element (the Object[]) the sql statement will be executed
	 * @return array of the number of rows affected by each statement
	 */
	int[] batchExecute(DataSource dataSource, String sql, List<Object[]> parameterValues);
	
	/**
	 * This is the universal method for insert, update and delete prepared statement batch processing.
	 * @param parameterNamesInCorrectOrder names of attributes in the order the values are to be set to prepered statement
	 * @param parameterValues for each of list element (the Map) the sql statement will be executed
	 * @return array of the number of rows affected by each statement
	 */
	int[] batchExecute(DataSource dataSource, String sql, List<String> parameterNamesInCorrectOrder, List<Map<String, Object>> namedValues);
	
	/**
	 * This is the method for inserting multiple rows of named values into a table
	 * Warning! The keys in the namedValues Maps must be same as column names.
	 * Not-null and unique constraints must be obeyed
	 * @return array of the number of rows affected by each statement
	 */
	int[] batchInsert(DataSource dataSource, String tableName, List<Map<String, Object>> namedValuesList);
	
	/**
	 * This is the method for batch updating of many rows of named values in the table
	 * Warning! The keys in the namedValues Maps must be same as column names.
	 * Unique constraints must be obeyed 
	 * @return the array of the number of rows affected by each statement
	 */
	int[] batchUpdate(DataSource dataSource, String tableName, String primaryKeyName, List<Map<String, Object>> namedValuesList);
	
	/**
	 * This is the method for batch inserting or updating of many rows of named values.
	 * It first sorts rows into two lists, one for insert and one for updated.
	 * Than two batches are processed.
	 * Warning! The keys in the namedValues Maps must be same as column names.
	 * Unique constraints must be obeyed 
	 * @return two dimensional array of the number of rows affected by insert and by update
	 */
	int[] batchInsertOrUpdate(DataSource dataSource, String tableName, String primaryKeyName, List<Map<String, Object>> namedValuesList);
	
	/**
	 * same as {@link #batchInsertOrUpdate(DataSource, String, String, List)} but with additional parameter to
	 * pass a comparator which is used to check, whether it is necessary to really update a given record by 
	 * comparing source and destination values. This way UPDATE operations can be minimized.
	 */
	int[] batchInsertOrUpdate(DataSource dataSource, String tableName, String primaryKeyName, List<Map<String, Object>> namedValuesList, Comparator<Map<String,Object>> comparator);
	
	/**
	 * This is the method for deleting of multiple rows 
	 * @return the number of rows affected by each primary key value
	 */
	int[] batchDelete(DataSource dataSource, String tableName, String primaryKeyName, List<?> primaryKeyValues);
	
	/**
	 * Removes jdbcTemplate instantiated with dataSource from internal cache
	 * @param dataSource
	 */
	void removeFromCache(DataSource dataSource);
	
	/**
	 * @return true if the primaryKeyValue is already contained in primary key column
	 */
	boolean containsRecord(DataSource dataSource, String tableName, String primaryKeyName, Object primaryKeyValue);
	
	/**
	 * @return names of columns of the table,
	 * 		their cases being database specific (HSQL: always upper-case, MySQL: as defined in data definition, ...).
	 */
	List<String> getColumnNames(DataSource dataSource, String tableName);
	
	/**
	 * @return names of classes of columns of the table,
	 * 		their cases being database specific (HSQL: always upper-case, MySQL: as defined in data definition, ...).
	 */
	Map<String, String> getColumnClassNames(DataSource dataSource, String tableName);
	
	/**
	 * @return names of columns of the generated resultset the passed sql produces,
	 * 		their cases being database specific (HSQL: always upper-case, MySQL: as defined in data definition, ...).
	 */
	List<String> getColumnNamesFromSQL(DataSource dataSource, String sql);
	
	/**
	 * @return names of classes of columns of the generated resultset the passed sql produces,
	 * 		their cases being database specific (HSQL: always upper-case, MySQL: as defined in data definition, ...).
	 */
	Map<String, String> getColumnClassNamesFromSQL(DataSource dataSource, String sql);

	/**
	 * @return mandatoriness of columns of the table,
	 * 		their cases being database specific (HSQL: always upper-case, MySQL: as defined in data definition, ...).
	 */
	Map<String, Boolean> getColumnMandatoriness(DataSource dataSource, String tableName);
	
	
	
	/** Map key in Map returned from getConstraintNames(), standing for the list of primary key constraint names. */
	String PRIMARY_KEY_CONSTRAINT_TAG = "PK";
	
	/** Map key in Map returned from getConstraintNames(), standing for the list of foreign key constraint names. */
	String FOREIGN_KEY_CONSTRAINT_TAG = "FK";
	
	/** Map key in Map returned from getConstraintNames(), standing for the list of index names. */
	String INDEX_CONSTRAINT_TAG = "IX";
	
	/**
	 * Reads all available constraints and indexes on given table: primary keys, foreign keys ('imported'), indexes.
	 * This can be used for DROP statements.
	 * <p/>
	 * TODO: rework to a list of constraint names paired with column names,
	 * 		so that caller has a chance to find out to which column a constraint relates to.
	 * 
	 * @return a map (never null) with
	 * 		key = one of PRIMARY_KEY_CONSTRAINT_TAG, FOREIGN_KEY_CONSTRAINT_TAG, INDEX_CONSTRAINT_TAG, and
	 * 		value = list of names of the according constraints on given table.
	 */
	Map<String,List<String>> getConstraintNames(DataSource dataSource, String tableName);

}
