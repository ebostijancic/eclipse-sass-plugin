package at.workflow.webdesk.tools.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.hibernate.cache.ReadWriteCache.Item;
import org.hibernate.cache.entry.CacheEntry;

import at.workflow.tools.mail.Mail;
import at.workflow.tools.mail.MailService;
import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.tools.api.ClusterConfig;

/**
 * This listener was created to trace a customer problem, where hibernate second level caches
 * get corrupted. This listener traces all changes to a specified EHCache identified by the 
 * property 'cacheUnderInvestigation' and tries a few checks on that cache:
 * - check, whether the number of columns in the caching record is correct (supplied by <code>expectedColumnsCount</code>
 * - check, whether a specific column identified by the <code>columnIndexUnderInvestigation</code> has the exptected datatype
 * defined in <code>expectedColumnTypeUnderInvestigation</code>
 * - check, whether a specific column identified by the <code>columnIndexUnderInvestigation</code> has only values which are building
 * a valid UID (check is only done, if <code>expectedColumnUnderInvestigationIsUid</code> is true.
 * 
 * @see Notes://Miraculix/C1256B300058B5FC/64B015AC662A2F2FC1256D5F005C366F/7E134192E5AEB069C1257AC4003323B2
 * 
 * @author ggruber
 *
 */
public class LoggingCacheEventListener implements CacheEventListener {
	
	private String cacheUnderInvestigation;
	
	/** exptected number of columns in the cache entry */
	private int expectedColumnsCount;
	
	/** index of cache entry column under futher investigation */
	private int columnIndexUnderInvestigation;
	
	/** do check if column identified by columnIndexUnderInvestigation has the same datatype as the one defined here */
	private String expectedColumnTypeUnderInvestigation;
	
	/** do check, if column identified by columnIndexUnderInvestigation has a valid UID */
	private boolean expectedColumnUnderInvestigationIsUid;
	
	/** do check if column identified by columnIndexUnderInvestigation has the same value as the string defined here */
	private String expectedColumnValue;
	
	/** email to notify when a check does not succeed */
	private String mailAddressToNotify;
	
	/** use this to activate the listener */
	private boolean active = false;
	
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	private CacheManager cacheManager;
	private MailService mailService;
	
	public void init() {
		registerListenerToSelf();
		mailService = (MailService) WebdeskApplicationContext.getBean("PoMailService");
	}


	private void registerListenerToSelf() {
		if (cacheManager!=null && active==true) {
			Cache cache = cacheManager.getCache( cacheUnderInvestigation );
			if (cache!=null)
				cache.getCacheEventNotificationService().registerListener(this);
		}
	}


	@Override
	public void notifyElementExpired(Ehcache cache, Element element) {
	}

	@Override
	public void notifyElementPut(Ehcache cache, Element element)
			throws CacheException {
		investigateElement(cache, element);
	}

	@Override
	public void notifyElementRemoved(Ehcache cache, Element element)
			throws CacheException {
	}

	@Override
	public void notifyElementUpdated(Ehcache cache, Element element)
			throws CacheException {
		investigateElement(cache,element);
	}

	@SuppressWarnings("deprecation")
	private void investigateElement(Ehcache cache, Element element) {
		
		String cacheName = cache.getName();
		
		if ( StringUtils.isEmpty(cacheUnderInvestigation)==false && cacheUnderInvestigation.equals(cacheName) ) {
			
			if (logger.isDebugEnabled())
				logger.debug("[Cache: " + cacheName + " received Update for Key=" + element.getKey() + "] : new Value = " + element.getValue() );
			
			
			// this communication is meant for me
			Object value = element.getValue();
			
			if (value instanceof Item) {
				Item cacheItem = (Item) value;
				CacheEntry cacheEntry = (CacheEntry) cacheItem.getValue();
				
				if (logger.isDebugEnabled())
					logger.debug("CacheEntry=" + cacheEntry);
				
				Serializable[] cols = cacheEntry.getDisassembledState();

				if ( logger.isDebugEnabled() ) {
					outputCacheEntry(cols, Priority.DEBUG );
				}
				
				if (expectedColumnsCount != cols.length) {
					logger.error("The Cache-Entry should have " + expectedColumnsCount + ", but has " + cols.length + " columns!");
					outputCacheEntry( cols, Priority.INFO );
				}
				
				Serializable columnValueUnderInvestigatation = cols[columnIndexUnderInvestigation];
				
				if ( columnValueUnderInvestigatation!=null
						&& StringUtils.isEmpty( expectedColumnTypeUnderInvestigation ) == false
						&& expectedColumnTypeUnderInvestigation.equals( columnValueUnderInvestigatation.getClass().getName())==false ) {
					
					logger.error("The Cache-Entry at index " + columnIndexUnderInvestigation + " should have datatype" + expectedColumnTypeUnderInvestigation + ", but has type=" + columnValueUnderInvestigatation.getClass().getName() );
					outputCacheEntry( cols, Priority.INFO );
					notifyByMail(getCacheEntryContent(cols).toString());
				}
				
				if ( columnValueUnderInvestigatation!=null && expectedColumnUnderInvestigationIsUid == true && hasValueOnlyUidChars(columnValueUnderInvestigatation) == false ) 
				{
					logger.error("The Cache-Entry at index " + columnIndexUnderInvestigation + " should have a UID value, but hast this value=" +  columnValueUnderInvestigatation );
					outputCacheEntry( cols, Priority.INFO );
					notifyByMail(getCacheEntryContent(cols).toString());
				}
				
				List<String> expectedColumnValues = new ArrayList<String>();
				
				if (StringUtils.isEmpty( expectedColumnValue ) == false ) {
					expectedColumnValues.addAll( Arrays.asList( StringUtils.split(expectedColumnValue, ',')));
				}
				
				if (  expectedColumnValues.size()>0 && columnValueUnderInvestigatation!=null && expectedColumnValues.contains( columnValueUnderInvestigatation ) == false) {
					logger.error("The Cache-Entry at index " + columnIndexUnderInvestigation + " should have the value (or one of it)=" + expectedColumnValue + ", but hast this value=" +  columnValueUnderInvestigatation );
					outputCacheEntry( cols, Priority.INFO );
					notifyByMail(getCacheEntryContent(cols).toString());
				}
			}
		}
	}
	
