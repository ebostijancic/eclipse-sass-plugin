package at.workflow.webdesk.po.daos;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoLanguage;

/**
 * Created on 20.06.2005
 * @author hentner (Harald Entner)
 */
public interface PoLanguageDAO extends GenericDAO<PoLanguage> {
	
    /**
     * find PoLanguage by LanguageCode
     * @param langCode
     * @return PoLanguage Object
     */
    public PoLanguage findLanguageByCode(String langCode);
    
    /**
     * find PoLanguage by Name
     * @param name of language to find
     * @return PoLanguage Object
     */
    public PoLanguage findLanguageByName(String langName);
    
    /**
     * returns the default language as set in the administation
     * @return PoLangugage Object
     */
    public PoLanguage findDefaultLanguage();
    
	public void flush();

}
