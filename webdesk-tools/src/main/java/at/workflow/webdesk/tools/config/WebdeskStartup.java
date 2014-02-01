package at.workflow.webdesk.tools.config;

/**
 * interface to be implemented by modules which need
 * initialization code to be run AFTER the spring
 * container has refreshed and where the initialization
 * code has to be in some specific order.
 * 
 * just implement a spring bean based on this interface
 * and the rest will be done automatically.
 * 
 * @author ggruber
 *
 */
public interface WebdeskStartup {

	public void start();
}
