package scriptease.translator.codegenerator.code.contexts;

import scriptease.gui.quests.QuestNode;
import scriptease.model.complex.StoryItemSequence;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;

/**
 * StoryItemSequenceContext is Context for a StoryItemSequence object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class StoryItemSequenceContext extends ComplexStoryComponentContext {

	public StoryItemSequenceContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInformation) {
		super(model, indent, existingNames, translator, locationInformation);
	}

	public StoryItemSequenceContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public StoryItemSequenceContext(Context other, StoryItemSequence source) {
		this(other);
		component = source;
	}
}
