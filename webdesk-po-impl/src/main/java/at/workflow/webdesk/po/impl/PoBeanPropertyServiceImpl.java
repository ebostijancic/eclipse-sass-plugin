package at.workflow.webdesk.po.impl;

import java.beans.Expression;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import at.workflow.webdesk.po.ObjectTypeNotFoundException;
import at.workflow.webdesk.po.PoBeanPropertyService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoBeanPropertyDAO;
import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.po.model.PoBeanPropertyValue;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.po.model.PoRegistrationBean;

/**
 * @author hentner
 */
public class PoBeanPropertyServiceImpl implements PoBeanPropertyService, ApplicationContextAware {

	protected final Log logger = LogFactory.getLog(getClass());

	// daos
	private PoBeanPropertyDAO beanPropertyDAO;
	private PoModuleService moduleService;

	private List<PoBeanProperty> beanProperties = Collections.synchronizedList(new ArrayList<PoBeanProperty>());

	private Set<String> completeNegativeList = new HashSet<String>();

	private Set<String> negativeList;

	private ApplicationContext appCtx;

	private List<String> completePasswordList;

	// set of unique classnames
	private HashSet<String> classesNegativeList = new HashSet<String>();

	public void init() {
		String registrationClass = "at.workflow.webdesk.po.model.PoRegistrationBean";
		completeNegativeList.addAll(negativeList);
		// get PoRegistrationBean's and generate negative lists
		try {
			Class<?> regBean = Class.forName(registrationClass);
			@SuppressWarnings("rawtypes")
			Iterator itr = appCtx.getBeansOfType(regBean).keySet().iterator();
			while (itr.hasNext()) {
				String beanName = (String) itr.next();
				PoRegistrationBean bean = (PoRegistrationBean) appCtx.getBean(beanName);
				completeNegativeList.addAll(bean.getBeanPropertyNegativeList());
				completeNegativeList.add(beanName + ".*");
				completeNegativeList.addAll(bean.getBeanPropertyClassesNegativeList());
				classesNegativeList.addAll(bean.getBeanPropertyClassesNegativeList());
			}

		}
		catch (Exception e) {
			logger.warn("something went wrong while searching for spring beans of type = " + registrationClass, e);
		}
	}

	public void setNegativeList(Set<String> negativeList) {
		this.negativeList = negativeList;
	}

	public void setModuleService(PoModuleService moduleService) {
		this.moduleService = moduleService;
	}

	public void setBeanPropertyDAO(PoBeanPropertyDAO beanPropertyDAO) {
		this.beanPropertyDAO = beanPropertyDAO;
	}
	
	private boolean isCollectionASet(PoBeanProperty bp) {
		Object bean = appCtx.getBean(bp.getBeanName());
		String propertyName = bp.getPropertyName();
		String setterMethodName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1, propertyName.length());
		

