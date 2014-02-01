package at.workflow.webdesk.po.impl.adminactions;

import at.workflow.webdesk.po.SpecificAdminAction;
import at.workflow.webdesk.tools.NamingConventions;

public abstract class AbstractAdminAction implements SpecificAdminAction {

	@Override
	public abstract void run();
	
	@Override
	public String getI18nKey() {
		return NamingConventions.getModuleName(getClass().getName()) + "_admin_" + getClass().getSimpleName() + "_success";
	}

	@Override
	public String getSuccessMessage() {
		return NamingConventions.getModuleName(getClass().getName()) + "_admin_" + getClass().getSimpleName() + "_error";
	}

	@Override
	public String getErrorMessage() {
		return NamingConventions.getModuleName(getClass().getName()) + "_admin_" + getClass().getSimpleName() + "_caption";
	}
	

}
