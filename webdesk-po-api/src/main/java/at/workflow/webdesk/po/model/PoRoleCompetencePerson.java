package at.workflow.webdesk.po.model;


/**
 * This class links a role with a RoleHolder object and contains a link
 * to the competence which in this case is a person. 
 * 
 * @author hentner, ggruber
 */
@SuppressWarnings("serial")
public class PoRoleCompetencePerson extends PoRoleCompetenceBase {

    private PoPerson competence4Person;

    public PoPerson getCompetence4Person() {
        return competence4Person;
    }

    public void setCompetence4Person(PoPerson competence4Person) {
        this.competence4Person = competence4Person;
    }

    @Override
	public String getName() {
    	return this.getCompetence4Person().getLastName();
    }
}
