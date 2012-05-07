package scriptease.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.graph.nodes.KnowItNode;
import scriptease.gui.graph.nodes.TextNode;
import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestPointNode;

/**
 * GraphNodeVisitor used to resolve referencing problems created when cloning
 * GraphNodes. Replaces references to GraphNode nodes with a reference to the
 * first found equal GraphNode.
 * 
 * @author mfchurch
 * 
 */
public class GraphNodeReferenceResolver implements GraphNodeVisitor {
	private Collection<GraphNode> nodes;

	/**
	 * Iterate over the graph twice, first time compiling original nodes -
	 * second time replacing equal references with the original
	 * 
	 * @param graphNode
	 */
	public void resolveReferences(GraphNode graphNode) {
		nodes = new ArrayList<GraphNode>();

		// Use a set to group equal nodes together
		Set<GraphNode> equalSet = new HashSet<GraphNode>(graphNode
				.getNodeDepthMap().keySet());
		nodes.addAll(equalSet);

		// fix the problems
		graphNode.process(this);
	}

	@Override
	public void processTextNode(TextNode textNode) {
		processDefault(textNode);
	}

	@Override
	public void processKnowItNode(KnowItNode knowItNode) {
		processDefault(knowItNode);
	}

	private void processDefault(GraphNode graphNode) {
		final List<GraphNode> children = graphNode.getChildren();
		// for each child
		for (GraphNode child : children) {
			boolean replaced = false;
			// if it is not original
			for (GraphNode originalNode : nodes) {
				// replace it with the original
				if (originalNode.equals(child) && originalNode != child) {
					replaced = graphNode.replaceChild(child, originalNode);
					break;
				}
			}
			if (!replaced)
				child.process(this);
		}
	}

	@Override
	public void processQuestPointNode(QuestPointNode questPointNode) {
		processDefault(questPointNode);
	}

	@Override
	public void processQuestNode(QuestNode questNode) {
		processDefault(questNode);
	}
}
