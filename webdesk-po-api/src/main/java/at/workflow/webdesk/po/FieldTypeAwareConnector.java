package at.workflow.webdesk.po;
/**
 * This interface provides method to obtain
 * the type of a field as java Class.
 * 
 * @author sdzuban 04.07.2012
 */
public interface FieldTypeAwareConnector {

	Class<?> getTypeOfField(String fieldId);
}
