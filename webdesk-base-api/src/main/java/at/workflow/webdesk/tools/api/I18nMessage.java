package at.workflow.webdesk.tools.api;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

/**
 * Class for internationalized business logic messages.
 * <p/>
 * Example usage:
 * <pre>
 * 	new I18nMessage("hr_positionService.checkPosition_employeePercentOver100", new String [] {"1.1.2013 - 31.1.2013"});
 * </pre>
 * 
 * @author sdzuban 25.10.2013
 * @author fritzberger 18.11.2013 removed duplication of translation-flags default values.
 */
public class I18nMessage {

	private final String i18nKey;
    private final String[] positionalParameters;
    private final Boolean[] translationFlags;
    
    /**
     * @param i18nKey resource key for reading the translated message, or original text when no translation has been made.
     */
    public I18nMessage(String i18nKey) {
        this(i18nKey, (String []) null);
    }

    /**
     * @param i18nKey resource key for reading the translated message, or original text when no translation has been made.
     * @param positionalParameters to be substituted dynamically for positional place-holders within translated message without translation.
     */
    public I18nMessage(String i18nKey, String ... positionalParameters) {
    	this(i18nKey, positionalParameters, null);
    }

    /**
     * @param i18nKey resource key for reading the translated message, or original text when no translation has been made.
     * @param positionalParameters to be substituted dynamically for positional place-holders within translated message.
     * @param translationFlags flags indicating which of the positional parameters shall be translated
     */
    public I18nMessage(String i18nKey, String[] positionalParameters, Boolean[] translationFlags) {
    	
    	if (positionalParameters != null && translationFlags != null && 
    			positionalParameters.length != translationFlags.length)
    		throw new IllegalArgumentException("Number of positional parameters and number of translation flags must be equal.");
    	
    	this.i18nKey = i18nKey;
    	
    	this.positionalParameters = positionalParameters;
    	// positional parameters of proxies contain _$$javassistXYZ string that prevents translation of the class
    	if (positionalParameters != null)
	    	for (int i = 0; i < positionalParameters.length; i++)
	    		positionalParameters[i] = getParameterWithoutProxy(positionalParameters[i]);
    	
    	// I18nMessage must always have translation flags for all positional parameters 
    	// because of joining parameters e.g. in BusinessLogicException 
    	if (positionalParameters == null)
    		this.translationFlags = null; // translation flags are ignored
    	else if ( translationFlags != null)
    		this.translationFlags = translationFlags;
    	else {
    		this.translationFlags = new Boolean[positionalParameters.length];
    		for (int i = 0; i < positionalParameters.length; i++)
    			this.translationFlags[i] = Boolean.FALSE;
    	}
    }
    
    /**
     * @return the i18n-key to be used for translating the message.
     */
    public String getI18nKey() {
        return i18nKey;
    }

    /** @return the parameters to be inserted dynamically into translated message's positional place-holders. */
    public String[] getPositionalParameters() {
        return positionalParameters;
    }

    /** 
     * @return the flags whether the parameters 
     * to be inserted dynamically into translated message's positional place-holders 
     * shall be translated. 
     */
    public Boolean[] getTranslationFlags() {
        return translationFlags;
    }
    
    /**
     * Returns string with indexes replaced by positional parameters values, e.g.
     * if i18nKey = {0}_before_{1}, positionalParameters = [12.1.2014, 13.1.2014]
     * following message will be output:
     * 	12.1.2014_before_13.1.2014
     *  
     */
    @Override
    public String toString() {
    	String message = i18nKey;
    	if (positionalParameters != null) {
    		for (int i = 0; i < positionalParameters.length; i++)
    			message = message.replaceAll("\\{" + i + "\\}", positionalParameters[i]);
    	}
    	return getClass().getSimpleName() + ": (" + message + 
    			(positionalParameters != null ? ", parameters=" + Arrays.asList(positionalParameters) : "") + ")";
    }

	private String getParameterWithoutProxy(String param) {
		
		if (StringUtils.isNotBlank(param)) {
			// delete the _$$_javassist... proxy
			int idx = param.indexOf("_$$");
			if (idx >= 0)
				param = param.substring(0, idx);
			// delete any remaining $, they would cause exception when matching with {i} ($ is group reference prefix)
			if (param.indexOf('$') >= 0)
				param = param.replaceAll("\\$", "");
		}
		return param;
	}
}
