package at.workflow.webdesk.tools.errors;

/**
 * This is minimalistic interface providing only methods for
 * collecting of error log messages.
 * 
 * Reading and clearing is not provided to prevent any manipulation 
 * by logged class.
 * 
 * @author sdzuban 29.06.2013
 */
public interface ErrorLog {

	/** @param message adds simple textual message into log */
	void addErrorMessage(String message);
	
	/** 
	 * @param errorMessage message about the observed error
	 */
	void addErrorMessage(ErrorMessage errorMessage);

}
