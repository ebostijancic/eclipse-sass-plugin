package at.workflow.webdesk.po;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;


/**
 * this default connector implementation is used to evaluate the final value before starting the synchronization.
 * 
 * @author ebostijancic 23.07.2012
 *
 */
public class PoPreviewConnector implements PoDestinationConnectorInterface, PoSourceConnectorInterface {
	
	private Map<String, Object> previewRecord = new HashMap<String, Object>();
	private Map<String, Object> sourceRecord = new HashMap<String, Object>();
	private List<String> destFieldNames = new ArrayList<String>(255);
	private List<String> srcFieldNames = new ArrayList<String>(255);

	@Override
	public Object saveObject(Map<String, Object> record, String uniqueKeyName, List<String> fieldsInCorrectOrder) {
		previewRecord.clear();
		previewRecord = record;
		return record;
	}

		
	/**
	 * as preview connector has no fields by default, they should be copied from the expected destination
	 * connector to get the same mapping.
	 * 
	 * @param destFieldNames
	 */
	public void copyFieldNamesFromDestinationConnector(List<String> destFieldNames) {
		this.destFieldNames = new ArrayList<String>(destFieldNames);
		Collections.copy(this.destFieldNames, destFieldNames);
	}

	public void copyFieldNamesFromSourceConnector(List<String> sourceFieldNames) {
		this.srcFieldNames = new ArrayList<String>(sourceFieldNames);
		Collections.copy(this.srcFieldNames, sourceFieldNames);
	}
	
	public void addSourceRecord(Map<String, Object> record) {
		sourceRecord = new HashMap<String, Object>(record);
	}
		
	/**
	 * function used to return the previewed valued.
	 * @param field
	 * @return
	 */
	public Object getPreviewedValue(String field) {
		return previewRecord.get(field);
	}

	@Override
	public List<Map<String, Object>> findAllObjects(List<String> fieldNames, String constraint) {
		List<Map<String, Object>> allObjects = new ArrayList<Map<String,Object>>();
		allObjects.add(sourceRecord);
		return allObjects;
	}

	@Override
	public void setConfigurationFile(Document document) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		previewRecord.clear();
	}

	@Override
	public List<String> getFieldNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean containsObject(String id, String primaryKeyName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deleteObject(String id, String primaryKeyName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postProcessImportedObjects(List<Object> objects) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postProcessImportedRecords(List<Map<String, Object>> records) {
		// TODO Auto-generated method stub
		
	}
}
