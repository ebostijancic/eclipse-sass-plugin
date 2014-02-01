/**
 * 
 */
package at.workflow.webdesk.tools.api;

import junit.framework.TestCase;

/**
 * @author sdzuban
 *
 */
public class WTestI18nMessage extends TestCase {

	public void testToString() {
		
		I18nMessage message = new I18nMessage("i18nKey");
		System.out.println(message);
		assertTrue(message.toString().indexOf(I18nMessage.class.getSimpleName()) >= 0);
		assertTrue(message.toString().indexOf("i18nKey") >= 0);

		message = new I18nMessage("i18nKey", new String[] { "param" });
		System.out.println(message);
		assertTrue(message.toString().indexOf(I18nMessage.class.getSimpleName()) >= 0);
		assertTrue(message.toString().indexOf("i18nKey") >= 0);
		assertTrue(message.toString().indexOf("param") >= 0);
		
		message = new I18nMessage("i18nKey", new String[] { "param" }, new Boolean[] { Boolean.TRUE});
		System.out.println(message);
		assertTrue(message.toString().indexOf(I18nMessage.class.getSimpleName()) >= 0);
		assertTrue(message.toString().indexOf("i18nKey") >= 0);
		assertTrue(message.toString().indexOf("param") >= 0);
		
		message = new I18nMessage("i18nKey_{0}_{1}", new String[] { "param0", "param1" }, new Boolean[] { Boolean.TRUE, Boolean.TRUE});
		System.out.println(message);
		assertTrue(message.toString().indexOf(I18nMessage.class.getSimpleName()) >= 0);
		assertTrue(message.toString().indexOf("i18nKey_param0_param1") >= 0);
	}
}
