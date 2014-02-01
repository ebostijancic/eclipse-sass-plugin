package at.workflow.webdesk.po;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Document;

/**
 * Connectors are export/import utilities.
 * Data sources and destinations can be database tables, CVS files, ...,
 * anything that supports objects with named properties.
 * Every connector can export data, for a source connector this is a responsibility,
 * for a destination connector this is to find out if an object about to be imported
 * already exists in destination store. A destination connector can further import data.
 * There is only one specific responsibility in a source-connector which is not required
 * in a destination-connector: post-process records that have been imported by a peer
 * destination connector. The destination connector has also a post-processing, but this
 * receives imported persistence objects, not records. The term record is used here to
 * describe a Map of String/Object.
 * <p/>
 * This is the connector control flow (see PoConnectorSyncServiceImpl):
 * <ul>
 * 	<li>initialization: setConfigurationFile(), setApplicationContext(), ... init() is called.</li>
 * 	<li>on a source connector, findAllObjects() is called to export data</li>
 * 	<li>on a destination connector, cleanup is performed before importing anything, to optionally delete all records from a table</li>
 * 	<li>on a destination connector save is called for any exported record, returned persistence objects are collected to be post-processed</li>
 * 	<li>on a destination connector, post-process is called with all resulting imported persistence objects</li>
 * 	<li>on a source connector, post-process is called with all records associated to actually imported objects</li>
 * </ul>
 * 
 * @author DI Harald Entner, logged in as hentner, 07.04.2006
 * @author fritzberger 22.02.2012 - verified, corrected JavaDoc, refactoring.
 */
public interface PoConnectorInterface extends ApplicationContextAware {
    
	// initialization
	
    /**
     * @param document the configuration for this connector.
     */
    public void setConfigurationFile(Document document);
    
    /**
     * Initialization of the <code>PoConnectorInterface</code>'s implementation.
     * This is called after <code>setConfigurationFile()</code>,
     * <code>setApplicationContext()</code> and <code>setConnector()</code>.
     */
    public void init();

	// responsibilities
    
    /**
     * @return List of available field-names (meta-data) of the data-source,
     * 		mappable by this connector. So what's passed into <code>findAllObjects()</code>
     * 		must be identical or a subset of the List returned from here.
     */
    public List<String> getFieldNames();

    /**
     * Reads export-records (Map) from some data-store, whereby only given fields will be retrieved.
     * Even if this is a <i>source-connector</i> functionality it is here because every
     * destination-connector needs it to find out if an object about to be imported already exists.
     * @param fieldNames List of names of the wanted fields.
     * @param constraint specifying some data-source-specific constraint (e.g. an SQL WHERE clause)
     * 		restricting the number of read objects, can be null.
     * @return a List of Maps whereby one Map represents a record of the data-source,
     * 		whereby key is the field name and value the content for that field in that record.
     * 		Only fields given by argument <code>fieldNames</code> will be contained.
     */
    public List<Map<String, Object>> findAllObjects(List<String> fieldNames, String constraint);

}