	private void notifyByMail(String cacheEntry) {
		Mail mail = new Mail();
		
		String to = mailAddressToNotify;
		if ( StringUtils.isEmpty(mailAddressToNotify)==true) {
			to = "gabriel.gruber@workflow.at";
		}
		
		ClusterConfig options = (ClusterConfig) WebdeskApplicationContext.getApplicationContext().getBeansOfType(ClusterConfig.class).values().toArray()[0];
		String currentClusterNode = options.getClusterNode();
		
		mail.setTo( to );
		mail.setCopyTo( Arrays.asList( new String[] { "office@workflow.at" } ));
		mail.setSubject( "Webdesk: Cache Problem Occured again... [ClusterNode=" + currentClusterNode + "]" );
		
		StringBuffer stackTraceBuffer = new StringBuffer();
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			stackTraceBuffer.append(ste.toString() + "\n");
		}
		mail.setMessage( "The infamous caching problem (no row found...) just appeared again.\n\nCacheEntry:\n" + cacheEntry + "\n\nStacktrace:\n" + stackTraceBuffer.toString());
		
		try {
			mailService.sendMail(mail);
		} catch (Exception e) {
			logger.warn("Could not send Mail to inform a about infamous Caching problem..");
		}
	}



	private boolean hasValueOnlyUidChars(Serializable serializable) {
		String value = (String) serializable;
		return value.matches("[a-g,0-9]{32}");
	}


	private void outputCacheEntry(Serializable[] cols, Priority logPrio) {
		StringBuffer buffer = getCacheEntryContent(cols);
		logger.log(logPrio, "CacheEntry serialized Content = " + buffer);
		
		Thread.dumpStack();
	}


	private StringBuffer getCacheEntryContent(Serializable[] cols) {
		StringBuffer buffer = new StringBuffer();
		
		for (int i=0;i<cols.length;i++) {
			if (i>0)
				buffer.append( "|");
			buffer.append( cols[i] );
		}
		return buffer;
	}
	
	@Override
	public void dispose() {
		
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException  {
		throw new CloneNotSupportedException();
	}

	@Override
	public void notifyElementEvicted(Ehcache arg0, Element arg1) {
		
	}

	@Override
	public void notifyRemoveAll(Ehcache arg0) {
		
	}

	public void setCacheUnderInvestigation(String cacheUnderInvestigation) {
		this.cacheUnderInvestigation = cacheUnderInvestigation;
		registerListenerToSelf();
	}

	public void setExpectedColumnsCount(int expectedColumnsCount) {
		this.expectedColumnsCount = expectedColumnsCount;
	}


	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}


	public void setColumnIndexUnderInvestigation(int columnIndexUnderInvestigation) {
		this.columnIndexUnderInvestigation = columnIndexUnderInvestigation;
	}


	public void setExpectedColumnTypeUnderInvestigation(String expectedColumnTypeUnderInvestigation) {
		this.expectedColumnTypeUnderInvestigation = expectedColumnTypeUnderInvestigation;
	}


	public void setExpectedColumnUnderInvestigationIsUid(boolean expectedColumnUnderInvestigationIsUid) {
		this.expectedColumnUnderInvestigationIsUid = expectedColumnUnderInvestigationIsUid;
	}


	public void setMailAddressToNotify(String mailAddressToNotify) {
		this.mailAddressToNotify = mailAddressToNotify;
	}

	public void setExpectedColumnValue(String expectedColumnValue) {
		this.expectedColumnValue = expectedColumnValue;
	}


	public void setActive(boolean active) {
		this.active = active;
		registerListenerToSelf();
	}

}
