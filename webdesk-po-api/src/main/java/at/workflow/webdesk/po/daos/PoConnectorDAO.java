package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.FlowDirection;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoConnectorLink;
import at.workflow.webdesk.po.model.PoFieldMapping;
import at.workflow.webdesk.po.model.PoModule;

/**
 * Created on 07.04.2006
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          webdesk3.1<br>
 * created at:       02.07.2007<br>
 * package:          at.workflow.webdesk.po.daos<br>
 * compilation unit: PoConnectorDAO.java<br><br>
 *
 * <p>
 * Interface for read, write, update and find <code>PoConnectorLink</code> and 
 * <code>PoFieldMapping</code> objects.
 * </p>
 */
public interface PoConnectorDAO extends GenericDAO<PoConnector> {
    
    /**
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
     * 
     * @param link a <code>PoConnectorLink</code> object.
     * @return a list of <code>PoFieldMapping</code> objects linked with 
     * the given <code>PoConnectorLink</code>.
     */
    public List<PoFieldMapping> findFieldMappingsOfLink(PoConnectorLink link);
    
    /**
     * <p>Persists the given <code>PoConnectorLink</code>.</p>
     * 
     * @param link
     */
    public void saveConnectorLink(PoConnectorLink link);
    
    /**
     * Persists the given <code>PoFieldMapping</code>.
     * 
     * @param field
     */
    public void saveFieldMapping(PoFieldMapping field);
    
    /**
     * Deletes the given <code>PoConnectorLink</code>.
     * 
     * @param link
     */
    public void deleteConnectorLink(PoConnectorLink link);
    
    /**
     * <p>Deletes the given <code>PoFieldMapping</code></p>
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
    public List<PoConnector>  findConnectorByName(String name);

    /**
     * @return a <code>List</code> of <code>PoConnectorLink</code> objects.
     */
    public List<PoConnectorLink>  loadAllConnectorLinks();

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
	public List<PoConnector>  findAllConfigurableConnectors();
	
	/**
	 * @param writeable defines wheter the <code>PoConnector</code> is writeable or not.
	 * @return a <code>List</code> of <code>String</code>'s containing the <code>name</code>'s of the 
	 * usable <code>PoConnector</code>. In other words <code>PoConnector</code>'s which are not 
	 * configurable.
	 */
	public List<String> findAllUsableConnectorNames(boolean writeable);

	
	/**
	 * @param writeable defines wheter the connector has to be writeable or not.
	 * Use findAllUsableConnectors(FlowDirection[] flowDirections) instead.
	 * @return a <code>List</code> of <code>PoConnector</code>'s. In other words <code>PoConnector</code>'s which are not 
	 * configurable. If <code>writeable</code> is <code>true</code>, only <code>PoConnector</code> objects, that can write are returned.
	 */
	@Deprecated
	public List<PoConnector> findAllUsableConnectors(boolean writeable);

	/**
	 * @param flowDirections defines data flow directions the connector has to be capable of.
	 * @return a <code>List</code> of <code>PoConnector</code>'s.
	 */
	public List<PoConnector> findAllUsableConnectors(FlowDirection[] flowDirections);
	
	/**
	 * @param name the <code>name</code> of the <code>PoConnectorLink</code>
	 * @return a <code>PoConnectorLink</code> object if one with the 
	 * given <code>name</code> was found. 
	 */
	public PoConnectorLink findConnectorLinkByName(String name);

	/**
	 * finds connector links that have given connector as destination
	 * @param destination
	 * @return
	 */
	public List<PoConnectorLink> findConnectorLinksByDestinationName(String name);
	
}
