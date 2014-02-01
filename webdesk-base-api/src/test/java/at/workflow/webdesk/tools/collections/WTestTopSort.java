package at.workflow.webdesk.tools.collections;

import java.util.List;

import junit.framework.TestCase;
import at.workflow.webdesk.tools.collections.TopSort.Node;
import at.workflow.webdesk.tools.collections.TopSort.SortNode;

/**
 * @author fritzberger 23.12.2013
 */
public class WTestTopSort extends TestCase
{
	public void testTopSort() throws Exception	{
		// create instances with dependencies
		final SortNode d = new SortNode("d");
		final SortNode e = new SortNode("e");
		final SortNode f = new SortNode("f");
		final SortNode a = new SortNode("a", new Node[] { d, e, f });
		final SortNode b = new SortNode("b");
		final SortNode c = new SortNode("c", new Node [] { b, a });	// multiple parents
		final SortNode g = new SortNode("g", new Node[] { a, b, c });
		f.addChild(g);	// cycle

		final SortNode [] testArray = new SortNode [] { d, e, f, a, b, c, g };
		
		final TopSort sort = new TopSort(testArray);
		final List<Node> result = sort.getResult();
		
		// result must be: [g, c, b, a, f, e, d]
		assertEquals(f, result.get(0));
		assertEquals(g, result.get(1));
		assertEquals(c, result.get(2));
		assertEquals(b, result.get(3));
		assertEquals(a, result.get(4));
		assertEquals(e, result.get(5));
		assertEquals(d, result.get(6));
		
	}

}
