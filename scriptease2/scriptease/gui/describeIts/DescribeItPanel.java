package scriptease.gui.describeIts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEGraph.DescribeItNodeGraphModel;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraph.SelectionMode;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.SEGraph.renderers.DescribeItNodeRenderer;
import scriptease.gui.cell.ScriptWidgetFactory;
import scriptease.gui.control.ExpansionButton;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.APIDictionary;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.DescribeItManager;

/**
 * This view is used to allow the user to select various pathways from
 * DescribeIts to determine what effect they should have.
 * 
 * @author mfchurch
 * @author kschenk
 */
@SuppressWarnings("serial")
public class DescribeItPanel extends JPanel {
	private final SEGraph<DescribeItNode> expandedPanel;
	private final JPanel collapsedPanel;
	private final ExpansionButton expansionButton;

	private boolean collapsed;

	public DescribeItPanel(final KnowIt knowIt, boolean collapsed) {
		final APIDictionary apiDictionary;
		final DescribeItManager describeItManager;
		final DescribeIt describeIt;
		final DescribeItNode headNode;

		final DescribeItNodeGraphModel describeItGraphModel;

		final ScriptIt resolvedScriptIt;

		this.collapsed = collapsed;

		apiDictionary = TranslatorManager.getInstance().getActiveTranslator()
				.getApiDictionary();
		describeItManager = apiDictionary.getDescribeItManager();
		describeIt = describeItManager
				.findDescribeItForTypes(knowIt.getTypes());

		if (describeIt == null) {
			throw new NullPointerException("No DescribeIt found for " + knowIt
					+ " when attempting to create DescribeItPanel!");
		}

		headNode = describeIt.getStartNode();
		describeItGraphModel = new DescribeItNodeGraphModel(headNode);
		resolvedScriptIt = describeIt.getScriptItForPath(describeIt
				.getSelectedPath());

		this.expandedPanel = new SEGraph<DescribeItNode>(describeItGraphModel,
				SelectionMode.SELECT_PATH);
		this.collapsedPanel = new JPanel();
		this.expansionButton = ScriptWidgetFactory
				.buildExpansionButton(this.collapsed);

		if (resolvedScriptIt != null) {
			StoryComponentPanelFactory.getInstance().parseDisplayText(
					this.collapsedPanel, resolvedScriptIt);
		}

		this.expandedPanel.setNodeRenderer(new DescribeItNodeRenderer(
				this.expandedPanel));

		this.expandedPanel
				.addSEGraphObserver(new SEGraphAdapter<DescribeItNode>() {

					@Override
					public void nodesSelected(Collection<DescribeItNode> nodes) {
						final Collection<DescribeItNode> selectedNodes;

						selectedNodes = new ArrayList<DescribeItNode>();

						selectedNodes.addAll(nodes);

						describeIt.setSelectedPath(selectedNodes);
					}
				});

		this.expansionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// toggle
				DescribeItPanel.this.collapsed = !DescribeItPanel.this.collapsed;
				boolean shouldCollapse = DescribeItPanel.this.collapsed;
				if (shouldCollapse) {
					// Start undo when we open graph
					if (!UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().startUndoableAction(
								"Bind DescribeIt");

					final ScriptIt resolvedScriptIt;

					resolvedScriptIt = describeIt.getScriptItForPath(describeIt
							.getSelectedPath());

					if (resolvedScriptIt != null) {
						StoryComponentPanelFactory.getInstance()
								.parseDisplayText(
										DescribeItPanel.this.collapsedPanel,
										resolvedScriptIt);
						knowIt.setBinding(resolvedScriptIt);
					}
				} else {
					// End undo when we close it.
					if (UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().endUndoableAction();
				}
				collapsedPanel.setVisible(shouldCollapse);
				expandedPanel.setVisible(!shouldCollapse);
				expansionButton.setCollapsed(shouldCollapse);
				DescribeItPanel.this.revalidate();
			}
		});

		this.setOpaque(false);
		this.setLayout(new DescribeItPanelLayoutManager());

		if (describeIt.getPaths().size() <= 1)
			this.expansionButton.setVisible(false);

