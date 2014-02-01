package at.workflow.webdesk.po.impl.update.v72;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateTools;

public class PoCorrectHistorizationObjects extends PoAbstractUpgradeScript {

	@Override
	public void execute() {
		PoGeneralDbService database = (PoGeneralDbService) getBean("PoGeneralDbService");
		
		Object[] params = { new GregorianCalendar(2999,11,30).getTime() };
		@SuppressWarnings("unchecked")
		List<Historization> historicizedObjects = (List<Historization>) database.getElementsAsList("from at.workflow.webdesk.tools.api.Historization where validTo > ?", params);
		
		final Date infinityDate = new Date(DateTools.INFINITY_TIMEMILLIS);
		for (Historization historicizedObject : historicizedObjects) {
			historicizedObject.setValidto(infinityDate);
			database.saveObject(historicizedObject);
		}
	}

}
