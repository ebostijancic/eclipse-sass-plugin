package at.workflow.webdesk.po.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.tools.NamingConventions;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * This is the utility for automatic linking of entities (like PoGroup) with
 * <ul>
 * 	<li>another entity (like PoPerson)</li>
 * 	<li>a parent object (like its parent PoGroup)</li>
 * </ul>
 * 
 * Nothing is persisted by this utility.
 * <p>
 * 
 * It offers (beside others) two different methods with similar purpose:
 * <ul><li>
 * <b>getLinkObject()</b>: creating a link object and setting the related objects into it,
 * and setting the link object's validity dates (required!)
 * </li><li>
 * <b>link()</b>: doing what getLinkObject() does with only optional required validity dates,
 * and adding the link object to the linked entities (calls adder methods)
 * </li></ul>
 * 
 * What does <b>getLinkObject()</b>:
 * </ul><li>
 * the linking class is either supplied or discovered by reflection based on naming conventions,
 *  e.g. PoParentGroup or PoGroupParent, whichever exists for PoGroup/PoGroup relationship
 * </li><li>
 * creates the linking object
 * </li><li>
 * links created linking object with both the linked objects, e.g. group and parent group
 * </li><li>
 * fills supplied dates
 * </li><li>
 * the created and linked linking object is returned.
 * </li></ul>
 * <p>
 * 
 * What does <b>link()</b>: 
 * <ul><li>
 * the linking class is either supplied or discovered by reflection based on naming conventions,
 * e.g. PoParentGroup or PoGroupParent, whichever exists for PoGroup/PoGroup relationship
 * </li><li>
 * creates the linking object
 * </li><li>
 * links created linking object with both the linked objects, e.g. group and parent group
 * </li><li>
 * links both linked objects to the link object, i.e. all three entities are fully linked and need just to be stored
 * </li><li>
 * if the linking object is historizable, validity dates are set when provided
 * </li><li>
 * the created and linked linking object is returned.
 * </li></ul>
 * 
 * TODO: this class hard-codes a lot of naming conventions, and there is neither documentation (Daisy)
 * 		about those conventions, nor is there a check if the conventions have been kept!
 * 
 * TODO: do not force callers to pass a method reference by name (search "entity1AddMethodName"),
 * 		this violates typing and makes refactoring impossible.
 * 
 * @author sdzuban 18.03.2013
 */
public class PoLinkingUtils {

	private static class NamesAndClass {
		
		private String nameWithoutModule1, nameWithoutModule2;
		private Class<? extends PersistentObject> linkClass;

		public NamesAndClass(String nameWithoutModule1, String nameWithoutModule2, Class<? extends PersistentObject> linkClass) {
			super();
			this.nameWithoutModule1 = nameWithoutModule1;
			this.nameWithoutModule2 = nameWithoutModule2;
			this.linkClass = linkClass;
		}

		public String getNameWithoutModule1() {
			return nameWithoutModule1;
		}
		public String getNameWithoutModule2() {
			return nameWithoutModule2;
		}
		public Class<? extends PersistentObject> getLinkClass() {
			return linkClass;
		}
	}

	private static final String PACKAGE_BASE = "at.workflow.webdesk";

	/**
	 * Method for automatic creation of historizable linking entity like PoPersonGroup
	 * USAGE: 		PoLinkingUtils.link(person, group, from, to);
	 * @param entity1 entity to be linked, e.g. PoPerson or child PoGroup
	 * @param entity2 entity to be linked, e.g. PoGroup or parent PoGroup
	 * @param validFrom nullable, applied only when linking object is historizable
	 * @param validTo nullable, applied only when linking object is historizable
	 * @return linking entity object, unpersisted
	 */
	public static Historization getLinkObject(PersistentObject entity1, PersistentObject entity2, Date validFrom, Date validTo) {
		return getLinkObject(null, entity1, entity2, validFrom, validTo);
	}

