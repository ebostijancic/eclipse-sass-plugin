package at.workflow.webdesk.tools.comparator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Sorts according to a template list that can be passed to constructor.
 * Elements not contained in template List will be sorted to end.
 * For such elements the Comparator tries to use the Comparable interface, if they implement such.
 * <p/>
 * This is generic (typed to Object) to enable a template list holding elements with different type
 * than the elements in list to sort have. You can then override convertToTemplateType() to convert
 * the object in list to sort to the representation required by template list.
 * 
 * @author fritzberger 03.04.2013
 */
public class TemplateDrivenComparator implements Comparator<Object>
{
	private final List<?> templateList;	// the sort order template

	/** No-argument constructor for sub-classes that override templateList(). */
	protected TemplateDrivenComparator()	{
		templateList = null;	// will cause exception when templateList() is called
	}
	
	public TemplateDrivenComparator(List<?> templateList) {
		assert templateList != null && templateList.size() > 0 : "Makes no sense to create a TemplateDrivenSorter with no template list!";
		this.templateList = templateList;
	}

	public TemplateDrivenComparator(Object [] templateArray) {
		assert templateArray != null && templateArray.length > 0 : "Makes no sense to create a TemplateDrivenSorter with no template array!";
		this.templateList = Arrays.asList(templateArray);
	}
	
	/** @return the sort order the comparison should use. Override this for sort other orders, defined e.g. in sub-class array fields. */
	protected List<?> templateList()	{
		if (templateList == null)
			throw new IllegalStateException("Please override this method, or pass a non-empty template in constructor!");
		
		return templateList;
	}
	
	/** Sorts after template list of this class. Unknown names will be appended to end, alphabetically. */
	@Override
	public int compare(Object o1, Object o2) {
		o1 = convertToTemplateType(o1);
		o2 = convertToTemplateType(o2);
		
		// estimate template position for both elements
		int i1 = templateList().indexOf(o1);
		int i2 = templateList().indexOf(o2);
		
		if (i1 == -1)	// sort unknown to end
			i1 = Integer.MAX_VALUE;
		
		if (i2 == -1)
			i2 = Integer.MAX_VALUE;
		
		final boolean bothNotInTemplateList = (i1 == Integer.MAX_VALUE && i2 == Integer.MAX_VALUE);
		
		if (bothNotInTemplateList && shouldDetectComparable() && o1 instanceof Comparable && o2 instanceof Comparable)	{
			// try to sort unknown values as good as possible
			@SuppressWarnings("rawtypes")
			Comparable c1 = (Comparable) o1;
			@SuppressWarnings("rawtypes")
			Comparable c2 = (Comparable) o2;
			@SuppressWarnings("unchecked")
			int r = c1.compareTo(c2);
			return r;
		}
		
		if (bothNotInTemplateList)
			return bothNotInTemplateList(o1, o2);
		
		return i1 - i2;
	}
	
	/** Called when none of the compared values were in template-list. This implementation returns 0. */
	@SuppressWarnings("unused")	// parameters are for overrides
	protected int bothNotInTemplateList(Object o1, Object o2) {
		return 0;
	}

	/** Override and return false if you do not want this class to detect Comparable instances. */
	protected boolean shouldDetectComparable() {
		return true;
	}

	/**
	 * Override this when the data-type of elements in template-List is other than data-type of elements in list to sort.
	 * @return the given object without any conversion, this is for identical types in list to sort and template list.
	 */
	protected Object convertToTemplateType(Object objectInListToSort)	{
		return objectInListToSort;
	}

}
