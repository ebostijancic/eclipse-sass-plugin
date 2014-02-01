/*
 * Created on 20.04.2008
 * @author fpovysil
 * 
 **/
package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.PoUtilService;
import at.workflow.webdesk.po.model.LockObject;


/**
 * @author fpovysil <br>
 * 
 * Project:          	pep<br>
 * created at:       	20.4.2008<br>
 * package:          	at.workflow.webdesk.po.impl<br>
 * compilation unit: 	LockController.java<br><br>
 *  * 
 * Locked objects are hold in the ehcache H(x,y), which contains all locked objects.
 * 
 * x...key (32bit UID of the locked object + "_" + lockNumer)
 * y...a at.workflow.webdesk.po.model.LockStamp object. 
 * 
 *  * The <code>lockGroup</code> relates <code>users</code> to a <code>lockNumber</code>.
 * 
 * H(x,y) can be accessed via the PoUtilService, which is contained in the Application
 * Context. The key of the cache can be found in the PoConstants class [LOCKCACHE]. 
 * 
 * @see at.workflow.webdesk.po.model.LockStamp
 *
 */
public class LockController {
    
	
	private CacheManager cacheManager;
	private int minToLive;
	private Logger logger = Logger.getLogger(this.getClass());

	
	/**
	 * @param minToLive
	 * get bean-property minToLive
	 */
	public void setMinToLive(int minToLive) {
		this.minToLive = minToLive;
	}

	/**
	 * @return the time to live in milliseconds.
	 */
	public int getMinToLive() {
		return minToLive * 60 * 1000;
	}


	/**
	 * @param logLevel ("error", "debug" or "info")
	 * @param message
	 * logs the message to the LockController-logger
	 */
	public void logMessage(String logLevel, String message) {
		if ( logLevel.equals("error"))
			logger.error(message);
		if ( logLevel.equals("debug") )
			logger.debug(message);
		else if ( logLevel.equals("info"))
			logger.info(message);
	}
	
