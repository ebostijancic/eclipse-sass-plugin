package org.ebostijancic.simple.time.tracking.model;

import org.ebostijancic.simple.time.tracking.services.TimeEntryService;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TimeEntryTest extends AbstractTestCase {

	@Autowired
	private TimeEntryService timeEntryService;

	@Test
	public void testTimeEntrySave() {
		User user = createDefaultUser();

		TimeEntry timeEntry1 = new TimeEntry();
		timeEntry1.setBookingType(BookingType.COMING);

		timeEntry1.setStartOfEntry(new LocalDateTime("2013-12-16T10:30:00"));
		timeEntry1.setEndOfEntry(new LocalDateTime("2013-12-16T12:45:12"));
		timeEntry1.setUser(user);

		user.addTimeEntry(timeEntry1);
		userService.saveUser(user);

		assertFalse(userService.findAllUsers().isEmpty());

		assertFalse(timeEntryService.findAllTimeEntries().isEmpty());
		assertFalse(timeEntryService.findAllTimeEntriesForUser(user).isEmpty());
	}
}
