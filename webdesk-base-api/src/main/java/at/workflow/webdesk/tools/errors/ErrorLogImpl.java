package at.workflow.webdesk.tools.errors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The task for this class is to provide means for collection of
 * error information with the purpose of its cumulated presentation
 * after the processing is finished.
 * 
 * Example is tree table generation - it is not acceptable when any 
 * row of thousands of rows of data can - through erroneous value -
 * stop the whole evaluation process and force the system present
 * only an error message to the user.
 * The goal is a fault tolerant and fault aware processing which collects 
 * error information and recovers and continues the computation. The user
 * shall see table with all the correctly computed rows plus
 * error report listing every error that happen during processing. 
 * 
 * @author sdzuban 29.06.2013
 */
public class ErrorLogImpl implements ErrorLog {

    private final List<ErrorMessage> messages = new ArrayList<ErrorMessage>();

    public boolean hasErrorMessages() {
        return ! messages.isEmpty();
    }
    
    public List<ErrorMessage> getErrorMessages() {
        return Collections.unmodifiableList(messages);
    }
    
    public void addErrorLog(ErrorLogImpl otherLog) {
    	messages.addAll(otherLog.getErrorMessages());
    }

    @Override
	public void addErrorMessage(String message) {
    	messages.add(new ErrorMessage(message));
    }

	/** {@inheritDoc} */
	@Override
	public void addErrorMessage(ErrorMessage errorMessage) {
		messages.add(errorMessage);
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return messages.toString();
	}
}
