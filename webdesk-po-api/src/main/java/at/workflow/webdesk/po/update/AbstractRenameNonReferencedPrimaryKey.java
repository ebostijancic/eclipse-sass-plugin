package at.workflow.webdesk.po.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.po.PoGeneralSqlService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadata;

/**
 * Lets redefine a primary key (which MUST NOT be referenced by any foreign key constraint)
 * database-independently.
 * <p/>
 * Copies all primary key values to the new primary key column, which must have been created
 * by Hibernate boot. Then sets that new column to not-null (required to be a primary key).
 * Drops the primary key constraint and creates a new one for the new column.
 * Then drops the old primary key column.
 * <p/>
 * DO NOT USE this with normal columns that are no primary keys!<br/>
 * DO NOT USE this with primary keys that are referenced by foreign key constraints!
 * 		(Use AbstractRenamePrimaryKey for such.)
 * 
 * @author fritzberger 29.11.2012
 */
public abstract class AbstractRenameNonReferencedPrimaryKey extends PoAbstractUpgradeScript {

	private DataSource database;
	private PoGeneralSqlService sqlService;
	private PersistenceMetadata persistenceMetadata;
	
	/**
	 * Executes the DDL statements to rename a primary key attribute.
	 * @param tableClass the OQL table class used by JPA-layer, the database table name is the simple class name.
	 * @param oldColumnName the database attribute name of the old primary key to rename.
	 * @param newColumnName the database attribute name of the new primary key to rename, already created in Hibernate boot, but empty.
	 */
	protected void renamePrimaryKey(Class<? extends PersistentObject> tableClass, String oldColumnName, String newColumnName) {
		renamePrimaryKey(tableClass.getSimpleName(), oldColumnName, newColumnName);
	}
	
	/**
	 * Executes the DDL statements to rename a primary key attribute.
	 * @param tableClass the OQL table class used by JPA-layer, the database table name is the simple class name.
	 * @param tableClass the OQL table class used by JPA-layer, the database table name is the simple class name.
	 * @param oldColumnName the database attribute name of the old primary key to rename.
	 * @param newColumnName the database attribute name of the new primary key, already created during Hibernate boot, but having no values yet.
	 */
	protected void renamePrimaryKey(String tableName, String oldColumnName, String newColumnName) {
		if (tableName == null)
			throw new IllegalArgumentException("Need a table name for renaming primary key: "+tableName);
		
		if (oldColumnName == null)
			oldColumnName = "UID";
		
		init();	// retrieve services from application-context
		
		if (existsColumnInTable(tableName, oldColumnName) == false)	// check if this database has been already changed
			return;	// nothing to rename, this can happen when a script runs several times
		
		if (existsColumnInTable(tableName, newColumnName) == false)
			throw new IllegalStateException("The new column in "+tableName+" has not been created by Hibernate boot: "+newColumnName);
		
		sqlServiceExecute("UPDATE "+tableName+" SET "+newColumnName+" = "+oldColumnName);
		
		final Map<String,List<String>> constraints = sqlService().getConstraintNames(database, tableName);
		
		beforeDropPrimaryKeyConstraint(constraints);
		
		if (isMySql())	{	// MySQL needs special DDL statements
			mySqlRename(tableName, newColumnName);
		}
		else	{	// Microsoft SQL server and others (currently Webdesk runs on no others, 2012-11-30)
			msSqlServerRename(tableName, newColumnName, constraints);
		}
		
		beforeDropOldColumn(tableName, oldColumnName, constraints);
		
		try	{
			sqlServiceExecute("ALTER TABLE "+tableName+" DROP COLUMN "+oldColumnName);
		}
		catch (Exception e)	{
			logger.warn("Failed to drop old primary key column "+tableName+"."+oldColumnName+", message was: "+e.getMessage(), e);
		}
		
		afterDropOldColumn();
	}

	/** Called before the primary key constraint is dropped. Does nothing. Drop foreign key constraints on that table here! */
	protected void beforeDropPrimaryKeyConstraint(Map<String,List<String>> constraints)	{
	}

	/** Called before the old primary key column is dropped. Does nothing. */
	protected void beforeDropOldColumn(String tableName, String oldColumnName, Map<String,List<String>> constraints)	{
	}
	