		return hasMethod(bean.getClass(), setterMethodName, new Class[] { HashSet.class }) ||
			hasMethod(bean.getClass(), setterMethodName, new Class[] { Set.class });
	}
	
	private boolean hasMethod(Class<?> clazz, String methodName, Class<?>[] params) {
		Method mth = null;
		try {
			mth = clazz.getDeclaredMethod( methodName, params );
		} catch (SecurityException e) {
			return false;
		} catch (NoSuchMethodException e) {
			return false;
		}
		
		return mth!=null;
		
	}

	private int getType(PoBeanProperty beanProperty) throws ObjectTypeNotFoundException {
		String beanName = beanProperty.getBeanName();
		String propertyName = beanProperty.getPropertyName();

		Object bean = "";
		try {
			bean = appCtx.getBean(beanName);
		}
		catch (Exception e) {
			logger.warn("couldn't find bean with name " + beanName + ", so delete the persisted entry!");
			if (beanName != null && beanProperty.getUID() != null) {
				detachOrDeleteBeanProperty(beanProperty);
			}
		}

		Class<?> beanClass = bean.getClass();
		Class<?> type = getTypeFromField(propertyName, beanClass);

		if (type == null)	{
			type = getTypeFromGetter(propertyName, bean);
		}
		
		if (type == null)	{
			type = getTypeFromSetter(propertyName, bean);
		}

		if (type != null) {
			final String typeName = type.getName();
			if (typeName.equals("java.lang.Integer") || typeName.equals("int") || typeName.equals("java.lang.Long") || typeName.equals("long"))
				return PoConstants.INTEGER;
			
			if (typeName.equals("java.lang.Boolean") || typeName.equals("boolean"))
				return PoConstants.BOOLEAN;
			
			if (typeName.equals("java.lang.String"))
				return PoConstants.STRING;
			
			if (typeName.equals("double") || typeName.equals("java.lang.Double"))
				return PoConstants.DOUBLE;
			
			if (typeName.equals("java.util.List") ||
							typeName.equals("java.util.Set") ||
							typeName.equals("java.util.ArrayList") ||
							typeName.equals("java.util.HashSet") ||
							typeName.equals("at.workflow.webdesk.tools.injectionHelper.InjectionList")) // list
				// objects are considered as collection of strings
				return PoConstants.STRING;
		}
		else	{
			logger.warn("Could not determine type of '"+propertyName+"' in class (and superclasses of) "+beanClass);
		}

		if (beanProperty.getUID() != null) {
			deleteBeanProperty(beanProperty);
			logger.warn("Type of " + beanName + "." + propertyName + " evaluated to '" + type + "' which is not applicable. [ev. field does not exist. will be deleted.]");
		}

		// TODO fri_2011-08-10: Avoid stack traces at startup! Maybe this is normal in upgrade cases?
		throw new ObjectTypeNotFoundException("Could not determine type for "+propertyName+" of bean "+beanName+", type is "+type);
	}

	private Class<?> getTypeFromField(String propertyName, Class<?> beanClass) {
		Class<?> o = beanClass;
		
		Class<?> type = null;
		
		while (type == null) {
			try {
				type = o.getDeclaredField(propertyName).getType();
			}
			catch (NoSuchFieldException nsfe) {
				logger.debug("Did not find field  '" + propertyName + "' in class " + o);
			}
			
			if (o.getSuperclass() != null && ! o.getSuperclass().getName().equals("java.lang.Object"))
				o = o.getSuperclass();
			else
				break;
		}
		return type;
	}

	private Class<?> getTypeFromGetter(String propertyName, Object bean) {
		
		Class<?> type = null;
		
		try {	// maybe the getter works
			Expression e = new Expression(bean, "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1, propertyName.length()), null);
			e.execute();
			type = e.getValue().getClass();
		}
		catch (Exception exp) {
			if (logger.isDebugEnabled())
				logger.debug(exp, exp);
		}
		return type;
	}
	
	private Class<?> getTypeFromSetter(String propertyName, Class<?> clazz) {
		String setterName = getSetterMethodName(propertyName);
		Method method = ReflectionUtils.findMethod(clazz, setterName, null);
		
		if (method!=null && method.getParameterTypes().length == 1) {
			return method.getParameterTypes()[0];
		}
		return null;
	}
	
	private String getSetterMethodName(String propertyName) {
		return "set" + at.workflow.webdesk.tools.ReflectionUtils.getFirstCharUpperCasePropertyName(propertyName);
	}
	
	private Class<?> getTypeFromSetter(String propertyName, Object bean) {
		
		Class<?> type = null;
		
		try {	// look for a Method
			
			String setterName = getSetterMethodName(propertyName);
			Class<?> parameterType = getTypeFromSetter(propertyName, bean.getClass());
			
			if (parameterType != null) {
				return parameterType;
			}
			
			if (AopUtils.isAopProxy(bean)) {
			
				Method method = ReflectionUtils.findMethod(AopUtils.getTargetClass(bean), setterName, null);
				
				if (method != null && method.getParameterTypes().length == 1) {
					return method.getParameterTypes()[0];
				}	
			}
		}
		catch (Exception exp) {
			if (logger.isDebugEnabled())
				logger.debug(exp, exp);
		}
		return type;
	}


	private PoBeanProperty findBeanPropertyByKey(PoBeanProperty bp) {
		return beanPropertyDAO.findBeanPropertyByKey(bp);
	}

	@Override
	public PoBeanProperty findBeanPropertyByKey(String beanName, String property) {
		return beanPropertyDAO.findBeanPropertyByKey(beanName, property);
	}

	@Override
	public PoBeanProperty getBeanProperty(String id) {
		return beanPropertyDAO.get(id);
	}

	@Override
	public void deleteBeanProperty(PoBeanProperty b) {
		beanPropertyDAO.delete(b);
	}

	@Override
	public void saveBeanProperty(PoBeanProperty b) {
		// set listindex of property values having none
		Collection<PoBeanPropertyValue> entries = b.getEntries();

		if (entries != null && !entries.isEmpty()) {
			boolean foundOneZero = false;

			for (PoBeanPropertyValue entry : entries) {
				if (entry.getListIndex() == 0) { // is zero
					if (foundOneZero == false) { // first zero found
						foundOneZero = true;
					}
					else { // another zero found, seems to need correction
						int counter = 0;
						for (PoBeanPropertyValue val : entries) {
							val.setListIndex(counter);
							// TODO fri_2010-11-17: what about the lost rankings?
							counter++;
						}
						break;
					}
				}
			}
		}

		// try to set the Module
		if (b.getModule() == null) {
			b.setModule(getModuleForBeanProperty(b));
		}

		beanPropertyDAO.save(b);
	}

	private PoModule getModuleForBeanProperty(PoBeanProperty b) {
		PoModule ret = moduleService.tryToExtractModuleFromBeanName(b.getBeanName());
		PoModule defaultModule = moduleService.getModuleByName("po");

		if (ret == null)
			ret = moduleService.tryToExtractModuleFromClassName(b.getClassName());

		if (ret == null)
			ret = defaultModule;

		return ret;
	}
	
	@Override
	public void writeBeanPropertiesToDb() {
		writeBeanPropertiesToDb(false);
	}

	public void writeBeanPropertiesToDb(boolean ignoreDatabaseValues) {
		for (PoBeanProperty bp : beanProperties) {
			PoBeanProperty oldBp = findBeanPropertyByKey(bp);

			if (isPropertyAllowedForEditing(bp)) {
				try {
					bp.setPassword(isPropertyAPassword(bp));

					if (oldBp == null) {
						saveBeanProperty(bp);
					}
					else {
						// update the class if it has changed.
						if (bp.getClassName() != null)
							oldBp.setClassName(bp.getClassName());

						// update password flag
						oldBp.setPassword( bp.isPassword() );

						// update module, detached and type
						oldBp.setModule(getModuleForBeanProperty(bp));
						oldBp.setDetached(false);
						oldBp.setType(bp.getType());
						
						if (ignoreDatabaseValues==true) {
							// remove database version
							deleteBeanProperty(oldBp);
							
							// save new version according to default settings
							saveBeanProperty(bp);
						} else {
							saveBeanProperty(oldBp);
						}

					}
				} catch (Exception e) {
					logger.error(e, e);
				}
			}
			else {
				// remove right away
				if (oldBp != null)
					deleteBeanProperty(oldBp);
			}
		}
	}

	public List<PoBeanProperty> getBeans() {
		// beans are originally filled by the application context files
		// now we have to read those
		return beanProperties;
	}

	/**
	 * TODO This is a public interface method just for PoStartupImpl to be able to fill in
	 * beans read by beanPropertyCollector. When we would inject the beanPropertyCollector
	 * into this class here, the encapsulation of the beanProperties Collection would be stronger.
	 */
	@Override
	public void registerBeanProperties(List<PoBeanProperty> argBeanProps) {
		this.beanProperties.clear();
		this.beanProperties.addAll(argBeanProps);
	}

	@Override
	public void injectAll() {
		readBeanProperties(); // reads all bean-properties from the database
		injectAll(beanProperties);
		logger.info("Injection of Bean properties finished.");
	}

	private void detachOrDeleteBeanProperty(PoBeanProperty b) {
		if (b.getModule().isDetached()) {
			b.setDetached(true);
			saveBeanProperty(b);
		}
		else {
			deleteBeanProperty(b);
		}
	}

	@Override
	public void injectAll(Collection<PoBeanProperty> l) {
		for (PoBeanProperty b : l) {
			if (false == b.isDetached()) {
				try {
					// only inject if property is NOT in negative list
					if (b.getBeanName() != null && isPropertyAllowedForEditing(b))
						inject(b);
					else
						deleteBeanProperty(b);
				}
				catch (NoSuchMethodException e) {
					// property to invoke not found
					// delete from database
					logger.info("property to invoke -> + " + b + "[" + b.getType() + "] not found --> The entry will be detached or deleted!");
					detachOrDeleteBeanProperty(b);
				}
				catch (NoSuchBeanDefinitionException e) {
					// bean to inject not found
					// delete from database
					logger.info("bean to inject -> + " + b + " not found --> going to delete/detach it!");
					detachOrDeleteBeanProperty(b);
				}
				catch (Exception e) {
					logger.error("injectAll() problem: "+e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * checks if passed PoBeanProperty is allowed to be saved in the database
	 * and should be able to edit within the adminstration dialogs.
	 * 
	 * @param b <code>PoBeanProperty</code>
	 * @return true/false (boolean)
	 */
	@Override
	public boolean isPropertyAllowedForEditing(PoBeanProperty b) {
		if (isInList(b, getCompleteNegativeList()) == Boolean.TRUE)
			return false;

		if (classesNegativeList.contains(b.getClassName()))
			return false;

		return true;
	}

	/**
	 * checks if passed PoBeanProperty is allowed to be saved in the database
	 * and should be able to edit within the adminstration dialogs.
	 * 
	 * @param b <code>PoBeanProperty</code>
	 * @return true/false (boolean)
	 */
	private Boolean isPropertyAPassword(PoBeanProperty b) {
		return isInList(b, getCompletePasswordList());
	}

	private Boolean isInList(PoBeanProperty b, Collection<String> list) {
		// check for variants:
		// <propertyname> and <beanname>.<propertname>
		if (list.contains(b.getPropertyName()) || list.contains(b.getBeanName() + "." + b.getPropertyName()))
			return Boolean.TRUE;

		// check for variant
		// <beanname>.*
		if (list.contains(b.getBeanName() + ".*"))
			return Boolean.TRUE;

		return Boolean.FALSE;
	}

	public void setClassesNegativeList(HashSet<String> classesNegativeList) {
		this.classesNegativeList = classesNegativeList;
	}

	/**
	 * retrieve negativeLists of RegistrationBeans of modules.
	 * which are set in property BeanPropertyNegativeList
	 * 
	 * This function also has some initialisation features,
	 * we should move this code.
	 * 
	 * 
	 * @return List of complete Negative List
	 */
	public Set<String> getCompleteNegativeList() {
		return completeNegativeList;
	}

	/**
	 * retrieves the password list of RegistrationBeans of modules.
	 * which are set in property BeanPropertyNegativeList
	 * 
	 * @return List of complete Negative List
	 */
	private List<String> getCompletePasswordList() {
		if (completePasswordList != null && completePasswordList.size() > 0)
			return completePasswordList;

		List<String> ret = new ArrayList<String>();
		String registrationClass = "at.workflow.webdesk.po.model.PoRegistrationBean";
		try {
			Class<?> regBean = Class.forName(registrationClass);
			@SuppressWarnings("rawtypes")
			Iterator itr = appCtx.getBeansOfType(regBean).keySet().iterator();
			while (itr.hasNext()) {
				String beanName = (String) itr.next();
				PoRegistrationBean bean = (PoRegistrationBean) appCtx.getBean(beanName);
				ret.addAll(bean.getBeanPropertyPasswordList());
			}

		}
		catch (Exception e) {
			logger.warn("something went wrong while searching for spring beans of type = " + registrationClass, e);
		}
		completePasswordList = ret;
		return ret;

	}

	/**
	 * Injects the value(s) kept in the <code>entryList</code>
	 * into a Java field named by <code>propertyName</code> of a Spring bean named by <code>beanName</code>.
	 * @param beanProperty the bean property (from persistence) to inject into its memory representation.
	 * @throws Exception TODO summarize exception cases.
	 */
	private void inject(PoBeanProperty beanProperty) throws Exception {
		final int type;
		try {
			type = getType(beanProperty);
		}
		catch (Exception e) {	// the type could not be determined
			if (beanProperty.getUID() == null)
				logger.error("Could not detect type of bean-property "+beanProperty, e);
			else
				logger.warn("Could not detect type of bean-property "+beanProperty);
			return;
		}
			
		final Object theBean = checkIfBeanExistsOtherwiseDeleteProperty(beanProperty);
		if (theBean == null)
			return;

		// inject the property ( can be a list or a single value (of type string))
		final Collection<PoBeanPropertyValue> entryList = beanProperty.getEntries();
		
		if (entryList != null && entryList.size() > 0) {
			final Collection<Object> paramList = createParameterListForSetter(beanProperty, type);
			
			// only call setter if we have useful values to set (f.i. not null)
			if (paramList != null && paramList.size() > 0 && existsPropertySetter(beanProperty, theBean) != null) {
				if (isCollection(beanProperty, theBean)) {	// we have a list to set
					callSetter(beanProperty, theBean, paramList);
				}
				else if (paramList.size() == 1) {	// we have a scalar type, and list has exactly one element
					callSetter(beanProperty, theBean, paramList.toArray()[0]);
				}
			}
		}
	}
	
	private Class<?> existsPropertySetter(PoBeanProperty beanProperty, Object bean) {
		String propertyName = beanProperty.getPropertyName();
		return getTypeFromSetter(propertyName, bean);
	}
	
	private boolean isCollection(PoBeanProperty beanProperty, Object bean) {
		Class<?> parameterClass = existsPropertySetter(beanProperty, bean);
		assert parameterClass != null : "Make sure to NOT call this with a non-existing property!";
		return Collection.class.isAssignableFrom(parameterClass);
	}

	private Object checkIfBeanExistsOtherwiseDeleteProperty(
			PoBeanProperty beanProperty) {
		final String beanName = beanProperty.getBeanName();
		Object theBean = null;
		
		try {
			theBean = appCtx.getBean(beanName);
		}
		catch (Exception e) {
			logger.warn("Couldn't find bean with name " + beanName + ", so deleting or detaching it ...");
			if (beanName != null)
				detachOrDeleteBeanProperty(beanProperty);
			return null;
		}
		return theBean;
	}

	/** Calls the setter of the spring bean to inject the beanproperty value. */
	private void callSetter(PoBeanProperty beanProperty, Object theBean, Object parameter) {
		
		BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(theBean);
		
		// in case we have a primitive destination type and the parameter is null
		// do nothing, as it is not possible to set NULL on a primitive type.
		Class<?> propertyType = beanWrapper.getPropertyType(beanProperty.getPropertyName());
		if (propertyType.isPrimitive() && parameter==null)
			return;
		
		try {
			beanWrapper.setPropertyValue(beanProperty.getPropertyName(), parameter);
		} catch (Exception ex) {
			// TODO fri_2011-08-19: Avoid stack traces at startup! Maybe this is normal in upgrade cases?
			logger.error("something went wrong during injection: bean="+beanProperty.getBeanName()+", property="+beanProperty.getPropertyName()+", parameter="+parameter, ex);
		}
	}

	private Collection<Object> createParameterListForSetter(PoBeanProperty beanProperty, int type) throws Exception {
		Collection<Object> l = new ArrayList<Object>();
		
		if (isCollectionASet(beanProperty)) {
			l = new HashSet<Object>();
		}
		
		for (PoBeanPropertyValue bpv : beanProperty.getEntries()) {
			
			// ggruber 23.7.2012 -> should fix: 
			// Notes://Miraculix/C1256B300058B5FC/2883A98FAF7608A3C1256B300058B604/1CA0CB5D190DCE22C1257950005F91DA
			if (bpv.getProperty()==null) {
				l.add( null );
			} else {
				
				switch (type) {
				case PoConstants.INTEGER:
					l.add(Integer.valueOf(bpv.getProperty()));
					break;
				case PoConstants.BOOLEAN:
					l.add(Boolean.valueOf(bpv.getProperty()));
					break;
				case PoConstants.STRING:
					l.add(bpv.getProperty());
					break;
				case PoConstants.DOUBLE:
					l.add(Double.valueOf(bpv.getProperty()));
					break;
				default:
					l.add(bpv.getProperty());
					break;
				}
			}
			
		}
		return l;
	}

	@Override
	public void setApplicationContext(ApplicationContext a) throws BeansException {
		this.appCtx = a;
	}

	@Override
	public List<PoBeanProperty> readBeanProperties() {
		beanProperties = beanPropertyDAO.loadAll();
		return beanProperties;
	}

	@Override
	public List<PoBeanProperty> readBeanPropertiesForModule(String moduleUID) {

		return beanPropertyDAO.readBeanPropertiesForModule(moduleUID);
	}

	@Override
	public List<PoBeanProperty> readBeanPropertiesForBean(String beanName) {

		return beanPropertyDAO.readBeanPropertiesForBean(beanName);
	}

	@Override
	public List<PoBeanProperty> readBeanPropertiesForModuleAndBean(String moduleUID, String beanName) {

		return beanPropertyDAO.readBeanPropertiesForModuleAndBean(moduleUID, beanName);
	}

	@Override
	public List<String> readBeanNames() {
		return beanPropertyDAO.readBeanNames();
	}

	@Override
	public List<String> readBeanNamesForModule(String moduleUID) {

		return beanPropertyDAO.readBeanNamesForModule(moduleUID);
	}

	@Override
	public void updateBeanValue(PoBeanProperty b, String uid, String value) throws Exception {
		updateBeanValue(b, uid, value, 0);
	}

	@Override
	public void updateBeanValue(PoBeanProperty beanProperty, final String uid, String value, int ranking) throws Exception {
		if (beanProperty != null) {
			boolean found = false;

			if (beanProperty.getPropertyName().equals("logAllActions"))
				logger.debug("log all Actions");

			if (uid != null) {
				for (PoBeanPropertyValue bpv : beanProperty.getEntries()) {
					if (bpv != null && bpv.getUID().equals(uid)) {
						found = true;
						bpv.setListIndex(ranking);
						logger.debug("found PoBeanPropertyValue " + bpv + " by UID, before setting its value to " + value);
						bpv.setProperty(value);
					}
				}
			}

			if (found == false) {
				PoBeanPropertyValue bpv = new PoBeanPropertyValue();
				bpv.setBean(beanProperty);
				bpv.setProperty(value);
				Collection<PoBeanPropertyValue> values = beanProperty.getEntries();
				bpv.setListIndex(values.size());
				values.add(bpv);
				// TODO fri_2010-11-17:
				// When no value was found we must assert that uid == null, else an exception should be thrown - caller needed to update exactly that uid!
				// The new (incoming) value should be set to FIRST element in list, else update (to a 'scalar' value) did not really take place!
				// Ranking is ignored when uid did not exist - is this intended or a bug?
			}

			// rewrite complete object graph
			beanPropertyDAO.save(beanProperty);
		}
	}

	@Override
	public void updateBeanValueAndInject(PoBeanProperty beanProperty, String value) throws Exception {
		Collection<PoBeanPropertyValue> values = beanProperty.getEntries();
		assert values.size() <= 1 : "Can not update a bean property with more than one value - use updateBeanValue(beanProperty, uid, value) for such!";
		PoBeanPropertyValue bpv;
		if (values.size() == 1) {
			bpv = values.iterator().next();
		}
		else { // no value at all
			bpv = new PoBeanPropertyValue();
			bpv.setBean(beanProperty);
			values.add(bpv);
		}
		bpv.setProperty(value);
		bpv.setListIndex(0);

		saveBeanProperty(beanProperty);

		inject(beanProperty);
	}

	@Override
	public String checkPropertyValue(PoBeanProperty bp, String value) throws Exception {
		int type = getType(bp);
		switch (type) {
		case PoConstants.STRING:
			return "";
		case PoConstants.BOOLEAN:
			if (value.equals("true") || value.equals("false"))
				return "";
			return "po_not_a_boolean_value";
		case PoConstants.INTEGER:
			try {
				Integer.parseInt(value);
			}
			catch (NumberFormatException nfe) {
				return "po_integer_constraint";
			}
			return "";
		case PoConstants.DOUBLE:
			try {
				Double.parseDouble(value);
			}
			catch (NumberFormatException nfe) {
				return "po_double_constraint";
			}
			return "";
		default:
			return "type is not correctly set." + bp.getBeanName() + "." + bp.getPropertyName();
		}

	}

	@Override
	public void deleteBeanValue(PoBeanProperty bp, String uid) {
		boolean found = false;
		List<PoBeanPropertyValue> toRemove = new ArrayList<PoBeanPropertyValue>();
		for (PoBeanPropertyValue bpv : bp.getEntries()) {
			if (bpv == null || bpv.getUID().equals(uid)) {
				toRemove.add(bpv);
				if (bpv != null && bpv.getUID().equals(uid))
					found = true;
			}
		}

		bp.getEntries().removeAll(toRemove);
		if (found)
			beanPropertyDAO.save(bp);
	}

	@Override
	public void copyBeanProperty(String oldBeanName, String oldPropertyName, String newBeanName, String newPropertyName) {
		final PoBeanProperty oldBeanProperty = findBeanPropertyByKey(oldBeanName, oldPropertyName);
		if (oldBeanProperty == null) {
			logger.warn("Could not find BeanProperty with bean name '"+oldBeanName+"' and property name '"+oldPropertyName+"'");
			return;
		}
		
		PoBeanProperty newBeanProperty = findBeanPropertyByKey(newBeanName, newPropertyName);
		if (newBeanProperty == null) {	// create a new bean-property when not found
			newBeanProperty = new PoBeanProperty();
			newBeanProperty.setBeanName(newBeanName);
			newBeanProperty.setPropertyName(newPropertyName);
			newBeanProperty.setList( oldBeanProperty.isList() );
			newBeanProperty.setPassword(oldBeanProperty.isPassword());
		}
		
		// copy values of old bean-property to new one, overwriting existing values but preserving their UIDs
		int counter = 0;
		String valueLog = "";
		final Object [] existingBeanProperties = newBeanProperty.getEntries().toArray();	// will be empty when newly created
		
		for (PoBeanPropertyValue oldValue : oldBeanProperty.getEntries()) {
			PoBeanPropertyValue newValue = null;
			if (counter >= existingBeanProperties.length)	// new property value does not exist yet
				newValue = new PoBeanPropertyValue();
			else	// take UID of already existing value of new property
				newValue = (PoBeanPropertyValue) existingBeanProperties[counter];
			
			newValue.setBean(newBeanProperty);
			newValue.setListIndex(oldValue.getListIndex());
			newValue.setProperty(oldValue.getProperty());
			newBeanProperty.addEntry(newValue);
			
			counter++;	// go to next value
			valueLog += oldValue.getProperty()+" ";
		}
		
		// store bean-property and its value(s) to persistence
		saveBeanProperty(newBeanProperty);
		logger.info("Moved PoBeanProperty ["+oldBeanName+", "+oldPropertyName+"] to ["+newBeanName+", "+newPropertyName+"] with new value(s): "+valueLog);
	}

	@Override
	public void setBeanProperty(String beanName, String property, Object value) {

		PoBeanProperty bp = findBeanPropertyByKey(beanName, property);
		if (bp==null)
			throw new IllegalArgumentException("No configureable Beanproperty found for beanname=" + beanName + " and property=" + property);
		
		
		if (value instanceof Collection) {
			
			if (bp.isList()==false)
				throw new IllegalArgumentException("The passed value to update is a Collection or Array and does not correspond to the datatype of the beanproperty: " + toName(bp) + " !)");
			
			bp.getEntries().clear();
			Collection collValues = (Collection) value;
			int i=0;
			for (Object item : collValues) {
				PoBeanPropertyValue bpValue = new PoBeanPropertyValue( convertToString(bp, item));
				bpValue.setListIndex(i++);
				bp.addEntry(bpValue);
			}
			
			saveBeanProperty(bp);
			try {
				inject(bp);
			} catch (Exception e) {
				throw new PoRuntimeException("Problems updating and injecting value=" + value + " to beanproperty: " + toName(bp)); 
			}

		} else {
			
			String value2Inject = (value == null ? null : convertToString(bp, value));
			
			if (bp.isList() == true && bp.getEntries().size()>1) {
				// remove all entries but the first
				PoBeanPropertyValue toKeep = bp.getEntries().toArray(new PoBeanPropertyValue[] {})[0];
				bp.getEntries().clear();
				bp.addEntry(toKeep);
			}
			
			try {
				updateBeanValueAndInject(bp, value2Inject);
			} catch (Exception e) {
				throw new PoRuntimeException("Problems updating and injecting value=" + value2Inject + " to beanproperty: " + toName(bp));
			}
		}
		
	}
	
	
	private String toName(PoBeanProperty bp) {
		return bp.getBeanName() + "." + bp.getPropertyName();
	}

	private String convertToString(PoBeanProperty bp, Object value) {
		
		if (value==null)
			return null;

		if (bp.getType() == PoConstants.STRING) {
			return value.toString();
		} else if (bp.getType() == PoConstants.BOOLEAN && value instanceof Boolean) {
			return Boolean.toString((Boolean) value);
		} else if (bp.getType() == PoConstants.INTEGER && value instanceof Integer) {
			return Integer.toString((Integer) value);
		} else if (bp.getType() == PoConstants.DOUBLE && value instanceof Double) {
			return Double.toString((Double) value);
		} else {
			throw new PoRuntimeException("The passed value=" + value + " can not be converted to a String, or is not of request Po-Datatype="  + bp.getType());
		}
		
	}

}
