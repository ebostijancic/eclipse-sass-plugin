package at.workflow.webdesk.po.impl.test.mocks;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import at.workflow.webdesk.po.PoGeneralSqlService;
import at.workflow.webdesk.tools.PaginableQuery;

public class PoGeneralSqlMockService implements PoGeneralSqlService {

	/** {@inheritDoc} */
	@Override
	public List<Map<String, ?>> select(DataSource dataSource, PaginableQuery query) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public List<Map<String, Object>> selectRecords(DataSource dataSource, PaginableQuery query) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(DataSource dataSource, String sqlStatement) {
	}

	/** {@inheritDoc} */
	@Override
	public int execute(DataSource dataSource, PaginableQuery statementWithParameters) {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public int insert(DataSource dataSource, String tableName, Map<String, Object> namedValues) {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public int update(DataSource dataSource, String tableName, String primaryKeyName, Map<String, Object> namedValues) {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public int insertOrUpdate(DataSource dataSource, String tableName, String primaryKeyName, Map<String, Object> namedValues) {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public int delete(DataSource dataSource, String tableName, String primaryKeyName, Object primaryKeyValue) {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public int[] batchExecute(DataSource dataSource, String[] sqls) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public int[] batchExecute(DataSource dataSource, String sql, List<Object[]> parameterValues) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public int[] batchExecute(DataSource dataSource, String sql, List<String> parameterNamesInCorrectOrder, List<Map<String, Object>> namedValues) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public int[] batchInsert(DataSource dataSource, String tableName, List<Map<String, Object>> namedValuesList) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public int[] batchUpdate(DataSource dataSource, String tableName, String primaryKeyName, List<Map<String, Object>> namedValuesList) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public int[] batchInsertOrUpdate(DataSource dataSource, String tableName, String primaryKeyName, List<Map<String, Object>> namedValuesList) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public int[] batchDelete(DataSource dataSource, String tableName, String primaryKeyName, List<?> primaryKeyValues) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void removeFromCache(DataSource dataSource) {
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsRecord(DataSource dataSource, String tableName, String primaryKeyName, Object primaryKeyValue) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getColumnNames(DataSource dataSource, String tableName) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getColumnClassNames(DataSource dataSource, String tableName) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getColumnNamesFromSQL(DataSource dataSource, String sql) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getColumnClassNamesFromSQL(DataSource dataSource, String sql) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Boolean> getColumnMandatoriness(DataSource dataSource, String tableName) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, List<String>> getConstraintNames(DataSource dataSource, String tableName) {
		return null;
	}

	@Override
	public int[] batchInsertOrUpdate(DataSource dataSource, String tableName,
			String primaryKeyName, List<Map<String, Object>> namedValuesList,
			Comparator<Map<String, Object>> comparator) {
		// TODO Auto-generated method stub
		return null;
	}

}
