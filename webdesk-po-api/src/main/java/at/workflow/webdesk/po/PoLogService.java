package at.workflow.webdesk.po;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;

import at.workflow.webdesk.po.model.PoLog;
import at.workflow.webdesk.po.model.PoLogDetail;
import at.workflow.webdesk.po.model.PoLogRequestParameter;

/**
 * <p>
 * provides CRUD functions for logging.
 * is also used by the custom Log4JAppender (@link at.workflow.webdesk.po.impl.PoLogAppenderImpl)
 * </p>
 * @author ggruber
 * @author hentner (so far only docs)
 *
 *
 * @see at.workflow.webdesk.po.PoActionService
 * @see at.workflow.webdesk.po.PoOrganisationService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.PoModuleService
 * @see at.workflow.webdesk.po.PoFileService
 * @see at.workflow.webdesk.po.PoLanguageService
 * @see at.workflow.webdesk.po.PoBeanPropertyService
 * @see at.workflow.webdesk.po.PoJobService

 */
public interface PoLogService {

    /**
     * gets PoLog Object specified
     * @param uid
     * @return PoLog Object
     */
    public PoLog getLog(String uid);
    
    /**
     * save specified PoLog Object
     * @param log
     */
    public void saveLog(PoLog log);
    
    /**
     * deletes specified PoLog Object
     * @param log
     */
    public void deleteLog(PoLog log);
    
    /**
     * deletes all PoLog AND PoLogDetail Objects 
     */
    public void deleteAllLogs();
    
    /**
     * Deletes logs that were ended at least days along
     */
    public void deleteOlderXDays(int days);

    /**
     * refreshes Log4J Settings
     * 
     * @param filename of Log4J XML Configuration File
     */
    public void refreshLogSettings(String filename);
    
    public Set<String> getUsersToLog();
    
    public Set<String> getActionsToLog();
    
    public void setUsersToLog(Set<String> usersToLog);
    
    public void setActionsToLog(Set<String> actionsToLog);
    
    
    public boolean isLogAllActions();

    public void setLogAllActions(boolean logActions);

    public boolean isLogAllUsers();

    public void setLogAllUsers(boolean logAllUsers);
    
    public List<PoLog> findLogsInSameContinuation(PoLog log);
    
    public Document removeLog4JNamespaces(Document log4JConfig);
    
    public Document addLog4JNamespaces(Document log4JConfig);
    
    /**
     * @param log
     * @return a <code>List</code> of <code>PoLogDetail</code>'s.
     */
    public List<PoLogDetail> findLogDetails(PoLog log);
    
    /** returns all <code>PoLogRequestParameter</code> objects for a PoLog  */
    public List<PoLogRequestParameter> findLogRequestParameters(PoLog log);
    
}



