package at.workflow.webdesk.po.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.core.io.Resource;

import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoHelpMessageService;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.daos.PoHelpMessageDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoHelpMessage;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.tools.XMLTools;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          3.1DEV<br>
 * created at:       29.08.2007<br>
 * package:          at.workflow.webdesk.po.impl<br>
 * compilation unit: PoHelpMessageServiceImpl.java<br><br>
 *
 * <p>Implemenation of the <code>PoHelpMessageService</code>.</p>
 *
 *
 */
public class PoHelpMessageServiceImpl implements PoHelpMessageService {

	private Logger logger = Logger.getLogger(this.getClass());
	private PoHelpMessageDAO helpMessageDAO;
	private PoActionService actionService;
	private PoLanguageService languageService;
	
	@Override
	public List<PoHelpMessage> findHelpMessages(PoLanguage lang) {
		return helpMessageDAO.findHelpMessages(lang);
	}

	@Override
	public List<PoHelpMessage> findHelpMessagesOfAction(PoAction action) {
		return helpMessageDAO.findHelpMessagesOfAction(action);
	}

	@Override
	public Map<String,PoHelpMessage> findHelpMessagesOfActionAsMap(PoAction action) {
		Map<String, PoHelpMessage> res = new HashMap<String, PoHelpMessage>();
		for (PoHelpMessage hm : findHelpMessagesOfAction(action)) {
			res.put(hm.getLanguage().getUID(), hm);
		}
		return res;
	}

	@Override
	public PoHelpMessage getHelpMessage(PoAction action, PoLanguage language) {
		return helpMessageDAO.getHelpMessage(action, language);
	}

	@Override
	public boolean hasHelpMessage(PoAction action, String locale) {
		boolean res = false;
		try {
			if (action!=null) {
				PoLanguage language = languageService.findLanguageByCode(locale);
				PoHelpMessage hm = getHelpMessage(action, language);
				if (hm!=null) 
					if (hm.getTextAsString()!=null &&!hm.getTextAsString().equals("") || 
							(hm.getUrl()!=null && !hm.getUrl().equals("")))
						return true;
			}
		} catch(Exception e) {
			logger.error("An error occured while trying to check if an action has a help message");
		}
		return res;
	}

	@Override
	public void saveHelpMessage(PoHelpMessage hm) {
		helpMessageDAO.save(hm);
		
	}

	public void setActionService(PoActionService actionService) {
		this.actionService = actionService;
	}

	public void setHelpMessageDAO(PoHelpMessageDAO helpMessageDAO) {
		this.helpMessageDAO = helpMessageDAO;
	}

	public void setLanguageService(PoLanguageService languageService) {
		this.languageService = languageService;
	}

	
	
	@SuppressWarnings("unchecked")
	@Override
	public void registerHelpMessages(Resource[] ress, String folderOfPackage) {
		SAXBuilder builder = new SAXBuilder();
		List<PoLanguage> languages = languageService.findAllLanguages();
		for (int i =0; i<ress.length; i++) {
			Resource r = ress[i];
			
	        Document doc;
	        try {
	        	String url = r.getURL().getPath();
	        	url = url.substring(url.indexOf("at/workflow/webdesk/" + folderOfPackage+"/actions/")+(20+folderOfPackage.length()+9));
	        	url = url.substring(0, url.indexOf("/"));
	        	url = folderOfPackage+"_" + url;
	            doc = builder.build(r.getInputStream());
	            Element root = doc.getRootElement();
	            Iterator<PoLanguage> lItr = languages.iterator();
	            PoAction action = actionService.findActionByNameAndType(url,PoConstants.ACTION_TYPE_ACTION);
	            while (lItr.hasNext() && action!=null) {
	            	PoLanguage lang = lItr.next();
	            	if (root.getChild(lang.getCode())!=null) {
	            		PoHelpMessage hm=null;
	            		if (this.getHelpMessage(action, lang)!=null) {
	            			hm = this.getHelpMessage(action, lang);
	            			
	            		} else {
	            			hm = new PoHelpMessage();
	            			hm.setAllowUpdateOnVersionChange(true);
	            		}
	            		if (hm.isAllowUpdateOnVersionChange()) {
		            		hm.setAction(action);
		            		hm.setLanguage(lang);
		            		Element content = root.getChild(lang.getCode());
		            		hm.setText(XMLTools.getElementTreeAsString(content.getChildren()));
		            		this.saveHelpMessage(hm);
	            		}
	            	}
	            }
	            
	        } catch (Exception e) {
	        	
	        }
		}
	            
	}

}
