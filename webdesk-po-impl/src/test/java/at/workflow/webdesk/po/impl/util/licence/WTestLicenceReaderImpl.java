package at.workflow.webdesk.po.impl.util.licence;

import at.workflow.webdesk.po.impl.licence.LicenceReaderImpl;
import junit.framework.TestCase;

/**
 * Tests the LicenceReaderImpl constructor that loads the webdesk Java keystore.
 * 
 * @author fritzberger 22.10.2010
 */
public class WTestLicenceReaderImpl extends TestCase {

	@SuppressWarnings("unused")
	public void testLicenceReaderImpl()	{
		// test the constructor that loads the keystore
		new LicenceReaderImpl();
	}
}
