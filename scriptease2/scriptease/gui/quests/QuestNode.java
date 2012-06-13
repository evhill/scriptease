package scriptease.gui.quests;

import java.util.List;

import scriptease.controller.GraphNodeVisitor;
import scriptease.gui.graph.nodes.GraphNode;

/**
 * QuestNode is a node in the QuestGraph which represents a Quest. They can be
 * collapsed to hide the internal QuestPoint or Quests of the represented Quest.
 * On the contrary, they can be expanded to show the internal QuestPoints and
 * Quests which represent the given Quest.
 * 
 * @author mfchurch
 * 
 */
public class QuestNode extends GraphNode {
	private GraphNode startPoint;
	private GraphNode endPoint;
	private boolean collapsed;
	private String name;

	/**
	 * Constructor used by QuestNodeConverter
	 */
	public QuestNode() {
		super();
	}

	public QuestNode(String name, GraphNode start, boolean collapsed) {
		this(name, start, null, collapsed);
	}

	public QuestNode(String name, GraphNode start, GraphNode end,
			boolean collapsed) {
		super();
		this.name = name;
		this.startPoint = start;
		this.collapsed = collapsed;
		this.setEndPoint(end);
	}

	@Override
	public void process(GraphNodeVisitor processController) {
		processController.processQuestNode(this);
	}

	@Override
	public boolean isTerminalNode() {
		return this.children.isEmpty();
	}

	/**
	 * Asks the QuestNodePanel
	 * 
	 * @return
	 */
	public boolean isCollapsed() {
		return this.collapsed;
	}

	/**
	 * Finds the next node that matches the desiredFanIn with the same depth as
	 * the source of the given children. Opposite algorithm of findPreviousEnd
	 * 
	 * @param children
	 * @param desiredFanIn
	 * @return
	 */
	private GraphNode findNextEnd(final List<GraphNode> children,
			final int desiredFanIn) {
		// for each of the children
		for (GraphNode child : children) {
			// Get the child's fan in
			final int fanIn = child.getParents().size();
			// If the child's fan in is the desired we've found it!
			if (fanIn == desiredFanIn)
				return child;

			// Get the child's fan out
			final int fanOut = child.getChildren().size();
			// If it is greater than one, skip the sub group
			if (fanOut > 1) {
				GraphNode endOfSubGroup = findNextEnd(child.getChildren(),
						fanOut);
				if (endOfSubGroup == null)
					throw new IllegalStateException(
							"Cannot process invalid graphs");
				child = endOfSubGroup;
			}
			// then continue looking for the desiredFanIn
			GraphNode nextEnd = findNextEnd(child.getChildren(), desiredFanIn);
			if (nextEnd != null)
				return nextEnd;
		}
		// no valid next node was found
		return null;
	}

	/**
	 * Forwards to QuestNodePanel
	 * 
	 * @param collapsed
	 */
	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	@Override
	public String toString() {
		return "QuestNode [" + this.name + "]";
	}

	public GraphNode getStartPoint() {
		return this.startPoint;
	}

	public GraphNode getEndPoint() {
		return this.endPoint;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStartPoint(GraphNode start) {
		this.startPoint = start;
	}

	public void setEndPoint(GraphNode end) {
		if (end != null) {
			// Check if end is a descendant of start
			if (!end.isDescendant(this.startPoint))
				throw new IllegalArgumentException("Invalid Quest Graph");
		} else {
			// Calculate the closest valid end node
			end = this.findNextEnd(this.startPoint.getChildren(),
					this.startPoint.getChildren().size());
			if (end == null)
				throw new IllegalArgumentException(
						"No valid QuestNode end found for " + this.startPoint);
		}

		// move the children of the endPoint to the QuestNode's children
		List<GraphNode> children = end.getChildren();
		if (!children.isEmpty()) {
			for (GraphNode child : children) {
				child.removeParents();
				this.addChild(child);
			}
		}

		this.endPoint = end;
	}

	@Override
	public boolean represents(Object object) {
		return false;
	}
}
