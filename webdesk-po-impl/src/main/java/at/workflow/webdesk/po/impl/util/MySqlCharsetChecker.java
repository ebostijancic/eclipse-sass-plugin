package at.workflow.webdesk.po.impl.util;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import at.workflow.webdesk.po.PoDataSourceService;
import at.workflow.webdesk.po.PoGeneralSqlService;
import at.workflow.webdesk.tools.PositionalQuery;

/** 
 * This class checks the Sanity of the Charset Definitions of the 
 * Webdesk MySQL Database
 * 
 * @author ggruber
 */
public class MySqlCharsetChecker {

	private PoGeneralSqlService generalSQLService;
	private PoDataSourceService dataSourceService;
	
	private Logger logger = Logger.getLogger( getClass() );

	
	public void checkCharsetSanity() {	
		
		try {

			DataSource webdeskDataSource = dataSourceService.getDataSource( PoDataSourceService.WEBDESK );
			
			String dbName = dataSourceService.guessDefaultSchema(webdeskDataSource);
			String tableCharSetCheckSQL = "SELECT TABLE_COLLATION, count(*) as count FROM INFORMATION_SCHEMA.Tables WHERE table_schema=? GROUP BY TABLE_COLLATION";
			String columnCharSetCheckSQL = "SELECT COLLATION_NAME, count(*) as count FROM INFORMATION_SCHEMA.COLUMNS WHERE table_schema=? and COLLATION_NAME is not null group by COLLATION_NAME";
			
			// TODO: do not use PaginableQuery here, this is the only place where this happens (for JDBC), and there is no need for pagination here!
			List<Map<String,?>> charsetsPerTable = generalSQLService.select(webdeskDataSource, new PositionalQuery(tableCharSetCheckSQL, new Object[] { dbName }));
			List<Map<String,?>> charsetsPerColumns = generalSQLService.select(webdeskDataSource, new PositionalQuery(columnCharSetCheckSQL, new Object[] { dbName }));
			
			if (charsetsPerTable.size()>1 || charsetsPerColumns.size()>1) {
				
				System.err.println("========================================================================");
				System.err.println("                   WARNING !!!!!!! ");
				System.err.println("The Webdesk Database Schema has inconsistent Charset Definitions!");
				for (Map<String,?> record : charsetsPerTable) {
					System.err.println(record.get("count") + " records have " + record.get("TABLE_COLLATION") + " as charset.");
				}
				for (Map<String,?> record : charsetsPerColumns) {
					System.err.println(record.get("count") + " columns have " + record.get("COLLATION_NAME") + " as charset.");
				}
				System.err.println("Please repair your mysql database by dumping and reimporting your data");
				System.err.println("as described in: http://www.workflow.at/daisy/webdesk-tech/5800-dsy.html");
				System.err.println("========================================================================");
				
			}
		
		} catch (Exception e) {
			logger.warn("Checking the charset sanity of this MySQL server did not work! (might be a version < 5.x) Cause was: " + e.getMessage(),e);
		}
		
	}

	/** Spring Setter */ 
	public void setGeneralSQLService(PoGeneralSqlService generalSQLService) {
		this.generalSQLService = generalSQLService;
	}
	
	/** Spring Setter */ 
	public void setDataSourceService(PoDataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}
	
}
