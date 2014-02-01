package at.workflow.webdesk.po.model;

/**
 * This class represents a historized Roleholder definition, where a Person
 * is given a <code>PoRole</code> for some competence. Competences can be
 * a person, a group or simply 'for ALL'.
 * 
 * @author ggruber, hentner
 */
@SuppressWarnings("serial")
public class PoRoleHolderPerson extends PoHistorization implements PoRoleHolderLink {

	private String uid;
	private int ranking;
	private PoRoleCompetenceBase roleHolder;

    /** a link to a <code>PoRoleDeputy</code> object. */
    private PoRoleDeputy deputy;

    private PoPerson person;

    
    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }
    
    /**
     * <p>Returns a <code>PoRoleHolderCompetenceBase</code> object. 
	 * @deprecated use getRoleCompetenceBase() instead.
     */
    public PoRoleCompetenceBase getRoleHolder() {
        return roleHolder;
    }
    
    public PoRoleCompetenceBase getRoleCompetenceBase() {
    	return roleHolder;
    }
    
    public void setRoleHolder(PoRoleCompetenceBase roleHolder) {
        this.roleHolder = roleHolder;
    }

    public PoPerson getPerson() {
        return person;
    }

    public void setPerson(PoPerson person) {
        this.person = person;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public String getRoleHolderObjectUID() {
        return person.getUID();
    }

	/**
	 * @return a <code>PoRoleDeputy</code> object. If this field is filled, it means 
	 * that the actual <code>PoRoleHolderPerson</code> object is a copy of the 
	 * <code>PoRoleHolderPerson</code> object of the <code>officeholder</code>. The 
	 * officeholder is declared as part of the <code>PoRoleDeputy</code> object.
	 */
	public PoRoleDeputy getDeputy() {
		return deputy;
	}

	public void setDeputy(PoRoleDeputy deputy) {
		this.deputy = deputy;
	}
	
	@Override
	public String toString() {
		return "PoRoleHolderPerson [uid:"+getUID()+", person:"+(getPerson() != null ? getPerson().getUserName() : "null")+"]";
	}
}
