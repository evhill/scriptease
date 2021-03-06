package scriptease.model.atomic.knowitbindings;

import java.util.Collection;

import scriptease.controller.BindingVisitor;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;

/**
 * This class represents a <b>Resource</b> binding for a <code>KnowIt</code>.
 * 
 * @author graves
 * @see {@link scriptease.model.atomic.KnowIt}
 */
public class KnowItBindingResource extends KnowItBinding {
	private final Resource resource;
	private String originalParameterText;
	
	public KnowItBindingResource(Resource resource) {
		if (resource == null)
			throw new IllegalStateException(
					"GameConstant's cannot be set to null in a KnowItBindingConstant.");
		this.resource = resource;
		this.originalParameterText = resource.getName();
	}

	@Override
	public String getScriptValue() {
		return this.resource.getCodeText();
	}

	public String getName() {
		return this.resource.getName();
	}
	
	public String getOriginalParameterText(){
		return this.originalParameterText;
	}
	
	public void setOriginalParameterText(String text){
		this.originalParameterText = text;
	}

	public String getTag() {
		return this.resource.getTag();
	}

	public String getTemplateID() {
		return this.resource.getTemplateID();
	}

	@Override
	public Resource getValue() {
		return this.resource;
	}

	@Override
	public Collection<String> getTypes() {
		return this.resource.getTypes();
	}

	@Override
	public String toString() {
		if (this.isIdentifiableGameConstant())
			return this.getValue().getTemplateID();
		return this.getValue().getTypes().iterator().next();
	}

	@Override
	public boolean compatibleWith(KnowIt knowIt) {
		if (typeMatches(knowIt.getAcceptableTypes())) {
			if (knowIt.getOwner() != null
					&& !(this.getValue() instanceof SimpleResource)) {
				final SEModel model;

				model = SEModelManager.getInstance().getActiveModel();
				if (model instanceof StoryModel) {
					final Resource resource = this.getValue();

					final StoryModel storyModel = (StoryModel) model;
					final GameModule module = storyModel.getModule();
					final Collection<DialogueLine> dialogueRoots;

					dialogueRoots = storyModel.getDialogueRoots();

					if (resource.getTypes().contains(module.getDialogueType())
							&& dialogueRoots.contains(resource))
						return true;
					else if (resource.getTypes().contains(
							module.getDialogueLineType()))
						for (DialogueLine line : storyModel.getDialogueRoots()) {
							if (line.getDescendants().contains(resource))
								return true;
						}

					final String templateID = resource.getTemplateID();

					final Resource res = module
							.getInstanceForObjectIdentifier(templateID);

					if (res != null)
						return true;
				} else if (model == null) {
					return true;
				}
			} else
				return true;
		}
		return false;
	}

	/**
	 * Determines if this binding is wrapping a Game Object (<code>true</code>)
	 * or a Simple Game Constant (<code>false</code>).
	 * 
	 * @return <code>true</code> if the binding represents a Game Object,
	 *         <code>false</code> otherwise.
	 */
	public boolean isIdentifiableGameConstant() {
		return !(this.resource instanceof SimpleResource);
	}

	@Override
	public boolean equals(Object other) {
		if (this.resource.getCodeText() != null)
			return (other instanceof KnowItBindingResource)
					&& ((KnowItBindingResource) other).resource
							.equals(this.resource);
		return false;
	}

	@Override
	public KnowItBinding resolveBinding() {
		return this;
	}

	/**
	 * No need to clone constants, that is why they are constant.
	 * 
	 */
	@Override
	public KnowItBinding clone() {
		return new KnowItBindingResource(this.resource);
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processResource(this);
	}
}
