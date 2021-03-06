package scriptease.model.complex;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JComponent;

import scriptease.controller.ModelAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;

/**
 * A story group represents a collection of StoryNodes {@link StoryNode} using
 * the Composite Design Pattern. In other words, a story group is a collection
 * of story points {@link StoryPoint} and/or other story groups.
 * 
 * A story group can be expanded or collapsed and contains a SEGraph
 * {@link SEGraph} representation of itself.
 * 
 * @author jyuen
 */
public class StoryGroup extends StoryNode {

	public static final String STORY_GROUP_TYPE = "storyGroup";

	private static final String NEW_STORY_GROUP = "New Story Group";

	private StoryNode startNode;
	private StoryNode exitNode;

	private boolean expanded;

	public StoryGroup() {
		this(StoryGroup.NEW_STORY_GROUP, new HashSet<StoryNode>(), null, null,
				false);
	}

	/**
	 * Creates a new Story Group using the collection of StoryNodes
	 * {@link StoryNode}. The startNode and exitNode must be a part of this
	 * group or bad things will happen.
	 * 
	 * @param name
	 *            The name of the story group.
	 * @param storyNodes
	 *            The nodes that this group comprises of.
	 * @param startNode
	 *            The node that gets attached to this groups parents if the
	 *            group is ever ungrouped.
	 * @param exitNode
	 *            The node that gets attached to this groups successors if the
	 *            group is ever ungrouped.
	 * @param expanded
	 *            Whether the group is currently expanded or collapsed.
	 */
	public StoryGroup(String name, Collection<StoryNode> storyNodes,
			StoryNode startNode, StoryNode exitNode, boolean expanded) {
		super();

		this.registerChildType(StoryPoint.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(StoryGroup.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		this.startNode = startNode;
		this.exitNode = exitNode;

		this.expanded = expanded;

		for (StoryNode storyNode : storyNodes) {
			this.addStoryChild(storyNode);
		}

		this.successors = new HashSet<StoryNode>();
		this.parents = new HashSet<StoryNode>();
		this.uniqueID = this.getNextStoryNodeCounter();

		if (name == null || name.equals("")) {
			name = StoryGroup.NEW_STORY_GROUP;
		}

		this.setDisplayText(name);
	}

	/**
	 * Returns the start node of the group.
	 * 
	 * @return
	 */
	public StoryNode getStartNode() {
		return this.startNode;
	}

	/**
	 * Returns the exit node of the group.
	 * 
	 * @return
	 */
	public StoryNode getExitNode() {
		return this.exitNode;
	}

	/**
	 * Gets all the story points in this group and all subgroups.
	 * 
	 * @return
	 */
	public Collection<StoryPoint> getAllStoryPoints() {
		return this.getAllStoryPoints(this);
	}

	private Collection<StoryPoint> getAllStoryPoints(StoryGroup group) {
		final Collection<StoryPoint> storyPoints = new HashSet<StoryPoint>();

		for (StoryComponent child : group.getChildren()) {
			if (child instanceof StoryPoint) {
				storyPoints.add((StoryPoint) child);
			} else if (child instanceof StoryGroup) {
				storyPoints.addAll(this.getAllStoryPoints((StoryGroup) child));
			}
		}

		return storyPoints;
	}
	
	/**
	 * Gets all the story nodes in this group.
	 * 
	 * @return
	 */
	public Collection<StoryNode> getAllStoryNodes() {
		return this.getAllStoryNodes(this);
	}
	
	private Collection<StoryNode> getAllStoryNodes(StoryGroup group){
		final Collection<StoryNode> storyNodes = new HashSet<StoryNode>();
		for (StoryComponent child : group.getChildren()) {
			if (child instanceof StoryNode)
				storyNodes.add((StoryNode) child);
		}

		return storyNodes;
	}

	/**
	 * Returns the SEGraph associated with this group.
	 * 
	 * @return
	 */
	public SEGraph<StoryNode> getSEGraph() {
		return this.buildSEGraph();
	}

	private SEGraph<StoryNode> buildSEGraph() {
		final SEGraph<StoryNode> seGraph;

		seGraph = SEGraphFactory.buildStoryGraph(this.startNode,
				ScriptEaseUI.COLOUR_GROUP_BACKGROUND, false);

		seGraph.addSEGraphObserver(new SEGraphAdapter<StoryNode>() {

			@Override
			public void nodesSelected(final Collection<StoryNode> nodes) {
				SEModelManager.getInstance().getActiveModel()
						.process(new ModelAdapter() {
							@Override
							public void processStoryModel(StoryModel storyModel) {
								storyModel.getStoryComponentPanelTree()
										.setRoot(nodes.iterator().next());
							}
						});
			}
		});

		seGraph.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();

				component.setCursor(ScriptEaseUI.CURSOR_SELECT);
			}
		});

		return seGraph;
	}

	/**
	 * Returns true if the group is expanded.
	 * 
	 * @return
	 */
	public Boolean isExpanded() {
		return this.expanded;
	}

	/**
	 * Sets the start node of the StoryGroup.
	 * 
	 * @param startNode
	 */
	public void setStartNode(StoryNode startNode) {
		this.startNode = startNode;
	}

	/**
	 * Sets the exit node of the StoryGroup.
	 * 
	 * @param exitNode
	 */
	public void setExitNode(StoryNode exitNode) {
		this.exitNode = exitNode;
	}

	/**
	 * Sets whether the StoryGroup is expanded.
	 * 
	 * @param expanded
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	@Override
	public StoryGroup clone() {
		return (StoryGroup) super.clone();
	}

	@Override
	public String toString() {
		return "StoryGroup (\"" + this.getDisplayText() + "\")";
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processStoryGroup(this);
	}
}
