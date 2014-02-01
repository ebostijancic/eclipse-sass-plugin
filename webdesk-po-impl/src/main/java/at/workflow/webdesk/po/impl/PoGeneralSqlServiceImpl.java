package at.workflow.webdesk.po.impl;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.CallableStatementCreatorFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoDataSourceService;
import at.workflow.webdesk.po.PoGeneralSqlService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.impl.util.DataSourceUtil;
import at.workflow.webdesk.po.model.PoDataSourceDefinition;
import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.PaginableQuery;
import at.workflow.webdesk.tools.PaginationUtil;
import at.workflow.webdesk.tools.PositionalQuery;

/**
 * JdbcTemplate based implementation of PoGeneralSqlService.
 * 
 * @author sdzuban 03.05.2012
 * @author fritzberger 2012-12-17 added JDBC-based getConstraintNames().
 */
public class PoGeneralSqlServiceImpl implements PoGeneralSqlService {

//	----------------------------- INNER CLASSES -----------------------------------
	
	/**
	 * Setter of parameter values to prepared statement in batch processing.
	 * Most common value types are first in the long if/else statement.
	 * @author sdzuban 14.05.2012
	 */
	public abstract class BatchStatementParameterSetter extends AbstractInterruptibleBatchPreparedStatementSetter {

		protected void setValue(PreparedStatement ps, int paramIdx, Object value) throws SQLException {

			// jdbc counts from 1 -> paramIdx + 1 
			final int sqlParamIdx = paramIdx + 1;
			if (value instanceof String)
				ps.setString(sqlParamIdx, (String) value);
			else if (value instanceof Date)
				ps.setDate(sqlParamIdx, new java.sql.Date(((Date) value).getTime()));
			else if (value instanceof Boolean)
				ps.setBoolean(sqlParamIdx, (Boolean) value);
			else if (value instanceof Integer)
				ps.setInt(sqlParamIdx, (Integer) value);
			else if (value instanceof Long)
				ps.setLong(sqlParamIdx, (Long) value);
			else if (value instanceof Float)
				ps.setFloat(sqlParamIdx, (Float) value);
			else if (value instanceof Double)
				ps.setDouble(sqlParamIdx, (Double) value);
			else if (value instanceof BigDecimal)
				ps.setBigDecimal(sqlParamIdx, (BigDecimal) value);
			else if (value instanceof Blob)
				ps.setBlob(sqlParamIdx, (Blob) value);
			else if (value instanceof Byte)
				ps.setByte(sqlParamIdx, (Byte) value);
			else if (value instanceof byte[])
				ps.setBytes(sqlParamIdx, (byte[]) value);
			else if (value instanceof Clob)
				ps.setClob(sqlParamIdx, (Clob) value);
			else if (value instanceof Ref)
				ps.setRef(sqlParamIdx, (Ref) value);
			else if (value instanceof Short)
				ps.setShort(sqlParamIdx, (Short) value);
			else if (value instanceof Array)
				ps.setArray(sqlParamIdx, (Array) value);
			else if (value instanceof Time)
				ps.setTime(sqlParamIdx, (Time) value);
			else if (value instanceof Timestamp)
				ps.setTimestamp(sqlParamIdx, (Timestamp) value);
			else if (value instanceof URL)
				ps.setURL(sqlParamIdx, (URL) value);
			else
				ps.setObject(sqlParamIdx, value);
		}
	}
	
	/**
	 * Setter of positional parameter values to prepared statement in batch processing.
	 * @author sdzuban 14.05.2012
	 */
	public class BatchStatementPositionalParameterSetter extends BatchStatementParameterSetter {
		
		private List<Object[]> parameterValues;
		
		public BatchStatementPositionalParameterSetter (List<Object[]> parameterValues) {
			this.parameterValues = parameterValues;
		}
		
		/** {@inheritDoc} */
		@Override
		protected boolean setValuesIfAvailable(PreparedStatement ps, int statementIdx) throws SQLException {
			
			boolean iCanProcessOne = statementIdx < parameterValues.size();
			if (iCanProcessOne) {
				Object[] values = parameterValues.get(statementIdx);
				
				for (int paramIdx = 0; paramIdx < values.length; paramIdx++) {
					
					Object value = values[paramIdx];
					setValue(ps, paramIdx, value);
				}
			}
			return iCanProcessOne;
		}
	}
	
	/**
	 * Setter of named parameter values to prepared statement in batch processing.
	 * @author sdzuban 14.05.2012
	 */
	public class BatchStatementNamedParameterSetter extends BatchStatementParameterSetter {
		
		private List<Map<String, Object>> parameterValues;
		private List<String> namesInRightOrder;
		
