package org.ebostijancic.simple.time.tracking.services;

import java.util.List;

import org.ebostijancic.simple.time.tracking.model.TimeEntry;
import org.ebostijancic.simple.time.tracking.model.User;
import org.joda.time.LocalDate;

public interface TimeEntryService {

	public List<TimeEntry> findAllTimeEntries();

	public List<TimeEntry> findAllTimeEntriesForUser(User user);

	public List<TimeEntry> findAllTimeEntriesForUserOnDay(User user,
			LocalDate day);

	public TimeEntry saveTimeEntry(TimeEntry timeEntry);

	public boolean deleteTimeEntry(TimeEntry timeEntry);
}
