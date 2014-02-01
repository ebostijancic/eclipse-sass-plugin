package at.workflow.tools.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.tools.XMLTools;
import at.workflow.tools.XPathTools;

public class WTestXMLTools extends TestCase {

	private String conversionTestFileName = "conversion-test.rptdesign";
	
	public void testGetElementTreeAsString() {
		
		Element testTree = getTestTree();
		
		String result = XMLTools.getElementTreeAsString(testTree);
		
		System.out.println(result);
		
		assertEquals("<root><table><tr><td>test</td></tr></table></root>", result);
		
	}
	
	public void testRemoveNamesspaces () {
		
		Resource res = new ClassPathResource("MixedNamespacesContent.xml");
		SAXBuilder saxBuilder = new SAXBuilder();
		org.jdom.Document jdomDoc = null;
		org.jdom.Document jdomTarget = null;
		try {
			jdomDoc = saxBuilder.build(res.getInputStream());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		try {
			jdomTarget = XMLTools.removeNamespaces(jdomDoc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		String xmlStr = XMLTools.createStringFromW3cDoc(XMLTools.convertToW3cDoc(jdomTarget));
		System.out.println("result:\n");
		System.out.println(xmlStr);
		assertTrue(xmlStr.indexOf("xpdl")==-1);
		assertTrue(xmlStr.indexOf("i18n")==-1);
		
		try {
			jdomTarget = XMLTools.removeNamespaces(jdomDoc, "http://www.wfmc.org/2002/XPDL1.0");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		xmlStr = XMLTools.createStringFromW3cDoc(XMLTools.convertToW3cDoc(jdomTarget));
		System.out.println("result:\n");
		System.out.println(xmlStr);
		assertTrue(xmlStr.indexOf("xpdl")==-1);
		assertTrue(xmlStr.indexOf("i18n")>0);
		
		
		
	}
	
	public void testGetTextOnly () throws IOException {
		
		Resource res = new ClassPathResource("MixedNamespacesContent.xml");
		org.w3c.dom.Document doc = XMLTools.createW3cDocFromStream(res.getInputStream());
		
		String result1 = XMLTools.getTextOnly(doc);
		
		System.out.println("Result1:" + result1);
	}
	
	
	public void testGetHelpElementTreeAsString () {
		
		Element testHelp = getTestHelpElement();
		
		String result = XMLTools.getElementTreeAsString(testHelp);
		
		System.out.println(result);
		
		assertEquals("<helpMessage><en><p>test English</p></en><de><p>test Deutsch</p></de></helpMessage>", result);
		
	}
	
	private Element getTestHelpElement() {
	
		Document document = new Document();
		Element root = new Element("helpMessage");
		document.addContent(root);
		Element en = new Element("en");
		root.addContent(en);
		Element enP = new Element("p");
		enP.addContent("test English");
		en.addContent(enP);
		Element de = new Element("de");
		root.addContent(de);
		Element deP = new Element("p");
		deP.addContent("test Deutsch");
		de.addContent(deP);

		return root;
	}
	
	private Element getTestTree() {
		
		Document document = new Document();
		Element root = new Element("root");
		document.addContent(root);
		Element table = new Element("table");
		root.addContent(table);
		Element row = new Element("tr");
		table.addContent(row);
		Element data = new Element("td");
		data.addContent("test");
		row.addContent(data);
		
		return root;
	}

	public void testConversionToDocumentWithNull() throws Exception {
		
		try {
			XMLTools.createW3cDocFromStream(null);
			fail("Accepted null input stream");
		} catch (Exception e) { }
	}		

	public void testConversionInputStreamToDocument() throws Exception {
	    
		InputStream inputStream = new ClassPathResource(conversionTestFileName).getInputStream();
        XPathTools xPath = new XPathTools();
		
        org.w3c.dom.Document doc = XMLTools.createW3cDocFromStream(inputStream);
        assertNotNull(doc);
        
        assertNotNull(xPath.getNodeWithXPath(doc, "/report"));
        assertNotNull(xPath.getTextContent(doc, "/report/property[@name='createdBy']"));
        assertEquals(2, xPath.getNodesetWithXPath(doc, "/report/parameters/scalar-parameter").getLength());
        assertEquals(2, xPath.getNodesetWithXPath(doc, "/report/styles/style").getLength());
        
        assertNotNull(xPath.getNodeWithXPath(doc, "/report/page-setup/simple-master-page"));
        assertNotNull(xPath.getNodeWithXPath(doc, "/report/body/text-data"));
        
	}
	
	public void testConversionToInputStreamWithNull() throws Exception {
		
		try {
			XMLTools.createInputStreamFromW3cDoc(null);
		} catch (Exception e) {}
	}		
			
	public void testConversionW3cDocumentToInputStream() throws Exception {
		
        InputStream inputStream = new ClassPathResource(conversionTestFileName).getInputStream();
        
        org.w3c.dom.Document doc = XMLTools.createW3cDocFromStream(inputStream);
        assertNotNull(doc);
		
        // we will take only part of the document
        InputStream convertedStream = XMLTools.createInputStreamFromW3cDoc(doc);
        
        assertNotNull(convertedStream);
        assertEquals(3844, convertedStream.available());
        StringWriter writer = new StringWriter();
        IOUtils.copy(convertedStream, writer);//, encoding);
        String theString = writer.toString();
        assertNotNull(theString);
        assertTrue(!"".equals(theString.trim()));

        assertTrue(theString.contains("report"));
        assertTrue(theString.contains("property"));
        assertTrue(theString.contains("\"createdBy\""));
        assertTrue(theString.contains("parameters"));
        assertTrue(theString.contains("scalar-parameter"));
        assertTrue(theString.contains("styles"));
        assertTrue(theString.contains("style"));
        assertTrue(theString.contains("page-setup"));
        assertTrue(theString.contains("body"));
        assertTrue(theString.contains("text-data"));
        
        assertTrue(theString.contains("new Date()"));
        assertTrue(theString.contains("This is test report with two parameters:"));
	}
}
