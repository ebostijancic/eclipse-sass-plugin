/*
 * Created on 20.11.2006
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.impl;

import java.util.List;

import at.workflow.webdesk.po.PoSystemMessageService;
import at.workflow.webdesk.po.daos.PoSystemMessageDAO;
import at.workflow.webdesk.po.model.PoSystemMessage;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          webdesk3<br>
 * created at:       20.11.2006<br>
 * package:          at.workflow.webdesk.po.impl<br>
 * compilation unit: PoSystemMessageServiceImpl.java<br><br>
 * 
 * Implementation of the PoSystemMessageService interface.o
 *
 */
public class PoSystemMessageServiceImpl implements PoSystemMessageService {

    private PoSystemMessageDAO systemMessageDAO;
    
    /**
     * Determines how many days a system message is valid.
     */
    private int daysToLive;
    
    @Override
	public int getDaysToLive() {
		return daysToLive;
	}

	public void setDaysToLive(int daysToLive) {
		this.daysToLive = daysToLive;
	}

	public void setSystemMessageDAO(PoSystemMessageDAO systemMessageDAO) {
        this.systemMessageDAO = systemMessageDAO;
    }

    @Override
	public PoSystemMessage getSystemMessage(String uid) {
        return systemMessageDAO.get(uid);
    }

    @Override
	public List<PoSystemMessage> findSystemMessages(int daysToCheckInPast) {
        return systemMessageDAO.findSystemMessages(daysToCheckInPast);
    }
    
    @Override
	public List<PoSystemMessage> findSystemMessages() {
        return systemMessageDAO.loadAll();
    }

    @Override
	public void saveOrUpdateSystemMessage(PoSystemMessage systemMessage) {
        systemMessageDAO.save(systemMessage);
    }
    
    @Override
	public void deleteSystemMessage(String uid) {
    	systemMessageDAO.delete(this.getSystemMessage(uid));
    }

}
