package at.workflow.webdesk.po.impl.test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

public class TestHistorizationHelper extends AbstractTransactionalSpringHsqlDbTestCase	{

	public void run(TestServiceAdapter adapter) {
		
		List<? extends Historization> existingEntries = adapter.findEntries();
		
		// add a new Entry 
		Historization newObject = adapter.generateNew("Adapter01");
		adapter.save(newObject);

		Calendar from = new GregorianCalendar(); 
		Calendar to = new GregorianCalendar(); 
		to.add(Calendar.DAY_OF_MONTH, 10);
		
		adapter.doAssignment(newObject, from.getTime(), to.getTime());
		assertEquals((existingEntries.size() + 1),adapter.findEntries().size());

		// and so on... 
		
		
	}

}
