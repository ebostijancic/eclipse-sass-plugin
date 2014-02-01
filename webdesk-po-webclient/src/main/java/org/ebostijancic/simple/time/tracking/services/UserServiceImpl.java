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
package org.ebostijancic.simple.time.tracking.services;

import java.util.List;

import org.ebostijancic.simple.time.tracking.model.User;
import org.ebostijancic.simple.time.tracking.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

/**
 * 
 * @author emil
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public List<User> findAllUsers() {
		return Lists.newArrayList(userRepository.findAll());
	}

	@Override
	public User saveUser(User user) {
		return userRepository.save(user);
	}

	@Override
	public boolean deleteUser(User user) {
		long id = user.getId();
		userRepository.delete(user);
		return !(userRepository.findOne(id) != null);
	}

	@Override
	public void deleteUsers() {
		userRepository.deleteAll();
	}
}
