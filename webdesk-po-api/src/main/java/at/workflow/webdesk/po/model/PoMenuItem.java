package at.workflow.webdesk.po.model;


import java.util.ArrayList;
import java.util.List;
/**
 * Represents a Menuitem within the Webdesk Menutree. It can either be a Folder or a 
 * concrete link to an action <code>PoAction</code>
 * 
 * FIXME: divide this class into subclasses: 
 * PoMenuItemActionLink, PoMenuItemFolder, PoMenuItemTemplateLink
 * 
 * @author ggruber
 */
@SuppressWarnings("serial")
public class PoMenuItem extends PoHistorization implements Comparable<PoMenuItem> {

	private String uid;												// uid of the entity
    private String name;											// name of menuitem
    private int ranking;											// defines ranking among sister menuitems
    private String description;										// FIXME: not clear, why needed 
    private String iconPath;										// FIXME: this is kind of useless info, check why its needed
    
    private PoMenuItem parent;										// parent menuitem = folder
    private PoAction action;										// linked action if its NO folder
    private List<PoMenuItem> childs = new ArrayList<PoMenuItem>();	// list of child menutitems, if this is a folder
    private PoClient client;										// links menutree to client -> FIXME wrong datamodelling
    private String textModuleKey;									// textmodule to use, if it is a folder!

    /**
     * http://intranet/intern/ifwd_mgm.nsf/0/7D6078F1489FD3DEC1257576004B5944?OpenDocument 
     * notes://Miraculix/intern/ifwd_mgm.nsf/0/7D6078F1489FD3DEC1257576004B5944?EditDocument
     */
    private Integer templateId;										// FIXME: wrong data modelling -> see: link above! 

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getIconPath() {
        return iconPath;
    }
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }

    public PoMenuItem getParent() {
        return parent;
    }

    public void setParent(PoMenuItem parent) {
        this.parent = parent;
    }

    public List<PoMenuItem> getChilds() {
        return childs;
    }

    public void setChilds(List<PoMenuItem> value) {
        childs = value;
    }

    public boolean addChild(PoMenuItem element) {
    	element.setParent(this);
        return childs.add(element);
    }
    
    public boolean hasChilds() {
    	return childs != null && ! childs.isEmpty();
    }

    public PoAction getAction() {
        return action;
    }

    public void setAction(PoAction value) {
        action = value;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }
    
	public PoClient getClient() {
		return client;
	}

	public void setClient(PoClient client) {
		this.client = client;
	}
	
	public String getTextModuleKey() {
		return textModuleKey;
	}

	public void setTextModuleKey(String textModuleKey) {
		this.textModuleKey = textModuleKey;
	}

	public Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public int compareTo(PoMenuItem mi) {
		int res =  this.ranking - mi.ranking;
		if (res==0) 
		    if (mi.getUID().equals(this.getUID()))
		        return 0;
		    else 
		        return -1;
		else
		    return res;
	}
    
    @Override
	public String toString() {
        String ret = "PoMenuItem[" + 
            ", name=" + this.name +
            ", description=" + this.description + 
            ", textModuleKey=" + this.textModuleKey +
            ", action=" + this.action +            
            ", ranking=" + this.ranking +
            ", iconPath=" + this.iconPath +
            ", parent=" + this.parent +
            ", uid=" + this.uid + "]";
        return ret;
    }

    /**
     * @return a PoMenuItem
     * 
     * every value is copied, except the parent and child relationship and the UID
     */
    public PoMenuItem cloneSoft() {
        PoMenuItem newItem = new PoMenuItem();
        newItem.setAction(this.getAction());
        newItem.setClient(this.client);
        newItem.setDescription(this.getDescription());
        newItem.setIconPath(this.getIconPath());
        newItem.setName(this.getName());
        newItem.setRanking(this.getRanking());
        newItem.setTextModuleKey(this.getTextModuleKey());
        newItem.setValidfrom(this.getValidfrom());
        newItem.setValidto(this.getValidto());
        return newItem;
    }
	
}
