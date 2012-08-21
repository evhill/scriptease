/**
 * 
 */
package scriptease.gui.action.file;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import scriptease.gui.SEFrame;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.PatternModel;

/**
 * Represents and performs the Close Tab command, as well as encapsulates its
 * enabled and name display state. <br>
 * <br>
 * Close Tab entails closing the currently active tab. This may also remove the
 * model from the model pool if it is the last tab open for that model.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public class CloseStoryTabAction extends ActiveModelSensitiveAction {
	private static final String CLOSE_MODULE = Il8nResources
			.getString("Close_Model");
	private final PatternModel model;
	private final JComponent component;

	/**
	 * Defines an <code>CloseModelAction</code> object with a mnemonic.
	 */
	public CloseStoryTabAction(JComponent component, PatternModel model) {
		super(CloseStoryTabAction.CLOSE_MODULE);
		this.model = model;
		this.component = component;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SEFrame.getInstance().removeComponentForModel(this.component, this.model);
	}
}
