/*
 * Created on 01.07.2005
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import at.workflow.webdesk.po.CompetenceTarget;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoActionDAO;
import at.workflow.webdesk.po.daos.PoRoleCompetenceDAO;
import at.workflow.webdesk.po.daos.PoRoleDAO;
import at.workflow.webdesk.po.daos.PoRoleDeputyDAO;
import at.workflow.webdesk.po.daos.PoRoleHolderDAO;
import at.workflow.webdesk.po.daos.PoRoleHolderDynamicDAO;
import at.workflow.webdesk.po.daos.PoRoleHolderGroupDAO;
import at.workflow.webdesk.po.daos.PoRoleHolderPersonDAO;
import at.workflow.webdesk.po.impl.util.OrgAdminHelper;
import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAPermissionRole;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOptions;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceAll;
import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.po.model.PoRoleCompetenceClient;
import at.workflow.webdesk.po.model.PoRoleCompetenceGroup;
import at.workflow.webdesk.po.model.PoRoleCompetencePerson;
import at.workflow.webdesk.po.model.PoRoleDeputy;
import at.workflow.webdesk.po.model.PoRoleHolderDynamic;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.po.model.PoRoleHolderLink;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;
import at.workflow.webdesk.tools.api.UserAuthorizationService;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * Implementation of the PoRoleService
 * 
 * FIXME: contains a lot of 'almost' duplicated code....
 * 
 * @author: hentner, ggruber, dzuban
 *
 *
 */
public class PoRoleServiceImpl implements PoRoleService, UserAuthorizationService {

	
	// hentner/ why not using services, we miss caching!
	// e.g. getHierarchycalGroup
	
	private PoRoleDAO roleDAO;
	private PoRoleDeputyDAO roleDeputyDAO;
	private PoRoleHolderDAO roleHolderDAO;
	private PoRoleHolderGroupDAO roleHolderGroupDAO;
	private PoRoleHolderPersonDAO roleHolderPersonDAO;
	private PoRoleCompetenceDAO roleCompetenceDAO;
	private PoRoleHolderDynamicDAO roleHolderDynamicDAO;

	private PoActionDAO actionDAO;
	
	PoOptions options;

	PoOrganisationService orgService;
	
	private Logger logger = Logger.getLogger(PoRoleServiceImpl.class);

	public void setActionDAO(PoActionDAO actionDAO) {
		this.actionDAO = actionDAO;
	}

	public void setRoleDAO(PoRoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}

	@Override
	public void saveRole(PoRole role) {
		if (role.getValidfrom() == null)
			role.setValidfrom(new Date());
		if (role.getValidto() == null)
			role.setValidto(PoConstants.getInfDate());
		if (role.getRoleType() == 0) {
			role.setRoleType(PoRole.NORMAL_ROLE);
		}
 		roleDAO.save(role);
	}
	
	@Override
	public void saveRoleHolderPerson(PoRoleHolderPerson rhp) {
		roleHolderPersonDAO.save(rhp);
	}

	@Override
	public PoRole getRole(String uid) {
		return roleDAO.get(uid);
	}

	@Override
	public List<PoRole> loadAllRoles() {
		return roleDAO.loadAllRoles(new Date());
	}

	@Override
	public List<PoRole> loadAllRoles(PoClient client) {
		return roleDAO.loadAllRoles(client, new Date());
	}

	@Override
	public List<PoRole> findRolesOfPerson(PoPerson person) {
		return roleDAO.findRolesOfPerson(person, new Date());
	}
	
	@Override
	public List<PoRole> findRolesOfPerson(PoPerson person, Date date) {
		if (date == null) 
			date = new Date();
		return roleDAO.findRolesOfPerson(person, date);
	}
	
	@Override
	public List<PoRole> findDirectlyLinkedRolesOfPerson(PoPerson person) {
		return findDirectlyLinkedRolesOfPerson(person, null);
	}
	
	@Override
	public List<PoRole> findDirectlyLinkedRolesOfPerson(PoPerson person, Date date) {
		if (date==null)
			date = new Date();
		return roleDAO.findDirectlyLinkedRolesOfPerson(person, date);
	}
	

	@Override
	public List<PoRole> findRoleByName(String key) {
		return roleDAO.findRoleByName(key, new Date());
	}

	@Override
	public PoRole findRoleByName(String key, PoClient client) {
		return roleDAO.findRoleByName(key, client, new Date());
	}
	
	@Override
	public PoRole findRoleByNameAndGivenOrNullClient(String roleName, PoClient client) {
		assert roleName != null && client != null;
		PoRole role = findRoleByName(roleName, client);
		if (role != null)
			return role;
		return roleDAO.findRoleByNameAndNullClient(roleName);
	}

	/** This logic originally was in PtmEditProjectActionHandler. {@inheritDoc} */ 
	@Override
	public boolean hasPersonRole(PoPerson person, String roleName) {
		assert person != null && roleName != null && roleName.length() > 0;
		
		PoRole role = findRoleByNameAndGivenOrNullClient(roleName, person.getClient());
		if (role == null)	{
			logger.warn("The role named "+roleName+" does not exist as role, so can't check if person "+person.getFullName()+" has it.");
			return false;
		}
		List<PoRole> roles = findRolesOfPerson(person);
		return roles.contains(role);	// when this user has that role, it is allowed to create projects
	}
	
	@Override
	public List<PoRole> findRoleByName(String key, Date referenceDate) {
		return roleDAO.findRoleByName(key, referenceDate);
	}

	@Override
	public PoRole findRoleByName(String key, PoClient client, Date referenceDate) {
		return roleDAO.findRoleByName(key, client, referenceDate);
	}

	@Override
	public void deleteRole(PoRole role, boolean force) {
		GregorianCalendar past = new GregorianCalendar();
		past.add(GregorianCalendar.DAY_OF_MONTH, -1);
		Collection<PoRoleCompetenceBase> l = roleCompetenceDAO.findRoleCompetenceF(role, new Date());
		Collection<PoAPermissionRole> l_acts = actionDAO.findActionPermissionsOfRoleF(role,new Date());
		Iterator<PoRoleCompetenceBase> i_rh = l.iterator();
		Iterator<PoAPermissionRole> i_acts = l_acts.iterator();

		// look if there are dependant objects
		if (l.size() > 0 || l_acts.size() > 0)
			if (force) {
				// force deletion of dependant objects
				while (i_rh.hasNext()) {
					Object o = i_rh.next();
					if (o instanceof PoRoleCompetenceAll) {
						PoRoleCompetenceAll rh = (PoRoleCompetenceAll) o;

						Collection<PoRoleHolderPerson> c_rhp = rh.getRoleHolderPersons();
						// delete role holder persons
						if (c_rhp != null) {
							Iterator<PoRoleHolderPerson> i_rhp = c_rhp.iterator();
							while (i_rhp.hasNext()) {
								PoRoleHolderPerson rhp = i_rhp.next();
								rhp.setValidto(past.getTime());
								roleHolderPersonDAO.save(rhp);
							}
						}
						// delete role holder groups
						Collection<PoRoleHolderGroup> c_rhg = rh.getRoleHolderGroups();
						if (c_rhg != null) {
							Iterator<PoRoleHolderGroup> i_rhg = c_rhg.iterator();
							while (i_rhg.hasNext()) {
								PoRoleHolderGroup rhg = i_rhg.next();
								rhg.setValidto(past.getTime());
								roleHolderGroupDAO.save(rhg);
							}
						}
						// delete role holder dynamics
						Collection<PoRoleHolderDynamic> c_rhd = rh.getRoleHolderDynamics();
						if (c_rhd != null) {
							Iterator<PoRoleHolderDynamic> i_rhd = c_rhd.iterator();
							while (i_rhd.hasNext()) {
								PoRoleHolderDynamic rhd = i_rhd.next();
								rhd.setValidto(past.getTime());
								roleHolderDynamicDAO.save(rhd);
							}
						}
						rh.setValidto(past.getTime());
						roleCompetenceDAO.save(rh);
					}
					if (o instanceof PoRoleCompetenceClient) {
						PoRoleCompetenceClient rhcc = (PoRoleCompetenceClient) o;
						rhcc.setValidto(past.getTime());
						roleCompetenceDAO.save(rhcc);
					}
					if (o instanceof PoRoleCompetenceGroup) {
						PoRoleCompetenceGroup rhcg = (PoRoleCompetenceGroup) o;
						rhcg.setValidto(past.getTime());
						roleCompetenceDAO.save(rhcg);
					}
					if (o instanceof PoRoleCompetencePerson) {
						PoRoleCompetencePerson rhcp = (PoRoleCompetencePerson) o;
						rhcp.setValidto(past.getTime());
						roleCompetenceDAO.save(rhcp);
					}
				}
				// delete all permissions
				while (i_acts.hasNext()) {
					PoAPermissionBase perm = i_acts.next();

					// if permission starts in future, delete hard
					// if permission is actual or historical -> delete soft
					if (perm.getValidfrom().after(new Date()))
						actionDAO.deleteAPermission(perm);
				}
			} else
				throw new PoRuntimeException(
						PoRuntimeException.WARNING_DELETE_ROLE);

		role.setValidto(past.getTime());
		roleDAO.save(role);
		
		if (logger.isInfoEnabled())
			logger.info("Role " + role.getName() + " was deleted."); 
	}

	@Override
	public void deleteAndFlushRole(PoRole role) {
		
		if (role.getRoleHolders() != null) {
			
			// delete competences and deputies
			Iterator<PoRoleCompetenceBase> i_rh = role.getRoleHolders().iterator();
			while (i_rh.hasNext()) {
				PoRoleCompetenceBase rh = i_rh.next();
				Iterator<PoRoleHolderPerson> i_rhp = rh.getRoleHolderPersons().iterator();
				while (i_rhp.hasNext()) {
					PoRoleHolderPerson rhp = i_rhp.next();
					// delete deputy
					if (rhp.getDeputy()!=null) 
						deleteAndFlushRoleDeputy(rhp.getDeputy());
					roleHolderPersonDAO.delete(rhp);
					
				}
				roleCompetenceDAO.delete(rh);
				i_rh.remove();
			}
		}
		
		// FIXME: we are not deleting permissions here ..
		if (role.getClient() != null)
			role.getClient().getRoles().remove(role);
		roleDAO.delete(role);
	}

	private void deleteAndFlushRoleDeputy(PoRoleDeputy deputy) {
		roleDeputyDAO.delete(deputy);
		
	}

	/* ROLE HOLDER **************************************************** */

	@Override
	public PoRoleCompetenceBase getRoleCompetenceBase(String uid) {
		return roleCompetenceDAO.get(uid);
	}
	
	@Override
	public PoRoleCompetenceAll getRoleCompetenceAll(String uid) {
		return roleCompetenceDAO.getRoleCompetenceAll(uid);
	}

	@Override
	public PoRoleHolderPerson getRoleHolderPerson(String uid) {
		return roleHolderPersonDAO.get(uid);
	}

	@Override
	public PoRoleHolderGroup getRoleHolderGroup(String uid) {
		return roleHolderGroupDAO.get(uid);
	}

	@Override
	public void saveRoleCompetence(PoRoleCompetenceBase roleCompetence) {
		roleCompetenceDAO.save(roleCompetence);
	}

	@Override
	public boolean isRoleCompetencePerson(PoRoleCompetenceBase rhb) {
		return rhb instanceof PoRoleCompetencePerson;
	}

	@Override
	public boolean isRoleCompetenceGroup(PoRoleCompetenceBase rhb) {
		return rhb instanceof PoRoleCompetenceGroup;
	}

	@Override
	public boolean isRoleCompetenceClient(PoRoleCompetenceBase rhb) {
		return rhb instanceof PoRoleCompetenceClient;
	}
	
	@Override
	public boolean isRoleCompetenceAll(PoRoleCompetenceBase rhb) {
		return rhb instanceof PoRoleCompetenceAll;
	}

	@Override
	public void assignRole(PoRole role, PoPerson person, int ranking) {
		assignRole(role, person, new Date(), null, ranking);
	}

