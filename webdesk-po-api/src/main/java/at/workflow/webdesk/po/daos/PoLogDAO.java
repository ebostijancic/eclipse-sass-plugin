package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoLog;
import at.workflow.webdesk.po.model.PoLogDetail;
import at.workflow.webdesk.po.model.PoLogRequestParameter;

/**
 * @author ggruber, DI Harald Entner <br>
 * 
 * package:          at.workflow.webdesk.po.daos<br>
 * compilation unit: PoLogDAO.java<br><br>
 *
 * <p>
 * Interface for accessing <code>PoLog</code> and 
 * <code>PoLogDetail</code> objects.
 *
 */
public interface PoLogDAO extends GenericDAO<PoLog> {

    /**
     * Deletes all logs.
     */
    public void deleteAllLogs();
	
    /**
     * Deletes logs that were ended at least days along
     */
    public void deleteOlderXDays(int days);

    /**
     * @param log
     * @return a <code>List</code> of <code>PoLogDetail</code>'s.
     */
    public List<PoLogDetail> findLogDetails(PoLog log);

    /**
     * find Logs of Requests spawning the same Continuation
     * inside Webdesk / Cocoon
     * 
     * @param log
     * @return List of PoLogs
     */
	public List<PoLog> findLogsInSameContinuation(PoLog log);
	
	public List<PoLogRequestParameter> findLogRequestParameters(PoLog log);
}