	/**
	 * @param objectsToLock
	 * @return true, if all objects was locked successfully
	 */
	public boolean lockObjects(List objectsToLock) {
		try {
			// check if list contains at least one entry
			if ( objectsToLock.isEmpty() == true ) {
				logger.error("cannot lock objects, because list of objects to lock is null");
				throw new PoRuntimeException("cannot lock objects, because list of objects to lock is null");
			}	
			
			// get lock-cache
			LockObject lo;
			Cache cache = cacheManager.getCache(PoConstants.LOCKCACHE);
			try {
				// walk through the objects to lock and check if all objects can locked
				logger.debug("check, if all objects in list can be locked");
				Iterator iter = objectsToLock.iterator();
				while (iter.hasNext()) {
					Object o = iter.next();
				
					// check, if the object has the correct type
					if (o instanceof LockObject == false ) {
						logger.error("list contains wrong object type (type must be \"LockObject\")");
						throw new PoRuntimeException("list contains wrong object type (type must be \"LockObject\")");
					} else {
						lo = (LockObject) o;
					
						// check, if object-UID is not null
						if ( lo.getObjectUID() == null || lo.getObjectUID().equals("")) {
							logger.error("cannot lock object, because object-UID is null or empty");
							throw new PoRuntimeException("cannot lock object, because object-UID is null or empty");
						}	
						// check, if user was set
						if ( lo.getUser() == null || lo.getUser().equals("")) {
							logger.error("cannot lock object, because user is null or empty");
							throw new PoRuntimeException("cannot lock object, because user is null or empty");
						}	

						logger.debug("check, if object with object-uid: " + lo.getObjectUID() + ", lock-group: " + lo.getLockGroup() + " can be locked by user: " + lo.getUser());
						String key = lo.getCacheKey();
						logger.debug("cache-key: " + key);
						if ( cache.get(key) != null ) {
							logger.debug("object is already in lock-cache");
							Element el = cache.get(key);
							// LockObject existingLock = (LockObject) ((Element)cache.get(key)).getValue();
							LockObject existingLock = (LockObject) el.getValue();
							if ( existingLock.getUser().equals(lo.getUser()) == true ) {
								logger.debug("user is the same");
							} else {
								logger.debug("user is another one (" + existingLock.getUser() + ")");
								// object is locked by another user
								if ( (new Date().getTime() - existingLock.getRefreshedAt()) < this.getMinToLive() + 90000) {
									// object is still locked (timeout is not achieved)
									logger.info("object cannot be locked from user " + lo.getUser() + " because it is locked by user " + existingLock.getUser());
									logger.debug("cannot lock object because it is already locked by another user at this time");
									return false;
								} else
									logger.debug("locking is possible, because existing lock (for another user) is already in timeout");
							}
						} else
							logger.debug("no object with this key is in cache at this time");
						logger.debug("this object can be locked");
					}
				}
				logger.debug("all objects in the list can be leocked -> lock it");
				// lock can be set for all the object in the list
				String loggInfo = "";
				iter = objectsToLock.iterator();
				while (iter.hasNext()) {
					lo = (LockObject) iter.next();
					if ( cache.get(lo.getCacheKey()) != null ) {
						Element el = cache.get(lo.getCacheKey());
						LockObject existingLock = (LockObject) el.getValue();
						existingLock.setRefreshedAt(new Date().getTime());
						logger.debug("renew lock for object with object-uid: " + lo.getObjectUID() + ", lock-group: " + lo.getLockGroup() + " and user: " + lo.getUser());
					} else {
						logger.debug("create lock for object with object-uid: " + lo.getObjectUID() + ", lock-group: " + lo.getLockGroup() + " and user: " + lo.getUser());
						lo.setCreated(new Date().getTime());
						lo.setRefreshedAt(new Date().getTime());
						Element element = new Element(lo.getCacheKey(), lo);
						cache.put(element);
					}
					if (loggInfo.equals(""))
						loggInfo = "lock object(s) for user " + lo.getUser();
				}
				logger.info(loggInfo);
				
			} catch (CacheException ie) {
	        	logger.error("Cache Exception occured: ",ie);
	        	return false;
	        }
			return true;
			
		} catch(Throwable e) {
			logger.error(e, e);
			throw new PoRuntimeException(e);
		}
	}
	
	
	/**
	 * @param objectsToLock 
	 * @return true, if all objects was locked successfully
	 * convention for one object to lock: objectUID@lockGroup@user@addInfo
	 * convention for more than one object to lock: objectUID@lockGroup@user@addInfo|objectUID@lockGroup@user@addInfo|objectUID@lockGroupuser@addInfo
	 */
	public boolean lockObjects(String objectsToLock) {
		boolean result = false;
		try {
			logger.debug("string objects to lock: " + objectsToLock);
			if ( objectsToLock == null || objectsToLock.equals("")) {
				logger.debug("cannot lock object because object-string is empty");
				throw new PoRuntimeException("cannot lock object because object-string is empty");
			}
			// convert string to list of lock-objects
			List objList = convertStringToLockObjects(objectsToLock);
			// try to lock objects
			result = lockObjects(objList);
			return result;
		} catch(Exception e) {
			logger.error(e, e);
			throw new PoRuntimeException(e);
		}
	}
	
	
	/**
	 * @param objectsToUnlock
	 * @return true if unlock was successfully
	 * converts the string to a list of lock-objects and unlocks them (remove from cache) 
	 */
	public boolean unlockObject(String objectsToUnlock) {
		try {
			List objList = convertStringToLockObjects(objectsToUnlock);
			return unlockObjects(objList);
		} catch(Exception e) {
			logger.error(e, e);
			throw new PoRuntimeException(e);
		}
	}
	
	
	/**
	 * @param objectsToUnlock
	 * @return true if unlock was successfully
	 * unlocks the given lock-objects (removes it from cache)
	 */
	public boolean unlockObjects(List objectsToUnlock) {
		try {
			String loggInfo = "";
			// get cache
			Cache cache = cacheManager.getCache(PoConstants.LOCKCACHE);
			logger.debug("try to unlock the following lock-objects: ");
			Iterator iter = objectsToUnlock.iterator();
			while(iter.hasNext()) {
				LockObject lo = (LockObject) iter.next();
				if ( loggInfo.equals("") )
					loggInfo = "unlock object(s) from user " + lo.getUser();
				logger.debug("unlock-object with object-uid: " + lo.getObjectUID() + ", lock-group: " + lo.getLockGroup() + ", user: " + lo.getUser() + ", add. info: " + lo.getAddInfo());
				cache.remove(lo.getCacheKey());
				logger.debug("successfully unlocked");
			}
			logger.info(loggInfo);
			return true;
		} catch(Exception e) {
			logger.error(e, e);
			throw new PoRuntimeException(e);
		}
	}

	
	/**
	 * 
	 * @param objectsString
	 * @return list of Lo 	qckObjects
	 */
	private List convertStringToLockObjects(String objectsString) {
		List objList = new ArrayList();
		String[] obj = objectsString.split("\\|");
		for(int i=0; i< obj.length; i++ ) {
			logger.debug("try to convert object-string: " + obj[i]);
			String[] el = obj[i].split("\\@");
			if ( el.length != 4 ) {
				logger.debug("object-string has incorrect length");
				throw new PoRuntimeException("object-string has incorrect length");
			} else {
				logger.debug("converted object-string: object-uid: " + el[0] + ", lock-group: " + el[1] + ", user: " + el[2] + ", add. info: " + el[3]);
				LockObject lo = new LockObject(el[0], el[1], el[2], el[3]);
				objList.add(lo);
			}
		}
		return objList;
	}


