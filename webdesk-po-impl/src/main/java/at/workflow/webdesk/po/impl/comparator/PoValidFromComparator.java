package at.workflow.webdesk.po.impl.comparator;

import java.util.Comparator;
import java.util.Date;

import at.workflow.webdesk.po.model.PoHistorization;

public class PoValidFromComparator implements Comparator<PoHistorization> {

    @Override
	public int compare(PoHistorization o1, PoHistorization o2) {
        try {
            Date vf1_date = o1.getValidfrom();
            Date vf2_date = o2.getValidfrom();
            if (vf1_date.before(vf2_date))
                return -1;
            else
                if (vf2_date.after(new Date()))
                    return 1;
                else {
                    if (o1.getUID().equals(o2.getUID()))
                        return 0;
					return 1;
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
