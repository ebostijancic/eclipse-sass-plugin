package at.workflow.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.jdom.transform.XSLTransformException;
import org.jdom.transform.XSLTransformer;
import org.jdom.xpath.XPath;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class provides various static helper-methods for easier handling of XMLDocuments.
 * 
 * @author Alexander Malic (amalic) 17.04.2007
 */
public class XMLTools {

	private static final String GET_TEXT_ONLY_XSL = "at/workflow/tools/resources/getTextOnly.xsl";

	private static Log logger = LogFactory.getLog(XMLTools.class);

	/** Constant for DOCTYPE_HTML */
	public static final String DOCTYPE_HTML = "html";

	/** Transports XML validation result as list of warnings and list of errors to the caller */
	public static final class XMLValidationResult {

		private List<String> warnings;
		private List<String> errors;

		public XMLValidationResult(List<String> warnings, List<String> errors) {
			super();
			this.warnings = warnings;
			this.errors = errors;
		}

		public boolean isOK() {
			return errors == null || errors.isEmpty();
		}

		public List<String> getWarnings() {
			return warnings;
		}

		public List<String> getErrors() {
			return errors;
		}
	}

	/** used for logging of errors by schema validator */
	private static final class XsdValidationLoggingErrorHandler implements ErrorHandler {

		private static final String MSG = "Exception during validation: ";
		private List<String> warnings = new ArrayList<String>();
		private List<String> errors = new ArrayList<String>();

		@Override
		public void warning(SAXParseException ex) throws SAXException {
			warnings.add(ex.getMessage());
			logger.warn(MSG + ex.getMessage(), ex);
		}

		@Override
		public void error(SAXParseException ex) throws SAXException {
			errors.add(ex.getMessage());
			logger.error(MSG + ex.getMessage(), ex);
		}

		@Override
		public void fatalError(SAXParseException ex) throws SAXException {
			errors.add(ex.getMessage());
			logger.fatal(MSG + ex.getMessage(), ex);
		}

		public XMLValidationResult getValidationResult() {
			return new XMLValidationResult(warnings, errors);
		}
	}

	/** returns a jdom-Document with removed Namespaces */
	public static org.jdom.Document removeNamespaces(org.jdom.Document jdomDoc)
			throws TransformerFactoryConfigurationError, IOException,
			TransformerException {
		Resource res = new ClassPathResource(
				"at/workflow/tools/resources/RemoveNamespaces.xsl");
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer(new StreamSource(res.getInputStream()));
		JDOMSource source = new JDOMSource(jdomDoc);
		JDOMResult result = new JDOMResult();
		transformer.transform(source, result);
		return result.getDocument();
	}

	public static org.jdom.Document removeNamespaces(org.jdom.Document jdomDoc, String nameSpaceUri)
			throws TransformerFactoryConfigurationError, IOException, TransformerException {

		Resource res = new ClassPathResource(
				"at/workflow/tools/resources/RemoveNamespaces.xsl");
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer(new StreamSource(res.getInputStream()));
		JDOMSource source = new JDOMSource(jdomDoc);
		JDOMResult result = new JDOMResult();
		transformer.setParameter("nsToRemove", nameSpaceUri);
		transformer.transform(source, result);
		return result.getDocument();
	}

	/**
	 * removes nodes out of a org.w3c.dom.Document object
	 * tries to located the elements and simply detaches them
	 * then returns the resulting document
	 */
	@SuppressWarnings("unchecked")
	public static org.w3c.dom.Document removeNodes(org.w3c.dom.Document doc, String xpath) throws JDOMException {
		Document jdomDoc = convertToJdomDoc(doc);
		List<Element> elems = XPath.selectNodes(jdomDoc, xpath);
		Iterator<Element> elemItr = elems.iterator();
		while (elemItr.hasNext()) {
			Element elem = elemItr.next();
			elem.detach();
		}
		return convertToW3cDoc(jdomDoc);
	}

	/** returns a w3c-Document with removed Namespaces */
	public static org.w3c.dom.Document removeNamespaces(org.w3c.dom.Document w3cDoc) {
		
		Node node = w3cDoc.getFirstChild();
	    renameNamespaceRecursive(node, null);
	    
	    return w3cDoc;
		//return (convertToW3cDoc(removeNamespaces(convertToJdomDoc(w3cDoc))));
	}
	
