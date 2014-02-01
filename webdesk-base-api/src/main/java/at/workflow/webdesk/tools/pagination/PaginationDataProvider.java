package at.workflow.webdesk.tools.pagination;

import java.util.List;


/**
 * Interface to be implemented for all different Subclasses of PaginationCursor.
 * 
 * Created on 21.07.2005
 * @author ggruber
 */
public interface PaginationDataProvider {
    
	/** navigate inside the page object to the given index (pagenumber) */
	void navigateTo(PaginationCursor page, int pageIndex);
	
	/** retrieve the elements of the current page */
	List<?> getElements(PaginationCursor page);
	
	/** retrieve the elements of the page with start index. */
	List<?> getElements(PaginationCursor page, int startIndex);
	
	/** return the total number of elements which are held by the backing data list
	 * while having all defined filters applied. */
	long getTotalNumberOfElements(PaginationCursor page);
	
	/** default base implementation */
	public abstract class PaginationDataProviderBase implements PaginationDataProvider {
		
		/** navigate the given Page object to the specified pageIndex */
		@Override
		public void navigateTo(PaginationCursor page, int index) {
			page.setActPage(index);
		}
		
		@Override
		public abstract List<?> getElements(PaginationCursor page);
		
		@Override
		public abstract long getTotalNumberOfElements(PaginationCursor page);
		
	}
}
