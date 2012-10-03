package scriptease.gui.cell;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import scriptease.controller.BindingAdapter;
import scriptease.controller.MouseForwardingAdapter;
import scriptease.controller.groupvisitor.SameBindingGroupVisitor;
import scriptease.gui.transfer.ProxyTransferHandler;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameType.TypeValueWidgets;
import scriptease.translator.io.tools.SimpleGameConstant;
import scriptease.util.GUIOp;

/**
 * SlotPanel is a GUI slot which accepts KnowIt Bindings (binding slot). It
 * displays all of its acceptable types as well as a hint that the user can drop
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class SlotPanel extends JPanel {
	private static final Color GROUP_HIGHLIGHT_COLOUR = Color.CYAN;
	private static final Border GROUP_HIGHLIGHT_BORDER = BorderFactory
			.createLineBorder(GROUP_HIGHLIGHT_COLOUR, 2);
	private JPanel typesPanel;
	private JComponent inputComponent;
	private BindingWidget bindingWidget;

	public SlotPanel(final KnowIt knowIt) {
		if (knowIt == null)
			throw new IllegalStateException(
					"Cannot build a SlotPanel with a null KnowIt");

		// Set a border of 2 pixels around the slot.
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

		final Border slotBorder;
		// Set the layout for this panel.
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));

		// Set the layout for the types panel.
		this.typesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		this.inputComponent = null;
		this.bindingWidget = null;

		this.typesPanel.setOpaque(false);

		/*
		 * Slot panels have three functions: 1 is to display the legal types for
		 * the slot, 2 is to display the current binding, and 3 to provide an
		 * interface for rebinding/unbinding knowIts
		 */

		// 1. add and maintain the types list
		this.add(ScriptWidgetFactory.populateLegalTypesPanel(this.typesPanel,
				knowIt), 0);

		slotBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED);
		this.setBorder(slotBorder);

		// 2. now handle the binding portion
		this.bindingWidget = this.buildBindingWidget(knowIt);
		this.add(this.bindingWidget);

		enableTransferHandler();

		this.setBackground(GUIOp.scaleColour(
				this.bindingWidget.getBackground(), 0.95));
	}

	private void enableTransferHandler() {
		// Make it think you are dropping bindings on the widget
		ProxyTransferHandler proxyHandler = new ProxyTransferHandler(
				this.bindingWidget);
		this.setTransferHandler(proxyHandler);
		this.typesPanel.setTransferHandler(proxyHandler);
		this.inputComponent.setTransferHandler(proxyHandler);
	}

	private void disableTransferHandler() {
		this.setTransferHandler(null);
		this.typesPanel.setTransferHandler(null);
		this.inputComponent.setTransferHandler(null);
	}

	private BindingWidget buildBindingWidget(final KnowIt knowIt) {
		final BindingWidget bindingWidget;
		final KnowItBinding binding = knowIt.getBinding();
		bindingWidget = new BindingWidget(binding);

		// The slotPanel inherits the colour of its binding.
		bindingWidget.addPropertyChangeListener("background",
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("background")) {
							SlotPanel.this.setBackground(bindingWidget
									.getBackground());
						}
					}
				});

		// Build the input component
		binding.process(new BindingAdapter() {
			Translator translator = TranslatorManager.getInstance()
					.getActiveTranslator();
			GameTypeManager typeManager = this.translator == null ? null
					: this.translator.getGameTypeManager();

			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				SlotPanel.this.inputComponent = ScriptWidgetFactory.buildLabel(
						knowIt.getDisplayText(), Color.WHITE);
			}

			@Override
			public void processReference(KnowItBindingReference reference) {
				SlotPanel.this.inputComponent = ScriptWidgetFactory.buildLabel(
						reference.getValue().getDisplayText(), Color.WHITE);
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				SlotPanel.this.inputComponent = ScriptWidgetFactory.buildLabel(
						function.getValue().getDisplayText(), Color.WHITE);
			}

			@Override
			public void processStoryPoint(KnowItBindingStoryPoint storyPoint) {
				SlotPanel.this.inputComponent = ScriptWidgetFactory.buildLabel(
						storyPoint.getValue().getDisplayText(), Color.WHITE);
			}

			@Override
			public void processConstant(KnowItBindingConstant constant) {
				GameConstant constantValue = constant.getValue();
				String name = constantValue.getName();
				if (constantValue instanceof SimpleGameConstant) {
					final String bindingType = binding.getFirstType();
					TypeValueWidgets widgetName = this.typeManager == null ? null
							: this.typeManager.getGui(bindingType);

					if (widgetName == null)
						SlotPanel.this.inputComponent = ScriptWidgetFactory
								.buildLabel(name, Color.WHITE);
					else if (widgetName.equals(TypeValueWidgets.JSPINNER)) {
						SlotPanel.this.inputComponent = ScriptWidgetFactory
								.buildSpinnerEditor(knowIt, constantValue,
										bindingType);
					} else if (widgetName.equals(TypeValueWidgets.JCOMBOBOX)) {
						SlotPanel.this.inputComponent = ScriptWidgetFactory
								.buildComboEditor(knowIt, bindingType);
					} else {
						SlotPanel.this.inputComponent = ScriptWidgetFactory
								.buildValueEditor(knowIt);
					}
				} else {
					SlotPanel.this.inputComponent = ScriptWidgetFactory
							.buildLabel(name, Color.WHITE);
				}
			}

			@Override
			protected void defaultProcess(KnowItBinding binding) {
				SlotPanel.this.inputComponent = ScriptWidgetFactory.buildLabel(
						knowIt.getDisplayText(), Color.WHITE);
			}
		});

		bindingWidget.add(this.inputComponent);

		/**
		 * Mouse Listener for group highlighting
		 */
		bindingWidget.addMouseListener(new MouseAdapter() {
			final Border border = SlotPanel.this.getBorder();

			@Override
			public void mouseEntered(MouseEvent e) {
				setGroupBorder(GROUP_HIGHLIGHT_BORDER);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setGroupBorder(this.border);
			}

			private void setGroupBorder(final Border aBoder) {
				knowIt.getBinding().process(new BindingAdapter() {
					@Override
					public void processNull(KnowItBindingNull nullBinding) {
						// do nothing for null, not even default
					}

					@Override
					protected void defaultProcess(KnowItBinding binding) {
						SameBindingGroupVisitor groupVisitor = new SameBindingGroupVisitor(
								knowIt);
						Collection<KnowIt> group = groupVisitor.getGroup();
						if (group.size() > 1) {
							for (KnowIt knowIt : group) {
								Collection<JPanel> panels = ScriptWidgetFactory
										.getEditedJPanel(knowIt);
								for (JPanel panel : panels) {
									panel.setBorder(aBoder);
									panel.repaint();
								}
							}
						}
					}
				});
			}
		});

		return bindingWidget;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}

		if (enabled) {
			enableTransferHandler();
			this.removeMouseListener(MouseForwardingAdapter.getInstance());
			this.removeMouseMotionListener(MouseForwardingAdapter.getInstance());
		} else {
			disableTransferHandler();
			this.addMouseListener(MouseForwardingAdapter.getInstance());
			this.addMouseMotionListener(MouseForwardingAdapter.getInstance());
		}
	}
}