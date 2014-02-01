package at.workflow.webdesk.tools.pagination;

import java.util.List;

/**
 * Base class for paging of lists containing entity-data. It is used to either hold the actual List of objects
 * or depends on a persistence-layer which is responsible to deliver the correct elements batch out of the complete
 * list of objects.<br/><br/>    
 * 
 * Be aware that the methods getMaxPage(), getActPage(), getPageSize(), getTotalNumberOfElements() and getPagesToNavigate()
 * are used in the JX-Template Macro pagination in jx-util-macros_t.xml and therefore can not be extracted
 * via eclipse views to spot the call hierarchy.<br/><br/>
 * 
 * TODO the value of currentPage is not guaranteed, depends on order of method calls!
 * also see {@link QueryPaginationCursor}, {@link PoCriteriaPage} and {@link ListPaginationCursor}
 * @author hentner, ggruber
 */
public abstract class PaginationCursor {

	/** number of elements which fit into one page. Translates to the batchsize when querying
	 * from a database. */
	private int pageSize;

	/** the current pagenumer. Set this property and call getElements() to retrieve the subset
	 * of data corresponding to the page.
	 * Means that the following range of records is returned:
	 * records[ currentPage*Pagesize ] - record[(currentPage+1)*PageSize-1]
	 */
	private int currentPage;

	/** The upper limit of pages, until where we want to be able to navigate
	 * to individual pages via direct links. If this limit get exceeded by the
	 * total number of pages, a drop down should be displayed instead. 
	 * Has actually no effect on the paging mechnism and should be removed! */
	private int pagesToNavigate;

	/** the actual implementation fetching the data from the backing store */
	private PaginationDataProvider pageDAO;

	
	
	/** returns true, if the navigation cursor is currently located on the very first page */
	public boolean isFirstPage() {
		return currentPage == 1;
	}

	/** returns true, if the navigation cursor is currently located on the very last page */
	public boolean isLastPage() {
		return currentPage == getMaxPage();
	}

	/** returns true, if we are able to navigate to another page, meaning we are not at the last page yet. */
	public boolean hasNextPage() {
		return currentPage < getMaxPage();
	}

	/** returns true, if we are able to navigate to a previous page, meaning we are not at the very first page. */
	public boolean hasPreviousPage() {
		return currentPage != 1;
	}

	/** Return the element index of the first element on the current page 
	 * within the complete backing datalist. f.i. we have 20 elements in the backing list and
	 * a pagesize of 10 and we are on the 2nd page. Therefore the element of the first element
	 * on the page is 10 (assuming the index starts at zero).
	 * @return index of first element on current page
	 */
	public long getThisPageFirstElementNumber() {
		return currentPage * pageSize - pageSize;
	}

	/** Return the element index of the last element on the current page 
	 * within the complete backing datalist. f.i. we have 20 elements in the backing list and
	 * a pagesize of 7 and we are on the 3rd page. Therefore the index of the last element
	 * on the page is 3 (assuming the index starts at zero).
	 * @return index of last element on current page
	 */
	public long getThisPageLastElementNumber() {
		final long total = getTotalNumberOfElements();
		final int maxElementNumber = currentPage * pageSize - 1;
		return (maxElementNumber > total) ? total : maxElementNumber;
	}

	/** return the number of elements on the current page. Should be between 1 and the pageSize.
	 * If we are on the last page, the pagesize is probably smaller.
	 * @return number of elements on the current page.
	 */
	public int getThisPageSize() {
		final int maxPage = getMaxPage();
		if (currentPage < maxPage)
			return pageSize;
		return pageSize * maxPage - (int) getTotalNumberOfElements();
	}

	/** return the page size, the number of elements which fit into one page */
	public int getPageSize() {
		return pageSize;
	}

	/** return the current page number */
	public int getPageNumber() {
		return currentPage;
	}

	/** return the List of elements contained on the current page */
	public List<?> getElements() {
		return pageDAO.getElements(this);
	}

	/** return the List of elements contained on the current page */
	public List<?> getElements(int startIndex) {
		return pageDAO.getElements(this, startIndex);
	}

	/** navigate to the next page and return the elements on that next page */
	public List<?> getNextElements() {
		currentPage++;
		return pageDAO.getElements(this);
	}

	/** navigate to the next page and return a new Page object. */
	public void gotoNextPage() {

		if (currentPage <= getMaxPage())
			currentPage++;

		pageDAO.navigateTo(this, currentPage);
	}

	/** navigate to the previous page */
	public void gotoPreviousPage() {

		if (currentPage > 1)
			currentPage--;

		pageDAO.navigateTo(this, currentPage);
	}

	/** set the pagesize (batchsize) for this pagination */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/** set the backing implementation */
	public void setPageDAO(PaginationDataProvider pageDAO) {
		this.pageDAO = pageDAO;
	}

	/** return the total number of elements */
	public long getTotalNumberOfElements() {
		return pageDAO.getTotalNumberOfElements(this);
	}

	/** returns the last page number */
	public int getMaxPage() {
		final long totalRecords = getTotalNumberOfElements();
		if (pageSize > 0) {
			if (totalRecords % pageSize == 0)
				return new Long((totalRecords / new Long(pageSize).longValue())).intValue();

			return new Long((totalRecords / new Long(pageSize).longValue()) + 1).intValue();
		}
		return 0;
	}

	/**
	 * @return the current page, either 0 (never initialized or incremented)
	 * 		or 1-n (after once having called setActPage).
	 */
	public int getActPage() {
		return currentPage;
	}

	/**
	 * @param currentPage the current page to set, starts with 1, so 0 is translated to 1.
	 */
	public void setActPage(int currentPage) {
		int maxPage = this.getMaxPage();
		if (currentPage == 0) {
			this.currentPage = 1;
		} else if (currentPage > maxPage) {
			this.currentPage = maxPage;
		} else {
			this.currentPage = currentPage;
		}
	}

	/** @deprecated  */
	public int getPagesToNavigate() {
		return this.pagesToNavigate;
	}

	/** @deprecated  
	 * TODO: remove this API to somewhere else! */
	public void setPagesToNavigate(int pagesToNavigate) {
		this.pagesToNavigate = pagesToNavigate;
	}

}
