package at.workflow.webdesk.po.impl.licence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.cocoon.spring.configurator.ResourceFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.tools.ResourceHelper;
import at.workflow.webdesk.po.licence.Licence;
import at.workflow.webdesk.po.licence.LicenceReader;
import at.workflow.webdesk.po.model.LicenceDefinition;
import at.workflow.webdesk.tools.config.StartupPropertyProvider;

/**
 * The LicenceResourceFilter takes care of removing resources when
 * they're not declared via the licence file.
 * 
 * you can disable licence checking, by specifing webdesk.licenceCheckDisabled=true
 * inside your webdesk.properties (has to be in classpath!!!) or as systemproperty
 * 
 * @author hentner, ggruber
 */
public class LicenceResourceFilter implements ResourceFilter {

	private static final Log logger = LogFactory.getLog(LicenceResourceFilter.class);

	public LicenceReader licenceReader;
	private List<LicenceDefinition> licenceDefinitions;
	private Map<String, Licence> licences;

	public LicenceResourceFilter() {
		// we keep the licenceReader locally, as there is no spring application context present at the time
		// this class is created 
		licenceReader = new LicenceReaderImpl();
		//Read the licence Definitions
		licenceDefinitions = licenceReader.getLicenceDefinitions();
		// Read the given licences 
		licences = licenceReader.getLicenceMap();

		// show information on stdout
		logger.info("You have the following licences: ");
		for (Iterator<String> i = licences.keySet().iterator(); i.hasNext();) {
			String name = i.next();
			Licence lic = licences.get(name);
			logger.info(" * " + name + " = " + lic.getAmount());
			if (lic.getExpires() != null)
				logger.info(" expires: " + lic.getExpires());
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set filter(Set resources) {
		if (resources != null && resources.size() > 0) {
			logger.info("LicenceResourceFilter was invoked with " + resources.size() + " resources.");
			Resource wdProps = new ClassPathResource("webdesk.properties");
			Properties props = new Properties();
			try {
				if (wdProps.exists()) {
					props.load(wdProps.getInputStream());
				}
				else {
					logger.warn("webdesk.properties does not exist in classpath.");
				}

				if ("true".equals(System.getProperty(StartupPropertyProvider.WEBDESK_LICENCE_CHECK_DISABLED)) ||
							(props.getProperty(StartupPropertyProvider.WEBDESK_LICENCE_CHECK_DISABLED) != null &&
							props.getProperty(StartupPropertyProvider.WEBDESK_LICENCE_CHECK_DISABLED).toString().equals("true"))) {
					logger.info("Licence Check was disabled so all resources are loaded.");
					return resources;
				}
			}
			catch (IOException e1) {
				throw new RuntimeException(e1);
			}

			Set<String> excludedApplicationContexts = new HashSet<String>();
			Set<String> usedApplicationContexts = new HashSet<String>();
			Iterator<LicenceDefinition> defs = licenceDefinitions.iterator();
			// h(X,Y) will contain every module X ... module name, Y ... module path (classPath) 
			while (defs.hasNext()) {
				LicenceDefinition licDef = defs.next();
				// extract module only if a licence for the licencedef exist
				if (!licences.containsKey(licDef.getName()) ||
						licences.get(licDef.getName()).getExpires() != null &&
						licences.get(licDef.getName()).getExpires().before(new Date())) {
					// no valid licence for current licenceDefinition 
					excludedApplicationContexts.addAll(licDef.getContexts());
					if (licences.containsKey(licDef.getName())) {
						logger.info("Licence for Module " + licDef.getName() + " already expired: " +
								licences.get(licDef.getName()).getExpires());
					}
					else {
						if (logger.isDebugEnabled())
							logger.debug("No licence found for " + licDef.getName());
					}
				}
				else {
					// licence is correct
					usedApplicationContexts.addAll(licDef.getContexts());
				}
			}

			// remove applicationContexts from NEGATIVE list which are really used 
			excludedApplicationContexts.removeAll(usedApplicationContexts);
			logger.info("Excluding: " + excludedApplicationContexts);

			List<Resource> toRemove = new ArrayList<Resource>();
			Iterator<Resource> i = resources.iterator();
			while (i.hasNext()) {
				Resource res = i.next();
				String cp = ResourceHelper.getClassPathOfResource(res);

				if (contains(excludedApplicationContexts, cp)) {
					logger.debug("Excluding " + cp);
					toRemove.add(res);
				}
			}
			resources.removeAll(toRemove);
		}
		return resources;
	}

	private boolean contains(Set<String> excludedApplicationContexts, String cp) {
		for (Iterator<String> i = excludedApplicationContexts.iterator(); i.hasNext();) {
			String pattern = i.next();
			if (cp.indexOf(pattern) >= 0)
				return true;
		}
		return false;
	}

}
