package at.workflow.webdesk.po.licence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.ActionPermission;
import at.workflow.webdesk.po.model.LicenceDefinition;
import at.workflow.webdesk.po.model.PoAction;

public interface LicenceHelper {

	/**
	 * Add the allowed ActionNames of the passed LicenceDefinition to the global Map
	 * of Actions with the Value representing the number of users to which the action
	 * may be assigned.
	 * 
	 * @param licDef : LicenceDefinition to be processed
	 * @param actionLicenceMap: Map, where the key is the actionName and the value is a integer representing the max number of users: 
	 * @return the modified actionLicenceMap
	 */
	public Map<String, Integer> getAllowedActions(LicenceDefinition licDef,
			Map<String, Integer> actionLicenceMap);
	
	public List<LicenceInfo> getLicenceInfo();
	
	public List<LicenceInfo> getLicenceInfo(boolean retrieveInfosForAllLicenceDefinitions);
	
	public long getCurrentlyUsedVolumeAmount(LicenceDefinition ld);
	
	/** 
	 * Returns the number of persons which are currently allowed in the system to 
	 * execute the passed action (can be of any type!). If you pass a PoAction of type
	 * action, child configs will *NOT* be considered!
	 * 
	 * getNoOfUsersWithPermissionForAction
	 * getNoOfUsersWithPermissionForActionAndChildConfigs
	 * getMaxNoOfUsersLicencedForAction
	 */
	public int getNoOfUsersWithPermissionForAction(PoAction poAction);
	
	public int getNoOfUsersResultingOfPermission(ActionPermission actionPermission);

	/** 
	 * Returns the number of persons which are currently allowed in the system to 
	 * execute the passed action. If the action is configurable, this method will check all configs
	 * and will return the highest number of users over all variants of this action.
	 * 
	 * Will throw an IllegalArgumentException if you pass a PoAction of type CONFIG or PROCESSREF
	 */
	public int getNoOfUsersWithPermissionForActionAndChildConfigs(PoAction poAction);
	
	/** Return the maxiumum number of individuel users which can use this action (including child configs)
	 * without violating the licence */
	public int getMaxNoOfUsersLicencedForAction(PoAction poAction);
	
	public class LicenceInfo  {
		
		public LicenceInfo(String name, String description, String[] modules,  int maxInUse,
				int amount, Date expires) {
			super();
			this.name = name;
			this.description = description;
			this.modules = modules;
			this.maxInUse = maxInUse;
			this.amount = amount;
			this.expires = expires;
		}
		
		/** name of the licence **/
		private String name;
		
		/** description of the licence **/
		private String description;
		
		/** number indicating, how many of these licences are used at the moment */
		private int maxInUse;
		
		/** licenced volume, meaning the maximum usaable amount **/
		private int amount;
		
		/** list of modules being used by this licence */
		private String[] modules;
		
		/** Expiration date or null */
		private Date expires;
		
		/** Collection of LicenceInfosActionDetail objects containing licence-infos per action **/ 
		private Collection<LicenceInfoActionDetail> licenceInfoActionDetails = new ArrayList<LicenceInfoActionDetail>();
		
		public String getName() {
			return name;
		}
		public String getDescription() {
			return description;
		}
		public int getMaxInUse() {
			return maxInUse;
		}
		public int getAmount() {
			return amount;
		}
		public Collection<LicenceInfoActionDetail> getLicenceInfoActionDetails() {
			return licenceInfoActionDetails;
		}
		
		public void addLicenceInfoActionDetail(LicenceInfoActionDetail licenceInfoActionDetail) {
			this.licenceInfoActionDetails.add(licenceInfoActionDetail);
			if (licenceInfoActionDetail.getInUse() > this.maxInUse)
				this.maxInUse = licenceInfoActionDetail.getInUse();
		}
		public String[] getModules() {
			return modules;
		}
		public Date getExpires() {
			return expires;
		}
	}
	
	public class LicenceInfoActionDetail {
		
		public LicenceInfoActionDetail(PoAction action, Integer inUse,
				Integer oldAmount, Integer newAmount, Integer amount) {
			super();
			this.action = action;
			this.inUse = inUse;
			this.oldAmount = oldAmount;
			this.newAmount = newAmount;
			this.amount = amount;
		}
		
		private PoAction action;
		private Integer inUse;
		private Integer oldAmount;
		private Integer newAmount;
		private Integer amount;
		
		public PoAction getAction() {
			return action;
		}
		public Integer getInUse() {
			return inUse;
		}
		public Integer getOldAmount() {
			return oldAmount;
		}
		public Integer getNewAmount() {
			return newAmount;
		}
		public Integer getAmount() {
			return amount;
		}
	}
	
}