	@Override
	public void assignRole(PoRole role, PoGroup group, int ranking) {
		assignRole(role, group, new Date(), null, ranking);
	}

	
	@Override
	public void assignRole(PoRole role, PoPerson person, Date validFrom,
			Date validTo, int ranking) {
		if (validFrom == null)
			validFrom = new Date();
		if (validFrom.before(new Date()))
			validFrom.setTime(new Date().getTime());
		if (validTo==null)
			validTo = PoConstants.getInfDate();

		assignRoleReally(role, person, validFrom, validTo, ranking, false);
		// check if a role deputy for the given role and person exists,
		// if true, then do the same thing for the deputy 
		
		Iterator<PoRoleDeputy> depI = findRoleDeputiesOfPersonF(person, role, validFrom).iterator();
		while (depI.hasNext()) {
			PoRoleDeputy rd = depI.next();
			if (DateTools.doesOverlap(rd.getValidfrom(), rd.getValidto(), validFrom, validTo)) {
				Date cValidFrom = validFrom;
				Date cValidTo = validTo;
				if (cValidFrom.before(rd.getValidfrom()))
					cValidFrom = rd.getValidfrom();
				if (cValidTo.after(rd.getValidto()))
					cValidTo = rd.getValidto();
				// if an existing roleholderperson object is used, the deputy is already set
				PoRoleHolderPerson rhp = assignRoleReally(role, rd.getDeputy(),
						cValidFrom, cValidTo, ++ranking, true);
				rhp.setDeputy(rd);
				roleHolderPersonDAO.save(rhp);
			}
		}
	}
	
	
	private PoRoleHolderPerson assignRoleReally(PoRole role, PoPerson person, Date validFrom,
			Date validTo, int ranking, boolean isDeputy) {
		
		PoRoleHolderPerson genRhp=null;
//		only one role competence should exist at one time
// 		the date is not really important ... 
		List<PoRoleCompetenceAll> l_rh = roleCompetenceDAO.findRoleCompetenceAllF(role, validFrom);  
		
		// if more than one rolecompetence exists, the data is not correct 
		// ev. we should correct the data at this place! 
		if (l_rh.size() > 1) {
			rearrangeDataToEnsureConsistency(l_rh);
		}
			
		if (l_rh.size() == 1) {
			
			// exactly one role holder exists
			PoRoleCompetenceAll rh = l_rh.get(0);
			
			List<PoRoleHolderPerson> overlappedRoleHolder = getOverlappedRoleHolderPersons(person, rh, validFrom, validTo, isDeputy);
			
			if (overlappedRoleHolder.size()==0) {
				PoRoleHolderPerson rhp = new PoRoleHolderPerson();
				rhp.setRoleHolder(rh);
				rhp.setPerson(person);
				rhp.setRanking(ranking); // not yet implemented
				rhp.setValidfrom(validFrom);
				if (validTo == null)
					rhp.setValidto(PoConstants.getInfDate());
				else
					rhp.setValidto(validTo);
				
				rh.getRoleHolderPersons().add(rhp);
				roleHolderPersonDAO.save(rhp);
				role.addRoleHolder(rh);
				genRhp = rhp;
			} else {
				List<PoRoleHolderPerson> toRemove = new ArrayList<PoRoleHolderPerson>();
				genRhp = rearrangeRoleHolderPersons(overlappedRoleHolder, validFrom, validTo, toRemove); 
				rh.getRoleHolderPersons().removeAll(toRemove);
				saveRoleCompetence(rh);
			}
		} else { // no role holder exists
			PoRoleCompetenceAll rh = new PoRoleCompetenceAll();
			rh.setRole(role);
			rh.setType(PoConstants.ROLE_HOLDER_BASE_ALL.toString());
			rh.setValidfrom(new Date());
			// validto is only used to simulate deletion 
			rh.setValidto(PoConstants.getInfDate());
			PoRoleHolderPerson rhp = new PoRoleHolderPerson();
			rhp.setRoleHolder(rh);
			rhp.setPerson(person);
			rhp.setRanking(ranking);
			rhp.setValidfrom(validFrom);
			rhp.setValidto(validTo);
			
			rh.getRoleHolderPersons().add(rhp); // ev. NullPointer

			roleCompetenceDAO.save(rh);
			roleHolderPersonDAO.save(rhp);
			role.addRoleHolder(rh);
			genRhp = rhp;
		}
		return genRhp;
	}

	
	/**
	 * 
	 * <p>This function is used when the following situation occurs.</p> 
	 * <p>
	 * More than one instance of <code>PoRoleCompetenceBase</code> (there are 
	 * three different subclasses) with the same comptence object 
	 * (this can be none, which means <code>all</code>, a <code>PoPerson</code>
	 * permission or a <code>PoGroup</code> permission. The validity of 
	 * the <code>PoRoleCompetenceBase</code> object does not really matter, 
	 * as this is managed via the link to a person or a group.
	 * </p>
	 * <p>
	 * The <code>PoRoleCompetenceBase</code> objects are valid now 
	 * or will become valid in the future. 
	 * </p>
	 * <p>The result of this function is a <code>merge</code> of the data. In otherwords
	 * the earliest <code>validfrom</code> and the latest <code>validto</code> is extracted,
	 * the last entry of the <code>PoRoleCompetenceBase</code> object is choosen, these values
	 * will be set and the object is stored. All other objects in the list will be deleted. 
	 * The links to the <code>PoRoleHolderPerson</code> and <code>PoRoleHolderGroup</code> objects
	 * will be taken over.
	 * </p>
	 *  
	 * @param l_rh
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void rearrangeDataToEnsureConsistency(List l_rh) {
		logger.warn("Found inconsistent data. Try to rearrange data to ensure consistency.");
		
		// found multiple entries -> rearrange data!
		PoRoleCompetenceBase rca = null;
		Iterator<PoRoleCompetenceBase> l_rhI = l_rh.iterator();
		// find the entry with the latest validto
		while (l_rhI.hasNext()) {
			PoRoleCompetenceBase rcAll = l_rhI.next();
			if (rca==null || rcAll.getValidto().after(rca.getValidto()))
				rca = rcAll;
		}
		
		List<PoRoleCompetenceBase> toRemove = new ArrayList<PoRoleCompetenceBase>();
		l_rhI = l_rh.iterator();
		while (l_rhI.hasNext()) {
			PoRoleCompetenceBase rcAll = l_rhI.next();
			if (!rcAll.equals(rca)) {
				for (PoRoleHolderGroup rhg : rcAll.getRoleHolderGroups()) {
					rhg.setRoleHolder(rca);
					roleHolderGroupDAO.save(rhg);
				}
				rcAll.getRoleHolderGroups().clear();
				
				for (PoRoleHolderPerson rhp : rcAll.getRoleHolderPersons()) {
					rhp.setRoleHolder(rca);
					roleHolderPersonDAO.save(rhp);
				}
				rcAll.getRoleHolderPersons().clear();
				
				for (PoRoleHolderDynamic rhd : rcAll.getRoleHolderDynamics()) {
					rhd.setRoleHolder(rca);
					saveRoleHolderDynamic(rhd);
				}
				rcAll.getRoleHolderDynamics().clear();
				
				toRemove.add(rcAll);
			}
		}
		
		// delete the old PoRoleCompetenceAll objects 
		l_rh.clear();
		for (PoRoleCompetenceBase delRca : toRemove) {
			roleCompetenceDAO.delete(delRca);
		}
		
		// add the resulting PoRoleCompetenceAll object
		l_rh.add(rca);
		
		if (logger.isDebugEnabled())
			logger.debug("Consistency for Role Competencies ensured.");
		
	}

	/**
	 * <p>
	 * This function rearranges the <code>List</code> of <code>PoRoleHolderPerson</code> in such
	 * a way that no overlapping exists. Thus when there are three entries in the list of 
	 * <code>overlappedRoleHolder</code>'s, and we assume that <code>min</code> is the earliest 
	 * <code>validfrom</code> and <code>max</code> is the latest <code>validto</code> then 
	 * two of the entries will be deleted and one will be filled with <code>min</code> and 
	 * <code>max</code>.
	 * 
	 * 
	 * @param overlappedRoleHolder a <code>List</code> of <code>PoRoleHolderPerson</code> objects.
	 * @param validFrom
	 * @param validTo
	 * @param toRemove a <code>List</code> of deleted <code>PoRoleHolderPerson</code> objects. Do not forget 
	 * to remove this object from the other side, otherwise they will be resaved by cascade. An empty <code>List</code>
	 * has to be passed. 
	 */
	private PoRoleHolderPerson rearrangeRoleHolderPersons(List<PoRoleHolderPerson> overlappedRoleHolder, Date validFrom, Date validTo, List<PoRoleHolderPerson> toRemove) {
		
		PoRoleHolderPerson rhp = null;
		// there is an overlapping -> set timeranges (the last entry will be set and stored.
		Iterator<PoRoleHolderPerson> overlappedI = overlappedRoleHolder.iterator();
		while (overlappedI.hasNext()) {
			rhp = overlappedI.next();
			if (rhp.getValidfrom().before(validFrom))
				validFrom = rhp.getValidfrom();
			
			if (rhp.getValidto().after(validTo))
				validTo = rhp.getValidto();
			
			// delete all entries except the last one
			if (overlappedI.hasNext()) {
				roleHolderPersonDAO.delete(rhp);
				toRemove.add(rhp);
				if (rhp.getDeputy()!=null)
					rhp.getDeputy().getRoleHolderPersons().remove(rhp);
			}
		}
		
		// adapt the last entry and store it.
		rhp.setValidfrom(validFrom);
		rhp.setValidto(validTo);
		roleHolderPersonDAO.save(rhp);
		return rhp;
	}
	
	/**
	 * <p>
	 * This function rearranges the <code>List</code> of <code>PoRoleHolderDynamic</code> in such
	 * a way that no overlapping exists. Thus when there are three entries in the list of 
	 * <code>overlappedRoleHolder</code>'s, and we assume that <code>min</code> is the earliest 
	 * <code>validfrom</code> and <code>max</code> is the latest <code>validto</code> then 
	 * two of the entries will be deleted and one will be filled with <code>min</code> and 
	 * <code>max</code>.
	 * 
	 * 
	 * @param overlappedRoleHolder a <code>List</code> of <code>PoRoleHolderDynamic</code> objects.
	 * @param validFrom
	 * @param validTo
	 * @param toRemove a <code>List</code> of deleted <code>PoRoleHolderDynamic</code> objects. Do not forget 
	 * to remove this object from the other side, otherwise they will be resaved by cascade. An empty <code>List</code>
	 * has to be passed. 
	 */
	private PoRoleHolderDynamic rearrangeRoleHolderDynamics(List<PoRoleHolderDynamic> overlappedRoleHolder, Date validFrom, Date validTo, List<PoRoleHolderDynamic> toRemove) {
		
		PoRoleHolderDynamic rhd = null;
		// there is an overlapping -> set timeranges (the last entry will be set and stored.
		Iterator<PoRoleHolderDynamic> overlappedI = overlappedRoleHolder.iterator();
		while (overlappedI.hasNext()) {
			rhd = overlappedI.next();
			if (rhd.getValidfrom().before(validFrom))
				validFrom = rhd.getValidfrom();
			
			if (rhd.getValidto().after(validTo))
				validTo = rhd.getValidto();
			
			// delete all entries except the last one
			if (overlappedI.hasNext()) {
				roleHolderDynamicDAO.delete(rhd);
				toRemove.add(rhd);
			}
		}
		
		// adapt the last entry and store it.
		rhd.setValidfrom(validFrom);
		rhd.setValidto(validTo);
		roleHolderDynamicDAO.save(rhd);
		return rhd;
	}
	
	

	/**
	 * <p>
	 * This function rearranges the <code>List</code> of <code>PoRoleHolderGroup</code> in such
	 * a way that no overlapping exists. Thus when there are three entries in the list of 
	 * <code>overlappedRoleHolder</code>'s, and we assume that <code>min</code> is the earliest 
	 * <code>validfrom</code> and <code>max</code> is the latest <code>validto</code> then 
	 * two of the entries will be deleted and one will be filled with <code>min</code> and 
	 * <code>max</code>.
	 * 
	 * 
	 * @param overlappedRoleHolder a <code>List</code> of <code>PoRoleHolderGroup</code> objects.
	 * @param validFrom
	 * @param validTo
	 * @return a <code>List</code> of deleted <code>PoRoleHolderGroup</code> objects. Do not forget 
	 * to remove this object from the other side, otherwise they will be resaved by cascade.
	 */
	private List<PoRoleHolderGroup> rearrangeRoleHolderGroups(List<PoRoleHolderGroup> overlappedRoleHolder, Date validFrom, Date validTo) {
		List<PoRoleHolderGroup> res  = new ArrayList<PoRoleHolderGroup>();
		
		PoRoleHolderGroup rhg = null;
		// there is an overlapping -> set timeranges (the last entry will be set and stored.
		Iterator<PoRoleHolderGroup> overlappedI = overlappedRoleHolder.iterator();
		while (overlappedI.hasNext()) {
			rhg = overlappedI.next();
			if (rhg.getValidfrom().before(validFrom))
				validFrom = rhg.getValidfrom();
			
			if (rhg.getValidto().after(validTo))
				validTo = rhg.getValidto();
			
			if (overlappedI.hasNext()) {
				roleHolderGroupDAO.delete(rhg);
				res.add(rhg);
			}
		}
		
		// adapt the last entry and store it.
		rhg.setValidfrom(validFrom);
		rhg.setValidto(validTo);
		roleHolderGroupDAO.save(rhg);
		return res;
	}

	/**
	 * @param person
	 * @param rh
	 * @param validFrom
	 * @param validTo
	 * @return a <code>List</code> of overlapping <code>PoRoleHolderPerson</code> objects.
	 */
	private List<PoRoleHolderPerson> getOverlappedRoleHolderPersons(PoPerson person, PoRoleCompetenceBase rh, Date validFrom, Date validTo, boolean isDeputy) {
		Iterator<PoRoleHolderPerson> l = rh.getRoleHolderPersons().iterator(); // this could be many under special circumstances ! 
		List<PoRoleHolderPerson> overlappedRhp = new ArrayList<PoRoleHolderPerson>();
		
		while (l.hasNext()) {
			PoRoleHolderPerson rhp = l.next();
			if (isDeputy && rhp.getDeputy()!=null 
					|| !isDeputy && rhp.getDeputy()==null)
				if (rhp.getPerson().equals(person) && doesOverlap(rhp,validFrom, validTo)) {
					overlappedRhp.add(rhp);
				}
		}
		return overlappedRhp;
	}
	
	
	/**
	 * @param person
	 * @param rh
	 * @param validFrom
	 * @param validTo
	 * @return a <code>List</code> of overlapping <code>PoRoleHolderPerson</code> objects.
	 */
	private List<PoRoleHolderDynamic> getOverlappedRoleHolderDynamics(int type, PoRoleCompetenceBase rh, Date validFrom, Date validTo) {
		Iterator<PoRoleHolderDynamic> l = rh.getRoleHolderDynamics().iterator(); // this could be many under special circumstances ! 
		List<PoRoleHolderDynamic> overlappedRhd = new ArrayList<PoRoleHolderDynamic>();
		
		while (l.hasNext()) {
			PoRoleHolderDynamic rhd = l.next();
			if (rhd.getRoleHolderType() == type && doesOverlap(rhd,validFrom, validTo)) {
					overlappedRhd.add(rhd);
			}
		}
		return overlappedRhd;
	}
	
	

	/**
	 * @param person
	 * @param rh
	 * @param validFrom
	 * @param validTo
	 * @return a <code>List</code> of overlapping <code>PoRoleHolderPerson</code> objects.
	 */
	private List<PoRoleHolderGroup> getOverlappedRoleHolderGroups(PoGroup group, PoRoleCompetenceBase rh, Date validFrom, Date validTo) {
		Iterator<PoRoleHolderGroup> l = rh.getRoleHolderGroups().iterator();
		List<PoRoleHolderGroup> overlappedRhg = new ArrayList<PoRoleHolderGroup>();
		
		while (l.hasNext()) {
			PoRoleHolderGroup rhg = l.next();
			if (rhg.getGroup().equals(group) && DateTools.doesOverlap(rhg.getValidfrom(), rhg.getValidto(),validFrom, validTo)) {
				overlappedRhg.add(rhg);
			}
		}
		return overlappedRhg;
	}
	
	
	
	

	/*
	 * rhp 		+----------------------o
	 * 
	 * (1)  +------------------------------o 	(both outside)
	 * (2)  +--------------o					(vf. outside, vt. inside)
	 * (3)      +--------------------------o    (vf. equals, vt. outside)
	 * (4)      +----------o					(vf. equals, vt. inside)
	 * (5)      +----------------------o		(vf. equals, vt. equals)
	 * (6)              +--------------o		(vf. inside, vt. equals)
	 * (7)              +------------------o	(vf. inside, vt. outside)
	 * (8)  +--------------------------o		(vf. outside, vt. equals)
	 * (9)          +-----------o				(vf. inside, vt. inside)
	 * (10) +---o								(vf. outside, vt.equals start)
	 * (11)                            +---o	(vf. equals end, vt. outside
	 * 
	 * 
	 */
	private boolean doesOverlap(PoRoleHolderPerson rhp, Date vf, Date vt) {
		return DateTools.doesOverlap(rhp.getValidfrom(), rhp.getValidto(), vf, vt);
	}
		
	private boolean doesOverlap(PoRoleHolderDynamic rhd, Date vf, Date vt) {
		return DateTools.doesOverlap(rhd.getValidfrom(), rhd.getValidto(), vf, vt);
	}
		

	@Override
	public void assignRole(PoRole role, PoGroup group, Date validFrom,
			Date validTo, int ranking) {
		if (validFrom == null)
			validFrom = new Date();
		else
			if (validFrom.before(new Date()))
				validFrom.setTime(new Date().getTime());
		// find all PoRoleCompetenceAll objects of role 
		List<PoRoleCompetenceAll> l_rh = roleCompetenceDAO.findRoleCompetenceAllF(role, validFrom);  
		
		// if more than one exists -> rearrange Data 
		if (l_rh.size() > 1)
			rearrangeDataToEnsureConsistency(l_rh);
		
		if (l_rh.size() == 1) {
			// exactly one role holder exists
			PoRoleCompetenceAll rh = l_rh.get(0);
			// check if the role holder overlaps
			List<PoRoleHolderGroup> overlappedRoleHolder = getOverlappedRoleHolderGroups(group, rh, validFrom, validTo);
			
			// if nothing overlaps, store the new PoRoleHolderGroup
			if (overlappedRoleHolder.size()==0) {
				PoRoleHolderGroup rhg = new PoRoleHolderGroup();
				rhg.setRoleHolder(rh);
				rhg.setGroup(group);
				rhg.setRanking(ranking); // not yet implemented
				rhg.setValidfrom(validFrom);
				if (validTo == null)
					rhg.setValidto(PoConstants.getInfDate());
				else
					rhg.setValidto(validTo);
				rh.getRoleHolderGroups().add(rhg);
				roleHolderGroupDAO.save(rhg);
				role.addRoleHolder(rh);
			} else
				rh.getRoleHolderGroups().removeAll(rearrangeRoleHolderGroups(overlappedRoleHolder, validFrom, validTo));
			
		} else { // no role holder exists
			PoRoleCompetenceAll rh = new PoRoleCompetenceAll();
			rh.setRole(role);
			rh.setType(PoConstants.ROLE_HOLDER_BASE_ALL.toString());
			rh.setValidfrom(new Date());
			if (validTo == null)
				rh.setValidto(PoConstants.getInfDate());
			else
				rh.setValidto(validTo);
			PoRoleHolderGroup rhg = new PoRoleHolderGroup();
			rhg.setRoleHolder(rh);
			rhg.setGroup(group);
			rhg.setRanking(ranking);
			rhg.setValidfrom(validFrom);
			if (validTo == null)
				rhg.setValidto(PoConstants.getInfDate());
			else
				rhg.setValidto(validTo);
			rh.getRoleHolderGroups().add(rhg); // ev. NullPointer
			roleCompetenceDAO.save(rh);
			roleHolderGroupDAO.save(rhg); // ev. Reihenfolge
			role.addRoleHolder(rh);
		}
	}

