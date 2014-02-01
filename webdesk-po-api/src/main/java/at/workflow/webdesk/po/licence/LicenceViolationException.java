/*
 * Created on 31.03.2006
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.licence;

public class LicenceViolationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LicenceViolationException(String msg) {
        super(msg);
    }

    public LicenceViolationException() {
        super();
    }

    public static String ACTION_NOT_ALLOWED="V1 Licence does not allow Action.";
    
    public static String MAX_AMOUNT_TRANSGRESSED="V2 Max amount transgressed.";
    
    
}
