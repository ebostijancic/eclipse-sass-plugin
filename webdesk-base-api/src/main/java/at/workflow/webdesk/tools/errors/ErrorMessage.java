package at.workflow.webdesk.tools.errors;

import java.text.SimpleDateFormat;
import java.util.Date;

import at.workflow.webdesk.tools.date.DateTools;

/**
 * The task of this class is to transfer error data from the place
 * they occur to the user. Main application being transferring error
 * data from database rows processing to the UI to alert the user
 * that not all records were successfully processed.
 * 
 * This class encapsulates the error information as can be gathered 
 * during processing of any data.
 * It is immutable to prevent any manipulation of logged information.
 * 
 * @author sdzuban 29.06.2013
 */
public class ErrorMessage {
	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	private final Date time;
	private final String message;
	private final Exception exception;
	private final Object[] additionalInfo;

	/** @param message verbal message about the observed error */
	public ErrorMessage(String message) {
		this(message, null);
	}

	/** 
	 * @param message context information about the observed error.
	 * @param exception exception, if any, that was caught during the processing 
	 * @param additionalInfo additional data that can be collected like variable and/or field values etc. 
	 */
	public ErrorMessage(String message, Exception exception, Object...additionalInfo) {
		this.time = DateTools.now();
		this.message = message;
		this.exception = exception;
		this.additionalInfo = additionalInfo;
	}

	/** @returns timestamp of this error log entry */
	public Date getTime() {
		return time;
	}

	/** @returns verbal message about the observed error */
	public String getMessage() {
		return message;
	}
	
	/** @returns exception, if any, that was caught during the processing */
	public Exception getException() {
		return exception;
	}
	
	/** @returns additional data that can be collected like variable and/or field values etc. */
	public Object[] getAdditionalInfo() {
		return additionalInfo;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		
		String result = "[" + dateFormat.format(time) +  
				": " + (message == null ? "no message" : message) + 
				": " + (exception == null ? "no exception" : exception);
		
		if (additionalInfo != null && additionalInfo.length > 0) {
			for (Object artefact : additionalInfo)
				if (artefact != null)
					result += ", " + artefact.toString();
		} else
			result += ": no additional information";
		
		return result + "]";
	}
}

