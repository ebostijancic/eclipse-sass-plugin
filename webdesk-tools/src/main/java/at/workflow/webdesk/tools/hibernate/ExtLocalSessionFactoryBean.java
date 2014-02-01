package at.workflow.webdesk.tools.hibernate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import at.workflow.tools.ResourceHelper;

/**
 * Extension of the Spring Sessionfactory bean which adds the following features: 
 * extract locations of hbm files and classpath out of class implementing
 * HibernateHbmRegistration and  HibernateAnnotationsRegistration
 * 
 * @author ggruber
 *
 */
public class ExtLocalSessionFactoryBean extends AnnotationSessionFactoryBean implements BeanNameAware, ApplicationContextAware {

	private static final String LUCENE_INDEX_DIRECTORY = "/ftindex";
	private static final Log logger = LogFactory.getLog(ExtLocalSessionFactoryBean.class);
    private PathMatchingResourcePatternResolver pmResolver;
    private String contains;
    private String notContains;
	private String beanName;
	private ApplicationContext applicatonContext;
	
	private static Map<String, Configuration> dataSourceConfigMap = Collections.synchronizedMap(new HashMap<String, Configuration>());
	
	public static Configuration getConfiguration(String sessionFactoryBeanName) {
		return dataSourceConfigMap.get(sessionFactoryBeanName);
	}
    
	public ExtLocalSessionFactoryBean() {
        super();
        pmResolver = new PathMatchingResourcePatternResolver();        
    }
	
	@Override
	protected Configuration newConfiguration() throws HibernateException {
		Configuration config =  BeanUtils.instantiateClass(AnnotationConfiguration.class);
		dataSourceConfigMap.put(beanName, config);
		return config;
	}
    
    /**
     * @param registrationBeanMap
     * @throws IOException
     * 
     * @see filter(String path)
     */
	public void setRegistrationBeanMap(Map<String,HibernateHbmRegistration> registrationBeanMap) throws IOException {
    	
    	List<String> locations = new ArrayList<String>();
    	Iterator<String> itr = registrationBeanMap.keySet().iterator();
    	while (itr.hasNext()) {
    		String key = itr.next();
    		HibernateHbmRegistration hbmRegistration =  registrationBeanMap.get(key);
    		locations.addAll(hbmRegistration.getHbmFileLocations());
    	}
    	
    	if (getContains()!=null || getNotContains()!=null)
    		filterList(locations);
    	setMappingPatternLocations(locations.toArray());
    }
    
    public String getContains() {
		return contains;
	}

	public void setContains(String contains) {
		this.contains = contains;
	}

	public String getNotContains() {
		return notContains;
	}

	public void setNotContains(String notContains) {
		this.notContains = notContains;
	}

	private void filterList(List<String> locations) {
    	for (Iterator<String> i = locations.iterator(); i.hasNext();) {
    		String loc = i.next();
    		if (!filter(loc)) 
    			i.remove();
    	}
	}

	/**
	 * @param annotatedClassesBeanMap
	 * @throws IOException
	 * 
	 * @see filter(String path)
	 */
	public void setAnnotatedClassesBeanMap(Map<String, HibernateAnnotationsRegistration> annotatedClassesBeanMap)  throws IOException {
    	
    	Set<String> packages = new HashSet<String>();
    	List<Class<?>> classes = new ArrayList<Class<?>>();
    	
    	// iterate over all locations of annotated Classes 
    	Set<String>  beans = annotatedClassesBeanMap.keySet();
    	Iterator<String> itr = beans.iterator();
    	while (itr.hasNext()) {
    		String key = itr.next();
    		HibernateAnnotationsRegistration annotationsRegistration =  annotatedClassesBeanMap.get(key);
    		Iterator<String> classpathResourceIterator = annotationsRegistration.getAnnotatedClasses().iterator();
    		while (classpathResourceIterator.hasNext()) {
    			String classpathResource = classpathResourceIterator.next();
    			Resource[] ress = pmResolver.getResources(classpathResource);
                for (int j = 0; j < ress.length; j++) {
                    String cp = ResourceHelper.getClassPathOfResource(ress[j]);
                    if (filter(cp))
	                    try {
	                    	cp = cp.replaceAll("/", ".");
	                    	cp = cp.substring(0, cp.lastIndexOf(".class"));
							classes.add(Class.forName(cp));
							logger.debug("SessionFactoryBean added " + cp + " to annotated classes.");
							
						} catch (ClassNotFoundException e) {
							logger.warn("Was not able to add class " + cp + " to annotated classes.", e);
						}
                }
    		}
    		packages.addAll(annotationsRegistration.getAnnotatedPackages());
    	}
    	
    	
    	Class<?>[] annotatedClasses = new Class[classes.size()];
    	Iterator<Class<?>> clazzI = classes.iterator();
    	int i = 0;
    	while (clazzI.hasNext()) {
				annotatedClasses[i] = clazzI.next();
				i++;
    	}
    	setAnnotatedClasses(annotatedClasses);
    	
    	String[] pckNames = new String[packages.size()];
    	for (i=0;i<packages.size();i++) {
    		pckNames[i] = (String) packages.toArray()[i];
    	}
    	
    	setAnnotatedPackages(pckNames);
    }
    
