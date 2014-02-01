package at.workflow.webdesk.po.impl.test.mocks;

import java.util.List;

import org.springframework.core.io.Resource;

import at.workflow.webdesk.po.PoRegistrationService;
import at.workflow.webdesk.po.model.PoModule;

public class PoRegMockService implements PoRegistrationService {

	public void registerActions(Resource[] actionDescriptors, String folderOfPackage) {
		// TODO Auto-generated method stub

	}

	public void registerJobs(Resource[] jobDescriptors, String folderOfPackage) {
		// TODO Auto-generated method stub

	}

	public void syncTextModules(Resource[] textModules) {
		// TODO Auto-generated method stub

	}

	public void registerConfigs(Resource[] configs) {
		// TODO Auto-generated method stub

	}

	public void runRegistration(List<String> modules, boolean ignoreIniCache) {
		// TODO Auto-generated method stub

	}

	public void runRegistrationWithOutTextModules(List<String> modules) {
		// TODO Auto-generated method stub

	}

	public void runRegistrationOfActionsAndFlowscripts(List<String> modules) {
		// TODO Auto-generated method stub

	}

	public void registerJobConfigs(Resource[] ress) {
		// TODO Auto-generated method stub

	}

	public String appendRealPathIfNecessary(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerConnectors(Resource[] ress, PoModule module) {
		// TODO Auto-generated method stub

	}

}
