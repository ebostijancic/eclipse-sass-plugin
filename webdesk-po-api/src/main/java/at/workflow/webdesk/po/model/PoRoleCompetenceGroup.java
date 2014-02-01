package at.workflow.webdesk.po.model;

/**
 * This class links a role with a RoleHolder object and contains a link
 * to the competence which in this case is a group. 
 * 
 * @author ggruber
 */
@SuppressWarnings("serial")
public class PoRoleCompetenceGroup extends PoRoleCompetenceBase {

    private PoGroup competence4Group;
    private boolean inheritToChilds;

    public PoGroup getCompetence4Group() {
        return competence4Group;
    }

    public void setCompetence4Group(PoGroup competence4Group) {
        this.competence4Group = competence4Group;
    }


    // FIXME: this is probably unused!!!!!
    public boolean isInheritToChilds() {
        return inheritToChilds;
    }

    public void setInheritToChilds(boolean inheritToChilds) {
        this.inheritToChilds = inheritToChilds;
    }

    @Override
	public String getName() {
    	return this.getCompetence4Group().getShortName();
    }
}
