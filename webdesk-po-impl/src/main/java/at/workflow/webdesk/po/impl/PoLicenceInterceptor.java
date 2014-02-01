package at.workflow.webdesk.po.impl;

import java.beans.Expression;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.cocoon.configuration.Settings;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import at.workflow.tools.XMLTools;
import at.workflow.tools.mail.Mail;
import at.workflow.tools.mail.MailService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoLicenceActionService;
import at.workflow.webdesk.po.PoLicenceInterceptorInterface;
import at.workflow.webdesk.po.PoScriptingService;
import at.workflow.webdesk.po.impl.licence.PoLicenceCheckOfAction;
import at.workflow.webdesk.po.licence.LicenceHelper;
import at.workflow.webdesk.po.licence.LicenceHelper.LicenceInfo;
import at.workflow.webdesk.po.licence.LicenceReader;
import at.workflow.webdesk.po.licence.LicenceViolationException;
import at.workflow.webdesk.po.model.LicenceDefinition;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.tools.config.StartupPropertyProvider;

/**
 * This class intercepts almost all methods called inside the po-package. (others can be included as well). 
 * If it seems to be necessary, it checks whether the amount of allowed assignments is transgressed or not. If 
 * too many users are contained and so on. 
 * 
 *
 * @author hentner, ggruber
 */
public class PoLicenceInterceptor extends HibernateDaoSupport implements MethodInterceptor, ApplicationContextAware, PoLicenceInterceptorInterface {
	

	/** available LicenceInfoMails **/
	private enum LicenceInfoMailType { THRESHOLD_REACHED_MAIL, LICENCE_VIOLATION_MAIL };
	
	/** threshold Factor, used to define the amount of used licences to be reached, until Treshold_Reached_mail is sent **/
	private double tresholdFactor = 0.95;
	
	/** was Mail to LicenceManager about reaching the licence threshold already sent? **/
	private boolean sendMailToLicenceManagerIfThresholdReached = true;
	
	/** the mailaddress of the licence Manager **/
	private String licenceManagerEmail;
	
	/** the timestamp of the treshold mail to the licenceManager **/
	private Date licenceThresholdMailTimeStamp;
	
	/** the timestamp of the violation mail to the licenceManager **/
	private Date licenceViolationMailTimeStamp;
	
	/** is licencechecking enabled at all? **/
	private boolean licenceCheckEnabled = true;
	
	/** List of all licenceDefinitions contained in the code **/
    private List<LicenceDefinition> licenceDefinitions;
    
    protected final Logger logger = Logger.getLogger(getClass().getName());
    
    private Map<String, Integer> allowedActions = Collections.synchronizedMap(new HashMap<String, Integer>());
    
    private Map<String, PoLicenceCheckOfAction> methodsToCheck;
    
    private PoLicenceActionService licenceActionService;

    private ApplicationContext appCtx;
    
    private Settings settings;
    
    /** only if true, an actual exception will be thrown, can be set via properties */
    private boolean throwExceptionOnViolation = true;
    
    /** reference ot the licencehelper **/
    private LicenceHelper licenceHelper;
    
    /** reference to the licenceReader **/
    private LicenceReader licenceReader;
    
    /** reference to the mailService **/
    private MailService mailService;
    
    /** reference to the languageService **/
    private PoLanguageService languageService; 
    
    /** reference to the scriptingService needed for velocity manipulations */
    private PoScriptingService scriptingService;
    
    public void setLicenceReader(LicenceReader licenceReader) {
		this.licenceReader = licenceReader;
	}

	public void setLicenceHelper(LicenceHelper licenceHelper) {
		this.licenceHelper = licenceHelper;
	}

    public void setLicenceActionService(PoLicenceActionService licenceActionService) {
        this.licenceActionService = licenceActionService ;
    }

    public Map<String, Integer> getAllowedActions() {
        return allowedActions;
    }

