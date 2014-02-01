package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.HistorizationTimelineRepairer;
import at.workflow.webdesk.po.link.Linker;
import at.workflow.webdesk.po.model.PoHistorization;
import at.workflow.webdesk.po.timeline.HistorizationTimelineUtils;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * @author sdzuban 16.05.2013
 */
public abstract class AbstractHistorizationTimelineRepairerTestCase extends AbstractTransactionalSpringHsqlDbTestCase {
	
	public class TestLinker implements Linker {

		/** {@inheritDoc} */
		@Override
		public void addLinks(Historization relation) {
		}
		
	}
	
	public static class TestLink extends PoHistorization {
		
		private String uid;
		private String target;
		
		/** No-arg constructor needed by BeanUtils.cloneBean(). */
		public TestLink() {
		}
		
		public TestLink(String uid, Date validfrom, Date validto, String target) {
			setUID(uid);
			setValidfrom(validfrom != null ? DateTools.dateOnly(validfrom) : null);
			setValidto(validto != null ? DateTools.lastMomentOfDay(validto) : null);
			this.target = target;
		}
		
		@Override
		public String getUID() {
			return uid;
		}
		@Override
		public void setUID(String uid) {
			this.uid = uid;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}
		
	}
	
	protected static final Date date0000 = DateTools.toDate(2000, 1, 1);
	protected static final Date date0101 = DateTools.toDate(2100, 1, 1);
	protected static final Date date0203 = DateTools.toDate(2100, 3, 2);
	protected static final Date date0303 = DateTools.toDate(2100, 3, 3);
	protected static final Date date3103 = DateTools.toDate(2100, 3, 31);
	protected static final Date date0104 = DateTools.toDate(2100, 4, 1);
	protected static final Date date3005 = DateTools.toDate(2100, 5, 30);
	protected static final Date date3105 = DateTools.toDate(2100, 5, 31);
	protected static final Date date3006 = DateTools.toDate(2100, 6, 30);
	protected static final Date date0107 = DateTools.toDate(2100, 7, 1);
	protected static final Date date3009 = DateTools.toDate(2100, 9, 30);
	protected static final Date date0110 = DateTools.toDate(2100, 10, 1);
	protected static final Date date3010 = DateTools.toDate(2100, 10, 30);
	protected static final Date date3110 = DateTools.toDate(2100, 10, 31);
	protected static final Date date3112 = DateTools.toDate(2100, 12, 31);
	protected static final Date date9999 = DateTools.toDate(2200, 12, 31);
	
	protected final List<Historization> fromDB = new ArrayList<Historization>();
	protected final List<Historization> links = new ArrayList<Historization>();
	private HistorizationTimelineRepairer repairer;
	
	/** {@inheritDoc} */
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		if (repairer == null)
			repairer = (HistorizationTimelineRepairer) getBean("HistorizationTimelineRepairer");
		
		fromDB.clear();
		// the disorder is intentional to simulate real conditions
		fromDB.add(new TestLink("one", date0101, date3103, "target1"));
		fromDB.add(new TestLink("three", date0107, date3009, "target3"));
		fromDB.add(new TestLink("four", date0110, date3112, "target4"));
		fromDB.add(new TestLink("two", date0104, date3006, "target2"));
		
		links.clear();
		links.addAll(fromDB);
	}
	
	protected HistorizationTimelineRepairer getRepairer() {
		return repairer;
	}
	
	protected void assertDates(List<Historization> links, List<Date> dates) {
		int idx = 0;
		for (Historization link : HistorizationTimelineUtils.getSortedHistorizationList(links)) {
			assertEquals(DateTools.dateOnly(dates.get(idx++)), link.getValidfrom());
			assertEquals(DateTools.lastMomentOfDay(dates.get(idx++)), link.getValidto());
		}
	}
}
