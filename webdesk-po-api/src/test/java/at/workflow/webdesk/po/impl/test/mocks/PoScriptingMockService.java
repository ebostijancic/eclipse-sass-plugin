package at.workflow.webdesk.po.impl.test.mocks;

import java.util.Map;

import at.workflow.webdesk.po.PoScriptingService;
import at.workflow.webdesk.tools.api.JSExecutionContext;

public class PoScriptingMockService implements PoScriptingService {

	public Object executeJS(String jsFunction, Map<String, Object> parameterValueMap) {
		return null;
	}
	
	@Override
	public JSExecutionContext createContext(Object variableValuesHoldingBean) {
		return null;
	}

	public JSExecutionContext createContext(Map<String, ?> parameters) {
		return null;
	}

	public Object executeJS(String jsFunction, JSExecutionContext context) {
		return null;
	}

	public String velocitySubstitution(String template, Map<String, Object> placeholderValues) {
		return null;
	}


}
