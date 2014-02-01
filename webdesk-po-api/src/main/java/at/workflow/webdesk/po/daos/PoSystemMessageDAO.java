package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoSystemMessage;

/**
 * Created on 20.11.2006
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          webdesk3<br>
 * created at:       20.11.2006<br>
 * package:          at.workflow.webdesk.po.daos<br>
 * compilation unit: PoSystemMessageDAO.java<br><br>
 *
 * This Data Access Object (DAO) is used to read PoSystemMessage objects
 * from a database. 
 */
public interface PoSystemMessageDAO extends GenericDAO<PoSystemMessage> {
    
    /**
     * This function returns a list of PoSystemMessage objects. Every Message
     * that was written between now and now + <code>daysToLive</code> will
     * be contained in the resulting list.
     * 
     * 
     * @param daysToLive
     * @return a list of PoSystemMessage objects.
     * 
     */
    public List<PoSystemMessage> findSystemMessages(int daysToLive);
    
}
