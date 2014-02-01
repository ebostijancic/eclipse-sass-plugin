package at.workflow.webdesk.po.model;
/**
 * @author sdzuban 08.08.2012
 */
public class PoRoleCompetenceClient extends PoRoleCompetenceBase {

    private PoClient competence4Client;

    public PoClient getCompetence4Client() {
        return competence4Client;
    }

    public void setCompetence4Client(PoClient competence4Client) {
        this.competence4Client = competence4Client;
    }

    @Override
	public String getName() {
    	return this.getCompetence4Client().getName();
    }
}
