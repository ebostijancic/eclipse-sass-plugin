/**
 * 
 */
package at.workflow.webdesk.po.model;
/**
 * sdzuban 14.03.2011
 */
@SuppressWarnings("serial")
public class PoActionUrlPattern extends PoBase implements PoActionParameterBase {

	private String UID;
	private PoAction action;
	private String name;
	private String pattern;


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
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
}
