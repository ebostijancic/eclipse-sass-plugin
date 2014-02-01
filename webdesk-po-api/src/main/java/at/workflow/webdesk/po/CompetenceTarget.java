package at.workflow.webdesk.po;

/**
 * Marker interfact to identify Po Objects which can are subject to competence targets.
 * Normally PoGroup or PoPerson objects are implementing this! 
 * 
 * @author ggruber
 *
 */
public interface CompetenceTarget {

	/** competence target constants */
	public static final String ALL = "ALL";
	
	public static final String CLIENT = "Client";
	
	public static final String GROUP = "Group";
	
	public static final String PERSON = "Person";
}
