package at.workflow.webdesk.po.impl.test;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.PoBeanPropertyService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.po.model.PoBeanPropertyValue;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

public class WTestPoBeanPropertyServiceImpl extends AbstractTransactionalSpringHsqlDbTestCase {

	private PoBeanPropertyService beanPropertyService;
	private PoModuleService moduleService;
	public static PoModule module;
	
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		beanPropertyService =(PoBeanPropertyService) applicationContext.getBean("PoBeanPropertyService");
		moduleService =(PoModuleService) applicationContext.getBean("PoModuleService");
		
		if (module==null) {
			module = new PoModule();
			module.setName("po");
			module.setCreatedAt(new Date());
			moduleService.saveModule(module);
		}
		super.onSetUpAfterDataGeneration();
	}
	
	public void testSaveAndDeleteListValueBeanProperty() {

		// test save with propertyvalues
		PoBeanProperty bp = new PoBeanProperty();
		bp.setBeanName("PoOptions");
		bp.setClassName("at.workflow.webdesk.po.model.PoOptions");
		bp.setPropertyName("clusterNodes");
		bp.setList(true);
		bp.setModule(module);
		bp.setType(PoConstants.STRING);
		
		bp.addEntry(new PoBeanPropertyValue("cluster1"));
		bp.addEntry(new PoBeanPropertyValue("cluster2"));
		bp.addEntry(new PoBeanPropertyValue("cluster3"));
		
		beanPropertyService.saveBeanProperty(bp);
		
		// check if it worked
		List<PoBeanProperty> myBPs = beanPropertyService.readBeanProperties();
		assertTrue("There should be at least one BeanProperty in the db!", myBPs.size()>=1);

		PoBeanProperty bp2 = beanPropertyService.findBeanPropertyByKey("PoOptions", "clusterNodes");
		assertTrue("There should be 3 values for PoOptions.clusterNodes in the db!", bp2.getEntries().size()==3);
		
		// check deletion
		beanPropertyService.deleteBeanProperty(bp2);
		assertTrue("No PoOptions.clusterNodes should be left in the db", beanPropertyService.findBeanPropertyByKey("PoOptions", "clusterNodes") == null);
		
	}
}
