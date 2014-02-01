package at.workflow.webdesk.po.impl.test.mocks;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoUtilService;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.pagination.FilterAndSortPaginationCursor;
import at.workflow.webdesk.tools.pagination.PaginationCursor;
import at.workflow.webdesk.tools.pagination.PaginationDataProvider;
import at.workflow.webdesk.tools.pagination.impl.CriteriaPaginationCursor;

/**
 * mock object with no real implementation
 * 
 * @author ggruber
 *
 */
public class PoUtilMockService implements PoUtilService {


	public CriteriaPaginationCursor createCriteriaPage(Object searchObj,
			Object exampleObj, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	public FilterAndSortPaginationCursor createQueryPaginationCursor(String queryName, String[] queryParameters,
			Object[] obj, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	public void evictObject(Object obj) {
		// TODO Auto-generated method stub

	}

	public String generateUID() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCacheElementCount(String cacheId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getCacheSize(String cacheIdentifier) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getMethodName(String fieldName, String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isStringInGroup(PoGroup group, List searchList) {
		// TODO Auto-generated method stub
		return false;
	}

	public Date mergeDateAndTime(Date date, Date time) {
		// TODO Auto-generated method stub
		return null;
	}

	public void refreshObject(Object obj) {
		// TODO Auto-generated method stub

	}

	public void sendMail(String originator, PoPerson sendTo, String subject,
			String template, HashMap variables, List filename) {
		// TODO Auto-generated method stub

	}

	public void sendMail(String originator, PoGroup sendToGroup,
			String subject, String template, HashMap variables, List filename) {
		// TODO Auto-generated method stub

	}

	public void sendMail(String originator, String sendTo, String subject,
			String template, HashMap variables, List filenames, List byteArrays) {
		// TODO Auto-generated method stub

	}

	public void sendMail(String originator, List sendTo, String subject,
			String template, HashMap variables, List filenames, List byteArrays) {
		// TODO Auto-generated method stub

	}

	public void sendMail(String originator, String sendTo, String subject,
			String template, Map variables, List filenames, List byteArrays,
			String mimeType) {
		// TODO Auto-generated method stub

	}

	public void sendMail(String originator, String[] sendTo, String[] copyTo,
			String[] blindCopyTo, String subject, String template,
			HashMap variables, List filenames, List byteArrays) {
		// TODO Auto-generated method stub

	}

	public void sendMail(String originator, List emailAdressesAsList,
			String subject, String template, Map variables, List filenames,
			List byteArrays, String mimeType) {
		// TODO Auto-generated method stub

	}

	public void testThrowException() {
		// TODO Auto-generated method stub

	}

	public List<String> getEmailAdresses(PoGroup group) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Object> convertBeanToMap(Object o,
			List<String> fieldNames) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object convertMapToBean(Object bean, Map<String, Object> map,
			List<String> fieldNames) {
		// TODO Auto-generated method stub
		return null;
	}

	public String generateTemporaryAccessPermission(String userName,
			String sourceActionURL, String targetActionURL) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasTemporaryAccess(String userName, String sourceActionURL,
			String targetActionURL, String securityToken) {
		// TODO Auto-generated method stub
		return false;
	}

	public Thread decorateWithSession(Thread thread) {
		// TODO Auto-generated method stub
		return null;
	}

	public Thread decorateWithSession(Thread thread,
			boolean useCurrentRequestSession) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterAndSortPaginationCursor createQueryPaginationCursor(PositionalQuery query, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterAndSortPaginationCursor createQueryPaginationCursor(NamedQuery query, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterAndSortPaginationCursor createQueryPaginationCursor(String queryName, String[] queryParameters, Object[] obj, int pageSize, PaginationDataProvider alternateDataProvider) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PaginationCursor createListPage(List<?> elements, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date addBusinessDays(Date date, int businessDays) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getNextBusinessDay(Date from) {
		// TODO Auto-generated method stub
		return null;
	}


}
