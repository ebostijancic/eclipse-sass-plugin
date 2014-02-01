package at.workflow.webdesk.po;

/**
 * This is thrown from PoModuleUpdateService when a Webdesk upgrade fails.
 * 
 * @author fritzberger 30.11.2012
 */
public class UpgradeFailedException extends Exception {

	public UpgradeFailedException(String message, Exception cause) {
		super(message, cause);
	}
}
