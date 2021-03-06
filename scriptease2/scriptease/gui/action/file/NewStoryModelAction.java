package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.WindowFactory;
import scriptease.gui.internationalization.Il8nResources;

/**
 * Represents and performs the New Model command, as well as encapsulates its
 * enabled and name display state. <br>
 * <br>
 * New Model entails creating a new model after allowing the user to select a module.
 * 
 * @author graves
 */
@SuppressWarnings("serial")
public final class NewStoryModelAction extends AbstractAction {
	private static final String NEW_MODEL = Il8nResources
			.getString("New_Model");

	private static final Action instance = new NewStoryModelAction();
	
	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return NewStoryModelAction.instance;
	}
	
	/**
	 * Defines a <code>NewModelAction</code> object with accelerator and
	 * mnemonic.
	 */
	private NewStoryModelAction() {
		super(NewStoryModelAction.NEW_MODEL + "...");
		
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_N, ActionEvent.CTRL_MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// run a new model wizard!
		WindowFactory.getInstance().showNewStoryWizardDialog();
	}

}
