package at.workflow.webdesk.po.model;
/**
 * Objects of this class encapsulate 
 * target dimensions of images 
 * for specified class.
 * 
 * They are intended to be instantiated
 * as singleton spring beans in application context.
 * 
 * @author sdzuban 18.02.2013
 */
public class PoImageDimensions {

	/** classname for which these dimensions are applicable to */
	private String applicableTo;
	
	private int originalMaxWidth;
	private int originalMaxHeight;
	private int targetWidth;
	private int targetHeight;
	private int thumbnailWidth;
	private int thumbnailHeight;
	private boolean ignoreAspectRatio = false;
	
	
	public int getOriginalMaxWidth() {
		return originalMaxWidth;
	}
	public void setOriginalMaxWidth(int originalMaxWidth) {
		this.originalMaxWidth = originalMaxWidth;
	}
	public int getOriginalMaxHeight() {
		return originalMaxHeight;
	}
	public void setOriginalMaxHeight(int originalMaxHeight) {
		this.originalMaxHeight = originalMaxHeight;
	}
	public String getApplicableTo() {
		return applicableTo;
	}
	public void setApplicableTo(String applicableTo) {
		this.applicableTo = applicableTo;
	}
	public int getTargetWidth() {
		return targetWidth;
	}
	public void setTargetWidth(int targetWidth) {
		this.targetWidth = targetWidth;
	}
	public int getTargetHeight() {
		return targetHeight;
	}
	public void setTargetHeight(int targetHeight) {
		this.targetHeight = targetHeight;
	}
	public int getThumbnailWidth() {
		return thumbnailWidth;
	}
	public void setThumbnailWidth(int thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}
	public int getThumbnailHeight() {
		return thumbnailHeight;
	}
	public void setThumbnailHeight(int thumbnailHeight) {
		this.thumbnailHeight = thumbnailHeight;
	}
	public boolean isIgnoreAspectRatio() {
		return ignoreAspectRatio;
	}
	public void setIgnoreAspectRatio(boolean ignoreAspectRatio) {
		this.ignoreAspectRatio = ignoreAspectRatio;
	}	
}