		public BatchStatementNamedParameterSetter (List<String> namesInRightOrder, List<Map<String, Object>> parameterValues) {
			this.parameterValues = parameterValues;
			this.namesInRightOrder = namesInRightOrder;
		}
		
		/** {@inheritDoc} */
		@Override
		protected boolean setValuesIfAvailable(PreparedStatement ps, int statementIdx) throws SQLException {
			
			boolean iCanProcessOne = statementIdx < parameterValues.size();
			if (iCanProcessOne) {
				Map<String, ?> values = parameterValues.get(statementIdx);
				
				for (int paramIdx = 0; paramIdx < namesInRightOrder.size(); paramIdx++) {
					
					Object value = values.get(namesInRightOrder.get(paramIdx));
					setValue(ps, paramIdx, value);
				}
			}
			return iCanProcessOne;
		}
	}
	
//	------------------------ FIELDS AND PUBLIC METHODS ------------------------------
	
	protected final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Map<DataSource, JdbcTemplate> jdbcTemplateMap = new HashMap<DataSource, JdbcTemplate>();

	@Override
	public void removeFromCache(DataSource dataSource) {

		jdbcTemplateMap.remove(dataSource);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, ?>> select(DataSource dataSource, PaginableQuery query) {
		return getListOfMaps(dataSource, query);
	}
		
	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectRecords(DataSource dataSource, PaginableQuery query) {
		return getListOfMaps(dataSource, query);
	}
			
	/**
	 * CAUTION: this uses a fake pagination, it paginates by list operations after reading all queried records!
	 * Processes queries without parameters, with positional parameters, with named parameters.
	 * Returned Maps are CaseInsensitiveMaps, i.e. the case of the key is ignored.
	 * It is same as select with different signature.
	 * ALIASING:
	 * Table aliases: In the result maps table aliases are ignored and only column names are used solely as keys.
	 * Column aliases: in the result maps column aliases are used as keys.
	 * 
	 * @deprecated this uses a fake pagination, and is used by just one caller.
	 */
	@SuppressWarnings({ "rawtypes" })
	private List getListOfMaps(DataSource dataSource, PaginableQuery query) {
		
		if (query == null)
			throw new PoRuntimeException("No query supplied for query");
		
		if (query.getQueryText() == null || !query.getQueryText().trim().toLowerCase().startsWith("select")) {
			logger.error("Not a SELECT statement: " + query.getQueryText());
			throw new PoRuntimeException("Not a SELECT statement: " + query.getQueryText());
		}
		
		JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
		PaginableQuery myQuery = query;
		if (query instanceof NamedQuery)
			myQuery = ((NamedQuery) query).getPositionalQuery();
		
		jdbcTemplate.setMaxRows(myQuery.getFirstResult() + myQuery.getMaxResults());

		loggerDebug("Query with statement: ", myQuery);
		
		List<Map<String, Object>> result = null;
		try {
			if (myQuery instanceof PositionalQuery)
				result = jdbcTemplate.queryForList(myQuery.getQueryText(), ((PositionalQuery) myQuery).getParamValues());
			else
				result = jdbcTemplate.queryForList(myQuery.getQueryText());
		}
		catch (Exception e) {
			final String message = "Exception " + e + " while making query with " + myQuery.getQueryText();
			handleException(myQuery, e, message);
		}
		return PaginationUtil.getPaginatedResult(query, result);
	}


	/** {@inheritDoc} */
	@Override
	public void execute(DataSource dataSource, String ddlStatement) {
		
		if (StringUtils.isBlank(ddlStatement))
			throw new PoRuntimeException("No DDL statement supplied for execute");

		loggerDebug("Execute with DDL statement: ", ddlStatement);

		try {
			JdbcOperations jdbcTemplate = getJdbcTemplate(dataSource);
			jdbcTemplate.execute(ddlStatement);
		} catch (Exception e) {
			final String message = "Exception " + e + " while making update with " + ddlStatement;
			logger.error(message + ddlStatement);
			throw new PoRuntimeException(message, e);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public int execute(DataSource dataSource, PaginableQuery query) {
		
		if (query == null)
			throw new PoRuntimeException("No query supplied for update");
		
		final String sql = query.getQueryText();
		checkUpdateStatement(sql);
		
		PaginableQuery myQuery = query;
		if (query instanceof NamedQuery)
			myQuery = ((NamedQuery) query).getPositionalQuery();

		loggerDebug("Update with statement: ", myQuery);
		
		int result = 0;
		try {
			JdbcOperations jdbcTemplate = getJdbcTemplate(dataSource);
			if (myQuery instanceof PositionalQuery)
				result = jdbcTemplate.update(myQuery.getQueryText(), ((PositionalQuery) myQuery).getParamValues());
			else
				result = jdbcTemplate.update(myQuery.getQueryText());
		} catch (Exception e) {
			final String message = "Exception " + e + " while making update with " + myQuery.getQueryText();
			handleException(myQuery, e, message);
		}
		return result;
	}


	/** {@inheritDoc} */
	@Override
	public int insert(DataSource dataSource, String tableName, Map<String, Object> namedValues) {
		
		if (tableName == null || "".equals(tableName))
			throw new PoRuntimeException("No table name defined for insert.");
		
		if (namedValues == null || namedValues.isEmpty())
			throw new PoRuntimeException("No values provided for insert.");
		
		PositionalQuery query = getInsertQuery(tableName, namedValues); 
		return execute(dataSource, query);
	}

	/** {@inheritDoc} */
	@Override
	public int update(DataSource dataSource, String tableName, String primaryKeyName, Map<String, Object> namedValues) {
		
		if (tableName == null || "".equals(tableName))
			throw new PoRuntimeException("No table name defined for insert.");
		
		if (primaryKeyName == null || "".equals(primaryKeyName))
			throw new PoRuntimeException("No primaryKeyName defined for insert.");
		
		if (namedValues == null || namedValues.isEmpty())
			throw new PoRuntimeException("No values provided for insert.");
		
		PositionalQuery query = getUpdateQuery(tableName, primaryKeyName, namedValues);
		
		// special case where no useful update statement can
		// be created, in that case the number of records updated is zero.
		if (query==null)
			return 0;
		
		return execute(dataSource, query);
	}
	
	/** {@inheritDoc} */
	@Override
	public int insertOrUpdate(DataSource dataSource, String tableName, String primaryKeyName, Map<String, Object> namedValues) {
		
		if (containsRecord(dataSource, tableName, primaryKeyName, namedValues.get(primaryKeyName)))
			return update(dataSource, tableName, primaryKeyName, namedValues);
		return insert(dataSource, tableName, namedValues);
	}

	/** {@inheritDoc} */
	@Override
	public int delete(DataSource dataSource, String tableName, String primaryKeyName, Object primaryKeyValue) {
		
		if (tableName == null || "".equals(tableName))
			throw new PoRuntimeException("No table name defined for delete.");
		
		if (primaryKeyName == null || "".equals(primaryKeyName))
			throw new PoRuntimeException("No primaryKeyName defined for delete.");
		
		if (primaryKeyValue == null)
			throw new PoRuntimeException("No primary key value provided for delete.");
		
		String sql = "delete from " + tableName + " where " + primaryKeyName + " = ?";
		PositionalQuery query = new PositionalQuery(sql, new Object[] {primaryKeyValue});
		return execute(dataSource, query);
	}

	
//	------------------------------ BATCH METHODS -------------------------------
	
	/** {@inheritDoc} */
	@Override
	public int[] batchExecute(DataSource dataSource, String[] sqls) {
		
		if (sqls == null)
			throw new PoRuntimeException("No query supplied for batch update");
		
		for (String sql : sqls) {
			checkUpdateStatement(sql);

			loggerDebug("Batch update with statement: ", sql);
		}
		
		int[] result = null;
		try {
			JdbcOperations jdbcTemplate = getJdbcTemplate(dataSource);
			result = jdbcTemplate.batchUpdate(sqls);
		} catch (Exception e) {
			final String message = "Exception " + e + " while making batch update with " + Arrays.asList(sqls);
			handleException(e, message);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public int[] batchExecute(DataSource dataSource, String sql, List<Object[]> parameterValues) {
		
		if (sql == null)
			throw new PoRuntimeException("No query supplied for batch update");
		
		checkUpdateStatement(sql);
			
		loggerDebug("Batch update with statement: ", sql);
		
		int[] result = null;
		try {
			JdbcOperations jdbcTemplate = getJdbcTemplate(dataSource);
			result = jdbcTemplate.batchUpdate(sql, new BatchStatementPositionalParameterSetter(parameterValues));
		} catch (Exception e) {
			final String message = "Exception " + e + " while making batch update with " + sql;
			handleException(e, message);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public int[] batchExecute(DataSource dataSource, String sql, List<String> namesInRightOrder, List<Map<String, Object>> namedValues) {
		
		if (sql == null)
			throw new PoRuntimeException("No query supplied for batch update");
		
		checkUpdateStatement(sql);
			
		loggerDebug("Batch update with statement: ", sql);
		
		int[] result = null;
		try {
			JdbcOperations jdbcTemplate = getJdbcTemplate(dataSource);
			result = jdbcTemplate.batchUpdate(sql, new BatchStatementNamedParameterSetter(namesInRightOrder, namedValues));
		} catch (Exception e) {
			final String message = "Exception " + e + " while making batch update with " + sql;
			handleException(e, message);
		}
		return result;
	}


	/** {@inheritDoc} */
	@Override
	public int[] batchInsert(DataSource dataSource, String tableName, List<Map<String, Object>> namedValuesList) {

		if (tableName == null || "".equals(tableName))
			throw new PoRuntimeException("No table name defined for batch insert.");
		
		if (namedValuesList == null || namedValuesList.isEmpty())
			throw new PoRuntimeException("No values provided for batch insert.");
		
		List<String> names = new ArrayList<String>(namedValuesList.get(0).keySet());
		
		String query = getInsertString(tableName, names);
		
		return batchExecute(dataSource, query, names, namedValuesList);
	}
	
	/** {@inheritDoc} */
	@Override
	public int[] batchUpdate(DataSource dataSource, String tableName, String primaryKeyName, List<Map<String, Object>> namedValuesList) {
		
		if (tableName == null || "".equals(tableName))
			throw new PoRuntimeException("No table defined for batch update.");
		
		if (primaryKeyName == null || "".equals(primaryKeyName))
			throw new PoRuntimeException("No primary key name defined for batch update.");
		
		if (namedValuesList == null || namedValuesList.isEmpty())
			throw new PoRuntimeException("No values provided for batch update.");
		
		List<String> names = new ArrayList<String>(namedValuesList.get(0).keySet());
		// move primary key name to the end
		names.remove(primaryKeyName);
		names.add(primaryKeyName);
		
		if (names.size()==1) {
			// nothing to do, so return an array of integers with 0 
			// meaning that the complete batch affected 0 records.
			int[] ret = new int[namedValuesList.size()];
			for (int i=0; i<ret.length; i++) {
				ret[i] = 0;
			}
			return ret;
		}
		
		String query = getUpdateString(tableName, names);
		
		return batchExecute(dataSource, query, names, namedValuesList);
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsRecord(DataSource dataSource, String tableName, String primaryKeyName, Object primaryKeyValue) {
		
		if (tableName == null || "".equals(tableName))
			throw new PoRuntimeException("No table name defined for record presence check");
		
		if (primaryKeyName == null || "".equals(primaryKeyName))
			throw new PoRuntimeException("No primary key name provided for record presence check.");
		
		String sql = "select count(*) from " + tableName + " where " + primaryKeyName + " = ?";
		return getJdbcTemplate(dataSource).queryForInt(sql, new Object[] {primaryKeyValue}) > 0;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<String> getColumnNames(DataSource dataSource, String tableName) {
		assertTableName(tableName);
		
		return doGetColumnNames(dataSource, tableName);
	}

	private void assertTableName(String tableName) {
		if (tableName.toLowerCase().startsWith("select "))
			throw new IllegalArgumentException("Do not pass full SQLs, only tablenames to this method!");
	}
	@Override
	public List<String> getColumnNamesFromSQL(DataSource dataSource, String sql) {
		return doGetColumnNames(dataSource, sql);
	}
	
	private List<String> doGetColumnNames(DataSource dataSource, String tableNameOrSql) {
		
		try {
			String sql =  tableNameOrSql.startsWith("select")?tableNameOrSql : "select * from " + tableNameOrSql ;
			JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
			jdbcTemplate.setMaxRows(1);
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
			if (rowSet == null) 
				return Collections.emptyList();
			SqlRowSetMetaData metadata = rowSet.getMetaData();
			int columnCount = metadata.getColumnCount();
			List<String> columnNames = new ArrayList<String>();
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				String columnName = getColumnName(metadata, columnIndex);
				columnNames.add(columnName);
			}
			return columnNames;
			
		} catch (Exception e) {
			handleException(e, "Exception " + e + " while getting column names for table/sql " + tableNameOrSql);
		}
		return null; // should not ever happen but eclipse requires it
	}
	
	@Override
	public Map<String, String> getColumnClassNames(DataSource dataSource, String tableName) {
		assertTableName(tableName);
		return doGetColumnClassNames(dataSource, tableName);
	}
	
	@Override
	public Map<String, String> getColumnClassNamesFromSQL(DataSource dataSource, String sql) {
		return doGetColumnClassNames(dataSource, sql);
	}
	
	private Map<String, String> doGetColumnClassNames(DataSource dataSource, String tableNameOrSql) {
		
		try {
			String sql =  tableNameOrSql.startsWith("select")?tableNameOrSql : "select * from " + tableNameOrSql ;
			JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
			jdbcTemplate.setMaxRows(1);
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
			if (rowSet == null) 
				return Collections.emptyMap();
			SqlRowSetMetaData metadata = rowSet.getMetaData();
			Map<String, String> result = new HashMap<String, String>();
			for (int columnIdx = 1; columnIdx <= metadata.getColumnCount(); columnIdx++) {
				String columnName = getColumnName(metadata, columnIdx);
				String columnClassName = metadata.getColumnClassName(columnIdx);
				result.put(columnName, columnClassName);
			}
			return result;
			
		} catch (Exception e) {
			handleException(e, "Exception " + e + " while getting column names for table " + tableNameOrSql);
		}
		return null; // should not ever happen but eclipse requires it
	}

	private String getColumnName(SqlRowSetMetaData metadata, int columnIdx) {
		// try column alias first
		String columnName = metadata.getColumnLabel(columnIdx);
		if (StringUtils.isBlank(columnName))
			columnName = metadata.getColumnName(columnIdx);
		return columnName;
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<String, Boolean> getColumnMandatoriness(DataSource dataSource, String tableName) {
		
		Connection conn = null;
		Statement stmt = null;
		boolean canReuseHibernateConnection = canReUseConnectionFromCurrentHibernateSession(dataSource);
		try {
			
			if (canReuseHibernateConnection)
				conn = getConnectionFromCurrentHibernateSession();
			else
				conn = dataSource.getConnection();
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from " + tableName);
			rs.setFetchSize(1);
		    ResultSetMetaData metadata = rs.getMetaData();
			Map<String, Boolean> result = new HashMap<String, Boolean>();
			for (int columnIdx = 1; columnIdx <= metadata.getColumnCount(); columnIdx++) {
				String columnName = metadata.getColumnName(columnIdx);
				boolean isMandatory = 1 != metadata.isNullable(columnIdx);
				result.put(columnName, isMandatory);
			}
			return result;
			
		} catch (Exception e) {
			handleException(e, "Exception " + e + " while getting column names for table " + tableName);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) { }
			if (conn != null && canReuseHibernateConnection==false)
				try {
					conn.close();
				} catch (SQLException e) { }
		}
		
		return null; // should not ever happen but eclipse requires it
	}
	
	private Connection getConnectionFromCurrentHibernateSession() {
		SessionFactory sf = (SessionFactory) WebdeskApplicationContext.getBean("sessionFactory");
		return sf.getCurrentSession().connection();
	}

	@SuppressWarnings("deprecation")
	private boolean canReUseConnectionFromCurrentHibernateSession(DataSource dataSource) {
		PoDataSourceService dsService = (PoDataSourceService) WebdeskApplicationContext.getBean("PoDataSourceService");
		
		// check if we have the webdesk datasource! if not we cannot reuse the session factorys connection
		if (dsService.getDataSource( PoDataSourceService.WEBDESK).equals(dataSource)==false)
			return false;
		
		SessionFactory sf = (SessionFactory) WebdeskApplicationContext.getBean("sessionFactory");
		try {
			sf.getCurrentSession();
			sf.getCurrentSession().connection();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public int[] batchInsertOrUpdate(DataSource dataSource, String tableName, String primaryKeyName, List<Map<String, Object>> namedValuesList) {
		return batchInsertOrUpdate(dataSource, tableName, primaryKeyName, namedValuesList, null);
	}
	
	/**
	 * By passing a comparator, the processing will try to first load all existing values in the database of the current batch
	 * and collects its primary key values. By caching the list of existing primary key values and existing row contents the 
	 * decision whether a save will result in an insert or an update and also the decision whether an update operation should
	 * hit the database can be speed up significantly. 
	 */
	public int[] batchInsertOrUpdate(DataSource dataSource, String tableName, String primaryKeyName, List<Map<String, Object>> namedValuesList, Comparator<Map<String,Object>> comparator) {
		
		List<Map<String, Object>> recordsForInsert = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> recordsForUpdate = new ArrayList<Map<String,Object>>();
		
		boolean cachePrimaryKeysOfExistingData = true;
		
		// cache existing primary keys in order to avoid making selects
		// for every row to write in order to decide whether an insert or an update
		// has to be done
		List<Object> existingPrimaryKeys = new ArrayList<Object>();
		
		boolean checkIfUpdateNecessary = comparator!=null;
		
		Map<Object, Map<String,Object>> existingRows = new HashMap<Object, Map<String,Object>>();
		
		if (  checkIfUpdateNecessary ) {
			
			JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
			List<Object> allKeyValues = getListOfPrimaryKeys(primaryKeyName, namedValuesList);
			
			String selectClause = "select " + org.springframework.util.StringUtils.collectionToCommaDelimitedString( namedValuesList.get(0).keySet());
			
			/** 
			 * Be aware that depending on the DB vendor, there might be a limit of maximum string values within a IN statement. By choosing the right
			 * batch size, this can be controlled.
			 */
			String whereClause = " where " + primaryKeyName + " in (" + org.springframework.util.StringUtils.collectionToDelimitedString(allKeyValues, ",","'","'") + ")";
					
			@SuppressWarnings("unchecked")
			List<Map<String,Object>> rows = jdbcTemplate.queryForList(selectClause + " from " + tableName + whereClause );
			
			for (Map<String,Object> row : rows) {
				existingRows.put( row.get(primaryKeyName), row);
				if (cachePrimaryKeysOfExistingData) {
					existingPrimaryKeys.add( row.get(primaryKeyName) );
				}
			}
			
		} else {
			cachePrimaryKeysOfExistingData = false;
		}
		
		for(Map<String, Object> record : namedValuesList)
			if ( cachePrimaryKeysOfExistingData==true && existingPrimaryKeys.contains( record.get(primaryKeyName)) || 
					cachePrimaryKeysOfExistingData==false && containsRecord(dataSource, tableName, primaryKeyName, record.get(primaryKeyName))) {
				
				if ( checkIfUpdateNecessary ) {
					// here we compare the ex
					Map<String,Object> existingRow = existingRows.get( record.get(primaryKeyName));
					if (comparator.compare(record, existingRow)!=0)
						recordsForUpdate.add(record);
				} else {
					recordsForUpdate.add(record);
				}
			} else
				recordsForInsert.add(record);
		
		int inserts = 0;
		if (!recordsForInsert.isEmpty()) {
			int[] insertResult = batchInsert(dataSource, tableName, recordsForInsert);
			for (int insert : insertResult)
				inserts += insert; 
		}
		int updates = 0;
		if (!recordsForUpdate.isEmpty()) {
			int[] updateResult = batchUpdate(dataSource, tableName, primaryKeyName, recordsForUpdate);
			for (int update : updateResult)
				updates += update;
		}
		
		return new int[] {inserts, updates};
	}
	
	private List<Object> getListOfPrimaryKeys(String primaryKeyName, List<Map<String, Object>> namedValuesList) {
		
		List<Object> keys = new ArrayList<Object>();
		for (Map<String, Object> row : namedValuesList) {
			keys.add( row.get(primaryKeyName) );
		}
		return keys;
	}

	
	/** {@inheritDoc} */
	@Override
	public int[] batchDelete(DataSource dataSource, String tableName, String primaryKeyName, List<?> primaryKeyValues) {

		if (tableName == null || "".equals(tableName))
			throw new PoRuntimeException("No table name defined for delete.");
		
		if (primaryKeyName == null || "".equals(primaryKeyName))
			throw new PoRuntimeException("No primaryKeyName defined for delete.");
		
		if (primaryKeyValues == null || primaryKeyValues.isEmpty())
			throw new PoRuntimeException("No primary key values provided for delete.");
		
		String sql = "delete from " + tableName + " where " + primaryKeyName + " = ?";
		List<Object[]> keyValues = new ArrayList<Object[]>();
		for (Object keyValue : primaryKeyValues)
			keyValues.add(new Object[] {keyValue});
		
		return batchExecute(dataSource, sql, keyValues);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String,List<String>> getConstraintNames(DataSource dataSource, String tableName) {
		final PoDataSourceDefinition dataSourceDefinition = DataSourceUtil.getDefinitionFromDataSource(dataSource);
		
		try	{
			// the Microsoft JDBC driver blocks when retrieving meta data, so read by a proprietary query
			final String url = dataSourceDefinition.getUrl().toLowerCase();
			if (url.startsWith("jdbc:sqlserver:"))
				return getConstraintNamesFromMsSqlServer(dataSource, tableName);

			final boolean isMySql = url.startsWith("jdbc:mysql:");
			return getConstraintNamesFromJdbcMetaData(dataSource, tableName, isMySql);
		}
		catch (SQLException e)	{
			throw new PoRuntimeException("Could not read primary key constraint name for "+tableName, e);
		}
	}

	//	----------------------------- PRIVATE METHODS --------------------------------------

	private Map<String,List<String>> getConstraintNamesFromJdbcMetaData(DataSource dataSource, String tableName, boolean isMySql) throws SQLException	{
		final Map<String,List<String>> constraintsMap = new HashMap<String,List<String>>();
		
		final Connection connection = dataSource.getConnection();
		final DatabaseMetaData metaData = connection.getMetaData();
		final String catalog = null;
		final String schema = null;

		if (isMySql == false)	{
			// read primary key meta data for given tableName
			final ResultSet primaryKeys = readResultSetCaseInsensitive(catalog, schema, metaData, PRIMARY_KEY_CONSTRAINT_TAG, tableName);
			processResultSet(constraintsMap, primaryKeys, "PK_NAME", PRIMARY_KEY_CONSTRAINT_TAG, isMySql);
		}
		// In MySql all primary keys are named PRIMARY. JDBC MetaData deliver results not usable for a DROP CONSTRAINT statement.
		// But you do not need to know primary key constraint names on MySql: 'ALTER TABLE ... DROP PRIMARY KEY' is sufficient!

		// read foreign key meta data for given tableName (imported keys!)
		final ResultSet foreignKeys = readResultSetCaseInsensitive(catalog, schema, metaData, FOREIGN_KEY_CONSTRAINT_TAG, tableName);
		processResultSet(constraintsMap, foreignKeys, "FK_NAME", FOREIGN_KEY_CONSTRAINT_TAG, isMySql);

		// read index meta data for given tableName
		final ResultSet indexes = readResultSetCaseInsensitive(catalog, schema, metaData, INDEX_CONSTRAINT_TAG, tableName);
		processResultSet(constraintsMap, indexes, "INDEX_NAME", INDEX_CONSTRAINT_TAG, isMySql);

		return constraintsMap;
	}


	/**
	 * Returns the result-set for given kindOfConstraint on table.
	 * We could consult metaData.storesXxxIdentifiers() here, but it is safer to try all three variants of case-sensitiveness.
	 */
	private ResultSet readResultSetCaseInsensitive(String catalog, String schema, DatabaseMetaData metaData, String kindOfConstraint, String tableName) throws SQLException	{
		ResultSet resultSet = readResultSet(catalog, schema, metaData, kindOfConstraint, tableName);
				
		if (resultSet.next() == false)	{	// seems to be not case-sensitive
			resultSet = readResultSet(catalog, schema, metaData, kindOfConstraint, tableName.toUpperCase());
			
			if (resultSet.next() == false)	{	// seems to store in lower-case
				resultSet = readResultSet(catalog, schema, metaData, kindOfConstraint, tableName.toLowerCase());
				
				if (resultSet.next() == false)	{	// we have a real problem with this database
					return null;
				}
			}
		}
		return resultSet;
	}
	
	private ResultSet readResultSet(String catalog, String schema, DatabaseMetaData metaData, String kindOfConstraint, String tableName) throws SQLException	{
		return
				kindOfConstraint.equals(PRIMARY_KEY_CONSTRAINT_TAG) ? metaData.getPrimaryKeys(catalog, schema, tableName)
					: kindOfConstraint.equals(FOREIGN_KEY_CONSTRAINT_TAG) ? metaData.getImportedKeys(catalog, schema, tableName)
					: metaData.getIndexInfo(catalog, schema, tableName, false, true);
	}
	
	private void processResultSet(final Map<String, List<String>> returnData, ResultSet constraints, String constraintColumnName, String mapKey, boolean isMySql) throws SQLException {
		while (constraints != null)	{
			final String constraintName = constraints.getString(constraintColumnName);	// JDBC standard column name of the FK constraint
			
			if (constraints.wasNull() == false && constraintName != null && (isMySql == false || constraintName.equals("PRIMARY") == false))
				aggregate(returnData, mapKey, constraintName);
			
			if (constraints.next() == false)	{
				constraints.close();
				constraints = null;
			}
		}
	}
	
	
	/**
	 * @return a Map with key = one of "FK", "PK", "IDX", and value = name of that constraint.
	 * @throws PoRuntimeException when no constraints on primary key of given table could be read.
	 */
	private Map<String,List<String>> getConstraintNamesFromMsSqlServer(DataSource dataSource, String tableName) {
		final String query = "exec sp_help "+tableName;
		final Map<String,List<String>> constraints = new HashMap<String,List<String>>();

		final JdbcTemplate jdbc = getJdbcTemplate(dataSource);
		final CallableStatementCreator creator = new CallableStatementCreatorFactory(query).newCallableStatementCreator(new HashMap<String,Object>());
		
		final Map<String,Object> result = jdbc.call(creator, new ArrayList<SqlParameter>());
		
		for (Object resultSetsObj : result.values())	{
			
			@SuppressWarnings("unchecked")
			List<Map<String,Object>> resultSets = (List<Map<String, Object>>) resultSetsObj;
			
			for (Map<String,Object> resultSet : resultSets)	{
				final String constraintType = (String) resultSet.get("constraint_type");
				if (constraintType != null)	{
					if (constraintType.startsWith("FOREIGN KEY"))
						aggregate(constraints, FOREIGN_KEY_CONSTRAINT_TAG, (String) resultSet.get("constraint_name"));
					if (constraintType.startsWith("PRIMARY KEY"))
						aggregate(constraints, PRIMARY_KEY_CONSTRAINT_TAG, (String) resultSet.get("constraint_name"));
				}
			}
		}
		
		return constraints;
	}
		
	private void aggregate(Map<String, List<String>> map, String key, String valueToAggregate) {
		if (StringUtils.isEmpty(valueToAggregate))
			return;	// nothing to do
		
		List<String> value = map.get(key);
		if (value == null)
			map.put(key, value = new ArrayList<String>());
		
		value.add(valueToAggregate);
	}

	private void checkUpdateStatement(final String sql) {
		if (sql == null || 
				!sql.trim().toLowerCase().startsWith("insert") &&
				!sql.trim().toLowerCase().startsWith("update") &&
				!sql.trim().toLowerCase().startsWith("delete")) {
			logger.error("Not an update statement: " + sql);
			throw new PoRuntimeException("Not an update statement: " + sql);
		}
	}

	private void loggerDebug(final String message, PaginableQuery myQuery) {
		if (logger.isDebugEnabled()) {
			logger.debug(message + myQuery.getQueryText());
			if (myQuery instanceof PositionalQuery)
				logger.debug("Parameters: " + Arrays.asList(((PositionalQuery) myQuery).getParamValues()));
		}
	}
	
	private void loggerDebug(final String message, String sql) {
		if (logger.isDebugEnabled()) {
			logger.debug(message + sql);
		}
	}
	
	private void handleException(PaginableQuery myQuery, Exception e, final String message) {
		logger.error(message + myQuery.getQueryText());
		if (myQuery instanceof PositionalQuery)
			logger.error("Parameters: " + Arrays.asList(((PositionalQuery) myQuery).getParamValues()));
		throw new PoRuntimeException(message, e);
	}
	
	private void handleException(Exception e, final String message) {
		logger.error(message);
		throw new PoRuntimeException(message, e);
	}
	
	/**
	 * @return fresh, not pooled jdbcTemplate with given dataSource.
	 */
	private JdbcTemplate getJdbcTemplate(DataSource dataSource) {

		if (dataSource == null)
			return null;
		return new JdbcTemplate(dataSource);
	}

	/**
	 * @return pooled jdbcTemplate with given dataSource.
	 * If there is no pooled one a new one is created and stored in pool. 
	 */
/*	private JdbcTemplate getJdbcTemplate(DataSource dataSource) {
		
		JdbcTemplate jdbcTemplate;
		
		if (dataSource == null)
			return null;
		else if (jdbcTemplateMap.containsKey(dataSource))
			jdbcTemplate = jdbcTemplateMap.get(dataSource);
		else { // temporary dataSource, e.g. for dbConnector
			jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplateMap.put(dataSource, jdbcTemplate);
		}
		jdbcTemplate.setMaxRows(0); // restore to default
		return jdbcTemplate;
	}
*/	
	private PositionalQuery getInsertQuery(String tableName, Map<String, Object> namedValues) {

		List<String> names = new ArrayList<String>(namedValues.keySet());
		List<Object> values = new ArrayList<Object>();
		for (String name : names) {
			values.add(namedValues.get(name));
		}

		PositionalQuery query = new PositionalQuery(getInsertString(tableName, names), values.toArray());
		return query;
	}

	private String getInsertString(String tableName, List<String> names) {
		
		String namesString = "";
		String questionMarks = "";
		for (String name : names) {
			namesString += name + ", ";
			questionMarks += "?, ";
		}
		namesString = namesString.substring(0, namesString.length() - 2);
		questionMarks = questionMarks.substring(0, questionMarks.length() - 2);
		
		return "insert into " + tableName + " (" + namesString + ") values (" + questionMarks + ")";
	}

	private PositionalQuery getUpdateQuery(String tableName, String primaryKeyName, Map<String, Object> namedValues) {
		
		List<String> names = new ArrayList<String>(namedValues.keySet());
		// move primary key name to the end
		names.remove(primaryKeyName);
		names.add(primaryKeyName);
		
		// if the names list has only one entry, it MUST be the primary
		// key. in that case no update is useful at all...
		if (names.size()==1)
			return null;
		
		List<Object> values = new ArrayList<Object>();
		// non-primary keys
		for (String name : names) {
			values.add(namedValues.get(name));
		}
		
		PositionalQuery query = new PositionalQuery(getUpdateString(tableName, names), values.toArray());
		return query;
	}
	
	private String getUpdateString(String tableName, List<String> names) {
		
		final int namesCount = names.size();
		String sql = "update " + tableName + " set ";
		for (int idx = 0; idx < namesCount; idx++) {
			if (idx < namesCount - 1) // columns != primary key column
				sql += names.get(idx) + (idx < namesCount - 2 ? " = ?, " : " = ? ");
			else 
				sql += " where " + names.get(idx) + " = ?";
		}
		return sql;
	}
}