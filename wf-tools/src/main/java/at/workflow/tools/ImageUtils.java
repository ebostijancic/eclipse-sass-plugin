package at.workflow.tools;

import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;

import eu.medsea.util.MimeUtil;

/**
 * This is the utility for handling images.
 * 
 * @author sdzuban 11.02.2013
 */
public class ImageUtils {

	/**
	 * this class is used to store the meta information
	 * of an image. By just opening the stream once and retrieving
	 * the meta information, this service should not generate a high load.
	 * @author ebostijancic 18.10.2012
	 */
	public static class ImageMetaInformation {
		private int width;
		private int height;
		private String mimeType;
		
		public ImageMetaInformation(int width, int height, String mimeType) {
			super();
			this.width = width;
			this.height = height;
			this.mimeType = mimeType;
		}

		public int getWidth() {
			return width;
		}
		public int getHeight() {
			return height;
		}
		public String getMimeType() {
			return mimeType;
		}
	}
	
	/**
	 * method used to resize a image to the designated height. The scaling is proportional
	 * so both width and height will have the original proportions.
	 * @param sourceImage source image to resize.
	 * @param targetHeight target height of the resized image.
	 * @param filename of the image
	 * @return resized image with height = targetHeight
	 */
	public static byte[] resizeImageToHeight(byte[] sourceImage, final int targetHeight, String filename) {
		
		BufferedImage bufferedOriginalImage = getBufferedImageFromByteArray(sourceImage);
		BufferedImage resizedBufferedImage = Scalr.resize(bufferedOriginalImage, 
				Scalr.Mode.FIT_TO_HEIGHT, targetHeight, Scalr.OP_ANTIALIAS);
		return getByteArrayFromBufferedImage(resizedBufferedImage, filename);
	}
	
	/**
	 * method used to resize a image to the designated width. The scaling is proportional
	 * so both width and height will have the original proportions.
	 * @param sourceImage source image to resize.
	 * @param targetWidth target height of the resized image.
	 * @param filename of the image
	 * @return resized image with height = targetHeight
	 */
	public static byte[] resizeImageToWidth(byte[] sourceImage, final int targetWidth, String filename) {
		
		BufferedImage bufferedOriginalImage = getBufferedImageFromByteArray(sourceImage);
		BufferedImage resizedBufferedImage = Scalr.resize(bufferedOriginalImage, 
				Scalr.Mode.FIT_TO_WIDTH, targetWidth, Scalr.OP_ANTIALIAS);
		return getByteArrayFromBufferedImage(resizedBufferedImage, filename);
	}

	/**
	 * method used to resize a image by using a resize factor. The factor is used to multiply
	 * the height and with and resizeImage is invoked with the results of the multiplication.
	 * 
	 * f.i.: factor = 0.5 => half of the size of the sourceImage.
	 * 
	 * @param sourceImage source image to resize.
	 * @param sourceHeight original height of the image
	 * @param factor decimal factor to resize the image to.
	 * @param filename of the image
	 * @return resized image by the factor.
	 */
	public static byte[] resizeImage(byte[] sourceImage, final int sourceHeight, final float factor, String filename) {
		
		assert Math.round(sourceHeight * factor) > 0;
		
		int targetHeight = Math.round(sourceHeight * factor);
		return resizeImageToHeight(sourceImage, targetHeight, filename);
	}

	/**
	 * method used to crop a part of an image. Cartesian coordinates of the start
	 * and end point of the selected area are used from the params.
	 * @param sourceImage source image.
	 * @param startX x-coordinate of start point.
	 * @param startY y-coordinate of start point.
	 * @param endX x-coordinate of end point.
	 * @param endY y-coordinate of end point.
	 * @param filename of the image
	 * @return cropped part of source image.
	 * @throws IOException 
	 * @throws ImagingOpException 
	 * @throws IllegalArgumentException 
	 */
	public static byte[] cropImage(byte[] sourceImage, final int startX, final int startY, final int endX, final int endY, String filename) {
		BufferedImage bufferedOriginalImage = getBufferedImageFromByteArray(sourceImage);
		BufferedImage croppedImage = Scalr.crop(bufferedOriginalImage, 
				startX, startY, endX, endY, Scalr.OP_ANTIALIAS);
		return getByteArrayFromBufferedImage(croppedImage, filename);
	}


	// HELPER METHODS
	
	/**
	 * helper method used to extract meta information from byte array.
	 * 
	 * @param content
	 * @return ImageMetaInformation object containing the meta information.
	 * @throws IOException
	 */
	public static ImageMetaInformation getImageMetaInformation(byte[] content) {
		
		InputStream stream = new ByteArrayInputStream(content);
		String mimeType = MimeUtil.getMimeType(stream);
		
		BufferedImage image = null;
		try {
			image = ImageIO.read(stream);			
		} catch(IOException e) {
			throw new RuntimeException(e.getCause());
		}
				
		return new ImageMetaInformation(image.getWidth(), image.getHeight(), mimeType);
	}
	
	/**
	 * helper method used to create a byte array from an buffered image. The filename is used
	 * for storing the image into the ByteArrayOutputStream with the right image format.
	 * 
	 * @param image
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static byte[] getByteArrayFromBufferedImage(BufferedImage image, String filename) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			// FilenameUtils returns the correct extension when having dots in the filename.
			ImageIO.write(image, FilenameUtils.getExtension(filename), stream);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return stream.toByteArray();
	}
	
	/**
	 * helper method used to create a BufferedImage from byte array content by using ImageIO.read()
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage getBufferedImageFromByteArray(byte[] content) {
		
		InputStream stream = new ByteArrayInputStream(content);
		BufferedImage image = null;
		try {
			image = ImageIO.read(stream);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return image;
	}

}
