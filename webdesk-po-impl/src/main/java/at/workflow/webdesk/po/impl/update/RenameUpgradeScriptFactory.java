package at.workflow.webdesk.po.impl.update;

import at.workflow.webdesk.po.update.RenameUpgradeScript;

/**
 * This is a factory which creates <code>RenameUpdateScript</code>
 * objects. Which class is chosen depends on the passed <code>value</code>.
 * Currently only "beanproperty" is supported, and answered by an instance
 * of RenameBeanPropertyUpdateScript. 
 */
public class RenameUpgradeScriptFactory {

	/**
	 * @return an instance of RenameBeanPropertyUpdateScript when passed parameter is "beanproperty", else null.
	 */
	public static RenameUpgradeScript create(String value) {
		if ("beanproperty".equals(value))
			return new RenameBeanPropertyUpgradeScript();
		
		// we could also return a mock-Object, in order to avoid exceptions,
		// but in this case it s useless, an exception is more useful.
		return null;
	}

}
