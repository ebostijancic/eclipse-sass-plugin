package at.workflow.webdesk.tools;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;

/** 
 * Generates a Beanname out of the SimpleName of the underlying classname.
 * 
 * @author ggruber
 */
public class SimpleClassNameBeanNameGenerator implements BeanNameGenerator {

		@Override
		public String generateBeanName(BeanDefinition definition,
				BeanDefinitionRegistry registry) {
			
			return definition.getBeanClassName().substring(definition.getBeanClassName().lastIndexOf(".")+1);
		}

	}