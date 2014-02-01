package at.workflow.webdesk.po;

import org.w3c.dom.Document;

public interface XmlFileSystemSaver {
	
	public void saveXmlToFileSystem(Document doc, String filePath);
	
	public void saveXmlToFileSystem(org.jdom.Document doc, String filePath);
}
