package at.workflow.webdesk.tools.hibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.date.DateTools;

/** 
 * Parser-less functionality to extract, remove or replace a "where" or "order by" clause
 * within an HQL query expression. Looks for the tokens 'where', 'order by' and 
 * 'group by', NOT case-sensitive, i.e. you can use "WHERE" or "where".
 * This was made to support basic read functionality including paging support (->SimpleDataList).
 * <p/>
 * IT DOES NOT SUPPORT:
 * <ul>
 * 	<li>queries that contain nested sub-queries</li>
 * 	<li>hard-coded quoted filter values with complex contents</li>
 * 	<li>arbitrary spaces:
 * 		<ul>
 * 			<li>all three of "where", "order by", "group by" must be embedded within spaces (or newlines),</li>
 * 			<li>there must be exactly one space between "order" and "by",</li>
 * 			<li>the same for "group by"</li>
 * 		</ul>
 * 	<li></li>
 * </ul>
 * 
 * @author ggruber
 * @author fritzberger 2013-03-15
 */
public final class HqlUtils
{
	/** The HQL keyword "select". */
	public static final String SELECT = "select";
	/** The HQL keyword "from". */
	public static final String FROM = "from";
	/** The HQL keyword "where". */
	public static final String WHERE = "where";
	/** The HQL keyword "group by". */
	public static final String GROUP_BY = "group by";
	/** The HQL keyword "order by". */
	public static final String ORDER_BY = "order by";	// is always behind "group by"

	/** The HQL select clause keyword "distinct". */
	public static final String DISTINCT = "distinct";
	
	/** The HQL order-by keyword "desc". */
	private static final String DESC = "desc";
	/** The HQL order-by keyword "asc". */
	private static final String ASC = "asc";
	
	private static final String [] SELECT_KEYWORDS = new String [] {
		DISTINCT,
	};
	// TODO: further select clause keywords, but enclosing their expression with parentheses (no parentheses are implemented):
	//
	//     aggregate_expression ::= { AVG | MAX | MIN | SUM } ([DISTINCT] state_field_path_expression) | COUNT ([DISTINCT] identification_variable | state_field_path_expression | single_valued_association_path_expression)
	//
	// see http://docs.oracle.com/cd/E28613_01/apirefs.1211/e24396/ejb3_langref.html#ejb3_langref_select

	private static final String [] ORDER_BY_KEYWORDS = new String [] {
		DESC, ASC,
	};

	private static final String [] JOIN_KEYWORDS = new String [] {
		"left", "right", "outer", "inner", "join", "fetch",
	};

	
	/**
	 * @param query the HQL-query text to split.
	 * @return the "from" clause, or null if none found.
	 */
	public static String extractSelectClause(String query)	{
		final String [] parts = splitBy(query, FROM);	// there is always a FROM clause, no need to cut from tail
		return (parts == null) ? "" : parts[0];
	}
	
	/**
	 * @param query the HQL-query text to split.
	 * @return the "from" clause (including contained "join" clauses).
	 * @throws IllegalArgumentException if no "from" found.
	 */
	public static String extractFromClause(String query)	{
		final String [] parts = splitBy(query, FROM);
		if (parts == null)
			throw new IllegalArgumentException("A HQL query must have a FROM clause: >"+query+"<");
		
		return cutFromTail(parts[1], new String [][] {
				new String [] { WHERE },
				new String [] { GROUP_BY },
				new String [] { ORDER_BY },	// any "order by" will be behind "group by"
		});
	}
	
	/**
	 * Extracts the whereClause from the given HQL query. 
	 * @return the whereClause (including 'where'), or "" if no where-clause is present.
	 */
	public static String extractWhereClause(String query) {
		final String [] parts = splitBy(query, WHERE);
		if (parts == null)
			return "";
		
		return cutFromTail(parts[1], new String [][] {
				new String [] { GROUP_BY },
				new String [] { ORDER_BY },	// any "order by" will be behind "group by"
		});
	}

