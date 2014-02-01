package at.workflow.webdesk.po.impl.daos;

import java.util.ArrayList;
import java.util.List;

import at.workflow.webdesk.tools.pagination.PaginationCursor;
import at.workflow.webdesk.tools.pagination.PaginationDataProvider.PaginationDataProviderBase;
import at.workflow.webdesk.tools.pagination.impl.ListPaginationCursor;

public class PaginationDataProviderListImpl extends PaginationDataProviderBase {

	@Override
	public List<?> getElements(PaginationCursor page) {
		return getElements(page, -1);
	}
	
	@Override
	public List<?> getElements(PaginationCursor page, int startElementIndex) {
		final ListPaginationCursor listPage = (ListPaginationCursor) page;
		if (listPage.getMaxPage() == 0)	// empty!	
			return (new ArrayList<Object>());
			
		final int start = (startElementIndex >= 0)
			? startElementIndex
			: (page.getActPage() != 0)
				? (page.getActPage() - 1) * page.getPageSize()
				: 0;

		final int startIdx = (int) Math.min(start, page.getTotalNumberOfElements() == 0 ? 0 : (page.getTotalNumberOfElements() - 1));
		final int endIdx = (int) Math.min(start + page.getPageSize(), page.getTotalNumberOfElements());

		return listPage.getElementsCache().subList(startIdx, endIdx);
	}

	@Override
	public long getTotalNumberOfElements(PaginationCursor page) {
		return ((ListPaginationCursor) page).getElementsCache().size();
	}

}
