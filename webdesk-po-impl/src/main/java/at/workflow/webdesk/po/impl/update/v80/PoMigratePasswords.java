package at.workflow.webdesk.po.impl.update.v80;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import at.workflow.webdesk.po.PoPasswordService;
import at.workflow.webdesk.po.daos.PoPersonDAO;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;
import at.workflow.webdesk.tools.DaoJdbcUtil;

/**
 * Update script that inserts all persons' old passwords into new PoPassword table.
 * Mind that this accesses PoPerson through JDBC, because PoPerson.password is no
 * longer mapped by Java.
 * 
 * @author fritzberger 27.10.2010
 */
public class PoMigratePasswords extends PoAbstractUpgradeScript {

	private static boolean wasExecuted = false;
	
	/**
	 * Indicates if this script has been run in this startup.
	 * This should work across several webapps in same JVM when the servlet container
	 * uses a separate ClassLoader for each webapp (then the statics are <b>not</b> shared).
	 * @return true when this script has been run in this startup in this ClassLoader's object hierarchy.
	 */
	public static boolean wasExecuted()	{
		return PoMigratePasswords.wasExecuted;
	}
	
	@Override
	public void execute() {
		logger.info("PoMigratePasswords running, migrating PO 79->81, this is the fixed version. The 80->81 script should not run when this runs!");

		DaoJdbcUtil jdbc = (DaoJdbcUtil) getBean("DaoJdbcUtil");
		List<Map<String,Object>> passwords = jdbc.queryForList("select PERSON_UID, password from PoPerson", DaoJdbcUtil.DATASOURCE_WEBDESK);

		PoPasswordService passwordService = (PoPasswordService) getBean("PoPasswordService");
		PoPersonDAO personDao = (PoPersonDAO) getBean("PoPersonDAO");
		
		int i = 0, j = 0;
		for (Map<String,Object> record : passwords)	{
			String uid = (String) record.get("PERSON_UID");
			if (uid == null)	// try to be JDBC-safe against case-sensitive databases
				uid = (String) record.get("person_uid");
			
			String password = (String) record.get("password");
			if (password == null)
				password = (String) record.get("PASSWORD");
			
			if (uid == null)
				throw new IllegalArgumentException("Could not read UID from database record "+record);
			
			final PoPerson person = personDao.get(uid);
			
			if (password != null && password.length() > 0)	{
				logger.info("Migrating password for "+person.getFullName()+", username="+person.getUserName());
				passwordService.setNewPassword(person, null, password);
				i++;
			}
			else	{
				logger.info("Resetting password for "+person.getFullName()+", username="+person.getUserName());
				passwordService.resetPassword(person);
				j++;
			}
		}
		
		PoMigratePasswords.wasExecuted = true;
		
		logger.info("Having migrated passwords of "+i+" persons, reset passwords of "+j+".");
	}

}
