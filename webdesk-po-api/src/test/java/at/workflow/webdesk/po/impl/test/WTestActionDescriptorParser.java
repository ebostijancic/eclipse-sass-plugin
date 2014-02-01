package at.workflow.webdesk.po.impl.test;


import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.webdesk.po.impl.test.mocks.PoActionMockService;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoActionParameter;
import at.workflow.webdesk.po.model.PoActionUrlPattern;
import at.workflow.webdesk.po.model.PoContextParameter;
import at.workflow.webdesk.po.util.ActionDescriptorParser;

import junit.framework.TestCase;

public class WTestActionDescriptorParser extends TestCase {
	
	private static class PoActionServiceMock extends PoActionMockService {
		
		static final PoAction dummyAction = new PoAction(); 
		@Override
		public PoAction findActionWithFullName(String fullName) {
			return dummyAction;
		}
		
	}
	
	public void testParseActionDescriptor() throws Exception {
		
		Resource res = new ClassPathResource("at/workflow/webdesk/po/test/act-descr-test.xml");
		
		PoAction action = ActionDescriptorParser.parseActionInfosFromDescriptor(new PoActionServiceMock(), res.getInputStream(), null);
		
		assertEquals(action.getCaption(), "Aktion");
		assertEquals(action.getDescription(), "Formular zum Editieren einer Aktion");
		assertEquals(action.getImage(), "actions/exec.png");
		assertEquals(action.getImageSet(), "crystalIcons");
		assertEquals(action.getActionFolder(), "po");
		assertEquals(action.isConfigurable(), false);
		assertEquals(action.isAllowUpdateOnVersionChange(), true);
		assertEquals(action.getDefaultViewPermissionType(), 0);
		assertEquals(action.isUniversallyAllowed(), false);
		assertEquals(action.isDisableContinuationInvalidate(), false);
		assertEquals(action.getSqlQuery(), "from HrPersonGa");
		
		assertEquals(action.getController(), "PoEditActionActionHandler");
		assertEquals(action.getControllerPattern(), "cocoon-flow-java");
		
		assertEquals(action.getAllowsAction(), PoActionServiceMock.dummyAction);
		
		assertTrue(action.getAttributes().size() == 2);
		assertTrue(action.getAttributes().containsKey("defaultOrderBy"));
		assertTrue(action.getAttributes().get("defaultOrderBy").equals("order by hiddenLastName"));
		
		assertTrue("Urlpatterns should be 3 but have been " + action.getUrlPatterns().size(), action.getUrlPatterns().size() == 3);
		
		for (PoActionUrlPattern urlPattern : action.getUrlPatterns()) {
			assertTrue(urlPattern.getName().startsWith("Calendar"));
			assertTrue(urlPattern.getPattern()!=null);
		}
		
		assertTrue(action.getParameters().size() == 3);
		boolean hasParameterYear = false;
		for (PoActionParameter parameter : action.getParameters()) {
			if ("uid".equals(parameter.getName()) && !parameter.getType().equals("string"))
				fail("Parameter uid should be of type string!");
			
			if ("year".equals(parameter.getName()))
				hasParameterYear = true;
		}
		if (hasParameterYear == false)
			fail("There should be a parameter year!");
		
		assertTrue(action.getContextParameters().size() == 5);
		
		List<String> allNames = Arrays.asList( new String[] {"$year", "$month", "$day", "$fullDate", "$taid"} );
		
		for (PoContextParameter ctxParam : action.getContextParameters()) {
			assertTrue(allNames.contains( ctxParam.getName() ));
		}
		
	}
	

}
