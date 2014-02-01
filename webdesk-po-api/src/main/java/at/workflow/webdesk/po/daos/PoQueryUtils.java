package at.workflow.webdesk.po.daos;

public interface PoQueryUtils {
	
	public void refreshObject(Object obj);
	
	public void evictObject(Object obj);
	
	public String generateUID();
}