	public static void renameNamespaceRecursive(Node node, String namespace) {
		org.w3c.dom.Document document = node.getOwnerDocument();
	    if (node.getNodeType() == Node.ELEMENT_NODE) {
	        document.renameNode(node, namespace, node.getNodeName());
	    }
	    NodeList list = node.getChildNodes();
	    for (int i = 0; i < list.getLength(); ++i) {
	        renameNamespaceRecursive(list.item(i), namespace);
	    }
	}

	/** converts a jdom-Document to a w3c-Document */
	public static org.w3c.dom.Document convertToW3cDoc(org.jdom.Document jdomDoc) {
		DOMOutputter outputter = new DOMOutputter();
		try {
			return outputter.output(jdomDoc);
		}
		catch (JDOMException e) {
			throw new RuntimeException(e);
		}
	}

	/** converts a w3c-Document to a jdom-Document */
	public static org.jdom.Document convertToJdomDoc(org.w3c.dom.Document w3cDoc) {
		DOMBuilder builder = new DOMBuilder();
		return builder.build(w3cDoc);
	}

	/**
	 * Creates a (Html-)Dom-Document from the given String <code>s</code>.
	 * @param setDocType (if true, then the DocType is set to HTML)
	 * @return a Dom-Document or null if something unexpected happens
	 */
	public static org.w3c.dom.Document createW3cDocFromString(String s, String doctype) {

		DocType docType = null;
		if (doctype != null) {
			if (doctype.equals(DOCTYPE_HTML))
				docType = new DocType("html"
						, "-//W3C//DTD XHTML 1.0 Transitional//EN"
						, "http://www.w3.org/TR/html4/loose.dtd");
		}

		Reader stringReader = new StringReader(s);
		try {
			Document doc = getSaxBuilder().build(stringReader);
			if (docType != null)
				doc.setDocType(docType);
			return convertToW3cDoc(doc);

		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static org.w3c.dom.Document createW3cDocFromFile(File file) {
		try {
			org.jdom.Document doc = getSaxBuilder().build(file);
			return convertToW3cDoc(doc);
		}
		catch (JDOMException e) {
			throw new RuntimeException("JDOMException from building W3C XML document from "+file, e);
		}
		catch (IOException e) {
			throw new RuntimeException("IOException from building W3C XML document from "+file, e);
		}
	}

	public static SAXBuilder getNonValidatingWhitespaceIgnoringSaxBuilder() {
		return getSaxBuilder();
	}
	
	private static SAXBuilder getSaxBuilder() {
		SAXBuilder builder;
		builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		return builder;
	}

	/** Creates org.w3c.dom.Document directly from input stream, without intermediate JDom document. */
	public static org.w3c.dom.Document createW3cDocFromStream(InputStream inputStream) {

		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			org.w3c.dom.Document document = docBuilder.parse(inputStream);
			inputStream.close();
			document.getDocumentElement().normalize();
			return document;
		}
		catch (Exception e) {
			throw new RuntimeException("Problems building W3C Doc from Stream directly: " + e, e);
		}
	}

	/** returns an empty org.w3c.dom.Document */
	public static org.w3c.dom.Document createEmptyW3cDoc() throws ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
		return docBuilder.newDocument();
	}

	/** converts a String to a org.w3c.dom.Document */
	public static org.w3c.dom.Document fromString(String s) throws JDOMException, IOException {
		return createW3cDocFromString(s);
	}

	public static org.w3c.dom.Document createW3cDocFromString(String s) throws JDOMException, IOException {
		SAXBuilder saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
		Reader stringReader = new StringReader(s);
		Document doc = saxBuilder.build(stringReader);
		return convertToW3cDoc(doc);
	}

	/** converts a org.w3c.dom.Document to a String */
	public static String toString(org.w3c.dom.Document doc) throws JDOMException, IOException {
		return createStringFromW3cDoc(doc);
	}

	public static String createStringFromW3cDoc(org.w3c.dom.Document doc) {
		return createStringFromW3cDoc(doc, false);
	}

