package at.workflow.webdesk.tools.injectionHelper;

import java.util.ArrayList;
import java.util.List;

public class InjectionList extends ArrayList<Object> {
	
	private List<ElementsModificationListener> listeners = new ArrayList<ElementsModificationListener>();
	
	public interface ElementsModificationListener {
		public void elementsChanged(List<?> elements);
	}

	private static final long serialVersionUID = 1L;

	public void setElements(List<?> c) {
		this.clear();
		this.addAll(c);
		
		for (ElementsModificationListener listener : listeners) {
			listener.elementsChanged( c );
		}
	}
	
	public List<?> getElements() {
		return this;
	}
	
	public void installListener(ElementsModificationListener listener) {
		if ( !listeners.contains( listener ))
			listeners.add( listener );
	}
	
}
