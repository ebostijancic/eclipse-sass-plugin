package at.workflow.webdesk.tools.api;

import java.util.List;

/**
 * This interface describes how needed cluster config information has
 * to be passed.
 * 
 * @author ggruber
 *
 */
public interface ClusterConfig {
	
	public static final String DISTRIBUTED = "isDistributed";
	
	/**
	 * @return List of configured ClusterNodes (names)
	 */
	public List<String> getClusterNodes();
	
	/**
	 * get Name of the actual cluster node
	 */
	public String getClusterNode();
	
	/**
	 * @return true, if Webdesk is running in a cluster
	 */
	public boolean isDistributed();

}
