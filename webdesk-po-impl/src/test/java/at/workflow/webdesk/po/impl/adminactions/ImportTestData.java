package at.workflow.webdesk.po.impl.adminactions;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import at.workflow.webdesk.po.SpecificAdminAction;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.tools.testing.DataGenerator;

public class ImportTestData implements SpecificAdminAction, ApplicationContextAware {

	private ApplicationContext applicationContext;
	
	@Override
	public void run() {
		DataGenerator gen = new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/TestData.xml");
		try {
			gen.create(applicationContext);
		}
		catch (IOException e) {
			throw new RuntimeException("Problems while generating...", e);
		}
	}

	@Override
	public String getI18nKey() {
		return "po_ImportTestData_caption";
	}

	@Override
	public String getSuccessMessage() {
		return "po_ImportTestData_success";
	}

	@Override
	public String getErrorMessage() {
		return "po_ImportTestData_error";	
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
