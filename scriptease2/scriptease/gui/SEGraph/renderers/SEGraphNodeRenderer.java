package scriptease.gui.SEGraph.renderers;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction.ToolBarMode;
import scriptease.util.GUIOp;

/**
 * Renders individual components as graph nodes. The default behaviour sets the
 * background and border of the JComponent depending on the current tool
 * selected and the actions of the user.<br>
 * <br>
 * To add internal components to the JComponent, override the
 * {@link #configureInternalComponents(JComponent, Object)} method.
 * 
 * @author remiller
 * @author kschenk
 * 
 * @param <E>
 */
public class SEGraphNodeRenderer<E> {
	private final SEGraph<E> graph;

	// This is such a weird hack. I apologize. - remiller
	private JComponent hoveredComponent = null;
	private JComponent pressedComponent = null;

	public SEGraphNodeRenderer(SEGraph<E> graph) {
		this.graph = graph;
	}

	public final JComponent createComponentForNode(E node) {
		final JComponent component;
		// check if the node already has a component
		// otherwise build it and store it
		component = new JPanel();

		component.setOpaque(true);

		this.configureAppearance(component, node);
		this.configureInternalComponents(component, node);

		component.addMouseListener(this.componentAppearanceMouseListener(node));

		return component;
	}

	/**
	 * By default, this does nothing.<br>
	 * <br>
	 * It can be used by subclasses to add any special components inside of the
	 * component representing the node. For example, StoryPoint nodes add Fan In
	 * panels and binding widgets for the StoryPoint.
	 * 
	 * @param component
	 * @param node
	 */
	protected void configureInternalComponents(JComponent component, E node) {
	}

	protected E getStartNode() {
		return this.graph.getStartNode();
	}

	/**
	 * Creates a mouse adapter for appearance. For mouse listeners on components
	 * that act on the model, see {@link SEGraph}.
	 */
	private MouseAdapter componentAppearanceMouseListener(final E node) {
		return new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();
				final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();

				/*
				 * Only respond to releases that happen over this component. The
				 * default is to respond to releases if the press occurred in
				 * this component. This seems to be a Java bug, but I can't find
				 * any kind of complaint for it. Either way, we want this
				 * behaviour, not the default. - remiller
				 */
				if (!component.contains(
						mouseLoc.x - component.getLocationOnScreen().x,
						mouseLoc.y - component.getLocationOnScreen().y))
					return;

				SEGraphNodeRenderer.this.pressedComponent = null;

				resetAppearance();
				configureAppearance(component, node);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();

				SEGraphNodeRenderer.this.pressedComponent = component;
				configureAppearance(component, node);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();

				SEGraphNodeRenderer.this.hoveredComponent = component;
				configureAppearance(component, node);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();

				SEGraphNodeRenderer.this.hoveredComponent = null;
				SEGraphNodeRenderer.this.pressedComponent = null;

				configureAppearance(component, node);
			}
		};
	}

	/**
	 * Resets the appearance of all components to the default white colour.
	 */
	private void resetAppearance() {
		final Color backgroundColour;
		final Color borderColour;

		backgroundColour = Color.white;
		borderColour = Color.GRAY;


		for (Entry<E, JComponent> entry : this.graph.getNodesToComponentsMap()
				.getEntrySet()) {
			if (GraphToolBarModeAction.getMode() != ToolBarMode.SELECT) {
				if (entry.getValue() == this.graph.getSelectedComponent())
					continue;
			}
			this.setComponentAppearance(entry.getValue(), entry.getKey(),
					borderColour, backgroundColour);

		}

	/*	for (JComponent component : this.graph.getNodeComponents()) {
			if (GraphToolBarModeAction.getMode() != ToolBarMode.SELECT) {
				if (component == this.graph.getSelectedComponent())
					continue;
			}
			this.setComponentAppearance(component, null, borderColour,
					backgroundColour);

		}*/

	}

	/**
	 * Sets the appearance of the passed in node to the background colour and
	 * border colour passed in.
	 * 
	 * @param component
	 *            The component to set the appearance for.
	 * @param borderColour
	 *            The border colour to set on the component.
	 * @param backgroundColour
	 *            The background colour to set for the component.
	 */
	private void setComponentAppearance(JComponent component, E node,
			Color borderColour, Color backgroundColour) {
		final Border lineBorder;
		final Border lineSpaceBorder;

		lineBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED,
				borderColour, borderColour.darker());

		if (node != this.graph.getStartNode())
			lineSpaceBorder = BorderFactory.createCompoundBorder(lineBorder,
					BorderFactory.createEmptyBorder(3, 3, 3, 3));
		else {
			final Border secondLineBorder;

			secondLineBorder = BorderFactory.createCompoundBorder(lineBorder,
					BorderFactory.createEmptyBorder(3, 3, 3, 3));
			lineSpaceBorder = BorderFactory.createCompoundBorder(
					secondLineBorder,
					BorderFactory.createLineBorder(borderColour));
		}

		component.setBorder(lineSpaceBorder);
		component.setBackground(backgroundColour);
	}

	/**
	 * Method to configure the component's appearance based on the current mode
	 * of the ToolBar.
	 * 
	 * @param component
	 *            The display component to configure.
	 * @param node
	 *            The graph node to configure based on.
	 */
	private void configureAppearance(final JComponent component, final E node) {
		if (component == null)
			return;
		final Color toolColour;
		final Color toolHighlight;
		final Color toolPress;

		final Color borderColour;
		final Color backgroundColour;

		// first, determine the tool colour and highlight.
		/*
		 * These are using the game object colours because they're convenient
		 * and close enough. Feel free to add colours to ScriptEaseUI if you
		 * want other colours. - remiller
		 */
		if (this.hoveredComponent == component) {
			if (GraphToolBarModeAction.getMode() == ToolBarMode.INSERT) {
				toolColour = ScriptEaseUI.COLOUR_INSERT_NODE;
				toolHighlight = GUIOp.scaleWhite(toolColour, 1.1);
				toolPress = GUIOp.scaleWhite(toolHighlight, 1.1);
			} else if (GraphToolBarModeAction.getMode() == ToolBarMode.DELETE) {
				toolColour = ScriptEaseUI.COLOUR_DELETE_NODE;
				toolHighlight = GUIOp.scaleWhite(toolColour, 1.2);
				toolPress = GUIOp.scaleWhite(toolHighlight, 1.4);
			} else {
				toolColour = ScriptEaseUI.COLOUR_SELECTED_NODE;
				toolHighlight = GUIOp.scaleColour(
						ScriptEaseUI.COLOUR_SELECTED_NODE, 1.1);
				toolPress = GUIOp.scaleColour(toolHighlight, 1.1);
			}

			if (this.pressedComponent == component) {
				// If pressed while being hovered over
				backgroundColour = toolPress;
			} else {
				// If hovered over
				backgroundColour = toolHighlight;
			}
			borderColour = GUIOp.scaleColour(toolColour, 0.7);

		}

		/*
		 * Use a bright tool colour if its pressed, use the tool colour if it's
		 * hovered over, use gold if its selected and not hovered, white/gray
		 * otherwise.
		 */
		else if (this.graph.getSelectedNode() == node) {
			borderColour = GUIOp.scaleColour(ScriptEaseUI.COLOUR_SELECTED_NODE,
					0.6);
			backgroundColour = ScriptEaseUI.COLOUR_SELECTED_NODE;
			// If nothing and selected
		} else {
			borderColour = Color.gray;
			backgroundColour = Color.white;
			// If nothing
		}

		this.setComponentAppearance(component, node, borderColour,
				backgroundColour);
	}
}