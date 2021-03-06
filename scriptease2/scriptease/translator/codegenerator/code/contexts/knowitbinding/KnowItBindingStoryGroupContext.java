package scriptease.translator.codegenerator.code.contexts.knowitbinding;

import java.util.Collection;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryGroup;
import scriptease.model.complex.StoryGroup;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Code generation Context for a KnowItBindingStoryGroup object.
 * 
 * @see Context
 * @see KnowItBindingContext
 * @author jyuen
 */
public class KnowItBindingStoryGroupContext extends KnowItBindingContext {

	public KnowItBindingStoryGroupContext(Context other, KnowItBinding source) {
		super(other, source);
	}

	@Override
	public String getFormattedValue() {
		final Collection<AbstractFragment> typeFormat;

		typeFormat = this.getTranslator().getLibrary()
				.getType(StoryGroup.STORY_GROUP_TYPE).getFormat();

		if (typeFormat == null || typeFormat.isEmpty())
			return this.getValue();

		return AbstractFragment.resolveFormat(typeFormat, this);
	}

	/**
	 * Get the KnowItBinding's StoryPoint Name
	 */
	@Override
	public String getValue() {
		final StoryGroup group = ((KnowItBindingStoryGroup) this.binding)
				.getValue();

		final Context knowItContext;

		knowItContext = ContextFactory.getInstance().createContext(this, group);

		return knowItContext.getName();
	}
}