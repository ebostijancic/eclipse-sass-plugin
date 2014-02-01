package at.workflow.webdesk.po;

import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoHelpMessage;
import at.workflow.webdesk.po.model.PoLanguage;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          3.1DEV<br>
 * created at:       29.08.2007<br>
 * package:          at.workflow.webdesk.po<br>
 * compilation unit: PoHelpMessageService.java<br><br>
 * 
 * 
 * <p>This interface should be used, when something has to be done with 
 * <code>PoHelpMessage</code> objects.
 * </p> 
 * 
 *
 */
public interface PoHelpMessageService {
	/**
	 * <p>Persists the given <code>PoHelpMessage</code></p>
	 * 
	 * @param hm <code>PoHelpMessage</code>
	 */
	public void saveHelpMessage(PoHelpMessage hm);
	
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
	 * @param action
	 * @return a <code>Map</code> M(x,y), where the <code>key x</code> is the 
	 * <code>UID</code> of the assigned <code>PoLanguage</code>, and the <code>value y</code> is 
	 * the corresponding <code>PoHelpMessage</code>.
	 */
	public Map<String, PoHelpMessage> findHelpMessagesOfActionAsMap(PoAction action);
	
	/**
	 * @param Action
	 * @paramn Languagecode
	 * @return true if the given request has a helpmessage, false otherwise.
	 */
	public boolean hasHelpMessage(PoAction action, String locale);
	
	/**
	 * @param action a <code>PoAction</code>
	 * @param language a <code>PoLanguage</code>
	 * @return a <code>PoHelpMessage</code> with the given <code>action</code> and 
	 * <code>language</code>, or null if none was found.
	 * 
	 */
	public PoHelpMessage getHelpMessage(PoAction action, PoLanguage language); 
	
	
	/**
	 * <p>This function registers all HelpMessages that are contained in the given 
	 * <code>ress</code> bundle. 
	 * </p>
	 * 
	 * @param ress
	 * @param folderOfPackage
	 */
	public void registerHelpMessages(Resource[] ress, String folderOfPackage);
}