	/**
	 * Method for automatic creation of historizable linking entity like HrJobFamilyMember 
	 * whose name is not derivable from class names of linked entities HrJobFamily and HrJob
	 * USAGE: 		PoLinkingUtils.getLinkObject(HrJobFamilyMember.class, jobFamily, job, from, to);
	 * @param linkClass class of linking entity, if null the class will be derived from linked entities
	 * @param entity1 entity to be linked, e.g. PoPerson or child PoGroup
	 * @param entity2 entity to be linked, e.g. PoGroup or parent PoGroup
	 * @param validFrom nullable, applied only when linking object is historizable
	 * @param validTo nullable, applied only when linking object is historizable
	 * @return linking entity object, unpersisted
	 */
	public static Historization getLinkObject(
			Class<? extends PersistentObject> linkClass,
			PersistentObject entity1,
			PersistentObject entity2,
			Date validFrom,
			Date validTo)
	{
		NamesAndClass namesAndClass = getEntityNamesAndLinkClass(linkClass, entity1, entity2);

		try {
			PersistentObject link = namesAndClass.getLinkClass().newInstance();

			// TODO: code duplication
			if (namesAndClass.getNameWithoutModule1().equals(namesAndClass.getNameWithoutModule2())) { // parent-child relationship
				linkEntity(link, entity1, "setChild" + namesAndClass.getNameWithoutModule1());
				linkEntity(link, entity2, "setParent" + namesAndClass.getNameWithoutModule1());
			} else {
				linkEntity(link, entity1, null);
				linkEntity(link, entity2, null);
			}
			
			Historization histLink = (Historization) link;
			histLink.setValidfrom(validFrom);
			histLink.setValidto(validTo);
			return histLink;
		}
		catch (Exception e) {
			throw new RuntimeException("Link entity could not be linked: " + e, e);
		}
	}

	/**
	 * Basic method for automatic creation and linking of linking entity like PoPersonGroup
	 * USAGE: 		PoLinkingUtils.link(person, group);
	 * @param entity1 entity to be linked, e.g. PoPerson
	 * @param entity2 entity to be linked, e.g. PoGroup
	 * @return linking entity object linked to both entities by calling e.g. 'addPersonGroup', unpersisted
	 */
	public static PersistentObject link(PersistentObject entity1, PersistentObject entity2) {
		return link(entity1, entity2, null, null, null, null);
	}

	/**
	 * Basic method for automatic creation and linking of linking entity like PoPersonGroup
	 * USAGE: 		PoLinkingUtils.link(HrJobFamilyMember.class, jobFamily, job);
	 * @param linkClass class of linking entity, if null the class will be derived from linked entities
	 * @param entity1 entity to be linked, e.g. PoPerson
	 * @param entity2 entity to be linked, e.g. PoGroup
	 * @return linking entity object linked to both entities by calling e.g. 'addPersonGroup', unpersisted
	 */
	public static PersistentObject link(Class<? extends PersistentObject> linkClass, PersistentObject entity1, PersistentObject entity2) {
		return link(linkClass, entity1, entity2, null, null, null, null);
	}

	/**
	 * Basic method for automatic creation and linking of linking entity like PoPersonGroup
	 * USAGE: 		PoLinkingUtils.link(person, group, from, to);
	 * @param entity1 entity to be linked, e.g. PoPerson
	 * @param entity2 entity to be linked, e.g. PoGroup
	 * @param validFrom nullable, applied only when linking object is historizable
	 * @param validTo nullable, applied only when linking object is historizable
	 * @return linking entity object linked to both entities by calling e.g. 'addPersonGroup', unpersisted
	 */
	public static PersistentObject link(PersistentObject entity1, PersistentObject entity2, Date validFrom, Date validTo) {
		return link(entity1, entity2, validFrom, validTo, null, null);
	}

	/**
	 * Basic method for automatic creation and linking of linking entity like PoPersonGroup
	 * USAGE: 		PoLinkingUtils.link(HrJobFamilyMember.class, jobFamily, job, dateFrom, dateTo);
	 * @param linkClass class of linking entity, if null the class will be derived from linked entities
	 * @param entity1 entity to be linked, e.g. PoPerson
	 * @param entity2 entity to be linked, e.g. PoGroup
	 * @param validFrom nullable, applied only when linking object is historizable
	 * @param validTo nullable, applied only when linking object is historizable
	 * @return linking entity object linked to both entities by calling e.g. 'addPersonGroup', unpersisted
	 */
	public static PersistentObject link(Class<? extends PersistentObject> linkClass, PersistentObject entity1, PersistentObject entity2, Date validFrom, Date validTo) {
		return link(linkClass, entity1, entity2, validFrom, validTo, null, null);
	}

