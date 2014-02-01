package at.workflow.tools;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

/**
 * Simple Class which can be used to instantiate Objects
 *  
 * @author ggruber
 */
public class SimpleObjectFactory {
	protected final Logger logger = Logger.getLogger(this.getClass());
	
	public Object instantiate(String className) {
		Object ret = null;
		
		if (className!=null && !className.equals("")) {
			try {
				Class<?> cls = Class.forName(className);
				ret = cls.newInstance();
			} catch (Exception e) {
				this.logger.error("could not instantiate class with name " + className, e);
			}
		}
		return ret;
	}
	
	public Object instantiate(String className, Object parameter) {
		Object ret = null;
		
		if (className!=null && !className.equals("")) {
			try {
				Class<?> cls = Class.forName(className);
				
				Constructor<?> con = findConstructor(cls, parameter.getClass());
				ret = con.newInstance(new Object[] {parameter});
			} catch (Exception e) {
				this.logger.error("could not instantiate class with name " + className, e);
			}
		}
		return ret;
	}
	
	private Constructor<?> findConstructor(Class<?> cls, Class<?> parameterType) {
		Constructor<?> ret=null;
		
		boolean found=false;
		
		while (!found && !(parameterType.equals(Object.class))) {
			try {
				Class<?>[] paramTypes = { parameterType};
				ret = cls.getConstructor(paramTypes);
				return ret;
			} catch (NoSuchMethodException e) {
				found=false;
				parameterType = parameterType.getSuperclass(); 
			}
		}
		
		
		throw new RuntimeException("no constructor found for class=" + cls + " and single parametertype=" + parameterType);
	}
}
