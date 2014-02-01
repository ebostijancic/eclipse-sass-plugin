package at.workflow.webdesk.po.model;
/**
 * This class encapsulates information necessary for
 * administration and use of one Data Source.
 * 
 * @author sdzuban 03.05.2012
 */
public class PoDataSourceDefinition extends PoBase implements Comparable<PoDataSourceDefinition>{
	
	private String uid;
	private String name;
	private String driver;
	private String url;
	private String userName;
	private String password;
	private int maxIdle = -1;
	private int maxActive = -1;
	
	@Override
	public String getUID() {
		return uid;
	}
	
	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}
	
	/** required, unique */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getMaxIdle() {
		return maxIdle;
	}
	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}
	public int getMaxActive() {
		return maxActive;
	}
	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}
	/** {@inheritDoc} */
	@Override
	public int compareTo(PoDataSourceDefinition o) {
		if (o == null)
			return 1;
		return name.compareTo(o.getName());
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name + ": " + url + ": " + userName;
	}
}