	/** converts a org.w3c.dom.Document to a String, conditionally with 'pretty print' formatter  */
	public static String createStringFromW3cDoc(org.w3c.dom.Document doc, boolean prettyPrint) {
		XMLOutputter xmlOutputter = new XMLOutputter();
		if (prettyPrint)
			xmlOutputter = new XMLOutputter(Format.getPrettyFormat());

		return xmlOutputter.outputString(convertToJdomDoc(doc));
	}

	/** converts an org.w3c.dom.Document into an InputStream */
	public static InputStream createInputStreamFromW3cDoc(org.w3c.dom.Document document) {

		String string = createStringFromW3cDoc(document);
		if (string == null)
			return null;
		return new ByteArrayInputStream(string.getBytes(Charset.forName("utf-8")));
	}
	
	/** extracts Text of all nodes and returns it in one string. Ignores all tags and attributes (names and values)*/
	public static String getTextOnly(org.w3c.dom.Document w3cDoc) {
		
		DOMSource domSource=new DOMSource(w3cDoc);
		DOMResult domResult=new DOMResult();
		TransformerFactory factory = TransformerFactory.newInstance();
		ClassPathResource res = new ClassPathResource( GET_TEXT_ONLY_XSL );
	    Source xslt;
	    
		try {
			xslt = new StreamSource( res.getInputStream() );
			Transformer transf = factory.newTransformer(xslt);
			transf.transform(domSource, domResult);
		} catch (IOException e) {
			throw new RuntimeException("Problems extracting Text content from XML document..", e);
		} catch (TransformerException e) {
			throw new RuntimeException("Problems extracting Text content from XML document..", e);
		}
		return domResult.getNode().getFirstChild().getTextContent().trim();
		
	}

	public static String getElementTreeAsString(Element element) {
		StringBuffer res = new StringBuffer();
		getElementAsStringRec(element, res);
		return res.toString();
	}

	private static void getElementAsStringRec(Element e, StringBuffer sb) {
		sb.append("<" + e.getName() + ">");
		sb.append(e.getText());

		@SuppressWarnings("unchecked")
		Iterator<Element> i = e.getChildren().iterator();
		while (i.hasNext()) {
			Element subElement = i.next();
			getElementAsStringRec(subElement, sb);
		}
		sb.append("</" + e.getName() + ">");
	}

	/**
	 * @param children a <code>List</code> of <code>org.jdom.Element</code> objects.
	 * @return a <code>String</code> that represents the xml-tree of the <code>children</code> elements.
	 */
	public static String getElementTreeAsString(List<Element> children) {
		StringBuffer sb = new StringBuffer();
		Iterator<Element> i = children.iterator();
		while (i.hasNext()) {
			getElementAsStringRec(i.next(), sb);
		}
		return sb.toString();
	}

	/**
	 * check if supplied mailBody is emtpy
	 * @param mailBody: org.w3c.dom.Document of HTMLbody
	 */
	public static boolean isEmptyHtmlMailBody(org.w3c.dom.Document mailBody) {
		return isEmptyXmlDoc(mailBody);
	}

	public static boolean isEmptyXmlDoc(String s) {
		try {
			return isEmptyXmlDoc(createW3cDocFromString(s));
		}
		catch (Exception e) {
			return true;
		}
	}

	public static boolean isEmptyXmlDoc(org.w3c.dom.Document argDoc) {
		if (argDoc == null)
			return true;

		try {
			org.w3c.dom.Document cleanDoc = XMLTools.removeNamespaces(argDoc);
			if (cleanDoc == null)
				return true;

			String xmlAsString = getTextOnly( cleanDoc );
			if (xmlAsString == null || xmlAsString.equals(""))
				return true;
			return false;
		}
		catch (Exception e) {
			logger.warn("Problems checking if XML Document (" + argDoc +") is empty..", e);
		}

		return true;
	}

