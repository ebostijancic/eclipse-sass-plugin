package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

/**
 * Use this Base Class for all tests within PO.
 * 
 * Provide your custom setup by implementing method <code>onSetUpAfterDataGeneration()</code>.
 * All Data written to the database either with your custom setup script or within the 
 * actual testcase will be ROLLED BACK after the testcase has finished.
 * So no INTERFERENCE between testcases should happen.
 * 
 * The only data remaining in the database is the data written by the Datagenerators created
 * by this abstract testclass via getDataGenerators()
 * 
 * @author ggruber
 */
public abstract class AbstractPoTestCase extends AbstractTransactionalSpringHsqlDbTestCase {
	
	/**
	 * use this to provide custom Datagenerators
	 * @return
	 */
	@Override
	protected final DataGenerator[] getDataGenerators() {
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/TestData.xml", false) };
	}

}
