package at.workflow.webdesk.po.impl.test.nontransactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.daos.PoLanguageDAO;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.po.model.PoTextModule;
import at.workflow.webdesk.tools.DaoJdbcUtil;
import at.workflow.webdesk.tools.testing.AbstractAutoCommitSpringHsqlDbTestCase;

/**
 * Tests the re-attachment to a database session of Hibernate POJOs residing in EH-cache.
 * 
 * TODO: does not work - could not provoke LazyInitializationException.
 * As pre-condition for bringing this to work, comment out following lines in CustomEhCacheFacade:
 * <pre>
 * 		if (o instanceof PersistentObject)
 * 			o = cacheHibernateUtils.reassociate(o);
 * </pre>
 * This should all List POJOs prevent from being reloaded when retrieved from cache.
 * 
 * @author fritzberger 25.04.2012
 */
public class WTestServiceCacheFacade extends AbstractAutoCommitSpringHsqlDbTestCase {

	private PoLanguageService languageService;

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		languageService = (PoLanguageService) getBean("PoLanguageService");
		// this bean creates languages "de" and "en" in its init() method, but these are removed again by clearUsedTables()
	}
	
	/** @return true to make super-class open an explicit Hibernate database session. */
	@Override
	protected boolean createAndKeepOpenPersistenceSession() {
		return true;
	}

	/** Clears tables PoLog and PoTextModule on tear-down of each test method of this class. */
	@Override
	protected void clearUsedTables() {
		DaoJdbcUtil daoJdbcUtil = (DaoJdbcUtil) this.applicationContext.getBean("DaoJdbcUtil");
		daoJdbcUtil.execute("delete from PoTextModule", DaoJdbcUtil.DATASOURCE_WEBDESK);
		daoJdbcUtil.execute("delete from PoModule", DaoJdbcUtil.DATASOURCE_WEBDESK);
		daoJdbcUtil.execute("delete from PoLanguage", DaoJdbcUtil.DATASOURCE_WEBDESK);
	}

	/**
	 * Inserts two languages, each with one text-module, into database. Re-reads them and asserts they are there.
	 * Then closes the current database session and opens a new one, so that the domain-objects in service-cache
	 * (languages, text-modules) should be detached from their session. Then reads again the languages and calls
	 * their text-modules. This should provoke the Hibernate LazyInitializationException.
	 * TODO: it doesn't!
	 */
	public void testCachedList() throws Exception {
		List<PoLanguage> createdLanguages = createLanguages(3); 
		
		// first access goes to database
		List<PoLanguage> allLanguages = languageService.findAllLanguages();
		assertNotNull(allLanguages);
		assertEquals(createdLanguages.size(), allLanguages.size());
		
		PoLanguageDAO dao = (PoLanguageDAO) getBean("PoLanguageDAO");
		for (PoLanguage language : allLanguages)
			dao.evict(language);
		
		restartSession();	// this should detach POJOs in cache
		
		// second access goes to cache
		allLanguages = languageService.findAllLanguages();
		assertNotNull(allLanguages);
		assertEquals(createdLanguages.size(), allLanguages.size());
		
		// POJOs from cache might be detached, check it by provoking LazyInitializationException
		for (PoLanguage language : allLanguages)	{
			System.out.println("Language "+language.getName());
			for (PoTextModule textModule : language.getTextModules())	{
				assertNotNull(textModule);
				assertTrue(textModule.getName().length() > 0);
				assertNotNull(textModule.getModule());
				assertTrue(textModule.getModule().getName().length() > 0);
				System.out.println(" ... with text-module "+textModule.getName()+", module "+textModule.getModule().getName());
				for (PoTextModule moduleTextModule : textModule.getModule().getTextModules())	{
					assertNotNull(moduleTextModule);
				}
			}
		}
	}

	/** This is just to ensure that no data have been left by any unit test method. */
	public void testEnsureNoDataPresent() throws Exception {
		List<PoLanguage> allLanguages = languageService.findAllLanguages();
		assertEquals(0, allLanguages.size());
	}


	private List<PoLanguage> createLanguages(int count) {
		List<PoLanguage> languages = new ArrayList<PoLanguage>();
		for (int i = 0; i < count; i++)	{
			languages.add(newLanguage("SomeLang"+i, "l"+i, "Some text module "+i, "Some module "+i));
		}
		return languages;
	}

	private PoLanguage newLanguage(String langName, String langCode, String textModuleName, String moduleName) {
		PoModule module = new PoModule();
		module.setName(moduleName);
		
		PoModuleService moduleService = (PoModuleService) getBean("PoModuleService");
		moduleService.saveModule(module);
		
		PoLanguage language = new PoLanguage();
		language.setName(langName);
		language.setCode(langCode);
		
		languageService.saveLanguage(language);
		
		PoTextModule textModule = new PoTextModule();
		textModule.setName(textModuleName);
		
		textModule.setLanguage(language);
		language.addTextModule(textModule);
		
		module.setTextModules(new HashSet<PoTextModule>());
		module.getTextModules().add(textModule);
		textModule.setModule(module);
		
		languageService.saveTextModule(textModule);
		
		return language;
	}

}
