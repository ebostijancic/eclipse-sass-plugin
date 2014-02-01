package at.workflow.webdesk.po.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.springframework.core.io.ClassPathResource;

import at.workflow.tools.XMLTools;
import at.workflow.webdesk.po.Configurable;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoFileDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.tools.date.DateTools;

public class PoFileServiceImpl implements PoFileService{

	private Logger logger = Logger.getLogger(PoFileServiceImpl.class);


	/** reference to file DAO */
	private PoFileDAO fileDAO;

	/**
	 * @deprecated
	 */
	private String getRealPath() {
		return null;
	}

	public void saveFileToFileSystem(PoFile file) {

		if (file.getPath() != null && !file.getPath().equals("")) {
			FileOutputStream out; // declare a file output object
			PrintStream p; // declare a print stream object
			try {
				String path = "";
				// Create a new file output stream
				if (getRealPath() != null && !getRealPath().equals(""))
					path = getRealPath();
				if (file.getPath().indexOf(path) != 0) {
					createFile(path + file.getPath());
					out = new FileOutputStream(path + file.getPath());
					logger.info("Saved " + path + file.getPath()
							+ " to File System.");
				} else {
					createFile(file.getPath());
					out = new FileOutputStream(file.getPath());
					logger.info("Saved " + file.getPath() + " to File System.");
				}
				// Connect print stream to the output stream
				p = new PrintStream(out);
				out.write(file.getContent());
				p.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean createFile(String path) {
		String finalPath = path;
		finalPath = finalPath.replaceAll("\\\\", "/");
		finalPath = finalPath.substring(0, finalPath.lastIndexOf("/"));
		File f = new File(finalPath);
		f.mkdirs();
		f = new File(path);
		try {
			return f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void saveFile(PoFile file, boolean createNewVersion) {
		if (file.getValidfrom() == null)
			file.setValidfrom(new Date());
		if (file.getValidto() == null)
			file.setValidto(PoConstants.getInfDate());

		if (file.getTimeStamp() == null)
			file.setTimeStamp(new Date());
		
		if ((file.getUID() == null || file.getUID().equals(""))
				&& (file.getFileId() == null || file.getFileId().equals(""))) {
			// the first version of the file
			fileDAO.save(file);
			file.setValidfrom(new Date());
			file.setValidto(PoConstants.getInfDate());
			file.setVersionNumber(1);
			file.setFileId(file.getUID());
			fileDAO.save(file);
		} else {
			if (createNewVersion) {
				
				// check if we have to clone!
				// but before we are calling the DAO method getFileWithHighestVersion
				// which would persist a transient object 'file'
				boolean cloneNeeded = (file.getUID()!=null);
				
				// get last version & set new validTo
				PoFile lastFileVersion = fileDAO.getFileWithHighestVersion(file.getFileId());  // this call persists file
				lastFileVersion.setValidto(new Date());
				fileDAO.save(lastFileVersion);
				
				// now save the file as new version
				PoFile newFile = file;
				
				if (cloneNeeded) {
					// be sure we have a clone
					newFile = file.clone();
					newFile.setUID(null);
				}
				
				newFile.setValidfrom(new Date());
				newFile.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
				newFile.setTimeStamp(new Date()); // this is important
				newFile.setVersionNumber(lastFileVersion.getVersionNumber()+1);
				fileDAO.save(newFile);
			} else {
				fileDAO.save(file);
			}
			
		}
	}
	
	public PoFile getFileWithHighestVersion(String fileId) {
		return fileDAO.getFileWithHighestVersion(fileId);
	}
	
	public void updateFile(PoFile file) {
		fileDAO.save(file);
	}

	public void deleteFile(PoFile file) {
		fileDAO.delete(file);
	}

	public String getContent(PoFile file) {
		return new String(file.getContent(), Charset.forName("utf-8"));
	}

	public PoFile getFile(String uid) {
		return fileDAO.get(uid);
	}
	
	public PoFile getFile(PoAction action, int type, Date date) {
		return fileDAO.getFile(action, type, date);
	}

	public List<PoFile> loadAllFiles() {
		return fileDAO.loadAll();
	}

	public String getFileIdPerPath(String relPath) {
		return fileDAO.getFileIdPerPath(relPath);
	}

	public String getXmlNodeAsString(Document doc, String xpath) {
		if (doc == null)
			return null;
		String ret = null;
		XMLOutputter outputter = new XMLOutputter();
		try {
			Element myElement = (Element) XPath.selectSingleNode(doc, xpath);
			StringWriter sw = new StringWriter();
			Document tempDoc = new Document();
			if (myElement != null) {
				tempDoc.setRootElement((Element) myElement.clone());
				outputter.output(tempDoc, sw);
				ret = sw.toString();
			} else
				return null;
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			return null;
		}
		return ret;

	}

	public String getXmlNodeAsString(org.w3c.dom.Document doc, String xpath) {
		DOMBuilder builder = new DOMBuilder();
		Document myDoc = builder.build(doc);
		return getXmlNodeAsString(myDoc, xpath);
	}


	public PoFile getFileOfConfigurable(Configurable configurable) {
		PoFile file = fileDAO.getFileOfConfigurable(configurable);
		if (logger.isDebugEnabled())
			logger.debug("returning file with versionNumber: " + file.getVersionNumber());
		return file;
	}
		

	private Document generateConfigXml(Configurable configurable) {
		
		Document doc = new Document();
		Element root = new Element( getRootNode(configurable) );
		doc.setRootElement(root);
		
		// Layout and Formats node are
		// only needed for action-config
		if (configurable instanceof PoAction) {
			
			// this might be necessary for Jobs and Connectors also
			// if we decide to make jobs/connectors configs more reuseable accross systems.
			PoAction action = (PoAction)configurable; 
			if (action.getParent() == null)
				throw new IllegalStateException("The passed action has no parent. Therefor we can not generate a Config-XML!");
			
			String actionName = action.getParent().getName(); 
			root.addContent( generateParentReferenceElement(actionName, root) );
	
			// layout tag
			Element layout = new Element("layout");
			root.addContent(layout);
	
			generateFormatElements(layout);
		
		}

		return doc;
	}

	private Element generateParentReferenceElement(String actionName, Element root) {
		Element sitemap = new Element("sitemap");
		Element actionElement = new Element("action");
		sitemap.addContent(actionElement);
		actionElement.setText(actionName);
		return sitemap;
	}

	private void generateFormatElements(Element layout) {
		Element formats = new Element("formats");
		layout.addContent(formats);

		Element print = new Element("format");
		print.setAttribute("id", "print");
		print.setAttribute("active", "true");

		Element pdf = new Element("format");
		pdf.setAttribute("id", "pdf");

		Element xls = new Element("format");
		xls.setAttribute("id", "xls");

		formats.addContent(print);
		formats.addContent(pdf);
		formats.addContent(xls);
	}



	public byte[] readFileFromDisk(InputStream is) {
		try {
			return IOUtils.toByteArray(is);
		} catch (IOException e1) {
			throw new PoRuntimeException(e1);
		}

	}

	public void updateFileOfAction(PoAction action, org.w3c.dom.Document doc) {
		// maybe a refresh necessary
		if (action.getFiles().size() > 0) {
			// it is assumed that the passed action is a config and its
			// dedicated files
			// are action config files. The code have to be changed if another
			// kind
			// of file will be assigned to an action
			PoFile file = (PoFile) action.getFiles().toArray()[0];
			fileDAO.evict(file);
			file.setUID(null);
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			file.setTimeStamp(new Date());
			try {
				file.setContent(outputter.outputString(XMLTools.convertToJdomDoc(doc)).getBytes(
						"UTF8"));
			} catch (Exception e) {
				throw new PoRuntimeException(e);
			}
			saveFile(file, true);
		} else
			throw new PoRuntimeException(
					PoRuntimeException.ERROR_ACTION_HAS_NO_FILES);
	}

	public void setContentOfFile(PoFile file) throws IOException {
		if (file.getUID() == null) {
			if (file.getPath().indexOf("classpath") > -1) {
				ClassPathResource r = new ClassPathResource(file.getPath()
						.substring(file.getPath().indexOf("classpath") + 10));
				file.setContent(readFileFromDisk(r.getInputStream()));
			} else {
				String path = getRealPath() + file.getPath();
				file.setContent(this
						.readFileFromDisk(new FileInputStream(path)));
			}
		}
	}

	public int getType(String fileName) {
		if (fileName.endsWith("_d.xml"))
			return PoConstants.FILE_DEFINITION;
		if (fileName.endsWith("_t.xml"))
			return PoConstants.FILE_TEMPLATE;
		if (fileName.endsWith(".js"))
			return PoConstants.FILE_FLOWSCRIPT;
		if (fileName.endsWith(".xml"))
			return PoConstants.FILE_XML;
		// if (fileName.endsWith("_d.xml")) return PoConstants.FILE_DEFINITION;
		return -1; // undefined
	}

	public void saveXmlFile(Document doc, String path, int whereToSave) {
		saveXmlFile(doc, path, whereToSave, true);
	}

	public org.w3c.dom.Document getFileAsXML(String uid)  {
        PoFile file = getFile(uid);
        file = fileDAO.getFileWithHighestVersion(file.getFileId()); // get the last version
        org.w3c.dom.Document document;
        try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        document = builder.parse( new ByteArrayInputStream(file.getContent()));
        } catch (Exception e) {
        	throw new PoRuntimeException(e);
        }
        return document;
    }
    

	public org.w3c.dom.Document getFileAsXML(PoFile file) {
		org.w3c.dom.Document document;
        try {
        	
        	 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	 DocumentBuilder builder = factory.newDocumentBuilder();
        	 document = builder.parse( new ByteArrayInputStream(file.getContent()));
        } catch (Exception e) {
        	throw new PoRuntimeException(e);
        }
        return document;
	}
    
    
    

    public void updateConfigurationFile(Configurable configurable, org.w3c.dom.Document doc, String path) {
    	
    	// ignore path argument
    	path = proposeFullQualifiedNameForConfigurationFile(configurable);
    	
        PoFile file = new PoFile();
        if (configurable.getConfigFiles().size()>0) {
        	// there are existing files!
            PoFile existingFile = (PoFile) configurable.getConfigFiles().toArray()[0];
            file.setFileId(existingFile.getFileId());
        }
        
        // set links
        file.setConfigurable( configurable );
        configurable.addConfigFile( file );

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        file.setTimeStamp(new Date());
        
        try { 
        	file.setContent(outputter.outputString(XMLTools.convertToJdomDoc(doc)).getBytes("UTF8"));
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
        
        file.setMimeType("text/xml");
        file.setPath(path);
        
        // we want to create a new version 
        saveFile(file, true);
    }
    
    private String getRootNode(Configurable configurable) {
    	
    	if (configurable instanceof PoAction) {
    		return PoFileService.ACTION_CONFIG_XML_ROOT;
    	} else if (configurable instanceof PoConnector) {
    		return PoFileService.CONNECTOR_CONFIG_XML_ROOT;
    	} else if (configurable instanceof PoJob) {
    		return PoFileService.JOB_CONFIG_XML_ROOT;
    	}
    	
    	throw new IllegalStateException("The passed configureable is no PoAction, PoConnector or PoJob!");
    }
    
    
    public void generateConfigFileOfConfigurable(Configurable configurable) {
		try {
			org.jdom.Document doc = generateConfigXml( configurable );
			
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			
	        PoFile file = new PoFile();
	        
	        // set backlink
	        file.setConfigurable( configurable );
	        configurable.addConfigFile(file);
	        
	        if (configurable.getConfigFiles()!=null && configurable.getConfigFiles().size()>0)
	        	file.setFileId(((PoFile) configurable.getConfigFiles().toArray()[0]).getFileId());
	        
	        file.setContent(outputter.outputString(doc).getBytes("UTF8"));
	        file.setMimeType("text/xml");
	        file.setPath( proposeFullQualifiedNameForConfigurationFile(configurable) );
	        // not needed file.setPath()
	        file.setTimeStamp(new Date());
	        // not known file.setType()
	        saveFile(file, false); 
	        
		} catch (Exception e) {
			throw new PoRuntimeException(e);
		}
	}
    
    private String proposeFullQualifiedNameForConfigurationFile(Configurable configurable) {
    	
    	return "at/workflow/webdesk/" + configurable.getModule().getName() + "/" + getRootNode(configurable) + "s/" + configurable.getName() + "/" + configurable.getName() + ".xml";
    }
    
    
    public void saveXmlFile(Document doc, String path, int whereToSave, boolean prettyFormat) {
        PoFile file = new PoFile();
        FileOutputStream out=null;
        
        if (whereToSave==PoFileService.SAVE_FILE_FILESYSTEM_ONLY) {
	        path = path.replaceAll("\\\\","/");
	        int index = path.lastIndexOf("/");
	        String fileName = path.substring(index+1);
	        File p=null;
	        // previously the real path was considered here
	        p = new File(path.substring(0,index));
	        File f = new File(p,fileName);
	        // create Path, if it does not exist. 
	        if (!p.exists()) 
	            p.mkdirs();
	        // create File, if it does not exist. 
	        try {
	        	if (!f.exists())
	                f.createNewFile();
        	
	        	out = new FileOutputStream(f);
	        } catch (IOException e1) {
	        	logger.error(e1,e1);
	        }
        }
        try {
            XMLOutputter outputter = null;
            if (prettyFormat)
                outputter= new XMLOutputter(Format.getPrettyFormat());
            else
                outputter= new XMLOutputter(Format.getCompactFormat());
            file.setContent(outputter.outputString(doc).getBytes());
            file.setMimeType("text/xml");
            file.setPath(path);
            file.setTimeStamp(new Date());
            // stores the file in the filesystem, as well as in the database.
            
            if(whereToSave==PoFileService.SAVE_FILE_BOTH || whereToSave==PoFileService.SAVE_FILE_INTERNALLY_ONLY)
                    saveFile(file, false);
            if(whereToSave==PoFileService.SAVE_FILE_FILESYSTEM_ONLY){
                    outputter.output(doc,out);
                    out.close();
            }
                    
        } catch (Exception e) {
            logger.error(e,e);
        }
        
    }
	
    public void saveXmlFile(org.w3c.dom.Document doc, String path, int whereToSave) {
        DOMBuilder myBuilder = new DOMBuilder();
        saveXmlFile(myBuilder.build(doc),path,whereToSave);
    }

    public void saveXmlFile(Document doc, String path) {
        saveXmlFile(doc,path,0);        
    }
    
    public List<PoFile> findFilesOfActionOrderByVersion(PoAction action) {
    	return fileDAO.findFilesOfActionOrderByVersion(action);
    }

	public PoFile getFilePerPath(String relPath) {
		return fileDAO.getFileWithHighestVersion(fileDAO.getFileIdPerPath(relPath));
	}

	public List<PoFile> findFileWherePathLikeAndMaxVersion(String constraint) {
		return fileDAO.findFileWherePathLikeAndMaxVersion(constraint);
	}

	/** spring setter for DAO **/
	public void setFileDAO(PoFileDAO fileDAO) {
		this.fileDAO = fileDAO;
	}

	public List<String> loadAllFileIds() {
		return fileDAO.loadAllFileIds();
	}

	public void saveXmlToFileSystem(org.w3c.dom.Document doc, String filePath) {
		Document jdomDoc = XMLTools.convertToJdomDoc(doc);
		saveXmlFile(jdomDoc, filePath, 1);
	}

	public void saveXmlToFileSystem(Document doc, String filePath) {
		saveXmlFile(doc, filePath, 1);
	}

	@Override
	public PoFile getFileByIdAndVersion(String fileId, int versionNo) {
		return fileDAO.getFileWithVersionAndFileId(fileId, versionNo);
	}


}
