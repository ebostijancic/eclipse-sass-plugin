package at.workflow.webdesk.tools.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-annotation.
 * Marks an entity-type that has certain UI behaviors or features,
 * e.g. it contains only few records and thus is listable without paging.
 * <p/>
 * The contained attributes may or may not depend on each other.
 * 
 * @author fritzberger 12.04.2013
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UiHints
{
	/**
	 * Marks entity-types that hold a small amount of domain objects.
	 * For such entities a ReferenceChooserSmall will be allocated by FormViewGeneator, instead of ReferenceChooserBig.
	 * PoLanguage (2 - 70 records) would be annotated with this, but not PoTextModule (15000 records).
	 * @return true when the entity-type annotated with this hold only few records,
	 * 		so that all together can be rendered in a selection list, without the need for paging.
	 */
	boolean isSmallList() default false;
	
	/**
	 * This configures the shape of auto-complete proposals.
	 * When no accompanying autoCompleteProperties is present, properties are scanned off the expression.
	 * @return a JavaScript expression that contains auto-complete properties, e.g. on PoPerson.
	 * 		<code>lastName + ' ' + firstName + ' (' + client.name + ')'</code>.
	 */
	String autoCompleteJavaScript() default "";

	/**
	 * This configures the set of properties that are compared to user input on auto-complete.
	 * It is made up by a comma- or space-separated list of (possibly dotted) properties.
	 * @return a list of properties to be compared to user input on auto-complete, e.g. on PoPerson.
	 * 		<code>lastName firstName, client.name</code>.
	 */
	String [] autoCompleteProperties() default {};


	/**
	 * @return an HQL oder-by clause to be appended to DomainObjectReader query, without leading "order by",
	 * 		e.g. on PoLanguage <code>defaultLanguage desc, name, code</code>.
	 */
	String orderBy() default "";
	
	
	/**
	 * @return at what times the validfrom/validto fields (historization) should be editable.
	 * 		At the time this applies to detail-views only (WdBeanFieldGroup), not to relation tables,
	 * 		where other rules are necessary (see TimelinedTableInFieldGroup TableFieldFactory override).
	 * 		See issue WDHREXPERT-344.
	 */
	Editable validityEditable() default Editable.NEVER;

	/**
	 * Use this list of (possibly dotted) properties if you explicitly want
	 * to define a sort order of fields displayed in a details-view or an embedded details-view
	 * (mind that, when embedded, relation-properties would precede all other properties).
	 * @return the sort order of properties to be displayed in the details view, these names can be
	 * 		dotted names, referencing nested objects (e.g. "person.lastName" in an <i>HrPerson</i> annotation).
	 */
	String [] detailFields() default {};

}
