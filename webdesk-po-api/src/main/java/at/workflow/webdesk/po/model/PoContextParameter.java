/**
 * 
 */
package at.workflow.webdesk.po.model;
/**
 * sdzuban 14.03.2011
 */
@SuppressWarnings("serial")
public class PoContextParameter extends PoBase implements PoActionParameterBase, Comparable<PoContextParameter> {

	private String UID;
	private PoAction action;
	private String name;
	private String type;
	private String pattern;
	private String level;
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
	public String getName() {
		return name;
	}
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
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/** {@inheritDoc} */
	public int compareTo(PoContextParameter p) {
		if (name == null && p.name == null)
			return 0;
		if (name == null)
			return -1;
		if (p.name == null)
			return 1;
		return name.compareToIgnoreCase(p.name);
	}
}
