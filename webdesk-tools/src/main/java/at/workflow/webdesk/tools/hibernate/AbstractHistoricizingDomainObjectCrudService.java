package at.workflow.webdesk.tools.hibernate;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.HistoricizingDAO;
import at.workflow.webdesk.tools.api.HistoricizingDomainObjectCrudService;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.Interval;

/**
 * Generic implementations for services.
 * 
 * @author fritzberger 06.06.2013
 * @author sdzuban 14.06.2013 logging, historicization
 */
public abstract class AbstractHistoricizingDomainObjectCrudService <T extends Historization>
	extends AbstractDomainObjectCrudService<T>
	implements HistoricizingDomainObjectCrudService<T>
{
	@Override
	public void delete(T domainObject) {
		historicize(domainObject);
	}
	
	@Override
	public void deletePhysically(T domainObject) {
		super.delete(domainObject);
	}
	
	/**
	 * Default implementation that directly delegates to DAO.
	 * @param domainObject the entity which should be historicized, i.e. its validity shall be terminated.
	 * @throws RuntimeException when a business logic error occurs, or an error occurs in persistence layer.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected final void historicize(T domainObject) {
		final String uid = domainObject.getUID();
		logBeforeDelete(domainObject, uid, "Historicize");
		
		if (StringUtils.isEmpty(uid) == false) {
			((HistoricizingDAO) getDao()).historicize(domainObject);
			
			logAfterDelete(domainObject, uid, "Historicize");
		}
	}
	
	@Override
	protected Interval getValidty(T domainObject) {
		return domainObject.getValidity();
	}

}
