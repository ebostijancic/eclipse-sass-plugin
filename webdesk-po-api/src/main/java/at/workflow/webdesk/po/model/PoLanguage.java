package at.workflow.webdesk.po.model;

import java.util.Collection;
import java.util.HashSet;

import at.workflow.webdesk.tools.model.annotations.UiHints;

/**
 * Represents a Language in the Webdesk System. Is defined though its isoCode (like en or de)
 * and its fullname. Has linked textmodules, which provide localized messages which are 
 * presisted and can be overriden by the customer. Exactly *One* language has to be defined as 
 * default language
 * 
 * @author ggruber
 */

@UiHints(
		isSmallList = true,
		autoCompleteJavaScript = "name+' ('+code+')'",
		orderBy = "defaultLanguage desc, name, code")

public class PoLanguage extends PoBase {

    private String uid;
    private String code;
    private String name;
    private boolean defaultLanguage;
    private Collection<PoTextModule> textModules = new HashSet<PoTextModule>();
    private Collection<PoHelpMessage> helpMessages = new HashSet<PoHelpMessage>();

    /**
     * The Unique ID of the PoLanguage
     */
    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }

    /**
     * @return unique Code the language (f.i. 'de').
     */
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return human readable name of the language, f.i. "German".
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Collection of linked textmodules
     */
    public Collection<PoTextModule> getTextModules() {
        return textModules;
    }

    public void setTextModules(Collection<PoTextModule> value) {
        textModules = value;
    }

    public boolean addTextModule(PoTextModule element) {
        return textModules.add(element);
    }
    
    /**
     * @return boolean, if this language is the default language
     */
    public boolean getDefaultLanguage() {
    	return defaultLanguage;
    }
    
    public void setDefaultLanguage(boolean language) {
    	this.defaultLanguage = language;
    }

    /**
     * @return Collection of HelpMessages linked to this Language
     */
    public Collection<PoHelpMessage> getHelpMessages() {
		return helpMessages;
	}

	public void setHelpMessages(Collection<PoHelpMessage> helpMessages) {
		this.helpMessages = helpMessages;
	}
    
    @Override
	public String toString() {
        String ret = "PoLanguage[name=" + this.name +
            ", code=" + this.code + 
            ", defaultlanguage=" + this.defaultLanguage +
            ", uid=" + this.uid + "]";
        return ret;
    }
    
}
