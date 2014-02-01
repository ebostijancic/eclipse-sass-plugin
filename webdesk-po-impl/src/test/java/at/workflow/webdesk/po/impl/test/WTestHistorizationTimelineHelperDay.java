package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.po.HistorizationTimelineHelper;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.PoParentGroupHistorizationServiceAdapter;
import at.workflow.webdesk.po.impl.test.WTestHistorizationTimelineHelperDay.MyHistorizationAdapter.MyProtocolItem;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.DateTools.DatePrecision;
import at.workflow.webdesk.tools.date.HistorizationHelper;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * This is exploratory test case mapping the behaviour of HistorizationTimelineHelper.
 * 
 * TODO tests for structurally unequal overlapping links
 * 
 * @author sdzuban 19.03.2013
 */
public class WTestHistorizationTimelineHelperDay extends AbstractTransactionalSpringHsqlDbTestCase {

//	------------------------ INNER ADAPTER CLASSES -----------------------
	
	/**
	 * historization adapter for linking entity with protocol of method calls and data
	 * based on PoParentGroupHistorizationServiceAdapter 
	 */
	public static class MyHistorizationAdapter extends PoParentGroupHistorizationServiceAdapter {

		public class MyProtocolItem {
			
			private String methodName;
			private String linkUid;
			private Date validFrom;
			private Date validTo;
			
			public MyProtocolItem(String methodName, Historization historizationObject) {
				super();
				this.methodName = methodName;
				if (historizationObject != null) {
					linkUid = historizationObject.getUID();
					validFrom = historizationObject.getValidfrom();
					validTo = historizationObject.getValidto();
				}
			}
			public String getMethodName() {
				return methodName;
			}
			public String getLinkUid() {
				return linkUid;
			}
			public Date getValidFrom() {
				return validFrom;
			}
			public Date getValidTo() {
				return validTo;
			}
		}
		
		private List<MyProtocolItem> protocol = new ArrayList<MyProtocolItem>();
		
		public MyHistorizationAdapter(PoGroup parent, PoGroup child) {
			super(parent, child);
		}

		public List<MyProtocolItem> getProtocol() {
			return protocol;
		}

		/** {@inheritDoc} */
		@Override
		public Historization generateEmptyObject(Date from, Date to) {
			Historization pg = super.generateEmptyObject(from, to); 
			protocol.add(new MyProtocolItem("generateEmptyObject", pg));
			return pg;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isStructurallyEqual(Historization existingHistObject, Historization newObject) {
			protocol.add(new MyProtocolItem("isStructurallyEqual", newObject));
			return super.isStructurallyEqual(existingHistObject, newObject);
		}

		/** {@inheritDoc} */
		@Override
		public Historization copyHistObject(Historization existingHistObject) {
			protocol.add(new MyProtocolItem("copyHistObject", existingHistObject));
			return super.copyHistObject(existingHistObject);
		}
		
		/** {@inheritDoc} */
		@Override
		public void deleteObject(Historization historizationObject) {
			protocol.add(new MyProtocolItem("deleteObject", historizationObject));
			super.deleteObject(historizationObject);
		}

		/** {@inheritDoc} */
		@Override
		public void saveObject(Historization historizationObject) {
			protocol.add(new MyProtocolItem("saveObject", historizationObject));
			super.saveObject(historizationObject);
		}
	}

//	------------------- FIELDS ----------------------------
	
	private HistorizationTimelineHelper helper;
	private PoOrganisationService orgService;
	
	private PoGroup one, two;
	
//	-------------------------- METHODS ------------------------------------
	
	/** {@inheritDoc} */
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		if (helper == null) {
			helper = (HistorizationTimelineHelper) getBean("HistorizationTimelineHelper");
			orgService = (PoOrganisationService) getBean("PoOrganisationService");
		}
		
		PoClient c = new PoClient();
		c.setName("client");
		orgService.saveClient(c);
		PoOrgStructure os = new PoOrgStructure();
		os.setName("os");
		os.setHierarchy(true);
		os.setClient(c);
		orgService.saveOrgStructure(os);
		
		// two groups in same org structure without any relationship
		
		one = new PoGroup();
		one.setShortName("g1");
		one.setName("one");
		one.setOrgStructure(os);
		one.setClient(c);
		orgService.saveGroup(one);

		two = new PoGroup();
		two.setShortName("g2");
		two.setName("two");
		two.setOrgStructure(os);
		two.setClient(c);
		orgService.saveGroup(two);
	}

