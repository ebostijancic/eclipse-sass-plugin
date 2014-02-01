package at.workflow.webdesk.po.impl.update.v46;

import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.daos.PoGeneralDAO;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoHistorization;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          webdesk3-Trunk<br>
 * created at:       16.04.2008<br>
 * package:          at.workflow.webdesk.po.impl.update.v39<br>
 * compilation unit: SetTopLevelGroups.java<br><br>
 *
 *<p>
 *This update script corrects the validto dates of <code>PoHistorization</code> 
 *objects. It has been shown, that there are objects where the <code>validto</code>
 *field is after the <code>DateTools.INFDATE</code> which is not allowed, 
 *as this date is considered to be the latest date in the system. 
 *<p>
 *
 */
public class CorrectValidTo extends PoAbstractUpgradeScript {

	public void execute() {
		PoOrganisationService orgService = (PoOrganisationService) getBean("PoOrganisationService");
		PoGeneralDAO gd = (PoGeneralDAO) getBean("PoGeneralDAO");
		String query ="update PoHistorization set validto=? where validto=?";
		Iterator histObjI = gd.loadAllObjectsOfType(PoHistorization.class).iterator();
		int counter = 0;
		
		logger.info("Going to correct all Historiation obects that are after " + new Date(DateTools.INFINITY_TIMEMILLIS));
		while (histObjI.hasNext()) {
			PoHistorization histObj = (PoHistorization) histObjI.next();
			if (histObj.getValidto().getTime()>DateTools.INFINITY_TIMEMILLIS) {
				counter ++; 
				histObj.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
				gd.saveObject(histObj);
				if (counter%20==0)
					logger.info("Actually " + counter + " Historization objects adapted "+
							" from " + histObj.getValidto() + " to " + new Date(DateTools.INFINITY_TIMEMILLIS));
			}
		}
	}
	
	
}
