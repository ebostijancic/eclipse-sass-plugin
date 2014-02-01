package org.ebostijancic.simple.time.tracking.model;

import java.util.List;

import junit.framework.TestCase;

import org.ebostijancic.simple.time.tracking.App;
import org.ebostijancic.simple.time.tracking.services.UserService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * @author emil
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
public class UserTest extends TestCase {

	@Autowired
	private UserService userService;

	@Test
	public void testTrue() {
		assertTrue(true);
	}

	@After
	public void cleanup() {
		userService.deleteUsers();
	}

	@Test
	public void testSaveUser() {
		User user1 = createDefaultUser();

		user1 = userService.saveUser(user1);
		List<User> allUsers = userService.findAllUsers();
		assertTrue(allUsers.contains(user1));
	}

	private User createDefaultUser() {
		User user1 = new User();
		user1.setUserName("emil");
		user1.setPassword("Bostijancic");
		return user1;
	}

	@Test
	public void testUpdateUser() {
		User user1 = createDefaultUser();

		user1 = userService.saveUser(user1);

		assertTrue(user1.getUserName().equals("emil"));

		user1.setUserName("Vanessa");
		user1 = userService.saveUser(user1);

		assertTrue(userService.findAllUsers().size() == 1);

		user1 = userService.findAllUsers().get(0);
		assertTrue(user1.getUserName().equals("Vanessa"));

		assertTrue(userService.findAllUsers().size() == 1);
	}

	@Test
	public void testDeleteUser() {
		User user1 = createDefaultUser();

		user1 = userService.saveUser(user1);

		assertTrue(userService.findAllUsers().size() == 1);

		assertTrue(userService.deleteUser(user1));

		assertTrue(userService.findAllUsers().isEmpty());
	}

}
