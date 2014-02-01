package at.workflow.webdesk.tools.testing;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * decorates the create function by opening and binding a hibernate session
 * and closing it afterwards (to avoid lazy init problems..) 
 * 
 * @author ggruber
 */
public class DataGeneratorHibernateSessionDecorator implements DataGenerator {

	private static final Log logger = LogFactory.getLog(DataGeneratorHibernateSessionDecorator.class);
	
	private DataGenerator generator;
	
	public DataGeneratorHibernateSessionDecorator(DataGenerator generator) {
		this.generator = generator;
	}

	public void create(ApplicationContext appCtx) throws IOException {
		// get Hibernate Session
		SessionFactory sf =  (SessionFactory) appCtx.getBean("sessionFactory");
		Session session = SessionFactoryUtils.getSession(sf, true); 
	    try {
	    	TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
	    }
	    catch (Exception e) {
	    	logger.warn("Could not bind session to thread. If run with a testsuite this is not an error", e);
	    }
		
		this.generator.create(appCtx);
		
	    TransactionSynchronizationManager.unbindResource(sf);
	    session.close();
	}
	
}