	/**
	 * Method for automatic creation and linking of linking entity like PoPersonGroup
	 * @param entity1 entity to be linked, e.g. PoPerson
	 * @param entity2 entity to be linked, e.g. PoGroup
	 * @param entity1AddMethodName nullable, must be specified if different from add<linkingEntityName>,
	 * 	e.g. 'addMemberOfGroups' or 'addPersonGroups' instead of 'addPersonGroup'
	 * 	can be used also for linking of children and parents
	 * @param entity2AddMethodName nullable, must be specified if different from add<linkingEntityName>
	 * @return linking entity object linked to both entities by calling e.g. 'addPersonGroup' method, unpersisted
	 */
	public static PersistentObject link(PersistentObject entity1, PersistentObject entity2, String entity1AddMethodName, String entity2AddMethodName) {
		return link(entity1, entity2, null, null, entity1AddMethodName, entity2AddMethodName);
	}

	/**
	 * Method for automatic creation and linking of linking entity like PoPersonGroup
	 * @param entity1 entity to be linked, e.g. PoPerson or child PoGroup
	 * @param entity2 entity to be linked, e.g. PoGroup or parent PoGroup
	 * @param validFrom nullable, applied only when linking object is historizable
	 * @param validTo nullable, applied only when linking object is historizable
	 * @param entity1AddMethodName nullable, must be specified if different from add<linkingEntityName>,
	 * 	e.g. 'addMemberOfGroups' or 'addPersonGroups' instead of 'addPersonGroup'
	 * 	can be used also for linking of children and parents
	 * @param entity2AddMethodName nullable, must be specified if different from add<linkingEntityName>
	 * @return linking entity object linked to both entities by calling e.g. 'addPersonGroup' method, unpersisted
	 */
	public static PersistentObject link(PersistentObject entity1, PersistentObject entity2, Date validFrom, Date validTo, String entity1AddMethodName, String entity2AddMethodName) {
		return link(null, entity1, entity2, validFrom, validTo, entity1AddMethodName, entity2AddMethodName);
	}

	/**
	 * Method for automatic creation of historizable linking entity like HrJobFamilyMember 
	 * whose name is not derivable from class names of linked entities HrJobFamily and HrJob
	 * @param linkClass class of linking entity, if null the class will be derived from linked entities
	 * @param entity1 entity to be linked, e.g. PoPerson or child PoGroup
	 * @param entity2 entity to be linked, e.g. PoGroup or parent PoGroup
	 * @param validFrom nullable, applied only when linking object is historizable
	 * @param validTo nullable, applied only when linking object is historizable
	 * @param entity1AddMethodName nullable, must be specified if different from add<linkingEntityName>,
	 * 	e.g. 'addMemberOfGroups' or 'addPersonGroups' instead of 'addPersonGroup'
	 * 	can be used also for linking of children and parents
	 * @param entity2AddMethodName nullable, must be specified if different from add<linkingEntityName>
	 * @return linking entity object linked to both entities by calling e.g. 'addPersonGroup' method, unpersisted
	 */
	public static PersistentObject link(
			Class<? extends PersistentObject> linkClass,
			PersistentObject entity1,
			PersistentObject entity2,
			Date validFrom,
			Date validTo, 
			String entity1AddMethodName,
			String entity2AddMethodName)
	{
		NamesAndClass namesAndClass = getEntityNamesAndLinkClass(linkClass, entity1, entity2);

		try {
			PersistentObject link = namesAndClass.getLinkClass().newInstance();

			// TODO: code duplication
			if (namesAndClass.getNameWithoutModule1().equals(namesAndClass.getNameWithoutModule2())) { // parent-child relationship
				linkEntity(link, entity1, "setChild" + namesAndClass.getNameWithoutModule1(), entity1AddMethodName);
				linkEntity(link, entity2, "setParent" + namesAndClass.getNameWithoutModule1(), entity2AddMethodName);
			} else {
				linkEntity(link, entity1, null, entity1AddMethodName);
				linkEntity(link, entity2, null, entity2AddMethodName);
			}
			
			if (link instanceof Historization) {
				((Historization) link).setValidfrom(validFrom);
				((Historization) link).setValidto(validTo);
			} else if (validFrom != null || validTo != null)
				throw new IllegalArgumentException("It is not possible to historicize non-historizable entity.");
			
			return link;
		}
		catch (Exception e) {
			throw new RuntimeException("Link entity could not be linked: " + e, e);
		}
	}