	public void testPoDayHistorizationInitialization() {
		
		PoPersonGroup personGroup = new PoPersonGroup();
		assertEquals(DateTools.today(), personGroup.getValidfrom());
		assertEquals(DateTools.INFINITY, personGroup.getValidto());
	}
	
	public void testNulls() {
		
		try {
			helper.generateHistorizationObject(null, null, null, null);
			fail("Accepted null adapter");
		} catch (Exception e) { }
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		try {
			helper.generateHistorizationObject(null, adapter, null, null);
			fail("Accepted null existing objects list");
		} catch (Exception e) { }
		
		List<PoParentGroup> links = new ArrayList<PoParentGroup>();
		assertNotNull(helper.generateHistorizationObject(links, adapter, null, null));
	}
	
//	------------------------------- FIRST LINK -----------------------------------
	
	/** first link without time specifications is instantiated from now to 1.1.3000 */
	public void testSimplestEMPTYGeneration() {
		
		List<PoParentGroup> links = new ArrayList<PoParentGroup>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Historization result = helper.generateHistorizationObject(links, adapter, null, null);
		assertResult(new Date(), DateTools.INFINITY, result);
		
		assertLinking();
		
		assertProtocol(adapter);
	}

	public void testGenerationWithDates() {
		
		List<PoParentGroup> links = new ArrayList<PoParentGroup>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from = DateTools.toDate(2013, 3, 19);
		Date to = DateTools.toDate(2014, 3, 19);
		
		Historization result = helper.generateHistorizationObject(links, adapter, from, to);
		assertResult(from, to, result);
		
		assertLinking();
		
		assertProtocol(adapter);
	}

	public void testFirstPastPastLink() {
		
		List<PoParentGroup> links = new ArrayList<PoParentGroup>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from = DateTools.toDate(2000, 3, 19, 10, 15);
		Date to = DateTools.toDate(2001, 3, 19, 8, 50);
		
		Historization result = helper.generateHistorizationObject(links, adapter, from, to);
		assertResult(from, to, result);
		
		assertLinking();
		
		assertProtocol(adapter);
	}

	public void testFirstPastNowLink() {
		
		List<PoParentGroup> links = new ArrayList<PoParentGroup>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from = DateTools.toDate(2000, 3, 19, 10, 15);
		Date to = new Date();
		
		Historization result = helper.generateHistorizationObject(links, adapter, from, to);
		assertResult(from, to, result);
		
		assertLinking();
		
		assertProtocol(adapter);
	}
	
	public void testFirstPastFutureLink() {
		
		List<PoParentGroup> links = new ArrayList<PoParentGroup>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from = DateTools.toDate(2000, 3, 19, 10, 15);
		Date to = DateTools.toDate(2500, 3, 19, 8, 50);
		
		Historization result = helper.generateHistorizationObject(links, adapter, from, to);
		assertResult(from, to, result);
		
		assertLinking();
		
		assertProtocol(adapter);
	}
	
	public void testFirstNowFutureLink() {
		
		List<PoParentGroup> links = new ArrayList<PoParentGroup>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from = new Date();
		Date to = DateTools.toDate(2500, 3, 19, 8, 50);
		
		Historization result = helper.generateHistorizationObject(links, adapter, from, to);
		assertResult(from, to, result);
		
		assertLinking();
		
		assertProtocol(adapter);
	}
	
	public void testFirstFutureFutureLink() {
		
		List<PoParentGroup> links = new ArrayList<PoParentGroup>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from = DateTools.toDate(2500, 3, 19, 10, 15);
		Date to = DateTools.toDate(2600, 3, 19, 8, 50);
		
		Historization result = helper.generateHistorizationObject(links, adapter, from, to);
		assertResult(from, to, result);
		
		assertLinking();
		
		assertProtocol(adapter);
	}

//	----------------- SECOND LINK WITH SAME GOURPS AND DIFFERENT TIMES ----------------------
	
