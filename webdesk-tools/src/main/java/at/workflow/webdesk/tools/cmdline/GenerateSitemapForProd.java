package at.workflow.webdesk.tools.cmdline;

import java.io.FileOutputStream;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class GenerateSitemapForProd {

    public static void main(String[] args) {
        org.jdom.Document myDoc = new Document();
        SAXBuilder myBuilder = new SAXBuilder();
        try {
            // write "caching" to 3. Pipeline
            Resource r = new FileSystemResource(args[0]); 
            myDoc = myBuilder.build(r.getInputStream());
            
            // FIXME: if the wrong number is taken here
            // we will patch the wrong pipeline!!!
            Element myNode = (Element)XPath.selectSingleNode(myDoc, "/map:sitemap/map:pipelines/map:pipeline[4]");
            
            myNode.setAttribute("type","caching");
            
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(myDoc, new FileOutputStream(r.getFile()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
