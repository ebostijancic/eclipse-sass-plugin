package at.workflow.webdesk.po.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import at.workflow.webdesk.po.PoRuntimeException;

/**
 * Links two connectors (source and destination) in such a way 
 * that a correspondence between two record objects, one  
 * of the <code>sourceConnector</code> and one of the 
 * <code>destinationConnector</code>, is known. 
 * 
 * The number of records of the source- /dest-connector to be 
 * synchronised can be filtered by specifying appropriate
 * constraints (stored in <code>sourceConstraint</code> and 
 * <code>destinationConstraint</code>.
 * Constraints are implementation specific.
 * 
 * The <code>mappingFields</code> define a Mapping for
 * each field (column) to be synchronised. 
 * 
 * The direction defines whether to synchronise from source to destination,
 * the opposite way or both.
 */
@SuppressWarnings("serial")
public class PoConnectorLink extends PoBase {

    private String uid;
    
    private String name;
    
    private String destinationConnector;
    
    private String sourceConnector;
    
    
    private PoConnector srcConnector;
    
    private PoConnector destConnector;
    
    private String sourceConstraint;
    
    private String destinationConstraint;
    
    private Collection<PoFieldMapping> mappingFields = new LinkedHashSet<PoFieldMapping>();
    
    private boolean syncDeletions;
    
    private boolean converterAfterJavaScript;
    
    private int direction;
    
    private boolean convertNullToEmptyString;
    
    private String locale;

    public PoConnector getDestConnector() {
        return destConnector;
    }

    public void setDestConnector(PoConnector destinationConnector) {
        this.destConnector = destinationConnector;
    }

    public String getDestinationConstraint() {
        return destinationConstraint;
    }

    public void setDestinationConstraint(String destinationConstraint) {
        this.destinationConstraint = destinationConstraint;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * returns the Mapping Fields as a HashMap where 
     * selectively the source or the destination field is the 
     * key
     * @param source if true the name of the source Connector 
     * field is the key, otherwise the name of the 
     * destination connector field is the key. 
     * @return the Mapping Fields as a HashMap where 
     * selectively the source or the destination field is the 
     * key
     */
    public Map<String, PoFieldMapping> getMappingFieldsAsMap(boolean source) {
        Map<String, PoFieldMapping>  m = new HashMap<String, PoFieldMapping> ();
        Iterator<PoFieldMapping> mI = this.getMappingFields().iterator();
        while (mI.hasNext()) {
            PoFieldMapping fm = mI.next();
            if (source) 
                m.put(fm.getSourceName(),fm);
            else
                m.put(fm.getDestinationName(),fm);
        }
        return m;
    }
    
    /**
     * @param source
     * @return selectively the source or the destination connetor fields as an ArrayList 
     */
    public List<String> getMappingFieldsAsList(boolean source) {
        List<String> l = new ArrayList<String>();
        Iterator<PoFieldMapping> mI = this.getMappingFields().iterator();
        while (mI.hasNext()) {
            PoFieldMapping fm = mI.next();
            if (source) {
            	if (fm.getSourceName()!=null)
            		l.add(fm.getSourceName());
            } else {
            	if (fm.getDestinationName()!=null)
            		l.add(fm.getDestinationName());
            }
        }
        return l;
    }
    
    
    
    public String getPrimaryKey(boolean isSource) {
        Iterator<PoFieldMapping> i = this.mappingFields.iterator();
        while (i.hasNext()) {
            PoFieldMapping mf = i.next();
            if (mf.isPrimaryKey()) {
                if (isSource)
                    return mf.getSourceName();
                else
                    return mf.getDestinationName();
            }
        }
        throw new PoRuntimeException("No primary key defined in ConnectorLink!");
    }
    
    public String getWriteBackPrimaryKey(boolean isSource) {
        Iterator<PoFieldMapping> i = this.mappingFields.iterator();
        while (i.hasNext()) {
            PoFieldMapping mf = i.next();
            if (mf.isWriteBackPrimaryKey()) {
                if (isSource)
                    return mf.getSourceName();
                else
                    return mf.getDestinationName();
            }
        }
        throw new PoRuntimeException("No primary key defined in ConnectorLink!");
    }
    
    public Collection<PoFieldMapping> getMappingFields() {
        
        return mappingFields;
    }

    public void setMappingFields(Collection<PoFieldMapping> mappingFields) {
        this.mappingFields = mappingFields;
    }

    public void addMappingField(PoFieldMapping mappingField) {
    	mappingField.setConnectorLink(this);
    	this.mappingFields.add(mappingField);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PoConnector getSrcConnector() {
        return srcConnector;
    }

    public void setSrcConnector(PoConnector sourceConnector) {
        this.srcConnector = sourceConnector;
    }
    
    public String getSourceConstraint() {
        return sourceConstraint;
    }

    public void setSourceConstraint(String sourceConstraint) {
        this.sourceConstraint = sourceConstraint;
    }

    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }    
    
    @Override
	public String toString() {
        String ret = "PoConnectorLink[" +
            "name=" + this.name + 
            ", sourceConnector=" + this.sourceConnector +
            ", sourceConstraint=" + this.sourceConstraint + 
            ", destinationConnector=" + this.destinationConnector + 
            ", destinationConstraint=" + this.destinationConstraint + 
            ", direction=" + this.direction+ 
            ", uid=" + this.uid + "]";
        return ret;
    }

	public boolean isSyncDeletions() {
		return syncDeletions;
	}

	public void setSyncDeletions(boolean syncDeletions) {
		this.syncDeletions = syncDeletions;
	}

	/**
	 * @deprecated
	 */
	public String getDestinationConnector() {
		return destinationConnector;
	}

	/**
	 * @deprecated
	 */
	public void setDestinationConnector(String destinationConnector) {
		this.destinationConnector = destinationConnector;
	}

	/**
	 * @deprecated
	 */
	public String getSourceConnector() {
		return sourceConnector;
	}

	/**
	 * @deprecated
	 */
	public void setSourceConnector(String sourceConnector) {
		this.sourceConnector = sourceConnector;
	}

    /**
     * defines whether the converter should be processed 
     * after executing the javascript. defaults to false!
     */
	public boolean isConverterAfterJavaScript() {
		return converterAfterJavaScript;
	}

	public void setConverterAfterJavaScript(boolean converterAfterJavaScript) {
		this.converterAfterJavaScript = converterAfterJavaScript;
	}

	public boolean isConvertNullToEmptyString() {
		return convertNullToEmptyString;
	}

	public void setConvertNullToEmptyString(boolean convertNullToEmptyString) {
		this.convertNullToEmptyString = convertNullToEmptyString;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

}
