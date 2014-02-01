package at.workflow.webdesk.po.model;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.<br />
 * fri_2010-11-17: this means that compareTo() compares the listIndex, as does equals().
 * 
 * @author ggruber, hentner
 */
@SuppressWarnings("serial")
public class PoBeanPropertyValue extends PoBase implements Comparable<PoBeanPropertyValue> {

	private String uid;
	private String property;
	private PoBeanProperty bean;
	private int listIndex;

	/** Default contstructor, needed by Hibernate. */
	public PoBeanPropertyValue() {
	}

	/** Custom constructor with ... @param property the value of this property. */
	public PoBeanPropertyValue(String property) {
		this.property = property;
	}

	public PoBeanProperty getBean() {
		return bean;
	}

	public void setBean(PoBeanProperty bean) {
		this.bean = bean;
	}

	/** @return the value of this property. */
	public String getProperty() {
		return property;
	}

	/** Sets the value of this property. @param property the value of this property. */
	public void setProperty(String property) {
		this.property = property;
	}

	@Override
	public String getUID() {
		return uid;
	}

	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}

	public int getListIndex() {
		return listIndex;
	}

	public void setListIndex(int listIndex) {
		this.listIndex = listIndex;
	}

	/**
	 * Implements Comparable by comparing list indexes.
	 */
	@Override
	public int compareTo(PoBeanPropertyValue other) {
		return getListIndex() == other.getListIndex() ? 0 : getListIndex() > other.getListIndex() ? +1 : -1;
	}
	
	/**
	 * Implements Comparable and returns true when list indexes are equal.
	 * Mind that this is NOT an override of equals(Object) and thus does NOT violate the hashCode/equals contract!
	 */
	public boolean equals(PoBeanPropertyValue other) {
		return listIndex == other.listIndex;
	}


	@Override
	public String toString() {
		String beanName = "";
		String className = "";
		String propertyName = "";
		if (bean != null) {
			beanName = bean.getBeanName();
			className = bean.getClassName();
			propertyName = bean.getPropertyName();
		}
		return "PoBeanPropertyValue[" +
				"beanName=" + beanName +
				", className=" + className +
				", propertyName=" + propertyName +
				", property=" + getProperty() +
				", UID=" + uid + "]";

	}

}
