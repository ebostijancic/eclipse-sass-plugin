package at.workflow.webdesk.po.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * This class represents the details of a 
 * <code>PoLog</code> object, thus there may be 
 * <code>n</code> objects corresponding to one
 * <code>PoLog</code>. If an error occurs, every stack 
 * entry is stored in <code>PoLogDetailThrowable</code>
 *
 * @author ggruber
 */
public class PoLogDetail extends PoBase{
	
	private static final long serialVersionUID = 1L;
	
	private String UID; 
    private String logLevel;
    private String logger;
    private String message;
    private Date timeStamp;
    private PoLog log;
    
    private Set<PoLogDetailThrowable> logDetailThrowables = new HashSet<PoLogDetailThrowable>();
    
    /**
     * @return Returns the logger.
     */
    public String getLogger() {
        return logger;
    }
    /**
     * @param logger The logger to set.
     */
    public void setLogger(String logger) {
        this.logger = logger;
    }
    /**
     * @return Returns the logLevel.
     */
    public String getLogLevel() {
        return logLevel;
    }
    /**
     * @param logLevel The logLevel to set.
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }
    /**
     * @return Returns the timeStamp.
     */
    public Date getTimeStamp() {
        return timeStamp;
    }
    /**
     * @param timeStamp The timeStamp to set.
     */
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
    /**
     * @return Returns the log.
     */
    public PoLog getLog() {
        return log;
    }
    /**
     * @param log The log to set.
     */
    public void setLog(PoLog log) {
        this.log = log;
    }
    /**
     * @return Returns the uID.
     */
    @Override
	public String getUID() {
        return UID;
    }
    /**
     * @param uid The uID to set.
     */
    @Override
	public void setUID(String uid) {
        UID = uid;
    }
    
    public void addLogDetailThrowable(PoLogDetailThrowable exceptionLog) {
        this.logDetailThrowables.add(exceptionLog);
    }
    
    /**
     * @return Returns the logDetailThrowables.
     */
    public Set<PoLogDetailThrowable> getLogDetailThrowables() {
        return logDetailThrowables;
    }
    /**
     * @param logDetailThrowables The logDetailThrowables to set.
     */
    public void setLogDetailThrowables(Set<PoLogDetailThrowable> logDetailThrowables) {
        this.logDetailThrowables = logDetailThrowables;
    }
}
