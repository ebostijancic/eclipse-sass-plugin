package at.workflow.webdesk.po.model;

/**
 * This class ist used to represent a dynamic roleholder. For 
 * instance if you want the hierarchical group of a person 
 * to be the roleholder you should use this class. 
 * 
 * @author hentner
 */
@SuppressWarnings("serial")
public class PoRoleHolderDynamic extends PoHistorization implements
		PoRoleHolderLink {

	private String uid;
	private PoRoleCompetenceBase roleCompetenceBase;
	private int roleHolderType=0;
	

	@Override
	public String getUID() {
		return uid;
	}

    /**
     * <p>Returns a <code>PoRoleHolderCompetenceBase</code> object. 
     */
    public PoRoleCompetenceBase getRoleCompetenceBase() {
    	return roleCompetenceBase;
    }
    
    public void setRoleCompetenceBase(PoRoleCompetenceBase roleCompetenceBase) {
    	this.roleCompetenceBase = roleCompetenceBase;
    }
    
    public PoRoleCompetenceBase getRoleHolder() {
    	return getRoleCompetenceBase();
    }
    
    public void setRoleHolder(PoRoleCompetenceBase roleCompetenceBase) {
        this.roleCompetenceBase = roleCompetenceBase;
    }

	public String getRoleHolderObjectUID() {
		// there is no roleholder, thus the roleholdertype is 
		// returned.
		
		return new Integer(roleHolderType).toString();
	}


	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}

	public int getRoleHolderType() {
		return roleHolderType;
	}

	public void setRoleHolderType(int roleHolderType) {
		this.roleHolderType = roleHolderType;
	}

}
