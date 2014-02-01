package at.workflow.webdesk.po;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.jdom.Document;

import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoFile;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          wdDEV<br>
 * created at:       11.05.2007<br>
 * package:          at.workflow.webdesk.po<br>
 * compilation unit: PoFileService.java<br><br>
 *
 *
 */
public interface PoFileService extends XmlFileSystemSaver {
	public static final int SAVE_FILE_INTERNALLY_ONLY = 2;
	public static final int SAVE_FILE_FILESYSTEM_ONLY = 1;
	public static final int SAVE_FILE_BOTH = 0;
	
	public static final String ACTION_CONFIG_XML_ROOT = "action-config";
	public static final String JOB_CONFIG_XML_ROOT = "job-config";
	public static final String CONNECTOR_CONFIG_XML_ROOT = "config";


	/**
     * Saves the given PoFile object to the database.
     * If newVersion=true, a copy of the current file will be
     * saved as *NEWEST* version, no matter if the current file
     * was the latest version or not.
     * 
     * If a new version is saved, validFrom and validTo are changed 
     * accordingly in the old an new version object. 
     * 
     * @param newVersion: should a new version be created?
	 * @param file PoFile object
	 */
	public void saveFile(PoFile file, boolean newVersion);
    
   
    /**
     * Saves the given PoFile object to the filesystem.
     * 
     * @param file
     */
    public void saveFileToFileSystem(PoFile file); 
    
    
    
    /**
     * Saves the given XML-Document to the database and to the filesystem.
     * 
     * 
     * @param doc a w3c Document
     * @param path the path under which the file should be saved.
     */
    public void saveXmlFile(Document doc, String path);
    
    
    /**
     * @param doc
     * @param path
     * @param whereToSave integer value, <br>
     *  0 .. saves the given jDom object to the filesystem and to the db <br>
     *  1 .. saves the given jDom object to the filesystem<br>
     *  2 .. saves the given jDom object to the db
     *  
     *  
     */
    public void saveXmlFile(Document doc, String path, int whereToSave);
    
    public void saveXmlFile(org.w3c.dom.Document doc, String path, int whereToSave);

    
    /**
     * @param doc
     * @param path
     * @param whereToSave integer value, <br>
     *  0 .. saves the given jDom object to the filesystem and to the db <br>
     *  1 .. saves the given jDom object to the filesystem<br>
     *  2 .. saves the given jDom object to the db
     * @param prettyFormat if true then the document is stored with pretty format otherwise
     * without formatting
     */
    public void saveXmlFile(Document doc, String path, int whereToSave, boolean prettyFormat);
    
    
	/**
	 * @param file
	 * 
	 * updates a given file in the database, as well as on the filesystem.
	 *  
	 */
	public void updateFile(PoFile file);
    

	/**
	 * @param file
	 *            deletes a file from database.
	 */
	public void deleteFile(PoFile file);

	/**
	 * @param uid
	 * @return the PoFile object with the given uid, null otherwise.
	 */
	public PoFile getFile(String uid);

	public String getContent(PoFile file);

	/**
	 * @param path
	 * @param file
	 */
	public void setContentOfFile(PoFile file) throws IOException;

	/**
	 * @param action
	 * @param type
	 *            determines which file is choosen. Have a look at PoConstants
	 *            to see the various
	 * @param date
	 * @return a PoFile object with the given action assigned. The type
	 *         determines which file is choosen. The file has to be valid at the
	 *         given date.
	 */
	public PoFile getFile(PoAction action, int type, Date date);

    
	/**
	 * @param configurable
	 * @return A <code>PoFile</code> object with the highest <code>versionNumber</code>. 
	 */
	public PoFile getFileOfConfigurable(Configurable configurable);
	
    
	/**
	 * @return a list of all files.
	 */
	public List<PoFile> loadAllFiles();
	
	/**
	 * @param fileName
	 * @return the type of the file e.g. template, definition, flowscript, ...
	 */
	public int getType(String fileName);

	/**
	 * @param relPath
	 * @return the fileId of the file with the given path
	 */
	public String getFileIdPerPath(String relPath);

    
	public PoFile getFilePerPath(String relPath);
	
	/**
     * @param uid the Uid of the PoFile object
     * @return the content of the file as a org.w3c.dom.Document if <br>
     * a PoFile object with the given id was found and if it is a valid xml document.
     */
    public org.w3c.dom.Document getFileAsXML(String uid); 
        
    
    /**
     * Takes an already dedicated file of the action, takes its attributes and
     * saves a new version of the file with the given document as the new content.
     * 
     * Throws a PoRuntimeException if no dedicated file exists.
     * 
     * @param action 
     * @param doc the new content of the file (XML - Format) 
     */
    public void updateFileOfAction(PoAction action, org.w3c.dom.Document doc);
    
    
    /**
     * 
     * @param doc (org.jdom.Document)
     * @param xpath (java.util.String)
     * @return a String Representation of the xpath Expression
     */
    public String getXmlNodeAsString(Document doc,String xpath);
    
    /**
     * 
     * @param doc (org.w3c.dom.Document)
     * @param xpath (java.util.String)
     * @return a String Representation of the xpath Expression
     */
    public String getXmlNodeAsString(org.w3c.dom.Document doc, String xpath);


    
	/**
	 * @param action
	 * @return a list of <code>PoFile</code> objects, ordered by their version number.
	 */
	public List<PoFile> findFilesOfActionOrderByVersion(PoAction action);
	
	
	/**
	 * @param fileId
	 * @return a <code>PoFile</code> object with the given <code>fileId</code> and 
	 * the highest version.
	 */
	public PoFile getFileWithHighestVersion(String fileId);


	/**
	 * @param string
	 * @return a <code>List</code> of <code>PoFile</code> objects 
	 * where the path contains <code>string</code>. Only <code>PoFile</code>
	 * objects with highest version are returned.
	 */
	public List<PoFile> findFileWherePathLikeAndMaxVersion(String string);


	/** ready Inputstream to byte[] without throwing checked
	 * exceptions, essentialy delegate to IOUtils.toByteArray(is)
	 * @param stream: Inputstream to read
	 * @return the byte-array
	 */
	public byte[] readFileFromDisk(InputStream stream);
	
	/**
	 * @return a <code>List</code> of [<code>String</code>] <code>PoFile</code> <code>UID</code>'s.
	 * 
	 */
	public List<String> loadAllFileIds();
	
	/**
	 * @param configurable The linked Object of the <code>PoFile</code>. Has to implement <code>Configurable</code>.
	 * @param doc a <code>org.w3c.dom.Document</code>
	 * @param path the <code>path</code> of the file 
	 */
	public void updateConfigurationFile(Configurable configurable, org.w3c.dom.Document doc, String path);
	
	/**
	 * @param configurable The linked Object of the <code>PoFile</code>. Has to implement <code>Configurable</code>.
	 */
	public void generateConfigFileOfConfigurable(Configurable configurable);
	
	
	/**
	 * @param file a <code>PoFile</code>
	 * @return an <code>org.w3c.dom.Document</code> object, if the content of the 
	 * given <code>file</code> is a valid XML file. 
	 */
	public org.w3c.dom.Document getFileAsXML(PoFile file);


	public PoFile getFileByIdAndVersion(String fileId, int versionNo);
	
}
