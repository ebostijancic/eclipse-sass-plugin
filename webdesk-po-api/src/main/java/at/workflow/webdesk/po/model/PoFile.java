package at.workflow.webdesk.po.model;

import java.util.Date;

import at.workflow.webdesk.po.Configurable;

/**
 * Class which represents a File which is used for various purposes 
 * inside webdesk. Can be linked to module, action, connector or job
 * 
 * FIXME: divide into subclasses to get it more clear (for each linked
 * object), move types / mimetypes into enums
 * overthink historization -> still needed?
 * 
 * @author ggruber
 */
@SuppressWarnings("serial")
public class PoFile extends PoBase implements Cloneable {
	
	private String uid;
	private String fileId;						// fileId and versionNumber together form a unique key
	private int versionNumber;
	private String path;						// a string representation of the name (does not have to be unique)
	private String mimeType;					// the mime-type of the file
	private byte[] content;						// the actual content					
	private int size;							// size of content
	
	// linked objects (one of the following)
	private PoAction action;
	private PoConnector connector;
	private PoModule module;
	private PoJob job;
	
	// historization -> redundant to versions
	private Date validfrom;						// is used to historize content, is redundant to versionnumber!
	private Date validto;
	
	private Date timeStamp;
	
	/** 
	 * @deprecated a categorization not used at the moment
	 */
	private int type;
	

    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public PoAction getAction() {
        return action;
    }

    public void setAction(PoAction action) {
        this.action = action;
    }
    
    public void setConnector(PoConnector connector) {
		this.connector = connector;
	}
    
    public PoConnector getConnector() {
        return connector;
    }

    public PoModule getModule() {
        return module;
    }
    public void setModule(PoModule module) {
        this.module = module;
    }
    
    public Date getValidfrom() {
        return validfrom;
    }

    public void setValidfrom(Date validfrom) {
        this.validfrom = validfrom;
    }

    public Date getValidto() {
        return validto;
    }

    public void setValidto(Date validto) {
        this.validto = validto;
    }

    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }
    
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }
    
    /** @return Returns the timeStamp. */
    public Date getTimeStamp() {
        return timeStamp;
    }
    
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    /** @return a fileId which is the same for all Versions of a File (so it is not unique, in contrast to the UID). */
    public String getFileId() {
        return fileId;
    }
    
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    
    public PoJob getJob() {
        return job;
    }
    
    public void setJob(PoJob job) {
        this.job = job;
    }
    
    public void setConfigurable(Configurable configurable) {
    	if (configurable instanceof PoJob) {
    		setJob( (PoJob) configurable );
    	} else if (configurable instanceof PoConnector) {
    		setConnector( (PoConnector) configurable );
    	} else if (configurable instanceof PoAction ) {
    		setAction( (PoAction)configurable );
    	}
    }
    
    @Override
    public PoFile clone() {
    	PoFile newFile = new PoFile();
    	newFile.setFileId(this.getFileId());
		newFile.setValidfrom(this.getValidfrom());
		newFile.setValidto(this.getValidto());
		newFile.setConnector(this.getConnector());
		newFile.setAction(this.getAction());
		newFile.setContent(this.getContent());
		newFile.setJob(this.getJob());
		newFile.setMimeType(this.getMimeType());
		newFile.setModule(this.getModule());
		newFile.setPath(this.getPath());
		newFile.setType(this.getType());
		newFile.setSize(this.getSize());
		newFile.setTimeStamp(new Date());
    	return newFile;
    }
    
    /**
     * Do not use. Public just for Hibernate.
     * Any value will be overwritten on save(), where content length is written into this field.
     * TODO: move logic "size = content.length" from DAO to here by setting size any time content is set.
     */
	public void setSize(Integer size) {
		if (size == null)
			this.size = 0;
		else
			this.size = size;
	}
	
	/**
	 * @return the <code>size</code> of the content of <code>PoFile</code> in bytes in case this
	 * 		was read from persistence and no one called setSize() meanwhile.
	 */
	public int getSize() {
		return size;
	}

    @Override
	public String toString() {
        String ret = "PoFile[" + 
            "fileId=" + fileId +
            ", mimeType=" + mimeType +
            ", path=" + path +
            ", versionNumber=" + versionNumber +
            ", timeStamp=" + timeStamp +
            ", size=" + size +
            ", uid=" + uid + "]";
        return ret;
    }
    
}
