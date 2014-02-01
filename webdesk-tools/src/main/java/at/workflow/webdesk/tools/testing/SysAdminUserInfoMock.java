package at.workflow.webdesk.tools.testing;

import at.workflow.webdesk.tools.WebdeskConstants;
import at.workflow.webdesk.tools.api.SysAdminUserInfo;
import at.workflow.webdesk.tools.api.User;

public class SysAdminUserInfoMock implements SysAdminUserInfo {

	public User getSysAdminPerson() {
		return null;
	}

	public String getSysAdminUser() {
		return WebdeskConstants.SADMIN;
	}

}
