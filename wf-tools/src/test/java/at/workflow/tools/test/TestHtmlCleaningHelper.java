package at.workflow.tools.test;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.tools.HtmlCleaningHelper;
import at.workflow.tools.XMLTools;

public class TestHtmlCleaningHelper extends TestCase {
	
	public void testHtmlCleaning() throws Exception {
		
		Resource res = new ClassPathResource("htmlbody.txt");
		String htmlString;
		htmlString = FileUtils.readFileToString(res.getFile());
		
		Document doc = HtmlCleaningHelper.cleanAndCreateHtmlDoc(htmlString);
		
		String result = XMLTools.createStringFromW3cDoc(XMLTools.convertToW3cDoc(doc));
		
		System.out.println("input=\n" + htmlString);
		System.out.println("\n\n output=\n" + result);
		
		
	}

}
