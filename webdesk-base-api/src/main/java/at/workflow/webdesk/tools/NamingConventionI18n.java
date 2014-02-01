package at.workflow.webdesk.tools;


/**
 * Builds i18n resource keys from different parameters.
 * This is the implementation of naming conventions for i18n resources used in HR-expert UI.
 * It is placed in hr-api module because this is also used by the i18n tool.
 * 
 * @author sdzuban 08.05.2013
 * @author fritzberger 23.05.2013 refactored code to structured programming level.
 * @author fritzberger 24.05.2013 added method for enums, made class final with private constructor.
 */
public final class NamingConventionI18n {

	/**
	 * @param tabName e.g. "Kontakte".
	 * @return e.g. "hr_tab_title_Kontakte".
	 */
	public static String getI18nKeyAsTabTitle(String tabName) {
		return "hr_tab_title_"+tabName;
	}

	/**
	 * @param clazz e.g. at.workflow.webesk.hr.model.HrPerson.
	 * @return e.g. "hr_person".
	 */
	public static String getI18nKey(Class<?> clazz) {
		assert clazz != null : "Class must not be null";
		
		final String className = clazz.getSimpleName();
		final String moduleName = NamingConventions.getModuleName(className);
		final String classNameWithoutModule = NamingConventions.getSimpleClassNameWithoutModuleName(className);
		
		return moduleName.toLowerCase()+"_"+classNameWithoutModule.toLowerCase();
	}

	/**
	 * @param clazz e.g. at.workflow.webesk.hr.model.HrPerson.
	 * @param propertyExpression e.g. "middleName", or "person.userName", or "person.memberOfGroups[2].group.name".
	 * @return e.g. "hr_person_middleName", or "hr_person_person.userName". or "hr_person_person.memberOfGroups".
	 */
	public static String getI18nKey(Class<?> clazz, String propertyExpression) {
		assert propertyExpression != null : "Field must not be null";
		
		final String propertyNameBeforeIndex = BeanAccessor.containsIndexing(propertyExpression);
		final String propertyName = (propertyNameBeforeIndex != null) ? propertyNameBeforeIndex : propertyExpression;
		return getI18nKey(clazz)+"_"+propertyName;
	}

	/**
	 * @param enumValue e.g. at.workflow.webesk.po.model.PoPerson.Gender.FEMALE.
	 * @return e.g. "hr_person_gender_FEMALE".
	 */
	public static String getI18nKey(Enum<?> enumValue) {
		assert enumValue != null : "Enum value must not be null";
		
		final String enumString = enumValue.toString();
		
	    // we must cover two cases: "hr_skill_timeunit_YEAR" or "hr_importancelevel_CRUCIAL"
		// first is an inner enum "TimeUnit" in HrSkill.class, second is a top-level enum
		final String fullEnumClassName = enumValue.getClass().getName();
		final String simpleEnclosingClassname = TextUtils.getEnclosingSimpleClassname(fullEnumClassName);
    
		if (simpleEnclosingClassname == null)	{	// top-level enum, not an inner class
			// make "hr_importancelevel_CRUCIAL" from HrImportanceLevel.CRUCIAL
			final String moduleName = NamingConventions.getModuleName(fullEnumClassName).toLowerCase();
			final String simpleEnumWithoutModule = NamingConventions.getSimpleClassNameWithoutModuleName(fullEnumClassName).toLowerCase();
			return moduleName+"_"+simpleEnumWithoutModule+"_"+enumString;
		}
		
		// else: inner enum class
		// make "hr_skill_timeunit_YEAR" from HrSkill.TimeUnit.YEAR
		final String moduleName = NamingConventions.getModuleName(simpleEnclosingClassname).toLowerCase();
		final String simpleEnclosingWithoutModule = NamingConventions.getSimpleClassNameWithoutModuleName(simpleEnclosingClassname).toLowerCase();
		final String simpleEnumClassname = enumValue.getClass().getSimpleName().toLowerCase();	// gives "Gender", not "Person$Gender"
		
		return moduleName+"_"+simpleEnclosingWithoutModule+"_"+simpleEnumClassname+"_"+enumString;
	}


	private NamingConventionI18n()	{}	// do not instantiate
}
