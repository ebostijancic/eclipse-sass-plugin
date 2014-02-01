package at.workflow.webdesk.po;

/**
 * All actions extending this interface must be in Spring application context,
 * and are then available under the "Extended functions" menu ("Erweiterte Funktionen")
 * in selectbox of "Module specific actions" section ("Modulspezifische Aktionen").
 * <p/>
 * This interface is used to define specific admin 
 * actions that will be automatically provided inside
 * the <code>po_refreshSettings.act</code> action. 
 * 
 * created at:       21.04.2008<br>
 * @author DI Harald Entner logged in as hentner
 */
public interface SpecificAdminAction {
	
	/**
	 * This function will be invoked when the specific action 
	 * is choosen to be executed. Put your code here.
	 */
	public void run();
	
	/**
	 * @return the <code>i18n</code> key for the title of this action.
	 */
	public String getI18nKey();
	
	/**
	 * @return the success message of this action.
	 */
	public String getSuccessMessage();
	
	/**
	 * @return the error message of this action. If null is returned, 
	 * and run has thrown an exception, then the <code>getMessage</code> method
	 * of the <code>Exception</code> provides the error message.
	 */
	public String getErrorMessage(); 

}
