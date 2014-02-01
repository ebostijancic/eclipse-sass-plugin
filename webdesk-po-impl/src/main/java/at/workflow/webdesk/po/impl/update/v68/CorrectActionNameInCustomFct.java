package at.workflow.webdesk.po.impl.update.v68;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * This updatescript corrects custom flowscript controllers from
 * var func = function() {} to  function func() {}
 * in order to working under 3.3 (cocoon 2.2)
 * 
 * Furthermore mimetypes and sizes are corrected of files (PoFile objects).
 * Paths of Custom-Action files are corrected, as they might have trailing
 * dots and/or slashes.
 * 
 *@author hentner, ggruber
 *
 */
public class CorrectActionNameInCustomFct extends PoAbstractUpgradeScript {

	@Override
	public void execute() {
		PoFileService fileService = (PoFileService) WebdeskApplicationContext.getBean("PoFileService");
		List <PoFile>files = fileService.findFileWherePathLikeAndMaxVersion("./custom/%.js");
		for (Iterator<PoFile> fileI = files.iterator(); fileI.hasNext();) {
			PoFile file = fileI.next();
			BufferedReader br = 
				new BufferedReader(new InputStreamReader(new ByteArrayInputStream(file.getContent())));
			StringBuffer finalContent = new StringBuffer();
			String line="";
			try {
				boolean adapted = false;
				while ((line=br.readLine())!=null) {
					if (line.matches("var\\s*custom\\w*_\\w*\\s*\\=\\s*function\\(.*")) {
						// extract functionName
						adapted=true;
						String functionName = line.substring(0, line.indexOf("=")-1);
						functionName = functionName.substring(line.indexOf("var")+4);
						functionName = functionName.trim();

						String parameters = line.substring(line.indexOf("("));
						parameters = parameters.substring(0, parameters.indexOf(")")+1);
						// we have to consider the parameters as well
						finalContent.append("function " + functionName + parameters);
						if (line.indexOf("{")!=-1)
							finalContent.append(" {");
						finalContent.append(System.getProperty("line.separator"));
					} else 
						finalContent.append(line + System.getProperty("line.separator"));

				}

				if (adapted) {
					file.setContent(finalContent.toString().getBytes());
					fileService.saveFile(file, true);
					logger.info("Adapted custom action " + file.getPath());
					System.out.println("Adapted custom action " + file.getPath());
				}
			} catch (IOException e) {
				logger.error(e,e);
			}
		}

		files = fileService.loadAllFiles();
		System.out.println("Setting the mime-type for " +files.size() + " files.");
		int counter = 0;
		for (Iterator<PoFile> fileI=files.iterator(); fileI.hasNext();) {
			PoFile file = fileI.next();

			repairPathForFilesOfCustomActions(file);
			repairMimeType(file);
			repairSize(file);

			fileService.saveFile(file, false);

			counter ++; 
			if (counter%100==0)
				System.out.println("Repaired mime-type, Size and Path for " + counter + " files already.");
		}
	}


	private void repairSize(PoFile file) {
		if (file.getContent()!=null && file.getSize() == 0) 
			file.setSize(file.getContent().length);
	}


	private void repairPathForFilesOfCustomActions(PoFile file) {

		if (file.getPath()!=null && file.getAction()!=null && 
				(file.getAction().getActionType() == PoConstants.ACTION_TYPE_CUSTOM || file.getAction().getName().startsWith("custom")) )  {

			// remove different starting patterns in existing path names (especially in the old .js files)
			removeStartingString(file, "/./");
			removeStartingString(file, "././");
			removeStartingString(file, "./");
		}
	}


	private void repairMimeType(PoFile file) {

		// if mimetype is there and has 'xml' inside
		if (file.getMimeType()!=null && file.getMimeType().toLowerCase().indexOf("xml")!=-1)
			file.setMimeType("text/xml");

		// if mimetype is not set and path is there -> set it
		if (file.getMimeType() == null && file.getPath()!=null && !"".equals(file.getPath()))  {
			file.setMimeType(getMimeTypeFromPath(file.getPath()));
		}

	}


	private void removeStartingString(PoFile file, String toRemove) {
		if (file.getPath().startsWith(toRemove))
			file.setPath(file.getPath().substring(toRemove.length(),file.getPath().length()));
	}


	private String getMimeTypeFromPath(String path) {
		if (path.trim().endsWith(".js"))
			return "text/javascript";
		if (path.trim().endsWith(".xml"))
			return "text/xml";
		if (path.trim().endsWith(".html"))
			return "text/html";

		if (path.trim().endsWith(".css"))
			return "text/css";
		if (path.trim().endsWith(".jpg"))
			return "image/jpeg";
		if (path.trim().endsWith(".gif"))
			return "image/gif";
		if (path.trim().endsWith(".png"))
			return "image/png";

		return "";

	}


}
