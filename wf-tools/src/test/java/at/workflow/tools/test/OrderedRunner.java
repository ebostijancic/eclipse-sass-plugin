package at.workflow.tools.test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Provides sort order for test-methods within a unit test.
 * For example see OrderedExampleTest.
 * 
 * @see http://stackoverflow.com/questions/3089151/specifying-an-order-to-junit-4-tests-at-the-method-level-not-class-level
 * @author fritzberger 11.09.2012
 */
public class OrderedRunner extends BlockJUnit4ClassRunner
{
	public OrderedRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		final List<FrameworkMethod> list = super.computeTestMethods();
		
		Collections.sort(list, new Comparator<FrameworkMethod>() {
			@Override
			public int compare(FrameworkMethod f1, FrameworkMethod f2) {
				final Order o1 = f1.getAnnotation(Order.class);
				final Order o2 = f2.getAnnotation(Order.class);

				if (o1 == null || o2 == null)
					return -1;	// put to front

				return o1.order() - o2.order();
			}
		});
		
		return list;
	}

}