	/**
	 * Replaces a 'where'-clause in the given HQL query with the given new where-clause,
	 * removes the existing where-clause if new where-clause is null or empty.
	 * It is expected that the new where clause starts with the keyword 'where'.
	 * @param query the HQL query text to change.
	 * @param newWhereClause the new where clause, starting with the keyword 'where'
	 * @return a new HQL query, with removed or replaced where-clause.
	 */
	public static String replaceWhereClause(final String query, final String newWhereClause) {
		if (StringUtils.isBlank(newWhereClause))
			return removeWhereClause(query);
		
		// separate SELECT and FROM clause from WHERE and remainder
		String [] parts = splitBy(query, WHERE);
		if (parts == null)
			parts = splitBy(query, GROUP_BY);
		if (parts == null)
			parts = splitBy(query, ORDER_BY);
		
		final String selectFrom = (parts == null) ? query : parts[0];
		final String whereRemainder = (parts == null) ? null : parts[1];
		
		// separate WHERE clause from remainder
		parts = splitBy(whereRemainder, GROUP_BY);
		if (parts == null)	// no "group by" was found
			parts = splitBy(whereRemainder, ORDER_BY);
		
		return (parts != null) ? selectFrom+" "+newWhereClause+" "+parts[1] : selectFrom+" "+newWhereClause;
	}

	/** Removes the where-clause from the given HQL query. */
	public static String removeWhereClause(String query) {
		final String whereClause = extractWhereClause(query);
		if (StringUtils.isBlank(whereClause))
			return query;

		return StringUtils.remove(query, whereClause).trim();
	}

	
	
	/**
	 * Extracts the "order by" clause from given HQL query text.
	 * @param query the text where the "order by" should be extracted from, may contain no "order by".
	 * @return null when no "order by" was contained, else the part starting with "order by".
	 */
	public static String extractOrderByClause(String query) {
		final String [] parts = splitBy(query, ORDER_BY);
		return (parts == null) ? "" : parts[1];	// "order by" clause always is last behind "group by" and "having"
	}

	/**
	 * Builds a new "order by" clause from given properties and ascending flags.
	 * @param propertyIds the property names to use for the new "order by".
	 * @param ascending the according "asc" or "desc" flags for any property.
	 * @return the new "order by" clause, starting with "order by".
	 */
	public static String createOrderByClause(String[] propertyIds, boolean[] ascending) {
		if (propertyIds == null || propertyIds.length <= 0)
			return null;
		
		if (propertyIds.length != ascending.length)
			throw new IllegalArgumentException("Length of properties must be the same as length of ascending flags!");
		
		final StringBuilder sb = new StringBuilder("order by ");
		for (int i = 0; i < propertyIds.length; i++)	{
			final String separator = (i > 0) ? ", " : "";
			sb.append(separator + propertyIds[i]+" "+(ascending[i] ? "asc" : "desc"));
		}
		
		return sb.toString();
	}

	/**
	 * Replaces the originalOrderBy by newOrderBy in given query.
	 * Removes the "order by" clause when  is empty or null.
	 * @param query the whole query text where originalOrderBy is contained.
	 * @param newOrderBy the new "order by" clause, MUST start with "order by"!
	 * @return the query with substituted "order by" clause.
	 */
	public static String replaceOrderByClause(String query, String newOrderBy) {
		if (query == null)
			return null;
		
		final String [] parts = splitBy(query, ORDER_BY);
		if (parts == null)	// no "order by" was found
			return query+(newOrderBy == null ? "" : " "+newOrderBy);
		
		return parts[0]+" "+newOrderBy;
	}

	
	/**
	 * Splits a text into two trimmed parts using the given token as separator,
	 * whereby the token will be contained at start of second part (does not remove the token).
	 * @param query the HQL-query text to split.
	 * @param token the text that is the split point, will be contained in second part.
	 * @return the first part in return[0] and the second part in return[1] (also containing token)
	 * 		when the split token was found, else null.
	 */
	public static String [] splitBy(String query, String token) {
		return splitBy(query, new String [] { token });
	}
	
