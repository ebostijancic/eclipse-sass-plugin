package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoHelpMessage;
import at.workflow.webdesk.po.model.PoLanguage;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          3.1DEV<br>
 * created at:       29.08.2007<br>
 * package:          at.workflow.webdesk.po.daos<br>
 * compilation unit: PoHelpMessageDAO.java<br><br>
 * 
 * <p>
 * 	This interface can be used to handle <code>PoHelpMessage</code> objects, which represents 
 *  internationalised help messages.
 * </p>
 *
 */
public interface PoHelpMessageDAO extends GenericDAO<PoHelpMessage> {
	
	/**
	 * @param lang the <code>PoLanguage</code> of the message. 
	 *
	 * @return a <code>List</code> of <code>PoHelpMessages</code>
	 * 	 
	 */
	public List<PoHelpMessage> findHelpMessages(PoLanguage lang);
	
	/**
	 * <p>
	 * 	Returns a <code>List</code> of <code>PoHelpMessages</code> that 
	 *  are assigned to the given <code>action</code>.
	 * 
	 * </p>
	 *
	 *
	 * @param action 
	 * @return  a <code>List</code> of <code>PoHelpMessages</code>
	 */
	public List<PoHelpMessage> findHelpMessagesOfAction(PoAction action);
	
	
	/**
	 * @param action a <code>PoAction</code>
	 * @param language a <code>PoLanguage</code>
	 * @return a <code>PoHelpMessage</code> with the given <code>action</code> and 
	 * <code>language</code>, or null if none was found.
	 * 
	 */
	public PoHelpMessage getHelpMessage(PoAction action, PoLanguage language); 

}
