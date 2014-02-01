/*
 * Created on 23.06.2005
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.impl;

import java.io.Serializable;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.workflow.webdesk.po.impl.daos.PoAuditLogDAOImpl;

/**
 * @author hentner
 *
 */
public class PoAuditLogInterceptor  implements MethodInterceptor, Serializable{


	  private final Log logger = LogFactory.getLog(getClass());
	  private PoAuditLogDAOImpl auditLogDao;
	  private List functionConstraints;
	  
	  
	  public void setFunctionConstraints(List l) {
	  		this.functionConstraints = l;
	  }
	  
	  
	  public void setAuditLogDao(PoAuditLogDAOImpl auditLogDao) {
	  		this.auditLogDao = auditLogDao;
	  }
	  

	  public Object invoke(MethodInvocation methodInvocation) throws Throwable
	  {		
         //System.out.println("auditLogInterceptor invoked");
	  	 return methodInvocation.proceed();	
	  	 /*
	  		long startTime = System.currentTimeMillis();
	  		// delivers the classes of the arguments
		  	Object[] parameterClasses = methodInvocation.getMethod().getParameterTypes();
		  	// delivers the arguments
		  	Object[] parameter = methodInvocation.getArguments();
		  	String uid = "";
		  	Object original = null;
	  		try
			{
		  		// Only methods that store something are considered.  		
		  		if (!this.functionConstraints.contains(methodInvocation.getMethod().getName().substring(0,4))) {
		  	  		return methodInvocation.proceed();
		  	  	}
		  		logger.info("Beginning method: " + methodInvocation.getMethod().getDeclaringClass() + "::" + methodInvocation.getMethod().getName());
		  	  	// For now it is supposed that the first argument is the 
			  	// object that will be saved. 
		  		Method m;
				// invoke the getUID method of the given object
				try {
					m = parameter[0].getClass().getMethod("getUID",null);
					Object o_uid = m.invoke(parameter[0],null);
					if (o_uid!=null)
						uid = o_uid.toString();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}
			  	if (parameter.length>0) {  	
			  		original = auditLogDao.getOriginal(parameter[0]);
			  		// Read out the attributes that should be historized
			  	} 
		  		// change everything in the database
		  		Object retVal = methodInvocation.proceed();
		  		return retVal;
			}	
		    finally
		    {	
//				check if the fields that should be logged has changed.
		  		if (original!=null && !uid.equals("")) {
		  			//System.out.println("name: " + parameter[0].getClass().getName());
			    	Iterator logValues =  auditLogDao.getAuditLogDefinitionByObjectName(parameter[0].getClass().getName());
			    	while (logValues.hasNext()) {
			  			String item = (String) logValues.next();
			  			auditLogDao.logIfNotEqual(original, parameter[0], item, uid);
			  		}
		  		}
		    	//logger.info("Ending method: "  + methodInvocation.getMethod().getDeclaringClass() + "::" + methodInvocation.getMethod().getName());
		    	logger.info("Method invocation time: " + (System.currentTimeMillis() - startTime) + " msecs.");
		    	// Log data that has changed to database
		    }
		    
		*/
	 }


	public List getFunctionConstraints() {
		return functionConstraints;
	}
}
