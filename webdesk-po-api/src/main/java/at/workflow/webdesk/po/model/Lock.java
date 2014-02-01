package at.workflow.webdesk.po.model;

import java.util.Date;

import at.workflow.webdesk.po.PoRuntimeException;


/**
 * @author DI Harald Entner <br>
 *
 * The Lock class can assign lock information to model classes. Every model
 * class that uses pessimistic locking needs a Lock component.  
 *
 */
public class Lock {
    
	private String table;
	
	private String userName1;
    private Date since1;
    private transient boolean self1;
     
    private String userName2;
    private Date since2;
    private transient boolean self2;
    
    
    public Lock() {}
    
    public Lock(String user, String table) {
        userName1=user;
        since1=new Date();
        self1=true;
        this.table=table;
    }
    
    
    public Lock(String user, String table, int number) {
    	switch (number) {
    	case 1:
    		userName1=user;
            since1=new Date();
            self1=true;
            break;
    	case 2:
    		userName2=user;
            since2=new Date();
            self2=true;
            break;
        default:
            throw new PoRuntimeException("Number " + number + " to lock not found. [Table: " + table+"]");	
    	}
    	this.table=table;
    }
    
    

    public boolean isSelf1() {
        return self1;
    }

    public Date getSince1() {
        return since1;
    }

    public String getUserName1() {
        return userName1;
    }

    public void setSelf1(boolean self) {
        this.self1 = self;
    }

    public void setSince1(Date since) {
        this.since1 = since;
    }

    public void setUserName1(String userName) {
        this.userName1 = userName;
    }

	public boolean isSelf2() {
		return self2;
	}

	public void setSelf2(boolean self2) {
		this.self2 = self2;
	}

	public Date getSince2() {
		return since2;
	}

	public void setSince2(Date since2) {
		this.since2 = since2;
	}

	public String getUserName2() {
		return userName2;
	}

	public void setUserName2(String userName2) {
		this.userName2 = userName2;
	}
}