	/**
	 * Splits a text into two trimmed parts using the given token as separator,
	 * whereby the token will be contained at start of second part (does not remove the token).
	 * This searches the <b>leftmost</b> occurrence of one of the search-tokens.
	 * @param query the HQL-query text to split.
	 * @param tokens the text that is the split point, will be contained in second part.
	 * @return the first part in return[0] and the second part in return[1] (also containing token)
	 * 		when the split token was found, else null.
	 */
	public static String [] splitBy(String query, String [] tokens) {
		if (query == null)
			return null;
		
		final String lowerCaseQuery = query.toLowerCase();	// mind that the lower-case text is NOT used to extract the query, as HQL is case-sensitive!
		
		TokenAndIndex tokenAndIndex = indexOf(lowerCaseQuery, tokens);
		boolean wordStart = false;
		boolean wordEnd = false;
		
		while (tokenAndIndex.index >= 0 && (wordStart == false || wordEnd == false))	{
			wordStart = (tokenAndIndex.index <= 0 || Character.isWhitespace(lowerCaseQuery.charAt(tokenAndIndex.index - 1)));
			final int endIndex = (tokenAndIndex.index >= 0) ? tokenAndIndex.index + tokenAndIndex.token.length() : -1;
			wordEnd = (endIndex >= lowerCaseQuery.length() || endIndex >= 0 && Character.isWhitespace(lowerCaseQuery.charAt(endIndex)));
			
			if (wordStart == false || wordEnd == false)
				tokenAndIndex = indexOf(lowerCaseQuery, tokens, tokenAndIndex.index + tokenAndIndex.token.length());
		}
		
		return (tokenAndIndex.index >= 0 && wordStart && wordEnd)
				? new String [] { query.substring(0, tokenAndIndex.index).trim(), query.substring(tokenAndIndex.index).trim() }
				: null;
	}
	
	
	/**
	 * Accepts "select" clauses of a HQL query, e.g.
	 * 		"select p.userName, pg.shortName",
	 * and gives back a list of tokens scanned from that text, e.g.
	 * 		["p.userName", "pg.shortName"].
	 * 
	 * @param clause the SELECT clause to be tokenized (use one of the extractSelectClause() to retrieve it).
	 * @return all tokens of a SELECT clause, without commas,
	 * 		any optional DISTINCT keyword will stay prepended within the String
	 * 		(separated by one space before the property name).
	 * 		All names can contain dots. 
	 */
	public static String [] tokenizeSelectClause(String clause)	{
		return tokenizeClause(SELECT, clause, true);
	}
		
	/**
	 * Accepts "order by" clauses of a HQL query, e.g.
	 * 		"order by p.lastName asc, p.credit desc",
	 * and gives back a list of tokens scanned from that text, e.g.
	 * 		["p.lastName asc", "p.credit desc"].
	 * 
	 * @param clause the ORDER BY clause text to be tokenized (use extractOrderByClause() to retrieve it).
	 * @return all tokens of a ORDER BY clause, without commas,
	 * 		any optional DESC or ASC keyword will be stay appended within the String
	 * 		(separated by one space after the property name).
	 * 		All names can contain dots. 
	 */
	public static String [] tokenizeOrderByClause(String clause)	{
		return tokenizeClause(ORDER_BY, clause, false);
	}
	

	/**
	 * Splits a FROM clause into its parts (every FROM part can contain several JOINs).
	 * @param clause the FROM clause to split.
	 * @return an array of String arrays[2],
	 * 		an outer array element represents one comma-section of the FROM clause,
	 * 		the first inner array element represents the FROM part, the second the JOIN part (can be "").
	 * 		Example:
	 * 			"from PoPerson p join p.memberOfGroups as pg, PoClient as c"
	 * 		would yield
	 * 			[["PoPerson p", "join p.memberOfGroups as pg"], ["PoClient c", ""]]
	 */
	public static String [][] splitFromClause(String clause)	{
		final String clauseText = initTokenization(FROM, clause);
		final List<String[]> result = new ArrayList<String[]>();
		
		final StringTokenizer stok = new StringTokenizer(clauseText, ",");
		while (stok.hasMoreTokens())	{
			final String token = stok.nextToken().trim();
			
			final String [] parts = splitBy(token, JOIN_KEYWORDS);
			final String from = removeContainedAsKeyword(parts != null ? parts[0] : token);
			final String join = removeAllLeadingJoinKeywords(parts != null ? parts[1] : "");
			
			result.add(new String [] { from, join });
		}
		
		return result.toArray(new String [result.size()][]);
	}

