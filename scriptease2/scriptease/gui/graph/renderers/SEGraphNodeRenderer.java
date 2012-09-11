package scriptease.gui.graph.renderers;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.graph.SEGraph;
import scriptease.util.BiHashMap;
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

	private JComponent selectedComponent;
	// This is such a weird hack. I apologize. - remiller
	private Set<JComponent> hoverComponents = new HashSet<JComponent>();
	private Set<JComponent> pressComponents = new HashSet<JComponent>();

	private BiHashMap<E, JComponent> componentMap = new BiHashMap<E, JComponent>();

	public final JComponent getComponentForNode(E node) {
		final JComponent component;
		// check if the node already has a component
		final JComponent storedComponent = this.componentMap.getValue(node);
		if (storedComponent != null) {
			component = storedComponent;
		} else {
			// otherwise build it and store it
			component = new JPanel();
			this.componentMap.put(node, component);

			component.setOpaque(true);
			this.configureAppearance(component);
			component.addMouseListener(this.componentAppearanceMouseListener());
			this.configureInternalComponents(component);
		}
		// return the component for the node
		return component;
	}

	/**
	 * Returns the node represented by a component.
	 * 
	 * @param component
	 * @return
	 */
	protected E getNodeForComponent(JComponent component) {
		return this.componentMap.getKey(component);
	}

	/**
	 * By default, this does nothing.<br>
	 * <br>
	 * It can be used by subclasses to add any special components inside of the
	 * component representing the node. For example, QuestPoint nodes add Fan In
	 * panels and binding widgets for the Quest Point.
	 * 
	 * @param component
	 */
	protected void configureInternalComponents(JComponent component) {
	}

	/**
	 * Creates a mouse adapter for appearance. For mouse listeners on components
	 * that act on the model, see {@link SEGraph}.
	 */
	private MouseAdapter componentAppearanceMouseListener() {
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

				SEGraphNodeRenderer.this.pressComponents.remove(component);

				resetAppearance();
				configureAppearance(component);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();

				SEGraphNodeRenderer.this.pressComponents.add(component);
				configureAppearance(component);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();

				SEGraphNodeRenderer.this.hoverComponents.add(component);
				configureAppearance(component);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();

				SEGraphNodeRenderer.this.hoverComponents.remove(component);
				SEGraphNodeRenderer.this.pressComponents.remove(component);

				configureAppearance(component);
			}
		};
	}

	/**
	 * Sets the selected node and updates its appearance.
	 * 
	 * @param node
	 *            The new selected node.
	 */
	public void setSelectedNode(E node) {
		final JComponent component;

		final Color backgroundColour;
		final Color borderColour;

		component = this.getComponentForNode(node);
		this.selectedComponent = component;

		backgroundColour = ScriptEaseUI.SELECTED_GRAPH_NODE;
		borderColour = GUIOp.scaleColour(ScriptEaseUI.SELECTED_GRAPH_NODE, 0.6);

		this.setComponentAppearance(component, borderColour, backgroundColour);
	}

	/**
	 * Resets the appearance of all components to the default white colour.
	 */
	private void resetAppearance() {
		final Color backgroundColour;
		final Color borderColour;

		backgroundColour = Color.white;
		borderColour = Color.GRAY;

		for (JComponent component : this.componentMap.getValues()) {
			this.setComponentAppearance(component, borderColour,
					backgroundColour);
		}
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
	private void setComponentAppearance(JComponent component,
			Color borderColour, Color backgroundColour) {
		final Border lineBorder;
		final Border lineSpaceBorder;

		lineBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED,
				borderColour, borderColour.darker());
		lineSpaceBorder = BorderFactory.createCompoundBorder(lineBorder,
				BorderFactory.createEmptyBorder(3, 3, 3, 3));

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
	private void configureAppearance(final JComponent component) {
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
		if (this.hoverComponents.contains(component)) {
			if (ToolBarButtonAction.getMode() == ToolBarButtonMode.INSERT_GRAPH_NODE) {
				toolColour = ScriptEaseUI.COLOUR_KNOWN_OBJECT;
				toolHighlight = GUIOp.scaleWhite(toolColour, 1.6);
				toolPress = GUIOp.scaleWhite(toolHighlight, 1.8);
			} else if (ToolBarButtonAction.getMode() == ToolBarButtonMode.DELETE_GRAPH_NODE) {
				toolColour = ScriptEaseUI.COLOUR_UNBOUND;
				toolHighlight = GUIOp.scaleWhite(toolColour, 1.3);
				toolPress = GUIOp.scaleWhite(toolHighlight, 1.8);
			} else {
				toolColour = ScriptEaseUI.SELECTED_GRAPH_NODE;
				toolHighlight = GUIOp.scaleColour(
						ScriptEaseUI.SELECTED_GRAPH_NODE, 1.05);
				toolPress = GUIOp.scaleColour(toolHighlight, 1.6);
			}

			if (this.pressComponents.contains(component)) {
				// If pressed while being hovered over
				backgroundColour = toolPress;
			} else {
				// If hovered over
				backgroundColour = toolHighlight;
			}
			borderColour = GUIOp.scaleColour(toolColour, 0.7);

			this.setComponentAppearance(component, borderColour,
					backgroundColour);
		}

		/*
		 * Use a bright tool colour if its pressed, use the tool colour if it's
		 * hovered over, use gold if its selected and not hovered, white/gray
		 * otherwise.
		 */
		else if (this.selectedComponent == component) {
			// If nothing and selected
			this.setSelectedNode(this
					.getNodeForComponent(this.selectedComponent));
		} else {
			// If nothing
			this.setComponentAppearance(component, Color.gray, Color.white);
		}
	}
}