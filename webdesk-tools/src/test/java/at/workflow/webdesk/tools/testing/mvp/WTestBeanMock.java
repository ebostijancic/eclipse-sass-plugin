package at.workflow.webdesk.tools.testing.mvp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.mvp.BeanMock;

/**
 * Tests the BeanMock implementation, which was created for presentation-layer unit tests (MVP).
 * To be tested:
 * <ul>
 * 	<li>setter/getter symmetry</li>
 * 	<li>property-change listener support</li>
 * 	<li>setter/getter with locations, symmetry</li>
 * 	<li>adding and removing locations</li>
 * </ul>
 * 
 * @author fritzberger 15.10.2010
 */
public class WTestBeanMock extends TestCase
{
	// test helpers
	
	interface Bean
	{
		void addPropertyChangeListener(PropertyChangeListener listener);
		void removePropertyChangeListener(PropertyChangeListener listener);
	}
	
	interface PassiveView extends Bean
	{
		String getWidgetSet();
		void setWidgetSet(String widgetSetName);
		
		void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
		void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
		
		Date getCreationDate();
		void setCreationDate(Date creationDate);
		
		SubView getHead();	// read-only property (without setter)
		
		SubView getBody();
		void setBody(SubView body);
	}
	
	interface SubView extends Bean
	{
		String getDescription();
		void setDescription(String description);
	}

	
	/** Tests if mocks supports connected setters and getters, and listener service. */
	public void testSetterGetterAndListeners()	{
		final PassiveView mainView = BeanMock.mock(PassiveView.class);
		assertNotNull(mainView);
		
		// test that primitive properties are not mocked recursively
		assertNull(mainView.getWidgetSet());
		assertNull(mainView.getCreationDate());
		
		// test that non-primitive properties are mocked recursively
		assertNotNull(mainView.getHead());
		assertNotNull(mainView.getBody());
		
		// test if setter and getters are connected
		
		mainView.setWidgetSet("SomeWidgetSet");
		assertEquals("SomeWidgetSet", mainView.getWidgetSet());
		mainView.setWidgetSet("SomeOtherWidgetSet");
		assertEquals("SomeOtherWidgetSet", mainView.getWidgetSet());
		
		final Date creationDate = DateTools.toDate(2012, 1, 1);
		mainView.setCreationDate(creationDate);
		assertEquals(creationDate, mainView.getCreationDate());
		final Date creationDate2 = DateTools.toDate(2001, 12, 31);
		mainView.setCreationDate(creationDate2);
		assertEquals(creationDate2, mainView.getCreationDate());
		
		assertNull(mainView.getBody().getDescription());
		mainView.getBody().setDescription("Description 1");
		assertNotNull(mainView.getBody().getDescription());
		assertEquals("Description 1", mainView.getBody().getDescription());
		mainView.getBody().setDescription("Description 2");
		assertEquals("Description 2", mainView.getBody().getDescription());

		// test if mocks are created recursively, and, once created, are always the same
		
		final SubView head = mainView.getHead();
		assertNotNull(head);
		assertFalse(head.equals(mainView));
		
		final SubView head2 = mainView.getHead();
		assertTrue(head.equals(head2));
		
		
		final SubView body = mainView.getBody();
		assertNotNull(body);
		assertFalse(body.equals(mainView));
		assertFalse(body.equals(head));
		
		final SubView body2 = mainView.getBody();
		assertEquals(body, body2);
		
		// test if setter overrides remembering sub-mocks
		
		final SubView mockedBody = BeanMock.mock(SubView.class);
		mainView.setBody(mockedBody);
		assertEquals(mockedBody, mainView.getBody());
		
		class TestPropertyChangeListener implements PropertyChangeListener
		{
			List<Object> oldValues = new ArrayList<Object>();
			List<Object> newValues = new ArrayList<Object>();
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				oldValues.add(event.getOldValue());
				newValues.add(event.getNewValue());
			}
		};
		
		// test listening to one property only
		
		final TestPropertyChangeListener onePropertyListener = new TestPropertyChangeListener();
		
		mainView.addPropertyChangeListener("creationDate", onePropertyListener);	// listens to creationDate changes
		mainView.setCreationDate(creationDate);
		mainView.setCreationDate(creationDate);	// no change, no event
		mainView.setCreationDate(creationDate2);
		
		assertEquals(2, onePropertyListener.oldValues.size());
		assertEquals(creationDate, onePropertyListener.newValues.get(0));
		assertEquals(creationDate2, onePropertyListener.newValues.get(1));
		assertEquals(creationDate, onePropertyListener.oldValues.get(1));
		
		// test listening to all properties
		
