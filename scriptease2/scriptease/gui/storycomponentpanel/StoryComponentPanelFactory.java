package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.controller.AbstractNoOpBindingVisitor;
import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.SETree.transfer.StoryComponentPanelTransferHandler;
import scriptease.gui.control.ExpansionButton;
import scriptease.gui.describeIts.DescribeItPanel;
import scriptease.gui.quests.QuestPoint;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingDescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingRunTime;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * Builds a pane filled with ScriptEase Pattern Constructor GUI widgets for
 * displaying and editing Story Components. <br>
 * <br>
 * It is a StoryVisitor which is what allows it to build GUI for any Story
 * Component.<br>
 * <br>
 * Use {@link StoryComponentPanelFactory#buildPanel(StoryComponent, boolean)} to
 * have a GUIComponent Builder build a JPanel to represent the StoryComponent
 * supplied.
 * 
 * @author graves
 * @author remiller
 * @author mfchurch
 * @author kschenk
 */
public class StoryComponentPanelFactory {
	private static final StoryComponentPanelFactory instance = new StoryComponentPanelFactory();
	private static final String QUESTION = "question";

	public static StoryComponentPanelFactory getInstance() {
		return instance;
	}

	/**
	 * Creates, populates and returns a StoryComponentPanel visually
	 * representing the current state of the StoryComponent.
	 * 
	 * @param component
	 * @param editable
	 * @param collapsed
	 * @return
	 */
	public StoryComponentPanel buildStoryComponentPanel(StoryComponent component) {
		if (component == null)
			throw new IllegalArgumentException(
					"Cannot build a StoryComponentPanel for a null StoryComponent");

		StoryComponentPanel panel = new StoryComponentPanel(component);

		component.process(componentProcessor(panel));
		panel.setTransferHandler(StoryComponentPanelTransferHandler
				.getInstance());

		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setAlignmentY(Component.TOP_ALIGNMENT);
		return panel;
	}

	/**
	 * Reconstruct the StoryComponentPanel with the given child added
	 * 
	 * @param panel
	 * @param child
	 */
	public void addChild(StoryComponentPanel panel, StoryComponent child) {
		final StoryComponent parent = panel.getStoryComponent();
		final StoryComponentPanelManager selectionManager = panel
				.getSelectionManager();

		if (parent instanceof ComplexStoryComponent) {
			if (selectionManager != null) {
				int index = ((ComplexStoryComponent) parent)
						.getChildIndex(child);
				if (index == -1)
					throw new IllegalStateException(child
							+ " is not a child of " + parent);
				StoryComponentPanel childPanel = buildStoryComponentPanel(child);

				// Add the child panel to the parent panel.
				panel.add(childPanel, StoryComponentPanelLayoutManager.CHILD,
						index);

				// Update the settings of the child panel to match the parents
				StoryComponentPanelTree parentTree = panel.getParentTree();
				if (parentTree != null) {
					childPanel.updateComplexSettings();
				}
				// if the parent is selected, select the child as well
				boolean select = selectionManager.getSelectedPanels().contains(
						panel);
				selectionManager.addComplexPanel(childPanel, select);
			}
		} else {
			throw new IllegalStateException(parent
					+ " is not a ComplexStoryComponent and cannot have "
					+ child + " added");
		}
	}

	/**
	 * Reconstruct the StoryComponentPanel with the given child removed
	 * 
	 * @param panel
	 * @param child
	 */
	public void removeChild(StoryComponentPanel panel, StoryComponent child) {
		final StoryComponent parent = panel.getStoryComponent();

		// Get the StoryComponentPanel with the child component
		if (parent instanceof ComplexStoryComponent) {
			StoryComponentPanel childPanel = null;
			for (StoryComponentPanel aPanel : panel.getChildrenPanels()) {
				if (aPanel.getStoryComponent() == child) {
					childPanel = aPanel;
					break;
				}
			}

			if (childPanel != null) {
				panel.remove(childPanel);
				StoryComponentPanelManager selectionManager = panel
						.getSelectionManager();
				if (selectionManager != null)
					selectionManager.cleanUpPanel(childPanel);
			} else
				System.err.println("Attempted to remove " + child
						+ "'s StoryComponentPanel when it is not a child of "
						+ parent);
		} else
			throw new IllegalStateException(parent
					+ " is not a ComplexStoryComponent and cannot have "
					+ child + " removed");
	}

	/**
	 * Reconstructs the main panel of the given StoryComponentPanel with the
	 * current state of the model.
	 * 
	 * @param panel
	 */
	public void refreshStoryComponentPanel(StoryComponentPanel panel) {
		final StoryComponent storyComponent = panel.getStoryComponent();
		final StoryComponentPanelLayoutManager layout = panel.getLayout();
		if (layout != null) {
			final JPanel mainPanel = layout.getMainPanel();
			if (mainPanel != null) {
				// clear the panel
				mainPanel.removeAll();
				// rebuild the panel according to it's storycomponent type
				storyComponent.process(new AbstractNoOpStoryVisitor() {
					@Override
					protected void defaultProcessComplex(
							ComplexStoryComponent complex) {
						parseDisplayText(mainPanel, complex);
					}

					@Override
					public void processQuestPoint(QuestPoint questPoint) {
						buildMainQuestPointPanel(questPoint, mainPanel);
					}

					@Override
					public void processKnowIt(KnowIt knowIt) {
						buildMainKnowItPanel(knowIt, mainPanel);
					}
				});
			} else
				throw new IllegalStateException(
						"Attempted to refresh a null Main Panel");
		}
	}

	/**
	 * Converts the display text of a StoryComponent from a String into a
	 * graphical representation that uses different GUI components.
	 * 
	 * @param storyComponent
	 * @author graves
	 * @author mfchurch
	 */
	public void parseDisplayText(JPanel displayNamePanel,
			StoryComponent storyComponent) {
		String toParse = storyComponent.getDisplayText();
		int paramTagStart;
		int paramTagEnd;
		String tagName;
		JLabel plainTextLabel;
		String plainText;
		KnowIt knowIt = null;
		displayNamePanel.removeAll();
		displayNamePanel.setOpaque(false);
		displayNamePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

		// Add the StoryComponent's labels
		for (String labelText : storyComponent.getLabels()) {
			if (!labelText.isEmpty()) {
				JLabel label = ScriptWidgetFactory.buildLabel(labelText,
						ScriptWidgetFactory.LABEL_TEXT_COLOUR,
						ScriptWidgetFactory.LABEL_BACKGROUND_COLOUR);
				displayNamePanel.add(label);
				displayNamePanel.add(Box.createHorizontalStrut(5));
			}
		}

		// Loop through the display text until there is no more to parse.
		while (toParse.length() > 0) {
			// Get the first occurrence of a parameter name.
			paramTagStart = toParse.indexOf("<");
			paramTagEnd = toParse.indexOf(">", paramTagStart);

			if (paramTagStart == -1 || paramTagEnd == -1) {
				// No more parameter references found, so the rest is text.
				plainText = toParse;
			} else {
				// Parameter tag was found. Check to see if it's valid.
				tagName = toParse.substring(paramTagStart + 1, paramTagEnd);
				plainText = toParse.substring(0, paramTagStart);

				if (tagName.equalsIgnoreCase(QUESTION)
						&& storyComponent instanceof AskIt) {
					knowIt = ((AskIt) storyComponent).getCondition();
				}
				// Now check for a parameter/implicit tag
				else {
					// Check for parameters
					if (knowIt == null && storyComponent instanceof ScriptIt) {
						knowIt = getParameterWithText(
								(ScriptIt) storyComponent, tagName);
					}

					// Invalid parameter tags are treated as plain text,
					// valid
					// ones are represented with the appropriate widget.
					if (knowIt == null)
						plainText += toParse.substring(paramTagStart,
								paramTagEnd + 1);
				}
			}

			plainTextLabel = ScriptWidgetFactory.buildLabel(plainText, null);

			displayNamePanel.add(plainTextLabel);

			if (knowIt != null) {
				addWidget(displayNamePanel, knowIt, false);
				knowIt = null;
			}

			// Update toParse for the next iteration of the loop.
			toParse = paramTagEnd < 0 ? "" : toParse.substring(paramTagEnd + 1);
		}

		displayNamePanel.setMaximumSize(displayNamePanel.getPreferredSize());
	}

	/**
	 * Finds and returns the parameter of <code>component</code> with display
	 * text equal to <code>parameterName</code> (case insensitive), if it
	 * exists.
	 * 
	 * @param component
	 * @param parameterName
	 * @return the parameter of <code>component</code> with display text
	 *         <code>parameterName</code>
	 */
	private static KnowIt getParameterWithText(ScriptIt component,
			String parameterName) {
		final Collection<KnowIt> parameters = component.getParameters();
		for (KnowIt parameter : parameters) {
			if (parameter.getDisplayText().equalsIgnoreCase(parameterName)) {
				return parameter;
			}
		}

		// Otherwise check implicits
		final Collection<KnowIt> implicits = component.getImplicits();
		for (KnowIt implicit : implicits) {
			if (implicit.getDisplayText().equalsIgnoreCase(parameterName)) {
				return implicit;
			}
		}
		return null;
	}

	/**
	 * Adds the resolved widget for the given knowIt to the given JPanel
	 * 
	 * @param displayNamePanel
	 * @param knowIt
	 */
	private void addWidget(final JPanel displayNamePanel, final KnowIt knowIt,
			final boolean editable) {
		final KnowItBinding binding = knowIt.getBinding();
		binding.process(new AbstractNoOpBindingVisitor() {
			// functions, descriptions and runTimes all get a draggable bubble
			// with no slot
			@Override
			public void processFunction(KnowItBindingFunction function) {
				displayNamePanel.add(ScriptWidgetFactory.buildBindingWidget(
						knowIt, editable));
			}

			@Override
			public void processRunTime(KnowItBindingRunTime runTime) {
				displayNamePanel.add(ScriptWidgetFactory.buildBindingWidget(
						knowIt, editable));
			}

			@Override
			public void processDescribeIt(KnowItBindingDescribeIt described) {
				displayNamePanel.add(ScriptWidgetFactory.buildBindingWidget(
						knowIt, editable));
			}

			// everything else gets a regular slot
			@Override
			protected void defaultProcess(KnowItBinding binding) {
				displayNamePanel.add(ScriptWidgetFactory.buildSlotPanel(knowIt));
			}
		});
	}

	private AbstractNoOpStoryVisitor componentProcessor(
			final StoryComponentPanel panel) {
		return new AbstractNoOpStoryVisitor() {
			@Override
			public void processQuestPoint(QuestPoint questPoint) {
				// Add an expansion button
				addExpansionButton(questPoint, panel);

				JPanel mainPanel = new JPanel();
				buildMainQuestPointPanel(questPoint, mainPanel);

				// Add a BindingWidget for the QuestPoint
				panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);

				// Add the children panels
				addChildrenPanels(questPoint, panel);
			}

			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				// Add an expansion button
				addExpansionButton(complex, panel);

				JPanel mainPanel = new JPanel();
				parseDisplayText(mainPanel, complex);

				// Add a label for the complex story component
				panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);

				// Add the children panels
				addChildrenPanels(complex, panel);
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				JPanel mainPanel = new JPanel();
				buildMainKnowItPanel(knowIt, mainPanel);
				panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);
			}
		};
	}

	private void addExpansionButton(ComplexStoryComponent complex,
			final StoryComponentPanel panel) {
		// Add expansion button if you are not root
		if (complex.getOwner() != null) {
			final ExpansionButton expansionButton = ScriptWidgetFactory
					.buildExpansionButton(!panel.showChildren());
			expansionButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean showChildren = !panel.showChildren();
					panel.setShowChildren(showChildren);
					expansionButton.setCollapsed(!showChildren);
					panel.revalidate();
				}
			});
			panel.add(expansionButton, StoryComponentPanelLayoutManager.BUTTON);
		}
	}

	private void addChildrenPanels(ComplexStoryComponent complex,
			StoryComponentPanel panel) {
		final boolean hasChildren = complex.getChildCount() > 0;
		if (hasChildren) {
			// Add child panels
			for (StoryComponent component : complex.getChildren()) {
				StoryComponentPanel childPanel = StoryComponentPanelFactory
						.getInstance().buildStoryComponentPanel(component);
				panel.add(childPanel, StoryComponentPanelLayoutManager.CHILD);
			}
		}
	}

	private void buildMainQuestPointPanel(QuestPoint questPoint,
			JPanel mainPanel) {
		// Add a BindingWidget for the QuestPoint
		mainPanel
				.add(ScriptWidgetFactory.buildBindingWidget(questPoint, false));
		mainPanel.setOpaque(false);
	}

	private void buildMainKnowItPanel(KnowIt knowIt, final JPanel mainPanel) {
		// Add displayName panel
		mainPanel.removeAll();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

		addWidget(mainPanel, knowIt, true);
		final KnowItBinding binding = knowIt.getBinding().resolveBinding();

		binding.process(new AbstractNoOpBindingVisitor() {
			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				// do nothing for KnowItBindingNull. Not even the default.
				// That's right. We hate empty bindings so much that we won't
				// even talk to them.
			}

			private void processDefault() {
				mainPanel.add(ScriptWidgetFactory.buildLabel(" "
						+ DescribeIt.DESCRIBES + " ", Color.black));
			}

			@Override
			public void processDescribeIt(KnowItBindingDescribeIt described) {
				processDefault();
				DescribeIt describeIt = described.getValue();
				DescribeItPanel describeItPanel = new DescribeItPanel(
						describeIt, true);
				mainPanel.add(describeItPanel);
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				processDefault();
				ScriptIt scriptIt = function.getValue();
				JPanel displayNamePanel = new JPanel();
				parseDisplayText(displayNamePanel, scriptIt);
				mainPanel.add(displayNamePanel);
			}
		});
	}
}
