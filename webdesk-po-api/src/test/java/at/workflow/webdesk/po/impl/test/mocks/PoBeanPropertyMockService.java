package at.workflow.webdesk.po.impl.test.mocks;

import java.util.Collection;
import java.util.List;

import at.workflow.webdesk.po.PoBeanPropertyService;
import at.workflow.webdesk.po.model.PoBeanProperty;

public class PoBeanPropertyMockService implements PoBeanPropertyService {

	public String checkPropertyValue(PoBeanProperty bp, String value)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void copyBeanProperty(String oldBeanName, String oldPropertyName,
			String newBeanName, String newPropertyName) {
		// TODO Auto-generated method stub

	}

	public void deleteBeanProperty(PoBeanProperty beanProperty) {
		// TODO Auto-generated method stub

	}

	public void deleteBeanValue(PoBeanProperty bp, String uid) {
		// TODO Auto-generated method stub

	}

	public PoBeanProperty findBeanPropertyByKey(PoBeanProperty bp) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoBeanProperty findBeanPropertyByKey(String beanName, String property) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoBeanProperty getBeanProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getBeans() {
		// TODO Auto-generated method stub
		return null;
	}


	public void injectAll() {
		// TODO Auto-generated method stub

	}

	public boolean isPropertyAllowedForEditing(PoBeanProperty b) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<String> readBeanNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> readBeanNamesForModule(String moduleUID) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoBeanProperty> readBeanProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoBeanProperty> readBeanPropertiesForBean(String beanName) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoBeanProperty> readBeanPropertiesForModule(String moduleUID) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoBeanProperty> readBeanPropertiesForModuleAndBean(
			String moduleUID, String beanName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveBeanProperty(PoBeanProperty b) {
		// TODO Auto-generated method stub

	}


	public void writeBeanPropertiesToDb() {
		// TODO Auto-generated method stub

	}

	public void updateBeanValue(PoBeanProperty b, String uid, String value) throws Exception {
		// TODO Auto-generated method stub
	}

	public void updateBeanValue(PoBeanProperty b, String uid, String value, int ranking) throws Exception {
		// TODO Auto-generated method stub
	}

	public void updateBeanValueAndInject(PoBeanProperty b, String value) throws Exception {
		// TODO
	}

	@Override
	public void injectAll(Collection<PoBeanProperty> l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBeanProperty(String beanName, String property, Object value) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void registerBeanProperties(List<PoBeanProperty> beans) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeBeanPropertiesToDb(boolean ignoreDatabaseValues) {
		// TODO Auto-generated method stub
		
	}

	
}
