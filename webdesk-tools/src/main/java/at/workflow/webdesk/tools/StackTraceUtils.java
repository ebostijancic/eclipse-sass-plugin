package at.workflow.webdesk.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * delivers Stacktrace as string or as List of Strings
 * 
 * @author ggruber
 *
 */
public final class StackTraceUtils {
	
	  public static String getStackTrace(Throwable aThrowable) {
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    aThrowable.printStackTrace(printWriter);
	    return result.toString();
	  }
	  
	  public static List<String> getStackTraceAsList(Throwable aThrowable) {
		  
		  List<String> ret = new ArrayList<String>();
		  for (int i=0; i<aThrowable.getStackTrace().length; i++) {
			  StackTraceElement elem = aThrowable.getStackTrace()[i];
			  ret.add(elem.toString());			  
		  }
		  
		  if (aThrowable.getCause()!=null) {
			  ret.add("");
			  ret.add("Caused by:");
			  ret.addAll(getStackTraceAsList(aThrowable.getCause()));
		  }
		  
		  return ret;
	  }
}
