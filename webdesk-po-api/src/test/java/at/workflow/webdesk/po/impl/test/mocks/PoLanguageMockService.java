package at.workflow.webdesk.po.impl.test.mocks;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.core.io.Resource;

import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoTextModule;

public class PoLanguageMockService implements PoLanguageService {
	
    private Map<String, PoLanguage> languages = new HashMap<String, PoLanguage>();
    private String defaultLanguageCode = "de";
	
	
	public List<PoLanguage> findAllLanguages() {
		List<PoLanguage> ret = new ArrayList<PoLanguage>();
		ret.addAll(this.languages.values());
		return ret;
	}

	public PoLanguage getLanguage(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoLanguage> loadAllLanguages() {
		return findAllLanguages();
	}

	public void saveLanguage(PoLanguage language) {
		// TODO Auto-generated method stub

	}

	public void deleteAndFlushLanguage(PoLanguage language) {
		// TODO Auto-generated method stub

	}

	public void saveNewDefaultLanguage(PoLanguage language) {
		// TODO Auto-generated method stub

	}

	public void saveTextModule(PoTextModule textModule) {
		// TODO Auto-generated method stub

	}

	public void saveAndRefreshTextModule(PoTextModule textModuel) {
		// TODO Auto-generated method stub

	}

	public List<PoTextModule> findStandardTextModules() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoTextModule> findTextModules(PoLanguage language) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoTextModule> findTextModuleByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoTextModule findTextModuleByNameAndLanguage(String name,
			PoLanguage lang) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoTextModule findTextModuleByNameAndLanguage(String name,
			PoLanguage lang, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public void inheritTextModule(PoTextModule father, PoTextModule child) {
		// TODO Auto-generated method stub

	}

	public void deleteAndFlushTextModule(PoTextModule textModule) {
		// TODO Auto-generated method stub

	}

	public PoLanguage findLanguageByCode(String langCode) {
		return this.languages.get(langCode);
	}

	public PoLanguage findDefaultLanguage() {
		return this.languages.get(defaultLanguageCode);
	}

	public List<PoTextModule> findTextModulesForAction(PoAction action,
			String languageKey) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoTextModule getTextModule(String UID) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoTextModule> findCommonTextModules(PoLanguage language) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setUpdateOnVersionChangeTrueForAllTextModules() {
		// TODO Auto-generated method stub

	}

	public void setUpdateOnVersionChangeFalseForAllTextModules() {
		// TODO Auto-generated method stub

	}

	public PoTextModule findParentTextModule(PoTextModule textModule) {
		// TODO Auto-generated method stub
		return null;
	}

	public String insertParams(String source, List<String> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> replaceTextModulesInDependenceList(List<String> params,
			List<Boolean> keys, String langCode) {
		// TODO Auto-generated method stub
		return null;
	}

	public String replaceContainingReferences(String value, PoLanguage myLang) {
		// TODO Auto-generated method stub
		return null;
	}

	public void syncTextModule(Resource r, Map<String, String> unResolvedMap) {
		// TODO Auto-generated method stub

	}

	public void syncTextModule(Resource resource,
			Map<String, String> unResolvedTextModuleParents,
			Map<String, String> textModuleCache) {
		// TODO Auto-generated method stub

	}

	public void writeLanguageXMLToOutputStream(PoLanguage poLanguage,
			OutputStream outputStream, boolean appendLanguageAttribute)
			throws IOException {
		// TODO Auto-generated method stub

	}

	public List<PoTextModule> findTextModules(PoLanguage myLang,
			Date dateOfCreation) {
		// TODO Auto-generated method stub
		return null;
	}

	public void init() {
		// TODO Auto-generated method stub
		
		PoLanguage langDe = new PoLanguage();
		langDe.setCode("de");
		langDe.setDefaultLanguage(true);
		langDe.setName("Deutsch");
		
		languages.put("de", langDe);
		
		PoLanguage langEn = new PoLanguage();
		langEn.setCode("en");
		langEn.setDefaultLanguage(false);
		langEn.setName("Englisch");
		
		languages.put("en", langEn);

	}

	public String translate(String i18nKey, Locale locale, List<String> params,
			List<Boolean> doParamsI18n) {
		// TODO Auto-generated method stub
		return null;
	}

	public String translate(String localeStr, String i18nKey) {
		// TODO Auto-generated method stub
		return null;
	}

	public String translate(Locale locale, String i18nKey) {
		// TODO Auto-generated method stub
		return null;
	}

	public String translate(String i18nKey) {
		// TODO Auto-generated method stub
		return null;
	}

}