	/**
	 * 
	 * @param objectsToLock
	 * @return serialized string
	 * calls method serializeObjectsToLock(List)
	 */
	public String serializeObjectsToLock(LockObject objectsToLock) {
		List objList = new ArrayList();
		objList.add(objectsToLock);
		return serializeObjectsToLock(objList);
	}
	
	
	/**
	 * @param objectsToLock
	 * @return serialized string
	 * format for one serialized object: objectUID@lockGroup@user@addInfo
	 * format for more than one serialized object: objectUID@lockGroup@user@addInfo|objectUID@lockGroup@user@addInfo|objectUID@lockGroupuser@addInfo
	 */
	public String serializeObjectsToLock(List objectsToLock) {
		String result = "";
		if ( objectsToLock == null ) {
			logger.debug("cannot convert null");
			throw new PoRuntimeException("cannot convert null");
		}
		Iterator iter = objectsToLock.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o instanceof LockObject == false ) {
				logger.error("list contains wrong object type (type must be \"LockObject\")");
				throw new PoRuntimeException("list contains wrong object type (type must be \"LockObject\")");
			} else {
				LockObject lo = (LockObject) o;
				// check, if object-UID is not null
				if ( lo.getObjectUID() == null || lo.getObjectUID().equals("")) {
					logger.error("cannot serialize object, because object-UID is null or empty");
					throw new PoRuntimeException("cannot serialize object, because object-UID is null or empty");
				}	
				// check, if user was set
				if ( lo.getUser() == null || lo.getUser().equals("")) {
					logger.error("cannot serialize object, because user is null or empty");
					throw new PoRuntimeException("cannot serialize object, because user is null or empty");
				}	
				// check, if forbidden character @ exists 
				if ( lo.getObjectUID().indexOf("@") != -1 && lo.getLockGroup().indexOf("@") != -1 && lo.getUser().indexOf("@") != -1 && lo.getAddInfo().indexOf("@") != -1 ) {
					logger.error("cannot serialize object, because forbidden character @ was found");
					throw new PoRuntimeException("cannot serialize object, because forbidden character @ was found");
				}
				// serialize object
				if ( ! result.equals("") )
					result += "|";
				result += lo.getObjectUID() + "@" + lo.getLockGroup() + "@" + lo.getUser() + "@" + lo.getAddInfo();
			}
		}
		logger.debug("serialized string: " + result);
		return result;
	}
	
	
	/**
	 * @param objectUID
	 * @return the user from the cache entry with ge geiben key(objectUID and "1")
	 */
	public String getUserOfLockObject(String objectUID) {
		return getUserOfLockObject(objectUID, "1");
	}
	
	
	/**
	 * @param objectUID
	 * @param lockGroup
	 * @returns the user from the cache-entry with the given key (objectUID and lockGroup)
	 * 			if lockGroup = null or empty: lockGroup will set to "1"
	 */
	public String getUserOfLockObject(String objectUID, String lockGroup) {
		String result = "";
		try {
			Cache cache = cacheManager.getCache(PoConstants.LOCKCACHE);
			try {
				if (lockGroup == null || lockGroup.equals("")) {
					lockGroup = "1";
					logger.debug("set lockgroup to 1 because it was empty");
				}
				String key = objectUID + "_" + lockGroup;
				logger.debug("try to get user form lock-cache with key: " + key);
				if ( cache.get(key) != null ) {
					LockObject existingLock = (LockObject) ((Element)cache.get(key)).getValue();
					result = existingLock.getUser();
					logger.debug("user found: " + result);
				}
				else
					logger.debug("user not found, because there is no valid cache-entry for the given key");
			} catch (CacheException ie) {
	        	logger.error("Cache Exception occured: ",ie);
	        	return result;
	        }
	 	} catch (Exception e) {
	 		logger.error(e,e);
	 	}
	 	return result;
	}
		
	
    /**
     * Method for getting all lock-cache-entries (for admin use only) 
     * @return list of all cache-entries (LockObjects)  
     */
	public List getAllCacheEntries() {
	   List result = new ArrayList();
	   try {
		   // get cache
		   Cache cache = cacheManager.getCache(PoConstants.LOCKCACHE);
		   // get all keys
		   List keys = cache.getKeys();
		   Iterator iterKeys= keys.iterator();
		   while (iterKeys.hasNext()) {
			   String key = (String) iterKeys.next();
			   if ( cache.get(key) != null ) {
				   LockObject existingLock = (LockObject) ((Element)cache.get(key)).getValue();
				   result.add(existingLock);
			   }
		   }
       } catch (Exception e) {
    	   logger.error(e,e);
       }
       return result;
	}

	
	/**
     * Method for remvoing all lock-cache-entries (for admin use only) 
     * @return void  
     */
	public void unlockAllObjects() {
		Cache cache = cacheManager.getCache(PoConstants.LOCKCACHE);
        try {
        	cache.removeAll();
        } catch (Exception e) {
        	logger.error(e,e);
        }
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	
	
	
    
}
