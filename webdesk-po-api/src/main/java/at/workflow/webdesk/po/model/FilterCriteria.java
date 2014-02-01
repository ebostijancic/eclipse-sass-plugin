package at.workflow.webdesk.po.model;

/**
 * This transient class represents a single FilterCriteria what is used for filtering the Travels
 * in the Action getOrgTravels.
 * 
 * @author Alexander Malic <br>
 */
public class FilterCriteria {
	/**
	 * the name of the filter
	 */
	public String name;
	
	/**
	 * the type of the filter
	 * <ul>
	 * 	<li>hql</li>
	 * 	<li>script</li>
	 * </ul>
	 */
	public String type;
	
	/**
	 * the hql/javascript-expression
	 */
	public String expression;
	
	public String originalExpression;
	
	public String onCreateScript;
	
	/**
	 * default Constructor
	 */
	public FilterCriteria() {}
	
	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 * @param expression
	 */
	public FilterCriteria(String name, String type, String expression) {
		this.name=name;
		this.type=type;
		this.expression=expression;
		this.originalExpression = expression;
	}

	// getters and setters (form compatibility)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getOriginalExpression() {
		return originalExpression;
	}

	public void setOriginalExpression(String originalExpression) {
		this.originalExpression = originalExpression;
	}

	public String getOnCreateScript() {
		return onCreateScript;
	}

	public void setOnCreateScript(String onCreateScript) {
		this.onCreateScript = onCreateScript;
	}

}
