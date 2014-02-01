package at.workflow.webdesk.po.impl.update.v41;

import java.util.List;

import org.jdom.transform.XSLTransformer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;

import at.workflow.tools.XMLTools;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * Migrates Configuration files of actions.
 * <p/>
 * More precisely, the <code>pattern</code> and <code>decimal-seperator</code> 
 * are included and set to default values. 
 * <p/>
 * Only <code>column</code> nodes with attribute <code>type</code>=<code>account</code>
 * are considered.
 *
 * @author DI Harald Entner (hentner), created at 23.04.2008
 */
public class MigrateOrgJournalConfigXmls extends PoAbstractUpgradeScript {
	
	private PoActionService actionService;
	private PoFileService fileService;

	@Override
	public void execute() {
		actionService= (PoActionService) this.getBean("PoActionService");
		fileService = (PoFileService) this.getBean("PoFileService");
		
		// adapt OrgJournal Configuration Files
		PoAction action = actionService.findActionByNameAndType("ta_getOrgJournal", PoConstants.ACTION_TYPE_ACTION);
		updateFiles(actionService.findConfigsFromAction(action));
		
		// adapt Journal Configuration Files
		action = actionService.findActionByNameAndType("ta_getJournal", PoConstants.ACTION_TYPE_ACTION);
		updateFiles(actionService.findConfigsFromAction(action));
	}
	
	private void updateFiles(List<PoAction> actions) {
		for (PoAction action : actions) {
			Document configXml = fileService.getFileAsXML(fileService.getFileOfConfigurable(action).getUID());
			
			Resource res = new ClassPathResource("at/workflow/webdesk/po/impl/update/v41/updateConfigs.xsl");
	
			org.jdom.Document resDoc = null;
			try {
				XSLTransformer transformer = new XSLTransformer(res.getInputStream());
				resDoc = transformer.transform(XMLTools.convertToJdomDoc(configXml));
			}
			catch (Exception e) {
				throw new PoRuntimeException("failed to update fileconnector config!");
			}
			
			fileService.updateFileOfAction(action, XMLTools.convertToW3cDoc(resDoc));
			logger.info("Added pattern and decimal-seperator to Config " + action.getName());
		}
	}

}
