package scriptease.gui.storycomponentbuilder.propertypanel;

import javax.swing.JPanel;

import scriptease.gui.SETree.cell.ScriptWidgetFactory.BindingWidgetBuilder;
import scriptease.model.atomic.KnowIt;

@SuppressWarnings("serial")
public class BindingPanel extends StoryComponentPropertyPanel {

	public BindingPanel(KnowIt parameter) {
		super(parameter);
	}

	@Override
	protected JPanel buildDisplayPanel() {
		return BindingWidgetBuilder.getInstance().buildBindingWidget(
				((KnowIt) this.component), false);
	}

	@Override
	public String toString() {
		return "BindingPanel [" + ((KnowIt) this.component).getBinding() + "]";
	}
}