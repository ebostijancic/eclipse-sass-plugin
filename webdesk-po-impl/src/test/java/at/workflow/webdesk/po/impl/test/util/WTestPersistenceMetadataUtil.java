package at.workflow.webdesk.po.impl.test.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.ClassUtils;
import at.workflow.webdesk.tools.BeanReflectUtil.BeanProperty;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadata;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadataUtil;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * Tests the PersistenceMetadataUtil to return no overlapping lists.
 * 
 * @author fritzberger 22.10.2010
 */
public class WTestPersistenceMetadataUtil extends AbstractTransactionalSpringHsqlDbTestCase {

	public void testPersistenceMetadataUtil()	{
		 List<BeanProperty> properties = PersistenceMetadataUtil.getPersistentProperties(PoPerson.class, null);
		 assertProperties(properties);
	}
		 
	public void testPersistenceMetadataUtilWithMetaData()	{
		PersistenceMetadata metaData = (PersistenceMetadata) WebdeskApplicationContext.getBean("PersistenceMetadata");
		List<BeanProperty> properties = PersistenceMetadataUtil.getPersistentProperties(PoPerson.class, null, metaData);
		assertProperties(properties);
	}
	
	private void assertProperties(List<BeanProperty> properties) {
		 
		 List<BeanProperty> standardProperties = PersistenceMetadataUtil.getStandardProperties(properties);
		 assertFalse(standardProperties.isEmpty());
		 for (BeanProperty bp : standardProperties)
			 assertTrue(ClassUtils.isStandardType(bp.getter.getReturnType()));
		 
		 List<BeanProperty> collectionProperties = PersistenceMetadataUtil.getCollectionProperties(properties);
		 assertFalse(collectionProperties.isEmpty());
		 for (BeanProperty bp : collectionProperties)
			 assertTrue(ClassUtils.isCollection(bp.getter.getReturnType()));
		 
		 List<BeanProperty> objectReferenceProperties = PersistenceMetadataUtil.getBeanReferenceProperties(properties);
		 assertFalse(objectReferenceProperties.isEmpty());
		 for (BeanProperty bp : objectReferenceProperties)
			 assertTrue(ClassUtils.isBeanReference(bp.getter.getReturnType()));
		 
		 boolean clientPresent = false;
		 for (BeanProperty bp : objectReferenceProperties)
			 if (bp.propertyName.equals("client"))
				 clientPresent = true;
		 assertTrue(clientPresent);
		 
		 assertNoIntersections(standardProperties, objectReferenceProperties, collectionProperties);
	}

	private void assertNoIntersections(List<BeanProperty> standardProperties, List<BeanProperty> objectReferenceProperties, List<BeanProperty> collectionProperties) {
		Map<String,BeanProperty> uniqueKeys = new HashMap<String,BeanProperty>()	{
			@Override
			public BeanProperty put(String key, BeanProperty value) {
				assertFalse(containsKey(key));
				return super.put(key, value);
			}
		};
		
		standardProperties.addAll(objectReferenceProperties);
		standardProperties.addAll(collectionProperties);
		for (BeanProperty bp : standardProperties)
			uniqueKeys.put(bp.propertyName, bp);
	}

}
