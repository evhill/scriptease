package scriptease.controller;

import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.StoryModel;

/**
 * Default implementation of ModelVisitor that does nothing. Ever. <br>
 * <br>
 * It is <b>stylistically required</b> that all other ModelVisitor
 * implementations extend this class, allowing us to avoid having to update all
 * of the visitors whenever the interface changes. Subclasses also get the perk
 * of only having to override the methods they <i>do</i> support.<br>
 * <br>
 * Subclasses that wish to provide default behaviour for processing can override
 * {@link #defaultProcess(PatternModel)}. <br>
 * <br>
 * AbstractNoOpStoryVisitor is an Adapter (of the Adapter design pattern) to
 * ModelVisitor.
 * 
 * @author kschenk
 * @see ModelVisitor
 */
public abstract class AbstractNoOpModelVisitor implements ModelVisitor {
	@Override
	public void processStoryModel(StoryModel storyModel) {
		this.defaultProcess(storyModel);
	}

	@Override
	public void processLibraryModel(LibraryModel libraryModel) {
		this.defaultProcess(libraryModel);
	}

	/**
	 * The default process method that is called by every
	 * process<i>Z</i>(<i>Z</i> <i>z</i>) method in this class' standard
	 * methods. <br>
	 * <br>
	 * Override this method if you want to provide a non-null default behaviour
	 * for every non-overridden process<i>Z</i> method. Unless it is overridden,
	 * it does nothing.
	 * 
	 * @param component
	 *            The PatternModel to process with a default behaviour.
	 */
	protected void defaultProcess(PatternModel model) {
	}
}