	public void testSecondNonInterferingLinkInPast() {
		
		List<Historization> links = new ArrayList<Historization>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from1 = DateTools.toDate(2000, 3, 19, 10, 15);
		Date to1 = DateTools.toDate(2001, 3, 19, 8, 50);
		Historization result = helper.generateHistorizationObject(links, adapter, from1, to1);
		
		links.add(result);
		Date from2 = DateTools.toDate(2002, 3, 19, 11, 30);
		Date to2 = DateTools.toDate(2003, 3, 19, 12, 45);
		result = helper.generateHistorizationObject(links, adapter, from2, to2);
		assertNotNull(result);
		assertResult(from2, to2, result);
		
		assertLinking2();
		
		List<MyProtocolItem> protocol = adapter.getProtocol();
		assertNotNull(protocol);
		assertEquals(4, protocol.size());
		// first link
		assertEquals("generateEmptyObject", protocol.get(0).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(0).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(0).getValidTo());
		assertEquals("saveObject", protocol.get(1).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(1).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(1).getValidTo());
		assertEquals("generateEmptyObject", protocol.get(2).getMethodName());
		assertEquals("saveObject", protocol.get(3).getMethodName());
		assertEquals(getFromDay(from2), protocol.get(3).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(3).getValidTo());
	}
	
	public void testSecondNonInterferingLinkInFuture() {
		
		List<Historization> links = new ArrayList<Historization>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from1 = DateTools.toDate(2500, 3, 19, 10, 15);
		Date to1 = DateTools.toDate(2600, 3, 19, 8, 50);
		Historization result = helper.generateHistorizationObject(links, adapter, from1, to1);

		links.add(result);
		Date from2 = DateTools.toDate(2700, 3, 19, 11, 30);
		Date to2 = DateTools.toDate(2800, 3, 19, 12, 45);
		result = helper.generateHistorizationObject(links, adapter, from2, to2);
		assertNotNull(result);
		assertResult(from2, to2, result);

		assertLinking2();
		
		List<MyProtocolItem> protocol = adapter.getProtocol();
		assertNotNull(protocol);
		assertEquals(4, protocol.size());
		// first link
		assertEquals("generateEmptyObject", protocol.get(0).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(0).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(0).getValidTo());
		assertEquals("saveObject", protocol.get(1).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(1).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(1).getValidTo());
		assertEquals("generateEmptyObject", protocol.get(2).getMethodName());
		assertEquals("saveObject", protocol.get(3).getMethodName());
		assertEquals(getFromDay(from2), protocol.get(3).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(3).getValidTo());
	}
	
	public void testSecondLinkInsideInPast() {
		
		List<Historization> links = new ArrayList<Historization>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from1 = DateTools.toDate(2000, 3, 19, 10, 15);
		Date to1 = DateTools.toDate(2005, 3, 19, 8, 50);
		Historization result = helper.generateHistorizationObject(links, adapter, from1, to1);
		
		links.add(result);
		Date from2 = DateTools.toDate(2002, 3, 19, 11, 30);
		Date to2 = DateTools.toDate(2003, 3, 19, 12, 45);
		result = helper.generateHistorizationObject(links, adapter, from2, to2);
		assertNull(result);
		
		assertLinking();
		
		List<MyProtocolItem> protocol = adapter.getProtocol();
		assertNotNull(protocol);
		assertEquals(4, protocol.size());
		// first link
		assertEquals("generateEmptyObject", protocol.get(0).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(1).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(1).getValidTo());
		assertEquals("saveObject", protocol.get(1).getMethodName());
		// second link
		assertEquals("generateEmptyObject", protocol.get(2).getMethodName());
		assertEquals(getFromDay(from2), protocol.get(2).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(2).getValidTo());
		assertEquals("isStructurallyEqual", protocol.get(3).getMethodName());
	}
	
	/** the inner link is ignored */
	public void testSecondLinkInsideInFuture() {
		
		List<Historization> links = new ArrayList<Historization>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from1 = DateTools.toDate(2100, 3, 19, 10, 15);
		Date to1 = DateTools.toDate(2400, 3, 19, 8, 50);
		Historization result = helper.generateHistorizationObject(links, adapter, from1, to1);
		
		links.add(result);
		Date from2 = DateTools.toDate(2200, 3, 19, 11, 30);
		Date to2 = DateTools.toDate(2300, 3, 19, 12, 45);
		result = helper.generateHistorizationObject(links, adapter, from2, to2);
		assertNull(result);
		
		assertLinking();
		
		List<MyProtocolItem> protocol = adapter.getProtocol();
		assertNotNull(protocol);
		assertEquals(4, protocol.size());
		// first link
		assertEquals("generateEmptyObject", protocol.get(0).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(0).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(0).getValidTo());
		assertEquals("saveObject", protocol.get(1).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(1).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(1).getValidTo());
		// second link
		assertEquals("generateEmptyObject", protocol.get(2).getMethodName());
		assertEquals(getFromDay(from2), protocol.get(2).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(2).getValidTo());
		assertEquals("isStructurallyEqual", protocol.get(3).getMethodName());
	}
	
