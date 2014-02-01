package at.workflow.webdesk.po.impl.test;

import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoImageService;
import at.workflow.webdesk.po.model.PoImage;
import at.workflow.webdesk.po.model.PoImageDimensions;
import at.workflow.webdesk.po.model.PoPersonImages;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * @author sdzuban 11.02.2013
 */
public class WTestPoImageService extends AbstractTransactionalSpringHsqlDbTestCase {

	private static final String FILE_NAME = "WTestPoImageService.jpg";
	private static final int WIDTH = 120;
	private static final int HEIGHT = 120;
	private static final int MAX_WIDTH = 96;
	private static final int MAX_HEIGHT = 96;
	
	private PoImageService service;
	private byte[] imageBytes;
	private PoImage source, image;
	private PoImageDimensions dimensionsBean;
	
	/** {@inheritDoc} */
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		if (service == null) {
			service = (PoImageService) getBean("PoImageService");
			Resource imageResource = new ClassPathResource("at/workflow/webdesk/po/impl/test/" + FILE_NAME);
			imageBytes = FileUtils.readFileToByteArray(imageResource.getFile());
		
			Map<String, PoImageDimensions> beans = WebdeskApplicationContext.getApplicationContext().getBeansOfType(PoImageDimensions.class);
			for (PoImageDimensions dimBean : beans.values())
				if (PoPersonImages.class.getCanonicalName().equals(dimBean.getApplicableTo())) {
					dimensionsBean = dimBean;
					break;
				}
		}
		
	}
	
	public void testCRUD() {
		
		image = service.createImage(imageBytes, FILE_NAME);
		
		assertNotNull(image);
		assertNull(image.getUID());
		assertNotNull(image.getImageBytes());
		assertEquals(FILE_NAME, image.getFilename());
		assertEquals("image/jpeg", image.getMimeType());
		assertEquals(HEIGHT, image.getHeight());
		assertEquals(WIDTH, image.getWidth());
		
		service.saveImage(image);
		
		String uid = image.getUID();
		assertNotNull(uid);
		
		PoImage image2 = service.getImage(uid);
		assertEquals(image, image2);
		
		List<PoImage> found = service.findImageByFileName(FILE_NAME);
		assertNotNull(found);
		assertEquals(1, found.size());
		assertEquals(image, found.get(0));
		
		service.deleteImage(image);
		
		image2 = service.getImage(uid);
		assertNull(image2);
		
		found = service.findImageByFileName(FILE_NAME);
		assertNotNull(found);
		assertTrue(found.isEmpty());
	}
	
	public void testResizeToHeight() {
		
		source = service.createImage(imageBytes, FILE_NAME);
		service.saveImage(source);
		
		image = service.resizeImageToHeight(source, HEIGHT * 2, false);
		assertImage();
		assertNull(image.getUID());
		
		image = service.resizeImageToHeight(source, HEIGHT * 2, true);
		assertImage();
		assertEquals(source, image);
	}

	public void testResizeToWidth() {
		
		source = service.createImage(imageBytes, FILE_NAME);
		service.saveImage(source);
		
		image = service.resizeImageToWidth(source, WIDTH * 2, false);
		assertImage();
		assertNull(image.getUID());
		
		image = service.resizeImageToWidth(source, WIDTH * 2, true);
		assertImage();
		assertEquals(source, image);
	}
	
	public void testResize() {
		
		source = service.createImage(imageBytes, FILE_NAME);
		service.saveImage(source);
		
		image = service.resizeImage(source, 2.0f, false);
		assertImage();
		assertNull(image.getUID());
		
		image = service.resizeImage(source, 2.0f, true);
		assertImage();
		assertEquals(source, image);
	}
	
	public void testFit() {
		
		source = service.createImage(imageBytes, FILE_NAME);
		service.saveImage(source);
		
		image = service.fitImageToMaxDimensions(source, MAX_WIDTH, MAX_HEIGHT, false);
		assertImageMaxDims();
		assertNull(image.getUID());
		image = service.fitImageToMaxDimensions(source, 2*MAX_WIDTH, MAX_HEIGHT, false);
		assertImageMaxDims();
		image = service.fitImageToMaxDimensions(source, MAX_WIDTH, 2*MAX_HEIGHT, false);
		assertImageMaxDims();

		image = service.fitImageToMaxDimensions(source, MAX_WIDTH, MAX_HEIGHT, true);
		assertImageMaxDims();
		assertEquals(source, image);
		
	}
	
	public void testCrop() {
		
		source = service.createImage(imageBytes, FILE_NAME);
		service.saveImage(source);
		
		image = service.cropImage(source, 20, 20, 80, 80, false);

		assertNotNull(image);
		assertNull(image.getUID());
		assertNotNull(image.getImageBytes());
		assertEquals(FILE_NAME, image.getFilename());
		assertEquals("image/jpeg", image.getMimeType());
		assertEquals(80, image.getHeight());
		assertEquals(80, image.getWidth());
		
		image = service.cropImage(source, 20, 20, 80, 80, true);
		
		assertNotNull(image);
		assertEquals(source, image);
		assertNotNull(image.getImageBytes());
		assertEquals(FILE_NAME, image.getFilename());
		assertEquals("image/jpeg", image.getMimeType());
		assertEquals(80, image.getHeight());
		assertEquals(80, image.getWidth());
	}

	public void testResizeAndCropImageToHeightAndWidth() {
		
		testResizeAndCropImageToHeightAndWidth(false);
		
		testResizeAndCropImageToHeightAndWidth(true);
	}
	
	private void testResizeAndCropImageToHeightAndWidth(boolean inPlace) {
		
		source = service.createImage(imageBytes, FILE_NAME);
		service.saveImage(source);
		
		// magnify, with proportion
		PoImage result = service.resizeAndCropImageToWidthAndHeight(source, WIDTH * 2, HEIGHT * 2, inPlace);
		assertNotNull(result);
		if (inPlace)
			assertEquals(source, result);
		else
			assertNull(result.getUID());
		assertEquals(HEIGHT * 2, result.getHeight());
		assertEquals(WIDTH * 2, result.getWidth());
		
		// reduce, with proportion
		result = service.resizeAndCropImageToWidthAndHeight(source, WIDTH / 2, HEIGHT / 2, inPlace);
		assertNotNull(result);
		if (inPlace)
			assertEquals(source, result);
		else
			assertNull(result.getUID());
		assertEquals(HEIGHT / 2, result.getHeight());
		assertEquals(WIDTH / 2, result.getWidth());
		
		// magnify, without proportion, crop in width
		result = service.resizeAndCropImageToWidthAndHeight(source, WIDTH, HEIGHT * 3 / 2, inPlace);
		assertNotNull(result);
		if (inPlace)
			assertEquals(source, result);
		else
			assertNull(result.getUID());
		assertEquals((HEIGHT * 3 / 2), result.getHeight());
		assertEquals(WIDTH, result.getWidth(), 1);
		
		// reduce, without proportion, crop in width
		result = service.resizeAndCropImageToWidthAndHeight(source, WIDTH, HEIGHT * 3 / 4, inPlace);
		assertNotNull(result);
		if (inPlace)
			assertEquals(source, result);
		else
			assertNull(result.getUID());
		assertEquals(HEIGHT * 3 / 4, result.getHeight());
		assertEquals(WIDTH, result.getWidth(), 1);
		
		// magnify, without proportion, crop in height
		result = service.resizeAndCropImageToWidthAndHeight(source, WIDTH * 3 / 2, HEIGHT, inPlace);
		assertNotNull(result);
		if (inPlace)
			assertEquals(source, result);
		else
			assertNull(result.getUID());
		assertEquals((HEIGHT), result.getHeight(), 1);
		assertEquals(WIDTH * 3 / 2, result.getWidth());
		
		// reduce, without proportion, crop in height
		result = service.resizeAndCropImageToWidthAndHeight(source, WIDTH * 3 / 4, HEIGHT, inPlace);
		assertNotNull(result);
		if (inPlace)
			assertEquals(source, result);
		else
			assertNull(result.getUID());
		assertEquals(HEIGHT, result.getHeight(), 1);
		assertEquals(WIDTH * 3 / 4, result.getWidth());
	}
	
	public void testGenerateImageAndThumbnailNew() {
		
		assertNotNull(dimensionsBean);
		
		// new image and thumbnail 
		source = service.createImage(imageBytes, FILE_NAME);
		service.saveImage(source);
		
		PoPersonImages pim = new PoPersonImages();
		pim.setOriginal(source);
		service.generateImageAndThumbnail(pim);
		
		PoImage orig = pim.getOriginal();
		assertNotNull(orig);
		assertEquals(source, orig);
		assertEquals(source.getImageBytes(), orig.getImageBytes());
		
		PoImage img = pim.getImage();
		assertNotNull(img);
		assertNull(img.getUID());
		assertEquals(dimensionsBean.getTargetWidth(), img.getWidth());
		assertEquals(dimensionsBean.getTargetHeight(), img.getHeight());
		
		PoImage thumb = pim.getThumbnail();
		assertNotNull(thumb);
		assertNull(thumb.getUID());
		assertEquals(dimensionsBean.getThumbnailWidth(), thumb.getWidth());
		assertEquals(dimensionsBean.getThumbnailHeight(), thumb.getHeight());
		
	}

	public void testGenerateImageAndThumbnailOverwrite() {
		
		assertNotNull(dimensionsBean);
		
		// overwrite image and thumbnail 
		source = service.createImage(imageBytes, FILE_NAME);
		service.saveImage(source);
		
		PoPersonImages pim = new PoPersonImages();
		pim.setOriginal(source);
		service.generateImageAndThumbnail(pim);
		
		PoImage img = pim.getImage();
		service.saveImage(img);
		PoImage thumb = pim.getThumbnail();
		service.saveImage(thumb);
		
		// overwrite image and thumbnail 
		service.generateImageAndThumbnail(pim);
		
		assertNotNull(pim.getImage());
		assertEquals(img, pim.getImage());
		assertNotNull(pim.getThumbnail());
		assertEquals(thumb, pim.getThumbnail());
	}
	
	private void assertImage() {
		
		assertNotNull(image);
		assertNotNull(image.getImageBytes());
		assertEquals(FILE_NAME, image.getFilename());
		assertEquals("image/jpeg", image.getMimeType());
		assertEquals(HEIGHT * 2, image.getHeight());
		assertEquals(WIDTH * 2, image.getWidth());
	}
	
	private void assertImageMaxDims() {
		
		assertNotNull(image);
		assertNotNull(image.getImageBytes());
		assertEquals(FILE_NAME, image.getFilename());
		assertEquals("image/jpeg", image.getMimeType());
		assertEquals(MAX_HEIGHT, image.getHeight());
		assertEquals(MAX_WIDTH, image.getWidth());
	}
	
}
