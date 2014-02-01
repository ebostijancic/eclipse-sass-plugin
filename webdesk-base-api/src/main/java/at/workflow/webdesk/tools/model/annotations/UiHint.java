package at.workflow.webdesk.tools.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Common Field annotation, suitable for normal fields and relation Collection.
 * 
 * @author fritzberger 12.04.2013
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UiHint
{
	boolean IGNORE_DEFAULT = false;
	boolean READONLY_DEFAULT = false;
	
	/**
	 * @return true if the property this annotation is attached to should not be displayed but ignored completely.
	 */
	boolean ignore() default IGNORE_DEFAULT;
	
	/**
	 * @return the URL bookmark of the editor the property this annotation is attached to
	 * 		should be edited in. For example you could write <i>ignoreExceptIn = "orgunit"</i>,
	 * 		then it would be editable in "orgunit"-editor only (http://somehost:8080/hr-expert/#orgunit).
	 * 		The name of the URL bookmark is made by dropping the module prefix from domain class,
	 * 		and converting all to lower case.
	 */
	String ignoreButNotIn() default "";
	
	/**
	 * @return true if the property this annotation is attached to
	 * 		should be displayed but never be editable (e.g. validfrom, validto).
	 */
	boolean readOnly() default READONLY_DEFAULT;

}
