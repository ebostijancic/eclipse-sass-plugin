package at.workflow.webdesk.po.impl.test;

import java.util.List;

import at.workflow.webdesk.po.impl.daos.PoImageDAOImpl;
import at.workflow.webdesk.po.model.PoImage;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author sdzuban 11.02.2013
 */
public class WTestCrudPoImageDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoImage> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoImage.class);
		super.onSetUpAfterDataGeneration();
	}

	public void testFindByFileName() {
		
		PoImageDAOImpl dao = (PoImageDAOImpl) getBean("PoImageDAO");
		
		List<PoImage> result = dao.findImageByFileName("FileName");
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		PoImage img = new PoImage(new byte[] {}, "FileName", "png");
		dao.save(img);

		result = dao.findImageByFileName("FileName");
		
		assertNotNull(result);
		assertEquals(1, result.size());
		
		img = new PoImage(new byte[] {}, "FileName2", "png");
		dao.save(img);
		
		result = dao.findImageByFileName("FileName");
		
		assertNotNull(result);
		assertEquals(1, result.size());
		
		img = new PoImage(new byte[] {}, "FileName", "png");
		dao.save(img);
		
		result = dao.findImageByFileName("FileName");
		
		assertNotNull(result);
		assertEquals(2, result.size());
		
	}
}
