package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoBeanPropertyDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoBeanProperty> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoBeanProperty.class);
		super.onSetUpAfterDataGeneration();
	}

	@Override
	protected Object getRandomValue(Class<? extends PersistentObject> parentClass, String propertyName, Class<?> propertyClass) {
		if (propertyName.equals("detached"))	{
			return Boolean.FALSE;	// else they would not be read anymore by loadAll() and this test would fail
		}
		return super.getRandomValue(parentClass, propertyName, propertyClass);
	}

}
