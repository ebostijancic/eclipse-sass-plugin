package at.workflow.webdesk.po.impl.update.v100;

import at.workflow.webdesk.po.model.PoBeanPropertyValue;
import at.workflow.webdesk.po.update.AbstractRenameNonReferencedPrimaryKey;

/**
 * Renames the GwInsertedEntry UID primary key column to INSERTEDENTRY_UID.
 * 
 * @author fritzberger 29.11.2012
 */
public class RenamePrimaryKeyOfPoBeanPropertyValue extends AbstractRenameNonReferencedPrimaryKey {

	@Override
	public void execute() {
		renamePrimaryKey(PoBeanPropertyValue.class, "UID", "BEANVALUE_UID");
	}
	
}