	/** original link is replaced with the enclosing one */
	public void testSecondLinkOutsideInPast() { 
		
		List<Historization> links = new ArrayList<Historization>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from1 = DateTools.toDate(2002, 3, 19, 10, 15);
		Date to1 = DateTools.toDate(2003, 3, 19, 8, 50);
		Historization result = helper.generateHistorizationObject(links, adapter, from1, to1);
		
		links.add(result);
		Date from2 = DateTools.toDate(2001, 3, 19, 11, 30);
		Date to2 = DateTools.toDate(2004, 3, 19, 12, 45);
		result = helper.generateHistorizationObject(links, adapter, from2, to2);
		assertNotNull(result);
		assertResult(from2, to2, result);
		
		assertLinking();
		
		List<MyProtocolItem> protocol = adapter.getProtocol();
		assertNotNull(protocol);
		assertEquals(5, protocol.size());
		// first link
		assertEquals("generateEmptyObject", protocol.get(0).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(0).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(0).getValidTo());
		assertEquals("saveObject", protocol.get(1).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(1).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(1).getValidTo());
		// second link
		assertEquals("generateEmptyObject", protocol.get(2).getMethodName());
		assertEquals(getFromDay(from2), protocol.get(2).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(2).getValidTo());
		assertEquals("deleteObject", protocol.get(3).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(3).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(3).getValidTo());
		assertEquals("saveObject", protocol.get(4).getMethodName());
		assertEquals(getFromDay(from2), protocol.get(4).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(4).getValidTo());
	}
	
	
	/** original link is replaced with the enclosing one */
	public void testSecondLinkOutsideInFuture() { 
		
		List<Historization> links = new ArrayList<Historization>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from1 = DateTools.toDate(2200, 3, 19, 10, 15);
		Date to1 = DateTools.toDate(2300, 3, 19, 8, 50);
		Historization result = helper.generateHistorizationObject(links, adapter, from1, to1);
		
		links.add(result);
		Date from2 = DateTools.toDate(2100, 3, 19, 11, 30);
		Date to2 = DateTools.toDate(2400, 3, 19, 12, 45);
		result = helper.generateHistorizationObject(links, adapter, from2, to2);
		assertNotNull(result);
		assertResult(from2, to2, result);
		
		assertLinking();
		
		List<MyProtocolItem> protocol = adapter.getProtocol();
		assertNotNull(protocol);
		assertEquals(5, protocol.size());
		// first link
		assertEquals("generateEmptyObject", protocol.get(0).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(0).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(0).getValidTo());
		assertEquals("saveObject", protocol.get(1).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(1).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(1).getValidTo());
		// second link
		assertEquals("generateEmptyObject", protocol.get(2).getMethodName());
		assertEquals(getFromDay(from2), protocol.get(2).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(2).getValidTo());
		assertEquals("deleteObject", protocol.get(3).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(3).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(3).getValidTo());
		assertEquals("saveObject", protocol.get(4).getMethodName());
		assertEquals(getFromDay(from2), protocol.get(4).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(4).getValidTo());
	}
	
	/** moves the original link to start after the new link ends */
	public void testSecondLinkOverlappingLeftInPast() { 
		
		List<Historization> links = new ArrayList<Historization>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from1 = DateTools.toDate(2002, 3, 19, 10, 15);
		Date to1 = DateTools.toDate(2004, 3, 19, 8, 50);
		Historization result = helper.generateHistorizationObject(links, adapter, from1, to1);
		
		links.add(result);
		Date from2 = DateTools.toDate(2001, 3, 19, 11, 30);
		Date to2 = DateTools.toDate(2003, 3, 19, 12, 45);
		result = helper.generateHistorizationObject(links, adapter, from2, to2);
		assertNotNull(result);
		assertResult(from2, to2, result);
		
		assertLinking2();
		
		List<MyProtocolItem> protocol = adapter.getProtocol();
		assertNotNull(protocol);
		assertEquals(5, protocol.size());
		// first link
		assertEquals("generateEmptyObject", protocol.get(0).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(0).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(0).getValidTo());
		assertEquals("saveObject", protocol.get(1).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(1).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(1).getValidTo());
		// second link
		assertEquals("generateEmptyObject", protocol.get(2).getMethodName());
		assertEquals("saveObject", protocol.get(3).getMethodName());
		assertEquals(getFromDay(to2), protocol.get(3).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(3).getValidTo());
		assertEquals("saveObject", protocol.get(4).getMethodName());
		assertEquals(getFromDay(from2), protocol.get(4).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(4).getValidTo());
	}
	
