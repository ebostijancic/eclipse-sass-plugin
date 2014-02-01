package at.workflow.webdesk.po.impl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.model.PoLog;
import at.workflow.webdesk.po.model.PoLogDetail;
import at.workflow.webdesk.po.model.PoLogDetailThrowable;

/**
 * Extended Log4J appender that uses Hibernate to store log events in a database.
 * 
 * Session Factory is provided at runtime through the Name of the Spring Bean
 * which is then looked up in the application Context.
 * 
 * however logging with this appender can only work, if Spring has successfully started.
 * 
 * @Author Gabriel Gruber
 * 
 * TODO implement a multi-threaded unit test for this class that at least covers the append call.
 * TODO remove unnecessary synchronized blocks as there are for "buffer" to win performance.
 * TODO remove all deprecated methods after removing sessionFactoryBeanName param from all log4j*.xml (this param is ignored here!)
 */
public class PoLogAppenderImpl extends AppenderSkeleton {

	private static final String LOG_SESSIONFACTORY_BEANNAME = "logSessionFactory";

	private SessionFactory sessionFactory;
	private Session session;
	private boolean createNewSession = true;
	private static Vector<LoggingEvent> buffer = new Vector<LoggingEvent>();
	private static Boolean appending = Boolean.FALSE;
	private static Object appendLock = new Object();

