package at.workflow.webdesk.po.daos;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoActionCache;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          3.1DEV<br>
 * created at:       07.11.2007<br>
 * package:          at.workflow.webdesk.po.daos<br>
 * compilation unit: PoActionCacheDAO.java<br><br>
 * 
 *<p>Interface for the data access of <code>PoActionCache</code> objects.</p> 
 *
 */
public interface PoActionCacheDAO extends GenericDAO<PoActionCache> {
    /**
     * @param action a <code>PoAction</code> object which is linked with the returned <code>PoActionCache</code>.
     * @param person a <code>PoPerson</code> object which is linked with the returned <code>PoActionCache</code>.
     * @return a <code>PoActionCache</code> object
     * if one was found, null otherwise 
     */
    public PoActionCache findActionCache(PoAction action, PoPerson person);
    
}