	/**
	 * This method is intended for static links 
	 * but can also be applied to historicized links
	 * that shall be definitely removed from the database.
	 * <p>
	 * It removes static and temporal links if class names are regularly created
	 * executes following two statement chains:
	 *     link.getFirst().getLinks().remove(x);
	 *     link.getSecond().getLinks().remove(x);
	 * or if the getter(s) with irregular names are explicitly provided.
	 * <p>
	 * Nothing is persisted, all the changes are soft and one of the linked entities 
	 * must be saved to make the change permanent.
	 * 
	 * @param link to be destroyed
	 * @param gettersOfLinksCollection optional one or two irregular names of getters for the links collection, 
	 * e.g. getMemberOfGroups for PoPerson because that is how what would be regular getPersonGroups method is called there
	 */
	@SuppressWarnings("rawtypes")
	public static void removeLink(Object link, String... gettersOfLinksCollection) {

		if (link == null)
			throw new IllegalArgumentException("Cannot remove null link");
		
		String linkClassName = link.getClass().getSimpleName();
		String moduleName = NamingConventions.getModuleName(linkClassName);
		String entitiesNames = linkClassName.substring(moduleName.length());
		String firstEntityName = NamingConventions.getModuleName(entitiesNames);
		String secondEntityName = entitiesNames.substring(firstEntityName.length());

		try {
			Method getFirstEntity = link.getClass().getMethod("get" + firstEntityName);
			Object firstEntity = getFirstEntity.invoke(link);
			if (firstEntity == null)
				throw new PoRuntimeException("Cannot unlink from null entity " + moduleName + firstEntityName);
			
			Method getLinks = getLinksCollectionGetter(firstEntity, entitiesNames, gettersOfLinksCollection);
			if (false == ((Collection) getLinks.invoke(firstEntity)).remove(link))
				throw new PoRuntimeException("Could not unlink " + firstEntity + " from " + link);

			Method getSecondEntity = link.getClass().getMethod("get" + secondEntityName);
			Object secondEntity = getSecondEntity.invoke(link);
			if (secondEntity == null)
				throw new PoRuntimeException("Cannot unlink from null entity " + moduleName + secondEntityName);
			
			getLinks = getLinksCollectionGetter(secondEntity, entitiesNames, gettersOfLinksCollection);
			if (false == ((Collection) getLinks.invoke(secondEntity)).remove(link))
				throw new PoRuntimeException("Could not unlink " + secondEntity + " from " + link);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method terminates links in following way:
	 * <li>
	 * if it is simple link without validfrom and valid to it is removed
	 * </li>
	 * <li>
	 * if it is historized link but is not valid yet it is removed
	 * </li>
	 * <li>
	 * if it is historized link and is currently valid it is historicized
	 * </li>
	 * <li>
	 * if it is historized link and is no more valid it is ignored
	 * </li>
	 * <p>
	 * Nothing is persisted, all the changes are soft and one of the linked entities 
	 * must be saved to make the change permanent.
	 * 
	 * @param link
	 * @param gettersOfLinksCollection optional one or two irregular names of getters for the links collection, 
	 * e.g. getMemberOfGroups for PoPerson because that is how what would be regular getPersonGroups method is called there
	 */
	public static void terminateLink(PersistentObject link, String... gettersOfLinksCollection) {
		
		if (link instanceof Historization) {
		     Historization hist = (Historization) link;
		     // not yet started
		     if (hist.getValidfrom().after(DateTools.now()))
		    	 removeLink(link, gettersOfLinksCollection);
		     else if (hist.getValidto().after(DateTools.now()))
		    	 hist.historicize();
		     else
		         ;// nothing to do, is already terminated
		} else
			removeLink(link, gettersOfLinksCollection);
	}

	//	--------------------------- PRIVATE METHODS ----------------------------------

	private static NamesAndClass getEntityNamesAndLinkClass(
			Class<? extends PersistentObject> linkClass,
			PersistentObject entity1,
			PersistentObject entity2)
	{
		if (entity1 == null || entity2 == null)
			throw new IllegalArgumentException("Linked entities must be non-null!");

		String simpleClassname1 = entity1.getClass().getSimpleName();	// "PoPerson"
		String moduleName1 = NamingConventions.getModuleName(simpleClassname1);	// "Po"
		String simpleClassname2 = entity2.getClass().getSimpleName();	// "PoGroup"
		String moduleName2 = NamingConventions.getModuleName(simpleClassname2);	// "Po"

		if (!moduleName1.equals(moduleName2))
			throw new IllegalArgumentException("Cannot link entities from different modules: " + moduleName1 + " and " + moduleName2);

		String moduleName = moduleName1;
		String nameWithoutModule1 = NamingConventions.getSimpleClassNameWithoutModuleName(simpleClassname1);	// "Person"
		String nameWithoutModule2 = NamingConventions.getSimpleClassNameWithoutModuleName(simpleClassname2);	// "Group"

		Class<? extends PersistentObject> linkCls = linkClass;	// 
		if (linkCls == null) {
			if (simpleClassname1.equals(simpleClassname2)) // parent-child relationship
				linkCls = getLinkingEntityClass(moduleName, "Parent", nameWithoutModule2);
			else
				linkCls = getLinkingEntityClass(moduleName, nameWithoutModule1, nameWithoutModule2);

			if (linkCls == null)
				throw new RuntimeException("No link entity class could be determined");
		}

		NamesAndClass result = new NamesAndClass(nameWithoutModule1, nameWithoutModule2, linkCls);
		return result;
	}

	/**
	 * @return the class that relates two entity classes given by nameWithoutModule1 and nameWithoutModule2.
	 */
	@SuppressWarnings("unchecked")
	private static Class<PersistentObject> getLinkingEntityClass(
			String moduleName,	// "Po"
			String nameWithoutModule1,	// "Person"
			String nameWithoutModule2) {	// "Group"

		Class<PersistentObject> linkClass = null;
		String packageName = PACKAGE_BASE + "." + moduleName.toLowerCase() + ".model.";	// "at.workflow.webdesk.po.model"
		// first try
		String linkEntityName = packageName + moduleName + nameWithoutModule1 + nameWithoutModule2;	// "at.workflow.webdesk.po.model.PoPersonGroup"
		try {
			linkClass = (Class<PersistentObject>) Class.forName(linkEntityName);
		}
		catch (ClassNotFoundException e) {
			// second try
			linkEntityName = packageName + moduleName + nameWithoutModule2 + nameWithoutModule1;	// "at.workflow.webdesk.po.model.PoGroupPerson"
			try {
				linkClass = (Class<PersistentObject>) Class.forName(linkEntityName);
			}
			catch (ClassNotFoundException e1) {
				throw new RuntimeException("Standard linking entity not found: " + e + ": " + e1, e1);
			}
		}

		return linkClass;
	}

	/** links link to entity using setter or conventional method */
	private static void linkEntity(PersistentObject link, PersistentObject entity, String setMethodName) 
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		// link to entity
		String setterName = setMethodName;
		if (StringUtils.isBlank(setterName)) {
			String entityClassName = entity.getClass().getSimpleName();
			String entityName = entityClassName.substring(NamingConventions.getModuleName(entityClassName).length());
			setterName = "set" + entityName;
		}
		Method setter = link.getClass().getMethod(setterName, entity.getClass());
		setter.invoke(link, entity);
	}

	/** links entity to link by adding link to entitys list of links */
	private static void linkEntity(PersistentObject link, PersistentObject entity, String setMethodName, String addMethodName) 
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		// link to entity
		linkEntity(link, entity, setMethodName);

		// entity to link
		String addName = addMethodName;
		if (StringUtils.isBlank(addName)) {
			// TODO: fri_2013-06-12: this duplicates the method getAdderMethodName(collectionProperty),
			// but refactoring this would be quite tiresome, because callers of this method do not know a the Collection property name in parent entity
			String linkClassName = link.getClass().getSimpleName();
			String linkName = linkClassName.substring(NamingConventions.getModuleName(linkClassName).length());
			addName = "add" + linkName;
		}

		Method adder = entity.getClass().getMethod(addName, link.getClass());
		adder.setAccessible(true);	// fri_2013-06-12: needed for package-visible test classes
		adder.invoke(entity, link);
	}

	private static Method getLinksCollectionGetter(Object entity, String entitiesNames, String[] linksCollectionGetterNames) {
		
		Method getLinksCollection;
		try {
			getLinksCollection = entity.getClass().getMethod("get" + entitiesNames + "s");
			return getLinksCollection;
		} catch (Exception e) {
			for (String getLinks : linksCollectionGetterNames)
				try {
					getLinksCollection = entity.getClass().getMethod(getLinks);
					return getLinksCollection;
				} catch (Exception ee) { }
		}
		throw new PoRuntimeException("No links collection getter found for " + entity.getClass().getSimpleName() + 
				" entities " + entitiesNames);
	}
	
}