	public void assignRoleWithPersonCompetence(PoRole role,
			PoGroup competenceGroup, PoPerson controlledPerson, int ranking) {
		assignRoleWithPersonCompetence(role, competenceGroup,
				controlledPerson, new Date(), null, ranking);
	}

	
	@Override
	public void assignRoleWithGroupCompetence(PoRole role,
			PoPerson competencePerson, PoGroup controlledGroup, Date validFrom,
			Date validTo, int ranking) {
		if (validFrom == null)
			validFrom = new Date();
		else
			if (validFrom.before(new Date()))
				validFrom.setTime(new Date().getTime());
		if (validTo ==null)
			validTo = PoConstants.getInfDate();
		
		assignRoleWithGroupCompetenceReally(role, competencePerson, 
				controlledGroup, validFrom, validTo, ranking,false);
		
		// check deputies -> if one was found, add competence to deputy as well 
		for (PoRoleDeputy rd : findRoleDeputiesOfPerson(competencePerson, role, validFrom)) {
			if (DateTools.doesOverlap(rd.getValidfrom(), rd.getValidto(), validFrom, validTo)) {
				Date cValidFrom = validFrom;
				Date cValidTo = validTo;
				if (cValidFrom.before(rd.getValidfrom()))
					cValidFrom = rd.getValidfrom();
				if (cValidTo.after(rd.getValidto()))
					cValidTo = rd.getValidto();
				
				// -> if an existing link is used, the role holder is set
				PoRoleHolderPerson rhp = assignRoleWithGroupCompetenceReally(role, rd.getDeputy(),
						controlledGroup, cValidFrom, cValidTo, ++ranking, true);
				rhp.setDeputy(rd);
				saveRoleHolderPerson(rhp);
			}
		}
	}
	
	private PoRoleHolderPerson assignRoleWithGroupCompetenceReally(PoRole role,
			PoPerson competencePerson, PoGroup controlledGroup, Date validFrom,
			Date validTo, int ranking, boolean isDeputy) {
		if (controlledGroup==null)
			throw new PoRuntimeException("Cannot assign a null group");
		
		PoRoleHolderPerson genRhp=null;
		
        List<PoRoleCompetenceGroup> l = roleCompetenceDAO.findRoleCompetenceGroupWithCompetence4GroupF(role,
				controlledGroup, validFrom);
		// an exception is thrown if a roleholder for the role, the person and
		// the group, which is currently valid
		// is found

		if (l.size() > 1)
			rearrangeDataToEnsureConsistency(l);
		
		if (l.size() == 1) {
			
			PoRoleCompetenceGroup rh = l.get(0);
			
			List<PoRoleHolderPerson> overlappedRoleHolder = getOverlappedRoleHolderPersons(competencePerson, rh, validFrom, validTo, isDeputy);
			
			if(overlappedRoleHolder.size()==0) {
				PoRoleHolderPerson rhp = new PoRoleHolderPerson();
				rhp.setRoleHolder(rh);
				rhp.setPerson(competencePerson);
				rhp.setRanking(ranking);
				rhp.setValidfrom(validFrom);
				rhp.setValidto(validTo);
				rh.getRoleHolderPersons().add(rhp);
				roleHolderPersonDAO.save(rhp);
				genRhp = rhp;
			} else {
				List<PoRoleHolderPerson> toRemove = new ArrayList<PoRoleHolderPerson>();
				genRhp = rearrangeRoleHolderPersons(overlappedRoleHolder, validFrom, validTo, toRemove);
				rh.getRoleHolderPersons().removeAll(toRemove);
			}
		} else {
			PoRoleCompetenceGroup rh = generateRoleCompetenceGroup(role, controlledGroup);
			PoRoleHolderPerson rhp = new PoRoleHolderPerson();
			rhp.setRoleHolder(rh);
			rhp.setPerson(competencePerson);
			rhp.setRanking(ranking);
			rhp.setValidfrom(validFrom);
			if (validTo != null)
				rhp.setValidto(validTo);
			else
				rhp.setValidto(PoConstants.getInfDate());
			rh.getRoleHolderPersons().add(rhp); //
			role.addRoleHolder(rh);
			roleCompetenceDAO.save(rh);
			roleHolderPersonDAO.save(rhp);
			genRhp = rhp;
		}
		return genRhp;
	}

	@Override
	public void assignRoleWithClientCompetence(PoRole role,
			PoPerson competencePerson, PoClient client, Date validFrom,
			Date validTo, int ranking) {
		if (validFrom == null)
			validFrom = new Date();
		else
			if (validFrom.before(new Date()))
				validFrom.setTime(new Date().getTime());
		if (validTo ==null)
			validTo = PoConstants.getInfDate();
		
		assignRoleWithClientCompetenceReally(role, competencePerson, 
				client, validFrom, validTo, ranking, false);
		
	}
	
	private PoRoleHolderPerson assignRoleWithClientCompetenceReally(PoRole role,
			PoPerson competencePerson, PoClient client, Date validFrom,
			Date validTo, int ranking, boolean isDeputy) {
		if (client==null)
			throw new PoRuntimeException("Cannot assign a null client");
		
		PoRoleHolderPerson genRhp=null;
		
		List<PoRoleCompetenceClient> rcClients = roleCompetenceDAO.findRoleCompetenceClientWithCompetence4ClientF(role,
				client, validFrom);
		// an exception is thrown if a roleholder for the role, the person and
		// the group, which is currently valid
		// is found
		
		if (rcClients.size() > 1)
			rearrangeDataToEnsureConsistency(rcClients);
		
		if (rcClients.size() == 1) {
			
			PoRoleCompetenceClient rh = rcClients.get(0);
			
			List<PoRoleHolderPerson> overlappedRoleHolder = getOverlappedRoleHolderPersons(competencePerson, rh, validFrom, validTo, isDeputy);
			
			if(overlappedRoleHolder.size()==0) {
				PoRoleHolderPerson rhp = new PoRoleHolderPerson();
				rhp.setRoleHolder(rh);
				rhp.setPerson(competencePerson);
				rhp.setRanking(ranking);
				rhp.setValidfrom(validFrom);
				rhp.setValidto(validTo);
				rh.getRoleHolderPersons().add(rhp);
				roleHolderPersonDAO.save(rhp);
				genRhp = rhp;
			} else {
				List<PoRoleHolderPerson> toRemove = new ArrayList<PoRoleHolderPerson>();
				genRhp = rearrangeRoleHolderPersons(overlappedRoleHolder, validFrom, validTo, toRemove);
				rh.getRoleHolderPersons().removeAll(toRemove);
			}
		} else {
			PoRoleCompetenceClient rh = generateRoleCompetenceClient(role, client);
			PoRoleHolderPerson rhp = new PoRoleHolderPerson();
			rhp.setRoleHolder(rh);
			rhp.setPerson(competencePerson);
			rhp.setRanking(ranking);
			rhp.setValidfrom(validFrom);
			if (validTo != null)
				rhp.setValidto(validTo);
			else
				rhp.setValidto(PoConstants.getInfDate());
			rh.getRoleHolderPersons().add(rhp); //
			role.addRoleHolder(rh);
			roleCompetenceDAO.save(rh);
			roleHolderPersonDAO.save(rhp);
			genRhp = rhp;
		}
		return genRhp;
	}
	
	public void assignRoleWithGroupCompetence(PoRole role,
			PoGroup competenceGroup, PoGroup controlledGroup, int ranking) {
		assignRoleWithGroupCompetence(role, competenceGroup,
				controlledGroup, new Date(), null, ranking);
	}

	@Override
	public void assignRoleWithGroupCompetence(PoRole role,
			PoGroup competenceGroup, PoGroup controlledGroup, Date validFrom,
			Date validTo, int ranking) {

		if (validFrom == null)
			validFrom = new Date();
		else
			if (validFrom.before(new Date()))
				validFrom.setTime(new Date().getTime());

		if (validTo == null)
			validTo = PoConstants.getInfDate();

		List<PoRoleCompetenceGroup> l = roleCompetenceDAO.findRoleCompetenceGroupWithCompetence4GroupF(role,
				controlledGroup, validFrom);
		
		
		if (l.size() > 1)
			rearrangeDataToEnsureConsistency(l);
		
		
		if (l.size() == 1) {
			
			PoRoleCompetenceGroup rh = l.get(0);
			
			List<PoRoleHolderGroup> overlappedRoleHolder = getOverlappedRoleHolderGroups(competenceGroup, rh, validFrom, validTo);
			
			if (overlappedRoleHolder.size()==0) {
				PoRoleHolderGroup rhg = new PoRoleHolderGroup();
				rhg.setRoleHolder(rh);
				rhg.setGroup(competenceGroup);
				rhg.setRanking(ranking);
				rhg.setValidfrom(validFrom);
				if (validTo != null)
					rhg.setValidto(validTo);
				else
					rhg.setValidto(PoConstants.getInfDate());
				rh.getRoleHolderGroups().add(rhg);
	
				// competencePerson.addReferencedAsRoleHolder(rhp);
	
				roleCompetenceDAO.save(rh);
				roleHolderGroupDAO.save(rhg);
			} else 
				rh.getRoleHolderGroups().removeAll(
						rearrangeRoleHolderGroups(overlappedRoleHolder, validFrom, validTo));
		} else {
			// create a new one - so far no role holder exists
			PoRoleCompetenceGroup rh = generateRoleCompetenceGroup(role, controlledGroup);
			PoRoleHolderGroup rhg = new PoRoleHolderGroup();
			rhg.setRoleHolder(rh);
			rhg.setGroup(competenceGroup);
			rhg.setRanking(ranking);
			rhg.setValidfrom(validFrom);
			rhg.setValidto(validTo);
			rh.getRoleHolderGroups().add(rhg);
			role.addRoleHolder(rh);
			roleCompetenceDAO.save(rh);
			roleHolderGroupDAO.save(rhg);
		}

	}

	private PoRoleCompetenceGroup generateRoleCompetenceGroup(PoRole role, PoGroup controlledGroup) {
		PoRoleCompetenceGroup rh = new PoRoleCompetenceGroup();
		rh.setRole(role);
		rh.setType(PoConstants.ROLE_HOLDER_COMPETENCE_GROUP.toString());
		rh.setValidfrom(new Date());
		// validto is only used to simulate deletion
		rh.setValidto(PoConstants.getInfDate());
		rh.setCompetence4Group(controlledGroup);
		return rh;
	}

	public void assignRoleWithClientCompetence(PoRole role,
			PoGroup competenceGroup, PoClient client, int ranking) {
		assignRoleWithClientCompetence(role, competenceGroup,
				client, new Date(), null, ranking);
	}

	@Override
	public void assignRoleWithClientCompetence(PoRole role,
			PoGroup competenceGroup, PoClient client, Date validFrom,
			Date validTo, int ranking) {

		if (validFrom == null)
			validFrom = new Date();
		else
			if (validFrom.before(new Date()))
				validFrom.setTime(new Date().getTime());

		if (validTo == null)
			validTo = PoConstants.getInfDate();

		List<PoRoleCompetenceClient> rcClients = roleCompetenceDAO.findRoleCompetenceClientWithCompetence4ClientF(role,
				client, validFrom);
		
		
		if (rcClients.size() > 1)
			rearrangeDataToEnsureConsistency(rcClients);
		
		
		if (rcClients.size() == 1) {
			
			PoRoleCompetenceClient rh = rcClients.get(0);
			
			List<PoRoleHolderGroup> overlappedRoleHolder = getOverlappedRoleHolderGroups(competenceGroup, rh, validFrom, validTo);
			
			if (overlappedRoleHolder.size()==0) {
				PoRoleHolderGroup rhg = new PoRoleHolderGroup();
				rhg.setRoleHolder(rh);
				rhg.setGroup(competenceGroup);
				rhg.setRanking(ranking);
				rhg.setValidfrom(validFrom);
				if (validTo != null)
					rhg.setValidto(validTo);
				else
					rhg.setValidto(PoConstants.getInfDate());
				rh.getRoleHolderGroups().add(rhg);
	
				// competencePerson.addReferencedAsRoleHolder(rhp);
	
				roleCompetenceDAO.save(rh);
				roleHolderGroupDAO.save(rhg);
			} else 
				rh.getRoleHolderGroups().removeAll(
						rearrangeRoleHolderGroups(overlappedRoleHolder, validFrom, validTo));
		} else {
			// create a new one - so far no role holder exists
			PoRoleCompetenceClient rh = generateRoleCompetenceClient(role, client);
			PoRoleHolderGroup rhg = new PoRoleHolderGroup();
			rhg.setRoleHolder(rh);
			rhg.setGroup(competenceGroup);
			rhg.setRanking(ranking);
			rhg.setValidfrom(validFrom);
			rhg.setValidto(validTo);
			rh.getRoleHolderGroups().add(rhg);
			role.addRoleHolder(rh);
			roleCompetenceDAO.save(rh);
			roleHolderGroupDAO.save(rhg);
		}

	}

	private PoRoleCompetenceClient generateRoleCompetenceClient(PoRole role, PoClient client) {
		PoRoleCompetenceClient rh = new PoRoleCompetenceClient();
		rh.setRole(role);
		rh.setType(PoConstants.ROLE_HOLDER_COMPETENCE_CLIENT.toString());
		rh.setValidfrom(new Date());
		// validto is only used to simulate deletion
		rh.setValidto(PoConstants.getInfDate());
		rh.setCompetence4Client(client);
		return rh;
	}
	
	public void assignRoleWithPersonCompetence(PoRole role,
			PoPerson competencePerson, PoPerson controlledPerson, int ranking) {
		assignRoleWithPersonCompetence(role, competencePerson,
				controlledPerson, null, null, ranking);

	}

	
	@Override
	public void assignRoleWithPersonCompetence(PoRole role,
			PoPerson competencePerson, PoPerson controlledPerson,
			Date validFrom, Date validTo, int ranking) {
		if (validFrom == null)
			validFrom = new Date();
		else
			if (validFrom.before(new Date()))
				validFrom.setTime(new Date().getTime());
		if (validTo == null)
			validTo = new Date(DateTools.INFINITY_TIMEMILLIS);
		
		assignRoleWithPersonCompetenceReally(role, competencePerson, controlledPerson, 
				validFrom, validTo, ranking,false);
		
		
		// check deputies -> if one was found, add competence to deputy as well 
		for (PoRoleDeputy rd : findRoleDeputiesOfPersonF(competencePerson, role, validFrom) ) {
			if (DateTools.doesOverlap(rd.getValidfrom(),rd.getValidto(), validFrom, validTo)) {
				Date cValidFrom = validFrom;
				Date cValidTo = validTo;
				if (cValidFrom.before(rd.getValidfrom()))
					cValidFrom = rd.getValidfrom();
				if (cValidTo.after(rd.getValidto()))
					cValidTo = rd.getValidto();
				
				// if an existing roleholderperson is used, the deputy is already set
				PoRoleHolderPerson rhp_d = assignRoleWithPersonCompetenceReally(role, rd.getDeputy(),
						controlledPerson, cValidFrom, cValidTo, ++ranking, true);
				rhp_d.setDeputy(rd);
				saveRoleHolderPerson(rhp_d);
			}
		}
	}
	
	private PoRoleHolderPerson assignRoleWithPersonCompetenceReally(PoRole role,
			PoPerson competencePerson, PoPerson controlledPerson,
			Date validFrom, Date validTo, int ranking, boolean isDeputy) {
		
		PoRoleHolderPerson genRhp = null;
		List<PoRoleCompetencePerson> l = roleCompetenceDAO.findRoleCompetencePersonWithCompetence4PersonF(
				role, controlledPerson, validFrom);
		
		// more than one PoRoleCompetenceBase Object was found for the role and person -> rearrange data 
		if (l.size()>1)
			rearrangeDataToEnsureConsistency(l);
		
		if (l.size() == 1) {
			
			PoRoleCompetencePerson rh = l.get(0);
			
			List<PoRoleHolderPerson> overlappedRoleHolder = getOverlappedRoleHolderPersons(competencePerson, rh, validFrom, validTo, isDeputy);
			
			if (overlappedRoleHolder.size()==0) {
				PoRoleHolderPerson rhp = new PoRoleHolderPerson();
				rhp.setRoleHolder(rh);
				rhp.setPerson(competencePerson);
				rhp.setRanking(ranking);
				rhp.setValidfrom(validFrom);
				if (validTo != null)
					rhp.setValidto(validTo);
				else
					rhp.setValidto(PoConstants.getInfDate());
				rh.getRoleHolderPersons().add(rhp);
				roleHolderPersonDAO.save(rhp);
				genRhp = rhp;
			} else {
				List<PoRoleHolderPerson> toRemove = new ArrayList<PoRoleHolderPerson>();
				genRhp = rearrangeRoleHolderPersons(overlappedRoleHolder, validFrom, validTo, toRemove);
				rh.getRoleHolderPersons().removeAll(toRemove);
			}

		} else {
			PoRoleCompetencePerson rhcp = generateRoleCompetencePerson(role, controlledPerson);
			
			PoRoleHolderPerson rhp = new PoRoleHolderPerson();
			rhp.setRoleHolder(rhcp);
			rhp.setPerson(competencePerson);
			rhp.setRanking(ranking);
			rhp.setValidfrom(validFrom);
			if (validTo == null)
				rhp.setValidto(PoConstants.getInfDate());
			else
				rhp.setValidto(validTo);
			rhcp.getRoleHolderPersons().add(rhp);
			role.addRoleHolder(rhcp);

			roleCompetenceDAO.save(rhcp);
			roleHolderPersonDAO.save(rhp);
			genRhp = rhp;
		}
		return genRhp;
	}

