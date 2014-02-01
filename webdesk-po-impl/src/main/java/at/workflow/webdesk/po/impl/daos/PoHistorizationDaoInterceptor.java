package at.workflow.webdesk.po.impl.daos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PoHistorizationDaoInterceptor extends HibernateDaoSupport implements MethodInterceptor {
	
	List dates=new ArrayList();
	
	public void init() {
		
		String[] modelClasses = { "PoPerson", "PoGroup", "PoPersonGroup", "PoParentGroup", "PoRole", "PoRoleHolderPerson", 
				"PoRoleHolderGroup", "PoRoleCompetenceBase", "PoAPermissionBase", "PoAction" }; 
		List ret;
		
		for (int i=0; i<=modelClasses.length; i++) {
			
			ret = getHibernateTemplate().find("select distinct validfrom from " + modelClasses[i]);
			addToDates(ret, "validfrom");
			
			ret = getHibernateTemplate().find("select distinct validto from " + modelClasses[i]);
			addToDates(ret, "validto");
		}
		
		// now sort the list of dates
		// with the youngest being the first
		Collections.sort(dates, Collections.reverseOrder());
		
	}
	
	private void addToDates(List maps, String key) {
		Iterator itr = maps.iterator();
		while (itr.hasNext()) {
			Map map = (Map) itr.next();
			Date date = (Date) map.get(key);
			
			if (!dates.contains(date)) {
				dates.add(date);
			}
		}
	}
	
	
	public Object invoke(MethodInvocation methodInvoc) throws Throwable {
		
		for(int i=0;i<methodInvoc.getArguments().length;i++) {
			if (methodInvoc.getArguments()[i].getClass().getName().endsWith("Date")) {
				
				Date matchingDate = findCachableDate((Date)methodInvoc.getArguments()[i]);
				((Date)methodInvoc.getArguments()[i]).setTime(matchingDate.getTime());
			}
		}
		
		return methodInvoc.proceed();
	}
	
	private Date findCachableDate(Date date) {
		
		if (this.dates.size()==0)
			return new Date();
		
		int idx = Collections.binarySearch(dates, date);

		idx--;
		if (idx<0)
			idx=0;
			
		return (Date) this.dates.toArray()[idx];
		
	}

}
