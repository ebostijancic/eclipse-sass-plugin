package at.workflow.webdesk.po;

import java.util.Date;

import at.workflow.webdesk.tools.api.Historization;

/**
 * The purpose of this interface is to provide a service adapter 
 * that can handle service requests in a unique way, regardless of
 * the underlying implementation.
 * E.g. the {@link at.workflow.webdesk.po.HistorizationTimelineHelper}  
 * uses the <code>HistorizationServiceAdapterHelper</code> interface. 
 * <p>
 * Each Adapter must be initialized with enough
 * knowledge to be able to generate a new <code>Historization</code> 
 * object (there are many different implementations) that can be compared 
 * to existing <code>Historization</code> objects (of the same type); 
 * in other words it should be possible to run 
 * {@link #isStructurallyEqual(Historization, Historization)}
 * <p>
 * See also {@link at.workflow.webdesk.po.HistorizationTimelineHelper}  
 * 
 * 
 * @author hentner
 *
 */
public interface HistorizationServiceAdapter {
	
	
	/**
	 * Perform any unlinking and/or deleting as necessary.
	 * 
	 * It must mirror what saveObject method does.
	 * 
	 * @param historizationObject
	 */
	public void deleteObject(Historization historizationObject);
	
	
	/**
	 * Perform any postprocessing and persisting as needed,
	 * e.g. save it to db and/or add it to collections of link objects 
	 * of PoPerson and of PoGroup.
	 * 
	 * There are two ways about saving when defining e.g. new PoPerson:
	 * - build minimalistic PoPerson, save it, than build the linking objects and save them as well
	 * - build PoPerson and linking objects as the user requires them and save all when he presses save button (e.g. using cascading).
	 * 
	 * The main difference is the necessity to save PoPerson before assigning any person in the first scenario.
	 * 
	 * @param historizationObject
	 */
	public void saveObject(Historization historizationObject);
	
	
	/**
	 * Generates a <code>Historization</code> object 
	 * that is, fully initialised (besides the 
	 * dates, default values can be used as well) 
	 * 
	 * @param from initial value for validfrom field 
	 * @param to initial value for validto field
	 * @return a <code>Historization</code> object that is linked to both entities, e.g. to PoPerson and to PoGroup, 
	 * however PoPerson and PoGroup object shall not be linked to it
	 */
	public Historization generateEmptyObject(Date from, Date to);


	/**
	 * Compares two concrete 
	 * <code>Historization</code> objects 
	 * on "soft equality", which means that 
	 * they can be two different objects, 
	 * but represents the same associations. So they
	 * mainly differ due to the location on the timeline.
	 * TODO explain following: 
	 * one Group can have at most one parent at any time, in implementation only the parentGroup is compared
	 * one Person can be member of lot of groups, in implementation both person & group are compared 
	 * <p>
	 * A good example is the {@link at.workflow.webdesk.po.model.PoParentGroup}
	 * class which connects a {@link at.workflow.webdesk.po.model.PoGroup}
	 * with another {@link at.workflow.webdesk.po.model.PoGroup} to form a tree. So two 
	 * {@link at.workflow.webdesk.po.model.PoParentGroup} object would be the 
	 * structurally the same when they are linked with the same group objects. 
	 * <p> 
	 * 
	 * @param existingHistObject
	 * @param newObject
	 * @return <code>true</code> if <code>newObject</code> and 
	 * <code>existingHistObject</code> represents the same 
	 */
	public boolean isStructurallyEqual(Historization existingHistObject,
			Historization newObject);

	/**
	 * @param existingHistObject
	 * @return a copy of the <code>existingHistObject</code>, the <code>uid</code>
	 * shall not be set.
	 */
	public Historization copyHistObject(Historization existingHistObject);

}
