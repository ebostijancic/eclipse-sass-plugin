package at.workflow.webdesk.po.model;

/**
 * <p>
 * This class represents a helpmessage. Each <code>PoHelpMessage</code> have to be assigned to a 
 * <code>PoLanguage</code>. Additionally it can be assigned with a <code>PoAction</code>, but this 
 * is not neccessary. 
 * </p>
 * @author hentner, ggruber
 */
@SuppressWarnings("serial")
public class PoHelpMessage extends PoBase{
    
    private String uid;
    private byte[] text;
    private String url;
    private PoLanguage language;
    private PoAction action;
	private boolean allowUpdateOnVersionChange;
    
   
    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }

    public byte[] getText() {
        return text;
        
    }

    public String getTextAsString() {
    	if (text!=null)
    		return new String(text);
    	else
    		return "";
    }
    
    public void setText(byte[] text) {
        this.text = text;
    }
    
    public void setText(String text) {
    	this.text = text.getBytes();
    }

	public PoAction getAction() {
		return action;
	}

	public void setAction(PoAction action) {
		this.action = action;
	}

	public PoLanguage getLanguage() {
		return language;
	}

	public void setLanguage(PoLanguage language) {
		this.language = language;
	}

	public void setAllowUpdateOnVersionChange(boolean b) {
		this.allowUpdateOnVersionChange = b;
		
	}

	public boolean isAllowUpdateOnVersionChange() {
		return allowUpdateOnVersionChange;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