    public void setLicenceDefinitions(List<LicenceDefinition> licenceDefinitions) {
        this.licenceDefinitions = licenceDefinitions;
    }

    /* (non-Javadoc)
	 * @see at.workflow.webdesk.po.impl.PoLicenceInterceptorAPI#initAllowedActions()
	 */
    @Override
	public void initAllowedActions() {
    	if (licenceDefinitions==null) 
    		return;
    	
    	// reset mail timestamp
    	licenceThresholdMailTimeStamp = null;
    	licenceViolationMailTimeStamp = null;
    	
    	allowedActions = Collections.synchronizedMap(new HashMap<String, Integer>());
    	
        for (LicenceDefinition licDef : licenceDefinitions) {
            allowedActions = licenceHelper.getAllowedActions(licDef, allowedActions);
        }
        
        // a little security hole
        // but nice for developing...
        // property: licenceCheckDisabled=true
		if ("true".equals(settings.getProperty(StartupPropertyProvider.WEBDESK_LICENCE_CHECK_DISABLED)))
			licenceCheckEnabled=false;

    }

    
    /**
     * This method is very costly so we should avoid to call it where possible.
     * Check all permissions of the passed action and determine if some licence is violated.
     * 
     * @param action
     */
    private void checkLicence(PoAction action) {
    	
    	assert(action!=null && action.getActionType()==PoConstants.ACTION_TYPE_ACTION);
    	
        long startTime = System.currentTimeMillis();
        
    	final String actionName = action.getName();
        
        if ( allowedActions.get(actionName)!=null && !action.isDetached()) {
        	
        	int maxLicenced = allowedActions.get(actionName).intValue();
        	int count = licenceHelper.getNoOfUsersWithPermissionForActionAndChildConfigs(action);
        
            if ( count> maxLicenced) {
            	
            	PoAction actV = retrieveActionViolatingTheLicence(action, maxLicenced);
            	String actionNameViolatingLicence = actionName + ".act";
            	if (actV!=null) {
            		actionNameViolatingLicence = actV.getName() + "." + (actV.getActionType()==PoConstants.ACTION_TYPE_ACTION ? PoConstants.ACTION_POSTFIX_ACTION : PoConstants.ACTION_POSTFIX_CONFIG);
            	}
            	
            	String licMsg = count + " individual Webdesk Users have action "
            			+ actionNameViolatingLicence 
            			+ (actV==null ? " (or one of its child configs)":"")
            			+ " assigned. " + maxLicenced + " are allowed according to the licence!";
            	
            	if (logger.isTraceEnabled())
            		logger.trace(licMsg);
            	
            	sendLicenceViolationInfoMail(true);
            	
            	if (throwExceptionOnViolation) 
            		throw new LicenceViolationException(LicenceViolationException.MAX_AMOUNT_TRANSGRESSED + " --> " + licMsg);
            	else 
            		logger.warn(LicenceViolationException.MAX_AMOUNT_TRANSGRESSED + " --> " + licMsg);

            }
        }
        
        if (logger.isTraceEnabled()) 
        	logger.trace("Licence check needed " + (System.currentTimeMillis()-startTime) + " ms.");
    }
    
