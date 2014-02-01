package at.workflow.webdesk.po.impl.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;

import at.workflow.webdesk.po.Configurable;
import at.workflow.webdesk.po.PoConnectorService;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoModuleUpdateService;
import at.workflow.webdesk.po.PoRegistrationService;
import at.workflow.webdesk.po.daos.PoFileDAO;
import at.workflow.webdesk.po.impl.PoModuleUpdateServiceImpl;
import at.workflow.webdesk.po.impl.test.helper.RegistrationTestHelper;
import at.workflow.webdesk.po.model.FlowDirection;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.model.PoRegistrationBean;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

public class WTestPoConnectorService extends AbstractTransactionalSpringHsqlDbTestCase {
	
    //Services
    private PoConnectorService connectorService;
    private PoRegistrationService registrationService;
    private PoModuleService moduleService;
    private PoModuleUpdateService moduleUpdateService;
    private PoFileService fileService;
	private PoFileDAO fileDAO;
    
    
    @Override
    protected void onSetUpAfterDataGeneration() throws Exception {
    	
    	if (this.connectorService==null) {
    		this.connectorService = (PoConnectorService) getBean("PoConnectorService");
    		
    		// We use the PoRegistrationServiceTarget Springbean here, as it has *NO* hibernate interceptor
    		// bound, which would reset our FLUSHMODE to never, so that following Exception would occur on calling
    		// any register-Methods in PoRegistrationService:
    		// org.springframework.dao.InvalidDataAccessApiUsageException: Write operations are not allowed in read-only mode (FlushMode.NEVER/MANUAL): 
    		// Turn your Session into FlushMode.COMMIT/AUTO or remove 'readOnly' marker from transaction definition.
    		this.registrationService = (PoRegistrationService) getBean("PoRegistrationServiceTarget");
    		this.moduleService = (PoModuleService) getBean("PoModuleService");
    		this.moduleUpdateService = RegistrationTestHelper.createModuleUpdateServiceManually(getApplicationContext());
    		this.fileService = (PoFileService) getBean("PoFileService");
    		this.fileDAO = (PoFileDAO) getBean("PoFileDAO");
    	}
    	
    	Map<String, PoRegistrationBean> regBeanMap = new HashMap<String, PoRegistrationBean>();
    	regBeanMap.put("po", (PoRegistrationBean) getBean("PoRegistrationBean_po"));
    	
    	((PoModuleUpdateServiceImpl)this.moduleUpdateService).installModules(regBeanMap);

    	
    }

	private void registerTestConnectors() {
		Resource[] ress = { 
    				new ClassPathResource("at/workflow/webdesk/po/impl/connector/dummyDestination/connector-descr.xml"),
    				new ClassPathResource("at/workflow/webdesk/po/impl/connector/dummySource/connector-descr.xml") };
    			
    	registrationService.registerConnectors(ress, moduleService.getModuleByName("po"));
	}
	
	private String getFileIdOfFirstConfigFile(Configurable conf) {
		if( conf.getConfigFiles().size()>0) {
			PoFile firstFile = (PoFile) conf.getConfigFiles().toArray()[0];
			return firstFile.getFileId() ;
		}
		return null;
	}
	
	public void testNewConnector() {
		
		PoConnector conn = new PoConnector();
		conn.setName("dummy");
		conn.setModule( moduleService.getModuleByName("po") );
		connectorService.saveConnector(conn);
		fileService.generateConfigFileOfConfigurable(conn);
		
		assertEquals(1, connectorService.loadAllConnectors().size());

		assertTrue(conn.getConfigFiles().size()==1);
		assertTrue( fileDAO.findFilesWithFileId( getFileIdOfFirstConfigFile(conn) ).size() == 1);
		
		// change the config file
		PoFile configFile = fileService.getFileOfConfigurable(conn);
		Document doc = fileService.getFileAsXML(configFile);
		
		fileService.updateConfigurationFile(conn, doc, configFile.getPath());
		assertEquals(2, conn.getConfigFiles().size());

		int noOfFiles = fileDAO.findFilesWithFileId( configFile.getFileId() ).size();
		assertEquals( "No of configfiles should be 2, but was " + noOfFiles, 2, noOfFiles);	// fri_2013-03-25: Hudson fail:  No of configfiles should be 2, but was 3 expected:<2> but was:<3>
		
	}
    
    public void testCloneConnector() {
    	registerTestConnectors();
    	
    	List<PoConnector> configurableConns = connectorService.findAllConfigurableConnectors();
    	assertEquals(2, configurableConns.size());
    	
    	// create Connector Config
    	PoConnector connConfig = new PoConnector();
    	connConfig.setName("ConnectorConfig");
    	connConfig.setParent( configurableConns.get(0));
    	connConfig.setModule( moduleService.getModuleByName("po") );
    	
		connectorService.saveConnector(connConfig);
		fileService.generateConfigFileOfConfigurable(connConfig);
		
		assertEquals(1, fileDAO.findFilesWithFileId( getFileIdOfFirstConfigFile(connConfig)).size());
		assertEquals(1, connConfig.getConfigFiles().size());
		
		assertTrue(connectorService.loadAllConnectors().size()==3);
		
		PoConnector connClone = connectorService.cloneConnector(connConfig);
		assertEquals(1, fileDAO.findFilesWithFileId( getFileIdOfFirstConfigFile(connClone)).size());
		assertEquals(1, connClone.getConfigFiles().size());
		assertEquals(4, connectorService.loadAllConnectors().size());
    	
    	connectorService.deleteConnector( connClone );
    	assertTrue(connectorService.loadAllConnectors().size()==3);
    	
    }

	public void testGetAvailableConnectors() {
		
		PoConnector source = new PoConnector();
		source.setName("source");
		source.setModule( moduleService.getModuleByName("po") );
		source.setFlowDirection(FlowDirection.SOURCE);
		connectorService.saveConnector(source);
		
		PoConnector sourceAndDest = new PoConnector();
		sourceAndDest.setName("sourceAndDest");
		sourceAndDest.setModule( moduleService.getModuleByName("po") );
		sourceAndDest.setFlowDirection(FlowDirection.SOURCE_AND_DESTINATION);
		connectorService.saveConnector(sourceAndDest);
		
		assertEquals(2, connectorService.loadAllConnectors().size());
		
		List<PoConnector> result = connectorService.getAvailableConnectors(null);
		assertNotNull(result);
		assertTrue(result.isEmpty());
		
		result = connectorService.getAvailableConnectors(new FlowDirection[] {FlowDirection.DESTINATION});
		assertNotNull(result);
		assertEquals(0, result.size());
		
		result = connectorService.getAvailableConnectors(new FlowDirection[] {FlowDirection.SOURCE_AND_DESTINATION});
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("sourceAndDest", result.get(0).getName());
		
		result = connectorService.getAvailableConnectors(new FlowDirection[] {FlowDirection.SOURCE});
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("source", result.get(0).getName());
		
		result = connectorService.getAvailableConnectors(new FlowDirection[] {FlowDirection.SOURCE, FlowDirection.SOURCE_AND_DESTINATION});
		assertNotNull(result);
		assertEquals(2, result.size());
		
	}
    
}
