package at.workflow.webdesk.tools.config;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * This class is an implementation of an <code>URIResolver</code>
 * for JAXP based transformations to support the classpath:// and
 * resource:// protocols known from spring and cocoon.
 * 
 * @author ggruber
 *
 */
public class ClassPathUriResolver implements URIResolver {

	public Source resolve(String href, String base) throws TransformerException {

		Resource res=null;
		if (href.startsWith("resource:/") || href.startsWith("classpath") ) {
			String ref = href.replaceAll("resource://", "");
			ref = ref.replaceAll("resource:/", "");
			ref = ref.replaceAll("classpath*:", "classpath:");
			ref = ref.replaceAll("classpath://", "");
			ref = ref.replaceAll("classpath:/", "");
			res = new ClassPathResource(ref);
		} else {
			// try to use file based resolving
			res = new FileSystemResource(base + "/" + href);
		}
		try {
			return new StreamSource(res.getInputStream());
		} catch (IOException e) {
			throw new TransformerException("could not resolve URI=" + href, e);
		}
	}

}
