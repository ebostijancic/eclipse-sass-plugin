package at.workflow.webdesk.po;

import java.util.Date;
import java.util.List;
import java.util.Set;

import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAction;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          wdDEV<br>
 * created at:       28.06.2007<br>
 * package:          at.workflow.webdesk.po<br>
 * compilation unit: PoLicenceActionService.java<br><br>
 * 
 * <p>
 * Interface for the PoLicenceActionService
 * </p>
 * 
 * 
 * <p>
 * The <code>PoLicenceActionService</code> implements functionality of the 
 * <code>PoActionService</code>, but is used in the <code>PoLicenceInterceptor</code>, which 
 * intercepts function calls to the <code>PoActionService</code>. The <code>PoActionService</code>
 * uses the <code>PoOrganisationService</code>, which in return is intercepted by the
 * <code>PoLicenceInterceptor</code>, thus the PoActionService itself can't be used inside
 * the <code>PoLicenceInterceptor</code>.
 *</p>
 *
 *
 * @see at.workflow.webdesk.po.PoActionService
 * @see at.workflow.webdesk.po.PoOrganisationService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.PoModuleService
 * @see at.workflow.webdesk.po.PoFileService
 * @see at.workflow.webdesk.po.PoLogService
 * @see at.workflow.webdesk.po.PoLanguageService
 * @see at.workflow.webdesk.po.PoBeanPropertyService
 * @see at.workflow.webdesk.po.PoJobService

 *
 */
public interface PoLicenceActionService {

	/** returns a list of actions names hosted by the passed module (f.i. 'po') */
	List<String> findActionNamesOfModule(String string);

    /**
     * @param action
     * @param date
     * @return a list of PoAPermissionBase objects that are assigned to the given action and 
     * are valid at the given date.
     */
	List<PoAPermissionBase> findAllPermissionsForActionF(PoAction action, Date date);

	/** returns a set of person UIDs which correspond to the passed action permission */
	Set<String> findAssignedPersonsOfAction(PoAPermissionBase apb);

	/**
	 * @param name
	 * @return a <code>PoAction</code> object, if an action with the given <code>name</code> and 
	 * <code>type</code> was found. <code>null</code> otherwise.
	 */
	PoAction findActionByName(String string);
	
	/** returns a list of all currently valid actions */
	List<PoAction> findAllCurrentActions();

}
