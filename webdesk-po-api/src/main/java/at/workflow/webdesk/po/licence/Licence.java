package at.workflow.webdesk.po.licence;

import java.util.Date;

public class Licence {
	
	private String name;
	private int amount;
	private Date expires;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public Date getExpires() {
		return expires;
	}
	public void setExpires(Date expires) {
		this.expires = expires;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Licence))
			return false;
		if (this.getName()!=null)
			if (this.getName().equals(((Licence) o).getName()))
				return true;
			else
				return false;
		return false;
	}


}
