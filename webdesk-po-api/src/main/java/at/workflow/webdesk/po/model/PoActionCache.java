package at.workflow.webdesk.po.model;

import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * The <code>PoActionCache</code> class allows to cache data on a per-user-base in 
 * for convenience reasons. This way certain definitions or configurations in pure 
 * reporting actions can be persisted for later reuse.
 * Technically it supports saving some formData in a byte array which is currently used
 * to serialize XML data coming from a form object or a Map of request parameters.
 */
@SuppressWarnings("serial")
public class PoActionCache implements PersistentObject {

	private PoPerson person;
	private PoAction action;
	private byte[] formData;
    private String uid;

    @Override
	public String getUID() {
        return uid;
    }

    public void setUID(String uid) {
        this.uid = uid;
    }
    
    public byte[] getFormData() {
        return formData;
    }
    
    public String getFormDataAsString() {
    	return new String(formData);
    }
    
    public void setFormData(byte[] formData) {
        this.formData= formData;
    }
    
    public void setFormData(String formData) {
    	this.formData = formData.getBytes();
    }

	public PoAction getAction() {
		return action;
	}

	public void setAction(PoAction action) {
		this.action = action;
	}

    /**
     * Returns the <code>PoPerson</code> object. In case of the 
     * system administrator, null is returned. 
     */
	public PoPerson getPerson() {
		return person;
	}

	public void setPerson(PoPerson person) {
		this.person = person;
	}
	
}
