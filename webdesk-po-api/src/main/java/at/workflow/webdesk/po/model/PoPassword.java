package at.workflow.webdesk.po.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;

/**
 * Represents the persistent historized password of a person.
 * 
 * @author fritzberger 21.10.2010
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class PoPassword extends PoHistorization {

	private PoPerson person;
	private byte [] password;
	private boolean forcePasswordChange;
	private String uid;

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name="PASSWORD_UID", length=32)
    @Override
	public String getUID() {
		return uid;
	}

    @Override
	public void setUID(String uid) {
		this.uid = uid;
	}

	@ManyToOne
	@JoinColumn(name="PERSON_UID", nullable=false)
	@ForeignKey(name="FK_PASSWORD_PERSON")
	public PoPerson getPerson() {
		return person;
	}

	public void setPerson(PoPerson person) {
		this.person = person;
	}
	
    @Column(length=32, nullable=false, name="thePassword")	// PASSWORD is a SQL reserved word
	public byte[] getPassword() {
		return password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}

    @Column(nullable=false)
    public boolean isForcePasswordChange() {
		return forcePasswordChange;
	}

	public void setForcePasswordChange(boolean forcePasswordChange) {
		this.forcePasswordChange = forcePasswordChange;
	}

}
