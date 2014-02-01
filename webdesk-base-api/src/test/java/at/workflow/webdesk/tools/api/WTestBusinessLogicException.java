/**
 * 
 */
package at.workflow.webdesk.tools.api;

import junit.framework.TestCase;

/**
 * @author sdzuban
 *
 */
public class WTestBusinessLogicException extends TestCase {
	
	public class TestObject implements PersistentObject {
		
		private String testField;

		@Override
		public String getUID() {
			return null; // not relevant here
		}

		public String getTestField() {
			return testField;
		}

		public void setTestField(String testField) {
			this.testField = testField;
		}

	}

	public void testToString() {
		
		String i18nKey = "i18nKey";
		BusinessLogicException exception = new BusinessLogicException(new I18nMessage(i18nKey));
		System.out.println(exception);
		assertTrue(exception.toString().indexOf(i18nKey) >= 0);
		
		String parameter = "testParameter";
		exception = new BusinessLogicException(new I18nMessage(i18nKey, new String[] { parameter }));
		System.out.println(exception);
		assertTrue(exception.toString().indexOf(i18nKey) >= 0);
		assertTrue(exception.toString().indexOf(parameter) >= 0);
		
		exception = new BusinessLogicException(new I18nMessage(i18nKey, new String[] { parameter }, new Boolean[] { Boolean.TRUE}));
		System.out.println(exception);
		assertTrue(exception.toString().indexOf(i18nKey) >= 0);
		assertTrue(exception.toString().indexOf(parameter) >= 0);
		
		String property = "testProperty";
		exception = new BusinessLogicException(new String[] { property }, i18nKey);
		System.out.println(exception);
		assertTrue(exception.toString().indexOf(i18nKey) >= 0);
		assertTrue(exception.toString().indexOf(property) >= 0);
		
		exception = new BusinessLogicException(new String[] { property }, new I18nMessage(i18nKey, new String[] { parameter }));
		System.out.println(exception);
		assertTrue(exception.toString().indexOf(i18nKey) >= 0);
		assertTrue(exception.toString().indexOf(parameter) >= 0);
		assertTrue(exception.toString().indexOf(property) >= 0);

		String propertyName = "testField";
		String propertyI18nKey = "test_object_testField";
		String classI18nKey = "test_object";
		exception = new BusinessLogicException(i18nKey, TestObject.class, propertyName);
		System.out.println(exception);
		assertTrue(exception.toString().indexOf(i18nKey) >= 0);
		assertTrue(exception.toString().indexOf(propertyName) >= 0);
		assertTrue(exception.toString().indexOf(propertyI18nKey) >= 0);
		assertTrue(exception.toString().indexOf(classI18nKey) >= 0);
		
		exception = new BusinessLogicException(new I18nMessage(i18nKey, new String[] { parameter }), TestObject.class, propertyName);
		System.out.println(exception);
		assertTrue(exception.toString().indexOf(i18nKey) >= 0);
		assertTrue(exception.toString().indexOf(parameter) >= 0);
		assertTrue(exception.toString().indexOf(propertyName) >= 0);
		assertTrue(exception.toString().indexOf(propertyI18nKey) >= 0);
		assertTrue(exception.toString().indexOf(classI18nKey) >= 0);
		
	}
}
