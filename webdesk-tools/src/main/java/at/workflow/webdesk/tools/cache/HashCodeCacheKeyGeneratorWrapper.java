package at.workflow.webdesk.tools.cache;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.aop.support.AopUtils;
import org.springmodules.cache.key.CacheKeyGenerator;
import org.springmodules.cache.key.HashCodeCacheKey;
import org.springmodules.cache.key.HashCodeCalculator;
import org.springmodules.cache.util.Reflections;

import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * Custom hashcodekey generator which creates a hashcodekey out of 
 * methodname and arguments. It takes care that some arguments might
 * be hibernate proxies and safely reloads them to avoid 
 * nasty hibernate exceptions. 
 * 
 * 
 * <p>hentner: 
 * We have to assure that this code generates unique keys! 
 * The <code>hashCode</code> function doesn't guarantee to generate
 * unique keys. 
 * 
 * <p>
 * Take the String objects <code>H3ZL30S</code> and 
 * <code>H3ZL314</code> and both will return the same 
 * <code>hashCode</code>. It's obvious, as its not possible to 
 * relate every string object to an <code>int</code> value. 
 * 
 * <p>
 * If uniqueness is crucial, don't cache with the current 
 * implementation.
 * 
 * TODO the <code>generateKey</code> function does not return a unique key. 
 * 
 * 
 * @author ggruber, hentner
 *
 */
public class HashCodeCacheKeyGeneratorWrapper implements CacheKeyGenerator  {

	private CacheHibernateUtils cacheHibernateUtils;
	private Logger logger = Logger.getLogger(this.getClass());
	
	
	@Override
	public Serializable generateKey(MethodInvocation methodInvocation) {
		HashCodeCalculator hashCodeCalculator = new HashCodeCalculator();
	    Method method = methodInvocation.getMethod();
	    hashCodeCalculator.append(method.getName().hashCode());
	    
	    Object[] methodArguments = methodInvocation.getArguments();
	    
	    if (methodArguments != null) {
	      int methodArgumentCount = methodArguments.length;

	      for (int i = 0; i < methodArgumentCount; i++) {
	        Object methodArgument = methodArguments[i];
	    	
	        // doublecheck that argument is no proxy!!!!
	        if (methodArgument != null &&
	        		(AopUtils.isAopProxy(methodArgument) || methodArgument.getClass().getName().indexOf("$$") > 0) &&
	        		methodArgument instanceof PersistentObject && this.cacheHibernateUtils != null)
	        {	// reload object
	        	methodArgument = this.cacheHibernateUtils.reloadObject(methodArgument);
	        }
	        
	        
	        int hash = Reflections.reflectionHashCode(methodArgument);
	        hashCodeCalculator.append(hash);
	      }
	    }
	    
	    long checkSum = hashCodeCalculator.getCheckSum();
	    int hashCode = hashCodeCalculator.getHashCode();
	    Serializable cacheKey = new HashCodeCacheKey(checkSum, hashCode);
	    
	    
		
	    Object[] o = methodInvocation.getArguments();
		if (logger.isTraceEnabled()) {
			logger.trace("Generated hashcode for method " + methodInvocation.getMethod().getName() + ": " + cacheKey);
			for (int i=0; i< o.length; i++) 
				logger.trace("argument[" + i+"] : " + o[i]);
		}
		return cacheKey;
	}
	

	public void setCacheHibernateUtils(CacheHibernateUtils cacheHibernateUtils) {
		this.cacheHibernateUtils = cacheHibernateUtils;
	}

}
