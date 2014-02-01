package at.workflow.tools.utils;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import at.workflow.tools.XMLTools;
import at.workflow.tools.XMLTools.XMLValidationResult;

/**
 * This utility serves validation of xml file vs. xsd file.
 * 
 * Please edit file paths accordingly.
 * 
 * @author sdzuban 26.09.2012
 */
public class XSDValidation {

	@Test
	public void testDocumentValidity() throws Exception {
		
		Source document = new StreamSource(new File("c:\\data\\SEPA_RB32\\output\\sepa_121206_1056.xml"));
		Source schema = new StreamSource(new File("c:\\data\\SEPA_RB32\\ISO.pain.008.001.01.austrian.002a.xsd"));

// 		This is for print of the whole document		
//		Document doc = XMLTools.createW3cDocFromStream(new FileInputStream(new File("c:\\downloads\\sddcore.txt")));
//		System.out.println(XMLTools.createStringFromW3cDoc(doc, true));
		
		XMLValidationResult validationResult = XMLTools.validate(schema, document);
		if (!validationResult.isOK()) {
			fail(print(validationResult.getErrors()));
		}
	}
	
	private String print(List<String> messages) {
		String result = "";
		for (String message : messages) {
//			if (message.indexOf("BIC") < 0 && message.indexOf("IBAN") < 0) {
			System.out.println(message);
			result += " | " + message;
//			}
		}
		return result;
	}
}
