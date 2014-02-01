package at.workflow.webdesk.po.model;

/**
 * Represents an exception information inside a LogDetail object.
 * 
 * @author ggruber
 */
public class PoLogDetailThrowable extends PoBase{
	
	private static final long serialVersionUID = 1L;
	
	private int position = 0;
    private String message = null;
    private String UID;
    private PoLogDetail logDetail;
    
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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

    /**
     * @return Returns the logDetail.
     * 
     */
    public PoLogDetail getLogDetail() {
        return logDetail;
    }

    /**
     * @param logDetail The logDetail to set.
     */
    public void setLogDetail(PoLogDetail logDetail) {
        this.logDetail = logDetail;
    }
}
