package at.workflow.webdesk.tools.pagination.impl;

import java.util.HashMap;
import java.util.Map;

import at.workflow.webdesk.tools.pagination.PaginationCursor;

public class CriteriaPaginationCursor extends PaginationCursor {
	private Object searchObj;
	private Object exampleObj;
	private Map<String, String> orderObj;
	private Map<String, String> additionalConstraints;
	
	
	public CriteriaPaginationCursor(Object searchObj, Object exampleObj, int pageSize) {
		this.searchObj = searchObj;
		this.exampleObj= exampleObj;
		this.setPageSize(pageSize);
		this.orderObj = new HashMap<String, String>();
		this.additionalConstraints = new HashMap<String, String>();
	}

	
	/**
	 * @return Returns the exampleObj.
	 */
	public Object getExampleObj() {
		return exampleObj;
	}
	/**
	 * @param exampleObj The exampleObj to set.
	 */
	public void setExampleObj(Object exampleObj) {
		this.exampleObj = exampleObj;
	}
	/**
	 * @return Returns the searchObj.
	 */
	public Object getSearchObj() {
		return searchObj;
	}
	/**
	 * @param searchObj The searchObj to set.
	 */
	public void setSearchObj(Object searchObj) {
		this.searchObj = searchObj;
	}
	

	/**
	 * @return Returns the orderObj.
	 */
	public Map<String, String> getOrderObj() {
		return orderObj;
	}
	/**
	 * @param orderObj The orderObj to set.
	 */
	public void setOrderObj(Map<String, String> orderObj) {
		this.orderObj = orderObj;
	}
	
	/**
	 * @return Returns the additionalConstraints.
	 */
	public Map getAdditionalConstraints() {
		return additionalConstraints;
	}
	/**
	 * @param additionalConstraints The additionalConstraints to set.
	 */
	public void setAdditionalConstraints(Map additionalConstraints) {
		this.additionalConstraints = additionalConstraints;
	}


	
}