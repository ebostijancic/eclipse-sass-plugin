package at.workflow.tools.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * A model class holding values for sending an email.
 * Use the <code>MailService</code> in order to send the mail.
 * 
 * @author hentner
 */
public class Mail {

	private String from;
	private List<String> to; 
	private List<String> copyTo;
	private List<String> blindCopyTo;
	private String subject;
	private String message;
	private Map<String, String> variables;
	private List<String> filenames;
	private List<byte[]> byteArrays;
	
	/** Do-nothing constructor. */
	public Mail() {
	}
	
	/** Does setTo(email), setSubject(subject). */
	public Mail(String email, String subject) {
		this.setTo(email);
		this.setSubject(subject);
	}

	/** Does setTo(email), setSubject(subject), setMessage(message). */
	public Mail(String email, String subject, String message) {
		this(email, subject);
		this.setMessage(message);
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<String> getTo() {
		return to;
	}
	
	/** Same as setSendTo() - sets the receivers of the mail. */
	public void setTo(List<String> to) {
		this.to = to;
	}
	
	/**
	 * Sets the receiver(s) of this mail.
	 * If the string is a comma delimited list it will be parsed to a list of addresses.
	 */
	public void setTo(String email) {
		List<String> toList = parseToList(email);
		this.to = toList;
	}

	public String[] getToAsArray() {
		return asStringArray(getTo());
	}
	
	/** Same as setTo() - sets the receivers of the mail. */
	public void setSendTo(List<String> to) {
		this.to = to;
	}

	public List<String> getCopyTo() {
		return copyTo;
	}
	
	public String[] getCopyToAsArray() {
		return asStringArray(getCopyTo());
	}

	public void setCopyTo(List<String> copyTo) {
		this.copyTo = copyTo;
	}

	public List<String> getBlindCopyTo() {
		return blindCopyTo;
	}
	
	public String[] getBlindCopyToAsArray() {
		return asStringArray(getBlindCopyTo());
	}
	
	public void setBlindCopyTo(List<String> blindCopyTo) {
		this.blindCopyTo = blindCopyTo;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	/** @return the mail body text. */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the mail body text. When passed message contains HTML tags,
	 * it must be also surrounded by "&lt;html>" tags.
	 * @param message the mail body text to set.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Sets the mail body text, and the MIME type to MailService.HTML.
	 * The passed text MUST NOT be surrounded by "&lt;html>" tags.
	 * @param message the HTML mail body text to set, without surrounding HTML tags.
	 */
	public void setInnerHtmlMessage(String message) {
		setMessage("<html><head></head><body>"+message+"</body></html>");
	}

	/** @return the names that should be substituted in mail text. */
	public Map<String, String> getVariables() {
		return variables;
	}

	/**
	 * All Map-keys in mail text will be substituted by the Map-values, done by MailServiceImpl.
	 * This is a plain text substitution without assumed $ or % variable prefix.
	 * <p/>
	 * FIXME: variables should be declared differently, as sometimes not intended replaces could take place.
	 */
	public void setVariables(Map<String,String> variables) {
		this.variables = variables;
	}

	public List<String> getFilenames() {
		return filenames;
	}

	public void setFilenames(List<String> filenames) {
		this.filenames = filenames;
	}

	public List<byte[]> getByteArrays() {
		return byteArrays;
	}

	public void setByteArrays(List<byte[]> byteArrays) {
		this.byteArrays = byteArrays;
	}



	private String[] asStringArray(List<String> l) {
		if (l == null)
			return new String[0];
		String[] strArr = new String[l.size()];
		System.arraycopy(l.toArray(), 0, strArr, 0, l.size());
		return strArr;
	}

	@SuppressWarnings("unchecked")
	private List<String> parseToList(String email) {
		List<String> toList = new ArrayList<String>();
		if (StringUtils.hasText(email) && email.indexOf(",")>-1) {
			toList.addAll(CollectionUtils.arrayToList(StringUtils.commaDelimitedListToStringArray(email)));
		} else {
			toList.add(email);
		}
		return toList;
	}
	
}
