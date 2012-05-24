package scriptease.gui;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;

import scriptease.controller.AbstractNoOpGraphNodeVisitor;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeEvent.GraphNodeEventType;
import scriptease.controller.observer.GraphNodeObserver;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.action.story.graphs.ConnectGraphPointAction;
import scriptease.gui.action.story.graphs.DeleteGraphNodeAction;
import scriptease.gui.action.story.graphs.DisconnectGraphPointAction;
import scriptease.gui.action.story.graphs.InsertGraphNodeAction;
import scriptease.gui.action.story.graphs.SelectGraphNodeAction;
import scriptease.gui.graph.editor.GraphEditor;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.quests.QuestEditor;
import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.quests.QuestPointNode;
import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;

/**
 * ToolBarFactory is responsible for creating JToolBars, most importantly the
 * toolbars for editing graphs. A specialized Quest Editor Toolbar can also be
 * created.
 * 
 * The class also determines toolbar functionality. These toolbars are used to
 * act upon the GraphEditor, QuestEditor, and in the future, DescribeItEditor.
 * 
 * @author kschenk
 * 
 */
public class ToolBarFactory {

	private final JLabel nameLabel = new JLabel(Il8nResources.getString("Name")
			+ ":");
	private final JLabel fanInLabel = new JLabel("Fan In:");

	/**
	 * Do not delete this. Ever. Otherwise Java will Garbage Collect all
	 * references to the observer, and break the quest editor in ScriptEase2.
	 * 
	 * This makes sure the observer always exists.
	 */
	private static GraphNodeObserver graphBarObserver;

	private GraphNodeObserver questBarObserver;