    /**
     * @param path
     * @return <code>true</code> if <code>path</code> contains the String 
     * <code>contains</code> and <code>path</code> does not contain
     * <code>notContains</code>. <code>contains</code> and <code>notContains</code> 
     * can be <code>null</code>. <code>path</code> always contains <code>null</code>. 
     */
    private boolean filter(String path) {
		if (getContains()==null && getNotContains()==null)
			return true;
		if ( 
				(getContains()==null || path.contains(getContains())) &&
				(getNotContains()==null || !path.contains(getNotContains()))
			) 
			return true;
		else
			return false;
	}

	public void setMappingPatternLocations(Object[] mappingLocations)
            throws IOException {

		
        logger.debug("set Hibernate Mapping Files...");
        List<Resource> resources = new ArrayList<Resource>();
        Set<String> res = new HashSet<String>();

        // loop over mappingLocations
        for (int i = 0; i < mappingLocations.length; i++) {
            try {
                Resource[] ress = pmResolver.getResources((String) mappingLocations[i]);

                for (int j = 0; j < ress.length; j++) {
                    String cp = ResourceHelper.getClassPathOfResource(ress[j]).toLowerCase();
                    if (!res.contains(cp))
                        resources.add(ress[j]);
                    res.add(cp);
                }
            } catch (Exception e) {
                logger.warn("could not resolve hbm resources for " + mappingLocations[i]);
            }
        }
        Resource[] resourcesArray = resources.toArray(new Resource[0]);
        this.setMappingLocations(resourcesArray);

    }

	@Override
	public void setBeanName(String name) {
		beanName = name;
	}

	private String getIndexBase() {
		if(applicatonContext.containsBean("org.apache.cocoon.configuration.Settings")) {
			Object settings = applicatonContext.getBean("org.apache.cocoon.configuration.Settings");
			String cacheDir = null;
			try {
				cacheDir = org.apache.commons.beanutils.BeanUtils.getProperty(settings, "cacheDirectory");
			}
			catch (Exception e) {
				return getIndexBaseDefault();
			}
			if (cacheDir == null) {
				return getIndexBaseDefault();
			}
			return cacheDir + LUCENE_INDEX_DIRECTORY;
		} else {
			return getIndexBaseDefault();
		}
	}
	
	private String getIndexBaseDefault() {
		return System.getProperty("java.io.tmpdir") + LUCENE_INDEX_DIRECTORY;
	}
	
	@Override
	protected void postProcessConfiguration(Configuration config) throws HibernateException {
		/**
		 * Here we add the configuration properties for Lucene search engine that we use 
		 * with hibernate, we do this configuration in runtime and not in <property name="hibernateProperties">
		 * of the spring configuration file for testing and deployment purposes.
		 * @author iaranibar 
		 */
		Properties extraProps = new Properties();
		extraProps.put("hibernate.search.default.directory_provider", "org.hibernate.search.store.FSDirectoryProvider");
		extraProps.put("hibernate.search.default.indexBase", getIndexBase());
		
		try {
			FileUtils.forceMkdir(new File(getIndexBase()));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		config.addProperties(extraProps);
		super.postProcessConfiguration(config);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicatonContext = applicationContext;
	}

}
