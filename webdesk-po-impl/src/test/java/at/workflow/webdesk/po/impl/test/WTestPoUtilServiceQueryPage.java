package at.workflow.webdesk.po.impl.test;

import java.util.List;

import org.apache.commons.lang.text.StrBuilder;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoUtilService;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.Filter;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.pagination.FilterAndSortPaginationCursor;
import at.workflow.webdesk.tools.pagination.PaginationCursor;

public class WTestPoUtilServiceQueryPage extends AbstractPoTestCase {

	public void testQueryPaginationCursor() {

		PaginationCursor page = getUtilService().createQueryPaginationCursor(
				"select lastName, firstName, userName from PoPerson order by lastName asc",
				(String[]) null,
				(Object[]) null,
				10);

		List<PoPerson> allPersons = getOrgService().findAllCurrentPersons(); // TODO: test will fail as soon as someone puts an 'invalid' person into TestData.xml!

		logger.info("No of persons=" + allPersons.size());

		logger.info("actPage=" + page.getActPage());

		assertEquals(page.getTotalNumberOfElements(), allPersons.size());

		logger.info("actPage=" + page.getActPage());

		page.gotoNextPage();
		assertEquals(page.getThisPageLastElementNumber(), 9);
		outputPage(page);

		logger.info("actPage=" + page.getActPage());
		assertEquals(page.getThisPageSize(), 10);

		assertEquals(page.getMaxPage(), 10);

		page.gotoNextPage(); // page2
		page.gotoNextPage(); // page3
		page.gotoNextPage(); // page4
		page.gotoNextPage(); // page5

		assertEquals(page.getThisPageLastElementNumber(), 49);

		page.gotoNextPage(); // page6
		page.getNextElements(); // page7
		page.getNextElements(); // page8
		page.getNextElements(); // page9
		page.getNextElements(); // page10

		assertEquals(page.getThisPageLastElementNumber(), 99);

	}

	public void testQueryPaginationCursorWithFilter() {

		PaginationCursor page = getUtilService().createQueryPaginationCursor(
				"select lastName, firstName, userName from PoPerson where lastName=:argLastName order by lastName asc",
				new String[] { "argLastName" },
				new Object[] { "Weiss" },
				10);

		List<Object[]> results = retrieveElements(page);
		assertTrue(results.size() == 1);
		assertEquals(results.get(0)[0], "Weiss");

		// applying a new filter, means recreating the QueryPage

		page = getUtilService().createQueryPaginationCursor("select lastName, firstName, userName from PoPerson where firstName=:argFirstName order by lastName asc",
				new String[] { "argFirstName" }, new Object[] { "Florian" }, 10);

		results = retrieveElements(page);
		assertTrue(results.size() == 1);
		assertEquals(results.get(0)[0], "Weiss");

	}

	public void testQueryPaginationCursorWithChangingFilter() {

		PositionalQuery query = new PositionalQuery(
				"select p.lastName, p.firstName, p.userName, pg.group.shortName "+
					"from PoPerson p join p.memberOfGroups pg where pg.group.orgStructure.orgType = ?",
				new Object[] { Integer.valueOf(1) });
		
		FilterAndSortPaginationCursor page = getUtilService().createQueryPaginationCursor(query, 10);

		@SuppressWarnings("unchecked")
		List<Object[]> results = (List<Object[]>) page.getElements();
		assertTrue(results.size() > 0);

		page.applyFilters(new Filter[] { new Filter("p.lastName", "Weiss") });

		results = retrieveElements(page);

		assertTrue(results.size() == 1);
		assertEquals(results.get(0)[0], "Weiss");

		page.resetFilters();

		results = retrieveElements(page);
		assertTrue(results.size() == 10); // must be page size
		assertEquals(page.getTotalNumberOfElements(), 99);

		page.applyFilters(new Filter[] {
				new Filter("pg.group.shortName", "SKR"),
				new Filter("p.lastName", "like Fe%") }
				);

		results = retrieveElements(page);
		assertEquals(results.size(), 2);
		assertEquals(page.getTotalNumberOfElements(), 2);
	}

	public void testSetBigPageSize() {
		PositionalQuery query = new PositionalQuery(
				"select p.lastName, p.firstName, p.userName, pg.group.shortName "+
					"from PoPerson p join p.memberOfGroups pg where pg.group.orgStructure.orgType = ?",
				new Object[] { Integer.valueOf(1) });
		
		FilterAndSortPaginationCursor page = getUtilService().createQueryPaginationCursor(query, Integer.MAX_VALUE);

		@SuppressWarnings("unchecked")
		List<Object[]> results = (List<Object[]>) page.getElements();
		assertTrue(results.size() == 99);
	}

	

	@SuppressWarnings("unchecked")
	private List<Object[]> retrieveElements(PaginationCursor cursor) {
		return (List<Object[]>) cursor.getElements();
	}

	private void outputPage(PaginationCursor page) {
		logger.info("++++ Page=" + page.getActPage() + " ++++");
		logger.info("contains " + page.getThisPageSize() + " Elements on the Page, StartIndex= " + page.getThisPageFirstElementNumber() + ", EndIndex=" + page.getThisPageLastElementNumber());

		for (Object row : page.getElements()) {
			logger.info(toLoggableFormat(row));
		}
		logger.info("++++ End of Page=" + page.getActPage() + " ++++");

	}

	private Object toLoggableFormat(Object row) {

		if (row instanceof PersistentObject)
			return row.toString();

		if (row instanceof Object[]) {
			StrBuilder str = new StrBuilder();
			Object[] cols = (Object[]) row;
			int idx = 0;
			for (Object obj : cols) {
				str.append(obj.toString());
				if (idx++ < cols.length - 1)
					str.append(",");
			}
			return str.toString();
		}
		return null;
	}

	private PoUtilService getUtilService() {
		return (PoUtilService) getBean("PoUtilService");
	}

	private PoOrganisationService getOrgService() {
		return (PoOrganisationService) getBean("PoOrganisationService");
	}

}
