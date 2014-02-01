package at.workflow.webdesk.po.impl.adminactions;

import at.workflow.webdesk.po.PoLicenceInterceptorInterface;

public class InitializeAllowedActions extends AbstractAdminAction {

	private PoLicenceInterceptorInterface licenceInterceptor;
	
	@Override
	public void run() {
        licenceInterceptor.initAllowedActions();
	}

	public void setLicenceInterceptor(PoLicenceInterceptorInterface licenceInterceptor) {
		this.licenceInterceptor = licenceInterceptor;
	}
}
