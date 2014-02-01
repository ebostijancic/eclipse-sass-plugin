package at.workflow.webdesk.po.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.workflow.tools.ImageUtils;
import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.ImageAware;
import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.PoImageService;
import at.workflow.webdesk.po.impl.daos.PoImageDAOImpl;
import at.workflow.webdesk.po.model.PoImage;
import at.workflow.webdesk.po.model.PoImageDimensions;
import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * Service for handling of images.
 * 
 * Resizing and croping result can be done in place or by creation of new, unsaved image
 * to prevent everybody in webdesk obtaining croped/resized image from cache.
 * 
 * @author sdzuban 11.02.2013
 */
public class PoImageServiceImpl implements PoImageService {

	private PoImageDAOImpl imageDAO;
	private PoGeneralDbService dbService;
	
	private Map<String, PoImageDimensions> beanMap = new HashMap<String, PoImageDimensions>();
	
	/** {@inheritDoc} */
	@Override
	public void saveImage(PoImage image) {
		imageDAO.save(image);
	}
	
	/** {@inheritDoc} */
	@Override
	public PoImage getImage(String uid) {
		return imageDAO.get(uid);
	}
	
	/** {@inheritDoc} */
	@Override
	public void deleteImage(PoImage image) {
		imageDAO.delete(image);
	}
	