	/**
	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
	 */
	@Override
	protected void append(LoggingEvent loggingEvent) {

		// if no logging reference is set -> return
		if (MDC.get("uid") == null)
			return;

		// logging can only work, if servlet was successfully started
		// and spring applicationcontext is loaded
		if (WebdeskApplicationContext.getApplicationContext() == null)
			return;

		try {
			if (this.sessionFactory == null) {
				this.sessionFactory = (SessionFactory) WebdeskApplicationContext.getApplicationContext().getBean(LOG_SESSIONFACTORY_BEANNAME);
			}
		}
		catch (Exception e) {
			// something went wrong with Spring exit
			return;
		}

		/*
		 * Ensure exclusive access to the buffer in case another thread is
		 * currently writing the buffer.
		 */
		synchronized (buffer) {
			// Add the current event into the buffer

			buffer.add(loggingEvent);
			/*
			 * Ensure exclusive access to the appending flag to guarantee that
			 * it doesn't change in between checking it's value and setting it
			 */
			synchronized (appendLock) {
				if (!appending.booleanValue()) {
					/*
					 * No other thread is appending to the log, so this thread
					 * can perform the append
					 */
					appending = Boolean.TRUE;
				}
				else {
					/*
					 * Another thread is already appending to the log and it
					 * will take care of emptying the buffer
					 */
					return;
				}
			}
		}

		Transaction tx = null;
		try {

			try {
				if (session == null) {
					session = this.sessionFactory.openSession();
				}

				if (!session.isConnected()) {
					session.reconnect(SessionFactoryUtils.getDataSource(this.sessionFactory).getConnection());
				}
			}
			catch (SQLException e) {
				System.out.println("exception happend while logging into the database: " + e);
				e.printStackTrace();
				return;
			}

			/*
			 * Ensure exclusive access to the buffer in case another thread is
			 * currently adding to the buffer.
			 */
			synchronized (buffer) {
				tx = session.beginTransaction();

				PoLog myLog = null;
				LoggingEvent bufferLoggingEvent;
				PoLogDetail logDetail;

				/*
				 * Get the current buffer length. We only want to process events
				 * that are currently in the buffer. If events get added to the
				 * buffer after this point, they must have been caused by this
				 * loop, as we have synchronized on the buffer, so no other
				 * thread could be adding an event. Any events that get added to
				 * the buffer as a result of this loop will be discarded, as to
				 * attempt to process them will result in an infinite loop.
				 */

				int bufferLength = buffer.size();

				for (int i = 0; i < bufferLength; i++) {
					bufferLoggingEvent = buffer.get(i);

					if (bufferLoggingEvent.getMDC("uid") != null) {

						// nur loggen, wenn uid in MDC mitübergeben wurde!!!

						myLog = (PoLog) this.session.get(PoLog.class, (Serializable) bufferLoggingEvent.getMDC("uid"));

						logDetail = new PoLogDetail();
						String message = bufferLoggingEvent.getRenderedMessage();
						if (message != null && message.length() >= 2000)
							message = message.substring(0, 1990) + "...";

						logDetail.setMessage(message);
						Date logDate = new Date();
						logDate.setTime(bufferLoggingEvent.timeStamp);

						logDetail.setTimeStamp(logDate);

						String loggerName = bufferLoggingEvent.getLoggerName();
						if (loggerName.length() >= 80)
							loggerName = loggerName.substring(0, 75) + "...";
						logDetail.setLogger(loggerName);
						logDetail.setLog(myLog);

						if (bufferLoggingEvent.getLevel().equals(Level.ALL)) {
							logDetail.setLogLevel("ALL");
						}
						else if (bufferLoggingEvent.getLevel().equals(Level.DEBUG)) {
							logDetail.setLogLevel("DEBUG");
						}
						else if (bufferLoggingEvent.getLevel().equals(Level.ERROR)) {
							logDetail.setLogLevel("ERROR");
							MDC.put("ok", Boolean.FALSE);
						}
						else if (bufferLoggingEvent.getLevel().equals(Level.FATAL)) {
							logDetail.setLogLevel("FATAL");
							MDC.put("ok", Boolean.FALSE);
						}
						else if (bufferLoggingEvent.getLevel().equals(Level.INFO)) {
							logDetail.setLogLevel("INFO");
						}
						else if (bufferLoggingEvent.getLevel().equals(Level.OFF)) {
							logDetail.setLogLevel("OFF");
						}
						else if (bufferLoggingEvent.getLevel().equals(Level.WARN)) {
							logDetail.setLogLevel("WARN");
						}
						else {
							logDetail.setLogLevel("UNKNOWN");
						}
						session.save(logDetail);

						if (bufferLoggingEvent.getThrowableStrRep() != null) {
							for (int j = 0; j < bufferLoggingEvent.getThrowableStrRep().length; j++) {
								PoLogDetailThrowable exceptionLog = new PoLogDetailThrowable();
								String msg = bufferLoggingEvent.getThrowableStrRep()[j];
								if (msg.length() >= 2000)
									msg = msg.substring(0, 1990) + "...";
								exceptionLog.setMessage(msg);
								exceptionLog.setPosition(j);
								exceptionLog.setLogDetail(logDetail);
								session.save(exceptionLog);
							}

							// write flag back
							// to indicate an exception happened!
							MDC.put("ok", Boolean.FALSE);
						}
					}
				}

				tx.commit();

				/*
				 * flush session every time and clear buffer
				 * note, that this appender maintains its own hibernate session!
				 */
				session.flush();
				buffer.clear();

				/*
				 * Ensure exclusive access to the appending flag - this really
				 * shouldn't be needed as the only other check on this flag is
				 * also synchronized on the buffer. We don't want to do this in
				 * the finally block as between here and the finally block we
				 * will not be synchronized on the buffer and another process
				 * could add an event to the buffer, but the appending flag will
				 * still be true, so that event would not get written until
				 * another log event triggers the buffer to be emptied.
				 */
				resetAppender();
			}
		}
		catch (HibernateException he) {
			try {
				tx.rollback();
				System.out.println("exception happend while logging into the datbase: " + he);
				he.printStackTrace();
			}
			catch (Exception ee) {
			}
			
			this.errorHandler.error("HibernateException", he, ErrorCode.GENERIC_FAILURE);
			// Reset the appending flag
			resetAppender();
			return;
		}
		finally {

			// some Debug info
			// BasicDataSource dsWD = (BasicDataSource) WebdeskApplicationContext.getBean("webdesk-DataSource");
			// System.out.println("+++ State WD: idle=" + dsWD.getNumIdle() + ", active=" + dsWD.getNumActive());
			// BasicDataSource dsLogWD = (BasicDataSource) WebdeskApplicationContext.getBean("webdesk-log-DataSource");
			// System.out.println("+++ State WD-Log: idle=" + dsLogWD.getNumIdle() + ", active=" +
			// dsLogWD.getNumActive());

			if (this.session != null) {
				this.session.disconnect();
			}
		}
	}

	/**
	 * Does nothing.
	 * @see org.apache.log4j.Appender#close()
	 */
	@Override
	public void close() {
	}

	/**
	 * @return false.
	 * @see org.apache.log4j.Appender#requiresLayout()
	 */
	@Override
	public boolean requiresLayout() {
		return false;
	}

	/**
	 * @deprecated
	 */
	public void setSessionFactoryBeanName(String beanName) {
	}

	/**
	 * @deprecated
	 */
	public void setLogServiceBeanName(String string) {
	}

	/**
	 * @deprecated
	 */
	public boolean isCreateNewSession() {
		return createNewSession;
	}

	/**
	 * @deprecated
	 */
	public void setCreateNewSession(boolean createNewSession) {
		this.createNewSession = createNewSession;
	}

	/**
	 * conveniance function to reset the synchronised appending flag
	 * if this is permanently true, no log events will be flushed
	 */
	public static void resetAppender() {
		synchronized (appendLock) {
			appending = Boolean.FALSE;
		}
	}

}
