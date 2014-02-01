package at.workflow.webdesk.tools.cache;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInvocation;
import org.springmodules.cache.key.CacheKeyGenerator;

/**
 * This is a simple CacheKey Generator which takes the methodname and
 * the arguments of the called method, converts it to a string and returns it as
 * cachekey.
 * 
 * Makes sense for all methods with the form GetObjectByXXX(argument1)
 * where argument1 is unique.
 * 
 * the cachekey looks then like this:
 * <methodName>|<arg0>$<arg1>
 * 
 * @author ggruber
 *
 */
public class SimpleCacheKeyGenerator implements CacheKeyGenerator {

	@Override
	public Serializable generateKey(MethodInvocation arg0) {
		
		
		String ret = arg0.getMethod().getName();
		
		for(int i=0;i<arg0.getArguments().length;i++) {
			if (i==0)
				ret += "|";
			
			ret += nullSafeToString(arg0.getArguments()[i]);
			
			if (i<arg0.getArguments().length-1)
				ret += "$";
		}
		return ret;
	}
	
	private String nullSafeToString(Object obj) {
		if (obj==null)
			return "null";
		
		return obj.toString();
	}

}
