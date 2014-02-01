package at.workflow.webdesk.po.model;

import java.util.Date;


/**
 * Persists System Messages in Database. 
 * 
 * System Messages are shown on the Welcome Page but can be 
 * viewed in a separate window as well. 
 * 
 * They will be used in order to tell something to every user
 * of Webdesk R3. 
 *
 * @author hentner
 */
@SuppressWarnings("serial")
public class PoSystemMessage extends PoBase {
    
    private String uid;

    /**
     * userName of the message owner
     */
    private String userName;
    
    
    private String headline;
    
    private byte[] text;
    
    private Date createdAt;
    
    private Date becomesValidAt;
    
    private String url;
    
    
    /**
     * Determines when the <code>PoSystemMessage</code> becomes valid.
     * And, as a consequence, will be displayed when correctly configured.
     * 
     * @return a <code>java.util.Date</code> object.
     */
    public Date getBecomesValidAt() {
		return becomesValidAt;
	}

	public void setBecomesValidAt(Date becomesValidAt) {
		this.becomesValidAt = becomesValidAt;
	}

	/**
	 * Returns a Hyperlink. When the Hyperlink is set, The Body is not displayed.
	 * 
	 * @return a <code>String</code>.
	 */
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url= url;
	}

    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }

    /**
     * @return the date on which the message was written.
     */
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return the headline of the message
     */
    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public byte[] getText() {
        return text;
    }

    public String getTextAsString() {
    	return new String(text);
    }
    
    public void setText(byte[] text) {
        this.text = text;
    }
    
    public void setText(String text) {
    	this.text = text.getBytes();
    }

    /**
     * @return the username of the user that has written the message
     */
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
   


}
