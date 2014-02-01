package at.workflow.webdesk.po.model;

import javax.persistence.Transient;

/**
 * Parent-child relation between 2 groups, for defining a historicizable parent-child relation.
 * Although this is a separate table (and would allow m:n relations), this is a hierarchical relation.
 * 
 * @author ggruber, hentner
 */
@SuppressWarnings("serial")
public class PoParentGroup extends PoDayHistorization implements Comparable<PoParentGroup>{

	private String uid;
	private PoGroup parentGroup;
	private PoGroup childGroup;
	private int ranking; 
	
	/**
	 * The value defines in which order the childs of the group 
	 * are returned.
	 * 
	 * @return a <code>int</code> value.
	 */
	public int getRanking() {
		return ranking;
	}

	public void setRanking(Integer ranking) {
		if (ranking != null)
			this.ranking = ranking.intValue();
		else
			this.ranking=0;
	}

    public PoGroup getParentGroup() {
        return parentGroup;
    }

	public void setParentGroup(PoGroup parentGroup) {
	    this.parentGroup = parentGroup;
	}

    public PoGroup getChildGroup() {
        return childGroup;
    }

    public void setChildGroup(PoGroup childGroup) {
        this.childGroup = childGroup;
    }

    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }

    /** Utility for more intuitive code: <code>orgService.getParentGroup(group).group()</code>. @return the parentGroup field. */
    @Transient
    public PoGroup group() {
        return parentGroup;
    }

    @Override
	public String toString() {
    	if (getParentGroup() == null || getChildGroup() == null)
    		return "[uid=" + getUID() + "] parent or child is null: this relation is not yet initialised correctly!";
    	
    	return getParentGroup().getShortName() +
    			" is parent of " + getChildGroup().getShortName() + 
    			" from " + getValidfrom() +
    			" to " + getValidto() +
    			" [" + getUID() + "]";
    }

	@Override
	public int compareTo(PoParentGroup pg) {
		return equals(pg) ? 0 : pg.getChildGroup().getShortName().compareTo(getChildGroup().getShortName());
	}

}
