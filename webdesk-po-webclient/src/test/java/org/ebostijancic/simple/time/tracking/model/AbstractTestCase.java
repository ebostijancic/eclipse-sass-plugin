package org.ebostijancic.simple.time.tracking.model;

import junit.framework.TestCase;

import org.ebostijancic.simple.time.tracking.AppTestConfiguration;
import org.ebostijancic.simple.time.tracking.services.UserService;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AppTestConfiguration.class)
public abstract class AbstractTestCase extends TestCase {

	@Autowired
	protected UserService userService;

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@After
	public void cleanup() {

	}

	protected User createDefaultUser() {
		User user1 = new User();
		user1.setUserName("emil");
		user1.setPassword("Bostijancic");
		return user1;
	}
}
