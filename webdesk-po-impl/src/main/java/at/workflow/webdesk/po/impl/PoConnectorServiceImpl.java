package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import at.workflow.webdesk.po.MetaDataDrivenConnector;
import at.workflow.webdesk.po.PoConnectorInterface;
import at.workflow.webdesk.po.PoConnectorService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoDestinationConnectorInterface;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.PoSourceConnectorInterface;
import at.workflow.webdesk.po.daos.PoConnectorDAO;
import at.workflow.webdesk.po.model.FlowDirection;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoConnectorLink;
import at.workflow.webdesk.po.model.PoFieldMapping;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.tools.ReflectionUtils;

/**
 * Implementation of the PoConnectorService
 * Created on 07.04.2006
 * @author hentner (Harald Entner)
 */
public class PoConnectorServiceImpl implements PoConnectorService, ApplicationContextAware {

	protected final String logCategory = "webdesk.connectors.po.PoConnectorServiceImpl";
	protected final Logger logger = Logger.getLogger(logCategory);

	private PoFileService fileService;

	private PoConnectorDAO connectorDao;

	private ApplicationContext appCtx;

	@Override
	public PoConnector getConnector(String uid) {
		return connectorDao.get(uid);
	}

	@Override
	public PoConnectorLink getConnectorLink(String uid) {
		return connectorDao.getConnectorLink(uid);
	}

	@Override
	public PoFieldMapping getFieldMapping(String uid) {
		return connectorDao.getFieldMapping(uid);
	}

	@Override
	public List<PoFieldMapping> findFieldMappingsOfLink(PoConnectorLink link) {
		return connectorDao.findFieldMappingsOfLink(link);
	}

	@Override
	public void saveConnectorLink(PoConnectorLink link) {
		connectorDao.saveConnectorLink(link);
	}

	@Override
	public void saveFieldMapping(PoFieldMapping field) {
		connectorDao.saveFieldMapping(field);

	}

	@Override
	public void deleteConnectorLink(PoConnectorLink link) {
		connectorDao.deleteConnectorLink(link);

	}

	@Override
	public void deleteFieldMapping(PoFieldMapping field) {
		connectorDao.deleteFieldMapping(field);
	}

	@Override
	public List<PoConnector> findConnectorByName(String name) {
		return connectorDao.findConnectorByName(name);
	}

	@Override
	public PoConnector findConnectorByNameAndModule(String name, PoModule module) {
		return connectorDao.findConnectorByNameAndModule(name, module);
	}

	@Override
	public List<Integer> getSyncDirections() {
		ArrayList<Integer> al = new ArrayList<Integer>();
		al.add(new Integer(PoConstants.SYNC_SOURCE_DEST));
		al.add(new Integer(PoConstants.SYNC_DEST_SOURCE));
		al.add(new Integer(PoConstants.SYNC_BOTH));
		return al;
	}

	@Override
	public List<PoConnector> getAvailableConnectors(boolean writeable) {
		return connectorDao.findAllUsableConnectors(writeable);
	}

    @Override
	public List<PoConnector> getAvailableConnectors(FlowDirection[] flowDirections) {
    	return connectorDao.findAllUsableConnectors(flowDirections);
    }

	@Override
	public List<PoConnectorLink> loadAllConnectorLinks() {
		return connectorDao.loadAllConnectorLinks();
	}

	@Override
	public void deleteConnector(PoConnector connector) {
		connectorDao.delete(connector);

	}

	@Override
	public List<PoConnector> loadAllConnectors() {
		return connectorDao.loadAll();
	}

	@Override
	public void saveConnector(PoConnector connector) {
		connectorDao.save(connector);
	}

	@Override
	public List<PoConnector> findAllConfigurableConnectors() {
		return connectorDao.findAllConfigurableConnectors();
	}

	@Override
	public PoSourceConnectorInterface getSrcConnectorImpl(PoConnector connector) {
		return (PoSourceConnectorInterface) getConnectorImpl(connector);
	}

	@Override
	public PoDestinationConnectorInterface getDestConnectorImpl(PoConnector connector) {
		return (PoDestinationConnectorInterface) getConnectorImpl(connector);
	}

