package at.workflow.webdesk.tools.api;

public interface User {

	public String getUserName();
	
	public String getLanguageCode();

	public String getWorkflowId();

	public String getFirstName();

	public String getLastName();

	public String getEmail();
	
	public void setLastName(String string);
	
	public void setFirstName(String string);
	
	public void setUserName(String sysAdminUser);
	
	public void setUID(String sysAdminUser);

}
