package at.workflow.tools.test;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Example for ordered unit test method calls.
 * 
 * @see http://stackoverflow.com/questions/3089151/specifying-an-order-to-junit-4-tests-at-the-method-level-not-class-level
 * @author fritzberger 11.09.2012
 */
@RunWith(OrderedRunner.class)
public class OrderedExampleTest extends TestCase
{
	@Test
	@Order(order = 3)
	public void testDeleteArticle() {
	}

	@Test
	@Order(order = 2)
	public void testUpdateArticle() {
	}

	@Test
	@Order(order = 1)
	public void testInsertArticle() {
	}

}
