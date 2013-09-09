package scriptease.gui.SEGraph.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.models.StoryNodeGraphModel;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;

/**
 * Handles all graph grouping functionalities. A graph group represents a set of
 * nodes in a graph {@link SEGraph} that meet the the conditions to form a
 * group. These conditions including having at max one exit and at minimum two
 * nodes.
 * 
 * @author jyuen
 * @author neesha
 */
public class GraphGroupController<E> {

	private final SEGraph<E> graph;

	private Set<E> group;
	private E startNode;

	private final Collection<SEGraph<E>> existingGroups;

	public GraphGroupController(SEGraph<E> graph) {
		this.graph = graph;

		this.group = new HashSet<E>();
		this.startNode = null;

		// TODO : will want to read this from story file later.
		this.existingGroups = new ArrayList<SEGraph<E>>();
	}

	/**
	 * Adds the <code>node</code> to the group.
	 * 
	 * @param node
	 * @return
	 */
	public void addNodeToGroup(E node) {
		if (this.group.isEmpty() && this.isValidGroupStart(node)) {
			this.startNode = node;
			this.group.add(node);
		} else if (startNode != null) {
			// Check whether the node is already in the group
			if (group.contains(node) && node != startNode) {
				group.remove(node);
				this.removeGroupOrphans(node);
			} else {
				// Now we can start forming a group

				// We don't want to add anything behind the start node.
				if (!this.graph.model.getParents(node).contains(startNode)) {
					// We need to find path back to start
					final Set<E> tempGroup = new HashSet<E>();
					final Queue<E> backQueue = new LinkedList<E>();
					boolean foundStart = false;

					// Must always include start in set
					tempGroup.add(startNode);
					tempGroup.add(node);
					backQueue.add(node);

					while (!backQueue.isEmpty() && !foundStart) {
						final E currNode = backQueue.poll();

						for (E parent : this.graph.model.getParents(currNode)) {
							if (parent == startNode) {
								foundStart = true;
							}

							if (!tempGroup.contains(parent)) {
								tempGroup.add(parent);

								if (!backQueue.contains(parent)) {
									backQueue.offer(parent);
								}
							}
						}
					}

					if (foundStart) {
						// There is at least once path from start to node, time
						// to find all of them and add nodes
						group = this
								.findGroupPaths(node, group, this.startNode);
						group.add(node);
					}

				} else {
					// Parent is already in graph, don't do extra work
					group.add(node);
				}

			}
		}

		if (this.isGroup(group)) {
			// TODO do something here later - like change all their colors
			formGroup();
		}
	}

	/**
	 * Forms the selected nodes into a group node.
	 */
	public void formGroup() {
		// Make sure we even have a group.
		if (!this.isGroup())
			return;

		// Not dealing with dialogue groups for now
		if (!(this.startNode instanceof StoryPoint))
			return;

		final StoryNodeGraphModel model = (StoryNodeGraphModel) this.graph.model;

		final StoryNode exitNode = (StoryNode) this.getExitNode();

		final StoryNode startNode = (StoryNode) this.startNode;

		final StoryGroup newGroup = new StoryGroup("some name", startNode, exitNode);

		// Connect the parents of the start node to the new group node.
		for (StoryNode parent : startNode.getParents()) {
			parent.addSuccessor(newGroup);
		}

		// Connect the children of the exit node to the new group node.
		if (exitNode != null) {
			for (StoryNode child : model.getChildren(exitNode)) {
				model.connectNodes(child, newGroup);
			}
		}


		// Add clones of the story nodes to the group
		for (StoryNode node : this.cloneGroupNodes()) {
			newGroup.addStoryChild(node);
		}

		// Remove story nodes in the group from the graph
		for (E node : this.group) {
			this.graph.model.removeNode(node);
		}
	}

	/**
	 * Get the exit node of the current group. By default, the exit node is the
	 * node that contains children not within the group. If no such node exists,
	 * then the exit node is the first node encountered that is at the deepest
	 * level in the graph.
	 * 
	 * @return
	 */
	private E getExitNode() {
		if (!this.isGroup())
			return null;

		E exitNode = null;
		for (E node : this.group) {
			if (!this.group.containsAll(this.graph.getChildren(node))) {
				exitNode = node;
				break;
			}
		}

		if (exitNode == null) {
			final Map<E, Integer> depthMap = this.graph.model.getDepthMap();

			int deepestLevel = -1;
			for (E node : depthMap.keySet()) {
				if (this.group.contains(node)) {
					if (depthMap.get(node) > deepestLevel) {
						exitNode = node;
					}
				}
			}
		}

		return exitNode;
	}

	private Set<StoryNode> cloneGroupNodes() {
		final Set<StoryNode> clonedNodes = new HashSet<StoryNode>();

		final StoryNode groupStartNode = (StoryNode) this.startNode;

		this.cloneNodes(groupStartNode.shallowClone(), groupStartNode,
				clonedNodes);

		return clonedNodes;
	}

