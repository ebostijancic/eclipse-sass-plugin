package at.workflow.webdesk.po.model;

/**
 * Defines a group <code>PoGroup</code> as roleholder for a role and 
 * a defined competence target. This competence target is either a group
 * or a person, or *ALL*.
 * 
 * @author ggruber, hentner
 * 
 */
@SuppressWarnings("serial")
public class PoRoleHolderGroup extends PoHistorization implements PoRoleHolderLink {

    private String uid;
    private int ranking;
    private PoGroup group;
    private PoRoleCompetenceBase roleCompetence;

    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }
    
    @Override
	public PoRoleCompetenceBase getRoleCompetenceBase() {
    	return roleCompetence;
    }
    
    public void setRoleCompetence(PoRoleCompetenceBase roleCompetence) {
        this.roleCompetence = roleCompetence;
    }
    
    /** 
     * @deprecated use getRoleCompetenceBase instead
     */
    public PoRoleCompetenceBase getRoleHolder() {
    	return roleCompetence;
    }
    
    /**
     * The name of this setter does not reflect its meaning
     * we have to retain the wrong name due to backwards compatibility
     * 
     * @param roleCompetence
     */
    public void setRoleHolder(PoRoleCompetenceBase roleCompetence) {
        this.roleCompetence = roleCompetence;
    }


    public PoGroup getGroup() {
        return group;
    }

    public void setGroup(PoGroup group) {
        this.group = group;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public String getRoleHolderObjectUID() {
        return this.getGroup().getUID();
    }
}
