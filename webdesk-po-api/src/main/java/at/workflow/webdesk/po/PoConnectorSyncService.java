package at.workflow.webdesk.po;

import java.util.List;

import at.workflow.webdesk.po.model.PoConnectorLink;

/**
 * <p>
 * The ConnectorSyncService provides various ways to synchronize 
 * <code>PoConnectorLink</code> objects. 
 * It <b>does not provide transaction handling</b> which 
 * is up to the <code>PoConnectorLink</code> Implementations!
 * <p>
 * 
 * 
 * 
 * 
 * @author ggruber
 * @author DI Harald Entner <br>
 * 
 * 
 * @see at.workflow.webdesk.po.PoActionService
 * @see at.workflow.webdesk.po.PoOrganisationService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.PoModuleService
 * @see at.workflow.webdesk.po.PoFileService
 * @see at.workflow.webdesk.po.PoLogService
 * @see at.workflow.webdesk.po.PoLanguageService
 * @see at.workflow.webdesk.po.PoBeanPropertyService
 * @see at.workflow.webdesk.po.PoJobService
 *
 */
public interface PoConnectorSyncService {
	
	/**
	 * Constant for automatic string conversion.
	 */
	static String AUTOMATIC = "automatic";
	
    /**
     * @param link
     * 
     * Synchronises two datasources 
     * {<code>link.getSrcConnector()</code> and <code>link.getDestConnector()</code>}
     * 
     * 
     */
    public void synchronise(PoConnectorLink link);
    
    /**
     * This function does the same as <code>synchronise(PoConnectorLink)</code>,
     * but adds an additional constraint to the original <code>PoConnectorLink.sourceConstraint</code>
     * of the connector. 
     * <p>
     * FIXME Wouldn't it be more wisely, to set the constraint of the PoConnectorLink object, or create 
     * another, not persisted object, that has the same interface than a <code>PoConnectorLink</code> object
     * and use that for synchronisation. Otherwise we have to add a destination constraint in the signature as 
     * well which somehow becomes even more complicated for something that is not that complicated at all. 
     * <p>
     *
     * @param link
     * @param addSourceConstraint
     */
    public void synchronise(PoConnectorLink link, String addSourceConstraint);
    
    /**
     * This function synchronises <code>source</code> with <code>dest</code>. Both 
     * objects are allowed to be null, then the PoConnector objects <code>link.getSrcConstraint()</code>
     * or the <code>link.getDestConstraint()</code> are used respectively. 
     *  
     * 
     * @param link 
     * @param addSourceConstraint a constraint that will be added to the finaly choosen source <code>PoConnectorInterface</code> 
     * @param source a <code>PoConnectorInterface</code>, is used when provided
     * @param dest a <code>PoConnectorInterface</code>, is used when provided
     */
    public void synchronise(PoConnectorLink link, String addSourceConstraint, PoSourceConnectorInterface source, 
    		PoDestinationConnectorInterface dest);
    
    
    
    /**
     * @param connectorName
     * @return a <code>List</code> of <code>String</code> objects, representing the 
     * field names of a connector with name <code>connectorName</code>. 
     */
    public List<String> getFieldNamesForConnector(String connectorName);
    
}