	@Override
	public void assignRoleWithPersonCompetence(PoRole role,
			PoGroup competenceGroup, PoPerson controlledPerson, Date validFrom,
			Date validTo, int ranking) {
		if (validFrom == null)
			validFrom = new Date();
		else
			if (validFrom.before(new Date()))
				validFrom.setTime(new Date().getTime());
		
		if (validTo == null)
			validTo = new Date(DateTools.INFINITY_TIMEMILLIS);

		List<PoRoleCompetencePerson> l = roleCompetenceDAO.findRoleCompetencePersonWithCompetence4PersonF(
				role, controlledPerson, validFrom);
		
		if (l.size() > 1)
			rearrangeDataToEnsureConsistency(l);
		
		if (l.size() == 1) {
			
			PoRoleCompetencePerson rh = l.get(0);
			
			List<PoRoleHolderGroup> overlappedRoleHolder = getOverlappedRoleHolderGroups(competenceGroup, rh, validFrom, validTo);
			
			if (overlappedRoleHolder.size()==0) {
				PoRoleHolderGroup rhg = new PoRoleHolderGroup();
				rhg.setRoleHolder(rh);
				rhg.setGroup(competenceGroup);
				rhg.setRanking(ranking);
				rhg.setValidfrom(validFrom);
				rhg.setValidto(validTo);
				rh.getRoleHolderGroups().add(rhg);
				roleCompetenceDAO.save(rh);
				roleHolderGroupDAO.save(rhg);
			} else {
				List<PoRoleHolderGroup> toRemove = rearrangeRoleHolderGroups(overlappedRoleHolder, validFrom, validTo);
				rh.getRoleHolderGroups().removeAll(toRemove);
			}
		} else {

			PoRoleCompetencePerson rhcp = generateRoleCompetencePerson(role, controlledPerson);
			
			PoRoleHolderGroup rhg = new PoRoleHolderGroup();
			rhg.setRoleHolder(rhcp);
			rhg.setGroup(competenceGroup);
			rhg.setRanking(ranking);
			rhg.setValidfrom(validFrom);
			rhg.setValidto(validTo);
			rhcp.getRoleHolderGroups().add(rhg);
			role.addRoleHolder(rhcp);

			roleCompetenceDAO.save(rhcp);
			roleHolderGroupDAO.save(rhg);
		}
	}
	
	private PoRoleCompetencePerson generateRoleCompetencePerson(PoRole role, PoPerson controlledPerson) {
		PoRoleCompetencePerson rhcp = new PoRoleCompetencePerson();
		rhcp.setRole(role);
		rhcp.setType(PoConstants.ROLE_HOLDER_COMPETENCE_PERSON.toString());
		rhcp.setValidfrom(new Date());
		// validto is only used to simulation deletion
		rhcp.setValidto(PoConstants.getInfDate());
		rhcp.setCompetence4Person(controlledPerson);
		return rhcp;
	}
	
	
	/**
	 * This function returns a list of persons that have competence for the 
	 * given <code>controlledGroup</code>. 
	 * 
	 * @param role
	 * @param controlledGroup
	 * @param date
	 * @return a <code>List</code> of <code>PoPerson</code> objects.
	 */
	private List<PoPerson> findGroupsWithCompetence4GroupWithExpandedPersons(PoRole role, PoGroup controlledGroup, Date date) {
		List<PoPerson> ret = new ArrayList<PoPerson>();
		
		//	directly referenced groups
		List<PoGroup> l_g = roleDAO.findGroupsWithCompetence4Group(role,
				controlledGroup, date);
		
		// Merge persons from group and directly linked persons
		// Get persons from Groups and add them to the result List
		for (PoGroup g : l_g) {
			ret.addAll(orgService.findPersonsOfGroup(g, date));
		}
		return ret;
	}
	
	/**
	 * @param group
	 * @param date
	 * @return a <code>PoGroup</code> object if a parent exists,
	 * <code>null</code> otherwise.
	 */
	private PoGroup resolveParentGroup(PoGroup group, Date date) {
		PoParentGroup pg = orgService.getParentGroup(
				group, date);
		if (pg!=null)
			return pg.getParentGroup();
		return null;
	}
	
	/**
	 * @param group the group to find the level for.
	 * @param date the validity date for the group.
	 * @return the depth of the group, starting with <code>1</code> for the top level group.
	 */
	private int getGroupLevel(PoGroup group, Date date) {
		int i = 1;
		PoGroup parent = resolveParentGroup(group, date);
		while (parent != null) {
			parent = resolveParentGroup(parent, date);
			i++;
		}
		return i;
	}

