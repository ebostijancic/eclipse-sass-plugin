package at.workflow.webdesk.po.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import at.workflow.webdesk.tools.date.DateTools;

/**
 * Base class for images (pictures).
 * 
 * @author ebostijancic 02.10.2012
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class PoImage extends PoBase {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "IMAGE_UID", length = 32)
	private String UID;
	
	// length is same as in dm module
	@Column(nullable = false, length = 16777216)
	@Lob
	private byte[] imageBytes;
	
	private String filename;
	
	private String mimeType;
	
	private int height;
	
	private int width;

	
	public PoImage() {
	}
	
	public PoImage(byte [] imageBytes, String filename, String mimeType) {
		this.imageBytes = imageBytes;
		this.filename = filename;
		this.mimeType = mimeType;
	}

	public PoImage(byte [] imageBytes, String filename, String mimeType, int height, int width) {
		this(imageBytes, filename, mimeType);
		this.height = height;
		this.width = width;
	}
	
	
	@Override
	public String getUID() {
		return UID;
	}

	@Override
	public void setUID(String uID) {
		UID = uID;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] content) {
		this.imageBytes = content;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	/** @return the UID, optionally followed by the filename up to first "." occurrence (= without any extension). */
	@Transient
	public String getDownloadName() {
		final String downloadName = getUID();
		final int i = (filename != null) ? filename.indexOf('.') : -1;
		return (i > 0) ?  downloadName+filename.substring(i) : downloadName;
	}
	
	/** @return the URL that can be used in HTML pages as &lt;a href="...">, with appended random-value for temporal uniqueness. */
	@Transient
	public String getDownloadUrl()	{
		return "image/"+getDownloadName()+"?rnd="+DateTools.now().getTime();
	}

}
