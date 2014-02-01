package at.workflow.webdesk.po.impl.adminactions;

import java.util.List;

import at.workflow.webdesk.po.PoMenuService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoClient;

public class RefreshMenuStructureFolderNames extends AbstractAdminAction {
	
	
	private PoMenuService menuService;
	private PoOrganisationService organisationService;

	@Override
	public void run() {
		List<PoClient> clients = this.organisationService.loadAllClients();

		for (PoClient client : clients) {
			this.menuService.refreshTextModulesOfFolders(client);
		}
	}

	public void setMenuService(PoMenuService menuService) {
		this.menuService = menuService;
	}

	public void setOrganisationService(PoOrganisationService organisationService) {
		this.organisationService = organisationService;
	}
	
	private String getSimpleClassName(Class<?> clazz) {
		return clazz.getName().replaceAll(clazz.getPackage().getName(), "").substring(1);
	}

}