    private PoAction retrieveActionViolatingTheLicence(PoAction action, int maxLicenced) {
    	
		if (maxLicenced < licenceHelper.getNoOfUsersWithPermissionForAction(action))
			return action;
    	
    	for (PoAction config : action.getChilds()) {
    		if (maxLicenced < licenceHelper.getNoOfUsersWithPermissionForAction(config))
    			return config;
    	}
    	
    	
    	return null;
    }
    

    
    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    @Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    	if (licenceCheckEnabled) {
    		return invokeWithLicenceCheck(methodInvocation);
    	} else {
    		return methodInvocation.proceed();
    	}
    	
    }
    

    /**
     * invocation with licence check
     * 
     * This method is invoked before (finally:after) the originally called method. 
     * 
     * 
     * TransactionManager 
     *  »    «
     * This one (» maybe rollback)
     *  »    «
     * Service
     *  »    «
     * DAO  
     * 
     * @param methodInvocation
     * @return
     * @throws Throwable
     */
    private Object invokeWithLicenceCheck(MethodInvocation methodInvocation) throws Throwable {
    	
    	// per default we assume that a licenceCheck is necessary
        Boolean checkLicence = true;
        String methodName = methodInvocation.getMethod().getName();
        
        try {
            // if one of our classes is called
            if ( isCallFromWebdeskNameSpace(methodInvocation) ) {
                // if the method is contained in one of our classes
                if (methodsToCheck.containsKey(methodName)) {
            		checkLicence = true;
                    PoLicenceCheckOfAction licenceCheckOfAction = methodsToCheck.get(methodName);
                    // if a method that decides wheter a check is necessary or not is defined. If the execution fails
                    // this is considered true.
                    if (licenceCheckOfAction.getMethodNameOfCheckNec()!=null && !licenceCheckOfAction.getMethodNameOfCheckNec().equals("")) {
                    	// call a previously defined method which decides if a check is neccessary or not
                        checkLicence = isLicenceCheckNecessary(	methodInvocation, licenceCheckOfAction );
                    }
                }
            }
    		if (logger.isTraceEnabled() && checkLicence == true)
    			logger.trace("Invoke Method= " + methodName + " with licence check enabled");
            
    		// normal proceeding -> call original method
            Object o=  methodInvocation.proceed();
            return o;
            
        } finally {
            // if the licence should be checked!
            if (checkLicence && licenceDefinitions!=null && isCallFromWebdeskNameSpace(methodInvocation)) {
            	
            	// if a sql should be executed -> eg. save person: from PoPerson
            	// these are the volume checks -> 
            	for (LicenceDefinition licDef : licenceDefinitions) {
            		// check amounts if methodName is referenced in LicenceDefinition
            		// as trigger for volume licence checking
            		if ( licDef.getTriggerMethodsForVolumeLicencing().contains( methodName ) ) {
            			checkAmount(licDef);
            		}
            	}
            	
            	// a licenceCheck is defined.
            	// means an entry inside the map methodsToCheck has to exist with a reference
            	// to the PoLicenceCheckOfAction ConfigBean
            	PoLicenceCheckOfAction licenceCheckOfAction = methodsToCheck.get(methodName);
            	if (licenceCheckOfAction!=null) {

        			// check *ALL* actions
        			logger.debug(" *** FULL Licencecheck!");
        			
        			String query = "select count(*) from PoPerson where validto>current_timestamp()";
        			long amountOfPersons = executeCountHql(query);
        			
    				// we are only checking those actions
        			// which are defined as 'allowed' by the given licencedefinitions
    				long startTime = System.currentTimeMillis();
                    for ( String actionName : allowedActions.keySet() ) {
                        PoAction action = licenceActionService.findActionByName(actionName);
                        if (action!=null && !action.isDetached()) { 

                            int amount = 0;
                            if (allowedActions.containsKey(action.getName())) {
                            	amount = allowedActions.get(action.getName()).intValue();
                            }
                            
                            // only if the amount of persons is bigger
                            // we need a check...
                            if (amountOfPersons >= amount) {
                            	logger.trace("check licence for action=" + action.getName());
                                checkLicence(action);
                            }
                        }
                    }
                    if (logger.isTraceEnabled())
                    	logger.trace("Licence Check of all allowedActions needed " + (System.currentTimeMillis()-startTime) + "ms.");
            	}
            } 
        }   
    }
    
    private long executeCountHql(String hql) {
    	if (logger.isDebugEnabled())
    		logger.debug("execute query for licencecheck: " + hql);
    	
    	Query query = getHibernateTemplate().getSessionFactory().getCurrentSession().createQuery(hql);
    	Long res = (Long) query.list().get(0);

    	if (logger.isDebugEnabled())
    		logger.debug("query returned: " + res.longValue());
    	
    	return res.longValue();
    }

	private Boolean isLicenceCheckNecessary(MethodInvocation methodInvocation,
			PoLicenceCheckOfAction licenceCheckOfAction) throws Exception {
		
		Boolean checkLicence;
		String checkMethodName = licenceCheckOfAction.getMethodNameOfCheckNec();
		Object checkBean = appCtx.getBean(licenceCheckOfAction.getBeanNameOfCheckNec());
		Expression e = new Expression(checkBean,checkMethodName,methodInvocation.getArguments());
		checkLicence = (Boolean) e.getValue();
		return checkLicence;
	}

	private boolean isCallFromWebdeskNameSpace(MethodInvocation methodInvocation) {
		String className = "";
		try {
			// seems that there are problems with custom java jobs (maybe, not definitely the cause)
			className =  methodInvocation.getMethod().getDeclaringClass().getName();
		} catch (Exception e) {
			logger.warn("Could not determine class of method. " + methodInvocation.getMethod());
		}
		return className.indexOf("at.workflow.webdesk.") > -1;
	}

    
    /**
     * Checks Volume amounts of licencing
     * f.i. number of persons which can be managed inside webdesk with
     * the given licence.
     * 
     * @param o
     */
	private void checkAmount(LicenceDefinition ld) {
        long res = 0;
        res = licenceHelper.getCurrentlyUsedVolumeAmount(ld);
        
        if (res>ld.getCurrentlyLicencedAmount()) {
        	String licMsg = "Tried to create " + res + " objects, but only " + ld.getCurrentlyLicencedAmount() + " are allowed according to installed licence '" + ld.getName() + "'";
        	
        	sendLicenceViolationInfoMail(false);
        	
        	if (throwExceptionOnViolation)
        		throw new LicenceViolationException(LicenceViolationException.MAX_AMOUNT_TRANSGRESSED + " --> " + licMsg);
        	else 
        		logger.warn(LicenceViolationException.MAX_AMOUNT_TRANSGRESSED + " --> " + licMsg);
        }
        
        checkIfMailTresholdReached(res, ld);
    }

	private void checkIfMailTresholdReached(long res, LicenceDefinition ld) {
		
		double treshold = ld.getCurrentlyLicencedAmount() * tresholdFactor;
		
		if ( res > treshold  && sendMailToLicenceManagerIfThresholdReached ) {
			// current licence consumation has reached treshold
			// we send a mail if enabled
			sendLicenceInfoMail();
		}
		
	}
	
	private int diffInHours(Date date1, Date date2) {
		if (date2 != null) {
			long diffInMillis = date1.getTime() - date2.getTime();
			return new Long(diffInMillis / 1000 / 60 / 60).intValue();
		} else
			return 25;
	}
	
	
	private void sendLicenceInfoMail() {
		if (diffInHours(new Date(), licenceThresholdMailTimeStamp)>24 && licenceManagerDefined()) {
			try {
				sendMail(LicenceInfoMailType.THRESHOLD_REACHED_MAIL, false);
				licenceThresholdMailTimeStamp = new Date();
			} catch (Exception e) {
				logger.warn("problems sending the infomail...",e);
			}
		}
	}
	
	private void sendLicenceViolationInfoMail(boolean retrieveInfosForAllLicenceDefinitions) {
		if (diffInHours(new Date(), licenceViolationMailTimeStamp)>24 && licenceManagerDefined()) {
			try {
				sendMail(LicenceInfoMailType.LICENCE_VIOLATION_MAIL, retrieveInfosForAllLicenceDefinitions);
				licenceViolationMailTimeStamp = new Date();
			} catch (Exception e) {
				logger.warn("problems sending the infomail...",e);
			}
		}
	}
	
	private boolean licenceManagerDefined() {
		return licenceManagerEmail!=null && !"".equals(licenceManagerEmail.trim());
	}

	private void sendMail(LicenceInfoMailType mailType, boolean retrieveInfosForAllLicenceDefinitions) {
		Mail infoMail = new Mail();
		infoMail.setTo(licenceManagerEmail);
		List<String> cc= new ArrayList<String>();
		cc.add("licencing@workflow.at");
		infoMail.setCopyTo(cc);
		
		infoMail.setSubject("Webdesk EWP Licence Information [" + licenceReader.getCompany() + "]");
		
		String mailXmlFileName = "LicenceInfoMailTresholdReached";
		if (mailType.equals(LicenceInfoMailType.LICENCE_VIOLATION_MAIL)) {
			mailXmlFileName = "LicenceInfoMailViolation";
		}
		
		if (languageService.findDefaultLanguage()!=null && "de".equals(languageService.findDefaultLanguage().getCode())) {
			mailXmlFileName = mailXmlFileName + "_de";
		}
		
		String mailText = null;
		Resource ress = new ClassPathResource("at/workflow/webdesk/po/impl/licence/" + mailXmlFileName + ".xml");
		SAXBuilder myBuilder = new SAXBuilder();
		try {
			Document doc = myBuilder.build(ress.getInputStream());
			mailText = XMLTools.createStringFromW3cDoc(XMLTools.convertToW3cDoc(doc));
			
			List<LicenceInfo> licenceInfos = licenceHelper.getLicenceInfo(retrieveInfosForAllLicenceDefinitions);
			Map<String, Object> ctx = new HashMap<String, Object>();
			ctx.put("company", licenceReader.getCompany());
			ctx.put("companyDesc", licenceReader.getCompanyDesc());
			ctx.put("licenceInfos", licenceInfos);
			ctx.put("languageService", languageService);
			ctx.put("sdf", new SimpleDateFormat("dd.MM.yyyy"));
			mailText = scriptingService.velocitySubstitution(mailText, ctx);
			
		} catch (Exception e) {
			logger.warn("problem loading the mail content...", e);
		}
				
		infoMail.setMessage(mailText);
		
		mailService.sendMail(infoMail);
	}

	/**
	 * Iterates over *EVERY* single action of the webdesk system and
	 * checks whether its permission assignments violate the given licences.
	 * VERY EXPENSIVE method!
	 */
    public void checkAllActions() {
        long startTime = System.currentTimeMillis();
        Iterator<String> i = allowedActions.keySet().iterator();
        while (i.hasNext()) {
            PoAction action = licenceActionService.findActionByName(i.next());
            checkLicence(action);
        }
        logger.info("Licence Check of all allowedActions needed " + (System.currentTimeMillis()-startTime) + "ms.");
    }
    
    
    @Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        this.appCtx = arg0;
        
    }

	public void setMethodsToCheck(Map<String, PoLicenceCheckOfAction> methodsToCheck) {
		this.methodsToCheck = methodsToCheck;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public void setLicenceManagerEmail(String licenceManagerEmail) {
		this.licenceManagerEmail = licenceManagerEmail;
	}

	public void setTresholdFactor(double tresholdFactor) {
		this.tresholdFactor = tresholdFactor;
	}

	public void setSendMailToLicenceManagerIfTresholdReached(
			boolean sendMailToLicenceManagerIfTresholdReached) {
		this.sendMailToLicenceManagerIfThresholdReached = sendMailToLicenceManagerIfTresholdReached;
	}

	public void setScriptingService(PoScriptingService scriptingService) {
		this.scriptingService = scriptingService;
	}

	public void setLanguageService(PoLanguageService languageService) {
		this.languageService = languageService;
	}

	public void setThrowExceptionOnViolation(boolean throwExceptionOnViolation) {
		this.throwExceptionOnViolation = throwExceptionOnViolation;
	}
}
