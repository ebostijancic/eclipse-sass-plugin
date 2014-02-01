package at.workflow.webdesk.po.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import at.workflow.webdesk.po.model.PoClient;

/**
 * @author sdzuban 21.09.2012
 */
public class PoClientPrefixUtil {

    private static final Logger logger = Logger.getLogger(PoClientPrefixUtil.class);
	
//	private static final String MSG = "There is already one client using the chosen prefix. Please select another one.";
//	
//	public static void updatePrefix(PoClient client, Class<?> objectClass, String propertyName, String oldPrefix, String newPrefix,
//			Object service, String clientFinderName, String finderName, String saverName) {
//		
//		if (objectClass == null)
//			throw new RuntimeException("Null object class");
//		if (StringUtils.isEmpty(propertyName))
//			throw new RuntimeException("Null object property");
//		if (service == null)
//			throw new RuntimeException("Null service");
//		if (StringUtils.isEmpty(finderName))
//			throw new RuntimeException("Null finder name");
//		if (StringUtils.isEmpty(saverName))
//			throw new RuntimeException("Null saver name");
//		
//		String propName = propertyName.substring(0, 1).toUpperCase() + (propertyName.length() > 1 ? propertyName.substring(1) : ""); 
//		Method getter = getMethod(objectClass, "get" + propName);		
//		Method setter = getMethod(objectClass, "set" + propName);		
//		
//		Method clientFinder = getMethod(service.getClass(), clientFinderName, String.class);
//		Method finder = getMethod(service.getClass(), finderName, String.class);
//		Method saver = getMethod(service.getClass(), saverName, objectClass);
//
//		if (arePrefixesToSwap(client, oldPrefix, newPrefix)) {
//			try {
//				PoClient other = (PoClient) clientFinder.invoke(newPrefix);
//				if (other == null || other.equals(client)) {
//					if (other == null) { // client was not updated yet
//						client.setPersonEmployeeIdPrefix(newPrefix);
//						saver.invoke(client);
//					}
//					@SuppressWarnings("unchecked")
//					List<Object> entities = (List<Object>) finder.invoke(client, DateTools.now());
//					for (Object entity : entities) {
//							setter.invoke(entity, getUpdatedString((String) getter.invoke(entity), oldPrefix, newPrefix));
//							saver.invoke(entity);
//					}
//					logger.info("Updated prefixes of " + objectClass.getSimpleName() + " "+ entities.size() + " entities from " + oldPrefix + " to " + newPrefix);
//				} else {
//					logger.error(MSG);
//					throw new RuntimeException(MSG);
//				}
//			} catch (Exception e) {
//				String msg = "Exception while changing prefix from " + oldPrefix + " to " + newPrefix + ": " + e;
//				logger.error(msg, e);
//				throw new RuntimeException(msg, e);
//			}
//		}
//	}

	public static boolean arePrefixesToSwap(PoClient client, String oldPrefix, String newPrefix) {
		
		if (client == null) {
			logger.info("No client to update prefixes from " + oldPrefix + " to " + newPrefix);
			return false;
		}
		if (StringUtils.isEmpty(oldPrefix) && StringUtils.isEmpty(newPrefix) || 
				oldPrefix != null && oldPrefix.equals(newPrefix)) {
			logger.info("The prefixes " + oldPrefix + " and " + newPrefix + " are same. No update.");
			return false;
		}
		return true;
	}
	
	public static String getUpdatedString(String text, String oldPrefix, String newPrefix) {
		
		// no old prefix
		if (StringUtils.isEmpty(oldPrefix)) {
			if (StringUtils.isEmpty(newPrefix))
				return text;
			if (text == null)
				return newPrefix;
			if (text.startsWith(newPrefix))
				return text;
			return newPrefix + text;
		}
		// old prefix existing
		if (text == null)
			return newPrefix;
		if (text.indexOf(oldPrefix) != 0) // without old prefix
			return (newPrefix == null ? "" : newPrefix) + text;
		return (newPrefix == null ? "" : newPrefix) + text.substring(oldPrefix.length());
	}

//	public static Method getMethod(Class<?> clzz, String name, Class<?> ...paramTypes) {
//		try {
//			return clzz.getMethod(name, paramTypes);
//		} catch (Exception e) {
//			String msg = "Exception while getting method " + name;
//			logger.error(msg + e, e);
//			throw new RuntimeException(msg + e, e);
//		}
//	}
}