	/**
	 * @param clause all optional JOIN clauses of a FROM part, can contain several joins.
	 * @return all tokens scanned from the given join clause, aliases are appended behind property using a space.
	 */
	public static String [] tokenizeJoins(String clause)	{
		clause = clause.trim();
		final List<String> tokens = new ArrayList<String>();
		
		while (clause != null)	{	// while JOIN clauses are contained
			final String clauseText = removeAllLeadingJoinKeywords(clause);
			final String [] parts = splitBy(clauseText, JOIN_KEYWORDS);
			
			if (parts != null)	{	// do first part
				tokenizeJoin(parts[0], tokens);
				clause = parts[1];
			}
			else	{	// do the final join clause
				tokenizeJoin(clauseText, tokens);
				clause = null;
			}
		}
		return tokens.toArray(new String [tokens.size()]);
	}
	
	
	/** @return true if given token is one of DISTINCT, COUNT, SUM, AVG, MIN, MAX. */
	public static boolean isSelectClauseKeyword(String token) {
		return isContained(token, SELECT_KEYWORDS);
	}
	
	
	/** Scans exactly one JOIN clause. */
	private static void tokenizeJoin(String join, List<String> tokens) {
		if (join.length() <= 0)
			return;
		
		final StringTokenizer stok = new StringTokenizer(join, " \r\n\t");
		final String tableName = stok.nextToken().trim();
		final String secondToken = stok.hasMoreTokens() ? stok.nextToken().trim() : null;
		final String alias = (secondToken != null && secondToken.equalsIgnoreCase("as")) ? stok.nextToken().trim() : secondToken;
		tokens.add(tableName+(alias != null ? " "+alias : ""));
	}
	

	/** Asserts and removes startOfTextToAssert from head of clauseText. */
	private static String initTokenization(String startOfTextToAssert, String clauseText)	{
		return initTokenization(new String [] { startOfTextToAssert }, clauseText);
	}
	
	/** Asserts and removes the one of startOfTextToAssert array from head of clauseText. */
	private static String initTokenization(String [] startOfTextToAssert, String clauseText)	{
		if (clauseText == null)
			throw new IllegalArgumentException("Can not tokenize a null clause!");
		
		clauseText = clauseText.trim();
		
		final int found = findLeadingText(startOfTextToAssert, clauseText);
		if (found == -1)
			throw new IllegalArgumentException("Expected one of "+Arrays.toString(startOfTextToAssert)+" at start of >"+clauseText+"<");
			
		return clauseText.substring(found).trim();
	}

	private static int findLeadingText(String[] toSearch, String clauseText) {
		for (String token : toSearch)	{
			final int length = token.length();
			final String startString = clauseText.substring(0, Math.min(length, clauseText.length()));
			if (token.equalsIgnoreCase(startString))
				return length;
		}
		return -1;
	}
	
	private static String [] tokenizeClause(String startOfTextToAssert, String clause, boolean isSelectClause)	{
		final String clauseText = initTokenization(startOfTextToAssert, clause);
		final List<String> tokens = new ArrayList<String>();
		
		// simple clauses consist of parts separated by comma, like "distinct p.UID, p.firstName, p.lastName"
		for (final StringTokenizer stok = new StringTokenizer(clauseText, ","); stok.hasMoreTokens(); )	{
			final String token = stok.nextToken().trim();
			
			final String [] parts = containsSpace(token);
			if (parts != null)	{	// e.g. "distinct p.UID", or "p.lastName desc"
				final boolean validKeyword = isSelectClause ? isSelectClauseKeyword(parts[0]) : isOrderByClauseKeyword(parts[1]);
				if (validKeyword == false) 
					throw new IllegalArgumentException("Unknown keyword '"+parts[0]+"' in clause >"+clause+"<");
				
				tokens.add(parts[0]+" "+parts[1]);
			}
			else	{
				tokens.add(token);
			}
		}
		return tokens.toArray(new String [tokens.size()]);
	}
	
	/** Erases an optional "as" within given token. */
	private static String removeContainedAsKeyword(String from) {
		final String result;
		final String [] fromParts = containsSpace(from);	// could be "PoPerson p", or "PoPersonGroup as pg", remove the "as"
		
		if (fromParts != null)	{
			final String [] secondParts = containsSpace(fromParts[1]);
			
			if (secondParts != null)	{	// there must be "as"
				if (secondParts[0].equals("as") == false)
					throw new IllegalArgumentException("Unknown keyword '"+secondParts[0]+"' between table and alias in >"+from+"<");
				
				result = fromParts[0]+" "+secondParts[1];
			}
			else	{
				result = fromParts[0]+" "+fromParts[1];
			}
		}
		else	{
			result = from;
		}
		return result;
	}
		

	
	
	private static class TokenAndIndex
	{
		public final int index;
		public final String token;
		
		public TokenAndIndex(int index, String token) {
			this.index = index;
			this.token = token;
		}
	}
	
