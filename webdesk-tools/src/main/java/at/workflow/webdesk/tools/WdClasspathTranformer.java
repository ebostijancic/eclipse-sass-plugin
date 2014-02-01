package at.workflow.webdesk.tools;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Alexander Malic <br>
 *   	   logged in as: amalic<br><br>
 * 
 * Project:          webdesk3.1dev<br>
 * created at:       20.02.2007<br>
 * package:          at.workflow.webdesk.tools<br>
 * compilation unit: WdClasspathTranformer.java<br><br>
 * 
 * This Class is used for xslt-tranformation of a xml-file using a xsl-file.
 * The files are located with their ClassPath or absolute Path
 */
public class WdClasspathTranformer {
	
	/**
	 * @return returns the tranformed XmlFile as Jdom-Document
	 */
	public static org.jdom.Document transformToJdom(String inputXmlClassPathOrAbsolutePath,String xslClassPathOrAbsolutePath){
		try {
			Resource inputXmlRes=getResource(inputXmlClassPathOrAbsolutePath);
			org.jdom.Document xmlDoc=new SAXBuilder().build(inputXmlRes.getInputStream());
			return transformToJdom(xmlDoc, xslClassPathOrAbsolutePath);
		} catch(Exception e) {
			throw new RuntimeException("something weired happened while trying to tranform to JDom-Document",e);
		}
	}
	
	/**
	 * @return returns the tranformed XmlFile as Jdom-Document
	 */
	public static org.jdom.Document transformToJdom(org.jdom.Document xmlDoc,String xslClassPathOrAbsolutePath){
		try {
			Resource xslRes=getResource(xslClassPathOrAbsolutePath);

			// needs to be done via JAXP so the xsl-file can import other xsl's located inside or under the same Directory
			StreamSource ss=new StreamSource(xslRes.getInputStream(),getRelPath(xslRes));
			Transformer transformer = TransformerFactory.newInstance().newTransformer(ss);
	        JDOMResult out = new JDOMResult();
	        transformer.transform(new JDOMSource(xmlDoc), out);
	        
			return out.getDocument();
		} catch(Exception e) {
			throw new RuntimeException("something weired happened while trying to tranform to JDom-Document",e);
		}
	}
	
	/**
	 * @return returns the tranformed XmlFile as w3c-Document
	 */
	public static org.w3c.dom.Document transformToW3c(String inputXmlClassPathOrAbsolutePath,String xslClassPathOrAbsolutePath){
		try {
			Resource inputXmlRes=getResource(inputXmlClassPathOrAbsolutePath);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document xmlDoc=builder.parse(inputXmlRes.getInputStream());
			return transformToW3c(xmlDoc, xslClassPathOrAbsolutePath);
		} catch(Exception e) {
			throw new RuntimeException("something weired happened while trying to tranform to JDom-Document",e);
		}
	}
	
	/**
	 * @return returns the tranformed XmlFile as w3c-Document
	 */
	public static org.w3c.dom.Document transformToW3c(org.w3c.dom.Document xmlDoc, String xslClassPathOrAbsolutePath){
		try {
			Resource xslRes=getResource(xslClassPathOrAbsolutePath);
			
			// needs to be done via JAXP so the xsl-file can import other xsl's located inside or under the same Directory
			StreamSource ss=new StreamSource(xslRes.getInputStream(), getRelPath(xslRes));
			Transformer transformer = TransformerFactory.newInstance().newTransformer(ss);
            
			// transform the xmlDoc
			DOMSource domSource=new DOMSource(xmlDoc);
			DOMResult domResult=new DOMResult();
	        transformer.transform(domSource, domResult);
	        
	        // return the Document
	        return (org.w3c.dom.Document)domResult.getNode();
		} catch(Exception e) {
			throw new RuntimeException("something weired happened while trying to tranform to JDom-Document",e);
		}
	}
	
	
	private static Resource getResource(String classPathOrAbsolutePath) throws IOException {
		Resource res=new ClassPathResource(classPathOrAbsolutePath);
		if(!res.exists()) {
			res=new FileSystemResource(classPathOrAbsolutePath);
			if(!res.exists())
				throw new IOException("unable to locate File at Path \"" + classPathOrAbsolutePath + "\"");
		}
		return res;
	}
	
	private static String getRelPath(Resource res) throws IOException {
		String relPath=res.getFile().getPath().replaceAll("\\\\", "/");
		return relPath.substring(0,relPath.lastIndexOf("/")+1);
	}
}
