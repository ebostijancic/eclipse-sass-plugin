package at.workflow.webdesk.po.impl.update.v26;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import at.workflow.webdesk.po.PoConnectorService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoModuleUpdateService;
import at.workflow.webdesk.po.PoRegistrationService;
import at.workflow.webdesk.po.model.PoRegistrationBean;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoConnectorLink;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * This update Script registers the <code>PoConnectors</code> and links existing
 * <code>PoConnectorLinks</code> with the correct <code>PoConnector</code>'s.
 * 
 * @author DI Harald Entner (hentner) 19.07.2007
 */
public class PoUpdateConnectorsAndConnectorLinks extends PoAbstractUpgradeScript {

	private PoConnectorService connectorService;
	private PoRegistrationService registrationService;
	private PoModuleService moduleService;
	private PoModuleUpdateService moduleUpdateService;
	
	@Override
	@SuppressWarnings("rawtypes")
	public void execute() {
		logger.info("Run PoUpdateConnectorsAndConnectorLinks");
		connectorService = (PoConnectorService) getBean("PoConnectorService");
		registrationService = (PoRegistrationService) getBean("PoRegistrationService");
		moduleService = (PoModuleService) getBean("PoModuleService");
		moduleUpdateService = (PoModuleUpdateService) getBean("PoModuleUpdateService");
				
	    PathMatchingResourcePatternResolver pmResolver = new PathMatchingResourcePatternResolver();
		Iterator i = moduleUpdateService.getModules().iterator();
		logger.info("Found " + moduleUpdateService.getModules().size() + " modules. -> Will try to register Connectors.");
		while (i.hasNext()) {
			String module = (String) i.next();
			
			PoRegistrationBean rb = (PoRegistrationBean) getBean("PoRegistrationBean_" + module);
            
			Iterator j = rb.getRegisterConnectors().iterator();
            while (j.hasNext()) {
            	String pattern = "";
                try{
                    Resource[] ress = null;
                    pattern = (String)j.next();
                    ress = pmResolver.getResources(registrationService.appendRealPathIfNecessary(pattern));
                    registrationService.registerConnectors(ress,this.moduleService.getModuleByName(rb.getFolderOfPackage()));
                    logger.info("Registered " + ress.length + " Connectors.");
                } catch (Exception e) {
                	logger.error(e,e);
                	logger.warn("no File found at pattern: " + pattern);
                }            
            }   
		}
		
		// now all Connector's should reside in the database
		
		Iterator connLinks  =this.connectorService.loadAllConnectorLinks().iterator();
		while (connLinks.hasNext()) {
			PoConnectorLink cl = (PoConnectorLink) connLinks.next();
			boolean notComplete = false;
			PoConnector srcConn=null;
			PoConnector dstConn=null;
			if (cl.getSourceConnector()!=null && !cl.getSourceConnector().equals("")) {

				List l = this.connectorService.findConnectorByName(cl.getSourceConnector());
				if (l.size()==1) {
					srcConn = (PoConnector) l.get(0);
				} else
					notComplete=true;
				
			}

			if (cl.getDestinationConnector()!=null && !cl.getDestinationConnector().equals("")) {

				List l = this.connectorService.findConnectorByName(cl.getDestinationConnector());
				if (l.size()==1) {
					dstConn= (PoConnector) l.get(0);
				} else
					notComplete=true;
			}

			if (!notComplete) {
				cl.setSrcConnector(srcConn);
				cl.setDestConnector(dstConn);
				this.connectorService.saveConnectorLink(cl);
				logger.info("Successfully updated " + cl.getName() + ".");
			}
		}
	}

}
