package at.workflow.webdesk.po.connector.dummyEmptySrcConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;

import at.workflow.webdesk.po.PoSourceConnectorInterface;

public class DummyEmptySrcConnector implements PoSourceConnectorInterface {

	@Override
	public void setConfigurationFile(Document document) {
	}

	@Override
	public void init() {
	}

	@Override
	public List<String> getFieldNames() {
		return Arrays.asList(new String[] { "field1", "field2", "field3" });
	}

	@Override
	public List<Map<String, Object>> findAllObjects(List<String> fieldNames, String constraint) {
		return new ArrayList<Map<String,Object>>();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	}

	@Override
	public void postProcessImportedRecords(List<Map<String, Object>> records) {
	}

}
