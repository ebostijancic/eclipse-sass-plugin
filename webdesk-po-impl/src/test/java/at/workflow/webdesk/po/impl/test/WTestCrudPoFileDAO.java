package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoFileDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoFile> {

	private static final String content = "Test File Content";
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoFile.class);
		super.onSetUpAfterDataGeneration();
	}

	@Override
	protected Object getRandomValue(Class<? extends PersistentObject> parentClass, String propertyName, Class<?> propertyClass) {
		if (propertyName.equals("size"))	{
			return content.getBytes().length;
		}
		else if (propertyName.equals("content"))	{
			return content.getBytes();
		}
		return super.getRandomValue(parentClass, propertyName, propertyClass);
	}

}
