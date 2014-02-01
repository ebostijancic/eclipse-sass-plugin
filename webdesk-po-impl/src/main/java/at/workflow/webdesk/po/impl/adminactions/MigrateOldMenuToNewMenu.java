package at.workflow.webdesk.po.impl.adminactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoMenuService;
import at.workflow.webdesk.po.PoMenuTreeService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoMenuItem;
import at.workflow.webdesk.po.model.PoMenuItemActionLink;
import at.workflow.webdesk.po.model.PoMenuItemFolder;
import at.workflow.webdesk.po.model.PoMenuTreeClient;

public class MigrateOldMenuToNewMenu extends AbstractAdminAction {

	// PoMenuTreeService aka the new menu service.
	private PoMenuTreeService menuTreeService;
	private PoOrganisationService organisationService;
	private PoMenuService menuService;

	@Override
	public void run() {
		menuTreeService.clearMenuCache();
		menuService.clearMenuCache();

		Map<PoMenuItem, PoMenuItemActionLink> actionLinks = new HashMap<PoMenuItem, PoMenuItemActionLink>();
		Map<PoMenuItem, PoMenuItemFolder> folders = new HashMap<PoMenuItem, PoMenuItemFolder>();
		List<PoMenuItem> topLevelFolders = new ArrayList<PoMenuItem>();
		List<PoMenuItem> topLevelActions = new ArrayList<PoMenuItem>();

		for (PoClient client : organisationService.loadAllClients()) {
			final PoMenuTreeClient tree = createClientMenuTree(client);
			menuTreeService.saveMenuTree(tree);
			final List<PoMenuItem> itemsByClient = menuService.findMenuItemsByClient(client);

			for (PoMenuItem item : itemsByClient) {
				if (item.getParent() == null) {

					if (item.getAction() != null) {
						final PoMenuItemActionLink actionLink = getActionLinkFromMenuItem(item, tree, null);
						actionLinks.put(item, actionLink);
						menuTreeService.saveMenuItem(actionLink);
						topLevelActions.add(item);

					} else {
						final PoMenuItemFolder folder = getFolderFromMenuItem(item, tree, null);
						folders.put(item, folder);
						menuTreeService.saveMenuItem(folder);
						topLevelFolders.add(item);
					}
				}
			}

			itemsByClient.removeAll(topLevelActions);
			itemsByClient.removeAll(topLevelFolders);

			Collections.sort(itemsByClient, new Comparator<PoMenuItem>() {

				@Override
				public int compare(PoMenuItem o1, PoMenuItem o2) {
					if (o1.getRanking() > o2.getRanking()) {
						return 1;
					} else if (o1.getRanking() < o2.getRanking()) {
						return -1;
					}
					return 0;
				}
			});

			for (PoMenuItem item : itemsByClient) {
				if (folders.get(item) == null) {

					PoMenuItemFolder parent = folders.get(item.getParent());

					if (parent == null && item.getParent() != null) {
						final PoMenuItemFolder folder = getFolderFromMenuItem(item.getParent(), tree,
								folders.get(item.getParent().getParent()));
						folders.put(item.getParent(), folder);
						menuTreeService.saveMenuItem(folder);
					}

					parent = folders.get(item.getParent());

					if (item.getAction() == null) {
						final PoMenuItemFolder folder = getFolderFromMenuItem(item, tree, parent);
						folders.put(item, folder);
						menuTreeService.saveMenuItem(folder);
					} else if (item.getTemplateId() != null) {

					} else {
						final PoMenuItemActionLink actionLink = getActionLinkFromMenuItem(item, tree, parent);
						actionLinks.put(item, actionLink);
						menuTreeService.saveMenuItem(actionLink);
					}

					if (parent != null) {
						menuTreeService.saveMenuItem(parent);
					}
				}
			}

			menuTreeService.saveMenuTree(tree);
		}
	}

	private PoMenuItemFolder getFolderFromMenuItem(PoMenuItem item, PoMenuTreeClient tree, PoMenuItemFolder parentFolder) {
		PoMenuItemFolder folder = new PoMenuItemFolder();
		folder.setMenuTree(tree);

		if (parentFolder != null) {
			parentFolder.addChild(folder);
		} else {
			tree.addTopLevelItem(folder);
		}

		folder.setRanking(item.getRanking());
		folder.setI18nKey(item.getName());

		return folder;
	}

	private PoMenuTreeClient createClientMenuTree(PoClient client) {
		PoMenuTreeClient tree = new PoMenuTreeClient();
		tree.setClient(client);
		return tree;
	}

	private PoMenuItemActionLink getActionLinkFromMenuItem(PoMenuItem item, PoMenuTreeClient tree, PoMenuItemFolder parent) {
		PoMenuItemActionLink actionLink = new PoMenuItemActionLink();
		actionLink.setAction(item.getAction());
		actionLink.setParent(parent);
		actionLink.setMenuTree(tree);
		actionLink.setRanking(item.getRanking());

		if (parent != null) {
			parent.addChild(actionLink);
		}

		return actionLink;
	}

	public void setMenuTreeService(PoMenuTreeService menuTreeService) {
		this.menuTreeService = menuTreeService;
	}

	public void setMenuService(PoMenuService menuService) {
		this.menuService = menuService;
	}

	public void setOrganisationService(PoOrganisationService organisationService) {
		this.organisationService = organisationService;
	}
}
