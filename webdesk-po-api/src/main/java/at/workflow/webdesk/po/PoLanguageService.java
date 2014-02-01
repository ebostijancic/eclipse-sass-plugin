package at.workflow.webdesk.po;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.core.io.Resource;

import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoTextModule;

/**
 * This Service allows access to <code>PoLanguage</code> and
 * <code>PoTextModule</code> objects.
 * 
 * Created on 18.07.2005
 * @author hentner (Harald Entner)
 */
public interface PoLanguageService {

	List<PoLanguage> findAllLanguages();
	
	PoLanguage getLanguage(String uid);
    
    List<PoLanguage> loadAllLanguages();
	
	/**
	 * Saves a new language, or updates it if the language already exists.
	 * For new (non-existent) languages, this copies text-modules from default language to the new language.
	 */
	void saveLanguage(PoLanguage language); 
	
	/**
	 * deletes the language and the corresponding textmodules.
	 */
	void deleteAndFlushLanguage(PoLanguage language);
	
	/**
	 * sets the given language to the standard language
	 */
	void saveNewDefaultLanguage(PoLanguage language);
	
	/**
	 * saves the textmodule, or updates if one exists already.
	 */
	void saveTextModule(PoTextModule textModule);
	
	/**
	 * saves the textmodule, or updates if one exists already. Then refesh all TextModules.
	 */
	void saveAndRefreshTextModule(PoTextModule textModuel);
	
	/**
	 * @return the standard text module
	 */
	List<PoTextModule> findStandardTextModules();
	
	/**
	 * @return a list of PoTextModule objects that correspond to the given language
	 */
	List<PoTextModule> findTextModules(PoLanguage language);
	
	/**
	 * @return a list (it should be only one element - but the name is 
	 * not necessary uniqu in the database) of TextModules, whereas the 
	 * name is used as key. 
	 */
	List<PoTextModule> findTextModuleByName(String name);
    
    /**
     * gets PoTextModule for given Name and Language, returns null if none was found.
     * @param name -> key of Textmodule to be found
     * @param lang -> PoLanguage Object of corresponding Language
     * @return a <code>PoTextModule</code> object.
     */
    PoTextModule findTextModuleByNameAndLanguage(String name, PoLanguage lang);
 
	/**
	 * father module inherits the properties to the child
	 */
	void inheritTextModule(PoTextModule father, PoTextModule child);
	
	/**
	 * deletes the textModule permanently from the database.
	 */
	void deleteAndFlushTextModule(PoTextModule textModule);
	
    PoLanguage findLanguageByCode(String langCode);
    
    /**
     * @return the Webdesk default language (not the operating system's one!).
     * 		When no language was marked with the <code>defaultLanguage</code>-flag,
     * 		this looks for the language with code "de",
     * 		if this also fails, the first found language is returned.
     */
    PoLanguage findDefaultLanguage();
    
    /**
     * finds List of Textmodules for given Action and LanguageCode. If the action is null, 
     * only textModules without an action are returned. (common textModules).
     * 
     * @param action: PoAction object (can be null) 
     * @param languageKey: Key specifing the Language (f.i. 'en')
     * @return List of PoTextModules
     */
    List<PoTextModule> findTextModulesForAction(PoAction action,String languageKey);

    /**
     * @return a Text Module object with the given UID, if a 
     * Text Module is found, null otherwise. 
     */
    PoTextModule getTextModule(String UID);
    
    /**
     * finds all common textmodules of a specified language
     * @return List of PoTextModule Objects
     */
    List<PoTextModule> findCommonTextModules(PoLanguage language);
    
    /**
     * sets Flag allowUpdateOnVersionChange on every PoTextModule to true
     */
    void setUpdateOnVersionChangeTrueForAllTextModules();
    
    /**
     * sets Flag allowUpdateOnVersionChange on every PoTextModule to false
     */
    void setUpdateOnVersionChangeFalseForAllTextModules();

    /**
     * @return the Parent PoTextModule of the given PoTextModule 
     */
    PoTextModule findParentTextModule(PoTextModule textModule);

