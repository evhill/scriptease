package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * This class is a structural asset that simply allows for the model to group
 * whole Patterns together. <br>
 * <br>
 * In a tree view, it would be represented like a folder. It doesn't really have
 * many properties for itself, since its primary purpose is to group its
 * children together. If it helps, consider this type to be less of a direct
 * StoryComponent, and more of a meta-StoryComponent: it is the information that
 * its children belong together.<br>
 * <br>
 * Things like the top-level, overall story of the module would be a
 * StoryComponentContainer. <br>
 * <br>
 * If there are any major new ScriptEase pattern types, ie. on the level of
 * behaviours, encounters, stories, etc, they should be registered here.
 * 
 * @author remiller
 * @author mfchurch
 * @author jyuen
 * 
 */
public class StoryComponentContainer extends ComplexStoryComponent {
	public StoryComponentContainer() {
		this("");
		
		final int max = ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE;
		
		this.registerChildType(AskIt.class, max);
		this.registerChildType(ScriptIt.class, max);
		this.registerChildType(KnowIt.class, max);
		this.registerChildType(StoryComponentContainer.class, max);
		this.registerChildType(Note.class, max);
		this.registerChildType(ControlIt.class, max);
		this.registerChildType(ActivityIt.class, max);
		this.registerChildType(Behaviour.class, max);
		this.registerChildType(PickIt.class, max);
	}

	public StoryComponentContainer(String displayName) {
		this(displayName, new ArrayList<Class<? extends StoryComponent>>());
	}

	public StoryComponentContainer(
			Collection<Class<? extends StoryComponent>> validTypes) {
		this("", validTypes);
	}

	public StoryComponentContainer(String displayName,
			Collection<Class<? extends StoryComponent>> validTypes) {
		super(LibraryModel.getNonLibrary(), displayName);

		for (Class<? extends StoryComponent> validType : validTypes) {
			registerChildType(validType,
					ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		}
	}

	@Override
	public StoryComponentContainer clone() {
		return (StoryComponentContainer) super.clone();
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processStoryComponentContainer(this);
	}

	@Override
	public String toString() {
		return "StoryComponentContainer (\"" + this.getDisplayText() + "\")";
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}
}
