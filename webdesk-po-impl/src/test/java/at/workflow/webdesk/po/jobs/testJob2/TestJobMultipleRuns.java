package at.workflow.webdesk.po.jobs.testJob2;

import java.util.LinkedHashSet;
import java.util.Set;

import at.workflow.webdesk.po.jobs.AbstractWebdeskJob;

public class TestJobMultipleRuns extends AbstractWebdeskJob {
	
	
	private static Set<String> instanceHashCodes = new LinkedHashSet<String>();
	

	@Override
	public void run() {
		String hc = "" + this.hashCode();
		logger.info("hashCode()=" + hc);
		instanceHashCodes.add(hc);
	}
	
	public static Set<String> getHashCodesOfInstances() {
		return instanceHashCodes;
	}
	public static void clearHashCodesOfInstances() {
		instanceHashCodes.clear();
	}

}
