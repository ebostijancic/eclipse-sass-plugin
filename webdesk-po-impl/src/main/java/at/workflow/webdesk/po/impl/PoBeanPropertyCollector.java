package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.po.model.PoBeanPropertyValue;

/**
 * This class is used to collect bean properties (with values) of Spring beans
 * at web-application startup, in a PO-compliant form (via PoBeanProperty instances).
 * <p/>
 * The list of these values is used later to synchronize beans in database with the
 * newly deployed ones (in XML files). There could be new beans, or deleted beans.
 * <p/>
 * The database itself cannot be used here, otherwise we would automatically
 * instantiate all dependent factory-beans (like caches ...).
 * 
 * @author gkossakowski
 * @author ggruber
 */
public class PoBeanPropertyCollector extends PropertyOverrideConfigurer {
	
	private static final Log logger = LogFactory.getLog(PoBeanPropertyCollector.class);
	
	private static final String [] ignoredBeanNamePatterns = new String []	{
		"org.apache",	// excluding Cocoon beans
		"javax",
		"org.springframework",
		"org.mozilla",
		"org.xml",
	};
	
	private List<PoBeanProperty> collectedBeanProperties = new ArrayList<PoBeanProperty>(); 
	
	/** Called on startup to find out what Spring beans have been allocated on application-context boot. */
	public List<PoBeanProperty> getCollectedBeanProperties() {
		return collectedBeanProperties;
	}
	
	/** Spring callback. {@inheritDoc} */ 
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		logger.info("collecting Beanproperties...");
		collectedBeanProperties = collectBeanProperties(beanFactory);
	}
	
	private List<PoBeanProperty> collectBeanProperties(ConfigurableListableBeanFactory beanFactory) {
		List<PoBeanProperty> beanProperties = new LinkedList<PoBeanProperty>();
		
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			if (isIgnoredBeanName(beanName))
				continue;
			
			BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
			
			// only process singletons
			if (beanDefinition.isSingleton()) {
				
				for (PropertyValue propertyValue : beanDefinition.getPropertyValues().getPropertyValues()) {
					PoBeanProperty poBeanProperty = new PoBeanProperty();
					poBeanProperty.setBeanName(beanName);
					poBeanProperty.setClassName(beanDefinition.getBeanClassName());
					poBeanProperty.setPropertyName(propertyValue.getName());
					poBeanProperty.setDetached(false);
					// it is not possible to set the Module at this point, as there is no application context reachable 
					
					if (false == isPropertyValuePrimitive(propertyValue)) {	// process non-primitive values
						Object o = propertyValue.getValue();
						
						if (o instanceof Collection<?> ) {	// process collection values (lists and sets)
							poBeanProperty.setList(true);
							SortedSet<PoBeanPropertyValue> entries = new TreeSet<PoBeanPropertyValue>();
							int counter = 0;
							for (Object singleValue : (Collection<?>) o) {
								if (false == (singleValue instanceof RuntimeBeanReference) && false == (singleValue instanceof BeanDefinitionHolder)) {
									PoBeanPropertyValue value = createBeanPropertyValue(convertToString(singleValue), poBeanProperty, counter++); 
									entries.add(value);
									poBeanProperty.setType(convertRawTypeToPoType(singleValue));
								}
							}
							poBeanProperty.setEntries(entries);
							beanProperties.add(poBeanProperty);
						}
						
					} else {	// process primitive values
						poBeanProperty.setList(false);
						poBeanProperty.setType(convertTypeToPoType(propertyValue));
						
						SortedSet<PoBeanPropertyValue> entries = new TreeSet<PoBeanPropertyValue>();
						entries.add(createBeanPropertyValue(convertToString(propertyValue.getValue()),poBeanProperty, 0 ));
						poBeanProperty.setEntries(entries);
						
						beanProperties.add(poBeanProperty);
					}
				}
			}
			
		}
		
		return beanProperties;
	}

	
	private boolean isIgnoredBeanName(String beanName)	{
		for (String ignoredPattern : ignoredBeanNamePatterns)	{
			if (beanName.contains(ignoredPattern))
				return true;
		}
		return false;
	}
	
	private PoBeanPropertyValue createBeanPropertyValue(String property, PoBeanProperty beanProperty, int listIndex) {
		PoBeanPropertyValue ret = new PoBeanPropertyValue();
		ret.setBean(beanProperty);
		ret.setProperty(property);
		ret.setListIndex(listIndex);
		return ret;
	}
	
	private boolean isPropertyValuePrimitive(PropertyValue propertyValue) {
		Object o = propertyValue.getValue();
		return (o instanceof Boolean || o instanceof String || 
				o instanceof Integer || o instanceof Double ||
				o instanceof TypedStringValue);
	}
	
	private int convertTypeToPoType(PropertyValue propertyValue) {
		return convertRawTypeToPoType(propertyValue.getValue());
	}
	
	private int convertRawTypeToPoType(Object value) {
		if (value instanceof String || value instanceof TypedStringValue)
			return PoConstants.STRING;
		else if (value instanceof Boolean)
			return PoConstants.BOOLEAN;
		else if (value instanceof Integer)
			return PoConstants.INTEGER;
		else if (value instanceof Double)
			return PoConstants.DOUBLE;
		else
			throw new RuntimeException("The type " + value.getClass().getName() + " cannot be converted to PO-type.");
	}
	
	private String convertToString(Object value) {
		if (value instanceof TypedStringValue)
			return ((TypedStringValue)value).getValue();
		return value.toString();
	}

}
