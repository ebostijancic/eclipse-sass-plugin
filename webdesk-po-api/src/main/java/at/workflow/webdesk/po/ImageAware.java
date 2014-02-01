package at.workflow.webdesk.po;

import at.workflow.webdesk.po.model.PoImage;

/**
 * This interface specifies methods
 * that are needed for handling of images and thumbnails.
 * 
 * @author sdzuban 12.02.2013
 */
public interface ImageAware {

	/** @return the original image as uploaded */
	PoImage getOriginal();
	
	/** @return image saved with the object */
	PoImage getImage();
	
	/** @return thumbnail saved with the object */
	PoImage getThumbnail();
	
	/** assigns original image to the object */
	void setOriginal(PoImage original);
	
	/** assigns cropped and resized image to the object */
	void setImage(PoImage image);
	
	/** assigns thumbnail to the object */
	void setThumbnail(PoImage thumbnail);
	
//	------------------- CROPPING METHODS --------------------------
	
	/** @return x coordinate of cropping left top corner */
	int getCropStartX();
	
	/** @return y coordinate of cropping left top corner */
	int getCropStartY();
	
	/** @return width of cropping */
	int getCropWidth();
	
	/** @return height of cropping */
	int getCropHeight();
	
	/** set x coordinate of cropping left top corner */
	void setCropStartX(int x);
	
	/** set y coordinate of cropping left top corner */
	void setCropStartY(int y);
	
	/** set width of cropping */
	void setCropWidth(int width);
	
	/** set height of cropping */
	void setCropHeight(int height);
	
}