    /**
     * Replaces placeholders defined in string <code>source</code> 
     * [as {0},{1},..{n}] with strings defined in the passed params list.
     * 
     * @param source a string object containing placeholders [as {0},{1},..{n}] 
     * @param params an ArrayList of string objects (params(0) -> {0}, and so on)
     * @return the resulting string
     */
    String insertParams(String source, List<String> params);
    
    /**
     * This function replaces param(i) with the corresponding i18n with key=param(i)
     * if keys(i) is true. param(i)=param(i) if keys(i) is false.
     * 
     * @param params an ArrayList of string objects 
     * @param keys a list of boolean values
     * @param langCode the language code as string [de,en,...]
     * @return a list of string objects
     */
    List<String> replaceTextModulesInDependenceList(List<String> params,List<Boolean> keys, String langCode);

    /**
     * This function replaces i18n keys of the passed "value" string. 
     * The keys should look like ${KEY}. If the KEY is found in the given 
     * language it's translation is used, otherwise the key itself is used.
     */
    String replaceContainingReferences(String value, PoLanguage myLang);
	
    /** 
     * <p>This function tries to parse <code>PoTextModule</code>'s definitions (which can be 
     * defined inside <code>XML</code> files. The Syntax of such a file can be found in the 
     * programmers documentation.
     * </p>
     * <p>If <code>updateOnVersionChange</code> is set to true, an update of a <code>PoTextModule</code> is
     * possible, otherwise the value contained in the database is kept.
     * 
     * @param r the Resource which contains i18n definitions
     * @param unResolvedMap is a Map of textmodules where the parent could not be resolved, the key ist the UID
     * 
     * The unResolvedMap is passed in order to add all textmodules beeing registered where
     * the parent could not be resolved inside the system. this map is later used to retry 
     * the registration for those textmodules for a number of times. 
     */
    void syncTextModule(Resource r, Map<String, String> unResolvedMap);
    
   	/**
   	 * same as other above method, but provides possibility to add a textModulecache where
   	 * you can find ALL textmodules with their keys (name + "_" + langCode)
   	 * for faster retrieval 
   	 */
	void syncTextModule(Resource resource, Map<String, String> unResolvedTextModuleParents, Map<String, String> textModuleCache);

	/**
	 * @see #writeTextModuleFile(PoLanguage, boolean)
	 */
	void writeLanguageXMLToOutputStream(PoLanguage poLanguage, OutputStream outputStream, boolean appendLanguageAttribute) throws IOException;
	
	List<PoTextModule> findTextModules(PoLanguage myLang, Date dateOfCreation);

	void init();

	/**
	 * Same as above, but with dynamically inserted parameters.
	 * @param i18nKey the key to turn to language-specific text.
	 * @param locale the target language.
	 * @param params dynamic positional arguments to be inserted into text.
	 * @param doParamsI18n same size as params, for every param if it should be internationalized or not.
	 * @return the internationalized text.
	 */
	String translate(String i18nKey, Locale locale, List<String> params, List<Boolean> doParamsI18n);
	
	/**
   	 * Finds the Translation for a i18n-Key and the given Locale-String.<p/>
   	 * If there is no Language definded for this Locale the DefaultLanguage will be used.
   	 * If there is no Textmodule defined for the given i18n-Key the i18n-Key will be returned.<br/>
   	 * 
   	 * @param localeStr the <code>locale.getLanguage()</code> string of the required language, one of "de", "en", "fr", ....
	 * @param i18nKey the language-neutral identifier for wanted text.
	 * @return translation of given key for given language.
   	 */
   	String translate(String localeStr, String i18nKey);
	
    /**
	 * Finds the Translation for a i18n-Key and the given Locale.<p/>
	 * If there is no Language definded for this Locale the DefaultLanguage will be used.
	 * If there is no Textmodule defined for the given i18n-Key the i18n-Key will be returned.<br/>
	 * 
	 * @param locale the required language.
	 * @param i18nKey the language-neutral identifier for wanted text.
	 * @return translation of given key for given language.
	 */
	String translate(Locale locale, String i18nKey);
	
	/**
	 * The translation with default language, or the given key if no translation was found.
	 * @param i18nKey the language-neutral identifier for wanted text.
	 * @return translation of given key for default language.
	 */
	String translate(String i18nKey);

}
