package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;

import at.workflow.webdesk.po.PoSearchService;
import at.workflow.webdesk.tools.ReflectionUtils;
import at.workflow.webdesk.tools.api.BusinessLogicException;
import at.workflow.webdesk.tools.api.I18nMessage;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadata;

public class PoSearchServiceImpl implements PoSearchService {

	private static Logger logger = Logger.getLogger(PoSearchServiceImpl.class);
	private static final int BATCH_SIZE = 100;

	private SessionFactory sessionFactory;
	private PersistenceMetadata persistenceMetadata;

	@Override
	public void cleanSearchIndex() {
		FullTextSession fullTextSession = createFullTextSession();
		for (Class<? extends PersistentObject> currentClassToIndex : getPersistenceClassesToIndex()) {
			cleanClassInIndex(currentClassToIndex, fullTextSession);
		}
	}

	@Override
	public void refreshSearchIndex() {
		FullTextSession fullTextSession = createFullTextSession();
		for (Class<? extends PersistentObject> currentClassToIndex : getPersistenceClassesToIndex()) {
			refreshClassIndex(currentClassToIndex, fullTextSession);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<? extends PersistentObject> search(String searchPhrase, Class<? extends PersistentObject>[] entityClasses) {
		
		FullTextSession fullTextSession = Search.getFullTextSession(getCurrentSession());

		Set<String> allClassesFields  = new HashSet<String>();
		for (Class<? extends PersistentObject> clazz : entityClasses) {
			String[] indexedFieldNames = ReflectionUtils.getIndexedFieldNames(clazz);
			
			// check if one of the names is already contained, see WDHREXPERT-559
			for (String indexedFieldName : indexedFieldNames)
				if (allClassesFields.contains(indexedFieldName))
					throw new IllegalArgumentException("The given entity classes contain fields with same name: "+indexedFieldName);
			
			allClassesFields.addAll(Arrays.asList(indexedFieldNames));
		}
		
		String[] fields = allClassesFields.toArray(new String[allClassesFields.size()]);
		
		final MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
		parser.setAllowLeadingWildcard(true);
		try {
			String normalizeSearchPhrase = lowerCaseAndWildcards4SearchPhrase(searchPhrase);
			final Query query = parser.parse(normalizeSearchPhrase);
			FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, entityClasses);
			return fullTextQuery.list();
		}
		catch (ParseException e) {
			logger.warn("Lucene could not parse the passed searchPhrase >" + searchPhrase + "<, error was: " + e.getMessage(), e);
			throw new BusinessLogicException(new I18nMessage(e.getMessage()));
		}
	}

	
	private String lowerCaseAndWildcards4SearchPhrase(String searchPhrase) {
		return "*" + searchPhrase.toLowerCase() + "*";
	}

	private FullTextSession createFullTextSession() {
		FullTextSession fullTextSession = Search.getFullTextSession(getCurrentSession());
		configureFullTexSessionForPerformantIndexing(fullTextSession);
		return fullTextSession;
	}

	private void cleanClassInIndex(Class<? extends PersistentObject> currentClassToIndex, FullTextSession fullTextSession) {
		assert fullTextSession != null;
		fullTextSession.purgeAll(currentClassToIndex);
	}

	private void refreshClassIndex(Class<? extends PersistentObject> currentClassToIndex, FullTextSession fullTextSession) {
		assert fullTextSession != null;
		ScrollableResults results = getScrollableResults(currentClassToIndex, fullTextSession);
		int currentIndex = 0;

		logger.info("Batch indexing :" + currentClassToIndex.getName());
		
		while (results.next()) {
			currentIndex++;
			fullTextSession.index(results.get(0)); // index element
			if (logger.isDebugEnabled())
				logger.debug("Indexing:" + results.get(0));
			
			if (currentIndex % BATCH_SIZE == 0) {
				fullTextSession.flushToIndexes(); // apply changes to indexes
				fullTextSession.clear(); // clear processed queue
			}
		}
	}

	private ScrollableResults getScrollableResults(Class<? extends PersistentObject> currentClassToIndex, FullTextSession fullTextSession) {
		return fullTextSession.createCriteria(currentClassToIndex)
				.setFetchSize(BATCH_SIZE)
				.scroll(ScrollMode.FORWARD_ONLY);
	}
	
	private Collection<? extends Class<? extends PersistentObject>> getPersistenceClassesToIndex() {
		final Collection<Class<? extends PersistentObject>> persistenceClassesToIndex = new ArrayList<Class<? extends PersistentObject>>();
		for (Class<? extends PersistentObject> clazz : getPersistedClassesInDB()) {
			if (ReflectionUtils.isIndexedForTextSearch(clazz)) {
				persistenceClassesToIndex.add(clazz);
			}
		}
		return persistenceClassesToIndex;
	}

	private List<Class<? extends PersistentObject>> getPersistedClassesInDB() {
		return persistenceMetadata.getMappedClasses();
	}

	private Session getCurrentSession() {
		Session session = sessionFactory.getCurrentSession();
		assert session != null;
		return session;
	}

	private void configureFullTexSessionForPerformantIndexing(FullTextSession fullTextSession) {
		assert fullTextSession != null;
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
	}

	
	
	/** Spring XML noise, do not use. */
	public void setPersistenceMetadata(PersistenceMetadata persistenceMetadata) {
		this.persistenceMetadata = persistenceMetadata;
	}

	/** Spring XML noise, do not use. */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}
