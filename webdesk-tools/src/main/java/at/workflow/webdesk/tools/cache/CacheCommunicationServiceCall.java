package at.workflow.webdesk.tools.cache;

import java.io.Serializable;
import java.util.Date;

/**
 * Bean to hold all information for a Service-Call which has to be propagated to all
 * nodes within the same webdesk cluster.
 *  
 * @author ggruber
 *
 */
@SuppressWarnings("serial")
public class CacheCommunicationServiceCall implements Serializable {
	
	public CacheCommunicationServiceCall(String id, String beanName, String methodName, Object[] args, String originatingNode) {
		super();
		this.id = id;
		this.beanName = beanName;
		this.methodName = methodName;
		this.args = args;
		this.originatingNode = originatingNode;
	}
	private String id;
	private String beanName;
	private String methodName;
	private Date created = new Date();
	private String originatingNode;
	
	private Object[] args;
	
	
	public String getBeanName() {
		return beanName;
	}
	public String getMethodName() {
		return methodName;
	}
	public Object[] getArgs() {
		return args;
	}
	public String getId() {
		return id;
	}
	public Date getCreated() {
		return created;
	}
	public String getOriginatingNode() {
		return originatingNode;
	}

}
