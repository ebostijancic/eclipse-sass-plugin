package at.workflow.webdesk.po.update;

import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoRuntimeException;

/**
 * Lets redefine a primary key column that is referencing (or might be referenced by) other tables.
 * Provides means to drop and create foreign key constraints database-independently.
 * 
 * @author fritzberger 05.12.2012
 */
public abstract class AbstractRenamePrimaryKey extends AbstractRenameNonReferencedPrimaryKey {

	/** Drop foreign key constraints here, using dropForeignKeyConstraint(). */
	@Override
	protected abstract void beforeDropPrimaryKeyConstraint(Map<String,List<String>> constraints);

	/** Create foreign key constraints here, using createForeignKeyConstraint(). */
	@Override
	protected abstract void afterDropOldColumn();

	/**
	 * Drops the given foreign key constraint on given table database-independently.
	 * None of the parameters must be null.
	 * @param tableName the database table name the foreign key is declared on.
	 * @param foreignKeyConstraintName the name of the foreign key constraint to drop.
	 */
	protected final void dropForeignKeyConstraint(String tableName, String foreignKeyConstraintName)	{
		final String statement = isMySql()
			? "ALTER TABLE "+tableName+" DROP FOREIGN KEY "+foreignKeyConstraintName
			: "ALTER TABLE "+tableName+" DROP CONSTRAINT "+foreignKeyConstraintName;
		
		try	{
			sqlServiceExecute(statement);
		}
		catch (Exception e)	{	// do not take this serious, MySQL has some charset/collation bug that prevents this from succeeding
			Throwable th = (e instanceof PoRuntimeException) ? e.getCause() : e;
			logger.warn("Drop foreign key constraint failed: "+statement+", error was: "+th);
		}
	}

	/**
	 * Creates the given foreign key constraint on given table database-independently.
	 * None of the parameters must be null.
	 * @param tableName the database table name the foreign key should be declared on.
	 * @param foreignKeyConstraintName the name of the foreign key constraint to declare.
	 * @param foreignKey the name(s) of the foreign key column(s).
	 * @param referencedTable the name of the table that is referenced by the foreign key constraint.
	 * @param referencedTablePrimaryKey the name(s) of the primary key column(s) of the table that is referenced by the foreign key constraint.
	 */
	protected final void createForeignKeyConstraint(String tableName, String foreignKeyConstraintName, String foreignKey, String referencedTable, String referencedTablePrimaryKey)	{
	    final String statement =
	    	"ALTER TABLE "+tableName+" ADD CONSTRAINT "+foreignKeyConstraintName+" FOREIGN KEY ("+foreignKey+") REFERENCES "+referencedTable+"("+referencedTablePrimaryKey+")";
	    
		try	{
			sqlServiceExecute(statement);
		}
		catch (Exception e)	{	// do not take this serious, MySQL has some charset/collation bug that prevents this from succeeding
			Throwable th = (e instanceof PoRuntimeException) ? e.getCause() : e;
			logger.warn("Create foreign key constraint failed: "+statement+", error was: "+th);
		}
	}

}
