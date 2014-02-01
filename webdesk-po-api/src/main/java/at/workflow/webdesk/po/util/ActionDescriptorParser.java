package at.workflow.webdesk.po.util;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.workflow.tools.XMLTools;
import at.workflow.tools.XPathTools;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoActionParameter;
import at.workflow.webdesk.po.model.PoActionUrlPattern;
import at.workflow.webdesk.po.model.PoContextParameter;
import at.workflow.webdesk.po.model.PoModule;


public abstract class ActionDescriptorParser {
		
	private static String getXpathString( Object context, String path) {
		return xpathTools.getXPathStringValue(context, path);
	}
	
	private static boolean getXpathBoolean( Object context, String path) {
		return xpathTools.getXPathBooleanValue(context, path);
	}
	
	private static boolean getXpathBoolean( Object context, String path, boolean def) {
		return xpathTools.getXPathBooleanValue(context, path, def);
	}
	
	private static XPathTools xpathTools = new XPathTools();
	

	public static PoAction parseActionInfosFromDescriptor(PoActionService actionService, Object obj, PoModule module) {
    	
        PoAction action = new PoAction();
        Document doc;
        if (obj instanceof File)
            doc = XMLTools.createW3cDocFromFile( (File) obj ) ;
        else
            doc = XMLTools.createW3cDocFromStream( (InputStream) obj );
        
        
        // this seems to be redundant...
    	action.setModule(module);
        
    	// we can't take the root node, cause it could also be a comment :-(
        Node root = xpathTools.getNodeWithXPath(doc, "/act-descr");
        
        if (module!=null)
        	action.setActionFolder( module.getName() );
        else 
        	action.setActionFolder( getXpathString(root, "actionfolder"));
        
        // TODO: refactor with respect to primary textmodules 
        // still necessary?
        action.setCaption( getXpathString(root, "caption"));
        action.setDescription( getXpathString(root, "description"));

        // mandatory
        action.setImage( getXpathString(root, "image") );
        action.setImageSet( getXpathString(root,"imageset") );
        action.setActionType(PoConstants.ACTION_TYPE_ACTION);
        action.setAllowUpdateOnVersionChange( getXpathBoolean(root, "allowupdateonversionchange") );
        
        // allowsAction
        String allowsActionStr = getXpathString(root,"allowsaction");
        if ( StringUtils.isNotEmpty(allowsActionStr) ) {
        	PoAction allowsAction = actionService.findActionWithFullName(allowsActionStr);
        	if (allowsAction!=null) {
        		action.setAllowsAction(allowsAction);
        	}
        }
        
        // optional
    	action.setUniversallyAllowed( getXpathBoolean(root, "universallyallowed", false) );
    	action.setConfigurable( getXpathBoolean(root, "isconfigurable", false) );
    	action.setDisableContinuationInvalidate( getXpathBoolean(root, "disableContinuationInvalidation", false) );
    	action.setRefreshOnComingBack( getXpathBoolean(root, "refreshOnComingBack", false) );
    	
    	
        if (getXpathString(root, "defaultviewpermissiontype")!=null)
        	action.setDefaultViewPermissionType( new Integer(getXpathString(root, "defaultviewpermissiontype")).intValue() );
        
    	action.setSqlQuery( getXpathString(root, "sqlQuery") );
        
        action.setController( getXpathString(root, "controller") );
        action.setControllerPattern( getXpathString(root, "controllerPattern") );
        
        
        NodeList nodeSet;
        
        // set attributes
        action.getAttributes().clear();
        
        nodeSet = xpathTools.getNodesetWithXPath(root, "attributes/value");
        for (int i=0;i<nodeSet.getLength();i++) {
        	Node valueNode = nodeSet.item(i);
        	String key = getXpathString(valueNode, "@key");
        	String value = valueNode.getTextContent();
    		action.getAttributes().put(key, value);
        }
        
        // set parameters
        action.getParameters().clear();
        nodeSet = xpathTools.getNodesetWithXPath(root, "parameters/parameter");
	    for (int i=0;i<nodeSet.getLength();i++) {
	    	Node paramNode = nodeSet.item(i);
        	PoActionParameter parameter = new PoActionParameter();
        	parameter.setAction(action);
        	parameter.setName( getXpathString(paramNode, "@name") );
        	parameter.setType( getXpathString(paramNode, "@type") );
        	parameter.setPattern( getXpathString(paramNode, "@pattern") );
        	parameter.setComment( getXpathString(paramNode, "@comment"));
        	action.getParameters().add(parameter);
        }

	    // set url patterns
        action.getUrlPatterns().clear();
        nodeSet = xpathTools.getNodesetWithXPath(root, "urlpatterns/urlpattern");
        for (int i=0;i<nodeSet.getLength();i++) {
        	Node upNode = nodeSet.item(i);
        	PoActionUrlPattern pattern = new PoActionUrlPattern();
        	pattern.setAction(action);
        	pattern.setName( getXpathString(upNode, "@name") );
        	pattern.setPattern( upNode.getTextContent() );
        	action.getUrlPatterns().add(pattern);
        }
        
        // set context parameters
        action.getContextParameters().clear();
        nodeSet = xpathTools.getNodesetWithXPath(root, "contextparameters/contextparameter");
        for (int i=0;i<nodeSet.getLength();i++) {
        	Node cpNode = nodeSet.item(i);
        
        	if (getXpathString(cpNode, "@levels")!=null) {
        		String[] levels = getXpathString(cpNode, "@levels").split(",");
        		for (String level : levels) {
        			createContextParameter(action, cpNode, level.trim());
        		}
        	} else {
        		createContextParameter(action, cpNode, null);
        	}
        }
            
        return action;
    }

	private static void createContextParameter(PoAction action, Node cpNode, String level) {
		PoContextParameter parameter = new PoContextParameter();
		parameter.setAction(action);
		parameter.setName( getXpathString(cpNode, "@name") );
		parameter.setType( getXpathString(cpNode, "@type"));
		parameter.setPattern( getXpathString(cpNode, "@pattern"));
		if (level == null)
			parameter.setLevel( getXpathString(cpNode, "@level"));
		else
			parameter.setLevel( level );
		
		parameter.setComment( getXpathString(cpNode, "@comment"));
		action.getContextParameters().add(parameter);
	}


}
