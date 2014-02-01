package at.workflow.webdesk.po;

import java.awt.image.ImagingOpException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.model.PoImage;
import at.workflow.webdesk.po.model.PoImageDimensions;

/**
 * Service for image handling
 * incl. resizing and cropping.
 * 
 * It contains basic crud methods
 * and two kind of resize/crop methods:
 * - those that return new image without UID and never resize/crop the original image
 * - those that resize/crop "in place" by modifying the original image
 * 
 * Cropped/resized images are created with original image as original.
 * 
 * @author ebostijancic 17.10.2012
 * @author sdzuban 11.02.2013 CRUD
 *
 */
public interface PoImageService {
	
	/**
	 * method used to save a PoImage.
	 * @param image
	 */
	void saveImage(PoImage image);
	
	/**
	 * Method to retrieve PoImage
	 * @param uid
	 * @return PoImage with the uid
	 */
	PoImage getImage(String uid);

	/**
	 * Deletes image from database
	 * This is simple delete and works only 
	 * if there is no image pointing to this image as original
	 * @param image
	 */
	void deleteImage(PoImage image);
	
	/**
	 * Deletes imageAware completely inclusive 
	 * original, image and thumbnail PoImages from database
	 * This is simple delete and works only 
	 * if there is no object pointing to this imageAware
	 * @param imageAware
	 */
	void deleteImageAware(ImageAware imageAware);
	
	/**
	 * @param fileName
	 * @return all the images with the given filename
	 */
	List<PoImage> findImageByFileName(String fileName);
	
	/**
	 * For creation of the image by gui
	 * @param original optional
	 * @param imageBytes mandatory
	 * @param filename
	 * @return PoImage
	 */
	PoImage createImage(byte[] imageBytes, String filename);
	
	/**
	 * For update of image in place
	 * @param image image to be updated
	 * @param imageBytes
	 * @param filename
	 * @return PoImage updated image object
	 */
	PoImage updateImage(PoImage image, byte[] imageBytes, String filename);
	
	/**
	 * method used to resize an image to the designated width. The scaling is 
	 * proportional and both width and height match the original proportions.
	 * SourceImage is put as original to created image.
	 * @param sourceImage source image to resize.
	 * @param targetWidth target width of the resized image.
	 * @param inPlace shall the change be performed to sourceImage or shall new PoImage be created?
	 * @return resized image with width = targetWidth. 
	 * It is either new PoImage object when inPlace = false, or updated sourceImage when inPlace = true
	 */
	PoImage resizeImageToWidth(PoImage sourceImage, final int targetWidth, boolean inPlace);
	
	/**
	 * method used to resize an image to the designated height. The scaling is 
	 * proportional and both width and height match the original proportions.
	 * SourceImage is put as original to created image.
	 * @param sourceImage source image to resize.
	 * @param targetHeight target height of the resized image.
	 * @param inPlace shall the change be performed to sourceImage or shall new PoImage be created?
	 * @return resized image with height = targetHeight.
	 * It is either new PoImage object when inPlace = false, or updated sourceImage when inPlace = true
	 */
	PoImage resizeImageToHeight(PoImage sourceImage, final int targetHeight, boolean inPlace);
	
	/**
	 * method used to fit an image to the maximal width and height. The scaling is 
	 * proportional and both width and height match the original proportions.
	 * Image is never magnified. If it is too big, it is scaled down so that 
	 * both height and width are less or equal their respective maximums, but never more than is really necessary.
	 * I.e. the result image is either as small as it was or it is scaled down to fit tightly to maxWidth and maxHeight. 
	 * @param sourceImage source image to resize.
	 * @param maxWidth maximal width of the resized image.
	 * @param maxHeight maximal height of the resized image.
	 * @param inPlace shall the change be performed to sourceImage or shall new PoImage be created?
	 * @return resized image with height = targetHeight.
	 * It is either new PoImage object when inPlace = false, or updated sourceImage when inPlace = true
	 */
	PoImage fitImageToMaxDimensions(PoImage sourceImage, final int maxWidth, final int maxHeight, boolean inPlace);
	
	/**
	 * method used to resize an image by using a resize factor.
	 * The height and width of the resulting image are multiplied by the factor.
	 * 
	 * f.i.: factor = 0.5 => half of the size of the sourceImage.
	 * 
	 * SourceImage is put as original to created image.
	 * @param sourceImage source image to resize.
	 * @param factor decimal factor to resize the image to.
	 * @param inPlace shall the change be performed to sourceImage or shall new PoImage be created?
	 * @return resized image by the factor.
	 * It is either new PoImage object when inPlace = false, or updated sourceImage when inPlace = true
	 */
	PoImage resizeImage(PoImage sourceImage, final float factor, boolean inPlace);
	
	/**
	 * method used to crop a part of an image. Cartesian coordinates of the start
	 * and end point of the selected area are used from the params.
	 * SourceImage is put as original to created image.
	 * @param sourceImage source image.
	 * @param x x-coordinate of start point.
	 * @param y y-coordinate of start point.
	 * @param width width of the image.
	 * @param height height of the image.
	 * @param inPlace shall the change be performed to sourceImage or shall new PoImage be created?
	 * @return cropped source image.
	 * It is either new PoImage object when inPlace = false, or updated sourceImage when inPlace = true
	 * @throws IOException 
	 * @throws ImagingOpException 
	 * @throws IllegalArgumentException 
	 */
	PoImage cropImage(PoImage sourceImage, final int x, final int y, final int width, final int height, boolean inPlace);
	
	/**
	 * Resizes image to fill target height and width while maintaining the proportions. 
	 * If source proportions and target proportions are not equal the image is resized so
	 * that the targetHeight x targetWidth rectangle is completely filled 
	 * and either height == targetHeight and width > targetWidth
	 * or height > targetHeight and width == targetWidth.
	 * The surplus dimension is symmetrically cropped. 
	 * SourceImage is put as original to created image.
	 * @param sourceImage to be resized and cropped
	 * @param targetWidth
	 * @param targetHeight
	 * @param inPlace shall the change be performed to sourceImage or shall new PoImage be created?
	 * @return resized image with width = targetWidth and height = targetHeight. 
	 * It is either new PoImage object when inPlace = false, or updated sourceImage when inPlace = true
	 */
	PoImage resizeAndCropImageToWidthAndHeight(PoImage sourceImage, int targetWidth, int targetHeight, boolean inPlace);

	/**
	 * This method accepts full class name of the ImageAware implementation
	 * and images containing the original and the cropping coordinates and sizes.
	 * It generates cropped image according to cropping info,
	 * crops/resizes it to measures defined by corresponding PoImageDimensions bean
	 * for image and for thumbnail.
	 * Original image and thumbnail information (if any) is overwritten.
	 * @param images must contain original image
	 */
	void generateImageAndThumbnail(ImageAware images);
	
	/**
	 * Retrieves the images dimensions applicable for images object 
	 * @param images
	 * @return dimensions defined for the class of images 
	 */
	PoImageDimensions getImageDimensions(ImageAware images);
	
	/** 
	 * @return map with all PoImageDimensions object defined in application context.
	 * Key is ImageAware class name the dimensions apply to.
	 */
	Map<String, PoImageDimensions> getImageDimensionsMap();
}
