/*
 * Created on 20.11.2006
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po;

import java.util.List;

import at.workflow.webdesk.po.model.PoSystemMessage;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          webdesk3.1<br>
 * created at:       18.06.2007<br>
 * package:          at.workflow.webdesk.po<br>
 * compilation unit: PoSystemMessageService.java<br><br>
 * <p>
 * This service can handle <code>PoSystemMessage</code> objects. These messages 
 * are intended to give the user of the system a way to communicate with other users, 
 * as well as to give the administrator a way to inform the user about updates, changes, ...
 * </p>
 *
 * @see at.workflow.webdesk.po.PoActionService
 * @see at.workflow.webdesk.po.PoOrganisationService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.PoModuleService
 * @see at.workflow.webdesk.po.PoFileService
 * @see at.workflow.webdesk.po.PoLogService
 * @see at.workflow.webdesk.po.PoLanguageService
 * @see at.workflow.webdesk.po.PoBeanPropertyService
 * @see at.workflow.webdesk.po.model.PoSystemMessage
 *
 */
public interface PoSystemMessageService {
    
    /**
     * @param uid
     * @return a PoSystemMessage object if one with the given uid was found. Null otherwise.
     */
    public PoSystemMessage getSystemMessage(String uid);
    
    /**
     * This function returns a list of PoSystemMessage objects. Every Message
     * that was written between now and now - <code>daysToCheckInPast</code> will
     * be contained in the resulting list.
     * 
     * 
     * @param daysToCheckInPast
     * @return a list of PoSystemMessage objects.
     * 
     */
    public List<PoSystemMessage> findSystemMessages(int daysToCheckInPast);
    
    /**
     * Returns all <code>PoSystemMessage</code> objects. Also these
     * messages that are invalid or will become valid in the future.
     * 
     * @return a list of <code>PoSystemMessage</code> objects.
     */
    public List<PoSystemMessage> findSystemMessages();
    
    /**
     * Stores the given PoSystemMessage object inside the database.
     * 
     * @param systemMessage a PoSystemMessage object.
     */
    public void saveOrUpdateSystemMessage(PoSystemMessage systemMessage);
    
    /**
     * Deletes the PoSystemMessage object with the given <code>uid</code>.
     * 
     * @param uid the <code>uid</code> of the <code>PoSystemMessage</code>.
     */
    public void deleteSystemMessage(String uid);

    
    /**
     * 
     * Returns the amount of days a message lives.
     * @return an int value.
     */
    public int getDaysToLive(); 
}
