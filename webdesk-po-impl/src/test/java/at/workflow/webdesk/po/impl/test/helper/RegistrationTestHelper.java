package at.workflow.webdesk.po.impl.test.helper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;

import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.daos.PoQueryUtils;
import at.workflow.webdesk.po.impl.PoModuleUpdateServiceImpl;
import at.workflow.webdesk.po.model.PoRegistrationBean;
import at.workflow.webdesk.tools.DaoJdbcUtil;

public class RegistrationTestHelper {
	
    public static PoRegistrationBean createRegistrationBean(String module) {
    	PoRegistrationBean registrationBean = new PoRegistrationBean();
    	
    	registrationBean.setImplFolder("impl");
    	registrationBean.setFolderOfPackage(module);
    	
    	List<String> regActionsList = new ArrayList<String>();
    	regActionsList.add("classpath*:/at/workflow/webdesk/po/impl/test/regdata/" + module + "/actions/*/act-descr.xml");
    	registrationBean.setRegisterActions(regActionsList);

    	List<String> regTextModulesList = new ArrayList<String>();
    	regTextModulesList.add("classpath*:/at/workflow/webdesk/po/impl/test/regdata/" + module + "/actions/*/i18n.xml");
    	regTextModulesList.add("classpath*:/at/workflow/webdesk/po/impl/test/regdata/" + module + "/actions/i18n.xml");
    	registrationBean.setSyncTextModules(regTextModulesList);
    	
    	return registrationBean;

    }
    
	public static PoModuleUpdateServiceImpl createModuleUpdateServiceManually(ApplicationContext appCtx) {
		// this has to be done as we want to avoid autoproxying through spring
		PoModuleUpdateServiceImpl moduleUpdateService = new PoModuleUpdateServiceImpl();
		moduleUpdateService.setApplicationContext(appCtx);
		moduleUpdateService.setDaoJdbcUtil((DaoJdbcUtil) appCtx.getBean("DaoJdbcUtil"));
		moduleUpdateService.setModuleService((PoModuleService) appCtx.getBean("PoModuleService"));
		moduleUpdateService.setQueryUtils((PoQueryUtils) appCtx.getBean("PoQueryUtils"));
		return moduleUpdateService;
	}
	
	public static void clearRegistrationTables(ApplicationContext appCtx) {
		DaoJdbcUtil daoJdbcUtil = (DaoJdbcUtil) appCtx.getBean("DaoJdbcUtil");
		daoJdbcUtil.execute("delete from PoJobTrigger", "webdesk");
		daoJdbcUtil.execute("delete from PoJob", "webdesk");
    	daoJdbcUtil.execute("delete from PoTextModule", "webdesk");
    	daoJdbcUtil.execute("delete from PoActionParameter", "webdesk");
    	daoJdbcUtil.execute("delete from PoActionCache", "webdesk");
    	daoJdbcUtil.execute("delete from PoAction", "webdesk");
    	daoJdbcUtil.execute("delete from PoLanguage", "webdesk");
    	daoJdbcUtil.execute("delete from PoModule", "webdesk");
	}

}
