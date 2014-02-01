package at.workflow.webdesk.tools.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field annotation.
 * Marks a field that must be unique "in time", meaning at one time point the value of
 * this property must be unique, but duplicates are possible when each of them is valid
 * in a time interval not overlapping with that of the others.
 * <p/>
 * This annotation is used in AbstractDomainObjectCrudService to do an automatic uniqueness
 * check on save().
 * 
 * @author fritzberger 14.06.2013
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UniqueInTime
{

}
