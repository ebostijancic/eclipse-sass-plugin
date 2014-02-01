package at.workflow.tools;

import java.io.IOException;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.JDomSerializer;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.jdom.Document;

public abstract class HtmlCleaningHelper {
	
	public static Document cleanAndCreateHtmlDoc(String htmlString) throws IOException {
		Clean clean = new Clean(htmlString);
		return new JDomSerializer(clean.properties, true).createJDom(clean.node);
	}
	
	public static String cleanHtmlString(String htmlString) throws IOException {
		Clean clean = new Clean(htmlString);
		return new PrettyXmlSerializer(clean.properties).getXmlAsString(clean.node);
		
	}
	
	private static class Clean
	{
		final TagNode node;
		final CleanerProperties properties;
		
		Clean(String argHtmlString) throws IOException	{
			String htmlString = argHtmlString;
			if (htmlString == null)
				htmlString = "";
			
			// create an instance of HtmlCleaner
			HtmlCleaner cleaner = createCleanerWithConfig();
			
			this.node = cleaner.clean(htmlString);
			
			// remove all class attributes
			removeClassAttribute(node);
			
			// remove margin style coming from msword
			removeWordMarginStyles(node);
			
			this.properties = cleaner.getProperties();
		}
	}
	
	private static void removeWordMarginStyles(TagNode node) {
		// remove margin style coming from msword
		TagNode[] matchingNodes = node.getElementsHavingAttribute("style", true);
		for (TagNode mn : matchingNodes) {
			
			String style = mn.getAttributeByName("style");
			String[] styles = style.split(";");
			String newStyle = "";
			for (String st : styles) {
				if (!st.trim().toLowerCase().startsWith("margin")) {
					if (!"".equals(newStyle)) {
						newStyle = newStyle + ";" + st;
					} else {
						newStyle = st;
					}
				}
			}
			mn.removeAttribute("style");
			if (!"".equals(newStyle)) 
				mn.addAttribute("style", newStyle);
		}
	}
	
	private static void removeClassAttribute(TagNode node) {
		// remove all class attributes
		TagNode[] matchingNodes = node.getElementsHavingAttribute("class", true);
		for (TagNode mn : matchingNodes) {
			mn.removeAttribute("class");
		}
	}
	
	private static HtmlCleaner createCleanerWithConfig() {
		// create an instance of HtmlCleaner
		HtmlCleaner cleaner = new HtmlCleaner();
		 
		// take default cleaner properties
		CleanerProperties props = cleaner.getProperties();
		 
		// customize cleaner's behaviour with property setters
		props.setAdvancedXmlEscape(true);
		props.setUseEmptyElementTags(false);
		props.setTranslateSpecialEntities(true);
		props.setRecognizeUnicodeChars(true);
		props.setOmitXmlDeclaration(true);
		
		return cleaner;
	}
}
