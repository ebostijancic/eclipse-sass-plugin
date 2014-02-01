package at.workflow.webdesk.po.model;

/**
 * @author ggruber
 */
@SuppressWarnings("serial")
public class PoFieldMapping extends PoBase {

    private String uid;
    
    private boolean isPrimaryKey;
    
    private boolean isWriteBackPrimaryKey;
    
    private boolean writeOnInsertOnly;
    
    private PoConnectorLink connectorLink;
    
    private String sourceName;
    
    private String destinationName;
    
    private String jsFunction;
    
    private int orderIndicator;
    
    private String converter;
    
    private String converterPattern;
    
    private String converterLocale;
    

    public String getConverter() {
		return converter;
	}

	public void setConverter(String converter) {
		this.converter = converter;
	}

	public String getConverterPattern() {
		return converterPattern;
	}

	public void setConverterPattern(String converterPattern) {
		this.converterPattern = converterPattern;
	}

	public void setOrderIndicator(int orderIndicator) {
        this.orderIndicator = orderIndicator;
    }

    public int getOrderIndicator() {
        return orderIndicator;
    }

    public void setOrder(int orderIndicator) {
        this.orderIndicator = orderIndicator;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getJsFunction() {
        return jsFunction;
    }

    public void setJsFunction(String jsFunction) {
        this.jsFunction = jsFunction;
    }
    
    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public PoConnectorLink getConnectorLink() {
        return connectorLink;
    }

    public void setConnectorLink(PoConnectorLink connectorLink) {
        this.connectorLink = connectorLink;
    }
    
    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }


	public boolean isWriteBackPrimaryKey() {
		return isWriteBackPrimaryKey;
	}

	public void setWriteBackPrimaryKey(boolean isSourcePrimaryKey) {
		this.isWriteBackPrimaryKey = isSourcePrimaryKey;
	}

	public boolean isWriteOnInsertOnly() {
		return writeOnInsertOnly;
	}

	public void setWriteOnInsertOnly(boolean writeOnInsertOnly) {
		this.writeOnInsertOnly = writeOnInsertOnly;
	}

	public String getConverterLocale() {
		return converterLocale;
	}

	public void setConverterLocale(String converterLocale) {
		this.converterLocale = converterLocale;
	}

}