	private StoryNode cloneNodes(StoryNode newNode, StoryNode node,
			Set<StoryNode> clonedNodes) {

		clonedNodes.add(newNode);

		outer: for (StoryNode child : node.getSuccessors()) {

			// Don't care about any nodes that aren't in the group.
			if (!this.group.contains(child))
				continue;

			// If the node already exists in this new graph model, we don't need
			// to reclone it.
			for (StoryNode existingNode : clonedNodes) {
				if (existingNode.getUniqueID() == child.getUniqueID()) {
					newNode.addSuccessor(existingNode);
					continue outer;
				}
			}

			final StoryNode clonedNode = child.shallowClone();
			newNode.addSuccessor(clonedNode);
			this.cloneNodes(clonedNode, child, clonedNodes);
		}

		return newNode;
	}

	/**
	 * Clears the current group.
	 */
	public void resetGroup() {
		this.group.clear();
	}

	/**
	 * Checks whether the current group is a valid group.
	 * 
	 * @return
	 */
	public boolean isGroup() {
		return this.isGroup(this.group);
	}

	/**
	 * Checks whether the current group contains <code>node</code>.
	 * 
	 * @return
	 */
	public boolean containsNode(E node) {
		return this.group.contains(node);
	}

	/**
	 * Gets the current group being formed.
	 * 
	 * @return
	 */
	public Set<E> getCurrentGroup() {
		return this.group;
	}

	/**
	 * Gets the start node of the group being formed.
	 * 
	 * @return
	 */
	public E getStartNode() {
		return this.startNode;
	}

	/**
	 * Checks whether the node is a valid group start
	 * 
	 * @param node
	 */
	private boolean isValidGroupStart(E node) {
		final Queue<E> nodes = new LinkedList<E>();

		for (E child : this.graph.model.getChildren(node)) {
			nodes.add(child);
		}

		while (nodes.size() >= 1) {
			final E currentNode = nodes.poll();

			Set<E> currentGroup = new HashSet<E>();

			// Must always include start in the set
			currentGroup.add(node);
			currentGroup = this.findGroupPaths(currentNode, currentGroup, null);

			if (this.isGroup(currentGroup)) {
				return true;
			} else {
				for (E temp : this.graph.model.getChildren(currentNode))
					nodes.add(temp);
			}
		}

		return false;
	}

	/**
	 * Find children who are now all orphaned and remove them from the group.
	 * 
	 * @param node
	 */
	private void removeGroupOrphans(E node) {
		final Queue<E> examine = new LinkedList<E>();
		examine.add(node);

		while (!examine.isEmpty()) {
			final E currentNode = examine.poll();

			for (E child : this.graph.model.getChildren(currentNode)) {
				boolean parentInGroup = false;

				for (E parent : this.graph.model.getParents(child)) {
					if (group.contains(parent)) {
						parentInGroup = true;
					}
				}

				if (!parentInGroup) {
					examine.add(child);
					group.remove(child);
				}
			}
		}
	}

	/**
	 * A nodes should be visited when: 1. All of it's children are visited. 2.
	 * It's been used to find a path to the goal.
	 * 
	 * Retreat once we find a child that is either the end or part of the group.
	 * Retreat when there are no children. When retreating, go back one, see if
	 * there are other children to visit.
	 * 
	 * @param endNode
	 * @param group
	 * @return
	 */
	private Set<E> findGroupPaths(E endNode, Set<E> group, E startNode) {
		final Stack<E> nodes = new Stack<E>();
		final Set<E> visited = new HashSet<E>();

		nodes.push(endNode);

		boolean notDone = true;
		boolean found = false;

		while (!nodes.isEmpty() && notDone) {
			E currentNode = nodes.peek();

			boolean foundChildInGroup = false;
			if (currentNode == startNode || group.contains(currentNode)
					&& currentNode != startNode) {

				// We found an end point, back up one
				visited.add(currentNode);
				nodes.pop();
				group.add(currentNode);

			} else if (this.graph.model.getParents(currentNode).size() > 0) {
				found = false;

				for (E child : this.graph.model.getParents(currentNode)) {
					if (group.contains(child)) {
						foundChildInGroup = true;
					} else if (!visited.contains(child)) {
						// not done yet add child
						found = true;
						nodes.push(child);
					}

					if (found)
						break;
				}

				if (!found) {
					// no children were found
					final E node = nodes.pop();
					visited.add(node);
					// if one child is in group, add parent...
					if (foundChildInGroup) {
						group.add(node);
					}
				}

			} else {
				// no children, so back up one
				visited.add(nodes.pop());
			}
		}

		return group;
	}

	/**
	 * Checks whether <code>nodes</code> is a valid group.
	 * 
	 * @param nodes
	 * @return
	 */
	private boolean isGroup(Set<E> nodes) {
		int numChild = 0;
		int numParents = 0;

		// Can't group only one node.
		if (nodes.size() <= 1)
			return false;

		for (E node : nodes) {
			for (E child : this.graph.model.getChildren(node)) {
				if (!nodes.contains(child)) {
					numChild++;
					break;
				}
			}

			for (E parent : this.graph.model.getParents(node)) {
				if (!nodes.contains(parent)) {
					numParents++;
					break;
				}
			}

			if (numParents > 1 || numChild > 1) {
				return false;
			}
		}

		return true;
	}
}