	/**
	 * Drops given index database-specific.
	 * @param tableName the name of the database table where the index is defined on.
	 * @param columnName the name of the column the index is defined on.
	 * @param indexName the name of the index to delete.
	 */
	protected final void dropIndex(String tableName, String columnName, String indexName) {
		// fri_2012-12-07: for now run in a try/catch, because I do not know what MySQL will do on this
		final String statement = isMySql()
				? "ALTER TABLE "+tableName+" DROP INDEX "+indexName
				: "DROP INDEX "+tableName+"."+indexName;
		
		try	{
			sqlServiceExecute(statement);
		}
		catch (Exception e)	{
			Throwable th = (e instanceof PoRuntimeException) ? e.getCause() : e;
			logger.warn("Drop index failed: "+statement+", error was: "+th);
		}
	}

	/** Called after the old primary key column has been dropped. Does nothing. Re-create foreign key constraints on that table here! */
	protected void afterDropOldColumn()	{
	}

	/**
	 * This is called when the new PRIMARY KEY statement is launched.
	 * Can be overridden when primary key consists of more than one column.
	 * This default implementation returns given column to be the only one
	 * in column clause of "ADD PRIMARY KEY" statement.
	 * @return the text to put in place of "_" in statement "... PRIMARY KEY (_)".
	 */
	protected String primaryKeyColumnList(String newColumnName)	{
		return newColumnName;
	}
	
	
	protected final void sqlServiceExecute(String statement)	{
		logger.info("Executing upgrade command: "+statement);
		sqlService().execute(database, statement);
	}

	protected final PoGeneralSqlService sqlService()	{
		return sqlService;
	}


	private void init() {
		database = (DataSource) getBean("webdesk-DataSource");
		sqlService = (PoGeneralSqlService) getBean("PoGeneralSqlService");
		persistenceMetadata = (PersistenceMetadata) getBean("PersistenceMetadata");
		
		if (database == null || sqlService == null || persistenceMetadata == null)
			throw new IllegalStateException("Either database or SQL-service or PersistenceMetadata do not exist!");
	}
	
	private boolean existsColumnInTable(String tableName, String oldColumnName) {
		final List<String> upperCaseColumns = new ArrayList<String>();
		for (String column : sqlService().getColumnNames(database, tableName))
			upperCaseColumns.add(column.toUpperCase());
		return upperCaseColumns.contains(oldColumnName.toUpperCase());
	}

	private void mySqlRename(String tableName, String newColumnName) {
		sqlServiceExecute("ALTER TABLE "+tableName+" DROP PRIMARY KEY");
		sqlServiceExecute("ALTER TABLE "+tableName+" ADD PRIMARY KEY ("+primaryKeyColumnList(newColumnName)+")");
	}

	private void msSqlServerRename(String tableName, String newColumnName, Map<String,List<String>> constraints) {
		final List<String> primaryKeyConstraints = constraints.get(PoGeneralSqlService.PRIMARY_KEY_CONSTRAINT_TAG);
		String primaryKeyConstraint = (primaryKeyConstraints != null && primaryKeyConstraints.size() > 0) ? primaryKeyConstraints.get(0) : null;
		
		if (StringUtils.isEmpty(primaryKeyConstraint) == false)
			sqlServiceExecute("ALTER TABLE "+tableName+" DROP CONSTRAINT "+primaryKeyConstraint);
		
		try	{	// must set new PK column to NOT NULL for some databases
			sqlServiceExecute("ALTER TABLE "+tableName+" ALTER COLUMN "+newColumnName+" VARCHAR(32) NOT NULL");
		}
		catch (Exception e)	{
			logger.warn("Failed to set new primary key column to NOT NULL: "+e.getMessage(), e);
		}
		
		if (primaryKeyConstraint == null)	{
			// case there was no primary key constraint before we have to create a name for it
			boolean isPtm = tableName.toLowerCase().startsWith("ptm");
			primaryKeyConstraint = "PK"+tableName.substring(isPtm ? 3 : 2);	// cut off 2 chars module-prefix, hope result is not longer than 32
		}
		
		try	{	// add new PK constraint
			sqlServiceExecute("ALTER TABLE "+tableName+" ADD CONSTRAINT "+primaryKeyConstraint+" PRIMARY KEY ("+primaryKeyColumnList(newColumnName)+")");
		}
		catch (Exception e)	{
			logger.warn("Failed to add primary key constraint to "+tableName+"."+newColumnName+", message was: "+e.getMessage(), e);
		}
	}

}
