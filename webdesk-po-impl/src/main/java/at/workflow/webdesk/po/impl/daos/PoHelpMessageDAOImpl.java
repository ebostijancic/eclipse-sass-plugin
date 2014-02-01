package at.workflow.webdesk.po.impl.daos;

import java.util.List;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.daos.PoHelpMessageDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoHelpMessage;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          3.1DEV<br>
 * created at:       29.08.2007<br>
 * package:          at.workflow.webdesk.po.impl.daos<br>
 * compilation unit: PoHelpMessageDAOImpl.java<br><br>
 * 
 * <p>Hibernate Implementation of the <code>PoHelpMessageDAO</code> interface.</p>
 * 
 * <p>
 * 	Handles <code>PoHelpMessage</code> objects.
 * </p>
 */
public class PoHelpMessageDAOImpl extends GenericHibernateDAOImpl<PoHelpMessage> implements PoHelpMessageDAO {

	@Override
	protected Class<PoHelpMessage> getEntityClass() {
		return PoHelpMessage.class;
	}
	
	public List findHelpMessages(PoLanguage lang) {
		Object[] key = {lang};
		return getHibernateTemplate().find("from PoHelpMessage where language=?", key);
	}

	public List findHelpMessagesOfAction(PoAction action) {
		Object[] key = {action};
		return getHibernateTemplate().find("from PoHelpMessage where action=?", key);
	}
	
	public PoHelpMessage getHelpMessage(PoAction action, PoLanguage language) {
		Object[] keys = {action, language};
		return (PoHelpMessage) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoHelpMessage where action=? and language=?", keys));
	}

}