		final TestPropertyChangeListener allPropertiesListener = new TestPropertyChangeListener();
		mainView.addPropertyChangeListener(allPropertiesListener);
		
		mainView.setCreationDate(creationDate);
		mainView.setWidgetSet("SomeCompletelyOtherWidgetSet");
		mainView.setCreationDate(creationDate2);
		
		assertEquals(3, allPropertiesListener.oldValues.size());	// listens to all changes
		assertEquals(4, onePropertyListener.oldValues.size());	// listens to creationDate changes
		
		assertEquals(creationDate, allPropertiesListener.newValues.get(0));
		assertEquals("SomeCompletelyOtherWidgetSet", allPropertiesListener.newValues.get(1));
		assertEquals(creationDate2, allPropertiesListener.newValues.get(2));
		
		assertEquals(creationDate, allPropertiesListener.oldValues.get(2));
	}
	
	
	
	
	// location test helpers

	interface LocatingView extends Bean
	{
		Integer getYearCount(Object location);
		void setYearCount(Object location, Integer yearCount);
		
		Date getCreationDate(Object location);
		void setCreationDate(Object location, Date creationDate);
		
		Number [] getLocations();
		void addLocation(Number location);
		void removeLocation(Number location);
	}
	
	
	/** Test location parameters for getters and setters. */
	public void testLocationArguments()	{
		final LocatingView locatingView = BeanMock.mock(LocatingView.class);
		
		final Number firstLocation = new Long(1);
		final Number secondLocation = new Long(2);
		
		assertNull(locatingView.getYearCount(firstLocation));	// nothing added yet
		assertNull(locatingView.getYearCount(secondLocation));	// nothing added yet
		
		// test size property
		final Integer twoHundredYears = new Integer(200);
		locatingView.setYearCount(firstLocation, twoHundredYears);
		assertNotNull(locatingView.getYearCount(firstLocation));
		assertEquals(twoHundredYears, locatingView.getYearCount(firstLocation));
		assertNull(locatingView.getYearCount(secondLocation));	// nothing added yet
		
		final Integer fiveHundredYears = new Integer(500);
		locatingView.setYearCount(secondLocation, fiveHundredYears);
		assertEquals(twoHundredYears, locatingView.getYearCount(firstLocation));
		assertNotNull(locatingView.getYearCount(secondLocation));
		assertEquals(fiveHundredYears, locatingView.getYearCount(secondLocation));
		
		// retrieve locations
		Number [] locations = locatingView.getLocations();
		assertNotNull(locations);
		assertEquals(2, locations.length);
		assertEquals(firstLocation, locations[0]);
		assertEquals(secondLocation, locations[1]);
		
		// test creationDate property
		final Date creationDate = DateTools.toDate(2008, 5, 31);
		locatingView.setCreationDate(secondLocation, creationDate);
		assertEquals(creationDate, locatingView.getCreationDate(secondLocation));
		assertNull(locatingView.getCreationDate(firstLocation));	// nothing added yet
		
		assertEquals(twoHundredYears, locatingView.getYearCount(firstLocation));
		assertEquals(fiveHundredYears, locatingView.getYearCount(secondLocation));
		
		// test setting null
		locatingView.setCreationDate(secondLocation, null);
		assertNull(locatingView.getCreationDate(secondLocation));
		locatingView.setYearCount(firstLocation, null);
		assertNull(locatingView.getYearCount(firstLocation));	// empty now
		
		assertNull(locatingView.getCreationDate(firstLocation));	// still empty
		assertEquals(fiveHundredYears, locatingView.getYearCount(secondLocation));	// still the value
		
		// remove locations
		locatingView.removeLocation(firstLocation);
		assertEquals(1, locatingView.getLocations().length);
		locatingView.removeLocation(secondLocation);
		assertEquals(0, locatingView.getLocations().length);
		assertNull(locatingView.getYearCount(secondLocation));	// must have disappeared
		assertNull(locatingView.getYearCount(firstLocation));
		
		// add locations
		locatingView.addLocation(firstLocation);
		assertEquals(1, locatingView.getLocations().length);
		locatingView.setYearCount(firstLocation, fiveHundredYears);
		assertEquals(fiveHundredYears, locatingView.getYearCount(firstLocation));
		assertNull(locatingView.getYearCount(secondLocation));
		
		locatingView.addLocation(secondLocation);
		assertEquals(2, locatingView.getLocations().length);
		locatingView.setYearCount(secondLocation, twoHundredYears);
		assertEquals(twoHundredYears, locatingView.getYearCount(secondLocation));
		assertEquals(fiveHundredYears, locatingView.getYearCount(firstLocation));
	}

}
