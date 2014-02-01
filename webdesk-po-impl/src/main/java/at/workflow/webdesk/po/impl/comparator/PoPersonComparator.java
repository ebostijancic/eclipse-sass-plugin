package at.workflow.webdesk.po.impl.comparator;

import java.util.Comparator;

import at.workflow.webdesk.po.model.PoPerson;

/**
 * Compares <code>PoPerson</code> objects according to their fullname.
 *
 * @author hentner
 */
public class PoPersonComparator implements Comparator<PoPerson> {

	@Override
	public int compare(PoPerson p1, PoPerson p2) {
		return p1.getFullName().compareTo(p2.getFullName());
	}

}