	/** {@inheritDoc} */
	@Override
	public void deleteImageAware(ImageAware imageAware) {
		
		if (imageAware.getOriginal() != null)
			deleteImage(imageAware.getOriginal());
		if (imageAware.getImage() != null)
			deleteImage(imageAware.getImage());
		if (imageAware.getThumbnail() != null)
			deleteImage(imageAware.getThumbnail());
		dbService.deleteObject((PersistentObject) imageAware);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<PoImage> findImageByFileName(String fileName) {
		return imageDAO.findImageByFileName(fileName);
	}

	
	/** {@inheritDoc} */
	@Override
	public PoImage createImage(byte[] imageBytes, String filename) {
		
		ImageUtils.ImageMetaInformation metaInfo = ImageUtils.getImageMetaInformation(imageBytes);
		return new PoImage(imageBytes, filename, metaInfo.getMimeType(), metaInfo.getHeight(), metaInfo.getWidth());
	}
	
	/** {@inheritDoc} */
	@Override
	public PoImage updateImage(PoImage image, byte[] imageBytes, String filename) {
		
		ImageUtils.ImageMetaInformation metaInfo = ImageUtils.getImageMetaInformation(imageBytes);
		image.setImageBytes(imageBytes);
		image.setMimeType(metaInfo.getMimeType());
		image.setFilename(filename);
		image.setHeight(metaInfo.getHeight());
		image.setWidth(metaInfo.getWidth());
		return image;
	}

	/** {@inheritDoc} */
	@Override
	public PoImage resizeImageToHeight(PoImage sourceImage, final int targetHeight, boolean inPlace) {
		
		if (sourceImage == null)
			throw new RuntimeException("Cannot resize null image");
		if (sourceImage.getMimeType() == null)
			throw new RuntimeException("Cannot resize image without mime type");
		if (sourceImage.getHeight() == 0)
			throw new RuntimeException("Cannot resize image with 0 height.");
		
		byte[] imageBytes = ImageUtils.resizeImageToHeight(sourceImage.getImageBytes(), targetHeight, sourceImage.getFilename());
		if(inPlace)
			return updateImage(sourceImage, imageBytes, sourceImage.getFilename());
		return createImage(imageBytes, sourceImage.getFilename());
	}
	
	/** {@inheritDoc} */
	@Override
	public PoImage resizeImageToWidth(PoImage sourceImage, final int targetWidth, boolean inPlace) {

		if (sourceImage == null)
			throw new RuntimeException("Cannot resize null image");
		if (sourceImage.getMimeType() == null)
			throw new RuntimeException("Cannot resize image without mime type");
		if (sourceImage.getWidth() == 0)
			throw new RuntimeException("Cannot resize image with 0 width.");
		
		byte[] imageBytes = ImageUtils.resizeImageToWidth(sourceImage.getImageBytes(), targetWidth, sourceImage.getFilename());
		if(inPlace)
			return updateImage(sourceImage, imageBytes, sourceImage.getFilename());
		return createImage(imageBytes, sourceImage.getFilename());
	}


	/** {@inheritDoc} */
	@Override
	public PoImage resizeImage(PoImage sourceImage, final float factor, boolean inPlace) {

		if (sourceImage == null)
			throw new RuntimeException("Cannot resize null image");
		
		int targetHeight = Math.round(sourceImage.getHeight() * factor);
		return resizeImageToHeight(sourceImage, targetHeight, inPlace);
	}

	/** {@inheritDoc} */
	@Override
	public PoImage fitImageToMaxDimensions(PoImage sourceImage, int maxWidth, int maxHeight, boolean inPlace) {

		if (sourceImage == null)
			throw new RuntimeException("Cannot resize null image");
		
		if (maxWidth <= 0 || maxHeight <= 0)
			throw new RuntimeException("Cannot resize to zero or negative dimension");
		
		// image fits the dimensionas already
		if (sourceImage.getWidth() <= maxWidth && sourceImage.getHeight() <= maxHeight) {
			if (inPlace)
				return sourceImage;
			return createImage(sourceImage.getImageBytes(), sourceImage.getFilename());
		}
		
		float widthRatio = (float) maxWidth / (float) sourceImage.getWidth(); 
		float heightRatio = (float) maxHeight / (float) sourceImage.getHeight();
		
		// both dimensions must obey their maximums
		float ratio = Math.min(widthRatio, heightRatio); 
		
		return resizeImage(sourceImage, ratio, inPlace);
	}
	
	/** {@inheritDoc} */
	@Override
	public PoImage cropImage(PoImage sourceImage, final int startWidth, final int startHeight, final int width, final int height, boolean inPlace) {
		
		if (sourceImage == null)
			throw new RuntimeException("Cannot crop null image");
		if (sourceImage.getMimeType() == null)
			throw new RuntimeException("Cannot crop image without mime type");
		
		byte[] croppedImage = ImageUtils.cropImage(sourceImage.getImageBytes(), startWidth, startHeight, width, height, sourceImage.getFilename());
		if(inPlace)
			return updateImage(sourceImage, croppedImage, sourceImage.getFilename());
		return createImage(croppedImage, sourceImage.getFilename());
	}

	/** {@inheritDoc} */
	@Override
	public PoImage resizeAndCropImageToWidthAndHeight(PoImage image, int targetWidth, int targetHeight, boolean inPlace) {
		
		PoImage imageToHeight = resizeImageToHeight(image, targetHeight, inPlace);
		int width = imageToHeight.getWidth();
		
		// original and required proportions are same
		if (width == targetWidth)
			return imageToHeight;
		
		// original is wider than required proportion -> crop sides
		if (width > targetWidth) {
			int diff = width - targetWidth;
			return cropImage(imageToHeight, diff/2, 0, targetWidth, targetHeight, inPlace);
		}
		
		// original is taller than required proportion -> crop top and bottom
		PoImage imageToWidth = resizeImageToWidth(image, targetWidth, inPlace);
		int height = imageToWidth.getHeight();
		int diff = height - targetHeight;
		return cropImage(imageToWidth, 0, diff/2, targetWidth, targetHeight, inPlace);
	}
	
	/** {@inheritDoc} */
	@Override
	public void generateImageAndThumbnail(ImageAware images) {
		
		init();
		
		if (images == null)
			throw new IllegalArgumentException("Images must be specified");
		if (images.getOriginal() == null)
			throw new IllegalArgumentException("Images must contain original");
		
		boolean hasImage = images.getImage() != null;
		boolean hasThumbnail = images.getThumbnail() != null;
		PoImageDimensions dims = beanMap.get(images.getClass().getCanonicalName());
		
		// create a copy
		PoImage cropped = updateImage(new PoImage(), images.getOriginal().getImageBytes(), images.getOriginal().getFilename());
		if (images.getCropWidth() != 0 || images.getCropHeight() != 0)
			// the required crop
			cropped = cropImage(images.getOriginal(), images.getCropStartX(), images.getCropStartY(), images.getCropWidth(), images.getCropHeight(), false);

		if (dims != null) {
			// trim to required dimensions and generate thumbnail
			
			// resize and crop to width and height only if keep aspect ratio
			PoImage image = cropped;
			if(dims.isIgnoreAspectRatio() == false)
				image = resizeAndCropImageToWidthAndHeight(cropped, dims.getTargetWidth(), dims.getTargetHeight(), false);
			
			if (hasImage)
				updateImage(images.getImage(), image.getImageBytes(), image.getFilename());
			else
				images.setImage(image);
			
			// thumbnail will not be affected by aspect ratio.
			PoImage thumbnail = resizeAndCropImageToWidthAndHeight(cropped, dims.getThumbnailWidth(), dims.getThumbnailHeight(), false);
			if (hasThumbnail)
				updateImage(images.getThumbnail(), thumbnail.getImageBytes(), thumbnail.getFilename());
			else
				images.setThumbnail(thumbnail);
		} else {
			// no dimensions, no thumbnail
			if (hasImage)
				updateImage(images.getImage(), cropped.getImageBytes(), cropped.getFilename());
			else
				images.setImage(cropped);
		}
	}

	/** {@inheritDoc} */
	@Override
	public PoImageDimensions getImageDimensions(ImageAware images) {
		init();
		return beanMap.get(images.getClass().getCanonicalName());
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<String, PoImageDimensions> getImageDimensionsMap() {
		init();
		return Collections.unmodifiableMap(beanMap);
	}

	public void setImageDAO(PoImageDAOImpl imageDAO) {
		this.imageDAO = imageDAO;
	}

	public void setDbService(PoGeneralDbService dbService) {
		this.dbService = dbService;
	}
	
	/**
	 * Reads all dimension beans and creates map where key is the class the bean is applicable to
	 */
	private void init() {
		if (!beanMap.isEmpty())
			return;
		Map<String, PoImageDimensions> beans = WebdeskApplicationContext.getApplicationContext().getBeansOfType(PoImageDimensions.class);
		for (PoImageDimensions dimBean : beans.values())
			beanMap.put(dimBean.getApplicableTo(), dimBean);
	}

}
