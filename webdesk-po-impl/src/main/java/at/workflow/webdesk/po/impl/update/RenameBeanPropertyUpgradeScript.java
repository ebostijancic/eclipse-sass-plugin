package at.workflow.webdesk.po.impl.update;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.po.PoBeanPropertyService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.po.update.RenameUpgradeScript;

public final class RenameBeanPropertyUpgradeScript extends RenameUpgradeScript{

	private String oldBeanName;
	private String newBeanName;
	private String oldPropertyName;
	private String newPropertyName;

	@Override
	protected void rename(String oldName, String newName, String targetModule)	{
		parse(oldName, newName);
		
		PoBeanPropertyService beanPropertyService = (PoBeanPropertyService) getBean("PoBeanPropertyService");
		beanPropertyService.copyBeanProperty(oldBeanName, oldPropertyName, newBeanName, newPropertyName);

		// adjust targetModule
		if (StringUtils.isEmpty(targetModule) == false) {
			PoBeanProperty newBp = beanPropertyService.findBeanPropertyByKey(newBeanName, newPropertyName);
			if (newBp.getModule()==null || newBp.getModule().getName().equals(targetModule) == false) {
				PoModuleService modulService = (PoModuleService) getBean("PoModuleService");
				PoModule newModule = modulService.getModuleByName(targetModule);
				if (newModule==null)
					throw new IllegalArgumentException("Passed targetModule=" + targetModule + " is not available in the System!");
				
				newBp.setModule( modulService.getModuleByName(targetModule) );
				beanPropertyService.saveBeanProperty(newBp);
			}
		}
		
	}

	private void parse(String oldName, String newName) {
		int oldSeparatorIndex = oldName.indexOf(".");
		
		this.oldBeanName = oldName.substring(0, oldSeparatorIndex);
		this.oldPropertyName = oldName.substring(oldSeparatorIndex + 1);
		
		int newSeparatorIndex = newName.indexOf(".");
		
		this.newBeanName = newName.substring(0, newSeparatorIndex);
		this.newPropertyName = newName.substring(newSeparatorIndex + 1);
	}
	
}
