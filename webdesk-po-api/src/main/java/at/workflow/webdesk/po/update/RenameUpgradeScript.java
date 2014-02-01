package at.workflow.webdesk.po.update;

import org.jdom.Attribute;
import org.jdom.Element;

public abstract class RenameUpgradeScript extends PoAbstractUpgradeScript {

	private String oldName;
	private String newName;
	private String targetModule;
	
	public void setXmlContext(Element xmlContext) {
		Attribute targetModuleAttr = xmlContext.getAttribute("new-module");
		this.targetModule = targetModuleAttr != null ? targetModuleAttr.getValue() : null;
		
		this.oldName = xmlContext.getAttribute("old").getValue();
		
		this.newName = xmlContext.getAttribute("new").getValue();
	}
	
	@Override
	public void execute() {
		rename(oldName, newName, targetModule);
	}
	
	/**
	 * Sub-classes MUST implement this to rename bean-properties.
	 */
	protected abstract void rename(String oldName, String newName, String targetModule);

}
