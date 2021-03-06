package scriptease.gui.SEGraph;

import java.awt.Color;

import javax.swing.BorderFactory;

import scriptease.controller.observer.StoryModelAdapter;
import scriptease.gui.SEGraph.SEGraph.SelectionMode;
import scriptease.gui.SEGraph.models.DescribeItNodeGraphModel;
import scriptease.gui.SEGraph.models.DialogueLineGraphModel;
import scriptease.gui.SEGraph.models.StoryNodeGraphModel;
import scriptease.gui.SEGraph.models.TaskGraphModel;
import scriptease.gui.SEGraph.renderers.DescribeItNodeRenderer;
import scriptease.gui.SEGraph.renderers.DialogueLineNodeRenderer;
import scriptease.gui.SEGraph.renderers.EditableDescribeItNodeRenderer;
import scriptease.gui.SEGraph.renderers.StoryNodeRenderer;
import scriptease.gui.SEGraph.renderers.TaskNodeRenderer;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.behaviours.Task;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;

/**
 * A factory for different graphs. This only creates the GUI for the graphs,
 * since functionality may vary. SEGraphObservers need to be added after
 * construction.
 * 
 * @author kschenk
 * @author jyuen
 * 
 */
public class SEGraphFactory {

	private SEGraphFactory() {
	}

	/**
	 * Builds a graph used to select different nodes in a description.
	 * 
	 * @param start
	 * @return
	 */
	public static SEGraph<DescribeItNode> buildDescribeItGraph(
			DescribeItNode start) {
		final DescribeItNodeGraphModel describeItGraphModel;
		final SEGraph<DescribeItNode> graph;

		describeItGraphModel = new DescribeItNodeGraphModel(start);
		graph = new SEGraph<DescribeItNode>(describeItGraphModel,
				SelectionMode.SELECT_PATH_FROM_START, true, true);

		graph.setNodeRenderer(new DescribeItNodeRenderer(graph));
		graph.setBorder(BorderFactory.createLineBorder(Color.black));
		graph.setBackground(Color.WHITE);

		return graph;
	}

	/**
	 * Builds a graph for descriptions that allows the nodes to be edited.
	 * 
	 * @param start
	 * @return
	 */
	public static SEGraph<DescribeItNode> buildDescribeItEditorGraph(
			DescribeItNode start, final boolean isEditable) {
		final DescribeItNodeGraphModel describeItGraphModel;
		final SEGraph<DescribeItNode> graph;
		
		describeItGraphModel = new DescribeItNodeGraphModel(start);
		
		graph = new SEGraph<DescribeItNode>(describeItGraphModel,
				SelectionMode.SELECT_PATH_FROM_START, !isEditable, true);

		graph.setNodeRenderer(new EditableDescribeItNodeRenderer(graph, isEditable));
		
		graph.setBackground(ScriptEaseUI.PRIMARY_UI);

		return graph;
	}

	/**
	 * Builds a graph for story nodes with default white background colour, and
	 * read only mode to false.
	 * 
	 * @param start
	 * @return
	 */
	public static SEGraph<StoryNode> buildStoryGraph(StoryNode start) {
		return SEGraphFactory.buildStoryGraph(start, Color.WHITE, false);
	}

	/**
	 * Builds a graph for story nodes.
	 * 
	 * @param start
	 *            The graph start story node.
	 * @param bgColour
	 *            The background colour of the graph.
	 * @param readOnly
	 *            Whether the graph is read only.
	 * @return
	 */
	public static SEGraph<StoryNode> buildStoryGraph(StoryNode start,
			Color bgColour, boolean readOnly) {
		final SEGraph<StoryNode> graph;
		final StoryNodeGraphModel storyGraphModel;

		storyGraphModel = new StoryNodeGraphModel(start);
		graph = new SEGraph<StoryNode>(storyGraphModel,
				SelectionMode.SELECT_NODE, readOnly, false);

		graph.setNodeRenderer(new StoryNodeRenderer(graph));
		graph.setBackground(bgColour);

		return graph;
	}

	/**
	 * Builds a graph for tasks.
	 * 
	 * @param start
	 *            The graph start task node.
	 * @return
	 */
	public static SEGraph<Task> buildTaskGraph(Task start, boolean readOnly) {
		final SEGraph<Task> graph;
		final TaskGraphModel taskGraphModel;

		taskGraphModel = new TaskGraphModel(start);
		graph = new SEGraph<Task>(taskGraphModel, SelectionMode.SELECT_NODE,
				readOnly, true);

		graph.setNodeRenderer(new TaskNodeRenderer(graph));

		return graph;
	}

	
	/**
	 * Builds a DialogueLine graph based on the passed in DialogueLine.
	 * 
	 * @param dialogueLine
	 * @return
	 */
	public static SEGraph<DialogueLine> buildDialogueLineGraph(
			StoryModel story, DialogueLine dialogueLine) {
		final SEGraph<DialogueLine> graph;
		final DialogueLineGraphModel model;

		model = new DialogueLineGraphModel(story, dialogueLine);
		graph = new SEGraph<DialogueLine>(model);

		graph.setNodeRenderer(new DialogueLineNodeRenderer(graph));
		graph.setBackground(Color.WHITE);

		story.addStoryModelObserver(new StoryModelAdapter() {
			private void redrawGraph() {
				graph.recalculateDepthMap();
				graph.repaint();
				graph.revalidate();
			}

			@Override
			public void dialogueChildAdded(DialogueLine added,
					DialogueLine parent) {
				this.redrawGraph();
			}

			@Override
			public void dialogueChildRemoved(DialogueLine removed,
					DialogueLine parent) {
				this.redrawGraph();
			}
		});

		return graph;
	}
}
