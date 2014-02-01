package at.workflow.webdesk.po.timeline;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import at.workflow.webdesk.po.model.PoBase;
import at.workflow.webdesk.po.model.PoHistorization;
import at.workflow.webdesk.tools.api.Historization;

/**
 * @author sdzuban 16.05.2013
 */
public abstract class AbstractTimelineBase extends TestCase {
	
	protected class TestEntity extends PoBase {
		
		private String uid;

		@Override
		public String getUID() {
			return uid;
		}
		@Override
		public void setUID(String uid) {
			this.uid = uid;
		}
	}
	
	protected class TestLink extends PoHistorization {
		
		private String uid;
		private TestEntity entity;
		private String name;
		
		public TestLink(TestEntity entity, String name) {
			super();
			this.entity = entity;
			this.name = name;
		}

		public TestLink(Date validfrom, Date validto) {
			setValidfrom(validfrom);
			setValidto(validto);
		}
		
		public TestEntity getEntity() {
			return entity;
		}
		public String getName() {
			return name;
		}
		public void setEntity(TestEntity entity) {
			this.entity = entity;
		}
		public void setName(String name) {
			this.name = name;
		}
		@Override
		public String getUID() {
			return uid;
		}
		@Override
		public void setUID(String uid) {
			this.uid = uid;
		}
	}
	
	protected void assertDates(List<Historization> links, List<Date> dates) {
		int idx = 0;
		for (Historization link : links) {
			assertEquals(dates.get(idx++), link.getValidfrom());
			assertEquals(dates.get(idx++), link.getValidto());
		}
	}
	
}
