package at.workflow.webdesk.po.model;

/** 
 * This entity stores a key/value pair representing one URL parameter
 * passed in a web request.  It belongs to a PoLog entry, which can
 * have several URL parameter values.
 * 
 * @author ggruber
 */
public class PoLogRequestParameter extends PoBase {

	private static final long serialVersionUID = 1L;
	
	private String UID;
	private String name;
	private String value;
	private PoLog log;
	
	@Override
	public String getUID() {
		return UID;
	}

	@Override
	public void setUID(String uid) {
		this.UID = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public PoLog getLog() {
		return log;
	}

	public void setLog(PoLog log) {
		this.log = log;
	}

}
