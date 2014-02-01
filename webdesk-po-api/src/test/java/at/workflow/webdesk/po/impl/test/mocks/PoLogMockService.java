package at.workflow.webdesk.po.impl.test.mocks;


import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;

import at.workflow.webdesk.po.PoLogService;
import at.workflow.webdesk.po.model.PoLog;
import at.workflow.webdesk.po.model.PoLogDetail;
import at.workflow.webdesk.po.model.PoLogRequestParameter;

public class PoLogMockService implements PoLogService {

    public PoLog getLog(String uid) {
        // TODO Auto-generated method stub
        return null;
    }

    public void saveLog(PoLog log) {
        // TODO Auto-generated method stub

    }

    public void deleteLog(PoLog log) {
        // TODO Auto-generated method stub

    }

    public void deleteAllLogs() {
        // TODO Auto-generated method stub

    }

    public void refreshLogSettings(String filename) {
        // TODO Auto-generated method stub

    }

    public Set getUsersToLog() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set getActionsToLog() {
        // TODO Auto-generated method stub
        return null;
    }


    public boolean isLogAllActions() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setLogAllActions(boolean logActions) {
        // TODO Auto-generated method stub

    }

    public boolean isLogAllUsers() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setLogAllUsers(boolean logAllUsers) {
        // TODO Auto-generated method stub

    }

    public Document removeLog4JNamespaces(Document log4JConfig) {
        // TODO Auto-generated method stub
        return null;
    }

    public Document addLog4JNamespaces(Document log4JConfig) {
        // TODO Auto-generated method stub
        return null;
    }

	public void deleteOlderXDays(int days) {
		// TODO Auto-generated method stub
		
	}

	public void setUsersToLog(Set<String> usersToLog) {
		// TODO Auto-generated method stub
		
	}

	public void setActionsToLog(Set<String> actionsToLog) {
		// TODO Auto-generated method stub
		
	}

	public List<PoLog> findLogsInSameContinuation(PoLog log) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoLogDetail> findLogDetails(PoLog log) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PoLogRequestParameter> findLogRequestParameters(PoLog log) {
		// TODO Auto-generated method stub
		return null;
	}

}
