package at.workflow.webdesk.po.impl.test;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Value;

import at.workflow.webdesk.tools.hibernate.ExtLocalSessionFactoryBean;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * this class demonstrates the use of hibernate metadata to find out
 * sql-types and collection types of hibernate properties.
 * 
 * @author ggruber
 *
 */
public class WTestOutputMetadata extends AbstractTransactionalSpringHsqlDbTestCase {

	private Logger logger = Logger.getLogger(this.getClass());
	
	public void testMetaDataAccess() throws Exception {
		
		Configuration config = ExtLocalSessionFactoryBean.getConfiguration("webdesk-SessionFactory");
		
		logger.info("We have mapped this classes:");
		
		System.out.println("++++ Entity Infos ++++");
		int i=0;
		Iterator<?> entityMpItr = config.getClassMappings();
		while (entityMpItr.hasNext()) {
			PersistentClass classMapping = (PersistentClass) entityMpItr.next();
			
			System.out.println("Entity:" + classMapping.getClassName() + " [Table=" + classMapping.getTable().getName() + "]");
			Iterator<?> propItr = classMapping.getPropertyIterator();
			while (propItr.hasNext()) {
				Property prop = (Property) propItr.next();
				Value propDef = prop.getValue();
				
				// take first column
				Column col = null;
				Value colDef = null;
				if (prop.getColumnIterator().hasNext()) {
					col = (Column) prop.getColumnIterator().next();
					colDef = col.getValue();
				}
				System.out.println("    property:" + prop.getName() + " [type=" + prop.getType().getName() +",isCollection=" + propDef.getType().isCollectionType() + "]");
				if (col!=null) {
					System.out.println("           [length=" + col.getLength() + ",sql-type=" + col.getSqlType() +",nullable=" + colDef.isNullable() + "]");
				}
			}
			System.out.println("        -------------------------              ");
			
			if (i++>3) 
				break;
		}
		
		
	}
	
}