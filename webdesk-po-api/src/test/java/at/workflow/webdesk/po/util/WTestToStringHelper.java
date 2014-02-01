package at.workflow.webdesk.po.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Test;

import at.workflow.webdesk.po.model.PoHistorization;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * @author sdzuban 03.04.2013
 */
public class WTestToStringHelper {

	@Test
	public void test() {

		ToStringHelperTest testObject = new ToStringHelperTest();
		testObject.setValidto(null);

		String result = ToStringHelper.toString(testObject);

		System.out.println(result);

		assertNotNull(result);
		int idx = result.indexOf('[');
		assertTrue(idx >= 0);
		result = result.substring(idx + 1, result.length() - 1);
		String [] fragments = result.split(", ");
		assertEquals("shortName=TheShortName", fragments[0]);
		assertContains("b=true", fragments);
		assertContains("d=15.06.2100 00:00:00.0", fragments);
		assertContains("uID=testUID", fragments);
		assertContains("validfrom=", fragments, true);
		assertContains("validto=01.01.3000 00:00:00.0", fragments);
	}

	@Test
	public void testStyle() {

		ToStringHelperTest testObject = new ToStringHelperTest();

		System.out.println();
		System.out.println("DEFAULT_STYLE");
		System.out.println(ToStringHelper.toString(testObject, ToStringStyle.DEFAULT_STYLE));
		System.out.println();
		System.out.println("MULTI_LINE_STYLE");
		System.out.println(ToStringHelper.toString(testObject, ToStringStyle.MULTI_LINE_STYLE));
		System.out.println();
		System.out.println("NO_FIELD_NAMES_STYLE");
		System.out.println(ToStringHelper.toString(testObject, ToStringStyle.NO_FIELD_NAMES_STYLE));
		System.out.println();
		System.out.println("SHORT_PREFIX_STYLE");
		System.out.println(ToStringHelper.toString(testObject, ToStringStyle.SHORT_PREFIX_STYLE));
		System.out.println();
		System.out.println("SIMPLE_STYLE");
		System.out.println(ToStringHelper.toString(testObject, ToStringStyle.SIMPLE_STYLE));
		System.out.println();
		
		// TODO: no asserts here?
	}
	
	private void assertContains(String fragment, String[] fragments) {
		assertContains(fragment, fragments, false);
	}
	
	private void assertContains(String fragment, String[] fragments, boolean startsWith) {
		for (String f : fragments)	{
			final boolean ok = startsWith ? f.startsWith(fragment) : f.equals(fragment);
			if (ok)
				return;
		}
		
		fail("Expected toString fragment >"+fragment+"< not found in "+Arrays.asList(fragments));
	}

	
	
	public class EmptyTestClass
	{
	}

	public class ClassWithName
	{
		private String name = "test";

		public String getName() {
			return name;
		}
	}

	public class ClassWithUID
	{
		private String uid = "testUID";

		public String getUID() {
			return uid;
		}
	}

	public class ClassWithOtherField
	{
		private String field = "testField";

		public String getField() {
			return field;
		}
	}

	public class ToStringHelperTest extends PoHistorization
	{
		// standard properties
		private boolean b = true;
		private Date d = DateTools.toDate(2100, 6, 15);

		// referential properties
		private EmptyTestClass empty = new EmptyTestClass();
		private ClassWithName nullName;
		private ClassWithName name = new ClassWithName();
		private ClassWithOtherField field = new ClassWithOtherField();

		// collections
		private Collection<?> nullCollection;
		private Collection<Integer> notNullCollection = new ArrayList<Integer>();
		private Collection<String> filledCollection = Arrays.<String> asList("a", "b", "c", "d", "e");

		public boolean isB() {
			return b;
		}

		public Date getD() {
			return d;
		}

		public EmptyTestClass getEmpty() {
			return empty;
		}

		public ClassWithName getNullName() {
			return nullName;
		}

		public ClassWithName getName() {
			return name;
		}

		public ClassWithOtherField getField() {
			return field;
		}

		public Collection<?> getEmptyCollection() {
			return nullCollection;
		}

		public Collection<Integer> getNotNullCollection() {
			return notNullCollection;
		}

		public Collection<String> getFilledCollection() {
			return filledCollection;
		}

		/** {@inheritDoc} */
		@Override
		public String getUID() {
			return "testUID";
		}

		/** {@inheritDoc} */
		@Override
		public void setUID(String uid) {
		}

		public String getShortName() {
			return "TheShortName";
		}
	}

}
