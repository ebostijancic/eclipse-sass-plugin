package org.ebostijancic.simple.time.tracking.services;

import java.util.Iterator;
import java.util.List;

import org.ebostijancic.simple.time.tracking.model.TimeEntry;
import org.ebostijancic.simple.time.tracking.model.User;
import org.ebostijancic.simple.time.tracking.repositories.TimeEntryRepository;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class TimeEntryServiceImpl implements TimeEntryService {

	@Autowired
	private TimeEntryRepository timeEntryRepository;

	@Override
	public List<TimeEntry> findAllTimeEntries() {
		return Lists.newArrayList(timeEntryRepository.findAll());
	}

	@Override
	public List<TimeEntry> findAllTimeEntriesForUser(User user) {
		return timeEntryRepository.findByUser(user);
	}

	@Override
	public List<TimeEntry> findAllTimeEntriesForUserOnDay(User user,
			LocalDate day) {
		Iterator<TimeEntry> timeEntryIterator = timeEntryRepository.findByUser(
				user).iterator();
		while (timeEntryIterator.hasNext()) {
			final TimeEntry entry = timeEntryIterator.next();
			final LocalDateTime startOfEntry = entry.getStartOfEntry();

			if (startOfEntry.dayOfYear().equals(day.dayOfYear()) == false
					|| startOfEntry.dayOfMonth().equals(day.dayOfMonth()) == false
					|| startOfEntry.year().equals(day.year())) {
				timeEntryIterator.remove();
			}
		}
		return Lists.newArrayList(timeEntryIterator);
	}

	@Override
	public TimeEntry saveTimeEntry(TimeEntry timeEntry) {
		return timeEntryRepository.save(timeEntry);
	}

	@Override
	public boolean deleteTimeEntry(TimeEntry timeEntry) {
		timeEntryRepository.delete(timeEntry);
		return true;
	}

}
