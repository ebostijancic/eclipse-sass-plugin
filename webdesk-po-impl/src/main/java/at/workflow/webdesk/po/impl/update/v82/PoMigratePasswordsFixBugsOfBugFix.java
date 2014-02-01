package at.workflow.webdesk.po.impl.update.v82;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import at.workflow.webdesk.po.PoBeanPropertyService;
import at.workflow.webdesk.po.impl.update.v81.PoMigratePasswordsFixBugs;
import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.po.model.PoBeanPropertyValue;

/**
 * Update script that fixes bugs the old version of v80 script committed.
 * That script has been fixed meanwhile, so there are no bugs to fix when
 * the v80 script has been run in this startup.
 * 
 * @author fritzberger 03.11.2010
 */
public class PoMigratePasswordsFixBugsOfBugFix extends PoMigratePasswordsFixBugs {

	private static final Logger log = Logger.getLogger(PoMigratePasswordsFixBugsOfBugFix.class);
	
	/**
	 * Removes the redundant entry in some PoPasswordXxx beans (UI "Einstellungen" - "Systemparameter").
	 * This rose from a misunderstanding of the PoBeanPropertyService interface.
	 */
	@Override
	public void execute() {
		if (PoMigratePasswordsFixBugs.wasExecuted())	{	// PO-module versionNumber 80->81
			return;	// do nothing, that script was fixed meanwhile
		}
		
		setSpringBeanPropertyDefaults();
	}
	
	/** Overridden to reduce the count of bean values to one. Calls super after that. */
	@Override
	protected void setSpringBeanProperty(String beanName, String propertyName, String value)	{
		PoBeanPropertyService beanService = (PoBeanPropertyService) getBean("PoBeanPropertyService");
		PoBeanProperty beanProp = beanService.findBeanPropertyByKey(beanName, propertyName);
		if (beanProp != null)	{
			Collection<PoBeanPropertyValue> values = beanProp.getEntries();
			final int size = values.size();
			if (size > 1)	{
				log.info("Reducing size of "+beanName+"."+propertyName+", was "+size);
				// delete all except one value
				int i = 0;
				List<PoBeanPropertyValue> detached = new ArrayList<PoBeanPropertyValue>(values);	// else ConcurrentModificicationException
				for (PoBeanPropertyValue v : detached)	{
					if (i > 0)
						beanService.deleteBeanValue(beanProp, v.getUID());
					i++;
				}
			}
		}
		super.setSpringBeanProperty(beanName, propertyName, value);
	}

}
