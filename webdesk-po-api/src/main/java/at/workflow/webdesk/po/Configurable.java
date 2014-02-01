package at.workflow.webdesk.po;

import java.util.Collection;

import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.model.PoModule;

public interface Configurable {
	
	public Collection<PoFile> getConfigFiles();

	public void addConfigFile(PoFile file);

	public String getUID();
	
	public PoModule getModule();
	
	public String getName();
	
}
