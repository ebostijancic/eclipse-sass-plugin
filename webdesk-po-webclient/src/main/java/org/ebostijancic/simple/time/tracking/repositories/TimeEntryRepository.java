package org.ebostijancic.simple.time.tracking.repositories;

import java.util.List;

import org.ebostijancic.simple.time.tracking.model.TimeEntry;
import org.ebostijancic.simple.time.tracking.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeEntryRepository extends CrudRepository<TimeEntry, Long> {

	List<TimeEntry> findByUser(User user);

}
