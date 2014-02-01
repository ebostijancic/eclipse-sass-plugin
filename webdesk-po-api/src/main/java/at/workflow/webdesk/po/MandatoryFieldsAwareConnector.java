package at.workflow.webdesk.po;
/**
 * This interface contains method 
 * which determines whether given field is mandatory or not.
 * 
 * @author sdzuban 04.07.2012
 */
public interface MandatoryFieldsAwareConnector {

	/** returns true, if the passed fieldName is mandatory in the containing Destination Connector */
 	boolean isFieldMandatory(String fieldName);
}
