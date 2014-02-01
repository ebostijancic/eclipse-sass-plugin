package at.workflow.webdesk.tools.pagination.impl;

import java.util.List;

import junit.framework.TestCase;
import at.workflow.webdesk.tools.Filter;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.pagination.PaginationCursor;
import at.workflow.webdesk.tools.pagination.PaginationDataProvider;

public class WTestQueryPaginationCursor extends TestCase {
	
	private static class PaginatationDataProviderMock implements PaginationDataProvider {

		@Override
		public void navigateTo(PaginationCursor page, int index) {
		}

		@Override
		public List<?> getElements(PaginationCursor page) {
			return null;
		}
		
		@Override
		public List<?> getElements(PaginationCursor page, int startIndex) {
			return null;
		}

		@Override
		public long getTotalNumberOfElements(PaginationCursor page) {
			return 0;
		}
		
	}
	
	public void testSetFilters() {
		
		// use positionalquery as start
		PositionalQuery query = new PositionalQuery("from PoPerson where firstName=?", new Object[] { "Florian" });
		
		QueryPaginationCursor cursor = new QueryPaginationCursor(query, 10);
		cursor.setPageDAO( new PaginatationDataProviderMock());
		
		Filter ft = new Filter("lastName", "Weiss");
		cursor.applyFilters(new Filter[] { ft });
		
		assertEquals(cursor.getHqlQuery(), "from PoPerson where firstName=:name1 and lastName=:name2");
		
		cursor.resetFilters();
		
		assertEquals(cursor.getHqlQuery(), "from PoPerson where firstName=:name1");
		
	}

}
