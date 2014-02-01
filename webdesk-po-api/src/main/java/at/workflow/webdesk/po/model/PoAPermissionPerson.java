package at.workflow.webdesk.po.model;

/**
 * Maps an action to a PoPerson for a specific timeframe. If an action is mapped this 
 * way, it reflects, that this PoPerson ist allowed to use the linked action. 
 * (PoAction Object)<br><br>
 * 
 * Created on 16.06.2005
 * @author Gabriel Gruber, Harald Entner
 */
@SuppressWarnings("serial")
public class PoAPermissionPerson extends PoAPermissionBase {

	private PoPerson person;

	@Override
	public void accept(PoAPermissionVisitor visitor) {
        visitor.visit(this);
    }
    

    /**
     * @return PoPerson object of person being referenced.
     */
    public PoPerson getPerson() {
        return person;
    }

    /**
     * @param person PoPerson object of person to give permission to.
     */
    public void setPerson(PoPerson person) {
        this.person = person;
    }

}
