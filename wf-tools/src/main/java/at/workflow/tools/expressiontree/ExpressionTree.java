package at.workflow.tools.expressiontree;

import java.io.Serializable;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          3.1Test<br>
 * created at:       09.10.2007<br>
 * package:          at.workflow.webdesk.tools<br>
 * compilation unit: ExpressionTree.java<br><br>
 *
 *
 * hentner, 9.10
 * TODO (BIG) this is a model component!!!!! insert a service
 *
 */
public class ExpressionTree  implements Serializable{
    // logger is not serializable
    // protected final Logger logger = Logger.getLogger(this.getClass().getName());
    
	private ExpressionTree leftChild;
    private ExpressionTree rightChild;
    private ExpressionTree parent;
    private ExpressionTreeNode value;

    
    public ExpressionTree getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(ExpressionTree leftChild) {
        this.leftChild = leftChild;
    }

    public ExpressionTree getParent() {
        return parent;
    }

    public void setParent(ExpressionTree parent) {
        this.parent = parent;
    }

    public ExpressionTree getRightChild() {
        return rightChild;
    }

    public void setRightChild(ExpressionTree rightChild) {
        this.rightChild = rightChild;
    }

    public ExpressionTreeNode getValue() {
        return value;
    }

    public void setValue(ExpressionTreeNode value) {
        this.value = value;
    }
    
}