		this.add(expansionButton);
		this.add(collapsedPanel);
		this.add(expandedPanel);
	}

	/**
	 * DescribeItPanelLayoutManager handles laying out the describeItPanel in
	 * either it's text form, or graph form
	 * 
	 * @author mfchurch
	 * 
	 */
	private class DescribeItPanelLayoutManager implements LayoutManager {
		private static final int BUTTON_X_INDENT = 5;

		@Override
		public void layoutContainer(Container parent) {
			final Insets insets = parent.getInsets();
			if (DescribeItPanel.this.collapsed) {
				layoutCollapsed(insets.left + insets.right, insets.top
						+ insets.bottom);
			} else {
				layoutExpanded(insets.left + insets.right, insets.top
						+ insets.bottom);
			}
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			final Insets insets = parent.getInsets();
			if (DescribeItPanel.this.collapsed)
				return minimumCollapsedLayoutSize(insets.left + insets.right,
						insets.top + insets.bottom);
			else
				return minimumExpandedLayoutSize(insets.left + insets.right,
						insets.top + insets.bottom);
		}

		/**
		 * Calculates the minimum layout size when the panel is collapsed (text)
		 * 
		 * @param parent
		 * @return
		 */
		private Dimension minimumCollapsedLayoutSize(int xSize, int ySize) {
			int buttonHeight = 0;
			int buttonWidth = 0;
			// Expansion button
			if (expansionButton.isVisible()) {
				buttonHeight += (int) expansionButton.getPreferredSize()
						.getHeight();
				buttonWidth += (int) expansionButton.getPreferredSize()
						.getWidth();
			}

			// Add the button indent
			xSize += buttonWidth + BUTTON_X_INDENT;
			ySize = Math.max(ySize, buttonHeight);

			xSize += DescribeItPanel.this.collapsedPanel.getPreferredSize()
					.getWidth();
			ySize = Math.max(ySize, (int) DescribeItPanel.this.collapsedPanel
					.getPreferredSize().getHeight());

			return new Dimension(xSize, ySize);
		}

		/**
		 * Layout the graph panel in it's collapsed form (text)
		 * 
		 * @param parent
		 */
		private void layoutCollapsed(int xLocation, int yLocation) {
			int buttonHeight = 0;
			int buttonWidth = 0;
			if (DescribeItPanel.this.expansionButton.isVisible()) {
				// Expansion button
				buttonHeight += (int) DescribeItPanel.this.expansionButton
						.getPreferredSize().getHeight();
				buttonWidth += (int) DescribeItPanel.this.expansionButton
						.getPreferredSize().getWidth();
				DescribeItPanel.this.expansionButton.setBounds(xLocation,
						((int) DescribeItPanel.this.getPreferredSize()
								.getHeight() - buttonHeight) / 2, buttonWidth,
						buttonHeight);
			}
			// Add the button indent
			xLocation += buttonWidth + BUTTON_X_INDENT;

			DescribeItPanel.this.collapsedPanel.setBounds(xLocation, yLocation,
					(int) DescribeItPanel.this.collapsedPanel
							.getPreferredSize().getWidth(),
					(int) DescribeItPanel.this.collapsedPanel
							.getPreferredSize().getHeight());
		}

		/**
		 * Calculates the minimum layout size when the panel is expanded (graph)
		 * 
		 * @param parent
		 * @return
		 */
		protected Dimension minimumExpandedLayoutSize(int xSize, int ySize) {
			int buttonHeight = 0;
			int buttonWidth = 0;
			if (DescribeItPanel.this.expansionButton.isVisible()) {
				// Expansion button
				buttonHeight += (int) DescribeItPanel.this.expansionButton
						.getPreferredSize().getHeight();
				buttonWidth += (int) DescribeItPanel.this.expansionButton
						.getPreferredSize().getWidth();
			}

			// Add the button indent
			xSize += buttonWidth;
			ySize = Math.max(buttonHeight, ySize);

			// calculate the minimum size with the graphPanel
			Dimension minimumSize = expandedPanel.getMinimumSize();
			minimumSize.setSize(minimumSize.getWidth() + xSize,
					Math.max(minimumSize.getHeight(), ySize));

			return minimumSize;
		}

		/**
		 * Layout the graph panel in it's expanded form (graph)
		 * 
		 * @param xLocation
		 * @param yLocation
		 */
		protected void layoutExpanded(int xLocation, int yLocation) {
			int buttonHeight = 0;
			int buttonWidth = 0;
			if (DescribeItPanel.this.expansionButton.isVisible()) {
				// Expansion button
				buttonHeight = (int) DescribeItPanel.this.expansionButton
						.getPreferredSize().getHeight();
				buttonWidth = (int) DescribeItPanel.this.expansionButton
						.getPreferredSize().getWidth();
				DescribeItPanel.this.expansionButton.setBounds(xLocation,
						(((int) DescribeItPanel.this.getPreferredSize()
								.getHeight() - buttonHeight) / 2), buttonWidth,
						buttonHeight);
			}
			// Add the button indent
			xLocation += buttonWidth;

			// graphPanel does the rest
			expandedPanel.setBounds(xLocation, yLocation, (int) expandedPanel
					.getPreferredSize().getWidth(), (int) expandedPanel
					.getPreferredSize().getHeight());
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return minimumLayoutSize(parent);
		}
	}

	@Override
	public String toString() {
		return "DescribeItPanel [" + this.expandedPanel.getStartNode() + "]";
	}
}
