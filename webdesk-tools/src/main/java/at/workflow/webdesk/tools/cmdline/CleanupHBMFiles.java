package at.workflow.webdesk.tools.cmdline;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * This commandline utility class is used by the ant genHbm task to 
 * "cleanup" the generated hbm Files. This 
 * @author ggruber
 *
 */
public class CleanupHBMFiles {
	
	private static FileFilter filter=new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().endsWith(".hbm.xml"));
		}
	};
	
	public static void main(String[] args){
		
		String baseDir=args[0];
		
		if(args.length<1)
			throw new RuntimeException("expected a Argument (Directory to start from)");
		
		File dir=new File(baseDir);
		
		if(!dir.isDirectory())
			throw new RuntimeException("expected a Directory as Argument");
		
		cleanupHbmFilesAndParseSubdirs(dir);
		
	}
	
	private static void cleanupHbmFilesAndParseSubdirs(File dir){
		File[] files=dir.listFiles(filter);
		for(int i=0;i<files.length;i++)
			if(files[i].isDirectory())
				cleanupHbmFilesAndParseSubdirs(files[i]);
			else
				cleanUpHbm(files[i]);
	}

	// clean up the hbm-file
	private static void cleanUpHbm(File file){
		System.out.println("cleaning up: " + file.getName() + " (" + file.getAbsolutePath() + ")");
		SAXBuilder sb = new SAXBuilder();
        // do not load dtds !!!
        // only valid for xalan (not for crimson)
		sb.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar",false);
		sb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
    	
		try{
			Document doc=sb.build(file);
			// remove specified Argument recursively
			removeArgumentsRec(doc.getRootElement(),"access");
			// write the File back to the Filesystem
			FileOutputStream os=new FileOutputStream(file);
			XMLOutputter outputter=new XMLOutputter();
			outputter.output(doc, os);
			os.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void removeArgumentsRec(Element e, String attribute){
		e.removeAttribute(attribute);
		for(int i=0;i<e.getChildren().size();i++){
			removeArgumentsRec((Element)e.getChildren().get(i),attribute);
		}
		if(e.getName().equals("filter-def")){
			if(e.getAttribute("name").getValue().endsWith("fromDate2Infinite")){
				Element filterParam=new Element("filter-param");
				filterParam.setAttribute("name", "filterFromDate");
				filterParam.setAttribute("type", "timestamp");
				e.addContent(filterParam);
			}
		}
		// delete generator tags with class attributes containing "none"
		if(e.getName().equals("generator") && e.getAttribute("class").getValue().equals("none")){
			e.getParent().removeContent(e);
		}
	}
	
}
