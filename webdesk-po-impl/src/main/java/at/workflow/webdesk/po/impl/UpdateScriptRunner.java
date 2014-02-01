package at.workflow.webdesk.po.impl;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

public class UpdateScriptRunner {
	
	private ApplicationContext appCtx;
	private Logger logger = Logger.getLogger(this.getClass());
	private PoAbstractUpgradeScript updateScript;

	public UpdateScriptRunner(final PoAbstractUpgradeScript updateScript) {
		this.updateScript = updateScript;
	}

	public void run() {
        
		updateScript.setApplicationContext(appCtx);
		
        if (updateScript.useTransaction()) {
            // create transaction around updatescript
            PlatformTransactionManager transactionManger = (PlatformTransactionManager) appCtx.getBean("TransactionManager");
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManger);
            transactionTemplate.execute(new TransactionCallback() {
                @Override
				public Object doInTransaction(TransactionStatus status) {
                    try {
                        updateScript.execute();
                    } catch (Exception e) {
                        logger.error(e,e);
                        throw new PoRuntimeException(e);
                    }
                    return null;
                }
            });
        }  else {
        	// run without transaction
        	// but with an open hibernate session using the session factory!
        	// get session and bind it to the local thread
        	SessionFactory sf = (SessionFactory) appCtx.getBean("sessionFactory");
            Session session = SessionFactoryUtils.getSession(sf, true); 
            session.setFlushMode(FlushMode.MANUAL);
            TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
            try{
            	updateScript.execute();
            }catch(Exception e) {
            	logger.error(e);
            	throw new PoRuntimeException(e);
            }	finally {
            	// flush, unbind and close it!
        	    TransactionSynchronizationManager.unbindResource(sf);
        	    session.close();
            }
        }
	}

	public void setApplicationContext(ApplicationContext appCtx2) {
		this.appCtx = appCtx2;
	}

}
