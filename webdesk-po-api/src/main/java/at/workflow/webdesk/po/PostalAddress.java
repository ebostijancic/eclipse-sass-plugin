package at.workflow.webdesk.po;

import at.workflow.webdesk.po.model.PoPerson.Gender;

/**
 * This interface specifies methods necessary 
 * for assembly of postal address.
 * 
 * @author sdzuban 21.08.2013
 */
public interface PostalAddress {
	
	/** @return MALE/FEMALE */
	Gender getGender();
	
	/** @return name consisting last and first names and ev. middle names initial */ 
	String getFullName();
	
	/** return street name, house number, apartment number */
	String getStreetAddress();
	
	String getZip();

	String getCity();

	String getCountry();

}
