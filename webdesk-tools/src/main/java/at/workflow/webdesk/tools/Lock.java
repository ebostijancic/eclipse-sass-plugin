/*
 * Created on 15.03.2007
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.tools;

import java.util.Date;

public class Lock {
    private final String _user;
    private final Date _since;
    private final transient boolean _self;
     
    //public Lock() {}
    
    public Lock(String user) {
        _user=user;
        _since=new Date();
        _self=true;
        
    }

    public boolean is_self() {
        return _self;
    }

    public Date getSince() {
        return _since;
    }

    public String getUser() {
        return _user;
    }
    
    
    
    
    
    

}
