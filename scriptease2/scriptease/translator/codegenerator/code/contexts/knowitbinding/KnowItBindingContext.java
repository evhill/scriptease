package scriptease.translator.codegenerator.code.contexts.knowitbinding;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * KnowItBindingContext is Context for a KnowItBinding object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingContext extends Context {
	protected KnowItBinding binding;

	public KnowItBindingContext(Context other, KnowItBinding source) {
		super(other);
		this.binding = source;
	}

	/**
	 * Get the KnowItBinding's Type
	 */
	@Override
	public String getType() {
		return this.getModel().getType(this.binding.getFirstType())
				.getCodeSymbol();
	}

	@Override
	public String getValue() {
		return this.binding.getScriptValue();
	}

	@Override
	public String getFormattedValue() {
		return this.getValue();
	}
}
