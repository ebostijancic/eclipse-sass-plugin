package at.workflow.webdesk.po;
/**
 * This interface defines methods for handling of share 
 * as e.g. share of cost center on the overall costs.
 * 
 * It is defined as Number to allow any number type to be used 
 * like Integer for relative share of the cost in percents 
 * or Double for absolute cost share.
 * 
 * @author sdzuban 02.04.2013
 */
public interface ShareAware {

	/** @return share of this object on the whole sum */
    Number getShare();

    /** sets share of this object on the whole sum */
    void setShare(Number share);
}
