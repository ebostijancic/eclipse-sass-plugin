package at.workflow.webdesk.po.impl.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * Parser for parsing JDBC Connection strings to extract hostname, port and databasename
 * Works correctly with MS-SQL Server and MySQL. Does currently not work with Oracle, as
 * the schema name can not be extracted from the connection string. 
 * 
 * @author ggruber
 *
 */
public class JdbcUrlParser {
	
	private String connectionString;
	
	protected String port;
	protected String hostName;
	protected String databaseName;
	
	public JdbcUrlParser(String connectionString) {
		super();
		this.connectionString = connectionString;
	}

	public void parse() {
		try {
			URL jdbcURL = new URL( getSanitizedUrlString( connectionString ) );
			
			hostName = jdbcURL.getHost();
			if ( jdbcURL.getPort() > 0 ) {
				port = Integer.toString( jdbcURL.getPort() );
			}
			
			String queryString = jdbcURL.getQuery();
			
			if (StringUtils.isEmpty( jdbcURL.getPath() ) == false) {
				databaseName = jdbcURL.getPath().replaceAll("/", "");
			} else if ( StringUtils.isEmpty(queryString)==false && 
					getParameterMap(queryString).get("databasename")!=null) {
				databaseName = getParameterMap(queryString).get("databasename");
			}
			
		}
		catch (MalformedURLException e) {
			throw new RuntimeException("Problems parsing the passed JDBC connection string..", e);
		}
	}

	public String getDatabaseName() {
		return databaseName;
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public String getPort() {
		return port;
	}
	
	private String getSanitizedUrlString(String jdbcUrl) {
	      String sanitizedString = null;
	      int schemeEndOffset = jdbcUrl.indexOf("://");
	      if (-1 == schemeEndOffset) {
	        // couldn't find one? try our best here.
	    	
	    	schemeEndOffset = jdbcUrl.indexOf("@");
	    	if (schemeEndOffset>0) {
	    		// Oracle
	    		throw new IllegalArgumentException("Parsing of Oracle JDBC Connection Strings currently not supported!");
	    		
	    	} else {
	    		sanitizedString = "http://" + jdbcUrl;
	    	}
	      } else {
	        sanitizedString = "http" + jdbcUrl.substring(schemeEndOffset);
	      }
	      
	      sanitizedString = sanitizedString.replaceFirst(";", "?");
	      sanitizedString = sanitizedString.replaceAll(";", "&");
	      
	      return sanitizedString;
	}
	
	private Map<String,String> getParameterMap(String queryString) {
		
		Map<String,String> ret = new HashMap<String,String>();
		String[] paramTupples = queryString.split("&");
		for (int i=0;i<paramTupples.length;i++) {
			if (!"".equals(paramTupples[i])) {
				String key = paramTupples[i].split("=")[0].toLowerCase();
				String value="";
				if (paramTupples[i].split("=").length==2) {						
					value = paramTupples[i].split("=")[1];
				}
				
				if (!ret.containsKey(key) || ret.get(key)==null || "".equals(ret.get(key)))
					ret.put(key, value);
			}
		}
		return ret;
	}
}