	/**
	 * Builds a toolbar to edit graphs with. Includes buttons for selecting
	 * nodes, adding and deleting nodes, and adding and deleting paths.
	 * 
	 * @return
	 */
	public static JToolBar buildGraphEditorToolBar(final GraphEditor editor) {
		final JToolBar graphEditorToolBar = new JToolBar();

		final ButtonGroup graphEditorButtonGroup = new ButtonGroup();

		final ArrayList<JToggleButton> buttonList = new ArrayList<JToggleButton>();

		final JToggleButton selectNodeButton = new JToggleButton(
				SelectGraphNodeAction.getInstance());

		final JToggleButton insertNodeButton = new JToggleButton(
				InsertGraphNodeAction.getInstance());

		final JToggleButton deleteNodeButton = new JToggleButton(
				DeleteGraphNodeAction.getInstance());

		final JToggleButton connectNodeButton = new JToggleButton(
				ConnectGraphPointAction.getInstance());

		final JToggleButton disconnectNodeButton = new JToggleButton(
				DisconnectGraphPointAction.getInstance());

		graphEditorToolBar.setLayout(new BoxLayout(graphEditorToolBar,
				BoxLayout.LINE_AXIS));
		graphEditorToolBar.setRollover(true);
		graphEditorToolBar.setFloatable(false);

		buttonList.add(selectNodeButton);
		buttonList.add(insertNodeButton);
		buttonList.add(deleteNodeButton);
		buttonList.add(connectNodeButton);
		buttonList.add(disconnectNodeButton);

		for (JToggleButton toolBarButton : buttonList) {
			toolBarButton.setHideActionText(true);
			toolBarButton.setFocusable(false);
			graphEditorButtonGroup.add(toolBarButton);
			graphEditorToolBar.add(toolBarButton);
		}

		ChangeListener graphEditorListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				switch (ToolBarButtonAction.getMode()) {

				case SELECT_GRAPH_NODE:
					graphEditorButtonGroup.setSelected(
							selectNodeButton.getModel(), true);
					break;
				case DELETE_GRAPH_NODE:
					graphEditorButtonGroup.setSelected(
							deleteNodeButton.getModel(), true);
					break;
				case INSERT_GRAPH_NODE:
					graphEditorButtonGroup.setSelected(
							insertNodeButton.getModel(), true);
					break;
				case CONNECT_GRAPH_NODE:
					graphEditorButtonGroup.setSelected(
							connectNodeButton.getModel(), true);
					break;
				case DISCONNECT_GRAPH_NODE:
					graphEditorButtonGroup.setSelected(
							disconnectNodeButton.getModel(), true);
					break;
				}
			}
		};

		SEFrame.getInstance().getStoryTabPane()
				.addChangeListener(graphEditorListener);

		graphBarObserver = new GraphToolBarObserver();

		GraphNode.observeDepthMap(graphBarObserver, editor.getHeadNode());

		return graphEditorToolBar;
	}

	/**
	 * Creates a JToolBar for the quest editor. It adds all of the graph editor
	 * buttons from the GraphEditorToolbar, and then adds Quest specific options
	 * for the user after a separator.
	 * 
	 * @return
	 */
	public JToolBar buildQuestEditorToolBar(final QuestEditor editor) {

		final JToolBar questEditorToolBar = buildGraphEditorToolBar(editor);

		final int TOOL_BAR_HEIGHT = 32;
		final int FAN_IN_SPINNER_LENGTH = 50;
		final int NAME_FIELD_LENGTH = 150;

		final JTextField nameField = nameField(new Dimension(NAME_FIELD_LENGTH,
				TOOL_BAR_HEIGHT));

		final JCheckBox commitBox = committingBox();

		final JSpinner fanInSpinner = buildFanInSpinner(new Dimension(
				FAN_IN_SPINNER_LENGTH, TOOL_BAR_HEIGHT));

		editor.getHeadNode().process(new AbstractNoOpGraphNodeVisitor() {
			public void processQuestPointNode(QuestPointNode questPointNode) {
				updateQuestToolBar(nameField, commitBox, fanInSpinner,
						questPointNode);
			}
		});

		Dimension minSize = new Dimension(15, TOOL_BAR_HEIGHT);
		Dimension prefSize = new Dimension(15, TOOL_BAR_HEIGHT);
		Dimension maxSize = new Dimension(15, TOOL_BAR_HEIGHT);

		questEditorToolBar.add(new Box.Filler(minSize, prefSize, maxSize));

		questEditorToolBar.addSeparator();

		questEditorToolBar.add(new Box.Filler(minSize, prefSize, maxSize));

		questEditorToolBar.add(nameLabel);

		nameLabel.setLabelFor(nameField);

		questEditorToolBar.add(nameField);

		questEditorToolBar.add(commitBox);

		questEditorToolBar.add(fanInLabel);

		fanInLabel.setLabelFor(fanInSpinner);

		questEditorToolBar.add(fanInSpinner);

		questBarObserver = new QuestToolBarObserver(nameField, commitBox,
				fanInSpinner, editor);

		GraphNode.observeDepthMap(questBarObserver, editor.getHeadNode());

		return questEditorToolBar;
	}

	/**
	 * Updates the entire quest tool bar, including the namefiled, the
	 * committing button, the fan in spinner, and all of the labels.
	 * 
	 * 
	 * @param nameField
	 * @param commitBox
	 * @param fanInSpinner
	 * @param questNode
	 */
	private void updateQuestToolBar(JTextField nameField, JCheckBox commitBox,
			JSpinner fanInSpinner, QuestPointNode questNode) {

		updateFanInSpinner(fanInSpinner, questNode);
		updateCommittingBox(commitBox, questNode);
		updateNameField(nameField, questNode);

	}

	/**
	 * Returns a name field JTextField with the proper appearance.
	 * 
	 * @param maxSize
	 * @return
	 */
	private JTextField nameField(final Dimension maxSize) {
		JTextField nameField = new JTextField(10);
		nameField.setMaximumSize(maxSize);

		return nameField;
	}

	/**
	 * Creates an DocumentListener for the TextField.
	 * 
	 * @return
	 */
	private DocumentListener nameFieldListener(JTextField nameField,
			QuestPointNode questNode) {
		final JTextField nField = nameField;
		final QuestPointNode qNode = questNode;

		DocumentListener nameFieldListener = new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				String text = nField.getText();
				qNode.getQuestPoint().setDisplayText(text);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				insertUpdate(e);
			}
		};

		return nameFieldListener;
	}

	/**
	 * Updates the name field for the quest node passed in.
	 * 
	 * @param nameField
	 * @param questNode
	 */
	private void updateNameField(JTextField nameField, QuestPointNode questNode) {

		if (questNode != null) {

			String displayText = questNode.getQuestPoint().getDisplayText();

			nameField.setDocument(new PlainDocument());
			nameField.getDocument().addDocumentListener(
					nameFieldListener(nameField, questNode));

			nameField.setText(displayText);

			if (questNode.isDeletable()) {
				nameLabel.setEnabled(true);
				nameField.setEnabled(true);

			} else {
				nameLabel.setEnabled(false);
				nameField.setEnabled(false);
			}

		} else {
			nameField.setText("");
			nameLabel.setEnabled(false);
			nameField.setEnabled(false);
		}
	}

	/**
	 * Returns a committing button with the proper appearance.
	 * 
	 * @return
	 */
	private JCheckBox committingBox() {
		final JCheckBox committingBox = new JCheckBox();
		committingBox.setText(Il8nResources.getString("Committing") + ":");
		committingBox.setHorizontalTextPosition(SwingConstants.LEFT);
		return committingBox;
	}

	/**
	 * Creates an ItemListener for the CommitButton. Listens if the CommitButton
	 * is selected or deselected, and sets the model appropriately.
	 * 
	 * @param questNode
	 * @return
	 */
	private ItemListener commitBoxListener(QuestPointNode questNode) {
		final QuestPointNode questN = questNode;

		ItemListener commitButtonListener = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					questN.getQuestPoint().setCommitting(true);
				} else {
					questN.getQuestPoint().setCommitting(false);
				}
			}
		};

		return commitButtonListener;
	}

	/**
	 * Updates the committing button for the quest node passed in.
	 * 
	 * @param cBox
	 *            The commmitting button to update.
	 * @param questNode
	 *            The QuestPointNode to update the committing button to.
	 */
	private void updateCommittingBox(JCheckBox cBox, QuestPointNode questNode) {
		if (questNode != null) {
			Boolean committing = questNode.getQuestPoint().getCommitting();

			if (cBox.getItemListeners().length > 0)
				cBox.removeItemListener(cBox.getItemListeners()[0]);

			cBox.addItemListener(commitBoxListener(questNode));

			// Checks if it's an end or start node

			cBox.setSelected(committing);

			if (questNode.isDeletable()) {
				cBox.setEnabled(true);
			} else {
				cBox.setEnabled(false);
			}
		} else {
			cBox.setEnabled(false);
		}
	}

	/**
	 * Returns a fanInSpinner, which has an attached listener that updates the
	 * model if its value is changed. This is used for quest fan in, i.e. how
	 * many preceding tests need to be finished before starting the selected
	 * one.
	 * 
	 * @return
	 */
	private JSpinner buildFanInSpinner(final Dimension maxSize) {
		final JSpinner fanInSpinner = new JSpinner();
		fanInSpinner.setMaximumSize(maxSize);

		fanInSpinner.setEnabled(false);
		fanInLabel.setEnabled(false);

		return fanInSpinner;
	}

	/**
	 * Creates a ChangeListener for a JSpinner. Returns listener. When the
	 * Spinner value changes, the listener updates the model and updates the fan
	 * in spinner itself to show the change.
	 * 
	 * @param fanInSpinner
	 * @param questPoint
	 * @return
	 */
	private ChangeListener fanInSpinnerListener(JSpinner fanInSpinner,
			QuestPointNode questNode) {
		final JSpinner fanInSpin = fanInSpinner;
		final QuestPointNode questN = questNode;

		ChangeListener fanInSpinnerListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				SpinnerModel spinnerModel = fanInSpin.getModel();
				Integer spinnerValue = (Integer) spinnerModel.getValue();

				questN.getQuestPoint().setFanIn(spinnerValue);

				// updateFanInSpinner(fanInSpin, questN);
			}
		};

		return fanInSpinnerListener;
	}

	/**
	 * Creates a SpinnerModel for the FanIn function based on the current quest
	 * point, then sets the FanIn Spinner Model to it.
	 * 
	 * If there is no Quest Point selected, the SpinnerModel is a spinner set to
	 * 1.
	 * 
	 * @param fanInSpinner
	 *            FanInSpinner to be passed
	 * @param questNode
	 *            QuestPointNode that the spinner is operating on. Pass in null
	 *            if there is none.
	 * 
	 * @return The SpinnerModel
	 */
	private void updateFanInSpinner(JSpinner fanInSpinner,
			QuestPointNode questNode) {

		if (questNode != null) {
			int maxFanIn = questNode.getParents().size();

			// If maxFanIn >1, maxFanIn. Otherwise, 1.
			maxFanIn = maxFanIn > 1 ? maxFanIn : 1;

			if (questNode.getQuestPoint().getFanIn() > maxFanIn) {
				questNode.getQuestPoint().setFanIn(1);
			}

			final SpinnerModel fanInSpinnerModel = new SpinnerNumberModel(
					questNode.getQuestPoint().getFanIn(), new Integer(1),
					new Integer(maxFanIn), new Integer(1));

			fanInSpinner.setModel(fanInSpinnerModel);

			if (fanInSpinner.getChangeListeners().length > 1) {
				fanInSpinner.removeChangeListener(fanInSpinner
						.getChangeListeners()[1]);
			}

			fanInSpinner.addChangeListener(fanInSpinnerListener(fanInSpinner,
					questNode));

			if (!questNode.isStartNode()) {
				fanInLabel.setEnabled(true);
				fanInSpinner.setEnabled(true);
			} else {
				fanInLabel.setEnabled(false);
				fanInSpinner.setEnabled(false);
			}

		} else {
			final SpinnerModel fanInSpinnerModel = new SpinnerNumberModel(
					new Integer(1), new Integer(1), new Integer(1),
					new Integer(1));

			fanInSpinner.setModel(fanInSpinnerModel);
			fanInLabel.setEnabled(false);
			fanInSpinner.setEnabled(false);
		}
	}

	/**
	 * Private observer for the Graph ToolBar
	 * 
	 * @author kschenk
	 * 
	 */
	private static class GraphToolBarObserver implements GraphNodeObserver {
		/**
		 * Adds the node to the graph bar observer if a new one is added.
		 */
		@Override
		public void nodeChanged(GraphNodeEvent event) {
			final GraphNode sourceNode = event.getSource();
			final GraphNodeEventType type = event.getEventType();

			GraphNode oldSelectedNode = GraphEditor.getOldSelectedNode();

			if (type == GraphNodeEventType.SELECTED) {
				switch (ToolBarButtonAction.getMode()) {
				case DELETE_GRAPH_NODE:
					if (sourceNode.isDeletable()) {
						List<GraphNode> parents = sourceNode.getParents();
						List<GraphNode> children = sourceNode.getChildren();

						sourceNode.removeParents();

						sourceNode.removeChildren();

						// Re-connect each parent with each child.
						for (GraphNode parent : parents) {
							for (GraphNode child : children) {
								parent.addChild(child);
							}
						}
					}
					break;
					
				case CONNECT_GRAPH_NODE:
					if (oldSelectedNode != null) {
						// Determine which node is shallower in the graph, and
						// which
						// is deeper.
						GraphNode shallowerNode = sourceNode
								.isDescendant(oldSelectedNode) ? oldSelectedNode
								: sourceNode;
						GraphNode deeperNode = sourceNode
								.isDescendant(oldSelectedNode) ? sourceNode
								: oldSelectedNode;

						// connect the nodes if not connected
						shallowerNode.addChild(deeperNode);

						// Reset the tool.
						GraphEditor.setOldSelectedNode(null);
					}
					// update the last selected node
					else
						GraphEditor.setOldSelectedNode(sourceNode);
					break;
					
				case DISCONNECT_GRAPH_NODE:
					if (oldSelectedNode != null) {
						// Determine which node is shallower in the graph, and
						// which
						// is deeper.
						GraphNode shallowerNode = sourceNode
								.isDescendant(oldSelectedNode) ? oldSelectedNode
								: sourceNode;
						GraphNode deeperNode = sourceNode
								.isDescendant(oldSelectedNode) ? sourceNode
								: oldSelectedNode;

						// Check that both nodes will still have at least one
						// parent and one child after the disconnect.
						if (shallowerNode.getChildren().size() > 1
								&& deeperNode.getParents().size() > 1) {
							shallowerNode.removeChild(deeperNode, false);
						}

						// Reset the tool.
						GraphEditor.setOldSelectedNode(null);
					}
					// update the last selected node
					else
						GraphEditor.setOldSelectedNode(sourceNode);
					break;
				}

			} else if (type == GraphNodeEventType.CONNECTION_ADDED) {
				GraphNode.observeDepthMap(graphBarObserver, sourceNode);
			}
		}
	}

	/**
	 * Private observer for the QuestToolBar. Also provides quest specific
	 * actions for the graph editor toolbar buttons.
	 * 
	 * @author kschenk
	 */
	private class QuestToolBarObserver implements GraphNodeObserver {
		private JTextField nameField;
		private JCheckBox commitButton;
		private JSpinner fanInSpinner;

		private GraphNode previousNode;
		private GraphNode headNode;

		// private GraphEditor editor;

		/**
		 * Creates the observer for the quest toolbar. It requires the name
		 * textfield, a commit button, a fan in spinner, and a quest editor.
		 * 
		 * @param nameField
		 * @param commitbox
		 * @param fanInSpinner
		 * @param editor
		 */
		public QuestToolBarObserver(JTextField nameField, JCheckBox commitbox,
				JSpinner fanInSpinner, QuestEditor editor) {
			this.nameField = nameField;
			this.commitButton = commitbox;
			this.fanInSpinner = fanInSpinner;
			this.previousNode = editor.getHeadNode();
			this.headNode = editor.getHeadNode();
		}

		/**
		 * Swaps the selected state of the newly selected node.
		 * 
		 * @param previousNode
		 * @param currentNode
		 */
		protected void swapSelected(GraphNode previousNode,
				GraphNode currentNode) {
			if (previousNode != null)
				previousNode.setSelected(false);
			currentNode.setSelected(true);
		}

		/**
		 * Specialty method for deleting the quest node, rather than just a
		 * graph node.
		 * 
		 * @param sourceNode
		 * @param questPointNode
		 */
		private void deleteQuestNode(final GraphNode sourceNode,
				QuestPointNode questPointNode) {
			if (sourceNode.isDeletable()) {
				List<GraphNode> children = questPointNode.getChildren();
				sourceNode.process(new AbstractNoOpGraphNodeVisitor() {
					@Override
					public void processQuestNode(QuestNode questNode) {
						// Remove the Quest

						List<GraphNode> parents = questNode.getParents();
						List<GraphNode> children = questNode.getChildren();

						if (!parents.isEmpty() && !children.isEmpty()) {

							questNode.removeParents();

							// Add startPoint to parents of QuestNode
							GraphNode startPoint = questNode.getStartPoint();
							List<GraphNode> parentsx = sourceNode.getParents();
							for (GraphNode parent : parentsx) {
								parent.addChild(startPoint);
							}

							// Add children of QuestNode to endPoint
							GraphNode endPoint = questNode.getEndPoint();
							endPoint.addChildren(questNode.getChildren());
						}
					}

					@Override
					public void processQuestPointNode(
							QuestPointNode questPointNode) {
						List<GraphNode> parents = questPointNode.getParents();
						List<GraphNode> children = questPointNode.getChildren();

						// Only delete the node if there are parents and
						// children to repair the graph with.
						if (!parents.isEmpty() && !children.isEmpty()) {

							// Remove the node from its parents.
							questPointNode.removeParents();

							// Remove the node from its children.
							questPointNode.removeChildren();

							// Re-connect each parent with each child.
							for (GraphNode parent : parents) {
								for (GraphNode child : children) {
									parent.addChild(child);
								}
							}
						}
					}
				});
				// Subtracts 1 from fan in of all children.
				for (GraphNode child : children) {
					child.process(new AbstractNoOpGraphNodeVisitor() {
						public void processQuestPointNode(
								QuestPointNode questPointNode) {

							QuestPoint questPoint = questPointNode
									.getQuestPoint();
							int fanIn = questPoint.getFanIn();

							if (fanIn > 1)
								questPoint.setFanIn(fanIn - 1);
						}
					});
				}
			}

			previousNode.process(new AbstractNoOpGraphNodeVisitor() {
				public void processQuestPointNode(QuestPointNode questPointNode) {
					updateQuestToolBar(nameField, commitButton, fanInSpinner,
							questPointNode);
				}
			});
		}

		/**
		 * Method to abstract commonalities from the Insert QuestPoint between
		 * and alternate tools.
		 * 
		 * @param node
		 * 
		 * @author graves
		 */
		private void insertQuestPoint(GraphNode node) {
			// if this is the second click,

			GraphNode oldSelectedNode = GraphEditor.getOldSelectedNode();

			if (oldSelectedNode != null) {

				// create a new node to insert:
				QuestPoint newQuestPoint = new QuestPoint("", 1, false);
				QuestPointNode newQuestPointNode = new QuestPointNode(
						newQuestPoint);

				// Cases for clicking the same node.
				if (oldSelectedNode == node) {

					if (oldSelectedNode == headNode) {
						// Get the children of the start node.
						List<GraphNode> startNodeChildren = oldSelectedNode
								.getChildren();

						// Remove them all.
						oldSelectedNode.removeChildren();

						// Add the new node to the start node as a child.
						oldSelectedNode.addChild(newQuestPointNode);

						// Add the old children to the new node.
						newQuestPointNode.addChildren(startNodeChildren);
					}

					else if (oldSelectedNode.isTerminalNode()) {
						// Get the parents of the end node.
						List<GraphNode> endNodeParents = oldSelectedNode
								.getParents();

						// Remove them all.
						oldSelectedNode.removeParents();

						// Add the end node to the new node as a child.
						newQuestPointNode.addChild(oldSelectedNode);

						// Add the old parents to the new node.
						for (GraphNode parent : endNodeParents) {
							parent.addChild(newQuestPointNode);
						}
					}
					// double clicking any other node does nothing.

					// Cases for clicking a new node
				} else {
					// determine which node is closer to the startNode in the
					// graph
					// (the parent) and which is further from the startNode (the
					// child).
					GraphNode closerToStartNode = node
							.isDescendant(oldSelectedNode) ? oldSelectedNode
							: node;
					GraphNode furtherFromStartNode = node
							.isDescendant(oldSelectedNode) ? node
							: oldSelectedNode;

					// Remove the old connection between the parent and child:
					closerToStartNode.removeChild(furtherFromStartNode, false);

					// Add the new node to the shallower node as a child
					// (addChild
					// automatically adds shallower as parent):
					closerToStartNode.addChild(newQuestPointNode);

					// Add the deeper node to the new node as a child.
					newQuestPointNode.addChild(furtherFromStartNode);
				}
				// Reset the tool:
				GraphEditor.setOldSelectedNode(null);

			} else {
				// otherwise this is the first click, so store the node for
				// later:
				GraphEditor.setOldSelectedNode(node);
			}

		}

		@Override
		public void nodeChanged(GraphNodeEvent event) {

			final GraphNode sourceNode = event.getSource();
			final GraphNodeEventType type = event.getEventType();

			if (type == GraphNodeEventType.SELECTED) {
				if (ToolBarButtonAction.getMode() != ToolBarButtonMode.DELETE_GRAPH_NODE) {
					swapSelected(previousNode, sourceNode);
					previousNode = sourceNode;
				}

				sourceNode.process(new AbstractNoOpGraphNodeVisitor() {
					@Override
					public void processQuestPointNode(
							QuestPointNode questPointNode) {
						switch (ToolBarButtonAction.getMode()) {

						case DISCONNECT_GRAPH_NODE:
							QuestPoint questPoint = questPointNode
									.getQuestPoint();
							int fanIn = questPoint.getFanIn();

							if (fanIn > 1)
								questPoint.setFanIn(fanIn - 1);

						case SELECT_GRAPH_NODE:
							final StoryModel model = StoryModelPool
									.getInstance().getActiveModel();

							if (model != null) {
								sourceNode
										.process(new AbstractNoOpGraphNodeVisitor() {
											@Override
											public void processQuestPointNode(
													QuestPointNode questPointNode) {

												QuestPoint questPoint = questPointNode
														.getQuestPoint();

												SEFrame.getInstance()
														.activatePanelForQuestPoint(
																model,
																questPoint);
											}
										});
							}
							updateQuestToolBar(nameField, commitButton,
									fanInSpinner, questPointNode);
							break;
						case INSERT_GRAPH_NODE:
							insertQuestPoint(sourceNode);
							updateQuestToolBar(nameField, commitButton,
									fanInSpinner, questPointNode);
							break;
						case CONNECT_GRAPH_NODE:
							updateQuestToolBar(nameField, commitButton,
									fanInSpinner, questPointNode);
							previousNode = sourceNode;
							break;

						case DELETE_GRAPH_NODE:
							deleteQuestNode(sourceNode, questPointNode);
							break;
						}
					}
				});

			} else if (type == GraphNodeEventType.CONNECTION_ADDED) {
				GraphNode.observeDepthMap(questBarObserver, sourceNode);
			}
		}
	}
}
