package at.workflow.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.context.support.ServletContextResource;

import at.workflow.tools.XMLTools;

/**
 * Simple helper class that tries to extract classpath from a given resource
 */
public class ResourceHelper {

	public static String getClassPathOfResource(Resource res) {
		String classpath;
		
		if (res instanceof ClassPathResource) {
			classpath = ((ClassPathResource) res).getPath();
		}
		else if (res instanceof FileSystemResource) {
			String path = ((FileSystemResource) res).getPath();
			path = path.replaceAll("\\\\", "/");
			if (path.indexOf("bin/") > -1)
				classpath = path.substring(path.indexOf("bin/") + 4);
			else
				classpath = path.substring(path.indexOf("classes/") + 8);

		}
		else if (res instanceof UrlResource) {
			try {
				String url = ((UrlResource) res).getURL().toExternalForm();
				classpath = url.substring(url.indexOf("!") + 2);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else if (res instanceof ServletContextResource) {
			String path = ((ServletContextResource) res).getPathWithinContext();
			path = path.replaceAll("\\\\", "/");
			classpath = path.substring(path.indexOf("classes/") + 8);
		}
		else {
			throw new RuntimeException("Unhandled Resource instance " + res.getClass().getName());
		}
		
		if (classpath.startsWith("classpath:/"))
			classpath = classpath.replaceFirst("classpath:/", "");
		
		return classpath;
	}

	/**
	 * @param is an <code>InputStream</code> of a valid <code>XML</code> resource.
	 * @return a <code>org.w3c.dom.Docuemnt</code> or <code>null</code> if
	 * no resource was found.
	 */
	public static org.w3c.dom.Document loadDocumentWithoutDoctype(InputStream is) {
		Document doc = null;
		is = removeDocType(is);

		SAXBuilder sxbuild = new SAXBuilder(false);

		try {
			doc = sxbuild.build(is);
		}
		catch (JDOMException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return XMLTools.convertToW3cDoc(doc);
	}

	private static InputStream removeDocType(InputStream is) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = "";
		StringBuffer res = new StringBuffer();

		try {
			while ((line = br.readLine()) != null) {
				if (!line.contains("<!DOCTYPE"))
					res.append(line);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return new ByteArrayInputStream(res.toString().getBytes(Charset.forName("utf-8")));

	}

}