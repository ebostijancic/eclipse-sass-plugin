package at.workflow.webdesk.tools.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used with relation Collection fields (e.g. HrPerson.employmentStates).
 * 
 * @author fritzberger 12.04.2013
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UiCollectionHint
{
	/** The default value for editInvalids(). */
	boolean EDIT_INVALIDS_DEFAULT = false;
	
	/** The default value for detailPanel(). */
	boolean DETAIL_PANEL_DEFAULT = false;
	
	/** The default value for twoTables(). */
	boolean TWO_TABLES_DEFAULT = true;
	
	/** The default value for futurePlanningAllowed(). */
	boolean MORE_THAN_ONE_VALID_DEFAULT = true;
	
	/**
	 * This is for relation tables.
	 * When applied to a bean Collection, this gives the symbolic name of a tab to be created
	 * above the tab the field would be displayed in. When the tab already exists, it is re-used.
	 * This is meant to reduce the number of tabs on first level, and to be applied to Collection
	 * fields (which are displayed as tabs in UI).
	 * @return the name of the tab-over-tab group that should contain the Collection 
	 */
	String tab() default "";
	
	/**
	 * This is for relation tables, expresses what should happen when the "New" action is triggered.
	 * Default a new row IS_EMPTY. When COPIES_RECENT, the most recent valid row (validFrom)
	 * will be copied if one exists. The fields validFrom and validTo will not be copied.
	 * @return the semantics for the "New" button.
	 */
	NewRow create() default NewRow.IS_EMPTY;
	
	/**
	 * True when the table should be editable using a <b>detail-panel</b> (no "inline" table-editing),
	 * meaning the table is read-only, containing just some significant properties, and the
	 * selection of a table row brings the selected entity to the detail-panel, which is
	 * editable only in edit-mode. Every change on the detail-panel is to saved to
	 * the entity immediately as soon as "Save" gets pressed, or the data of another
	 * entity are exposed (selection event).
	 * @return true when the table should be editable using a detail-panel, default is false. 
	 */
	boolean detailPanel() default DETAIL_PANEL_DEFAULT;

	/**
	 * Defines visible Table columns (all others should be invisible) and their order.
	 * @return a text that contains the names of properties to be shown in relation tables
	 * 		where the Collection annotated by this attribute is rendered.
	 */
	String [] tableColumns() default {};
	
	/**
	 * @return rendering informations about an additional table for a relation table
	 * 		(visually below it, containing information of another type, found by finder-method).
	 */
	InheritanceTable inheritanceTable() default @InheritanceTable(finderMethod = "");	// "default" makes this an optional attribute
	
	/**
	 * This is for relation tables containing historicizable items.
	 * @return true when both tables should be editable, the one containing current items and the
	 * 		other containing invalid items. Default is false, only the table containing current items
	 * 		should be editable.
	 */
	boolean editHistorized() default EDIT_INVALIDS_DEFAULT;
	
	/**
	 * This is for relation tables containing historicizable items.
	 * @return true when the elements of the Collection this annotation is attached to
	 * 		should be displayed in an upper Table, holding valid and future element(s),
	 * 		and a lower Table holding historicized elements.
	 */
	boolean twoTables() default TWO_TABLES_DEFAULT;
	
	/**
	 * This is for relation tables containing historicizable items.
	 * At the time being this works only with <code>twoTables == true</code>.
	 * @return true when user can add more than one item in Table of valid items.
	 * 		If this returns false (default), the user will have to remove the current
	 * 		item before being able to add a new one.
	 */
	boolean moreThanOneValid() default MORE_THAN_ONE_VALID_DEFAULT;

}