	private void findAuthorityRecursive(PoGroup controlledGroup, PoRole role, Date date, PersonRoleHolders result, int minAmount, int currentSearchLevel) {
		
		// directly referenced persons
		List<PoPerson> personsCompetent4Group = roleDAO.findPersonsWithCompetence4Group(role, controlledGroup, date);
		// directly referenced groups (expanded to persons)
		List<PoPerson> personsOfGroupsCompetent4Group = findGroupsWithCompetence4GroupWithExpandedPersons(role, controlledGroup, date);
		
		// add to result
		result.addAll(new PersonRoleHolders(personsCompetent4Group, currentSearchLevel));
		result.addAll(new PersonRoleHolders(personsOfGroupsCompetent4Group, currentSearchLevel));
		
		
		// walk up/down the tree if these conditions are met
		// 1) we don't exceed the maximum number of levels to walk in the tree
		//   	A N D
		// 2) we haven't found any roleholders at this level    OR
		//    the number of roleholders collected so far is not enough OR
		//    we want to collect all available roleholders in the 
		//    search direction
		if ( currentSearchLevel < role.getLevelsToSearch() &&
				( (personsOfGroupsCompetent4Group.isEmpty() && personsCompetent4Group.isEmpty()) || 
				 result.size() < minAmount || 
				 minAmount == -1 ) )
		{ 
			currentSearchLevel++;
			
			switch (role.getDirectionOfInheritance()) {
			case PoConstants.SEARCH_DIRECTION_UP:
				PoGroup parent = resolveParentGroup(controlledGroup, date);
				// only go up the tree if 
				// we haven't reached the maximum allowed
				// absolute organigram level number (defined in role)
				if (parent != null && getGroupLevel(parent, date) >= role.getMaxLevelToSearchUp())	{
					findAuthorityRecursive(parent, role, date, result, minAmount, currentSearchLevel);
				}
				break;
			case PoConstants.SEARCH_DIRECTION_DOWN:
				for (PoParentGroup childGroup : orgService.findChildGroups(controlledGroup, date)) {
					findAuthorityRecursive(childGroup.getChildGroup(), role, date, result, minAmount,  currentSearchLevel);
				}
				break;
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PoPerson> findAuthority(PoPerson controlledPerson, PoRole role, Date date, int minAmount) {
		if (logger.isDebugEnabled())
			logger.debug("findAuthorityWorker for " + orgService.getPerson(controlledPerson.getUID()) + " and Role " + role + " for Date " + date + " with minAmount Holders " + minAmount);

		// is there a direct connection between a (authority) person/group the given role and the controlled group
		PersonRoleHolders result = new PersonRoleHolders();
		
		PersonRoleHolders directRoleHolderPersons = doFindAuthority(controlledPerson, role, date, result, minAmount);
		
		// add roleholders defined with competence for the client of the controlledPerson
		doFindAuthority(controlledPerson.getClient(), role, date, result);
		
		// append the general RoleHolders only if client of role is null,
		// or client of role is same as client of controlledPerson!
		if (hasRoleSameOrNoClient(role, controlledPerson)) {
			// add persons defined via a dynamic role holder (with competence 4 all) 
			PoGroup hierarchicalGroup = null;
			for (PoRoleHolderDynamic rhd : roleHolderDynamicDAO.findRoleHolderDynamicWithCompetence4All(role, date)) {
				if (hierarchicalGroup == null)
					hierarchicalGroup = orgService.getPersonsHierarchicalGroup(controlledPerson, date);
				result.addAll(new PersonRoleHolders(resolveDynamicViewPermission(hierarchicalGroup, rhd.getRoleHolderType(), date), PersonRoleHolders.GENERAL_LEVEL));
			}
			result.addAll(findGeneralRoleHolders(role, date));
		}

		result.removeDuplicatesOnSameLevel();

		// this should handle the case to avoid self-approval
		// (including the case to avoid that the deputy can approve the request of)    TODO something is missing here!
		// -> takes care of extra flag in role!
		filterOutControlledPersonAndDeputies(
				controlledPerson,
				result,
				directRoleHolderPersons,
				role.isDoNotAllowSelfApproval(),
				// restriction of selfapproval by deputy makes only sense
				// if restriction of selfapproval is activated!
				role.isDoNotAllowApprovalByDeputy() && role.isDoNotAllowSelfApproval());
		
		List<PoPerson> personResult = result.toPersonsUnique();
		
		// if the role is parametrized to only return a fraction of all roleholders -> do it here...
		if (role.getMaxRoleHoldersToReturn() != null && role.getMaxRoleHoldersToReturn() > 0) {
			List<PoPerson> newResult = new ArrayList<PoPerson>();
			if (role.getMaxRoleHoldersToReturn() >= personResult.size()) {
				newResult.addAll(personResult);
			} else {
				newResult.addAll(personResult.subList(0, role.getMaxRoleHoldersToReturn()));
			}
			return newResult;
		}

		return personResult;
	}

	/** roleHolderGroups with Competence over the controlledPerson */
	private List<PoGroup> findGroupsWithDirectCompetence4Person(PoRole role, PoPerson controlledPerson, Date date) {
		List<PoGroup> result= new ArrayList<PoGroup>();
		for (PoRoleHolderGroup rhg : roleHolderGroupDAO.findDistinctRoleHolderGroupsWithCompetence4Person(role, controlledPerson, date))
			result.add(rhg.getGroup());		
		return result;
	}

	
	@Override
	public List<PoPerson> findAuthority(PoGroup group, PoRole role) {
		return findAuthority(group, role, new Date(), -1);
	}
	
	
	@Override
	public List<PoPerson> findAuthority(PoGroup group, PoRole role, Date date, int minAmount) {
		PersonRoleHolders result = new PersonRoleHolders();
		
		findAuthorityRecursive(group, role, date, result, minAmount, 1);
		
		// add roleholders defined on client level.
		findAuthority(group.getClient(), role, date, minAmount);
		
		if (hasRoleSameOrNoClient(role, group))
			result.addAll(findGeneralRoleHolders(role, date));

		result.removeDuplicatesOnSameLevel();
		return result.toPersonsUnique();
	}
	
	/** {@inheritDoc} */
	@Override
	public List<PoPerson> findAuthority(PoClient client, PoRole role) {
		return findAuthority(client, role, new Date(), -1);
	}

	/** {@inheritDoc} */
	@Override
	public List<PoPerson> findAuthority(PoClient client, PoRole role, Date date, int minAmount) {
		if (logger.isDebugEnabled())
			logger.debug("findAuthorityWorker for " + orgService.getClient(client.getUID()) + " and Role " + role + " for Date " + date + " with minAmount Holders " + minAmount);

		// is there a direct connection between a (authority) person/group the given role and the controlled group
		PersonRoleHolders result = new PersonRoleHolders();
		
		doFindAuthority(client, role, date, result);

		// no general RoleHolders 

		result.removeDuplicatesOnSameLevel();

		List<PoPerson> personResult = result.toPersonsUnique();
		
		// if the role is parametrized to only return a fraction of all roleholders -> do it here...
		if (role.getMaxRoleHoldersToReturn() != null && role.getMaxRoleHoldersToReturn() > 0) {
			List<PoPerson> newResult = new ArrayList<PoPerson>();
			if (role.getMaxRoleHoldersToReturn() >= personResult.size()) {
				newResult.addAll(personResult);
			} else {
				newResult.addAll(personResult.subList(0, role.getMaxRoleHoldersToReturn()));
			}
			return newResult;
		}
		return personResult;
	}

	private PersonRoleHolders findGeneralRoleHolders(PoRole role, Date date)	{
		PersonRoleHolders generalRoleHolders = new PersonRoleHolders(
				roleDAO.findPersonsWithRoleAndCompetence4All(role,date), PersonRoleHolders.GENERAL_LEVEL );
		
		for (PoGroup g : roleDAO.findGroupsWithRoleAndCompetence4All(role, date)) {
			generalRoleHolders.addPersonsUnique(orgService.findPersonsOfGroup(g, date), PersonRoleHolders.GENERAL_LEVEL);
		}
		return generalRoleHolders;
	}
	
	private boolean hasRoleSameOrNoClient(PoRole role, CompetenceTarget competenceTarget)	{
		if (role.getClient() == null)
			return true;
		
		if (competenceTarget == null)
			return false;
		
		final PoClient client;
		if (competenceTarget instanceof PoPerson)
			client = ((PoPerson) competenceTarget).getClient();
		else if (competenceTarget instanceof PoGroup)
			client = ((PoGroup) competenceTarget).getClient();
		else
			throw new IllegalArgumentException("Unknown CompetenceTarget implementation: "+competenceTarget.getClass()+" - please implement it!");
		
		return role.getClient().equals(client);
	}
	
	
	
	/**
	 * Helper class holding logic around authority search.
	 * This is a list of persons that are authorities for some target person.
	 * The persons are found on different levels of the organizational hierarchy.
	 * So they are tagged with their level when added.
	 * 
	 * @author fritzberger 10.06.2011
	 */
	@SuppressWarnings("serial")
	private static class PersonRoleHolders extends ArrayList<PersonRoleHolders.PersonOnLevel>
	{
		public static final int GENERAL_LEVEL = Integer.MAX_VALUE;	// persons with competence for all
		
		private static class PersonOnLevel
		{
			private final PoPerson person;
			private final int hierarchicalLevel;	// from 0 - n, whereby n is root level
			
			/**
			 * @param person the person to represent.
			 * @param hierarchicalLevel the level the person resides on.
			 * 		Ranges from zero to n, is zero on the person's level,
			 * 		is n on root level of the organizational hierarchy.
			 */
			PersonOnLevel(PoPerson person, int hierarchicalLevel) {
				this.person = person;
				this.hierarchicalLevel = hierarchicalLevel;
			}
			
			@Override
			public boolean equals(Object other)	{
				PersonOnLevel otherPersonOnLevel = (PersonOnLevel) other;
				return hierarchicalLevel == otherPersonOnLevel.hierarchicalLevel && person.equals(otherPersonOnLevel.person);
			}
			
			@Override
			public int hashCode() {
				return person.hashCode() + hierarchicalLevel;
			}
			
			@Override
			public String toString() {
		        return "PersonOnLevel[person=" + person.getFullName() + ", hierarchicalLevel=" + hierarchicalLevel + "]";
			}
		}

		
		/** Empty list constructor. */
		public PersonRoleHolders() {
		}
		
		/** Turns passed PoPerson list into a list of PersonRoleHolders with given search level. */
		public PersonRoleHolders(List<PoPerson> persons, int level) {
			for (PoPerson person : persons)
				add(new PersonOnLevel(person, level));
		}
		
		/** Adds all passed persons when not already level-specifically contained. */
		public void addPersonsUnique(List<PoPerson> persons, int level) {
			for (PoPerson person : persons)	{
				PersonOnLevel p = new PersonOnLevel(person, level);
				if (contains(p) == false)
					add(p);
			}
		}

		/** Turns this PersonRoleHolder list into a list of Persons without duplicates. */
		public List<PoPerson> toPersonsUnique() {
			List<PoPerson> persons = new ArrayList<PoPerson>();
			for (PersonOnLevel roleHolder : this) {
				if (false == persons.contains(roleHolder.person))
					persons.add(roleHolder.person);
			}
			return persons;
		}
		
		/** Remove all persons that are equal to passed person, ignoring the level. */
		public void removeAllPersons(PoPerson person) {
			for (int i = size() - 1; i >= 0; i--)
				if (get(i).person.equals(person))
					remove(i);
		}
	
		/** Removes all duplicate persons on same level. */
		public void removeDuplicatesOnSameLevel() {
			PersonRoleHolders cleanedList = new PersonRoleHolders();
			for (PersonOnLevel personRoleHolder : this) {
				if (personRoleHolder.person.isActiveUser() && false == cleanedList.contains(personRoleHolder))
					cleanedList.add(personRoleHolder);
			}
			clear();
			addAll(cleanedList);
		}
		
		/** @return true when passed person is contained, ignoring level. */
		public boolean containsPoPerson(PoPerson person) {
			for (int i = 0; i < size(); i++)	{
				PersonOnLevel personRoleHolder = get(i);
				if (personRoleHolder.person.equals(person))
					return true;
			}
			return false;
		}

		/** Removes all persons except the first person on every level. This is for removing deputies. */
		public void removeAllAfterPersonByLevel(PoPerson controlledPerson) {
			PersonRoleHolders cleanedList = new PersonRoleHolders();
			int currentLevel = -99;
			boolean removalState = false;
			
			for (int i = 0; i < size(); i++)	{
				PersonOnLevel personRoleHolder = get(i);
				if (personRoleHolder.hierarchicalLevel == GENERAL_LEVEL)
					continue;
				
				boolean levelChanged = (currentLevel != personRoleHolder.hierarchicalLevel);
				currentLevel = personRoleHolder.hierarchicalLevel;
				if (levelChanged)
					removalState = false;	// remove deputies per level only, not globally
				
				if (removalState == false)
					cleanedList.add(personRoleHolder);
				
				if (personRoleHolder.person.equals(controlledPerson))
					removalState = true;	// recognizing controlledPerson starts removal per level
			}
			clear();	// remove all from this list
			addAll(cleanedList);	// add all collected persons
		}
	}
	
	
	/**
	 * Finds out authorities.
	 * Returns roleholders of hierarchical and non hierarchical groups (recursivly).
	 * Does not add dynamic or general roleholders!
	 * 
	 * @param controlledPerson the person that is the actual competence target for passed role.
	 * @param role the role which is, by its competence, targeting the controlledPerson.
	 * @param date the date/time this query should be done for.
	 * @param result  result list, elements will be added in this method.
	 * @param minAmount at least required number of elements in <i>result</i> list after this call.
	 * @return List of roleholders (persons) directly responsible for <i>controlledPerson</i>. 
	 */
	private PersonRoleHolders doFindAuthority(PoPerson controlledPerson, PoRole role, Date date, PersonRoleHolders result, int minAmount) {
		
		// the hierarchical Group
		PoGroup userGroup = orgService.getPersonsHierarchicalGroup(controlledPerson, date);
		
		// find directly responsible persons
		// with competence over controlled person
		List<PoPerson> directResponsiblePersons = roleDAO.findPersonsWithCompetence4Person(role, controlledPerson, date);

		// find directly responsible groups
		// with competence over controlled person
		List<PoGroup> directResponsibleGroups = findGroupsWithDirectCompetence4Person(role, controlledPerson, date);
		
		// find dynamic role holders
		List<PoRoleHolderDynamic> directResponsibleDynamicRoleholders = roleHolderDynamicDAO.findRoleHolderDynamicWithCompetence4Person(role, controlledPerson, date);
		
		boolean noDirectRoleHoldersExist =
			directResponsibleGroups.isEmpty() &&
			directResponsiblePersons.isEmpty() &&
			directResponsibleDynamicRoleholders.isEmpty();
		
		if (noDirectRoleHoldersExist) {
			// no directly referenced authority could be found, neither persons nor groups nor dynamic roleholders.
			// add dynamic role holders linked with the hierarchical group
			addDynamicRoleHoldersWithCompetence4Group(userGroup, date, result, 1);
			
			// get all linked groups of the person
			List<PoGroup> groupsOfPerson = orgService.findPersonsLinkedGroups(controlledPerson, role.getOrgType(), date);
			
			// iterate over all groups of the subject person and walk up the tree, at least one stage!
			for (PoGroup g : groupsOfPerson) {
				findAuthorityRecursive(g, role, date, result, minAmount, 1);
			}
		}
		else {
			result.addAll(new PersonRoleHolders(directResponsiblePersons, 0));
			
			for (PoGroup g : directResponsibleGroups)
				result.addAll(new PersonRoleHolders(orgService.findPersonsOfGroup(g, date), 0));
			
			// add dynamic role holders linked with the hierarchical group
			addDynamicRoleHoldersWithCompetence4Group(userGroup, date, result, 0);

			if (result.size() < minAmount || minAmount == -1)	{	// ggruber 2.3.2006 -> added condition minAmount == -1 for case where all Roleholders are requested
				List<PoGroup> linkedGroups = orgService.findPersonsLinkedGroups(controlledPerson, role.getOrgType(), date);
				// under normal circumstances should only return 1 group (hierarchical) anyway!
				findMoreAuthoritiesWhenDirectRoleHolderExist(linkedGroups, role, date, result, minAmount);
			}
		}
		
		return new PersonRoleHolders(directResponsiblePersons, 0);
	}

	/**
	 * Finds out authorities for client.
	 * Returns roleholders of hierarchical and non hierarchical groups (recursively).
	 * Does not add dynamic or general roleholders!
	 * 
	 * @param controlledClient the client that is the actual competence target for passed role.
	 * @param role the role which is, by its competence, targeting the client.
	 * @param date the date/time this query should be done for.
	 * @param result  result list, elements will be added in this method.
	 * @return List of roleholders (persons) directly responsible for <i>controlledClient</i>. 
	 */
	private PersonRoleHolders doFindAuthority(PoClient controlledClient, PoRole role, Date date, PersonRoleHolders result) {
		
		// find directly responsible persons
		// with competence over client
		List<PoPerson> directResponsiblePersons = roleDAO.findPersonsWithCompetence4Client(role, controlledClient, date);
		
		// find directly responsible groups
		// with competence over client
		List<PoGroup> directResponsibleGroups = roleDAO.findGroupsWithCompetence4Client(role, controlledClient, date);
		
		// no dynamic role holders
		
		boolean directRoleHoldersExist =
				! directResponsibleGroups.isEmpty() ||
				! directResponsiblePersons.isEmpty();
		
		if (directRoleHoldersExist) {
			
			result.addAll(new PersonRoleHolders(directResponsiblePersons, 0));
			
			for (PoGroup g : directResponsibleGroups)
				result.addAll(new PersonRoleHolders(orgService.findPersonsOfGroup(g, date), 0));
		}
		return new PersonRoleHolders(directResponsiblePersons, 0);
	}
	
	/**
	 * In this case we don't look for roleholders of the hierarchical group,
	 * as we have a bunch of directly referenced roleholders!
	 */
	private void findMoreAuthoritiesWhenDirectRoleHolderExist(List<PoGroup> linkedGroups, PoRole role, Date date, PersonRoleHolders result, int minAmount) {
		for (PoGroup group : linkedGroups) {
			// ggruber 2.3.06 -> hack for special case when user has special roleholders:
			// DO NOT search in HIS group but skip his group and go up the hierarchy.
			// Makes sense when assigning special roleholders to managers to avoid that they can approve themselves.
			// fri_2011-05-23: is this comment obsolete?
			
			switch (role.getDirectionOfInheritance()) {
			case PoConstants.SEARCH_DIRECTION_UP:
				if (role.isIncludeHierarchicalGroup()) {
					findAuthorityRecursive(group, role, date, result, minAmount, 1);
				} else {
					// walk up the tree
					PoParentGroup parentGroup = orgService.getParentGroup(group, date);
					
					// only proceed if levelsToSearch > 0
					if (parentGroup != null && role.getLevelsToSearch() > 0) {
						findAuthorityRecursive(parentGroup.getParentGroup(), role, date, result, minAmount, 1);
					}
				}
				break;
			case PoConstants.SEARCH_DIRECTION_DOWN:
				if (role.isIncludeHierarchicalGroup()) {
					findAuthorityRecursive(group, role, date, result, minAmount, 1);
				} else {
					// walk down the tree
					List<PoParentGroup> parentChildRelations = orgService.findChildGroups(group, date);

					// only proceed if levelsToSearch > 0
					if (role.getLevelsToSearch() > 0)	{
						for (PoParentGroup parentGroup : parentChildRelations) {
							findAuthorityRecursive(parentGroup.getChildGroup(), role, date, result, minAmount, 1);
						}
					}
				}
				break;
			case PoConstants.SEARCH_DIRECTION_NONE:
				if (role.isIncludeHierarchicalGroup()) {
					findAuthorityRecursive(group, role, date, result, minAmount, 1);
				}
				break;
			}
		}
	}

	/** Adds any dynamic role holder with competence for the group to the <code>result</code> List. */
	private PersonRoleHolders addDynamicRoleHoldersWithCompetence4Group(PoGroup userGroup, Date date, PersonRoleHolders result, int targetHierarchyLevel) {
		for(PoRoleHolderDynamic rhd : roleHolderDynamicDAO.findRoleHolderDynamicWithCompetence4Group(userGroup, date)) {
			result.addAll(new PersonRoleHolders(resolveDynamicViewPermission(userGroup, rhd.getRoleHolderType(), date), targetHierarchyLevel));
		}
		return result;
	}

	/**
	 * @param group
	 * @param roleHolderType
	 * @param date
	 * @return a <code>List</code> filled with the <code>PoPerson</code> objects that
	 * matched the intention of the <code>roleHolderType</code> parameter. The group
	 * is a need in order to interpret the intention correctly, the date is as always 
	 * needed as a constraint of time fulfillment.
	 * 
	 */
	private List<PoPerson> resolveDynamicViewPermission(PoGroup group,
			int roleHolderType, Date date) {
		switch (roleHolderType) {
		case DYNAMIC_TYPE_OWN_HIERARCHY:
			return orgService.findPersonsOfGroup(group, date);
		case DYNAMIC_TYPE_OWN_HIERARCHYPLUS:
			return findPersonsOfGroupAndSubGroups(group, date, new ArrayList<PoPerson>());
		case DYNAMIC_TYPE_OWN_CLIENT:
			return orgService.findPersonsOfClient(group.getClient(), date);
		case DYNAMIC_TYPE_ALL_CLIENTS:
			return orgService.findAllPersons(date);
		default:
			logger.error("RoleHolderType is not known. resolveDynamicViewPermission " +" will return an empty list.");
			return new ArrayList<PoPerson>();
		}
	}
	
	private List<PoPerson> findPersonsOfGroupAndSubGroups(PoGroup hierarchicalGroup, Date date, List<PoPerson> res) {
		res.addAll(orgService.findPersonsOfGroup(hierarchicalGroup, date));
		Iterator<PoParentGroup> childGroupsI = orgService.findChildGroups(hierarchicalGroup, date).iterator();
		while (childGroupsI.hasNext()) {
			res = findPersonsOfGroupAndSubGroups(childGroupsI.next().getChildGroup() , date, res);
		}
		return res;
	}

	private void filterOutControlledPersonAndDeputies(
			PoPerson controlledPerson,
			PersonRoleHolders possibleRoleHolders,
			PersonRoleHolders directLinkedPersonList,
			boolean doNotAllowSelfApproval,
			boolean doNotAllowApprovalByDeputy) {
	
		// first filter out deputies, as they are identified only
		// by the fact that they are not first on each level
		if (doNotAllowApprovalByDeputy)
			possibleRoleHolders.removeAllAfterPersonByLevel(controlledPerson);
		
		// after this deputy removal remove remaining self-approval candidates, ignoring levels
		boolean personControlsHimselfExplicitly = directLinkedPersonList.containsPoPerson(controlledPerson);
		if (doNotAllowSelfApproval && personControlsHimselfExplicitly == false)
			possibleRoleHolders.removeAllPersons(controlledPerson);
	}
	
	
	/**
	 * filters out controlled person out of RH Resultlist
	 * is almost same as <code>filterOutControlledPersonAndDeputies</code>
	 * @param controlledPerson
	 * @param resultList
	 * @param directLinkedRHList
	 */
	private void filterOutControlledPersonAndDeputiesOfRhList(PoPerson controlledPerson,
			List<PoRoleHolderLink> resultList, List<PoRoleHolderLink> directLinkedRHList) {
		
		// removes controlled Person from List only if person is not in directLinkedRHList list
		// and corresponding role disallows self-approval

		List<PoRoleHolderLink> newResult = new ArrayList<PoRoleHolderLink>();
		Iterator<PoRoleHolderLink> itr = resultList.iterator();
		while (itr.hasNext()) {
			PoRoleHolderLink rhLink = itr.next();
			if ( ( false == rhLink.getRoleCompetenceBase().getRole().isDoNotAllowSelfApproval()
						|| directLinkedRHList.contains(rhLink)
						|| false == rhLink.getRoleHolderObjectUID().equals(controlledPerson.getUID()))
					&& rhLink.getRoleCompetenceBase().getRole().getRoleType() != PoRole.DUMMY_ROLE)
				newResult.add(rhLink);
		}
		resultList.clear();
		resultList.addAll(newResult);
	}


	@Override
	public List<PoPerson> findAuthority(PoPerson controlledPerson, PoRole role) {
		return findAuthority(controlledPerson, role, new Date(), -1);
	}

	@Override
	public void deleteAndFlushRoleHolderPersonLink(PoRoleHolderPerson roleHolderPerson) {
		
		// check deputies -> if one was found, remove the deputy roleholder person object as well 
		Iterator<PoRoleDeputy> depI = findRoleDeputiesOfPersonF(roleHolderPerson.getPerson(), 
				roleHolderPerson.getRoleCompetenceBase().getRole(), roleHolderPerson.getValidfrom()).iterator();
		
		while (depI.hasNext()) {
			PoRoleDeputy rd = depI.next();
			for (PoRoleHolderPerson rhp : rd.getRoleHolderPersons() ) {
				if (doesOverlap(rhp, roleHolderPerson.getValidfrom(), roleHolderPerson.getValidto()) &&
						rhp.getRoleCompetenceBase().equals(roleHolderPerson.getRoleCompetenceBase())) {
					rhp.setValidto(new Date());
					roleHolderPersonDAO.save(rhp);
				}
			}
		}	
		
		roleHolderPerson.getPerson().getMemberOfGroups().remove(roleHolderPerson);
		roleHolderPerson.getRoleCompetenceBase().getRoleHolderPersons().remove(
				roleHolderPerson);
		roleHolderPersonDAO.delete(roleHolderPerson);
	}

	@Override
	public void deleteAndFlushRoleHolderGroupLink(PoRoleHolderGroup rhGroupLink) {
		
		// link is only allowed to be deleted if the validFrom field points into
		// the future
		if (rhGroupLink.getValidfrom().after(new Date())) {
			rhGroupLink.getGroup().getReferencedAsRoleHolder().remove(
					rhGroupLink);
			rhGroupLink.getRoleCompetenceBase().getRoleHolderGroups().remove(
					rhGroupLink);
			roleHolderGroupDAO.delete(rhGroupLink);
		} else
			throw new PoRuntimeException(
					PoRuntimeException.ERROR_LINK_CANNOT_BE_DELETED_LINK_ALREADY_ACTIVE);
	}

	@Override
	public void removePersonFromRole(PoRoleHolderPerson roleHolderPerson) {
		Date vf = roleHolderPerson.getValidfrom();
		Date vt = roleHolderPerson.getValidto();
		
		removePersonFromRoleReally(roleHolderPerson);
		
		// check deputies -> if one was found, remove the deputy roleholder person object as well 
		Iterator<PoRoleDeputy> depI = findRoleDeputiesOfPersonF(roleHolderPerson.getPerson(), 
				roleHolderPerson.getRoleCompetenceBase().getRole(), roleHolderPerson.getValidfrom()).iterator();
		
		while (depI.hasNext()) {
			PoRoleDeputy rd = depI.next();
			for ( PoRoleHolderPerson rhp : rd.getRoleHolderPersons() ) {
				
				if (doesOverlap(rhp, vf, vt) &&
						rhp.getRoleCompetenceBase().equals(roleHolderPerson.getRoleCompetenceBase()))
					removePersonFromRoleReally(rhp);
			}
		}	
	}
		
	private List<PoRoleDeputy> findRoleDeputiesOfPersonF(PoPerson person, PoRole role, Date validfrom) {
		return roleDeputyDAO.findRoleDeputiesOfPersonF(person, role, validfrom);
	}

	private void removePersonFromRoleReally(PoRoleHolderPerson roleHolderPerson) {
		roleHolderPerson.setValidto(new Date());
		roleHolderPersonDAO.save(roleHolderPerson);
		
		// check if coresponding PoRoleCompetenceBase Object has no more holders
		PoRole role = roleHolderPerson.getRoleCompetenceBase().getRole();
		
		if (role.getRoleType() == PoRole.DUMMY_ROLE) {
			// delete dummy role
			role.setValidto(new Date());
			saveRole(role);
		}

		PoRoleCompetenceBase rc = roleHolderPerson.getRoleCompetenceBase();

		// affect the roleCompetence only when no more role holders are assigned
		if (findRoleHolderGroupF(rc.getRole(), new Date()).size() == 0
				&& roleHolderPersonDAO.findRoleHolderPerson(rc.getRole(), new Date()).size() == 0) {
			// no active roleholder exist anymore
			// delete roleholderbase object!
			deleteRoleCompetence(rc);
		}
	}

	
	@Override
	public void removeGroupFromRole(PoRoleHolderGroup roleHolderGroup) {
		roleHolderGroup.setValidto(new Date());
		roleHolderGroupDAO.save(roleHolderGroup);
		
		// check if coresponding PoRoleCompetenceBase Object has no more holders
		PoRole role = roleHolderGroup.getRoleCompetenceBase().getRole();
		
		if (role.getRoleType() == PoRole.DUMMY_ROLE) {
			// delete dummy role
			role.setValidto(new Date());
			saveRole(role);
		}

		PoRoleCompetenceBase rc = roleHolderGroup.getRoleCompetenceBase();

		// affect the roleCompetence only when no more role holders are assigned
		if (findRoleHolderGroupF(rc.getRole(), new Date()).size() == 0
				&& roleHolderPersonDAO.findRoleHolderPerson(rc.getRole(), new Date()).size() == 0) {
			// no active roleholder exist anymore
			// delete roleholderbase object!
			deleteRoleCompetence(rc);
		}
	}


	@Override
	public void changeValidityRHGroupLink(PoRoleHolderGroup rhGroupLink,
			Date validFrom, Date validTo) {
		if (validFrom != null) {
			if (validFrom.before(new Date()))
				validFrom = new Date();
		} else
			validFrom = new Date();
		if (validTo == null)
			validTo = PoConstants.getInfDate();
		
		// check overlappings
		List<PoRoleHolderGroup> overlappedRoleHolder = getOverlappedRoleHolderGroups(rhGroupLink.getGroup(), rhGroupLink.getRoleCompetenceBase(), 
				validFrom, validTo);
		if (overlappedRoleHolder.size()!=0) {
			List<PoRoleHolderGroup> toRemove = rearrangeRoleHolderGroups(overlappedRoleHolder, validFrom, validTo);
			rhGroupLink.getRoleCompetenceBase().getRoleHolderGroups().removeAll(toRemove);
		} else {
			rhGroupLink.setValidfrom(validFrom);
			rhGroupLink.setValidto(validTo);
			roleHolderGroupDAO.save(rhGroupLink);
		}
	}
	
	
	@Override
	public List<PoRoleHolderLink> findRoleHolderWithCompetenceForPerson(PoPerson controlledPerson, Date date) {
		
		PoGroup hierarchicalGroup = orgService.getPersonsHierarchicalGroup(controlledPerson, date);
		
		// result list
		List<PoRoleHolderLink> res = new ArrayList<PoRoleHolderLink>();
		
		// load directly linked roleholderlinks
		List<PoRoleHolderLink> direct_rh_list = roleHolderDAO.findRoleHolderWithCompetence4Person(controlledPerson, date);
		
		res.addAll(direct_rh_list);
		
		// add the dynamic role holders
		res.addAll(roleHolderDynamicDAO.findRoleHolderDynamicWithCompetence4Person(controlledPerson, date));
		res.addAll(roleHolderDynamicDAO.findRoleHolderDynamicWithCompetence4Group(hierarchicalGroup, date));
		res.addAll(roleHolderDynamicDAO.findRoleHolderDynamicWithCompetence4All(date));
		
		// get List of Roles out of previously generated roleholder list
		// which are of type organisation-hierarchy
		List<PoRole> roleList = new ArrayList<PoRole>();
		for (PoRoleHolderLink rhl : direct_rh_list) {
			if (rhl.getRoleCompetenceBase().getRole().getOrgType() == PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY
					&& !roleList.contains(rhl.getRoleCompetenceBase().getRole())) {
				roleList.add(rhl.getRoleCompetenceBase().getRole());
			}
		}

		// add general roleholder links
		res.addAll(roleHolderDAO.findRoleHolderWithCompetence4All(controlledPerson.getClient(), date));

		// get roleholders of hierarchical group
		List<PoRoleHolderLink> rhForGroupList = roleHolderDAO.findRoleHolderWithCompetence4Group(hierarchicalGroup, date);
		List<PoRoleHolderLink> rhForGroupList2 = new ArrayList<PoRoleHolderLink>();

		// Rolleninhaber der zugeh. hierarchischen Gruppe ausblenden, falls sie
		// schon direkt
		// über die Person zugeordnet wurden
		if (roleList.size() > 0) {
			for (PoRoleHolderLink rhl : rhForGroupList) { 
				if (!roleList.contains(rhl.getRoleCompetenceBase().getRole())) {
					rhForGroupList2.add(rhl);
				}
			}
			res.addAll(rhForGroupList2);
		} else {
			res.addAll(rhForGroupList);
		}
		
		// get roleholders linked to persons client
		List<PoRoleHolderLink> clientRoleholders = roleHolderDAO.findRoleHolderWithCompetence4Client(controlledPerson.getClient(), date);
		for (PoRoleHolderLink link : clientRoleholders)
			if (!res.contains(link))
				res.add(link);
		

		// filters out controlled Person of List
		// only if person is not in directly linked list
		// and corresponding role is activated to disallow selfapprovment
		filterOutControlledPersonAndDeputiesOfRhList(controlledPerson, res, direct_rh_list);
		
		List<PoRole> referencedRoles = getReferencedRolesOutOfRHLinks(res);

		// iterate over all missing roles
		// and find roleholders up the orgtree
		for (PoRole actRole : roleDAO.findRolesForClient(controlledPerson.getClient(), date) ) {
			if (false == referencedRoles.contains(actRole) 
					&& actRole.getOrgType()== PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY 
					&& actRole.getDirectionOfInheritance()== PoConstants.SEARCH_DIRECTION_UP) {
				
				// search for roleholders up the orghierarchy
				int noOfRoleHoldersToAdd = 0;
				PoParentGroup parentGroup = orgService.getParentGroup(orgService.getPersonsHierarchicalGroup(controlledPerson, date), date);
				while (parentGroup != null && noOfRoleHoldersToAdd == 0) {
					PoGroup hGroup = parentGroup.getParentGroup();
					parentGroup = orgService.getParentGroup(hGroup, date);
					List<PoRoleHolderLink> rhToAdd = roleHolderDAO.findRoleHolderWithCompetence4Group(actRole, hGroup, date);
					noOfRoleHoldersToAdd = rhToAdd.size();
					res.addAll(rhToAdd);
				}
			}
		}
		
		return sortRHLinksByRoleName(res);
	}
	
	
	private List<PoRole> getReferencedRolesOutOfRHLinks(List<PoRoleHolderLink> rhLinks) {
		// 1. create a list of existing roles in the roleholder list
		List<PoRole> referencedRoles = new ArrayList<PoRole>();
		for (PoRoleHolderLink rhLink : rhLinks ) {
			PoRole role= rhLink.getRoleCompetenceBase().getRole();
			if (!referencedRoles.contains(role))
				referencedRoles.add(role);
		}
		return referencedRoles;
	}


	private List<PoRoleHolderLink> sortRHLinksByRoleName(List<PoRoleHolderLink> rhLinks) {
		// "sorts" rhLinks by appearance of rolename
		// but leaves the order of the roleholders the same as they appear in
		// the firstplace!

		List<PoRoleHolderLink> ret = new ArrayList<PoRoleHolderLink>();
		if (rhLinks != null && rhLinks.size() > 0) {
			List<String> roleNames = new ArrayList<String>();
			Map<String, List<PoRoleHolderLink>> rhMap = new HashMap<String, List<PoRoleHolderLink>>();
			Map<String, List<String>> rhMapCons = new HashMap<String, List<String>>();
			// get RoleName List and make HashMap with Rhs
			for (PoRoleHolderLink rh : rhLinks) {
				String roleName = rh.getRoleCompetenceBase().getRole().getName();
				if (!roleNames.contains(roleName)) {
					roleNames.add(roleName);
					rhMap.put(roleName, new ArrayList<PoRoleHolderLink>());
					rhMapCons.put(roleName, new ArrayList<String>());
				}

				// add to List -> filter out duplicate entries
				// (same role and roleholder tuppel)
				if (!rhMapCons.get(roleName).contains(rh.getRoleHolderObjectUID())) {
					rhMap.get(roleName).add(rh);
					rhMapCons.get(roleName).add(rh.getRoleHolderObjectUID());
				}
			}

			java.util.Collections.sort(roleNames);

			// make List again
			for (String roleName : roleNames) {
				ret.addAll(rhMap.get(roleName));
			}
		}
		return ret;
	}

	@Override
	public List<PoRoleHolderLink> findRoleHolderWithCompetenceForGroup(PoGroup controlledGroup, Date date) {
		
		// get directly linked Roleholders
		List<PoRoleHolderLink> res = roleHolderDAO.findRoleHolderWithCompetence4Group(controlledGroup, date);

		// get roleholders linked to groups client
		res.addAll(roleHolderDAO.findRoleHolderWithCompetence4Client(controlledGroup.getClient(), date));
		
		// add dynamic role holders
		
		res.addAll(roleHolderDynamicDAO.findRoleHolderDynamicWithCompetence4Group(controlledGroup, date));
		res.addAll(roleHolderDynamicDAO.findRoleHolderDynamicWithCompetence4All(date));
		
		
		// add Roleholders which have rights over all
		res.addAll(roleHolderDAO.findRoleHolderWithCompetence4All(controlledGroup
				.getClient(), date));

		
		// now iterate over all roles
		// and and find roleholders for those for whom no roleholders
		// yet exist in the list
		
		// 1. create a list of existing roles in the roleholder list
		List<PoRole> referencedRoles = new ArrayList<PoRole>();
		for (PoRoleHolderLink rhLink : res) {
			PoRole role= rhLink.getRoleCompetenceBase().getRole();
			if (!referencedRoles.contains(role))
				referencedRoles.add(role);
			
		}
		
		// iterate over all missing roles
		// and find roleholders up the orgtree
		
		for (PoRole actRole : roleDAO.findRolesForClient(controlledGroup.getClient(), date)) {
			
			if (!referencedRoles.contains(actRole) 
					&& actRole.getOrgType()== PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY 
					&& actRole.getDirectionOfInheritance()== PoConstants.SEARCH_DIRECTION_UP) {
				// search for roleholders up the orghierarchy
				
				int noOfRoleHoldersToAdd=0;
				
				PoParentGroup parentGroup = orgService.getParentGroup(controlledGroup,
						date);
				while (parentGroup != null && noOfRoleHoldersToAdd==0) {
					PoGroup hGroup = parentGroup.getParentGroup();
					parentGroup = orgService.getParentGroup(hGroup, date);
					List<PoRoleHolderLink> rhToAdd = roleHolderDAO.findRoleHolderWithCompetence4Group(actRole, hGroup, date);
					noOfRoleHoldersToAdd = rhToAdd.size();
					res.addAll(rhToAdd);
				}
			}
		}

		// sort List of Roleholders before returning them!
		return sortRHLinksByRoleName(res);
	}

	/** {@inheritDoc} */
	@Override
	public List<PoRoleHolderLink> findRoleHolderWithCompetenceForClient(PoClient client, Date date) {
		// get directly linked Roleholders
		List<PoRoleHolderLink> res = roleHolderDAO.findRoleHolderWithCompetence4Client(client, date);

		// no dynamic role holders
		// no Roleholders which have rights over all

		// sort List of Roleholders before returning them!
		return sortRHLinksByRoleName(res);
	}

	@Override
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Group(PoRole role,
			PoGroup controlledGroup, Date referenceDate) {
		return roleHolderPersonDAO.findRoleHolderPersonWithCompetence4Group(role,
				controlledGroup, referenceDate);
	}

	/** {@inheritDoc} */
	@Override
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Client(PoRole role, 
			PoClient client, Date date) {
		return roleHolderPersonDAO.findRoleHolderPersonWithCompetence4Client(role, client, date);
	}

	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Person(
			PoRole role, PoPerson person, PoPerson controlledPerson,
			Date referenceDate, boolean deputy) {
		return roleHolderPersonDAO.findRoleHolderPersonWithCompetence4Person(role,
				person, controlledPerson, referenceDate, deputy);
	}

	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Group(
			PoRole role, PoPerson person, PoGroup controlledGroup,
			Date referenceDate, boolean deputy) {
		return roleHolderPersonDAO.findRoleHolderPersonWithCompetence4Group(role,
				person, controlledGroup, referenceDate, deputy);
	}

	@Override
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4AllF(PoRole role,
			PoPerson person, Date referenceDate) {
		return roleHolderPersonDAO.findRoleHolderPersonWithCompetence4AllF(role,
				person, referenceDate);
	}

	public PoRoleHolderGroup findRoleHolderGroupWithCompetence4Person(
			PoRole role, PoGroup group, PoPerson controlledPerson,
			Date referenceDate) {
		return roleHolderGroupDAO.findRoleHolderGroupWithCompetence4Person(role, group, controlledPerson, referenceDate);
	}

	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4AllF(PoRole role,
			PoGroup group, Date referenceDate) {
		return roleHolderGroupDAO.findRoleHolderGroupWithCompetence4AllF(role, group,
				referenceDate);
	}

	public List<PoRoleCompetenceAll> findRoleCompetenceAll(PoRole role) {
		return roleCompetenceDAO.findRoleCompetenceAll(role, new Date());
	}

	@Override
	public List<PoRoleHolderPerson> findRoleHolderPersonF(PoPerson person, Date date) {
		return roleHolderPersonDAO.findRoleHolderPersonF(person, date);
	}

	@Override
	public List<PoRoleHolderPerson> findRoleHolderPersonAll(PoPerson person) {
		return roleHolderPersonDAO.findRoleHolderPersonAll(person);
	}
	
	@Override
	public List<PoRoleHolderPerson> findRoleHolderPersonF(PoRole role, Date date) {
		return roleHolderPersonDAO.findRoleHolderPersonF(role, date);
	}

	@Override
	public List<PoRoleCompetenceAll> findRoleCompetenceAll(PoRole role, Date referenceDate) {
		return roleCompetenceDAO.findRoleCompetenceAll(role, referenceDate);
	}

	@Override
	public List<PoRoleCompetenceClient> findRoleCompetenceClient(PoRole role) {
		return roleCompetenceDAO.findRoleCompetenceClient(role);
	}
	
	@Override
	public List<PoRoleCompetenceGroup> findRoleCompetenceGroup(PoRole role) {
		return roleCompetenceDAO.findRoleCompetenceGroup(role);
	}

	@Override
	public List<PoRoleCompetencePerson> findRoleCompetencePerson(PoRole role) {
		return roleCompetenceDAO.findRoleCompetencePerson(role);
	}

	@Override
	public void deleteRoleCompetence(PoRoleCompetenceBase roleCompetence) {
		roleCompetence.setValidto(new Date());
		for (PoRoleHolderGroup rhg : roleCompetence.getRoleHolderGroups() ) {
			rhg.setValidto(new Date());
			//roleDAO.save(rhg);
		}
		for (PoRoleHolderPerson rhp : roleCompetence.getRoleHolderPersons()) {
			rhp.setValidto(new Date());
			//roleDAO.saveRoleHolderPerson(rhp);
		}
		roleCompetenceDAO.save(roleCompetence);
	}


	/* COMPETENCES OF ROLES ******************************************* */

	public List<PoPerson> findCompetencePersonsOfPerson(PoPerson person, PoRole role) {
		return roleDAO.findCompetencePersonsOfPerson(person, role, new Date());
	}

	@Override
	public List<PoPerson> findCompetencePersonsOfPerson(PoPerson person, PoRole role, Date date) {
		return roleDAO.findCompetencePersonsOfPerson(person, role, date);
	}

	@Override
	public List<PoGroup> findCompetenceGroupsOfPerson(PoPerson person, PoRole role, Date date) {
		return roleDAO.findCompetenceGroupsOfPerson(person, role, date);
	}

	public List<PoGroup> findCompetenceGroupsOfPerson(PoPerson person, PoRole role) {
		return roleDAO.findCompetenceGroupsOfPerson(person, role, new Date());
	}

	@Override
	public List<PoRoleHolderGroup> findRoleHolderGroupF(PoPerson person, Date date) {
		return roleHolderGroupDAO.findRoleHolderGroupF(person, date);
	}
	
	@Override
	public List<PoRoleHolderGroup> findRoleHolderGroupAll(PoPerson person) {
		return roleHolderGroupDAO.findRoleHolderGroupAll(person);
	}
	
	@Override
	public List<PoRoleHolderGroup> findRoleHolderGroupF(PoRole role, Date date) {
		return roleHolderGroupDAO.findRoleHolderGroupF(role, date);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.workflow.webdesk.po.PoRoleService#findRHGroupF(at.workflow.webdesk.po.model.PoPerson,
	 *      java.util.Date)
	 */
	@Override
	public List<PoRoleHolderGroup> findRoleHolderGroupF(PoGroup group, Date date) {
		return roleHolderGroupDAO.findRoleHolderGroupF(group, date);
	}

	
	@Override
	public List<PoRoleHolderGroup> findRoleHolderGroupAll(PoGroup group) {
		return roleHolderGroupDAO.findRoleHolderGroupAll(group);
	}
	
	
	@Override
	public void changeValidityRHPersonLink(PoRoleHolderPerson rhPersonLink, Date validFrom, Date validTo) {
		if (validFrom != null) {
			if (validFrom.before(new Date()))
				validFrom = new Date();
		} else	{
			validFrom = new Date();
		}
		
		if (validTo == null)
			validTo = PoConstants.getInfDate();
		
		changeValidityRHPersonLinkReally(rhPersonLink, validFrom, validTo, false);
		
		// the rhPersonLink changes its date! 
		// check if a deputy exist, if true then change validity of deputy link as well 
		for (PoRoleDeputy rd : findRoleDeputiesOfPersonF(
				rhPersonLink.getPerson(), rhPersonLink.getRoleCompetenceBase().getRole(), new Date())) {
			
			Date cValidFrom = rhPersonLink.getValidfrom();
			Date cValidTo = rhPersonLink.getValidto();
			if (cValidFrom.before(rd.getValidfrom()))
				cValidFrom = rd.getValidfrom();
			if (cValidTo.after(rd.getValidto()))
				cValidTo = rd.getValidto();
			
			updateDeputy(rd);
		}
	}
	
	private void changeValidityRHPersonLinkReally(PoRoleHolderPerson rhPersonLink,
			Date validFrom, Date validTo, boolean isDeputy) {
		PoRoleCompetenceBase rcb = rhPersonLink.getRoleCompetenceBase();
		// check overlappings
		List<PoRoleHolderPerson> overlappedRoleHolder = getOverlappedRoleHolderPersons(rhPersonLink.getPerson(), rcb, 
				validFrom, validTo, isDeputy);
		overlappedRoleHolder.remove(rhPersonLink);
		if (overlappedRoleHolder.size()!=0) {
			List<PoRoleHolderPerson> toRemove = new ArrayList<PoRoleHolderPerson>();
			rearrangeRoleHolderPersons(overlappedRoleHolder, validFrom, validTo, toRemove);
			rcb.getRoleHolderPersons().removeAll(toRemove);
		} else {
			rhPersonLink.setValidfrom(validFrom);
			rhPersonLink.setValidto(validTo);
			roleHolderPersonDAO.save(rhPersonLink);
		}
	}

	/** Not implemented, throws exception! */
	public void changeValidityRH(PoRoleCompetenceBase rh, Date validfrom, Date validto) {
		throw new PoRuntimeException("Not implemented yet");
	}

	@Override
	public PoRole findRoleByParticipantId(String key) {
		return roleDAO.findRoleByParticipantId(key, new Date());
	}

	@Override
	public PoRole findRoleByParticipantId(String key, Date effectiveDate) {
		return roleDAO.findRoleByParticipantId(key, effectiveDate);
	}

	@Override
	public PoRole findRoleByParticipantId(String key, PoClient client) {
		return roleDAO.findRoleByParticipantId(key, client, new Date());
	}

	@Override
	public PoRole findRoleByParticipantId(String key, PoClient client, Date effectiveDate) {
		return roleDAO.findRoleByParticipantId(key, client, effectiveDate);
	}

	@Override
	public List<PoPerson> findCompetencePersonsOfGroup(PoGroup group, PoRole role,
			Date date) {
		return roleDAO.findCompetencePersonsOfGroup(group, role, date);
	}

	@Override
	public List<PoGroup> findCompetenceGroupsOfGroup(PoGroup group, PoRole role,
			Date date) {
		return roleDAO.findCompetenceGroupsOfGroup(group, role, date);
	}

	@Override
	public List<PoClient> findCompetenceClientsOfPerson(PoPerson person, PoRole role,
			Date date) {
		return roleDAO.findCompetenceClientsOfPerson(person, role, date);
	}
	
	@Override
	public List<PoClient> findCompetenceClientsOfGroup(PoGroup group, PoRole role,
			Date date) {
		return roleDAO.findCompetenceClientsOfGroup(group, role, date);
	}
	
	@Override
	public List<PoRoleHolderPerson> findRoleHolderPersonsWithRoleF(PoRole role, Date date) {
		return roleHolderPersonDAO.findRoleHolderPersonsWithRoleF(role, date);
	}

	@Override
	public Object findPerformerOfDummyRole(PoRole role, Date effectiveDate) {
		return roleDAO.findPerformerOfDummyRole(role, effectiveDate);
	}

	@Override
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Person(PoRole role,
			PoPerson person, Date date) {
		return roleHolderPersonDAO.findRoleHolderPersonWithCompetence4Person(role, person,
				date);
	}

	@Override
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4Group(PoRole r,
			PoGroup group, Date date) {
		return roleHolderGroupDAO.findRoleHolderGroupWithCompetence4Group(r, group, date);
	}

	@Override
	public List<PoRole> findAllActiveRoles() {
		return roleDAO.findAllActiveRoles();
	}

	@Override
	public boolean hasPersonRoleAssigned(PoPerson p, PoRole r, Date date) {
		return roleHolderDAO.hasPersonRoleAssigned(p, r, date);
	}

	@Override
	public boolean hasPersonRoleAssignedWithCompetence4All(PoPerson p,
			PoRole r, Date date) {
		return roleHolderDAO.hasPersonRoleAssignedWithCompetence4All(p, r, date);
	}

	@Override
	public List<PoRoleCompetenceBase> findRoleCompetence(PoRole role) {
		return roleCompetenceDAO.findRoleCompetence(role);
	}

	@Override
	public List<PoRole> findRolesForClient(PoClient client, Date date) {
		return roleDAO.findRolesForClient(client, date);
	}

	public List<PoRoleDeputy> findRoleDeputiesOfPerson(PoPerson person, PoRole role, Date date) {
		return roleDeputyDAO.findRoleDeputiesOfPerson(person, role, date);
	}
	
	@Override
	public PoRoleDeputy saveDeputy(PoRoleDeputy deputy) {
		if (deputy.getOfficeHolder().equals(deputy.getDeputy()))
			throw new PoRuntimeException("Deputy and Officeholder are equal");
		
		Date now = new Date();
		if (deputy.getValidfrom().before(now))
			deputy.setValidfrom(now);
		
		if (deputy.getUID() == null) {
			roleDeputyDAO.evict(deputy);
			if (deputy.getRole() != null &&
					deputy.getOfficeHolder() != null &&
					deputy.getDeputy() != null)
				deputy = generateDeputy(deputy.getRole(), deputy.getOfficeHolder(), deputy.getDeputy(), deputy.getValidfrom(), deputy.getValidto());
		}
		else {
			updateDeputy(deputy);
		}
		return deputy;
	}

	
	@Override
	public PoRoleDeputy generateDeputy(PoRole role, PoPerson officeHolder, PoPerson deputy, Date validFrom, Date validTo) {
		boolean merged = false;
		PoRoleDeputy roleDeputy = null;
		List<PoRoleDeputy> existingDep = roleDeputyDAO.findRoleDeputiesOfPerson(officeHolder, validFrom);
		
		
		if (existingDep!=null) {
			Iterator<PoRoleDeputy> depI = existingDep.iterator();
			while (depI.hasNext()) {
				roleDeputy = depI.next();
				// validfrom und validto können sich geändert haben. -> anpassung der angehängten objekte
				// FIXME: in a testcase the roleHolderPersons were not correctly filled
				// thus the object is refreshed first. 
				// wondering if this happens in reality -> performance
				orgService.refresh(roleDeputy);
				// we have to set the role of the deputy at this place
				// entries will be deleted afterwards
				if (roleDeputy.getRole().equals(role) && roleDeputy.getOfficeHolder().equals(officeHolder) 
						&& DateTools.doesOverlap(roleDeputy.getValidfrom(), roleDeputy.getValidto(), validFrom, validTo)
						&& roleDeputy.getDeputy().equals(deputy)) {
					// merge role deputies
					if (roleDeputy.getValidfrom().after(validFrom))
						roleDeputy.setValidfrom(validFrom);
					if (roleDeputy.getValidto().before(validTo))
						roleDeputy.setValidto(validTo);
					
					roleDeputy.setRole(role);
					roleDeputy.setDeputy(deputy);
					
					updateDeputy(roleDeputy);
					merged = true;
				}
			}
		}
		if (!merged) {
		
			roleDeputy = new PoRoleDeputy();
			roleDeputy.setRole(role);
			roleDeputy.setOfficeHolder(officeHolder);
			roleDeputy.setValidfrom(validFrom);
			roleDeputy.setValidto(validTo);
			roleDeputy.setDeputy(deputy);
			generateRhpsForDeputy(roleDeputy);
		}
		return roleDeputy;
	}
	
	
	
	
	private void generateRhpsForDeputy(PoRoleDeputy roleDeputy) {
		Date validFrom = roleDeputy.getValidfrom();
		Date validTo = roleDeputy.getValidto();
		PoRole role = roleDeputy.getRole();
		PoPerson officeHolder = roleDeputy.getOfficeHolder();
		PoPerson deputy = roleDeputy.getDeputy();
		
		List<PoRoleHolderPerson> rhpListOfOfficeHolder = findRoleHolderPersonWithCompetence4AllF(
				role, officeHolder, validFrom);
		rhpListOfOfficeHolder.addAll(
				roleHolderPersonDAO.findRoleHolderPersonWithCompetence4PersonF(role, officeHolder, validFrom));
		
		rhpListOfOfficeHolder.addAll(
				roleHolderPersonDAO.findRoleHolderPersonWithCompetence4GroupF(role, officeHolder, validFrom));
		
		for (PoRoleHolderPerson rhp : rhpListOfOfficeHolder) {
			if (logger.isDebugEnabled())
				logger.debug("Copy rhp with " + rhp.getPerson().getUserName() + " and target " + rhp.getRoleCompetenceBase() + " and timerange: " + rhp.getValidfrom() + " " + rhp.getValidto());
			if (DateTools.doesOverlap(rhp.getValidfrom(), rhp.getValidto(),
					validFrom, validTo, true) && 
					rhp.getDeputy()==null) {
			
				Date cValidFrom;
				Date cValidTo;
				// define the timerange
				if (validFrom.after(rhp.getValidfrom()))
					cValidFrom = validFrom;
				else
					cValidFrom = rhp.getValidfrom();
				
				if (validTo.before(rhp.getValidto()))
					cValidTo = validTo;
				else
					cValidTo = rhp.getValidto();
				
				// copy the object, but set a new owner
				if (rhp.getRoleCompetenceBase() instanceof PoRoleCompetencePerson) {
					PoRoleCompetencePerson rcp = (PoRoleCompetencePerson) rhp.getRoleCompetenceBase();
					
					// avoid that a deputy gets competence for himself.
					if (!rcp.getCompetence4Person().equals(deputy)) {
						// deputy is set to false during search, because it is not set already 
						PoRoleHolderPerson rhp_d = assignRoleWithPersonCompetenceReally(role, deputy, 
								rcp.getCompetence4Person(), cValidFrom, cValidTo, rhp.getRanking()+1, true);
						rhp_d.setDeputy(roleDeputy);
						roleDeputy.addRoleHolderPerson(rhp_d);
						roleHolderPersonDAO.save(rhp_d);
					}
				} else if (rhp.getRoleCompetenceBase() instanceof PoRoleCompetenceGroup) {
					PoRoleCompetenceGroup rcg = (PoRoleCompetenceGroup) rhp.getRoleCompetenceBase();
					PoRoleHolderPerson rhp_d  = assignRoleWithGroupCompetenceReally(role, deputy, rcg.getCompetence4Group(), 
							cValidFrom, cValidTo, rhp.getRanking()+1, true);
					rhp_d.setDeputy(roleDeputy);
					roleDeputy.addRoleHolderPerson(rhp_d);
					roleHolderPersonDAO.save(rhp_d);
				} else {
					// A role with competence 4 all 
					
					PoRoleHolderPerson rhp_d = assignRoleReally(role, deputy, cValidFrom, cValidTo, rhp.getRanking()+1, true);
					rhp_d.setDeputy(roleDeputy);
					roleDeputy.addRoleHolderPerson(rhp_d);
					roleHolderPersonDAO.save(rhp_d);
				}
				roleDeputyDAO.save(roleDeputy);
			}
		}
		
	}

	@Override
	public void updateDeputy(PoRoleDeputy roleDeputy) {
		Iterator<PoRoleHolderPerson> rdI = roleDeputy.getRoleHolderPersons().iterator();
		PoRole role = roleDeputy.getRole();
		PoPerson deputy = roleDeputy.getDeputy();
		
		while (rdI.hasNext()) {
			PoRoleHolderPerson rhp = rdI.next();
			if (rdI.hasNext()) {
				
				rhp.getRoleCompetenceBase().getRoleHolderPersons().remove(rhp);
				rdI.remove();
				
				// delete old rhps, as update is risky and possibly not very performant
				// -> we loose some information
				// -> we should have a general discussion about which information we want to store, as 
				// actually there is sometimes information lost...
				roleHolderPersonDAO.delete(rhp);
			} else{
				// keep one link
				rhp.setValidto(new Date());
				roleHolderPersonDAO.save(rhp);
			}
		}
		
		roleDeputy.setRole(role);
		roleDeputy.setDeputy(deputy);
		generateRhpsForDeputy(roleDeputy);
		roleDeputyDAO.save(roleDeputy);
	}
	
	@Override
	public void deleteDeputy(PoRoleDeputy roleDeputy) {
		for (PoRoleHolderPerson rhp : roleDeputy.getRoleHolderPersons()) {
			rhp.setValidto(new Date());
			saveRoleHolderPerson(rhp);
		}
		roleDeputy.setValidto(new Date());
		roleDeputyDAO.save(roleDeputy);
	}
	

	@Override
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4PersonF(PoRole role, PoPerson officeHolder, PoPerson target, Date date) {
		return roleHolderPersonDAO.findRoleHolderPersonWithCompetence4PersonF(role, officeHolder, target, date);
	}

	@Override
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4ClientF(PoRole role, PoGroup officeHolder, PoClient target, Date date) {
		return roleHolderGroupDAO.findRoleHolderGroupWithCompetence4ClientF(role, officeHolder, target, date);
	}

	@Override
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4GroupF(PoRole role, PoGroup officeHolder, PoGroup target, Date date) {
		return roleHolderGroupDAO.findRoleHolderGroupWithCompetence4GroupF(role, officeHolder, target, date);
	}

	@Override
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4PersonF(PoRole role, PoGroup officeHolder, PoPerson target, Date date) {
		return roleHolderGroupDAO.findRoleHolderGroupWithCompetence4PersonF(role, officeHolder, target, date);
	}

	@Override
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4ClientF(PoRole role, PoPerson officeHolder, PoClient target, Date date) {
		return roleHolderPersonDAO.findRoleHolderPersonWithCompetence4ClientF(role, officeHolder, target, date);
	}
	
	@Override
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4GroupF(PoRole role, PoPerson officeHolder, PoGroup target, Date date) {
		return roleHolderPersonDAO.findRoleHolderPersonWithCompetence4GroupF(role, officeHolder, target, date);
	}
	
	@Override
	public List<PoRoleHolderGroup> findDistinctRoleHolderGroupsWithCompetence4Person(PoRole role, PoPerson controlledPerson, Date date) {
		return roleHolderGroupDAO.findDistinctRoleHolderGroupsWithCompetence4Person(role, controlledPerson, date);
	}

	@Override
	public void saveRoleHolderGroup(PoRoleHolderGroup rhg) {
		roleHolderGroupDAO.save(rhg);
	}

	@Override
	public void assignDynamicRoleHolder(int dynamicType, PoRole role, String target,
			String targetId, Date validFrom, Date validTo) {
		
		if (target.equals(DYNAMIC_TARGET_ALL)) {
			assignDynamicRoleHolderWithCompetence4All(dynamicType, role, validFrom, validTo);
		} else if (target.equals(DYNAMIC_TARGET_PERSON)) {
			assignDynamicRoleHolderWithCompetence4Person(dynamicType, role, orgService.getPerson(targetId), validFrom, validTo);
		} else if (target.equals(DYNAMIC_TARGET_GROUP)) {
			assignDynamicRoleHolderWithCompetence4Group(dynamicType, role, orgService.getGroup(targetId), validFrom, validTo);
		}
		
	}

	private PoRoleHolderDynamic assignDynamicRoleHolderWithCompetence4All(int dynamicType, PoRole role, 
			Date validFrom, Date validTo) {
		
		if (validFrom == null)
			validFrom = new Date();
		else
			if (validFrom.before(new Date()))
				validFrom.setTime(new Date().getTime());
		
		if (validTo==null)
			validTo = PoConstants.getInfDate();
		
		PoRoleHolderDynamic genRhd=null;
//		only one role competence should exist at one time
// 		the date is not really important ... 
		List<PoRoleCompetenceAll> l_rh = roleCompetenceDAO.findRoleCompetenceAllF(role, validFrom);  
		
		// if more than one rolecompetence exists, the data is not correct 
		// ev. we should correct the data at this place! 
		if (l_rh.size() > 1) {
			rearrangeDataToEnsureConsistency(l_rh);
		}
			
		if (l_rh.size() == 1) {
			
			// exactly one role holder exists
			PoRoleCompetenceAll rh = l_rh.get(0);

			List<PoRoleHolderDynamic> overlappedRoleHolder = getOverlappedRoleHolderDynamics(dynamicType, rh, validFrom, validTo);
			
			if (overlappedRoleHolder.size()==0) {
				PoRoleHolderDynamic rhd = new PoRoleHolderDynamic();
				rhd.setRoleHolder(rh);
				rhd.setRoleHolderType(dynamicType);
				rhd.setValidfrom(validFrom);
				if (validTo == null)
					rhd.setValidto(PoConstants.getInfDate());
				else
					rhd.setValidto(validTo);
				rh.getRoleHolderDynamics().add(rhd);
				roleHolderDynamicDAO.save(rhd);
				role.addRoleHolder(rh);
				genRhd = rhd;
			} else {
				List<PoRoleHolderDynamic> toRemove = new ArrayList<PoRoleHolderDynamic>();
				genRhd = rearrangeRoleHolderDynamics(overlappedRoleHolder, validFrom, validTo, toRemove); 
				rh.getRoleHolderDynamics().removeAll(toRemove);
				saveRoleCompetence(rh);
			}
		} else { // no role holder exists
			PoRoleCompetenceAll rh = new PoRoleCompetenceAll();
			rh.setRole(role);
			rh.setType(PoConstants.ROLE_HOLDER_BASE_ALL.toString());
			rh.setValidfrom(new Date());
			// validto is only used to simulate deletion 
			rh.setValidto(PoConstants.getInfDate());
			roleCompetenceDAO.save(rh);
			
			PoRoleHolderDynamic rhd = new PoRoleHolderDynamic();
			rhd.setRoleHolder(rh);
			rhd.setRoleHolderType(dynamicType);
			rhd.setValidfrom(validFrom);
			rhd.setValidto(validTo);
			rh.getRoleHolderDynamics().add(rhd); // ev. NullPointer
			
			
			roleHolderDynamicDAO.save(rhd);
			role.addRoleHolder(rh);
			genRhd = rhd;
		}
		return genRhd;
		
	}

	private void assignDynamicRoleHolderWithCompetence4Group(int dynamicType, PoRole role, 
			PoGroup controlledGroup, Date validFrom, Date validTo) {
		
		if (validFrom == null)
			validFrom = new Date();
		else
			if (validFrom.before(new Date()))
				validFrom.setTime(new Date().getTime());
		
		if (validTo==null)
			validTo = PoConstants.getInfDate();

		List<PoRoleCompetenceGroup> l = roleCompetenceDAO.findRoleCompetenceGroupWithCompetence4GroupF(
				role, controlledGroup, validFrom);
		
		if (l.size() > 1)
			rearrangeDataToEnsureConsistency(l);
		
		if (l.size() == 1) {
			
			PoRoleCompetenceGroup rh = l.get(0);
			
			List<PoRoleHolderDynamic> overlappedRoleHolder = getOverlappedRoleHolderDynamics(dynamicType, rh, validFrom, validTo);
			
			if (overlappedRoleHolder.size()==0) {
				PoRoleHolderDynamic rhd = new PoRoleHolderDynamic();
				rhd.setRoleHolder(rh);
				rhd.setValidfrom(validFrom);
				if (validTo != null)
					rhd.setValidto(validTo);
				else
					rhd.setValidto(PoConstants.getInfDate());
				rhd.setRoleHolderType(dynamicType);
				rh.getRoleHolderDynamics().add(rhd);
				roleHolderDynamicDAO.save(rhd);
			} else {
				List<PoRoleHolderDynamic> toRemove = new ArrayList<PoRoleHolderDynamic>(); 
				rearrangeRoleHolderDynamics(overlappedRoleHolder, validFrom, validTo, toRemove);
				rh.getRoleHolderDynamics().removeAll(toRemove);
			}
		} else {
			PoRoleCompetenceGroup rhcg = generateRoleCompetenceGroup(role, controlledGroup);
			
			PoRoleHolderDynamic rhd = new PoRoleHolderDynamic();
			rhd.setRoleHolder(rhcg);
			rhd.setRoleHolderType(dynamicType);
			rhd.setValidfrom(validFrom);
			if (validTo==null)
				rhd.setValidto(PoConstants.getInfDate());
			else
				rhd.setValidto(validTo);
			rhcg.getRoleHolderDynamics().add(rhd);
			role.addRoleHolder(rhcg);

			roleCompetenceDAO.save(rhcg);
			roleHolderDynamicDAO.save(rhd);
		}
		
		
	}

	private void assignDynamicRoleHolderWithCompetence4Person(int dynamicType, PoRole role, 
			PoPerson controlledPerson, Date validFrom, Date validTo) {
		if (validFrom == null)
			validFrom = new Date();
		else
			if (validFrom.before(new Date()))
				validFrom.setTime(new Date().getTime());

		if (validTo==null)
			validTo = PoConstants.getInfDate();
		
		List<PoRoleCompetencePerson> l = roleCompetenceDAO.findRoleCompetencePersonWithCompetence4PersonF(
				role, controlledPerson, validFrom);
		
		if (l.size() > 1)
			rearrangeDataToEnsureConsistency(l);
		
		if (l.size() == 1) {
			
			PoRoleCompetencePerson rh = l.get(0);
			
			List<PoRoleHolderDynamic> overlappedRoleHolder = getOverlappedRoleHolderDynamics(dynamicType, rh, validFrom, validTo);
			
			if (overlappedRoleHolder.size()==0) {
				PoRoleHolderDynamic rhd = new PoRoleHolderDynamic();
				rhd.setRoleHolder(rh);
				rhd.setValidfrom(validFrom);
				rhd.setRoleHolderType(dynamicType);
				if (validTo != null)
					rhd.setValidto(validTo);
				else
					rhd.setValidto(PoConstants.getInfDate());
				
				rh.getRoleHolderDynamics().add(rhd);
				roleHolderDynamicDAO.save(rhd);
			} else {
				List<PoRoleHolderDynamic> toRemove = new ArrayList<PoRoleHolderDynamic>(); 
				rearrangeRoleHolderDynamics(overlappedRoleHolder, validFrom, validTo, toRemove);
				rh.getRoleHolderDynamics().removeAll(toRemove);
			}
		} else {
			PoRoleCompetencePerson rhcp = generateRoleCompetencePerson(role, controlledPerson);
			
			PoRoleHolderDynamic rhd = new PoRoleHolderDynamic();
			rhd.setRoleHolder(rhcp);
			rhd.setRoleHolderType(dynamicType);
			rhd.setValidfrom(validFrom);
			if (validTo==null)
				rhd.setValidto(PoConstants.getInfDate());
			else
				rhd.setValidto(validTo);
			rhcp.getRoleHolderDynamics().add(rhd);
			role.addRoleHolder(rhcp);

			roleCompetenceDAO.save(rhcp);
			roleHolderDynamicDAO.save(rhd);
		}
	}
	
	
	@Override
	public List<PoRoleHolderDynamic> findRoleHolderDynamicF(PoRole role, Date date) {
		return roleHolderDynamicDAO.findRoleHolderDynamicF(role, date);
	}

	@Override
	public PoRoleHolderDynamic getRoleHolderDynamic(String uid) {
		return roleHolderDynamicDAO.get(uid);
	}

	@Override
	public void deleteAndFlushRoleHolderDynamic(PoRoleHolderDynamic rhd) {
		roleHolderDynamicDAO.delete(rhd);
		
	}

	@Override
	public void saveRoleHolderDynamic(PoRoleHolderDynamic rhd) {
		roleHolderDynamicDAO.save(rhd);
	}

	@Override
	public List<PoRoleCompetenceBase> findRoleCompetences(PoRole role, Date date) {
		long st = System.currentTimeMillis();
		List<PoRoleCompetenceBase> res = roleHolderDAO.findRoleHolders(role, date);
		
		if (logger.isDebugEnabled())
			logger.debug("duration of findRoleHolders:" + (System.currentTimeMillis() - st) + " ms.");
		
		return res; 
	}

	@Override
	public List<PoRole> findRoles(PoPerson person, Date date) {
		List<PoRole> res = roleDAO.findRoles(person, date);
		// eventually we can add a role (dynamic role holders) 	
		Iterator<PoRoleHolderDynamic> i = roleHolderDynamicDAO.findRoleHolderDynamic(date).iterator();
		while (i.hasNext()) {
			PoRoleHolderDynamic rhd = i.next();
			if (rhd.getRoleHolderType()==PoRoleService.DYNAMIC_TYPE_ALL_CLIENTS || 
					(rhd.getRoleHolderType()==PoRoleService.DYNAMIC_TYPE_OWN_CLIENT) &&
						(rhd.getRoleCompetenceBase().getRole().getClient()==null ||
								rhd.getRoleCompetenceBase().getRole().getClient().equals(
										person.getClient()))) {
				if (!res.contains(rhd.getRoleCompetenceBase().getRole()))
					res.add(rhd.getRoleCompetenceBase().getRole());
			} else {
				if (rhd.getRoleHolderType()==PoRoleService.DYNAMIC_TYPE_OWN_HIERARCHY) {
					if (rhd.getRoleCompetenceBase() instanceof PoRoleCompetencePerson) {
						PoRoleCompetencePerson rcp = (PoRoleCompetencePerson) rhd.getRoleCompetenceBase();
						if (orgService.getPersonsHierarchicalGroup(person, date).equals(
								orgService.getPersonsHierarchicalGroup(rcp.getCompetence4Person(), date)))
							if (!res.contains(rcp.getRole()))
								res.add(rcp.getRole());
					} else if (rhd.getRoleCompetenceBase() instanceof PoRoleCompetenceGroup) {
						PoRoleCompetenceGroup rcg = (PoRoleCompetenceGroup) rhd.getRoleCompetenceBase();
						if (orgService.getPersonsHierarchicalGroup(person, date).equals(
								rcg.getCompetence4Group()))
							if (!res.contains(rcg.getRole()))
								res.add(rcg.getRole());
					} else if (rhd.getRoleCompetenceBase() instanceof PoRoleCompetenceAll) {
						if (!res.contains(rhd.getRoleCompetenceBase().getRole()))
							res.add(rhd.getRoleCompetenceBase().getRole());
					}
				} else if (rhd.getRoleHolderType()==PoRoleService.DYNAMIC_TYPE_OWN_HIERARCHYPLUS) {
					if (rhd.getRoleCompetenceBase() instanceof PoRoleCompetencePerson) {
						PoRoleCompetencePerson rcp = (PoRoleCompetencePerson) rhd.getRoleCompetenceBase();
						
						if (orgService.isGroupChildGroup(
								orgService.getPersonsHierarchicalGroup(person, date), //child group 
								orgService.getPersonsHierarchicalGroup(rcp.getCompetence4Person(), date))!=null)
							if (!res.contains(rcp.getRole()))
								res.add(rcp.getRole());
					} else if (rhd.getRoleCompetenceBase() instanceof PoRoleCompetenceGroup) {
						PoRoleCompetenceGroup rcg = (PoRoleCompetenceGroup) rhd.getRoleCompetenceBase();
						if (orgService.isGroupChildGroup(
								orgService.getPersonsHierarchicalGroup(person, date), //child group 
								rcg.getCompetence4Group())!=null)
							if (!res.contains(rcg.getRole()))
								res.add(rcg.getRole());
					} else if (rhd.getRoleCompetenceBase() instanceof PoRoleCompetenceAll) {
						
						PoRole role = rhd.getRoleCompetenceBase().getRole();
						
						if (!res.contains(role))
							res.add(role);
					}
				}
			}
		}
		return res;
	}

	@Override
	public List<PoRole> findRoles(PoGroup group, Date date) {
		return roleDAO.findRoles(group, date);
	}

	@Override
	public List<PoRole> findRolesF(PoGroup group, Date date) {
		return roleDAO.findRolesF(group, date);
	}
	@Override
	public List<PoRoleHolderPerson> findRoleHolderPerson(PoRole role, Date date) {
		return roleHolderPersonDAO.findRoleHolderPerson(role, date);
	}

	// DI Setters
	public void setOrgService(PoOrganisationService orgService) {
		this.orgService = orgService;
	}

	public void setRoleHolderDynamicDAO(PoRoleHolderDynamicDAO roleHolderDynamicDAO) {
		this.roleHolderDynamicDAO = roleHolderDynamicDAO;
	}

	public void setRoleHolderDAO(PoRoleHolderDAO roleHolderDAO) {
		this.roleHolderDAO = roleHolderDAO;
	}

	public void setRoleDeputyDAO(PoRoleDeputyDAO roleDeputyDAO) {
		this.roleDeputyDAO = roleDeputyDAO;
	}

	public void setRoleCompetenceDAO(PoRoleCompetenceDAO roleCompetenceDAO) {
		this.roleCompetenceDAO = roleCompetenceDAO;
	}

	public void setRoleHolderGroupDAO(PoRoleHolderGroupDAO roleHolderGroupDAO) {
		this.roleHolderGroupDAO = roleHolderGroupDAO;
	}

	public void setRoleHolderPersonDAO(PoRoleHolderPersonDAO roleHolderPersonDAO) {
		this.roleHolderPersonDAO = roleHolderPersonDAO;
	}

	@Override
	public List<PoPerson> findRoleHolders(PoRole role) {
		final Date now = DateTools.now();
		final List<PoPerson> persons = new ArrayList<PoPerson>();
		
		List<PoRoleHolderPerson> roleHolderPersons = findRoleHolderPerson(role, now);
		for (PoRoleHolderPerson roleHolderPerson : roleHolderPersons)
			persons.add(roleHolderPerson.getPerson());
		
		List<PoRoleHolderGroup> roleHolderGroups = roleHolderGroupDAO.findRoleHolderGroup(role, now);
		for (PoRoleHolderGroup roleHolderGroup : roleHolderGroups)
			persons.addAll(orgService.findPersonsOfGroup(roleHolderGroup.getGroup(), now));
		
		return persons;
	}

	@Override
	public List<PoRoleDeputy> findRoleDeputiesOfPerson(PoPerson person) {
		return roleDeputyDAO.findRoleDeputiesOfPerson(person, new Date());
	}


	@Override
	public boolean isAuthorizedToUseJsonpWebservices(String userName) {
		assert userName!=null;
		
		// if no role is defined, assume we do not want any extra authorization step!
		if (StringUtils.isBlank(options.getRoleNameForJsonpWebServiceCalls()))
			return true;
		
		PoPerson person = orgService.findPersonByUserName(userName);
		if (person==null)
			throw new IllegalArgumentException("No valid user could be found with passed userName=" + userName);
		
		return hasPersonRole(person, options.getRoleNameForJsonpWebServiceCalls());
	}

	public void setOptions(PoOptions options) {
		this.options = options;
	}


}
