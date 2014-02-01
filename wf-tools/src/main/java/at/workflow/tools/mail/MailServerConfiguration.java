package at.workflow.tools.mail;

/**
 * A model holding values for a <code>Mail Server Configuration</code>.
 * 
 * @author hentner
 */
public class MailServerConfiguration {

	private String adress;
	private int port = 25;
	private String userName;
	private String password;
	private String defaultSender;
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getAdress() {
		return adress;
	}

	public void setAdress(String adress) {
		this.adress = adress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int i) {
		this.port = i;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDefaultSender() {
		return defaultSender;
	}

	public void setDefaultSender(String defaultSender) {
		this.defaultSender = defaultSender;
	}

}
