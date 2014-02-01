package at.workflow.webdesk.tools;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author sdzuban 25.01.2013
 */
public class WTestSimpleVelocityUtil {

	public static class Name {
		private String first;
		private String second;
		public Name(String first, String second) {
			this.first = first;
			this.second = second;
		}
		public String getFirst() {
			return first;
		}
		public String getSecond() {
			return second;
		}
	}
	
	@Test
	public void testSimple() {
		
		VelocityUtil velocityUtil = new VelocityUtil(this.getClass().getName());
		Map<String, Object> context = new HashMap<String, Object>();

		String result = velocityUtil.velocitySubstitution("", context);
		assertNotNull(result);
		assertEquals("", result);
		
		result = velocityUtil.velocitySubstitution("$name", context);
		assertNotNull(result);
		assertEquals("$name", result);
		
		context.put("name", "nm");
		result = velocityUtil.velocitySubstitution("$name", context);
		assertNotNull(result);
		assertEquals("nm", result);
	}
		
	@Test
	public void testComplex() {
			
		Name name = new Name("one", "two");
		
		VelocityUtil velocityUtil = new VelocityUtil(this.getClass().getName());
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("name", name);
		
		String result = velocityUtil.velocitySubstitution("$name.first", context);
		assertNotNull(result);
		assertEquals("one", result);
		
		result = velocityUtil.velocitySubstitution("$name.second", context);
		assertNotNull(result);
		assertEquals("two", result);
		
		result = velocityUtil.velocitySubstitution("$name.first ($name.second)", context);
		assertNotNull(result);
		assertEquals("one (two)", result);
	}

}