	/** extends the validity to encompass both the first and the second period */
	public void testSecondLinkOverlappingRightInPast() { 
		
		List<Historization> links = new ArrayList<Historization>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from1 = DateTools.toDate(2002, 3, 19, 10, 15);
		Date to1 = DateTools.toDate(2004, 3, 19, 8, 50);
		Historization result = helper.generateHistorizationObject(links, adapter, from1, to1);
		
		links.add(result);
		Date from2 = DateTools.toDate(2003, 3, 19, 11, 30);
		Date to2 = DateTools.toDate(2005, 3, 19, 12, 45);
		result = helper.generateHistorizationObject(links, adapter, from2, to2);
		assertNull(result);
		
		assertLinking();
		
		List<MyProtocolItem> protocol = adapter.getProtocol();
		assertNotNull(protocol);
		assertEquals(5, protocol.size());
		// first link
		assertEquals("generateEmptyObject", protocol.get(0).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(0).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(0).getValidTo());
		assertEquals("saveObject", protocol.get(1).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(1).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(1).getValidTo());
		// second link
		assertEquals("generateEmptyObject", protocol.get(2).getMethodName());
		assertEquals("isStructurallyEqual", protocol.get(3).getMethodName());
		assertEquals("saveObject", protocol.get(4).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(4).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(4).getValidTo());
	}
	
	/** moves the original link to start after the new link ends */
	public void testSecondLinkOverlappingLeftInFuture() { 
		
		List<Historization> links = new ArrayList<Historization>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from1 = DateTools.toDate(2200, 3, 19, 10, 15);
		Date to1 = DateTools.toDate(2400, 3, 19, 8, 50);
		Historization result = helper.generateHistorizationObject(links, adapter, from1, to1);
		
		links.add(result);
		Date from2 = DateTools.toDate(2100, 3, 19, 11, 45);
		Date to2 = DateTools.toDate(2300, 3, 19, 12, 30);
		result = helper.generateHistorizationObject(links, adapter, from2, to2);
		assertNotNull(result);
		assertResult(from2, to2, result);
		
		assertLinking2();
		
		List<MyProtocolItem> protocol = adapter.getProtocol();
		assertNotNull(protocol);
		assertEquals(5, protocol.size());
		// first link
		assertEquals("generateEmptyObject", protocol.get(0).getMethodName());
		assertEquals("saveObject", protocol.get(1).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(1).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(1).getValidTo());
		// second link
		assertEquals("generateEmptyObject", protocol.get(2).getMethodName());
		assertEquals("saveObject", protocol.get(3).getMethodName());
		assertEquals(getFromDay(to2), protocol.get(3).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(3).getValidTo());
		assertEquals("saveObject", protocol.get(4).getMethodName());
		assertEquals(getFromDay(from2), protocol.get(4).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(4).getValidTo());
	}
	
	/** extends the validity to encompass both the first and the second period */
	public void testSecondLinkOverlappingRightInFuture() { 
		
		List<Historization> links = new ArrayList<Historization>();
		
		MyHistorizationAdapter adapter = new MyHistorizationAdapter(one, two);
		
		Date from1 = DateTools.toDate(2200, 3, 19, 10, 15);
		Date to1 = DateTools.toDate(2400, 3, 19, 8, 50);
		Historization result = helper.generateHistorizationObject(links, adapter, from1, to1);
		
		links.add(result);
		Date from2 = DateTools.toDate(2300, 3, 19, 11, 45);
		Date to2 = DateTools.toDate(2500, 3, 19, 12, 45);
		result = helper.generateHistorizationObject(links, adapter, from2, to2);
		assertNull(result);
		
		assertLinking();
		
		List<MyProtocolItem> protocol = adapter.getProtocol();
		assertNotNull(protocol);
		assertEquals(5, protocol.size());
		// first link
		assertEquals("generateEmptyObject", protocol.get(0).getMethodName());
		assertEquals("saveObject", protocol.get(1).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(1).getValidFrom());
		assertEquals(getToDay(to1), protocol.get(1).getValidTo());
		// second link
		assertEquals("generateEmptyObject", protocol.get(2).getMethodName());
		assertEquals("isStructurallyEqual", protocol.get(3).getMethodName());
		assertEquals("saveObject", protocol.get(4).getMethodName());
		assertEquals(getFromDay(from1), protocol.get(4).getValidFrom());
		assertEquals(getToDay(to2), protocol.get(4).getValidTo());
	}
	
//	------------------------ DATE METHODS ---------------------------
	
