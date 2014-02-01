package at.workflow.webdesk.tools;

import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * Mind that using these queries does not prevent from sending the
 * wrong query language expression (HQL (OQL) or SQL) to the wrong service
 * (PoGeneralDbService or PoGeneralSqlService)!
 * 
 * This class encapsulates everything necessary 
 * for a paginated query without parameters.
 * 
 * For pagination records are counted from 1.
 * 
 * Method for pagination emulation is provided.
 * 
 * This is the top class in the hierarchy.
 * 
 * @author sdzuban 05.03.2012
 */
public class PaginableQuery {

	private int firstResult = 0;	// default is "start with first record"
	private int maxResults = 0;	// default is "read all records"
	
	private String queryText = "";

	/**
	 * @param queryText can be in any query language supported by services, e.g. HQL, SQL
	 */
	public PaginableQuery(String queryText) {
		setQueryText(queryText);
	}
	
	/**
	 * HQL constructor for one certain database table.
	 * @param persistenceClass the persistence class related to the table to be queried.
	 */
	public PaginableQuery(Class<? extends PersistentObject> persistenceClass) {
		this("from "+persistenceClass.getSimpleName());
	}

	
	/** Sets the position of the first row to be read, starting from 0 (zero). */
	public void setFirstResult(int firstResult) {
		if (firstResult < 0)
			throw new IllegalArgumentException("Wrong start index, must be bigger equal zero: "+firstResult);
		
		this.firstResult = firstResult;
	}
	
	/** Number of rows to be fetched */
	public void setMaxResults(int maxResults) {
		if (maxResults < 0)
			maxResults = 0;
		
		this.maxResults = maxResults;
	}

	/** @return the first position of the row to be read, starting from 0 (zero). */
	public int getFirstResult() {
		return firstResult;
	}
	
	/**
	 * For pagination emulation result of this method
	 * must be added to firstResult. 
	 */
	public int getMaxResults() {
		return maxResults;
	}
	
	public String getQueryText() {
		return queryText;
	}
	
	/**
	 *  public so that first the where-clause can be generated 
	 *  with all the existing parameters and than replaced with 
	 *  the whole queryText with select/from/where... 
	 * @param queryText
	 */
	public final void setQueryText(String queryText) {
	     this.queryText = queryText.trim();
	}
	

	
	@Override
	public String toString() {
		return super.toString()+": queryText=>"+queryText+"<. firstResult="+firstResult+", maxResults="+maxResults;
	}

}
