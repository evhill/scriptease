package scriptease.model;

import scriptease.controller.ModelVisitor;
import scriptease.translator.Translator;

/**
 * Abstract tree model of related patterns, either for a Story or a Library. A
 * Pattern Model has a <i>name</i>, an <i>author</i>, and a <i>root/i> node for
 * the tree of patterns that it contains.
 * 
 * @author remiller
 * @author kschenk
 */
public abstract class SEModel {
	private String title;
	private String author;

	public SEModel() {
		this("", "");
	}

	public SEModel(String title) {
		this(title, "");
	}

	public SEModel(String title, String author) {
		this.title = title;
		this.author = author;
	}

	/**
	 * @return The title of the StoryModel
	 */
	public final String getTitle() {
		return this.title;
	}

	/**
	 * @param title
	 *            The StoryModel's new title. Cannot be null.
	 * @throws IllegalArgumentException
	 *             if <code>title</code> is null.
	 */
	public final void setTitle(String title) {
		if (this.title == null)
			throw new IllegalArgumentException(
					"Cannot give a StoryModel a null name.");

		this.title = title;
	}

	/**
	 * @param author
	 *            The StoryModel's new author. Cannot be null.
	 * @throws IllegalArgumentException
	 *             if <code>author</code> is null.
	 */
	public final void setAuthor(String author) {
		if (this.author == null)
			throw new IllegalArgumentException(
					"Cannot give a PatternModel a null author.");

		this.author = author;
	}

	/**
	 * @return The author of the StoryModel
	 */
	public final String getAuthor() {
		return this.author;
	}

	/**
	 * Gets the translator to be used to translate this pattern model.
	 * 
	 * @return the translator
	 */
	public abstract Translator getTranslator();

	@Override
	public String toString() {
		return this.getTitle();
	}

	/**
	 * This is a double-dispatch hook for the
	 * {@link scriptease.controller.ModelVisitor} family of controllers.
	 * <code>visitor</code> implements each of: process[X] where [X] is each of
	 * the leaf members of the <code>PatternModel</code> family. <BR>
	 * <BR>
	 * To Use: Pass in a valid ModelVisitor to this method. The implementing
	 * atom of this method will dispatch the appropriate
	 * <code>ModelVisitor</code> method for the atom's type. Voila! Double
	 * dispatch! :-)
	 * 
	 * @param visitor
	 *            The <code>ModelVisitor</code> that will process this
	 *            PatternModel.
	 */
	public abstract void process(ModelVisitor visitor);
}
