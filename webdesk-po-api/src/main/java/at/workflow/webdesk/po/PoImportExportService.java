package at.workflow.webdesk.po;

import org.w3c.dom.Document;

import at.workflow.webdesk.po.model.PoClient;

/**
 * <p>
 * This interface defines some common import functions to import 
 * various webdesk data.
 * </p>
 * @author ggruber
 * 
 * @see at.workflow.webdesk.po.PoActionService
 * @see at.workflow.webdesk.po.PoOrganisationService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.PoModuleService
 * @see at.workflow.webdesk.po.PoFileService
 * @see at.workflow.webdesk.po.PoLogService
 * @see at.workflow.webdesk.po.PoLanguageService
 * @see at.workflow.webdesk.po.PoBeanPropertyService
 * @see at.workflow.webdesk.po.PoJobService

 *
 */
public interface PoImportExportService {
	
	public void importOrganisationStructure(Document doc);
	
	public void importOrganisationStructure(Document doc, PoClient client);
	
	public void importRoles(Document doc);
	
}
