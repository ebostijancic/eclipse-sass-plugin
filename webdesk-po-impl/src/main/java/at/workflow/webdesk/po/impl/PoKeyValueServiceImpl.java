package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoKeyValueService;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.daos.PoKeyValueTypeDAO;
import at.workflow.webdesk.po.daos.PoTextModuleDAO;
import at.workflow.webdesk.po.model.PoKeyValue;
import at.workflow.webdesk.po.model.PoKeyValueType;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoTextModule;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * TODO fri_2011-08-24:
 * This does not cover specific languages, it always works with default language.
 * Exception is <code>findKeyValueTypeWithTextModules()</code>.
 * 
 * @author ggruber 2013-11-26 some small refactorings.
 */
public class PoKeyValueServiceImpl implements PoKeyValueService {

	private static final Logger logger = Logger.getLogger(PoKeyValueServiceImpl.class);

	private PoKeyValueTypeDAO keyValueTypeDAO;
	private PoLanguageService languageService;
	private PoTextModuleDAO textModuleDAO;
	
	private PoKeyValueService instanceWithAopAdvices;

	@Override
	public void deleteKeyValueType(PoKeyValueType keyValueType) {
		keyValueTypeDAO.delete(keyValueType);
	}

	@Override
	public PoKeyValueType getKeyValueType(String uid) {
		return keyValueTypeDAO.get(uid);
	}

	@Override
	public PoKeyValueType findKeyValueType(String key) {
		return keyValueTypeDAO.findKeyValueTypeByName(key);
	}

	@Override
	public PoKeyValueType findKeyValueTypeWithTextModules(String key, PoLanguage language) {
		PoKeyValueType keyValueType = findKeyValueType(key);
		addTextModuleToKeyValues(keyValueType, language);
		return keyValueType;
	}

	@Override
	public List<PoKeyValue> getSelectionList(String keyValueTypeName) {
		return getSelectionListWithFilter(keyValueTypeName, "");
	}

	private List<PoKeyValue> getSelectionListWithFilter(String keyValueTypeName, String filter) {
		PoKeyValueType keyValueType = getInstanceWithAopAdvices().findKeyValueType(keyValueTypeName);

		if (keyValueType == null)
			return new ArrayList<PoKeyValue>();

		addTextModuleToKeyValues(keyValueType);

		// WORKAROUND for Filter problem
		// to filter out outdated keyValues...

		List<PoKeyValue> selectionListItems = new ArrayList<PoKeyValue>();
		for (PoKeyValue keyValue : keyValueType.getKeyValues())	{
			Date now = DateTools.now();
			if (keyValue.getValidto().after(now) && keyValue.getValidfrom().before(now))
				if (filter == null || filter.equals("") ||
						keyValue.getFilter() == null || keyValue.getFilter().equals("") ||
						filter.equals(keyValue.getFilter()))
					selectionListItems.add(keyValue);
		}

		return selectionListItems;
	}

	@Override
	public List<PoKeyValue> getSelectionList(String keyValueTypeName, String oldKey) {
		return getSelectionListWithFilter(keyValueTypeName, oldKey, "");
	}

	@Override
	public List<PoKeyValue> getSelectionListWithFilter(String keyValueTypeName, String oldKey, String filter) {
		List<PoKeyValue> keyValues = getSelectionListWithFilter(keyValueTypeName, filter);
		PoKeyValueType keyValueType = keyValueTypeDAO.findKeyValueTypeByName(keyValueTypeName);

		if (keyValueType == null)
			return new ArrayList<PoKeyValue>();

		if (oldKey != null && false == oldKey.equals("")) {
			PoKeyValue oldKeyValue = keyValueTypeDAO.findKeyValueIncludingOld(keyValueType, oldKey);
			if (oldKeyValue != null && false == keyValues.contains(oldKeyValue)) {
				// insert old KeyValue at first position
				keyValues.add(0, oldKeyValue);
			}
		}
		return keyValues;
	}

