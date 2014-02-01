package at.workflow.webdesk.po.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Transient;

import at.workflow.webdesk.Detachable;

/**
 * A Beanproperty is used to persist a Property of a Spring Bean.
 * The property can be scalar or of type list/array.
 * 
 * @author hentner, ggruber
 */
@SuppressWarnings("serial")
public class PoBeanProperty extends PoBase implements Detachable {

	private String uid;
	private PoModule module;
	private String beanName;
	private String className;
	private String propertyName;
	private Boolean list;
	private int type;
	private Collection<PoBeanPropertyValue> entries = new ArrayList<PoBeanPropertyValue>();
	private Boolean isPassword;
	private Boolean detached;

	/** @return <code>true</code> if the given <code>PoBeanProperty</code> represents a password, false if not or undefined (null). */
	public boolean isPassword() {
		return isPassword == null ? false : isPassword.booleanValue();
	}

	public void setPassword(Boolean isPassword) {
		this.isPassword = isPassword;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	/** @return the values of this property, never null. */
	public Collection<PoBeanPropertyValue> getEntries() {
		return entries;
	}

	/** Spring setter. Do not use unless this collection not managed by Hibernate (see cascade type). */
	public void setEntries(Collection<PoBeanPropertyValue> entries) {
		this.entries = entries;
	}

	/** Adds a value and links it to this property. */
	public void addEntry(PoBeanPropertyValue value) {
		value.setBean(this);
		entries.add(value);
	}

	/** Constructs and sets a single a value (links it to this property). Mind that this removes all existing values when present! */
	@Transient
	public void setSingleValue(String value) {
		entries.clear();
		addEntry(new PoBeanPropertyValue(value));
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	@Override
	public String getUID() {
		return uid;
	}

	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}

	public boolean isList() {
		return list == null ? false : list.booleanValue();
	}

	public void setList(Boolean list) {
		this.list = list;
	}

	/** @return corresponding <code>PoModule</code> object. */
	public PoModule getModule() {
		return module;
	}

	public void setModule(PoModule module) {
		this.module = module;
	}

	/** @return one of PoConstants.STRING, PoConstants.STRING, .BOOLEAN, .INTEGER, .DOUBLE. */
	public int getType() {
		return type;
	}

	/** @param type one of PoConstants.STRING, PoConstants.STRING, .BOOLEAN, .INTEGER, .DOUBLE. */
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public boolean isDetached() {
		if (detached == null) {
			if (module == null) {
				return false;
			}
			return module.isDetached();
		}
		return detached.booleanValue() || (module != null && module.isDetached());
	}

	public void setDetached(Boolean detached) {
		this.detached = detached;
	}

	@Override
	public void reactivate() {
		this.detached = new Boolean(true);
	}

	@Override
	public String toString() {
		if (entries.size() != 0)
			return beanName + "." + propertyName + ": " + entries.toArray()[0] + " -> " + (getEntries().size() - 1) + " more [" + getUID() + "]";
		return "no entries";
	}

}
