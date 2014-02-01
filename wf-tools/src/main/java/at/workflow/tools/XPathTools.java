package at.workflow.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Stateless utility methods in conjunction with w3c XML DOM and xpath expressions.
 */
public class XPathTools {

	/**
	 * Retrieves an XML node or attribute value as boolean.
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param xpath the xpath expression pointing to the target Node.
	 * @param def the default value to return when xpath did match nothing.
	 * @return true when found node value was "true" (or null was found and default was true), false in any other case.
	 */
	public boolean getXPathBooleanValue(Object nodeContext, String xpath, boolean def) {
		String ret = getXPathStringValue(nodeContext, xpath);
		if (ret == null)
			return def;
		if (ret.equals("true"))
			return true;
		return false;
	}
	
	/**
	 * Retrieves an XML node or attribute value as integer.
	 * Throws a <code>RuntimeException("Not a number: " + stringValue)</code> when there is an empty string.
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param xpath the xpath expression pointing to the target Node.
	 * @return the node value as as integer.
	 */
	public int getXPathIntValueNoEmptyStrings(Object nodeContext, String xpath) {
		int value = 0;
		String stringValue = null;
		try {
			stringValue = getXPathStringValue(nodeContext, xpath);			
			if ( stringValue != null ) 
				value = Integer.parseInt( stringValue );
		} catch (  NumberFormatException ne) {
			throw new RuntimeException("Not a number: " + stringValue);
		}
		return value;
	}
	
	/**
	 * Retrieves an XML node or attribute value as integer.
	 * Returns -1 when the node value is empty.
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param xpath the xpath expression pointing to the target Node.
	 * @return the node value as as integer.
	 */
	public int getXPathIntValue(Object nodeContext, String xpath) {
		String text = getXPathStringValue(nodeContext, xpath);
		if (text == null || text.length() <= 0)
			return -1;
		return Integer.valueOf(text);
	}
	
	/**
	 * Retrieves an XML node or attribute value as String.
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param xpath the xpath expression pointing to the target Node.
	 * @param defaultValue the default value to return when xpath did match nothing.
	 * @return the found node value, or default when nothing was found.
	 */
	public String getXPathStringValue(Object nodeContext, String xpath, String defaultValue) {
		Node node = getNodeWithXPath(nodeContext, xpath);
		if (node == null)
			return defaultValue;
		
		if (node instanceof Attr || node instanceof Text)
			return node.getNodeValue();
		
		return node.getTextContent();
	}
	
	/**
	 * Retrieves an XML node value as boolean.
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param xpath the xpath expression pointing to the target Node.
	 * @return true when found node value was "true" or "yes", false in any other case.
	 */
	public boolean getXPathBooleanYesValue(Object nodeContext, String xpath) {
		String ret = getXPathStringValue(nodeContext, xpath);
		if (ret != null && (ret.equals("true") || ret.equals("yes")))
			return true;
		return false;
	}
	
	/**
	 * Retrieves an XML node or attribute value as boolean.
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param xpath the xpath expression pointing to the target Node.
	 * @return true when found node value was non-null and "true", false in any other case.
	 */
	public boolean getXPathBooleanValue(Object nodeContext, String xpath) {
		String ret = getXPathStringValue(nodeContext, xpath);
		if (ret != null && ret.equals("true"))
			return true;
		return false;
	}
	
	/**
	 * Retrieves an XML node or attribute value as String.
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param xpath the xpath expression pointing to the target Node.
	 * @return the found node value, or null when nothing was found.
	 */
	public String getXPathStringValue(Object nodeContext, String xpath) {
		Node node = getNodeWithXPath(nodeContext, xpath);
		if (node == null)
			return null;
		
		if (node instanceof Attr || node instanceof Text)
			return node.getNodeValue();
		
		return node.getTextContent();
	}

	/**
	 * <b>This does NOT return the text content of the addressed node! Use getTextContent() instead!</b>
	 * Retrieves an XML Node String by <code>getNodeValue()</code>.
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param xpath the xpath expression pointing to the target Node.
	 * @return the node value of the found node, or null when nothing was found.
	 */
	public String getNodeValue(Object nodeContext, String xpath) {
		Object nodeObject = retrieveWithXPath(nodeContext, xpath, XPathConstants.NODE);
		if (nodeObject == null)
			return null;
		return ((Node) nodeObject).getNodeValue();
	}
	
	/**
	 * Retrieves an XML Node String <code>getTextContent()</code>.
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param xpath the xpath expression pointing to the target Node.
	 * @return the text content of the found node value, or null when nothing was found.
	 */
	public String getTextContent(Object nodeContext, String xpath) {
		if (nodeContext instanceof Node && xpath != null && xpath.equals("."))
			return ((Node) nodeContext).getTextContent();
		
		Object nodeObject = retrieveWithXPath(nodeContext, xpath, XPathConstants.NODE);
		if (nodeObject == null)
			return null;
		return ((Node) nodeObject).getTextContent();
	}
	
