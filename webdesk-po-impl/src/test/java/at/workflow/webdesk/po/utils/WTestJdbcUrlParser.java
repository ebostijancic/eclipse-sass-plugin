package at.workflow.webdesk.po.utils;

import at.workflow.webdesk.po.impl.util.JdbcUrlParser;

import junit.framework.TestCase;

public class WTestJdbcUrlParser extends TestCase {

	
	public void testDifferentUrlsFromMySQLAndSQLServer() {
		
		// MS SQL Server 
		assertJdbcConnectionString( "jdbc:sqlserver://localhost:1433;databaseName=rlbstmk-webdesk" , "localhost", "1433", "rlbstmk-webdesk");
		
		assertJdbcConnectionString( "jdbc:sqlserver://wfdb01t:1433;DatabaseName=spielo_shark;SelectMethod=cursor" , 
				"wfdb01t", "1433", "spielo_shark");
		
		assertJdbcConnectionString( "jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=northwind", 
				"localhost", "1433", "northwind");
		
		assertJdbcConnectionString( "jdbc:jtds:sqlserver://wfdb01/dbName;instance=instanceName", 
				"wfdb01", null, "dbName");

		// MYSQL
		assertJdbcConnectionString( "jdbc:mysql://localhost/demo33webdesk" , 
				"localhost", null, "demo33webdesk");
		
	}
	
	public void testOracleConnectionString() {
		JdbcUrlParser parser = new JdbcUrlParser( "jdbc:oracle:thin:@wforacle01:1521:webdesk" );
		
		try {
			parser.parse();
			fail("this should fail, as we are not supporting oracle for now..");
		} catch (Exception e) {
			
		}
		
	}
	
	private void assertJdbcConnectionString( String url, String expectedHostName, String expectedPort, String expectedDatabaseName) {
		
		JdbcUrlParser parser = new JdbcUrlParser(url);
		parser.parse();
		
		assertEquals(parser.getHostName(), expectedHostName );
		assertEquals(parser.getPort(),expectedPort);
		assertEquals(parser.getDatabaseName(),expectedDatabaseName);
		
	}
	
}