	/**
	 * @param text lower-case text to search for first occurrence of token.
	 * @return the index of the first occurrence of token in text, 0-n.
	 */
	private static TokenAndIndex indexOf(String text, String [] tokens)	{
		return indexOf(text, tokens, 0);
	}

	/**
	 * @param text lower-case text to search for first occurrence of token.
	 * @return the index of the first occurrence of token in text, 0-n.
	 */
	private static TokenAndIndex indexOf(String text, String [] tokens, int startIndex)	{
		int leftMostIndex = Integer.MAX_VALUE;
		String leftMostToken = null;
		for (String token :  tokens)	{
			final int i = text.indexOf(token, startIndex);
			if (i >= 0 && i < leftMostIndex)	{
				leftMostIndex = i;
				leftMostToken = token;
			}
		}
		return (leftMostIndex == Integer.MAX_VALUE) ? new TokenAndIndex(-1, null) : new TokenAndIndex(leftMostIndex, leftMostToken);
	}
	
	
	
	
	/** @return null when no token was found, else the first part of the split text. */
	private static String cutFromTail(String text, String [] tokens)	{
		final String [] parts = splitBy(text, tokens);
		return (parts == null) ? null : parts[0];
	}
	
	/** @return the untouched input text when no token was found, else cut at the first first found token, never null. */
	private static String cutFromTail(String text, String [][] tokenArrays)	{
		for (String [] tokens : tokenArrays)	{
			final String cuttenText = cutFromTail(text, tokens);
			if (cuttenText != null)	// found token
				return cuttenText;
		}
		return text;	// none of the tokens was found
	}
	
	
	private static boolean isContained(String token, String [] keywords) {
		token = token.toLowerCase();
		for (String keyWord : keywords)
			if (keyWord.equals(token))
				return true;
		return false;
	}
	
	
	private static String removeAllLeadingJoinKeywords(String joinClause)	{
		int found;
		do	{
			found = findLeadingText(JOIN_KEYWORDS, joinClause);
			if (found != -1)
				joinClause = joinClause.substring(found).trim();
		}
		while (found != -1);
		return joinClause;
	}

	/**
	 * @param token the token to split into two parts, expected to have no leading spaces.
	 * @return null when no space was found, else a String array[2],
	 * 		the leading text until the first space (excluding) is in first array element,
	 * 		and the trailing remainder (trimmed) in second array element.
	 */
	public static String [] containsSpace(String token) {
		final int i = StringUtils.indexOfAny(token, new char [] { ' ', '\t', '\r', '\n', });
		if (i < 0)	// not found
			return null;
		
		return new String [] { token.substring(0, i), token.substring(i).trim() };
	}

	/**
	 * @param token the token to split into two parts.
	 * @return null when no dot was found, else a String array[2],
	 * 		the leading text until the first dot (excluding) is in first array element,
	 * 		and the trailing remainder (trimmed) in second array element.
	 */
	public static String [] containsDot(String token) {
		final int i = token.indexOf(".");
		if (i < 0)	// not found
			return null;
		
		return new String [] { token.substring(0, i), token.substring(i + 1).trim() };
	}

	private static boolean isOrderByClauseKeyword(String token) {
		return isContained(token, ORDER_BY_KEYWORDS);
	}
	
	
	/**
	 * recreates NamedQuery and replaces all HQL method calls to current_timestamp() with a named parameter
	 * which is filled with DateTools.now(). Assumes that current_timestamp() is only used in the WHERE clause.
	 * 
	 * @param query is the original NamedQuery
	 * @return a new NamedQuery object.
	 */
	public static NamedQuery replaceCurrentTimeStampFunctionWithNow(NamedQuery query) {
		
		if (query.getQueryText().contains("current_timestamp()")==false)
			return new NamedQuery(query.getQueryText(), query.getParamNames(), query.getParamValues());
		
		String newQueryText = query.getQueryText().replaceAll("current_timestamp\\(\\)",":CURRENT_TIMESTAMP");
		String[] newParamNames = (String[]) ArrayUtils.add(query.getParamNames(), "CURRENT_TIMESTAMP");
		Object[] newParamValues = ArrayUtils.add(query.getParamValues(), DateTools.now());
		return new NamedQuery(newQueryText, newParamNames, newParamValues);
	}

	private HqlUtils() {}	// do not instantiate

}