	/**
	 * Retrieve an attribute's value for a given node if it
	 * exists, or null if it does not exist 
	 * @param node
	 * @param attrName
	 * @return value of attribute if it exists or null otherwise
	 */
	public String getAttributeValue(Node node, String attrName) {
		
		if (node.getAttributes().getNamedItem(attrName)==null)
			return null;
		
		return node.getAttributes().getNamedItem(attrName).getTextContent();
	}
	
	/**
	 * Retrieves a Node.
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param xpath the xpath expression pointing to the target Node.
	 * @return the Node the xpath expression is pointing to in nodeContext.
	 */
	public Node getNodeWithXPath(Object nodeContext, String xpath) {
		return (Node) retrieveWithXPath(nodeContext, xpath, XPathConstants.NODE);
	}
	
	/**
	 * Retrieves a NodeList: when given "a/b/c", you get a list of "c" elements within "/a/b".
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param xpath the xpath expression pointing to (one of) the target Nodes (not the container node!).
	 * @return the NodeList the xpath expression is pointing to in nodeContext.
	 */
	public NodeList getNodesetWithXPath(Object nodeContext, String xpath) {
		return (NodeList) retrieveWithXPath(nodeContext, xpath, XPathConstants.NODESET);
	}
	
	/**
	 * Retrieves a Node or NodeList, according to argument "what".
	 * @param nodeContext the w3c XML Document, Node or NodeList that is the start point for the xpath search.
	 * @param path the xpath expression pointing to the target Node.
	 * @param what one of <code>XPathConstants.NODESET or XPathConstants.NODE</code>
	 * @return the Node or NodeList the xpath expression is pointing to in nodeContext.
	 */
	public Object retrieveWithXPath(Object nodeContext, String path, QName what) {
		XPathFactory factory = XPathFactory.newInstance();
		Object result = null;
		try {
			if (StringUtils.isNotEmpty(path)) {
				XPath xPath = factory.newXPath();
				XPathExpression expression = xPath.compile(path);
				result = expression.evaluate(nodeContext, what); 
			} else {
				result = nodeContext;
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException("Exception parsing document:", e);
		}
		return result;
	}
	
	/**
	 * Reads specified attribute values of all the nodes on path of detail row.
	 * @param parentNode		reference node the pathOfDetailRow applies to
	 * @param pathOfDetailRow	path to the nodes of interest
	 * @param attributeNames		names of attributes of interest, either listed or as String[]. OPTIONAL. If missing all attributes are read.
	 * @return					List containing one Map of name/value pairs for each attribute names specified. One map per node is generated.
	 * 
	 * Possible calls:
	 * List<Map<String, String>> attributeMaps = xPath.getListOfAttributeMaps(parentNode, "formatPatterns/pattern", new String[] {"columnId", "formatPattern"});
	 * List<Map<String, String>> attributeMaps = xPath.getListOfAttributeMaps(parentNode, "formatPatterns/pattern", "columnId", "formatPattern");
	 * List<Map<String, String>> attributeMaps = xPath.getListOfAttributeMaps(parentNode, "formatPatterns/pattern");
	 *
	 */
	public List<Map<String, String>> getListOfAttributeMaps(Node parentNode, String pathOfDetailRow, String ... attributeNames) {

		if (parentNode == null || pathOfDetailRow == null)
			return Collections.emptyList();
		
		NodeList nodes = getNodesetWithXPath(parentNode, pathOfDetailRow);
		int size = nodes.getLength();
		
		List<Map<String, String>> result = new ArrayList<Map<String,String>>(size);
		
		for (int idx = 0; idx < size; idx++) {
			Node oneRow = nodes.item(idx);
			Map<String, String> rowValues = getAttributesMap(oneRow, attributeNames);
			result.add(rowValues);
		}
		return result;
	}

	/**
	 * Reads specified or all attribute values of the node.
	 * @param node				reference node the pathOfDetailRow applies to
	 * @param attributeNames	names of attributes of interest, either listed or as String[]. OPTIONAL. If missing all attributes are read.
	 * @return					Map of name/value pairs for each attribute names specified.
	 * 
	 * Possible calls:
	 * Map<String, String> attributesMap = xPath.getAttributesMap(node, new String[] {"columnId", "formatPattern"});
	 * Map<String, String> attributesMap = xPath.getAttributesMap(node, "columnId", "formatPattern");
	 * Map<String, String> attributesMap = xPath.getAttributesMap(node);
	 *
	 */
	public Map<String, String> getAttributesMap(Node node, String... attributeNames) {
		
		Map<String, String> rowValues = new HashMap<String, String>();

		if (attributeNames != null && attributeNames.length > 0) {
		
			for (String attrName : attributeNames)
				rowValues.put(attrName, getAttributeValue(node, attrName));
			
		} else {
			
			NamedNodeMap attributeNodes = node.getAttributes();
			for (int i = 0; i < attributeNodes.getLength(); i++) {
				Node attrNode = attributeNodes.item(i);
				rowValues.put(attrNode.getNodeName(), attrNode.getNodeValue());
			}
		}
		return rowValues;
	}
}
