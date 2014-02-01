package at.workflow.webdesk.tools.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * Field annotation, not applicable for relation Collections.
 * Marks a field that has certain UI behaviors or features,
 * e.g. e-mail field, listed field like language, ....
 * <p/>
 * The contained attributes may or may not depend on each other.
 * 
 * @author fritzberger 12.04.2013
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UiFieldHint
{
	/**
	 * @return false when the field this annotation is attached to
	 * 		must NOT be modified anymore after first save of the containing entity (e.g. client).
	 * 		Default is true, to let fields modify at any time;
	 */
	boolean update() default true;
	
	/**
	 * A reference value is a property value of a referenced bean, e.g. PoPerson has a "language"
	 * property of type String that holds the "name" property value of some entity in PoLanguage.
	 * This annotation attribute would return <code>PoLanguage.class</code>.
	 * @return the entity-type the value should be read from.
	 * 		To create a value for such a field, search a GeneralDomainObjectReader that supports this type,
	 * 		let user choose an entity by exposing a ReferenceChooser* field, then use
	 * 		<code>domainObjectReader.getTextRepresentation(entity)</code> method to create a String value for the field.
	 */
	Class<? extends PersistentObject> referenceValueBeanClass() default PersistentObject.class;	// "default" makes this an optional attribute
	
	/**
	 * A reference value is a property value of a referenced bean, e.g. PoPerson has a "language"
	 * property of type String that holds the "name" property value of some entity in PoLanguage.
	 * This annotation attribute would return <code>name</code>.
	 * @return the entity-type the value should be read from.
	 * 		To create a value for such a field, search a GeneralDomainObjectReader that supports this type,
	 * 		let user choose an entity by exposing a ReferenceChooser* field, then use
	 * 		<code>domainObjectReader.getTextRepresentation(entity)</code> method to create a String value for the field.
	 */
	String referenceValuePropertyName() default "";	// "default" makes this an optional attribute

	/**
	 * @return true when the field annotated with this holds an email-address.
	 */
	boolean isEmail() default false;

	/**
	 * @return true if only the time part (minutes) of the Date this annotation is attached to should be displayed.
	 * 		Default every Date field is displayed as day (without time part).
	 */
	boolean isTime() default false;

	/**
	 * @return true if the day and time (minutes) part of the Date this annotation is attached to should be displayed.
	 * 		Default every Date field is displayed as day (without time part).
	 */
	boolean isDayAndTime() default false;

	/**
	 * @return true if the day, time (minutes) and second part of the Date this annotation is attached to should be displayed.
	 * 		Default every Date field is displayed as day (without time part).
	 */
	boolean isDayAndTimeSeconds() default false;

	/**
	 * @return the id of the KeyValueType if the property this annotation is attached to is a KeyValueType field.
	 * 		KeyValueType is a term from Webdesk/Cocoon and designates a String value that is in fact an enum
	 * 		chosen from a set of customer-specific and runtime-estimated possible values, always of type String.
	 */
	String keyValueTypeId() default "";

	/**
	 * @return the id of the Spring bean to be called when the UI field's value changes.
	 */
	String fieldLogicBeans() default "";
	
//	/**
//	 * TODO: implement this in WdBeanFieldGroup!
//	 * @return true if the text of the property this annotation is attached to should be displayed masked ("*****").
//	 */
//	boolean isPassword() default false;

}
