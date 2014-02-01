/*
 * Created on 07.04.2006
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po;

import java.util.List;

import at.workflow.webdesk.po.model.FlowDirection;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoConnectorLink;
import at.workflow.webdesk.po.model.PoFieldMapping;
import at.workflow.webdesk.po.model.PoModule;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          webdesk3<br>
 * created at:       07.04.2006<br>
 * package:          at.workflow.webdesk.po<br>
 * compilation unit: PoConnectorService.java<br><br>
 *
 * <p>
 *  Allows access to <code>PoConnectorLink</code> and <code>PoFieldMapping</code>.
 * </p>
 *
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
 */
public interface PoConnectorService {
    
    
    /**
     * 
     * 
     * @param uid The <code>Identifier</code> of the object.
     * @return a <code>PoConnectorLink</code> object with the given <code>uid</code> or 
     * null if none was found.
     */
    public PoConnectorLink getConnectorLink(String uid);
    
    /**
     * @param uid
     * @return a <code>PoFieldMapping</code> with the given <code>uid</code>.
     */
    public PoFieldMapping getFieldMapping(String uid);
    
    /**
     * @return a <code>List</code> of <code>PoConnectorLink</code> objects.
     */
    public List<PoConnectorLink> loadAllConnectorLinks();
    
    /**
     * 
     * @param link a <code>PoConnectorLink</code> object.
     * @return a list of <code>PoFieldMapping</code> objects linked with 
     * the given <code>PoConnectorLink</code>.
     */
    public List<PoFieldMapping> findFieldMappingsOfLink(PoConnectorLink link);
    
    /**
     * Stores the given <code>PoConnector</code> link object to the database.
     * 
     * @param link a <code>PoConnectorLink</code> object.
     */
    public void saveConnectorLink(PoConnectorLink link);
    
    /**
     * Stores the given <code>PoFieldMapping</code>.
     * 
     * @param field a <code>PoFiledMapping</code>
     */
    public void saveFieldMapping(PoFieldMapping field);
    
    /**
     * Deletes the given <code>PoConnectorLink</code>. 
     * 
     * @param link
     */
    public void deleteConnectorLink(PoConnectorLink link);
    
    /**
     * Deletes the given <code>PoFieldMapping</code>
     * 
     * @param field
     */
    public void deleteFieldMapping(PoFieldMapping field);
    

    /**
     * 
     * @param name
     * @return a <code>List</code> of <code>PoConnector</code> objects with 
     * the given <code>name</code>.
     */
    public List<PoConnector> findConnectorByName(String name);
    
    /**
     * @return a <code>List</code> of <code>Integer</code> constants.
     * 
     * FIXME move constants from PoConstants to PoCOnnectorService
     */
    public List<Integer> getSyncDirections();
    
    /**
     * returns List of available Connectors (bean and classnames)
     * This is deprecated and will be removed in future.
     * Use getAvailableConnectors(FlowDirection[] flowDirections) instead.
     * @param writeable defines wheter the connector has to be writeable or not.
     * @return a <code>List</code> of <code>PoConnector</code>.
     */
    @Deprecated
    public List<PoConnector> getAvailableConnectors(boolean writeable);
    
  
    /**
     * returns List of available Connectors (bean and classnames)
     * @param flowDirections defines data flow directions the connector has to be able of.
     * @return a <code>List</code> of <code>PoConnector</code>.
     */
    public List<PoConnector> getAvailableConnectors(FlowDirection[] flowDirections);
    
    
	/**
	 * @param uid the <code>uid</code> of the <code>PoConnector</code>
	 * @return a <code>PoConnector</code>
	 */
	public PoConnector getConnector(String uid);
	
	/**
	 * @return a <code>List</code> of <code>PoConnector</code> objects.
	 */
	public List<PoConnector> loadAllConnectors();
	
	/**
	 * <p>Persists the given <code>PoConnector</code>.</p>
	 * 
	 * @param connector a <code>PoConnector</code>
	 */
	public void saveConnector(PoConnector connector);
	
	/**
	 * <p>Deletes the given <code>PoConnector</code></p>
	 * 
	 * @param connector a <code>PoConnector</code>
	 */
	public void deleteConnector(PoConnector connector);

	/**
	 * Returns a <code>PoConnector</code> if one with the given <code>name</code> and 
	 * <code>PoModule</code> was found, null otherwise.
	 * 
	 * @param name the <code>name</code> of the <code>PoConnector</code>
	 * @param module the <code>PoModule</code> which contains the <code>PoConnector</code>
	 * @return a <code>PoConnector</code>. 
	 */
	public PoConnector findConnectorByNameAndModule(String name, PoModule module);
    
	
	/**
	 * @return a <code>List</code> of <code>PoConnector</code>'s.
	 * 
	 */
	public List<PoConnector> findAllConfigurableConnectors();

	/**
	 * @param connector
	 * @return an instance of a class implementing <code>PoSrcConnectorInterface</code>
	 */
	public PoSourceConnectorInterface getSrcConnectorImpl(PoConnector connector);
	
	/**
	 * @param connector
	 * @return an instance of a class implementing <code>PoDestConnectorInterface</code>
	 */
	public PoDestinationConnectorInterface getDestConnectorImpl(PoConnector connector);
	
	
	/**
	 * @param connector
	 * @return a <code>PoConnectorInterface</code> implementation
	 */
	public PoConnectorInterface getConnectorImpl(PoConnector connector);
	
	
    
	/**
	 * @param name the <code>name</code> of the <code>PoConnectorLink</code>
	 * @return a <code>PoConnectorLink</code> object if one with the 
	 * given <code>name</code> was found. 
	 */
	public PoConnectorLink findConnectorLinkByName(String name);

	/**
	 * Finds connector links that have given connector as destination
	 * @param destination
	 * @return
	 */
	public List<PoConnectorLink> findConnectorLinksByDestinationName(String name);

	
	/**
	 * @param connector the <code>PoConnector</code> which should be cloned. 
	 * @return a cloned <code>PoConnector</code>.
	 * 
	 * <p>
	 * Use this function to clone a <code>PoConnector</code> instance. Only the 
	 * latest configuration file is copied. 
	 * 
	 */
	public PoConnector cloneConnector(PoConnector connector);
	
	/** extracts the FlowDirection of the Connector by inspection of the implementing
	 * classes. (Depending on the implementing Interfaces PoSourceConnector/PoDestinationConnector
	 * returns the correct enum type.
	 */
	public FlowDirection getFlowDirectionByInspection(PoConnector connector);
    
}
