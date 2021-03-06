package scriptease.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Reference to a code block in the translator. Most of the methods implemented
 * from {@link CodeBlock} are forwarded to the target {@link CodeBlockSource},
 * and therefore changes pushed to an instance of CodeBlockReference will appear
 * to effect all other <code>CodeBlockReference</code> instances that reference
 * the same target. Similarly, in the reverse direction, all same-targeted
 * <code>CodeBlockReference</code> instances will return the same data.<br>
 * <br>
 * A <code>CodeBlockReference</code> stores its own list of parameters, because
 * those are context/instance-specific (sharing them in the target makes no
 * sense).<br>
 * <br>
 * <code>CodeBlockReference</code>s can appear either Story side or Translator
 * side, unlike their cousin {@link CodeBlockSource}.
 * 
 * @author remiller
 */
public class CodeBlockReference extends CodeBlock {
	private CodeBlockSource target;

	/**
	 * Creates a CodeBlockReference that points to a codeblock source in the
	 * library. Note that you need to call {@link #setTarget(CodeBlockSource)}
	 * to actually set this up properly. Thus, it's usually a better idea to
	 * call the {@link #CodeBlockReference(CodeBlockSource)} constructor
	 * instead. This one is primarily used to load from XML.
	 */
	public CodeBlockReference(LibraryModel library) {
		super(library);
	}

	/**
	 * Creates a CodeBlockReference that points to the passed in CodeBlock.
	 * 
	 * @param codeBlock
	 */
	public CodeBlockReference(CodeBlockSource codeBlock) {
		super(codeBlock.getLibrary());
		this.setTarget(codeBlock);
	}

	@Override
	public void setOwner(StoryComponent newOwner) {
		super.setOwner(newOwner);

		// we set the parameters initially to null, and then to ourself to mimic
		// the way that parameters get loaded normally. Scoping rules are
		// dependant on it, which is silly, but that's how it is. - remiller
		for (KnowIt param : this.getParameters()) {
			param.setOwner(this);
		}
	}

	@Override
	public CodeBlockReference clone() {
		final CodeBlockReference clone;
		final Collection<KnowIt> originalParameters;
		final Collection<KnowIt> clonedParameters;

		clone = (CodeBlockReference) super.clone();
		originalParameters = this.getParameters();
		clonedParameters = new ArrayList<KnowIt>(originalParameters.size());

		clone.setTarget(this.getTarget());

		clone.init();

		// parameters
		for (KnowIt parameter : originalParameters) {
			final KnowIt clonedParameter = parameter.clone();
			clonedParameters.add(clonedParameter);
		}

		clone.setParameters(clonedParameters);

		// Poof! Tadaa!
		return clone;
	}

	/**
	 * Gets the target CodeBlockSource.
	 * 
	 * @return The target.
	 */
	public CodeBlockSource getTarget() {
		return this.target;
	}

	/**
	 * Sets the target of this Reference to the given target. It also clones the
	 * parameters from the target using {@link #setParameters(Collection)};
	 * 
	 * @param newTarget
	 *            The new target to point to. May be null.
	 */
	public void setTarget(CodeBlockSource newTarget) {
		final CodeBlockSource oldTarget = this.getTarget();

		if (newTarget == null)
			throw new NullPointerException("Cannot set target of " + this
					+ " to null.");

		if (oldTarget == newTarget)
			return;

		if (oldTarget != null)
			oldTarget.removeReference(this);

		this.target = newTarget;

		this.target.addReference(this);

		this.setLibrary(this.target.getLibrary());
		this.setParameters(this.target.getParameters());
	}

	/**
	 * Override of {@link CodeBlock#setParameters(Collection)} that re-clones
	 * parameters from the target and then tries to match bindings to the given
	 * list of parameters. If the current target is <code>null</code>, then this
	 * will have no effect.
	 * 
	 * @param newBindings
	 *            The list of parameters whose bindings should be used for
	 *            matching parameters in this code block.
	 */
	public void setParameters(Collection<KnowIt> newBindings) {
		final List<KnowIt> targetParameters;
		final List<KnowIt> newParameters;
		KnowIt clone;

		/*
		 * Update our parameters from our target before doing anything. They
		 * need to be cloned so that we don't start mucking with the default
		 * values. Plus, they're shared, so we don't want others to change their
		 * value.
		 */
		targetParameters = this.getTarget().getParameters();
		newParameters = new ArrayList<KnowIt>(targetParameters.size());

		for (KnowIt param : targetParameters) {
			clone = param.clone();
			newParameters.add(clone);
			// see setOwner() for why this is set to null
			clone.setOwner(null);
		}
		super.setParameters(newParameters);
		this.setBindings(newBindings);
	}

