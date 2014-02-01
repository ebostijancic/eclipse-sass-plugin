package at.workflow.webdesk.tools.pagination.impl;

import java.util.List;

import at.workflow.webdesk.tools.pagination.PaginationCursor;


/**
 * Special PaginationCursor implementation holding a cache of the complete list of objects, where
 * we want to navigate in smaller page chunk sizes (batches).
 * 
 * Create this class via this service-call {@link PoUtilService#createListPage(List, int)}
 * and backing list of objects, which you would like to use for page navigation.  
 * 
 * @author ggruber, hentner
 */
public class ListPaginationCursor extends PaginationCursor {

	/** the cache for the complete list of objects, where we want to navigate in smaller
	 * page sizes */
	private List<?> elementsCache;

	/** constructor */
	public ListPaginationCursor(List<?> elements, int pageSize) {
		this.elementsCache = elements;
		this.setPageSize(pageSize);
	}

	/** @return Returns the Complete list of cached Elements. */
	public List<?> getElementsCache() {
		return elementsCache;
	}
	/**
	 * @param elements The complete list of objects to be used as backing data for page navigation
	 */
	public void setElementsCache(List<?> elementsCache) {
		this.elementsCache = elementsCache;
	}

}
