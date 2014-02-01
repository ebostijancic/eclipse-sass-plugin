package at.workflow.webdesk.po.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;

import at.workflow.webdesk.tools.model.annotations.UiFieldHint;


/**
 * @author sdzuban 21.08.2013
 */
@Entity
public class PoPersonBankAccount extends PoHistorization {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "PERSONBANKACCOUNT_UID", length=32)
	private String UID;
	
	/** name of the account holder */
	private String accountOwner;
	
	private String bankInstituteName;
	// first and second positions is country code
	@Column(length=32, nullable=false)
	private String IBAN;
	
	// fifth and sixth positions is country code, must be same as in IBAN
	@Column(length=16, nullable=false)
	private String BIC;
	
	/** is this the primary account of the user */
	private boolean primaryAccount;
	
	/** key-value list specifying intended purpose of the account */
	@UiFieldHint(keyValueTypeId = "HrUsageCode")
	private String usageCode;

	@ManyToOne (fetch=FetchType.EAGER)
	@JoinColumn (name="PERSON_UID", nullable=false)
	@ForeignKey (name="FK_BANKACCOUNT_PERSON")
	private PoPerson person;

	
	/** {@inheritDoc} */
	@Override
	public String getUID() {
		return UID;
	}

	/** {@inheritDoc} */
	@Override
	public void setUID(String uid) {
		UID = uid;
	}

	public String getAccountOwner() {
		return accountOwner;
	}

	public void setAccountOwner(String accountOwner) {
		this.accountOwner = accountOwner;
	}

	public String getBankInstituteName() {
		return bankInstituteName;
	}

	public void setBankInstituteName(String bankInstituteName) {
		this.bankInstituteName = bankInstituteName;
	}

	public String getIBAN() {
		return IBAN;
	}

	public void setIBAN(String iBAN) {
		IBAN = iBAN;
	}

	public String getBIC() {
		return BIC;
	}

	public void setBIC(String bIC) {
		BIC = bIC;
	}

	public boolean isPrimaryAccount() {
		return primaryAccount;
	}

	public void setPrimaryAccount(boolean primaryAccount) {
		this.primaryAccount = primaryAccount;
	}

	public String getUsageCode() {
		return usageCode;
	}

	public void setUsageCode(String usageCode) {
		this.usageCode = usageCode;
	}

	public PoPerson getPerson() {
		return person;
	}

	public void setPerson(PoPerson person) {
		this.person = person;
	}

}
