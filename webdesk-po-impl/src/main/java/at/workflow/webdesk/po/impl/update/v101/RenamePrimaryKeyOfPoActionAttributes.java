package at.workflow.webdesk.po.impl.update.v101;

import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoGeneralSqlService;
import at.workflow.webdesk.po.update.AbstractRenamePrimaryKey;

/**
 * Renames the PoActionAttributes UID column (part of the primary key) to ACTION_UID,
 * as it actually is a reference to PoAction.
 * 
 * @author fritzberger 03.12.2012
 */
public class RenamePrimaryKeyOfPoActionAttributes extends AbstractRenamePrimaryKey {

	private static final String OLD_COLUMN_NAME = "UID";
	private static final String NEW_COLUMN_NAME = "ACTION_UID";
	private static final String TABLE_NAME = "PoActionAttributes";
	private static final String FOREIGN_KEY_CONSTRAINT_NAME = "FK_ACTIONATTRIBUTES_ACTION";

	@Override
	public void execute() {
		renamePrimaryKey(TABLE_NAME, OLD_COLUMN_NAME, NEW_COLUMN_NAME);
	}
	
	@Override
	protected String primaryKeyColumnList(String newColumnName)	{
		return newColumnName+", NAME";	// ... PRIMARY KEY (ACTION_UID, NAME)
	}
	
	@Override
	protected void beforeDropPrimaryKeyConstraint(Map<String,List<String>> constraints) {
		// PoActionAttributes has a FK constraint on column UID, we must remove that before dropping PK constraint
//		if (isMySql())	{
			// for some reason MySQL refuses to drop FK_ACTIONATTRIBUTES_ACTION, which makes drop PK constraint fail
			// as a workaround we drop all foreign keys and indexes on that table
			
			if (constraints.get(PoGeneralSqlService.INDEX_CONSTRAINT_TAG) != null)
				for (String index : constraints.get(PoGeneralSqlService.INDEX_CONSTRAINT_TAG))	{
					dropIndex(TABLE_NAME, OLD_COLUMN_NAME, index);
					dropForeignKeyConstraint(TABLE_NAME, index);
				}
			
			// MS SQL Server needs dropping of indexes and FK-constraints before dropping PK-constraint
			if (isMySql() == false)	{
				if (constraints.get(PoGeneralSqlService.FOREIGN_KEY_CONSTRAINT_TAG) != null)
					for (String foreignKeyConstraint : constraints.get(PoGeneralSqlService.FOREIGN_KEY_CONSTRAINT_TAG))	{
						dropIndex(TABLE_NAME, OLD_COLUMN_NAME, foreignKeyConstraint);
						dropForeignKeyConstraint(TABLE_NAME, foreignKeyConstraint);
					}
			}
//		}
//		else	{	// MS-SQL Server
//			dropForeignKeyConstraint(TABLE_NAME, FOREIGN_KEY_CONSTRAINT_NAME);
//		}
	}

	/**
	 * Drop remaining indexes on the old column here.
	 */
	@Override
	protected void beforeDropOldColumn(String tableName, String oldColumnName, Map<String, List<String>> constraints) {
		super.beforeDropOldColumn(tableName, oldColumnName, constraints);
		
		if (constraints.get(PoGeneralSqlService.INDEX_CONSTRAINT_TAG) != null)
			// not needed for SQL Server, but MySql does not drop automatically the index related to any foreign-key
			for (String indexName : constraints.get(PoGeneralSqlService.INDEX_CONSTRAINT_TAG))
				dropIndex(tableName, oldColumnName, indexName);
		
//		if (isMySql() == false)	{
//			if (constraints.get(PoGeneralSqlService.FOREIGN_KEY_CONSTRAINT_TAG) != null)
//				for (String foreignKeyConstraint : constraints.get(PoGeneralSqlService.FOREIGN_KEY_CONSTRAINT_TAG))
//					;//dropForeignKeyConstraint(TABLE_NAME, foreignKeyConstraint);
//		}
	}
	
	@Override
	protected void afterDropOldColumn() {
		createForeignKeyConstraint(TABLE_NAME, FOREIGN_KEY_CONSTRAINT_NAME, NEW_COLUMN_NAME, "PoAction", "ACTION_UID");
	}

}
