package at.workflow.webdesk.po.utils;

import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoDataSourceService;
import at.workflow.webdesk.po.PoGeneralSqlService;
import at.workflow.webdesk.po.model.PoDataSourceDefinition;
import at.workflow.webdesk.tools.PaginableQuery;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * Reads BLOB/byte[] from table and prints it out to the file.
 * Can be adapted to read and print anything from any available table to file or other output.
 * 
 * File creation and output are outcommented to prevent unintentional overwrite of existing files.
 * Comment them in when ready.
 * 
 * Please edit the parameters of the data source definition dsDef to point to your database
 * and sql to read your data.
 * 
 * @author sdzuban 04.12.2012
 */
public class DatabaseTableReader extends AbstractTransactionalSpringHsqlDbTestCase{
	
	private static final String MY_DATA_SOURCE = "dataSource";

	public void testReadDocContent() {
    	
    	PoDataSourceService dsService = (PoDataSourceService) applicationContext.getBean("PoDataSourceService");
    	PoGeneralSqlService dbService = (PoGeneralSqlService) applicationContext.getBean("PoGeneralSqlService");
    	
    	PoDataSourceDefinition dsDef = new PoDataSourceDefinition();
    	dsDef.setName(MY_DATA_SOURCE);
    	dsDef.setDriver("com.mysql.jdbc.Driver");
    	dsDef.setUrl("jdbc:mysql://localhost/webdesk");
    	dsDef.setPassword("admin");
    	dsDef.setUserName("root");
    	
    	dsService.saveDataSourceDefinition(dsDef);
    	List<Map<String, ?>> myDocs = dbService.select(dsService.getDataSource(MY_DATA_SOURCE), 
    			new PaginableQuery("select theContent from dmdoccontent where theContent like '%<report xmlns=\"http://www.eclipse.org/birt/2005/design\"%'"));
    	
    	int i = 0;
		for (Map<String, ?> myDoc : myDocs) {
			byte[] content = (byte[]) myDoc.get("theContent");
			if (content != null && content.length != 0) {
//				File file = new File("C:\\reports\\birt 2.3.2\\old reports\\report_" + i++ + ".xml");
				String contentString = new String(content);
//				try {
//					FileUtils.writeStringToFile(file, contentString);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
			}
		}
		System.out.println("Found " + i + " files.");
    }
}    

