package scriptease.translator.codegenerator.code.fragments;

import java.util.Collection;

import scriptease.controller.FragmentVisitor;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.container.SeriesFragment;

/**
 * Special code generation token that abstractly represents a piece of the code
 * grammar. This can be either a token where the code generator must take
 * special action or generate code, or it can be a literal string as expected by
 * the language. where a special action be taken. There are three types of
 * FormatFragments:
 * <ol>
 * <li>{@link SimpleDataFragment}s directives indicate that a single special
 * code token must be generated by the Code Generator.</li>
 * <li>{@link SeriesFragment}s indicate that a sequence of related code symbols
 * are to be generated. Parameter lists are an example.</li>
 * <li>{@link LiteralFragment}s are simply a text string to be inserted into
 * code verbatim from the format dictionary.</li>
 * </ol>
 * 
 * To get the final code for this fragment, use
 * {@link #resolve(CodeGenerationContext)} , passing in the StoryComponent that
 * is related to the token in code.<br>
 * <br>
 * 
 * @author remiller
 */
public abstract class AbstractFragment implements Cloneable {
	private String directive;
	private Context context;

	/**
	 * Builds a new Fragment with the given directive label.
	 * 
	 * @param text
	 *            The specific directive text to be used in
	 *            {@link #resolve(CodeGenerationContext)}.
	 */
	public AbstractFragment(String text) {
		this.directive = text;
	}

	protected void init() {
		this.directive = "";
		this.context = null;
	}

	@Override
	public AbstractFragment clone() {
		AbstractFragment clone = null;
		try {
			clone = (AbstractFragment) super.clone();
		} catch (CloneNotSupportedException e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), e);
		}

		clone.init();
		clone.setDirectiveText(this.directive);
		clone.setContext(this.context);

		return clone;
	}

	/**
	 * Resolves the directive as determined from the data contained in
	 * StoryComponent.
	 * 
	 * @param context
	 *            The context that will be used to resolve the directive in a
	 *            code-safe way. Must not be <code>null</code>
	 * @return The code text that replaces this code fragment.
	 */
	public String resolve(Context context) {
		// Set the global context, to be used by strategy pattern in
		// FragmentDataBuilders
		setContext(context);
		return "";
	}

	private void setContext(Context context) {
		this.context = context;
	}

	/**
	 * Gets the code generation directive to be used in resolving this fragment.
	 * 
	 * @return the directive assigned to this fragment
	 */
	public final String getDirectiveText() {
		return this.directive;
	}

	public void setDirectiveText(String text) {
		this.directive = text;
	}

	/**
	 * Resolves all of the tokens in the given format
	 * 
	 * @param format
	 *            the format to resolve
	 * @param context
	 *            the context within which the format is resolved.
	 * @return the code as resolved by resolving the format tokens in the
	 *         context.
	 */
	public static String resolveFormat(Collection<AbstractFragment> format,
			Context context) {
		final StringBuilder code = new StringBuilder();

		for (AbstractFragment token : format) {
			code.append(token.resolve(context));
		}

		return code.toString();
	}

	public Context getContext() {
		return this.context;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof AbstractFragment) {
			return this.hashCode() == other.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.directive.hashCode();
	}

	/**
	 * This is a double-dispatch hook for the
	 * {@link scriptease.controller.FragmentVisitor} family of
	 * controllers. <code>visitor</code> implements each of: process[X] where
	 * [X] is each of the leaf members of the <code>AbstractFragment</code>
	 * family. <BR>
	 * <BR>
	 * To Use: Pass in a valid AbstractFragmentVisitor to this method. The
	 * implementing atom of this method will dispatch the appropriate
	 * <code>AbstractFragmentVisitor</code> method for the atom's type. Voila!
	 * Double dispatch!
	 * 
	 * @param visitor
	 *            The <code>AbstractFragmentVisitor</code> that will process
	 *            this AbstractFragment.
	 */
	public abstract void process(FragmentVisitor visitor);
}
