package at.workflow.webdesk.po.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import at.workflow.webdesk.po.FilterService;
import at.workflow.webdesk.po.model.FilterCriteria;

public class FilterServiceImpl implements FilterService, ApplicationContextAware {
	
	public final Logger logger = Logger.getLogger(this.getClass());

	ApplicationContext appCtx;
	
	/*
	 * (non-Javadoc)
	 * @see at.workflow.webdesk.tm.TmFilterService#appendHQLFiltersToQuery(java.lang.String, java.util.List)
	 */
	@Override
	public String generateHqlFromFilters(List<FilterCriteria> filters) {
		String ret=new String();
		for(Iterator<FilterCriteria> itr=filters.iterator(); itr.hasNext(); ) {
			FilterCriteria filter=itr.next();
			if(filter.type.equals("hql")) {
				ret += (ret.length()>0?" and (":"(") + filter.expression + ")";
			}
		}
		if(ret.length()>0)
			return ret;
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.workflow.webdesk.tm.TmFilterService#applyScriptExpressionFilters(java.util.List, java.util.List)
	 */
	@Override
	public void removeObjectsNotMatchingExpression(List<?> objs, List<FilterCriteria> filters) {
		//prepare the Variables
		String script=new String();
		for(FilterCriteria filter : filters) {
			if(filter.type.equals("script")) {
				script += script.length()>0?" && (":"(" + filter.expression + ")";
			}
		}
		if (script.length()>0) {
			//prepare the Javascript-Engine
			Context cx = Context.enter();  
	    	Scriptable scope = cx.initStandardObjects();
	    	// in the script we will work with several Services
	    	
	    	Map<String, Object> serviceMap = findAllServices();
	    	for (String serviceName : serviceMap.keySet()) {
	    		ScriptableObject.putProperty(scope, serviceName, Context.javaToJS(serviceMap.get(serviceName), scope));
	    	}
	    	
//	    	ScriptableObject.putProperty(scope, "TmCurrencyService", Context.javaToJS(currencyService, scope));
//	    	ScriptableObject.putProperty(scope, "TmDefinitionService", Context.javaToJS(definitionService, scope));
//	    	ScriptableObject.putProperty(scope, "TmFleetService", Context.javaToJS(fleetService, scope));
//	    	ScriptableObject.putProperty(scope, "TmTravelService", Context.javaToJS(travelService, scope));
//	    	ScriptableObject.putProperty(scope, "TmTypeService", Context.javaToJS(typeService, scope));

	    	// and now lets evaluate the script for every travel object
	    	Boolean result;
	    	for(Iterator<?> itr=objs.iterator(); itr.hasNext(); ) {
	    		Object obj = itr.next();
	    		
	    		// *NOTE* bloody hack for backwards compatibility
	    		// WILL BE REMOVED IN THE FUTURE!!!
		    	ScriptableObject.putProperty(scope, "travel", Context.javaToJS(obj, scope));
		    	
		    	ScriptableObject.putProperty(scope, "object", Context.javaToJS(obj, scope));
		    	ScriptableObject.putProperty(scope, "row", Context.javaToJS(obj, scope));
		    	
		    	try {
			    	result=(Boolean)cx.evaluateString(scope, script, "no comment", 0, null);
			    	// if the result is not true it will be fitered out (removed from the collection)
			    	if(!result.booleanValue())
			    		itr.remove();
		    	} catch (Exception e) {
		    		this.logger.warn("could not evaluate script: " + script, e);
		    	}
	    	}
		}
		
	}
	
	private Map<String, Object> findAllServices() {
		
		Map<String, Object> services = new HashMap<String, Object>();
		
		String[] beanNames = this.appCtx.getBeanDefinitionNames();
		for(int i=0; i<beanNames.length;i++) {
			if (beanNames[i].endsWith("Service")) {
				services.put(beanNames[i], this.appCtx.getBean(beanNames[i]));
			}
		}
		return services;
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.appCtx = applicationContext;
	}

}
