package at.workflow.webdesk.tools.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
	Topologisches Sortieren bedeutet, dass die Knoten nach ihrer
	Abhängigkeit von anderen sortiert werden.
	Hat ein Knoten Kind-Knoten, so ist er abhängig von ihnen.
	Ansonst bleibt die Reihenfolge so wie sie im uebergebenen Graphen
	vorhanden war.
	Der Graph kann auch Zyklen haben, diese werden ignoriert.
	<p>
	Algorithmus:
	<pre>
		Betrachte alle unerreichten Knoten. 
		Betrachte alle unerreichten Nachfolgeknoten rekursiv.
		Gibt es keine unerreichten Nachfolgeknoten mehr, gib den Knoten aus.
	</pre>
	ACHTUNG: Die Knoten (die das Interface Node implementieren muessen) muessen <i>hashCode()</i>
	implementieren, wenn sie <i>equals()</i> ueberschreiben. Ansonst laufen sie Gefahr,
	in der intern verwendeten Hashtable nicht mehr gefunden zu werden.
	
	@author Fritz Ritzberger, 2005
*/
public class TopSort
{
	/**
	 * Objekte, die topologisch sortiert werden wollen, implementieren diese Schnittstelle.
	 * Die Methoden equals() und hashCode() muessen implementiert werden, um Zyklen
	 * im Graph entdecken zu koennen.
	 */
	public interface Node
	{
		/** Liefert true wenn dieser Knoten nicht ein Ordner ist. */
		public boolean isLeaf();

		/** Liefert die Liste der assoziierten Knoten, null wenn dieser Knoten kein Ordner ist. */
		public Node[] children();
	}

	/**
	 * Default-implementierung des interface Node.
	 */
	public static class SortNode implements Node
	{
		public final String name;
		private List<Node> children;

		public SortNode(String name) {
			this(name, null);
		}
		
		public SortNode(String name, Node [] children) {
			this.name = name;
			
			if (children != null)
				for (Node child : children)
					addChild(child);
		}

		@Override
		public boolean isLeaf() {
			return children == null || children.size() <= 0;
		}
		@Override
		public Node [] children() {
			return children.toArray(new Node[children.size()]);
		}
		public void addChild(Node child) {
			if (children == null)
				children = new ArrayList<Node>();
			children.add(child);
		}

		@Override
		public boolean equals(Object o) {
			return name.equals(((SortNode) o).name);
		}
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	
	private List<Node> result = new ArrayList<Node>();

	/**
	 * Sortiert die uebergebene Hierarchie in eine Liste,
	 * unabhaengige Knoten voran, abhaengige danach.
	 */
	public TopSort(Node root)	{
		this(new Node[] { root });
	}
	/**
	 * Sortiert die uebergebene Hierarchien in eine einzige Liste,
	 * unabhaengige Knoten voran, abhaengige danach.
	 */
	public TopSort(Node [] nodes)	{
		loopChildren(nodes, new HashSet<Node>());
	}

	/** Liefert eine topologisch sortierte Liste mit allen Elementen aus dem Baum. */
	public List<Node> getResult() {
		return result;
	}

	
	private void loopChildren(Node [] nodes, Set<Node> done)	{
		for (int i = 0; i < nodes.length; i++)
			checkDependency(nodes[i], done);
	}

	private void checkDependency(Node node, Set<Node> done)	{
		if (done.contains(node) == false) { // wenn unerreichter Knoten
			done.add(node); // vermerke als erreicht

			if (node.isLeaf() == false) // wenn dieser Abhaengigkeiten hat
				loopChildren(node.children(), done); // erreiche erst alle Knoten, von denen dieser abhaengt

			result.add(0, node); // hat nun keine unerreichten Nachfolger mehr
		}
	}

}
