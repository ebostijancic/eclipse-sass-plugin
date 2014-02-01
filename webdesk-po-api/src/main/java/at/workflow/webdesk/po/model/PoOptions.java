package at.workflow.webdesk.po.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.configuration.Settings;

import at.workflow.webdesk.tools.api.ClusterConfig;

/**
 * Core system configuration properties.
 */
public class PoOptions implements ClusterConfig {

    private boolean jsErrorHandling;
    private String pathToWelcomeLogo;
    private String firstAction;
    private String clusterNode;
    private List<String> clusterNodes = new ArrayList<String>();
	private boolean hideContextMenu;
	private boolean allowSkins;
	private List<String> skins = new ArrayList<String>();
	private String progressOpacityInPercent;
	private boolean allowUserDeputy = false;
	private String pathToStaticPages;
	private boolean enableJsonpWebServiceCalls = false;
	private boolean enableMulticlientConstraints = false;
	private boolean enableEmbeddedTabs = false;
	private String roleNameForJsonpWebServiceCalls;
	
	public boolean isEnableEmbeddedTabs() {
		return enableEmbeddedTabs;
	}

	public void setEnableEmbeddedTabs(boolean enableEmbeddedTabs) {
		this.enableEmbeddedTabs = enableEmbeddedTabs;
	}

	public boolean isEnableMulticlientConstraints() {
		return enableMulticlientConstraints;
	}

	public void setEnableMulticlientConstraints(boolean enableMulticlientConstraints) {
		this.enableMulticlientConstraints = enableMulticlientConstraints;
	}

	public boolean isEnableJsonpWebServiceCalls() {
		return enableJsonpWebServiceCalls;
	}

	public void setEnableJsonpWebServiceCalls(boolean enableJsonpWebServiceCalls) {
		this.enableJsonpWebServiceCalls = enableJsonpWebServiceCalls;
	}

	private Settings settings;
    
    @Override
	public String getClusterNode() {
		return clusterNode;
	}

	public void setClusterNode(String clusterNode) {
		this.clusterNode = clusterNode;
	}

	public String getFirstAction() {
        return firstAction;
    }

    public void setFirstAction(String firstAction) {
        this.firstAction = firstAction;
    }

    public boolean isJsErrorHandling() {
        return jsErrorHandling;
    }

    public void setJsErrorHandling(boolean jsErrorHandling) {
        this.jsErrorHandling = jsErrorHandling;
    }
    
    public String getPathToWelcomeLogo() {
    	return this.pathToWelcomeLogo;
    }

	public void setPathToWelcomeLogo(String pathToWelcomeLogo) {
		this.pathToWelcomeLogo = pathToWelcomeLogo;
	}

	@Override
	public List<String> getClusterNodes() {
		return clusterNodes;
	}

	public void setClusterNodes(List<String> clusterNodes) {
		this.clusterNodes = clusterNodes;
	}

	// due to some problems with overriding of beanproperties
	// we cannot just ignore missing entries in this case and
	// have to use the Spring Configurator API to get the setting
	@Override
	public boolean isDistributed() {
		// failsave for testing
		if (this.settings==null)
			return false;
		
		return (this.settings.getProperty("isDistributed")!=null && this.settings.getProperty("isDistributed").equals("true"));
	}
	
	
	public boolean runningInWorkingCluster() {
		return isDistributed() && getClusterNodes()!=null && getClusterNodes().size()>1 && getClusterNodes().contains(clusterNode);
	}


	/**
	 * @return <code>true</code> if the user is allowed
	 * to define its own deputy.
	 */
	public boolean isAllowUserDeputy() {
		return allowUserDeputy;
	}

	public void setAllowUserDeputy(boolean allowUserDeputy) {
		this.allowUserDeputy = allowUserDeputy;
	}

	public boolean isHideContextMenu() {
		return hideContextMenu;
	}

	public void setHideContextMenu(boolean hideContextMenu) {
		this.hideContextMenu = hideContextMenu;
	}

	public List<String> getSkins() {
		return skins;
	}

	public void setSkins(List<String> skins) {
		this.skins = skins;
	}

	public boolean isAllowSkins() {
		return allowSkins;
	}

	public void setAllowSkins(boolean allowSkins) {
		this.allowSkins = allowSkins;
	}

	public String getProgressOpacityInPercent() {
		return progressOpacityInPercent;
	}

	public void setProgressOpacityInPercent(String progressOpacityInPercent) {
		this.progressOpacityInPercent = progressOpacityInPercent;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public String getPathToStaticPages() {
		return pathToStaticPages;
	}

	public void setPathToStaticPages(String pathToStaticPages) {
		this.pathToStaticPages = pathToStaticPages;
	}

	public String getRoleNameForJsonpWebServiceCalls() {
		return roleNameForJsonpWebServiceCalls;
	}

	public void setRoleNameForJsonpWebServiceCalls(
			String roleNameForJsonpWebServiceCalls) {
		this.roleNameForJsonpWebServiceCalls = roleNameForJsonpWebServiceCalls;
	}

}
