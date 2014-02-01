package at.workflow.webdesk.tools.testing;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

/**
 * @author hentner
 *
 * <p>
 * Use this interface in order to define a <code>DataGenerator</code>, which
 * creates any kind of data. This is useful when used inside a Testcase. 
 *
 */
public interface DataGenerator {

	/**
	 * Inside this function the data should be generated. 
	 */
	public void create(ApplicationContext appCtx)  throws IOException;
	
}