	public void testDatePredicates() {
		
		Date date = DateTools.toDate(2013, 6, 15, 12, 34);
		Date firstMoment = DateTools.dateOnly(DateUtils.addDays(date, 1));
		Date lastMoment = DateTools.lastMomentOfDay(date);
		assertFalse(helper.isFirstSecondOfDay(date));
		assertTrue(helper.isFirstSecondOfDay(firstMoment));
		
		assertFalse(helper.isLastSecondOfDay(date));
		assertTrue(helper.isLastSecondOfDay(lastMoment));
		
		assertTrue(helper.areDatesConsecutive(date, date));
		assertTrue(helper.areDatesConsecutive(lastMoment, firstMoment));
		assertFalse(helper.areDatesConsecutive(lastMoment, date));
		assertFalse(helper.areDatesConsecutive(date, firstMoment));
		
	}
	
	public void testDateGetters() {
		
		Date date = DateTools.toDate(2013, 6, 15, 12, 34);
		Date firstMoment = DateTools.dateOnly(DateUtils.addDays(date, 1));
		Date lastMoment = DateTools.lastMomentOfDay(date);
		
		assertEquals(date, helper.getDateFrom(date));
		assertEquals(firstMoment, helper.getDateFrom(lastMoment));
		
		assertEquals(date, helper.getDateTo(date));
		assertEquals(lastMoment, helper.getDateTo(firstMoment));
	}
	
	
//	------------------------ private methods -------------------------
	
	/** checks correctness of the historization reslut */
	private void assertResult(Date from, Date to, Historization result) {
		
		from = getFromDay(from);
		to = getToDay(to);
		
		assertNotNull(result);
		assertTrue(result instanceof PoParentGroup);
		PoParentGroup myResult = (PoParentGroup) result;
		assertNotNull(myResult.getUID());
		assertEquals(one, myResult.getParentGroup());
		assertEquals(two, myResult.getChildGroup());
		assertTrue(DateTools.datesAreEqual(from, myResult.getValidfrom(), DatePrecision.SECOND));
		assertTrue(DateTools.datesAreEqual(to, myResult.getValidto(), DatePrecision.SECOND));
	}

	/** checks that generateEmptyObject and saveObject were called */
	private void assertProtocol(MyHistorizationAdapter adapter) {
		
		List<MyProtocolItem> protocol = adapter.getProtocol();
		assertNotNull(protocol);
		assertEquals(2, protocol.size());
		assertEquals("generateEmptyObject", protocol.get(0).getMethodName());
		assertNull(protocol.get(0).getLinkUid());
		assertEquals("saveObject", protocol.get(1).getMethodName());
		assertNull(protocol.get(1).getLinkUid());
	}

	/** chechks that there is exactly one link between child and parent */
	private void assertLinking() {
		
		assertNotNull(one.getParentGroups());
		assertEquals(0, one.getParentGroups().size());
		assertNotNull(one.getChildGroups());
		assertEquals(1, one.getChildGroups().size());
		
		assertNotNull(two.getParentGroups());
		assertEquals(1, two.getParentGroups().size());
		assertNotNull(two.getChildGroups());
		assertEquals(0, two.getChildGroups().size());
		
		assertEquals(one.getChildGroups().iterator().next(), two.getParentGroups().iterator().next());
	}
	
	/** checks that there are two separate links between child and parent */ 
	private void assertLinking2() {
		
		assertNotNull(one.getParentGroups());
		assertEquals(0, one.getParentGroups().size());
		assertNotNull(one.getChildGroups());
		assertEquals(2, one.getChildGroups().size());
		
		assertNotNull(two.getParentGroups());
		assertEquals(2, two.getParentGroups().size());
		assertNotNull(two.getChildGroups());
		assertEquals(0, two.getChildGroups().size());
	}
	
	private Date getFromDay(Date date) {
		return HistorizationHelper.generateUsefulValidFromDay(date);
	}
	
	private Date getToDay(Date date) {
		return HistorizationHelper.generateUsefulValidToDay(date);
	}
}