package at.workflow.webdesk.po.impl.test;

import net.sf.ehcache.CacheManager;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

public class WTestPoLanguageServiceImpl extends AbstractTransactionalSpringHsqlDbTestCase {
	
    //Services
    private PoLanguageService languageService;
    private CacheManager cacheManager;
    
	@Override
	protected void onSetUpBeforeDataGeneration() throws Exception {
		languageService = (PoLanguageService) getBean("PoLanguageService");
		languageService.init();
	}
    
    @Override
    protected void onSetUpAfterDataGeneration() throws Exception {
		cacheManager = (CacheManager) getBean("CacheManager");
	}
    	
    private PoLanguage createLanguage(String code, String name) {
    	PoLanguage lang1 = new PoLanguage();
    	lang1.setCode(code);
    	lang1.setName(name);
    	languageService.saveLanguage(lang1);
    	
    	return lang1;
    	
    }
    public void testCreateLanguages() {
    	
    	logger.info("Number of languages in transaction=" + languageService.loadAllLanguages().size());
    	
    	createLanguage("fr", "Francaise");
    	createLanguage("es", "Espanol");
    	
    	logger.info("Number of languages in transanction=" + languageService.loadAllLanguages().size());
    	assertTrue(languageService.loadAllLanguages().size() == 2 + PoConstants.getStandardLanguagesMap().size()); 
    }
    
    public void testCreateSameLanguage() {
    	PoLanguage german = languageService.findLanguageByCode("de");
    	assertNotNull("We anticipate de as default language!", german);

    	final String germanCode = german.getCode();
    	final String germanName = german.getName();
    	
    	try {
    		createLanguage(germanCode, germanName);
    		fail("it should not be possible to create 2 languages with same code!");
    	}
    	catch (PoRuntimeException e) {
    		// this was intended!
    	}
    	
    	assertTrue("expected number of langguages = " + PoConstants.getStandardLanguagesMap().size() + ", but was " + languageService.loadAllLanguages().size(), 
    			languageService.loadAllLanguages().size() == PoConstants.getStandardLanguagesMap().size());
    	
    	// change the code
    	german.setCode("de-at");
    	languageService.saveLanguage(german);

    	try {
    		createLanguage(germanCode, germanName);
    		fail("it should not be possible to create 2 languages with same Name!");
    	}
    	catch (PoRuntimeException e) {
    		// this was intended!
    	}

    	german.setName("Österreischisch");
    	languageService.saveLanguage(german);
    }
    
    @Override
    protected void onTearDownAfterTransaction() throws Exception {
    	super.onTearDownAfterTransaction();
    	
    	cacheManager.clearAllStartingWith("at.workflow.webdesk.po.model.PoLanguage");
    	logger.info("After Rollback: Number of languages=" + languageService.loadAllLanguages().size());
    }

}
