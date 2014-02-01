package at.workflow.webdesk.po.impl.daos;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import at.workflow.webdesk.po.daos.PoSystemMessageDAO;
import at.workflow.webdesk.po.model.PoSystemMessage;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * Created on 20.11.2006
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          webdesk3<br>
 * created at:       20.11.2006<br>
 * package:          at.workflow.webdesk.po.impl.daos<br>
 * compilation unit: PoSystemMessageDAOImpl.java<br><br>
 *
 * The hibernate implementation of PoSystemMessageDAO.
 *
 */
public class PoSystemMessageDAOImpl extends GenericHibernateDAOImpl<PoSystemMessage> implements PoSystemMessageDAO {

	@Override
	protected Class<PoSystemMessage> getEntityClass() {
		return PoSystemMessage.class;
	}
	
	@Override
    public PoSystemMessage get(Serializable uid) {
        return (PoSystemMessage) getHibernateTemplate().load(PoSystemMessage.class, uid);
    }

	@Override
	public List<PoSystemMessage> loadAll() {
		return (List<PoSystemMessage>) getHibernateTemplate().find("from PoSystemMessage order by createdAt desc");
	}

    public List<PoSystemMessage> findSystemMessages(int daysToLive) {
        if (daysToLive == 0)
        	return null;
        
        Calendar g1 = GregorianCalendar.getInstance();
        g1.set(Calendar.HOUR_OF_DAY, 0); 
        g1.set(Calendar.MINUTE, 0); 
        g1.set(Calendar.SECOND, 0);
        
        Calendar g2 = GregorianCalendar.getInstance();
        g2.set(Calendar.HOUR_OF_DAY, 23); 
        g2.set(Calendar.MINUTE, 59); 
        g2.set(Calendar.SECOND, 59); 
        
        g1.add(Calendar.DAY_OF_MONTH, -daysToLive);
        Object[] keys = {g1.getTime(), g2.getTime()};
        return getHibernateTemplate().find("from PoSystemMessage sm where sm.becomesValidAt >= ? and sm.becomesValidAt <= ? order by createdAt desc", keys);
    }
    
}
