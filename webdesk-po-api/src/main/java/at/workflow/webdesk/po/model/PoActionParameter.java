package at.workflow.webdesk.po.model;

/**
 * Allows to define a possible URL parameter which is interpreted by the linked action. By defining this
 * on a meta level, useful links from one action to another can be easily configured.
 * 
 * <br/><br/>All parameter tags inside the 'parameters' tag in the act-descr.xml will be converted to an object of this class.
 * f.i.
 * <br/><br/>
 * &lt;parameters&gt;<br/>
 * &#160;&#160;&#160;&lt;parameter name="projectUID" type="string" comment="required, to load activities applicable to the project" /&gt;<br/>
 * &lt;/parameters&gt;
 * 
 * @author ggruber
 */
@SuppressWarnings("serial")
public class PoActionParameter extends PoBase implements PoActionParameterBase, Comparable<PoActionParameter> {
	
	private String UID;
	
	/** link to the action having the URL parameter */
	private PoAction action;
	
	/** name of the URL parameter */
	private String name;
	
	/** type of the URL paraemter */
	private String type;
	
	/** Pattern used for the URL Parameter value in case of a Date or Number */
	private String pattern;
	
	/** Comment describing the use of this URL parameter */
	private String comment;


	@Override
	public String getUID() {
		return UID;
	}
	@Override
	public void setUID(String uid) {
		UID = uid;
	}
	public PoAction getAction() {
		return action;
	}
	public void setAction(PoAction action) {
		this.action = action;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@Override
	public String toString() {
		return "Parameter " + (name == null ? "" : name) + 
		", Typ " + (type == null ? "" : type) +
		", Pattern " + (pattern == null ? "" : pattern);
	}
	
	/** {@inheritDoc} */
	@Override
	public int compareTo(PoActionParameter p) {
		if (name == null && p.name == null)
			return 0;
		if (name == null)
			return -1;
		if (p.name == null)
			return 1;
		return name.compareToIgnoreCase(p.name);
	}
	
	
}
