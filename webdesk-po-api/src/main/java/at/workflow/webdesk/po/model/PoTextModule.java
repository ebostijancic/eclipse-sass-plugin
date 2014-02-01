package at.workflow.webdesk.po.model;


import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * Represents a single localized message (a textmodule) for a given Language and
 * a specific usecase. A Textmodule can be linked to an arbitrary webdesk artifact, which can
 * be an action, or more general a webdesk module. If a Textmodule is linked to an action, it
 * is also linked to the corresponding module, the action belongs to.
 * In order to be able to reuse existing translations, a textmodule can inherit an existing 
 * translation for another usecase by having set the 'parent-reference'. In that case the value
 * will be taken from the parent instead of using its own value. By using this pattern, actions
 * can use existing translations without losing the ability to later change any message on the 
 * screen without necessarily changing this translation for the whole webdesk system. 
 * 
 * @author ggruber
 */
@SuppressWarnings("serial")
public class PoTextModule extends PoBase {

    private String uid;
    private String name;
    private String value;
    private PoLanguage language;
    private PoAction action;
    private PoTextModule parent;
    private Collection<PoTextModule> childs = new HashSet<PoTextModule>();
    private Boolean allowUpdateOnVersionChange;
    private PoModule module;
    private Date lastModified;

    /**
     * returns unique ID of the textmodule
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
     * returns unique name of the textmodule which follows follwing convention:
     * &lt;modulename>_&lt;artifactName>_&lt;internalName>, f.i. po_editClient.act_label_name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * returns actual translation as string or null if translation is empty 
     */
    public String getValue() {
        if (this.value!=null && value.equals(""))
            return null;
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
	 * returns the language the translation
     */
    public PoLanguage getLanguage() {
        return language;
    }

    public void setLanguage(PoLanguage language) {
        this.language = language;
    }

    /**
     * returns the action where this textmodule belongs to. might be null.
     */
    public PoAction getAction() {
        return action;
    }

    public void setAction(PoAction action) {
        this.action = action;
    }


    /**
     * returns the PoTextModule from which the current textmodule inherits its value.
     */
    public PoTextModule getParent() {
        return parent;
    }



    public void setParent(PoTextModule parent) {
        this.parent = parent;
    }
    
    /**
     * defines whether this translation-value will be reset to its default if a registration occurs (f.i. as result of a systemupdate)
     */
    public boolean isAllowUpdateOnVersionChange() {
        if (allowUpdateOnVersionChange==null || allowUpdateOnVersionChange.booleanValue()==false)
            return false;
        else
            return true;
    }

    public void setAllowUpdateOnVersionChange(Boolean allowUpdateOnVersionChange) {
        this.allowUpdateOnVersionChange = allowUpdateOnVersionChange;
    }

    /**
	 * returns a list of textmodules, which inherit their translation value from the current textmodule.
	 * Those translations MUST be from the same language!
     */
    public Collection<PoTextModule> getChilds() {
        return childs;
    }

    public void setChilds(Collection<PoTextModule> values) {
        childs = values;
    }

    public boolean addChild(PoTextModule element) {
        return childs.add(element);
    }

    @Override
	public String toString() {
        String ret = "PoTextModule[" + 
            " name=" + this.name +
            ", value=" + this.value + 
            ", module=" + this.module + 
            ", uid=" + this.uid + "]";
        return ret;
    }


    /**
     * returns webdesk module, this textmodule belongs to.
     */
    public PoModule getModule() {
        return module;
    }

    public void setModule(PoModule module) {
        this.module = module;
    }

	/**
	 * returns Date of last Modification.
	 */
	public Date getLastModified() {
		if (lastModified==null)
			lastModified = new Date();
			
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		if (lastModified==null)
			this.lastModified = new Date();
		this.lastModified = lastModified;
	}

   
}
