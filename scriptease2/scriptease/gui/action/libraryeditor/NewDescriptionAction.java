package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.ScriptEase;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Inserts a new Description into the Library.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class NewDescriptionAction extends ActiveModelSensitiveAction {
	private static final String NEW_DESCRIPTION_NAME = "Description";

	private static final NewDescriptionAction instance = new NewDescriptionAction();

	public static final NewDescriptionAction getInstance() {
		return instance;
	}

	private NewDescriptionAction() {
		super(NewDescriptionAction.NEW_DESCRIPTION_NAME);

		this.putValue(Action.SHORT_DESCRIPTION,
				NewDescriptionAction.NEW_DESCRIPTION_NAME);
		this.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK
						+ ActionEvent.SHIFT_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final LibraryModel library;
		final DescribeItNode describeItNode;
		final DescribeIt describeIt;
		final KnowIt knowIt;

		library = (LibraryModel) SEModelManager.getInstance().getActiveModel();

		if (!library.isReadOnly() || ScriptEase.DEBUG_MODE) {
			describeItNode = new DescribeItNode("Placeholder Node");
			describeIt = new DescribeIt("New DescribeIt", describeItNode);
			knowIt = library.createKnowItForDescribeIt(describeIt);

			library.addDescribeIt(describeIt, knowIt);
			library.add(knowIt);
		}
	}
}
