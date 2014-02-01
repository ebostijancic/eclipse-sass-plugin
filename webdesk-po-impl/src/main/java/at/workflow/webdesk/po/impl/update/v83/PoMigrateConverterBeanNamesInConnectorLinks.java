package at.workflow.webdesk.po.impl.update.v83;

import at.workflow.webdesk.po.PoConnectorService;
import at.workflow.webdesk.po.model.PoConnectorLink;
import at.workflow.webdesk.po.model.PoFieldMapping;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * As bean names for converters have changed (from converter_<SomeConverterName> to <SomeConverterName>) 
 * we have to remove the trailing "converter_" string in the name.
 * 
 * @author ggruber
 */
public class PoMigrateConverterBeanNamesInConnectorLinks extends PoAbstractUpgradeScript {

	@Override
	public void execute() {
		
		PoConnectorService connService = (PoConnectorService) this.getBean("PoConnectorService");
		for (PoConnectorLink connLink : connService.loadAllConnectorLinks()) {
			boolean toBeSaved=false;
			for (PoFieldMapping fieldMapping : connLink.getMappingFields()) {
				if (fieldMapping.getConverter()!=null && fieldMapping.getConverter().startsWith("converter_")) {
					this.logger.info("Migrated Converter-Beanname in PoFieldMapping = " + fieldMapping);
					fieldMapping.setConverter(fieldMapping.getConverter().replaceAll("converter_", ""));
					toBeSaved=true;
				}
			}
			
			if (toBeSaved) {
				connService.saveConnectorLink(connLink);
				this.logger.info("Migrated Converter-Beannames of Connector-Link = " + connLink);
			}
			
		}
		
	}

}