	@Override
	public boolean addParameter(KnowIt parameter) {
		List<KnowIt> parameters = this.getTarget().getParameters();
		final boolean success = parameters.add(parameter);
		if (success) {
			setParameters(parameters);
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD));
		}
		return success;
	}

	@Override
	public boolean removeParameter(KnowIt parameter) {
		List<KnowIt> parameters = this.getTarget().getParameters();
		final boolean success = parameters.remove(parameter);
		if (success) {
			setParameters(parameters);
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE));
		}
		return success;
	}

	@Override
	public void setTypesByName(Collection<String> types) {
		this.getTarget().setTypesByName(types);
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_PARAMETER_TYPE));
	}

	@Override
	public void setSubject(String subject) {
		this.getTarget().setSubject(subject);
	}

	@Override
	public void setSlot(String slot) {
		this.getTarget().setSlot(slot);
	}

	@Override
	public void setIncludes(Collection<String> includes) {
		this.getTarget().setIncludes(includes);
	}

	@Override
	public boolean hasSubject() {
		return this.getTarget().hasSubject();
	}

	@Override
	public boolean hasSlot() {
		return this.getTarget().hasSlot();
	}

	@Override
	public String getSlot() {
		final ScriptIt cause;
		final CodeBlockSource target = this.getTarget();

		if (target.hasSlot()) {
			return target.getSlot();
		} else {
			cause = this.getCause();
			final CodeBlock parentBlock = cause.getMainCodeBlock();
			return parentBlock.getSlot();
		}
	}

	@Override
	public boolean moveCodeFragment(AbstractFragment fragment, int delta) {
		return this.target.moveCodeFragment(fragment, delta);
	}

	@Override
	public boolean deleteCodeFragment(AbstractFragment fragment) {
		return this.target.deleteCodeFragment(fragment);
	}

	@Override
	public boolean insertCodeFragment(AbstractFragment fragment,
			AbstractFragment previousFragment) {
		return this.target.insertCodeFragment(fragment, previousFragment);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof CodeBlockReference
				&& this.hashCode() == other.hashCode();
	}

	@Override
	public int hashCode() {
		int hashCode;

		hashCode = super.hashCode();
		hashCode += this.target.hashCode();

		return hashCode;
	}

	public Collection<String> getIncludes() {
		return this.getTarget().getIncludes();
	}

	@Override
	public String toString() {
		return "CodeBlockRef [Target:" + this.getTarget().toString()
				+ ", Params:" + this.getParameters() + "]";
	}

	@Override
	public Collection<String> getTypes() {
		return this.getTarget().getTypes();
	}

	@Override
	public void addType(String type) {
		this.getTarget().addType(type);
	}

	@Override
	public void removeType(String type) {
		this.getTarget().removeType(type);
	}

	@Override
	public String getSubjectName() {
		return this.getTarget().getSubjectName();
	}

	@Override
	public void setCode(List<AbstractFragment> code) {
		this.getTarget().setCode(code);
	}

	@Override
	public List<AbstractFragment> getCode() {
		return this.getTarget().getCode();
	}

	protected void targetUpdated(CodeBlockSource source) {
		if (this.getTarget() != source) {
			throw new IllegalStateException("Received update event to "
					+ this.toString()
					+ " from a source that is not my target. That's awkward.");
		}

		this.setParameters(source.getParameters());
		this.resetImplicits();
	}

	/**
	 * Returns the index of the target in the list of codeblocks.
	 * 
	 * @return
	 */
	public int getID() {
		final ScriptIt owner = this.target.getOwner();
		final int id = owner.getCodeBlocks().indexOf(this.target);

		return id;
	}

	/**
	 * Sets this references parameter bindings to the bindings of the given
	 * list, matching as best it can to the KnowIt's display name. Not
	 * guaranteed to match all KnowIts.
	 * 
	 * @param newBindings
	 *            The KnowIts whose bindings are to be used to populate the
	 *            current parameters.
	 */
	private void setBindings(Collection<KnowIt> newBindings) {
		/*
		 * Let's go about trying to rebind whatever is possible to match as
		 * closely as we can the given parameter list.
		 */
		for (KnowIt param : this.getParameters()) {
			// find the matching parameter from the given ones
			KnowIt match = null;
			for (KnowIt bindingParam : newBindings) {
				if (param.getDisplayText()
						.equals(bindingParam.getDisplayText())) {
					match = bindingParam;
					break;
				}
			}
			if (match == null)
				continue;

			// use its binding now that we have it
			param.setBinding(match.getBinding());
		}
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processCodeBlockReference(this);
	}

	@Override
	public void revalidateKnowItBindings() {
		// Do nothing.
	}
}