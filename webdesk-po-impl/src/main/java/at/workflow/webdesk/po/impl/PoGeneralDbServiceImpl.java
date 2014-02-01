package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.daos.PoGeneralDAO;
import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.PaginableQuery;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * Provides generic persistence access via PoGeneralDAO.
 */
@SuppressWarnings("unchecked")
public class PoGeneralDbServiceImpl implements PoGeneralDbService {

	/** collects all the select clause terms between "select" and "from" no matter how formatted the string is */
//	parts of the regex: <something>select(select clause terms)from<something> 
	private static final String SELECT_TERMS_EXTRACTOR_REGEX = "[\\s\\S]*\\b[sS][eE][lL][eE][cC][tT]\\b\\s*([\\s\\S]*?\\S)\\s+\\b[fF][rR][oO][mM]\\b[\\s\\S]*"; 
	public static Pattern SELECT_TERMS_EXTRACTOR_PATTERN = Pattern.compile(SELECT_TERMS_EXTRACTOR_REGEX);
	
	
	private PoGeneralDAO generalDAO;
	
	@Override
	public void deleteObject(PersistentObject object) {
		generalDAO.deleteObject(object);
	}

	@Override
	public void evictObject(Object o) {
		generalDAO.evictObject(o);
	}

	// ----- methods working directly with query objects ----------
	

	/** {@inheritDoc} */
	@Override
	public List selectObjectsOrArrays(PaginableQuery query) {
		return generalDAO.getListOfPOJOsOrArrays(query);
	}

	/** {@inheritDoc} */
	@Override
	public List<Map<String, ?>> select(PaginableQuery query) {
		List<Object[]> listOfArrays = generalDAO.getListOfPOJOsOrArrays(query);
		return convertResultToListOfNamedMaps(listOfArrays, query.getQueryText());
	}
	
	@Override
	public List getElementsAsList(PositionalQuery query) {
		return generalDAO.getElementsAsList(query);
	}
	
	@Override
	public List<Map<String, ?>> getElementsAsListOfNamedMaps(PositionalQuery query) {
		List<Object[]> listOfArrays = generalDAO.getElementsAsList(query);
		return convertResultToListOfNamedMaps(listOfArrays, query.getQueryText());
	}
	
	@Override
	public List getElementsAsList(NamedQuery query) { 
		return generalDAO.findByNamedParam(query);
	}
	
	@Override
	public List<Map<String, ?>> getElementsAsListOfNamedMaps(NamedQuery query) {
		List<Object[]> listOfArrays = generalDAO.findByNamedParam(query);
		return convertResultToListOfNamedMaps(listOfArrays, query.getQueryText());
	}
	
	// ------- methods working with query string and keys ----------- 
	
	@Override
	public List getElementsAsList(String query, Object[] keys) {
		return generalDAO.getElementsAsList(query, keys);
	}
	
	@Override
	public List<Map<String, ?>> getElementsAsListOfMaps(String query, Object[] keys) {
		List<Object[]> listOfArrays = generalDAO.getElementsAsList(query, keys);
		List<Map<String, ?>> listOfMaps = new ArrayList<Map<String, ?>>();
		for (Object[] array : listOfArrays) {
			listOfMaps.add(generateColumnMap(array));
		}
		return listOfMaps;
	}

	@Override
	public List<Map<String, ?>> getElementsAsListOfNamedMaps(String query, Object[] keys) {
		List<Object[]> listOfArrays = generalDAO.getElementsAsList(query, keys);
		return convertResultToListOfNamedMaps(listOfArrays, query);
	}
	
	@Override
	public List<Map<String, ?>> convertResultToListOfNamedMaps(List<Object[]> listOfArrays, String query) {
		
		if (listOfArrays == null || listOfArrays.isEmpty())
			return Collections.emptyList();	
		
		List<Map<String, ?>> listOfMaps = new ArrayList<Map<String, ?>>();
		
		String[] names = getSelectExpressionsAndAliases(query);
		if (names != null) {
			for (Object[] array : listOfArrays) {
				listOfMaps.add(generateNamedMap(names, array));
			}
		} else {
			for (Object[] array : listOfArrays) {
				listOfMaps.add(generateColumnMap(array));
			}
		}
		return listOfMaps;
	}
	
	@Override
	public List findByNamedParam(String query, String[] paramNames,	Object[] values) {
		return generalDAO.findByNamedParam(query, paramNames, values);
	}
	
	/** fri_25-02-2011: generates "columnX" string where X is the 0-n index of the column in the given array. */
	private Map<String, Object> generateColumnMap(Object[] array) {
		Map<String, Object> ret = new HashMap<String, Object>();
		for (int i = 0; i < array.length; i++) {
			ret.put("column" + i , array[i]);
		}
		return ret;
	}

	// ---------- methods directly accessing objects -----------
	
	@Override
	public PersistentObject getObject(Class<? extends PersistentObject> clazz, String uid) {
		return generalDAO.getObject(clazz, uid);
	}

	@Override
	public List<? extends PersistentObject> loadAllObjectsOfType(Class<? extends PersistentObject> clazz) {
		return generalDAO.loadAllObjectsOfType(clazz);
	}

	@Override
	public void saveObject(PersistentObject object) {
		generalDAO.saveObject(object);
	}

	public void setGeneralDAO(PoGeneralDAO generalDAO) {
		this.generalDAO = generalDAO;
	}

	/**
	 * Parses select clause and extracts expressions and aliases.
	 * @param query
	 * @return array of expressions/aliases of the select clause
	 * in the order as they appear in the select clause
	 */
	public static String[] getSelectExpressionsAndAliases(String query) {
		
		String trimmed = query.trim();
		
		Matcher matcher = SELECT_TERMS_EXTRACTOR_PATTERN.matcher(trimmed);
		if (matcher.matches()) {
        
			String selectExpressionsString = matcher.group(1);
			String[] selectExpressions = selectExpressionsString.split(",");
			String[] expressionsAndAliases = new String[selectExpressions.length];
			
			int idx = 0;
			for (String selectExpression : selectExpressions) {
				trimmed = selectExpression.trim();
				String[] parts = null;
				if (trimmed.indexOf(" as ") > 0)
					parts = trimmed.split(" as ");
				else if (trimmed.indexOf(" AS ") > 0)
					parts = trimmed.split(" AS ");
				
				if (parts == null) { // no alias, simple expression
					expressionsAndAliases[idx++] = trimmed;
				} else {
					expressionsAndAliases[idx++] = parts[1].trim();
				}
			}
			return expressionsAndAliases;
		}
		return null;
	}

	/**
	 * Processes one result row to map where keys are the names and values are row values
	 * @param names
	 * @param rowValues
	 * @return
	 */
	private Map<String, Object> generateNamedMap(String[] names, Object[] rowValues) {
		Map<String, Object> ret = new HashMap<String, Object>();
		for (int i = 0; i < rowValues.length; i++) {
			ret.put(names[i] , rowValues[i]);
		}
		return ret;
	}
}
