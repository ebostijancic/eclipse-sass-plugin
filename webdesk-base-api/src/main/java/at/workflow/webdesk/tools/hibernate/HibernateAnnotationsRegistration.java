package at.workflow.webdesk.tools.hibernate;

import java.util.List;

/**
 * @author hentner
 *
 * <p>
 * This interface can be used to declare a class as a information 
 * holder of <i>annotated</i> classes.
 * 
 *
 */
public interface HibernateAnnotationsRegistration {
	
	/**
	 * @return a <code>List</code> of <code>String</code>'s 
	 * representing classpaths (with wildcards) of annotated classes. 
	 */
	public List<String> getAnnotatedClasses();
	
	/**
	 * return a <code>List</code> of <code>String</code>'s
	 * representing annotated packages (f.i. at.workflow.webdesk.po.model) 
	 */
	public List<String> getAnnotatedPackages();
}
