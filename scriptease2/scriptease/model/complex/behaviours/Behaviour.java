package scriptease.model.complex.behaviours;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * A Behaviour represents a series of Tasks {@link Task}. A Behaviour can be
 * independent or collaborative, as defined by its tasks. An independent
 * behaviour is one that is executed by only one subject, while a collaborative
 * behaviour has a respondant to the subject in execution.
 * 
 * For example, an independent behaviour could be a subject walking around
 * randomly by him/herself. A collaborative behaviour could be a tavern patron
 * interacting with the bartender to perform a order drink behaviour.
 * 
 * @author jyuen
 */
public class Behaviour extends ScriptIt {

	private Task startTask;
	private Type type;

	private int priority;

	public enum Type {
		INDEPENDENT, COLLABORATIVE
	}

	public Behaviour(String name) {
		this(name, Type.INDEPENDENT, null, 0);
	}
	
	/**
	 * Creates a new behaviour.
	 * 
	 * @param name
	 *            the name for the behaviour
	 * @param type
	 *            the type of the behaviour - Independent or Collaborative
	 * @param startTask
	 *            the start task for this behaviour
	 * @param priority
	 *            the priority of this behaviour - higher priority means higher
	 *            order of execution.
	 */
	public Behaviour(String name, Behaviour.Type type, Task startTask,
			int priority) {
		super(name);

		this.startTask = startTask;
		this.priority = priority;
		this.type = type;
	}

	// ******************* GETTERS AND SETTERS **********************//

	/**
	 * @return the priority
	 */
	public Integer getPriority() {
		return priority;
	}

	/**
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * @return the startTask
	 */
	public Task getStartTask() {
		return startTask;
	}

	/**
	 * @param startTask
	 *            the startTask to set
	 */
	public void setStartTask(Task startTask) {
		this.startTask = startTask;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public Behaviour clone() {
		final Behaviour component = (Behaviour) super.clone();

		component.type = this.type;
		component.priority = this.priority;
		component.startTask = this.startTask.clone();
		
		return component;
	}
	
	@Override
	public void process(StoryVisitor visitor) {
		visitor.processBehaviour(this);
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}
}
