package at.workflow.webdesk.tools.api;

public interface UserLookupService {

	public User lookupUser(String userName);
	
	public User lookupUserByMail(String mail);
	
}
