package scriptease.gui.action.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.ScriptEase;
import scriptease.controller.StoryAdapter;
import scriptease.controller.observer.SEFocusObserver;
import scriptease.gui.SEFocusManager;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Represents and performs the Duplicate command, as well as encapsulates its
 * enabled and name display state.
 * 
 * @author mfchurch
 */
@SuppressWarnings("serial")
public final class DuplicateAction extends ActiveModelSensitiveAction {
	private static final String DUPLICATE_TEXT = "Duplicate";

	private static final Action instance = new DuplicateAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return DuplicateAction.instance;
	}

	/**
	 * Defines a <code>DeleteStoryComponentAction</code> object with no icon.
	 */
	private DuplicateAction() {
		super(DuplicateAction.DUPLICATE_TEXT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));

		SEFocusManager.getInstance().addSEFocusObserver(new SEFocusObserver() {

			@Override
			public void gainFocus(Component oldFocus) {
				DuplicateAction.this.updateEnabledState();
			}

			@Override
			public void loseFocus(Component oldFocus) {
				DuplicateAction.this.updateEnabledState();
			}
		});
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * current selection.
	 */
	protected boolean isLegal() {
		final SEModel activeModel;
		final Component focusOwner;
		final boolean isLegal;

		focusOwner = SEFocusManager.getInstance().getFocus();
		activeModel = SEModelManager.getInstance().getActiveModel();

		if (focusOwner instanceof StoryComponentPanel) {
			isLegal = ((StoryComponentPanel) focusOwner).isRemovable();
		} else if (focusOwner instanceof StoryComponentPanelJList) {
			isLegal = SEModelManager.getInstance().getActiveModel() instanceof LibraryModel;
		} else
			isLegal = false;

		return activeModel != null && isLegal;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component focusOwner;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			// Delete StoryComponentPanels
			final StoryComponentPanel panel;
			final StoryComponentPanelManager manager;

			panel = (StoryComponentPanel) focusOwner;
			manager = panel.getSelectionManager();

			if (manager != null)
				manager.duplicateSelected();
		} else if (focusOwner instanceof StoryComponentPanelJList
				&& SEModelManager.getInstance().getActiveModel() instanceof LibraryModel
				&& (!((LibraryModel) SEModelManager.getInstance()
						.getActiveModel()).getReadOnly() || ScriptEase.DEBUG_MODE)) {
			// Delete elements from StoryComponentPanelJList
			final StoryComponentPanelJList list;
			list = (StoryComponentPanelJList) focusOwner;

			for (Object selectedObject : list.getSelectedValues()) {
				final StoryComponentPanel selectedPanel = (StoryComponentPanel) selectedObject;
				final StoryComponent selectedComponent = selectedPanel
						.getStoryComponent();
				// We want to add the new component to the current library
				// For example if I want to make my own variant of a component
				// that belongs to a read only library
				// We should make a new, editable component if we're working in
				// an editable library.
				// -zturchan
				// final LibraryModel libraryModel =
				// selectedComponent.getLibrary();
				final LibraryModel library = (LibraryModel) SEModelManager
						.getInstance().getActiveModel();
				selectedComponent.process(new StoryAdapter() {

					// Clone ScriptIts, then replace the referenced codeBlocks
					// with modifiable duplicates since we want them to be
					// unique
					@Override
					public void processScriptIt(ScriptIt scriptIt) {
						final ScriptIt clone = scriptIt.clone();
						final Collection<CodeBlock> codeBlocks = clone
								.getCodeBlocks();
						for (CodeBlock codeBlock : codeBlocks) {
							clone.removeCodeBlock(codeBlock);
							codeBlock.process(new StoryAdapter() {

								@Override
								public void processCodeBlockSource(
										CodeBlockSource codeBlockSource) {
									clone.addCodeBlock(codeBlockSource
											.duplicate(library));
								}

								@Override
								public void processCodeBlockReference(
										CodeBlockReference codeBlockReference) {
									codeBlockReference.getTarget()
											.process(this);
								}
							});
						}
						library.add(clone);
					}

					@Override
					protected void defaultProcess(StoryComponent component) {
						final StoryComponent clone = component.clone();
						library.add(clone);
					}
				});
			}
		}

		// Reset the focus after we duplicate something.
		SEFocusManager.getInstance().setFocus(null);
	}
}