	@Override
	public PoConnectorInterface getConnectorImpl(PoConnector connector) {
		PoConnectorInterface myConn = null;
		try {
			if (connector.getParent() != null) {
				// PoConnector is a configured connector
				myConn = getConnectorInstance(connector.getParent());
				myConn.setConfigurationFile(fileService.getFileAsXML( fileService.getFileOfConfigurable(connector).getUID() ));
			} else {
				myConn = getConnectorInstance(connector);
			}
		}
		catch (Exception e) {
			logger.error("Error occured while instantiating connector " + connector.getName() + "...", e);
			throw new PoRuntimeException(e);
		}

		// Initialize connector
		if (myConn != null) {
			myConn.setApplicationContext(appCtx);
			if (myConn instanceof MetaDataDrivenConnector)
				((MetaDataDrivenConnector) myConn).setConnectorMetaData(connector);
			// init() needs sometimes setConfigurationFile(document) otherwise exceptions are thrown
			myConn.init();
		}

		return myConn;
	}

	private PoConnectorInterface getConnectorInstance(PoConnector connector) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (appCtx.containsBean(connector.getName())) {
			// prefer spring bean when present
			return (PoConnectorInterface) appCtx.getBean(connector.getName(), Class.forName(connector.getClassName()));
		}
		// instantiate by classname
		return (PoConnectorInterface) Class.forName(connector.getClassName()).newInstance();
		// fri_2012-06-05 TODO: do not allow a connector without bean definition, this leads
		// to ambiguous implementations because otherwise injected fields must be evaluated manually then.
		// sd: this is necessary for existing configurations of e.g. dbConnector to work
	}

	@Override
	public PoConnectorLink findConnectorLinkByName(String name) {
		return connectorDao.findConnectorLinkByName(name);
	}

	@Override
	public List<PoConnectorLink> findConnectorLinksByDestinationName(String name) {
		return connectorDao.findConnectorLinksByDestinationName(name);
	}

	@Override
	public PoConnector cloneConnector(PoConnector connector) {
		PoConnector cloned = new PoConnector();
		if (connector.getParent() != null)
			cloned.setParent(connector.getParent());
		
		cloned.setClassName(connector.getClassName());
		cloned.setConfigurable(connector.isConfigurable());
		cloned.setModule(connector.getModule());
		
		cloned.setName(connector.getName() + "_cloned" + UUID.randomUUID()); // not unique!
		cloned.setUpdateOnVersionChange(connector.isUpdateOnVersionChange());
		cloned.setFlowDirection( connector.getFlowDirection() );

		// we have to persist our clone
		saveConnector(cloned);
		
		// first we have to generate a default configuration
		if (cloned.getParent() != null) {
			// save the configuration XML of the original connector into the cloned one.
			PoFile origFile = fileService.getFileOfConfigurable(connector);
			fileService.updateConfigurationFile(cloned,
					fileService.getFileAsXML(origFile),
					"at/workflow/webdesk/"+cloned.getParent().getModule().getName()+"/connector/configs/"+cloned.getName()+".xml");
		}

		return cloned;
	}
	
	public FlowDirection getFlowDirectionByInspection(PoConnector connector) {
		
		String className = connector.getClassName();
		if (StringUtils.isBlank(className) && connector.getParent() != null)
			className = connector.getParent().getClassName();
		
		@SuppressWarnings("rawtypes")
		List<Class> implementedInterfaces = ReflectionUtils.getAllImplementedInterfaces(className); 
		boolean isSource = implementedInterfaces.contains(PoSourceConnectorInterface.class);
		boolean isDestination = implementedInterfaces.contains(PoDestinationConnectorInterface.class);
		if (isSource && isDestination)
			return FlowDirection.SOURCE_AND_DESTINATION;
		else if (isSource)
			return FlowDirection.SOURCE;
		else if (isDestination)
			return FlowDirection.DESTINATION;
		else
			throw new IllegalStateException("No valid Flowdirection could be extracted out of Connector with name=" + connector.getName());
	}

	public void setFileService(PoFileService fileService) {
		this.fileService = fileService;
	}

	@Override
	public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
		this.appCtx = appCtx;
	}

	public void setConnectorDao(PoConnectorDAO connectorDao) {
		this.connectorDao = connectorDao;
	}

}
