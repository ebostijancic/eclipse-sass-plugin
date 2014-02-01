package at.workflow.webdesk.po.impl.update.v81;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import at.workflow.webdesk.po.PoBeanPropertyService;
import at.workflow.webdesk.po.PoPasswordService;
import at.workflow.webdesk.po.daos.PoPasswordDAO;
import at.workflow.webdesk.po.impl.update.v80.PoMigratePasswords;
import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.po.model.PoPassword;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * Update script that fixes bugs the old version of v80 script committed.
 * That script has been fixed meanwhile, so there are no bugs to fix when
 * the v80 script has been run in this startup.
 * 
 * @author fritzberger 03.11.2010
 */
public class PoMigratePasswordsFixBugs extends PoAbstractUpgradeScript {

	private static boolean wasExecuted = false;
	
	/** @return true when this script has been run in this startup in this ClassLoader's object hierarchy. */
	public static boolean wasExecuted()	{
		return PoMigratePasswordsFixBugs.wasExecuted;
	}
	
	/**
	 * Resets all passwords that were never historicized (i.e. changed manually).
	 */
	@Override
	public void execute() {
		if (PoMigratePasswords.wasExecuted())	{	// PO-module versionNumber 79->80
			return;	// do nothing, as that script was fixed meanwhile and should do everything necessary
		}
		
		logger.info("PoMigratePasswordsFixBugs running, migrating PO 80->81 and fixing password migration - this should run only when fixed 79->81 script did not run.");
		
		// improve defaults of early installations
		setSpringBeanPropertyDefaults();
		
		PoPasswordDAO passwordDao = (PoPasswordDAO) getBean("PoPasswordDAO");
		List<PoPassword> passwords = passwordDao.loadAll();
		
		// do not correct passwords that were corrected manually (these are historicized)
		Map<String,Boolean> wasHistoricizedMap = buildWasHistoricizedMap(passwords);
		
		int i = 0;
		for (PoPassword password : passwords)	{
			if (wasHistoricizedMap.get(wasHistoricizedKey(password)) == Boolean.FALSE)	{
				PoPasswordService passwordService = (PoPasswordService) getBean("PoPasswordService");
				PoPerson person = password.getPerson();
				passwordService.resetPassword(person);
				logger.info("Password was reset for: "+person.getFullName());
				i++;
			}
		}
		
		wasExecuted = true;
		
		logger.info("Having reset passwords of "+i+" persons.");
	}
	
	
	protected final void setSpringBeanPropertyDefaults()	{
		// improve defaults of early installations
		setSpringBeanProperty("PoPasswordQuality", "minimalLength", "0");
		setSpringBeanProperty("PoPasswordQuality", "minimalDigitsCount", "0");
		setSpringBeanProperty("PoPasswordQuality", "numberOfDifferingExpiredPasswords", "0");
		setSpringBeanProperty("PoPasswordResetPolicy", "standardResetPassword", "webdesk");
		setSpringBeanProperty("PoPasswordResetPolicy", "useUsernameAsStandardPassword", "true");
	}
	
	protected void setSpringBeanProperty(String beanName, String propertyName, String value)	{
		PoBeanPropertyService beanService = (PoBeanPropertyService) getBean("PoBeanPropertyService");
		PoBeanProperty beanProp = beanService.findBeanPropertyByKey(beanName, propertyName);
		if (beanProp != null)	{
			try	{
				beanService.updateBeanValueAndInject(beanProp, value);
			}
			catch (Exception e)	{
				e.printStackTrace();
			}
		}
	}

	
	
	private Map<String,Boolean> buildWasHistoricizedMap(List<PoPassword> passwords)	{
		Map<String,Boolean> wasHistoricizedMap = new Hashtable<String,Boolean>(passwords.size());
		for (PoPassword password : passwords)	{
			String key = wasHistoricizedKey(password);
			if (wasHistoricizedMap.containsKey(key))	// if it exists more than once it was historicized
				wasHistoricizedMap.put(key, Boolean.TRUE);
			else	// as long as there is only one occurrence, assume that this is a double-encrypted password
				wasHistoricizedMap.put(key, Boolean.FALSE);
		}
		// the result map contains FALSE for all passwords that are 1:1 to a person
		// and TRUE for all passwords that are n:1 to a person.
		return wasHistoricizedMap;
	}
	
	private String wasHistoricizedKey(PoPassword pw)	{
		return pw.getPerson().getUID();		
	}
	
}