	// TODO make this language specific
	@Override
	public void saveKeyValueType(PoKeyValueType keyValueType) {
		keyValueTypeDAO.save(keyValueType);

		PoLanguage defaultLanguage = languageService.findDefaultLanguage();
		
		// iterate through all keyValues
		// look if a new PoTextModule is linked
		// if yes --> save it (otherwise create it)
		// ensure all textmodules for all other languages are created
		
		for (PoKeyValue keyValue : keyValueType.getKeyValues())	{
			String tmName = PoConstants.SELECTIONLIST_TEXTMODULE_PREFIX + keyValueType.getName() + "_" + keyValue.getKey();

			if (keyValue.getTextModule() != null) {
				// set default values // check first if the textmodule does already exist
				if (languageService.findTextModuleByNameAndLanguage(tmName, defaultLanguage) == null) {
					// create TextModule
					keyValue.getTextModule().setLanguage(defaultLanguage);
					keyValue.getTextModule().setAllowUpdateOnVersionChange(true);
					keyValue.getTextModule().setName(tmName);
					languageService.saveTextModule(keyValue.getTextModule());

				} else {
					// Update existing textmodule
					PoTextModule tm = languageService.findTextModuleByNameAndLanguage(tmName, defaultLanguage);
					textModuleDAO.evict(tm);
					keyValue.getTextModule().setName(tmName);
					keyValue.getTextModule().setUID(tm.getUID());
					keyValue.getTextModule().setLanguage(defaultLanguage);
					languageService.saveTextModule(keyValue.getTextModule());
				}
			}
			else {
				logger.error("There is no textmodule assigned to the given keyvalue");
			}

			// ensure textmodules for all other languages are created!
			for (PoLanguage language : languageService.loadAllLanguages()) {
				PoTextModule textModule = textModuleDAO.findTextModuleByNameAndLanguage(tmName, language);
				if (textModule == null && false == language.equals(defaultLanguage)) {
					// create new textmodule
					textModule = new PoTextModule();
					textModule.setLanguage(language);
					textModule.setName(PoConstants.SELECTIONLIST_TEXTMODULE_PREFIX + keyValueType.getName() + "_" + keyValue.getKey());
					textModule.setValue("[" + language.getCode() + "]" + keyValue.getKey());
					textModule.setAllowUpdateOnVersionChange(true);
					languageService.saveTextModule(textModule);
				}
			}
		}
	}

	public void setKeyValueTypeDAO(PoKeyValueTypeDAO keyValueTypeDAO) {
		this.keyValueTypeDAO = keyValueTypeDAO;
	}

	private void addTextModuleToKeyValues(PoKeyValueType valueType) {
		addTextModuleToKeyValues(valueType, null);
	}
	
	private void addTextModuleToKeyValues(PoKeyValueType myValueType, PoLanguage language) {
		if (myValueType.getKeyValues() == null)
			return;
		
		if (language == null)
			language = languageService.findDefaultLanguage();
		
		for (PoKeyValue keyValue : myValueType.getKeyValues()) {
			if (keyValue.getTextModule() == null) {
				PoTextModule myTextModule = languageService.findTextModuleByNameAndLanguage(
						PoConstants.SELECTIONLIST_TEXTMODULE_PREFIX + myValueType.getName() + "_" + keyValue.getKey(),
						language);
				
				if (myTextModule != null) {
					keyValue.setTextModule(myTextModule);
				}
			}
		}
	}

	@Override
	public PoKeyValueType getKeyValueTypeF(String uid, Date referenceDate) {
		PoKeyValueType keyValueType = keyValueTypeDAO.getKeyValueTypeF(uid, referenceDate);
		addTextModuleToKeyValues(keyValueType);
		return keyValueType;
	}

	@Override
	public Map<String, List<PoKeyValue>> getKeyValueTypesAsMap() {
		Map<String, List<PoKeyValue>> res = new HashMap<String, List<PoKeyValue>>();
		for (PoKeyValueType keyValueType : keyValueTypeDAO.findKeyValueTypes()) {
			res.put(keyValueType.getName(), getSelectionList(keyValueType.getName()));
		}
		return res;
	}

	@Override
	public boolean hasDuplicateEntries(PoKeyValueType kvt) {
		int origSize = kvt.getKeyValues().size();
		int sizeInMap = getKeyValueKeys(kvt).size();
		if (origSize == sizeInMap)
			return false;
		return true;
	}

	@Override
	public Map<String, String> getKeyValueKeys(PoKeyValueType kvt) {
		Map<String, String> result = new HashMap<String, String>();
		for (PoKeyValue keyValue : kvt.getKeyValues()) {
			result.put(keyValue.getKey(), keyValue.getUID());
		}
		return result;
	}

	@Override
	public String translateKeyValue(String keyValueTypeId, String key, PoLanguage language) {
		if (language == null)
			language = languageService.findDefaultLanguage();

		PoKeyValueType keyValues = findKeyValueTypeWithTextModules(keyValueTypeId, language);
		for (PoKeyValue keyValue : keyValues.getKeyValues())	{
			if (keyValue.getKey().equals(key) && keyValue.getTextModule().getLanguage().equals(language))	{
				return keyValue.getTextModule().getValue();
			}
		}
		logger.warn("Found no translation for key/value "+key+" with language "+language+" !!! FIXME");
		return key;
	}

	@Override
	public String getLabel(String keyValueTypeName, String key, String locale) {

		Locale loc = Locale.getDefault();
		if (StringUtils.isNotBlank(locale))
				loc = new Locale(locale);

		PoLanguage language = languageService.findLanguageByCode(loc.getLanguage());

		return translateKeyValue(keyValueTypeName, key, language);
	}
	
	private PoKeyValueService getInstanceWithAopAdvices()	{
		if (instanceWithAopAdvices == null)
			instanceWithAopAdvices = (PoKeyValueService) WebdeskApplicationContext.getBean("PoKeyValueService");
		return instanceWithAopAdvices;
	}
	

	/** Spring setter. */
	public void setLanguageService(PoLanguageService languageService) {
		this.languageService = languageService;
	}

	/** Spring setter. */
	public void setTextModuleDAO(PoTextModuleDAO textModuleDAO) {
		this.textModuleDAO = textModuleDAO;
	}

}
