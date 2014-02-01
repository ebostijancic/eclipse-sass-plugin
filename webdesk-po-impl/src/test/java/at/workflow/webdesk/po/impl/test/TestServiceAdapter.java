package at.workflow.webdesk.po.impl.test;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.tools.api.Historization;

/**
 * A Service Adapter to use with an implementation of 
 * {@link at.workflow.webdesk.po.impl.test.TestHistorizationHelper}
 * 
 * 
 * @author hentner
 *
 */
public interface TestServiceAdapter {

	public List<? extends Historization> findEntries();

	/**
	 * @param string
	 * @return a <code>Historization</code> object. Keep in mind, that 
	 * the {@link #save(Historization)} method should be able to save the 
	 * generated object. Additionally this object will be passed (with 
	 * adapted dates) to the {@link #doAssignment(Historization, Date, Date)} method
	 */
	public Historization generateNew(String string);

	/**
	 * 
	 * This function makes an assignment. The <code>newObject</code>, which was probably
	 * generated via {@link #generateNew(String)} is the changing part. The other side 
	 * of the assignment is constant and should be considered in the constructor of 
	 * the <code>TestServiceAdapter</code> implementation. 
	 * 
	 * @param newObject
	 * @param from
	 * @param to
	 */
	public void doAssignment(Historization newObject, Date from, Date to);

	/**
	 * Saves the <code>Historization</code> object that was generated 
	 * with {@link #generateNew(String)}
	 * 
	 * @param newObject
	 */
	public void save(Historization newObject);

}
