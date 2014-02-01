package at.workflow.webdesk.po.impl.licence;

/**
 * Define a spring bean instance for each method which should be intercepted
 * by a check for a potential licence violation. Be aware that you also have to make
 * sure, that the service itself is intercepted by the PoLicenceInterceptor.
 * 
 * @author ggruber, hentner
 */
public class PoLicenceCheckOfAction {

	private String name;
	
	private String beanNameOfCheckNec;
	
	private String methodNameOfCheckNec;
	
	/**
	 * @return the name of the spring bean used to decide, whether licence violation check is necessary
	 */
	public String getBeanNameOfCheckNec() {
		return beanNameOfCheckNec;
	}

	public void setBeanNameOfCheckNec(String beanNameOfCheckNec) {
		this.beanNameOfCheckNec = beanNameOfCheckNec;
	}

	/**
	 * @return the name of the method used to decide, whether licence violation check is necessary 
	 */
	public String getMethodNameOfCheckNec() {
		return methodNameOfCheckNec;
	}

	public void setMethodNameOfCheckNec(String methodNameOfCheckNec) {
		this.methodNameOfCheckNec = methodNameOfCheckNec;
	}

	/**
	 * @return the name the method which should be checked for a potential licence violation 
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
}
