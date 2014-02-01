package at.workflow.webdesk.po.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jdom.DocType;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.w3c.dom.Document;

import at.workflow.webdesk.po.PoLogService;
import at.workflow.webdesk.po.daos.PoLogDAO;
import at.workflow.webdesk.po.model.PoLog;
import at.workflow.webdesk.po.model.PoLogDetail;
import at.workflow.webdesk.po.model.PoLogRequestParameter;

public class PoLogServiceImpl implements PoLogService {
    
    private static final Logger logger = Logger.getLogger(PoLogServiceImpl.class);
    
    private PoLogDAO logDAO;
    private boolean logAllActions = false;
    private boolean logAllUsers = false;
    private Set<String> actionsToLog = Collections.synchronizedSet(new HashSet<String>());
    private Set<String> usersToLog = Collections.synchronizedSet(new HashSet<String>());
    
    @Override
	public PoLog getLog(String uid) {
        return this.logDAO.get(uid);
    }
    
    @Override
	public void saveLog(PoLog log) {
        this.logDAO.save(log);
    }
    
    @Override
	public void deleteLog(PoLog log) {
        this.logDAO.delete(log);
    }
    
    @Override
	public void deleteAllLogs() {
        this.logDAO.deleteAllLogs();
    }
    
    @Override
	public void deleteOlderXDays(int days) {
    	this.logDAO.deleteOlderXDays(days);
    }
    /**
     * @param logDAO The logDAO to set.
     */
    public void setLogDAO(PoLogDAO logDAO) {
        this.logDAO = logDAO;
    }
    
    @Override
	public void refreshLogSettings(String filename) {
        LogManager.shutdown();
        // System.setProperty("context-root", WebdeskEnvironment.getRealPath());
        DOMConfigurator myConfigurator = new DOMConfigurator();
        logger.info("refresh Log4J Settings with file at path " + filename);
        myConfigurator.doConfigure(filename, logger.getLoggerRepository());
        // reset Appender
        PoLogAppenderImpl.resetAppender();
    }

    /**
     * @return Returns the actionsToLog.
     */
    @Override
	public Set<String> getActionsToLog() {
        return actionsToLog;
    }

    /**
     * @param actionsToLog The actionsToLog to set.
     */
    @Override
	public void setActionsToLog(Set<String> actionsToLog) {
        this.actionsToLog.clear();
        this.actionsToLog.addAll(actionsToLog);
    }

    /**
     * @return Returns the logAllActions.
     */
    @Override
	public boolean isLogAllActions() {
        return logAllActions;
    }

    /**
     * @param logAllActions The logAllActions to set.
     */
    @Override
	public void setLogAllActions(boolean logAllActions) {
        this.logAllActions = logAllActions;
    }

    /**
     * @return Returns the logAllUsers.
     */
    @Override
	public boolean isLogAllUsers() {
        return logAllUsers;
    }

    /**
     * @param logAllUsers The logAllUsers to set.
     */
    @Override
	public void setLogAllUsers(boolean logAllUsers) {
        this.logAllUsers = logAllUsers;
    }

    /**
     * @return Returns the usersToLog.
     */
    @Override
	public Set<String> getUsersToLog() {
        return usersToLog;
    }

    
    @Override
	public Document removeLog4JNamespaces(Document log4JConfig) {
        
        Document ret = null;
        DOMBuilder myBuilder = new DOMBuilder();
        org.jdom.Document myDoc = myBuilder.build(log4JConfig); 
        
        myDoc.getRootElement().removeNamespaceDeclaration(Namespace.getNamespace("log4j","http://jakarta.apache.org/log4j/"));
        myDoc.getRootElement().setNamespace(Namespace.NO_NAMESPACE);
        
        DOMOutputter myOutputter = new DOMOutputter();
        try {
            ret = myOutputter.output(myDoc);
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }
    
    @Override
	public Document addLog4JNamespaces(Document log4JConfig) {
        Document ret = null;
        DOMBuilder myBuilder = new DOMBuilder();
        org.jdom.Document myDoc = myBuilder.build(log4JConfig); 
        
        myDoc.setDocType(new DocType("log4j:configuration", "entities/log4j.dtd"));
        myDoc.getRootElement().setNamespace(Namespace.getNamespace("log4j","http://jakarta.apache.org/log4j/"));
        myDoc.getRootElement().addNamespaceDeclaration(Namespace.getNamespace("log4j","http://jakarta.apache.org/log4j/"));
        
        // make sure that root node is at the end
        Element root = myDoc.getRootElement().getChild("root");
        root.detach();
        myDoc.getRootElement().addContent(root);
        
        DOMOutputter myOutputter = new DOMOutputter();
        try {
            ret = myOutputter.output(myDoc);
            
            ret.createElementNS("http://jakarta.apache.org/log4j/", "log4j");
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

	@Override
	public List<PoLogDetail> findLogDetails(PoLog log) {
		return logDAO.findLogDetails(log);
	}

	@Override
	public List<PoLog> findLogsInSameContinuation(PoLog log) {
		return logDAO.findLogsInSameContinuation(log);
	}


    /**
     * @param usersToLog The usersToLog to set.
     */
    @Override
	public void setUsersToLog(Set<String> usersToLog) {
        this.usersToLog.clear();
        this.usersToLog.addAll(usersToLog);
    }

	@Override
	public List<PoLogRequestParameter> findLogRequestParameters(PoLog log) {
		return logDAO.findLogRequestParameters(log);
	}

    
}
