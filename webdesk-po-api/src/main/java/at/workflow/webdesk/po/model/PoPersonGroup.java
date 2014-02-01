package at.workflow.webdesk.po.model;

/**
 * This class represents the membership of a person to a group. This group
 * can be of different types (Orgstructure). Depending of the OrgType the Groups'Orgstructure
 * the person may be member of multiple groups of the same orgstructure at a time or only a
 * single group of the same orgstructure at the same time.
 * Membership is generally historized with complete time portion, to allow change of groups
 * over the day.
 *  
 * @author ggruber
 */
@SuppressWarnings("serial")
public class PoPersonGroup extends PoDayHistorization {

    private String uid;
    private PoPerson person;
    private PoGroup group;

	@Override
	public String getUID() {
		return uid;
	}

	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}


    public PoPerson getPerson() {
        return person;
    }
    public void setPerson(PoPerson person1) {
        this.person = person1;
    }

    public PoGroup getGroup() {
        return group;
    }
    public void setGroup(PoGroup group) {
        this.group = group;
    }
    
    @Override
	public String toString() {
    	return super.toString();
    	
    	// fri_2013-06-17: got exception from here
//    	if (getGroup()==null || getPerson()==null) 
//    		return "[" + getUID() + "] is not yet initialised correctly";
//    	return getPerson().getUserName() + " is contained in group " + getGroup().getShortName() +
//    		" from " + getValidfrom() + " to " + getValidto() + " [" + getUID()+"]";
    }

}
