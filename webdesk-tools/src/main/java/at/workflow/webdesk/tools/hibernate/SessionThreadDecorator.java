package at.workflow.webdesk.tools.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import at.workflow.webdesk.WebdeskApplicationContext;

public class SessionThreadDecorator extends Thread {

	public SessionThreadDecorator(Thread wrappedThread, boolean useCurrentThreadsSession) {
		super();
		this.wrappedThread = wrappedThread;
		
		if (useCurrentThreadsSession) {
			lookupSession(getSessionFactory());
		}
	}
	
	private Thread wrappedThread;
	private Session session = null;
	protected final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void run() {
		SessionFactory sf = null;
		try {
			sf = getSessionFactory();
			lookupSession(sf);
			try {
				TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
			} catch (Exception e) {
				logger.warn("Could not bind session to thread!", e);
			} 
			wrappedThread.run();

		} finally {
			TransactionSynchronizationManager.unbindResource(sf);
			if (session!=null) {
				session.close();
			}
		}
	}

	/**
	 * get Session from current thread if existent
	 * or create new one
	 */
	private void lookupSession(SessionFactory sf) {
		session = SessionFactoryUtils.getSession(sf, true); 
		session.setFlushMode(FlushMode.MANUAL);
	}

	private SessionFactory getSessionFactory() {
		return (SessionFactory) WebdeskApplicationContext.getBean("sessionFactory");
	}
}
