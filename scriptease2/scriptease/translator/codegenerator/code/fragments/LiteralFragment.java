package scriptease.translator.codegenerator.code.fragments;

import scriptease.controller.FragmentVisitor;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * Represents a code location where a text string is to be inserted verbatim
 * from the format dictionary.
 * 
 * @author remiller
 */
public class LiteralFragment extends AbstractFragment {
	/**
	 * See:
	 * {@link AbstractFragment#FormatFragment(String, CharacterRange, char[])}
	 * 
	 * @param text
	 *            The specific literal.
	 */
	public LiteralFragment(String text) {
		super(text);
	}

	/**
	 * Gets the literal text. The context is unimportant and can be
	 * <code>null</code>.
	 */
	@Override
	public String resolve(Context context) {
		return this.getDirectiveText();
	}

	@Override
	public String toString() {
		return "\"" + this.getDirectiveText() + "\"";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LiteralFragment)
			return this.hashCode() == obj.hashCode();
		return false;
	}

	@Override
	public void process(FragmentVisitor visitor) {
		visitor.processLiteralFragment(this);
	}
}
