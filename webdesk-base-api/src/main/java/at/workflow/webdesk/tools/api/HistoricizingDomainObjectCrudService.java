package at.workflow.webdesk.tools.api;

/**
 * TODO: JavaDoc
 * 
 * @author ggruber 15.05.2013
 * @param <T> POJO the type managed by this service.
 */
public interface HistoricizingDomainObjectCrudService <T extends Historization>
	extends DomainObjectCrudService <T>
{
	/**
	 * @param domainObject the entity which should be deleted physically (not historicized).
	 * @throws RuntimeException when a business logic error occurs, or an error occurs in persistence layer.
	 */
	void deletePhysically(T domainObject);

}
