package at.workflow.webdesk.tools.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To be used for relations (Collections) that require the read-only display of
 * additional inherited items, shown as "Effective assignments".
 * 
 * @author fritzberger 14.06.2013
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface InheritanceTable
{
	/**
	 * Name of the service method that returns objects to additionally show
	 * below a relation annotated by this annotation.
	 * TODO: service relation.
	 * @return the name of a method to find by reflection.
	 */
	String finderMethod();
	
	/**
	 * Optional interface or class to be used by UI for rendering the service method result list,
	 * only properties contained in it should be rendered. In case it is an interface,
	 * no setters must be present to make up a property (like it is with classes).
	 * For example interface SkillRequirement will be used to display a mixed list containing
	 * HrJobSkill, HrJobFamilySkill, HrPositionSkill instances.
	 * MIND: this returns Object.class instead of null !
	 * 
	 * @return the class or interface to use for viewing service finder result, or Object.class
	 * 		when the class of the Table this InheritanceTable is attached to should be used.
	 */
	Class<?> displayType() default Object.class;

}
