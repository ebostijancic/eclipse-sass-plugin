/*
 * Copyright 2013 Emil Bostijancic.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ebostijancic.simple.time.tracking.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.joda.time.LocalDateTime;

/**
 * 
 * @author emil
 */
@Entity
public class TimeEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	private User user;

	private LocalDateTime startOfEntry;

	private LocalDateTime endOfEntry;

	private String comment;

	private BookingType bookingType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getStartOfEntry() {
		return startOfEntry;
	}

	public void setStartOfEntry(LocalDateTime startOfEntry) {
		this.startOfEntry = startOfEntry;
	}

	public LocalDateTime getEndOfEntry() {
		return endOfEntry;
	}

	public void setEndOfEntry(LocalDateTime endOfEntry) {
		this.endOfEntry = endOfEntry;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public BookingType getBookingType() {
		return bookingType;
	}

	public void setBookingType(BookingType bookingType) {
		this.bookingType = bookingType;
	}

}
