package at.workflow.webdesk.tools.api;

/**
 * Base class for internationalized business logic error messages.
 * <p/>
 * Proposed structure of the <code>i18nKey</code> parameter is as follows:
 * <pre>[module]_[simpleClassName].[methodName]_[errorDescription]</pre>.
 * This is NOT an implemented naming convention, as there is no need for
 * deriving an error message from properties or classes. 
 * <p/>
 * Example usage:
 * <pre>
 * 	throw new I18nRuntimeException("hr_HrJobDAOImpl.checkJob_jobNameNotUnique", job.getName());
 * </pre>
 * 
 * @author sdzuban 12.04.2013
 * @author fritzberger 27.05.2013: Improved JavaDoc, better names for constructor parameters.
 */
public class I18nRuntimeException extends RuntimeException {

    private final I18nMessage i18nMessage;

    /**
     * @param i18nMessage error message
     */
    public I18nRuntimeException(I18nMessage i18nMessage) {
    	this(null, i18nMessage);
    }
    
    /**
     * @param cause the real cause for this exception when this is a follow-up exception.
     * @param i18nMessage error message
     */
    public I18nRuntimeException(Throwable cause, I18nMessage i18nMessage) {
    	super(i18nMessage.getI18nKey(), cause);
    	this.i18nMessage = i18nMessage;
    }
    
    /**
     * @param i18nKey resource key for reading the translated error message, or original text when no translation has been made.
     */
    public I18nRuntimeException(String i18nKey) {
        this(new I18nMessage(i18nKey));
    }

    /**
     * @param cause the real cause for this exception when this is a follow-up exception.
     * @param i18nKey resource key for reading the translated error message, or original text when no translation has been made.
     */
    public I18nRuntimeException(Throwable cause, String i18nKey) {
        this(cause, new I18nMessage(i18nKey));
    }


    /** returns the i18n message object */
    public I18nMessage getI18nMessage() {
    	return i18nMessage;
    }
    
    @Override
    public String toString() {
    	return super.toString() + ", i18nMessage=" + i18nMessage.toString();
    }
}
