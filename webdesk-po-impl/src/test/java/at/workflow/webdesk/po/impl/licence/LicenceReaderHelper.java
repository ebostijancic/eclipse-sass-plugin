package at.workflow.webdesk.po.impl.licence;

import java.util.List;

import at.workflow.webdesk.po.impl.PoLicenceInterceptor;
import at.workflow.webdesk.po.impl.licence.LicenceReaderImpl;
import at.workflow.webdesk.po.model.LicenceDefinition;

/**
 * A Helper class for testing licences...
 * 
 * @author ggruber
 */
public class LicenceReaderHelper {
	
	public void simulateLicence(String licenceString, LicenceReaderImpl licenceReader, PoLicenceInterceptor licenceInterceptor) {
		
		licenceReader.createLicencesFromString(licenceString);
		licenceReader.readLicenceDefinitions();
		
		// ============ Read Licence Definition Files and inject into
		List<LicenceDefinition> licDefs = licenceReader.getLicenceDefinitions(); // parses the xml structure of the
																		// previously loaded files
		licenceInterceptor.setLicenceDefinitions(licDefs);
		licenceInterceptor.initAllowedActions();
		
	}
	
	public void deactivateLicenceCheck(PoLicenceInterceptor licenceInterceptor) {
		licenceInterceptor.setLicenceDefinitions(null);
		
	}

}
