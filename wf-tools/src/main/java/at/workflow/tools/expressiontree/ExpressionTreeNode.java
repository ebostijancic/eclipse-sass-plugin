package at.workflow.tools.expressiontree;

import java.io.Serializable;

/**
 * Created on 17.01.2006
 * 
 * @author DI Harald Entner <br>
 * logged in as: hentner<br><br>
 * 
 * Project: webdesk3<br>
 * changed at: 17.01.2006<br>
 * package: at.workflow.webdesk.ta.model<br>
 * compilation unit: ExpressionTreeNode.java<br><br>
 */
public class ExpressionTreeNode implements Serializable {

	public static int test = 2;

	// Node Types

	public static final int OPERATOR = 1;

	public static final int VALUE = 2;

	public static final int COLUMN = 3;

	public static final int CONJUNCTION = 4;

	// Value Types of Node (If node is a value)

	public static int STRINGVALUE = 5;

	public static int NUMVALUE = 6;

	public static final int DATEVALUE = 7;

	// if column is a taColumn - differentiate between them
	public static final String PSTAMMTABLE = "pstamm";

	public static final String VKONT = "vkont";

	public static final String TGWERT = "tgwert";

	private Object value;

	private String table;

	/**
	 * type can be operator, value, column
	 */
	private int type;

	private int valueType;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public int getValueType() {
		return valueType;
	}

	public void setValueType(int valueType) {
		this.valueType = valueType;
	}

}
