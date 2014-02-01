package at.workflow.webdesk.po.impl.test.mocks;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoActionCache;
import at.workflow.webdesk.po.model.PoActionParameter;
import at.workflow.webdesk.po.model.PoContextParameter;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoTextModule;

public class PoActionMockService implements PoActionService {

	public PoAction getAction(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveAction(PoAction action) {
		// TODO Auto-generated method stub

	}

	public PoAction findActionWithFullName(String fullName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteAction(PoAction action, Date date) {
		// TODO Auto-generated method stub

	}

	public void deleteAndFlushAction(PoAction action) {
		// TODO Auto-generated method stub

	}

	public PoAction getConfigFromAction(PoPerson person, PoAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoAction getConfigFromAction(PoPerson person, PoAction action,
			String targetUserUid) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoAction getConfigFromActionWithoutTargetPermCheck(PoPerson person,
			PoAction action, String targetPersonUid) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoAction findActionByNameAndType(String name, int type) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoAction findActionByURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoAction> findActionsOfPerson(PoPerson person, Date date) {
		return new ArrayList<PoAction>();
	}

	public List<PoAction> findActionsOfRole(PoRole role, Date date) {
		return new ArrayList<PoAction>();
	}

	public List<PoAction> findConfigsFromAction(PoAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoAction> findConfigs() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoAction> findAllCurrentConfigs() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoAction> loadAllActions() {
		return new ArrayList<PoAction>();
	}

	public List<PoAction> findAllActions(Date date) {
		return new ArrayList<PoAction>();
	}

	public List<String> loadAllImageSets() {
		return new ArrayList<String>();
	}

	public List<String> loadImagesFromImageSet(String imageSet) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoFile> findFilesFromAction(PoAction action) {
		return new ArrayList<PoFile>();
	}

	public String getLocationOfJs(String functionName) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocationOfJs(String functionName, int actionType) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoAction> findActionByProcessDefId(String procDefId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoAction> loadAllCurrentActions() {
		return new ArrayList<PoAction>();
	}

	public void createPrimaryTextModulesOfAction(PoAction action) {
		// TODO Auto-generated method stub

	}

	public void createPrimaryTextModulesOfAction(PoAction newConfig,
			boolean updateOnVersionChange) {
		// TODO Auto-generated method stub

	}

	public void checkAction(PoAction action) {
		// TODO Auto-generated method stub

	}

	public PoTextModule getPrimaryTextModuleOfAction(PoAction action,
			String attribute, PoLanguage language) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<PoActionParameter> getActionParameters(PoAction action) {
		return new ArrayList<PoActionParameter>();
	}

	public void registerAction(Object actionDescr, String path,
			String folderOfPackage) {
		// TODO Auto-generated method stub

	}

	public void registerAction(Object actionDescr, String path,
			String folderOfPackage, Map<String, String> actionCache) {
		// TODO Auto-generated method stub

	}

	public PoAction setSoftValuesOfAction(PoAction newConfig, PoAction oldConfig) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoAction getActionByPath(String path, String folderOfPackage) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoAction getActionFromConfigFile(InputStream is, String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public void replaceAction(PoAction oldConfig, PoAction newConfig) {
		// TODO Auto-generated method stub

	}

	public List<String> findActionNamesOfModule(String name) {
		return new ArrayList<String>();
	}

	public String getActionPostfix(PoAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoActionCache getActionCache(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoActionCache findActionCache(PoAction action, PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveActionCache(PoActionCache actionCache) {
		// TODO Auto-generated method stub

	}

	public List<String> findInUseProcessDefinitions() {
		return new ArrayList<String>();
	}

	public String getActionURL(PoAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public Collection<PoContextParameter> getActionContextParameter(
			PoAction action) {
		// TODO Auto-generated method stub
		return null;
	}

}