	/**
	 * returns HelpMessage as w3c DOM Object
	 * returns the blank body content of a valid HTML document
	 * surrounded by a DIV tag.
	 * @return org.w3c.dom.Document xHTML Snippet
	 */
	public static org.w3c.dom.Document getTextAsHtmlDocument(String text) {
		String workingHtmlText = new String(text);
		// We have to remove the html, head, and body nodes, otherwise js errors are thrown.
		try {
			if (workingHtmlText.indexOf("<body>") > 0) {
				workingHtmlText = workingHtmlText.substring(workingHtmlText.indexOf("<body"), workingHtmlText.length());
				workingHtmlText = workingHtmlText.substring(workingHtmlText.indexOf(">") + 1, workingHtmlText.length());
				workingHtmlText = workingHtmlText.substring(0, workingHtmlText.indexOf("</body>"));
			}
		}
		catch (Exception e) {
			logger.debug(e, e);
			workingHtmlText = "An Error occured while trying to split up document:" + new String(text);
		}
		workingHtmlText = "<div>" + workingHtmlText + "</div>";
		if (text != null)
			return XMLTools.createW3cDocFromString(workingHtmlText, "html");
		return null;
	}

	public static boolean getXPathBooleanValue(org.w3c.dom.Document argDoc, String xpath) {
		Document jdomDoc = convertToJdomDoc(argDoc);
		return getXPathBooleanValue(jdomDoc, xpath);
	}

	public static boolean getXPathBooleanValue(Document jdomDoc, String xpath) {
		String ret = getXPathStringValue(jdomDoc, xpath);

		if (ret != null && ret.equals("true"))
			return true;

		return false;
	}

	public static String getXPathStringValue(org.w3c.dom.Document argDoc, String xpath) {
		Document jdomDoc = convertToJdomDoc(argDoc);
		return getXPathStringValue(jdomDoc, xpath);
	}

	public static String getXPathStringValue(Document jdomDoc, String xpath) {
		Object obj;
		try {
			obj = XPath.selectSingleNode(jdomDoc, xpath);
		}
		catch (JDOMException e) {
			e.printStackTrace();
			return null;
		}
		if (obj == null)
			return null;

		if (obj instanceof org.jdom.Attribute)
			return ((org.jdom.Attribute) obj).getValue();

		if (obj instanceof org.jdom.Element)
			return ((org.jdom.Element) obj).getTextTrim();

		return obj.toString();
	}

	/**
	 * Gets an attribute from a document and returns an integer
	 */
	public static int getXPathIntValue(org.w3c.dom.Document argDoc, String xpath) {
		Document jdomDoc = convertToJdomDoc(argDoc);
		return getXPathIntValue(jdomDoc, xpath);
	}

	/**
	 * Gets an attribute from a document and returns an integer
	 */
	public static int getXPathIntValue(org.jdom.Document jdomDoc, String xpath) {
		int value = 0;
		try {
			String stringValue = getXPathStringValue(jdomDoc, xpath);
			if (stringValue != null)
				value = Integer.parseInt(stringValue);
		}
		catch (NumberFormatException ne) {
			value = 0;
		}
		return value;
	}

	/**
	 * Creates Source from Document
	 */
	public static Source createSourceFromW3cDoc(org.w3c.dom.Document document) {

		String string = createStringFromW3cDoc(document);
		if (string == null)
			return null;
		return new StreamSource(new ByteArrayInputStream(string.getBytes(Charset.forName("utf-8"))));
	}

	/**
	 * Validates document against schema
	 * Use new StreamSource(new File(filePath)) when reading file 
	 * Use createSourceFromW3cDoc(document) when processing w3c Document
	 * @return XML validation result containing list of warnings and list of errors  
	 * @throws runtime exception when other problems occurred
	 */
	public static XMLValidationResult validate(Source schemaSource, Source documentSource) {

		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = schemaFactory.newSchema(schemaSource);
			Validator validator = schema.newValidator();
			XsdValidationLoggingErrorHandler errorHandler = new XsdValidationLoggingErrorHandler();
			validator.setErrorHandler(errorHandler);
			validator.validate(documentSource);
			return errorHandler.getValidationResult();
		}
		catch (Exception e) {
			final String msg = "Exception while validating document with schema: ";
			logger.error(msg + e, e);
			throw new RuntimeException(msg + e, e);
		}
	}
}
