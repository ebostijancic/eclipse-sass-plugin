package at.workflow.webdesk.po.licence;

import java.util.ArrayList;
import java.util.HashMap;

import at.workflow.webdesk.po.model.LicenceDefinition;

public interface LicenceReader {

	public abstract ArrayList<LicenceDefinition> getLicenceDefinitions();

	public abstract HashMap<String, Licence> getLicenceMap();
	
	/** Get Name of Company */
	public String getCompany();
	
	/** Get Description of Company */
	public String getCompanyDesc();
	

}