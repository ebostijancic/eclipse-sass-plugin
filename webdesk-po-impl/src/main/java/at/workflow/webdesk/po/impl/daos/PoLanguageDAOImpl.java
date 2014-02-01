package at.workflow.webdesk.po.impl.daos;

import org.springframework.dao.support.DataAccessUtils;
import at.workflow.webdesk.po.daos.PoLanguageDAO;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * Hibernate Data Access object for the <code>PoLanguage</code> (and corresponding 
 * <code>PoTextModule</code> objects.
 * <br>
 * Created on 22.06.2005<br>
 * created at:       14.05.2007<br>
 * @author DI Harald Entner (hentner)<br>
 */
public class PoLanguageDAOImpl extends GenericHibernateDAOImpl<PoLanguage> implements PoLanguageDAO {

	@Override
	protected Class<PoLanguage> getEntityClass() {
		return PoLanguage.class;
	}

    /**
     * Find a PoLanguage by its code.
     * @param langCode, e.g. "de" or "en".
     * @return the PoLangauge object having that code, or null when not found.
     */
    public PoLanguage findLanguageByCode(String langCode) {
        Object[] keyValues = { langCode };
        return (PoLanguage)DataAccessUtils.uniqueResult(this.getHibernateTemplate().find("from PoLanguage where code = ?", keyValues));
    }
    
    public PoLanguage findLanguageByName(String langName) {
    	Object[] keyValues = { langName };
    	return (PoLanguage)DataAccessUtils.uniqueResult(this.getHibernateTemplate().find("from PoLanguage where name = ?", keyValues));
    }
		
    public PoLanguage findDefaultLanguage() {
        Object[] keyValues = { Boolean.TRUE };
        return (PoLanguage)DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoLanguage where defaultLanguage = ?", keyValues));
    }

	public void flush() {
		getHibernateTemplate().clear();
		getHibernateTemplate().flush();
	}

}
