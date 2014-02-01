package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoActionParameter;

public interface PoActionParameterDAO extends GenericDAO<PoActionParameter>{

	public List<PoActionParameter> getActionParameters(PoAction action);
	
}
