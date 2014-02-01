package at.workflow.webdesk.po.impl.daos;

import java.util.List;

import at.workflow.webdesk.po.model.PoMenuItemBase;
import at.workflow.webdesk.po.model.PoMenuItemFolder;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoMenuItemFolderDAOImpl extends GenericHibernateDAOImpl<PoMenuItemFolder> {
	private PoMenuItemBaseDAOImpl menuItemBaseDAOImpl;

	@Override
	protected Class<PoMenuItemFolder> getEntityClass() {
		return PoMenuItemFolder.class;
	}

	/* (non-Javadoc)
	 * @see at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl#delete(java.lang.Object)
	 */
	@Override
	public void delete(PoMenuItemFolder entity) {
		List<PoMenuItemBase> children = entity.getChilds();
		for(PoMenuItemBase child : children) {
			menuItemBaseDAOImpl.delete(child);
		}
		super.delete(entity);
	}

	/**
	 * spring setter
	 * @param menuItemBaseDAOImpl the menuItemBaseDAOImpl to set
	 */
	public void setMenuItemBaseDAOImpl(PoMenuItemBaseDAOImpl menuItemBaseDAOImpl) {
		this.menuItemBaseDAOImpl = menuItemBaseDAOImpl;
	}
}
