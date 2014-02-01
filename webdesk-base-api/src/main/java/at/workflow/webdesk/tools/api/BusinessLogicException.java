package at.workflow.webdesk.tools.api;

import java.util.Arrays;

import at.workflow.webdesk.tools.NamingConventionI18n;
import at.workflow.webdesk.tools.arrays.ArrayTools;

/**
 * This exception is to be thrown when a business logic error occurs (-> fachlicher Fehler).
 * This is NOT to be used for technical or any other than business problems (use IllegalArgumentException for such)!
 * Because of that it does not support construction without an internationalized message.
 * 
 * @author fritzberger 06.06.2013
 * @author sdzuban 25.10.2013
 */
public class BusinessLogicException extends I18nRuntimeException
{
	private final String [] errorPropertyNames;
	
	/**
	 * Convenience constructor to build a BusinessLogicException
	 * with a message that contains the given property name as translation.
	 * @param i18nMessageKey the resource-key denoting the message, 
	 * Translations can have two standard positional parameters, {0} for the translated name of the property and 
	 * {1} for the translated class name. Keys and translation flags for them are generated automatically.
	 * @param persistenceClass the class the given property resides in.
	 * @param propertyName the property to be passed as positional parameter to given message.
	 */
	public BusinessLogicException(String i18nMessageKey, Class<? extends PersistentObject> persistenceClass, String propertyName)	{
		this(
				new String [] { propertyName },
				new I18nMessage(i18nMessageKey,
						new String [] { NamingConventionI18n.getI18nKey(persistenceClass, propertyName), NamingConventionI18n.getI18nKey(persistenceClass) },
						new Boolean [] { Boolean.TRUE, Boolean.TRUE })
				);
	}
	
	/**
	 * Convenience constructor to build a BusinessLogicException
	 * with a message that contains the given property name as translation.
	 * 
	 * TODO: this misleading constructor anticipates that parameter propertyName
	 * 		is used in text message of given i18nMessage, but not contained in the positional parameter
	 * 		values of given i18nMessage (which itself would be a violation of the I18nMessage contract),
	 * 		and thus alters the i18nMessage, even if the caller did everything right.
	 * 		Correct would be: The parameter propertyName is processed by the UI independently
	 * 		of the message text. 
	 * 
	 * @param i18nMessage the message.  
	 * 		Translations can have two standard positional parameters, {0} for the translated name of the property and 
	 * 		{1} for the translated class name. Keys and translation flags for them are inserted automatically.
	 * 		All original message parameters are appended after standard positional parameters and are numbered from {2} onwards.
	 * @param persistenceClass the class the given property resides in.
	 * @param propertyName the property to be passed as positional parameter to given message.
	 */
	public BusinessLogicException(I18nMessage i18nMessage, Class<? extends PersistentObject> persistenceClass, String propertyName)	{
		this(
				new String [] { propertyName },
				new I18nMessage(i18nMessage.getI18nKey(),
						ArrayTools.mergeStringArrays(
								new String [] {
										NamingConventionI18n.getI18nKey(persistenceClass, propertyName),
										NamingConventionI18n.getI18nKey(persistenceClass)
								},
								i18nMessage.getPositionalParameters()),
						ArrayTools.mergeBooleanArrays(
								new Boolean [] {
										Boolean.TRUE,
										Boolean.TRUE
								}, 
								i18nMessage.getTranslationFlags())
				));
	}
	
	/** Business layer message that lets define error property names. */
    public BusinessLogicException(String [] errorPropertyNames, String i18nMessageKey) {
        this(errorPropertyNames, new I18nMessage(i18nMessageKey));
    }

    /** Internationalized business layer message. */
    public BusinessLogicException(I18nMessage i18nMessage) {
    	this(null, i18nMessage);
    }
    
    /** Internationalized business layer message that lets define error property names. */
    public BusinessLogicException(String [] errorPropertyNames, I18nMessage i18nMessage) {
    	super(i18nMessage);
    	this.errorPropertyNames = errorPropertyNames;
    }
    
    /**
     * @return the names of the properties responsible for this error;
     * 		these names could also be dotted names (relative to a "root" entity), e.g. "person.userName" for entity HrPerson.
     */
    public String [] getErrorPropertyNames()	{
    	return (errorPropertyNames != null) ? errorPropertyNames : new String[0];
    }
    
    @Override
    public String toString() {
    	return super.toString() + (errorPropertyNames != null ? ", errorPropertyNames=" + Arrays.asList(errorPropertyNames) : "");
    }
}
