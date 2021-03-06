package scriptease.gui.action;

import javax.swing.AbstractAction;

import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.model.semodel.SEModelManager;

/**
 * Abstract Action implementation that defines a group of Actions whose enabled
 * state is sensitive to whether there is an active model or not. <br>
 * <br>
 * Actions that want to be sensitive to the active model should extend this
 * class. <br>
 * <br>
 * All or almost all of the actions that affect StoryComponents or StoryModels
 * should have this behaviour. If a subclass wants to redefine or extend the
 * definition of when the action should be enabled, it should override
 * {@link #isLegal()}.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public abstract class ActiveModelSensitiveAction extends AbstractAction {
	/**
	 * Builds an action that is sensitive to the model pool's active model
	 * state. It is protected to disallow non-actions from instantiating this
	 * class.
	 * 
	 * @param name
	 *            The desired name of the action.
	 */
	protected ActiveModelSensitiveAction(String name) {
		super(name);

		SEModelManager.getInstance().addSEModelObserver(this,
				new SEModelObserver() {
					@Override
					public void modelChanged(final SEModelEvent event) {
						if (event.getEventType() == SEModelEvent.Type.ACTIVATED
								|| event.getEventType() == SEModelEvent.Type.REMOVED) {
							ActiveModelSensitiveAction.this
									.updateEnabledState();
						}
					}
				});

		this.updateEnabledState();
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * definition of {@link #isLegal()}.
	 */
	protected final void updateEnabledState() {
		this.setEnabled(this.isLegal());
	}

	/**
	 * Determines if this action is a legal action to perform at the current
	 * time. This information is used to determine if it should be enabled
	 * and/or visible.<br>
	 * <br>
	 * Subclasses can override this method to extend the definition of whether
	 * the action is legal or not. If they do override it, it is <i>highly</i>
	 * recommended to include a call to <code>isLegal</code> in the returned
	 * boolean so that the whatever checks are made here also apply.
	 * 
	 * @return True if this action is legal.
	 */
	protected boolean isLegal() {
		return SEModelManager.getInstance().hasActiveModel();
	}
}
