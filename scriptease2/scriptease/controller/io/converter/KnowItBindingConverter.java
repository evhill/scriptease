package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import scriptease.controller.BindingVisitor;
import scriptease.controller.io.FileIO;
import scriptease.model.TypedComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingRunTime;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.tools.GameConstantFactory;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts any KnowItBinding to/from XML.
 * 
 * @author remiller
 * @author mfchurch
 */
public class KnowItBindingConverter implements Converter {
	private static final String TAG_VALUE = "Value";

	private static final String ATTRIBUTE_BINDING_FLAVOUR = "flavour";
	private static final String ATTRIBUTE_VALUE_CONSTANT_FLAVOUR = "constant";
	private static final String ATTRIBUTE_VALUE_GAME_OBJECT_FLAVOUR = "gameObject";
	private static final String ATTRIBUTE_VALUE_FUNCTION_FLAVOUR = "function";
	private static final String ATTRIBUTE_VALUE_REFERENCE_FLAVOUR = "reference";
	private static final String ATTRIBUTE_VALUE_RUNTIME_FLAVOUR = "runTime";
	private static final String ATTRIBUTE_VALUE_NULL_FLAVOUR = "null";
	private static final String ATTRIBUTE_VALUE_STORY_POINT_FLAVOUR = "storyPoint";

	/**
	 * Can convert any subclass of KnowItBinding
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		/*
		 * This is intentionally ignoring the possibility that a KnowItBinding
		 * subclass could indirectly implement KnowItBinding. Doing it this way
		 * avoids running up the inheritance hierarchy of every single class we
		 * want to convert, thus giving more efficient and clearer code. Feel
		 * free to change it if it causes problems.
		 * 
		 * - remiller
		 */
		return type.equals(KnowItBinding.class)
				|| type.getSuperclass().equals(KnowItBinding.class)
				|| Arrays.asList(type.getInterfaces()).contains(
						KnowItBinding.class);
	}

	// ====================== OUT ======================

	@Override
	public void marshal(final Object source,
			final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final KnowItBinding binding = (KnowItBinding) source;

		// redirect to the appropriate writing method.
		binding.process(new BindingVisitor() {
			public void processConstant(KnowItBindingConstant constant) {
				if (constant.getFirstType().equals(StoryPoint.STORY_POINT_TYPE)) {
					// deal with it B-->:)
					KnowItBindingConverter.this.marshallConstantBinding(
							constant, writer);
				} else if (constant.isIdentifiableGameConstant())
					KnowItBindingConverter.this
							.marshallIdentifiableGameConstantBinding(constant,
									writer);
				else
					KnowItBindingConverter.this.marshallConstantBinding(
							constant, writer);
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				KnowItBindingConverter.this.marshallFunctionBinding(function,
						writer, context);
			}

			@Override
			public void processReference(KnowItBindingReference reference) {
				KnowItBindingConverter.this.marshallReferenceBinding(reference,
						writer, context);
			}

			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
						ATTRIBUTE_VALUE_NULL_FLAVOUR);
			}

			@Override
			public void processRunTime(KnowItBindingRunTime runTime) {
				KnowItBindingConverter.this.marshallRunTimeBinding(runTime,
						writer);
			}

			@Override
			public void processStoryPoint(KnowItBindingStoryPoint storyPoint) {
				KnowItBindingConverter.this.marshallStoryPointBinding(
						storyPoint, writer, context);
			}
		});
	}

	/*
	 * Converts a Game Constant to XML
	 */
	private void marshallConstantBinding(KnowItBindingConstant binding,
			HierarchicalStreamWriter writer) {
		final GameConstant constant = binding.getValue();

		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_CONSTANT_FLAVOUR);

		writer.startNode(TypedComponent.TAG_TYPE);
		writer.setValue(constant.getTypes().iterator().next());
		writer.endNode();

		writer.startNode(TAG_VALUE);
		writer.setValue(constant.getCodeText());
		writer.endNode();
	}

	private void marshallRunTimeBinding(KnowItBindingRunTime binding,
			HierarchicalStreamWriter writer) {
		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_RUNTIME_FLAVOUR);

		writer.startNode(TypedComponent.TAG_TYPES);
		for (String type : binding.getTypes()) {
			writer.startNode(TypedComponent.TAG_TYPE);
			writer.setValue(type);
			writer.endNode();
		}
		writer.endNode();
	}

	/*
	 * Converts a Game Constant to XML
	 */
	private void marshallIdentifiableGameConstantBinding(
			KnowItBindingConstant binding, HierarchicalStreamWriter writer) {
		final GameConstant gameObject = binding.getValue();

		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_GAME_OBJECT_FLAVOUR);

		writer.setValue(gameObject.getTemplateID());
	}

	/*
	 * Converts a Function Reference to XML
	 */
	private void marshallFunctionBinding(KnowItBindingFunction binding,
			HierarchicalStreamWriter writer, MarshallingContext context) {
		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_FUNCTION_FLAVOUR);

		writer.startNode(ScriptItConverter.TAG_SCRIPTIT);
		context.convertAnother(binding.getValue());
		writer.endNode();
	}

	/*
	 * Converts a KnowIt Reference to XML
	 */
	private void marshallReferenceBinding(KnowItBindingReference binding,
			HierarchicalStreamWriter writer, MarshallingContext context) {
		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_REFERENCE_FLAVOUR);

		writer.startNode(KnowItConverter.TAG_KNOWIT);
		context.convertAnother(binding.getValue());
		writer.endNode();
	}

	/*
	 * Converts a Story Point reference to XML
	 */
	private void marshallStoryPointBinding(KnowItBindingStoryPoint binding,
			HierarchicalStreamWriter writer, MarshallingContext context) {
		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_STORY_POINT_FLAVOUR);

		writer.startNode(StoryPointConverter.TAG_STORYPOINT);
		context.convertAnother(binding.getValue());
		writer.endNode();
	}

	// ====================== IN ======================

	/**
	 * Unmarshals the KnowItBinding and returns it. returns null if a problem
	 * occured while marshalling.
	 */
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String flavour = reader.getAttribute(ATTRIBUTE_BINDING_FLAVOUR);
		final KnowItBinding binding;

		reader.getNodeName();

		// Let's figure out which subtype of KnowItBinding we want.
		if (flavour == null
				|| flavour.equalsIgnoreCase(ATTRIBUTE_VALUE_NULL_FLAVOUR))
			binding = null;
		else {
			reader.getNodeName();

			if (flavour.equalsIgnoreCase(ATTRIBUTE_VALUE_CONSTANT_FLAVOUR))
				binding = this.unmarshallConstantBinding(reader);
			else if (flavour.equalsIgnoreCase(ATTRIBUTE_VALUE_RUNTIME_FLAVOUR))
				binding = this.unmarshallRunTimeBinding(reader);
			else if (flavour
					.equalsIgnoreCase(ATTRIBUTE_VALUE_GAME_OBJECT_FLAVOUR))
				binding = this.unmarshallGameObjectBinding(reader);
			else if (flavour.equalsIgnoreCase(ATTRIBUTE_VALUE_FUNCTION_FLAVOUR))
				binding = this.unmarshallFunctionBinding(reader, context);
			else if (flavour
					.equalsIgnoreCase(ATTRIBUTE_VALUE_REFERENCE_FLAVOUR))
				binding = this.unmarshallReferenceBinding(reader, context);
			else if (flavour
					.equalsIgnoreCase(ATTRIBUTE_VALUE_STORY_POINT_FLAVOUR))
				binding = this.unmarshallStoryPointBinding(reader, context);
			else
				// VizziniAmazementException - remiller
				throw new ConversionException("Inconceivable binding type: "
						+ flavour);
		}

		if (binding == null)
			return new KnowItBindingNull();
		else
			return binding;
	}

	private KnowItBindingConstant unmarshallConstantBinding(
			HierarchicalStreamReader reader) {
		final GameConstant constant;
		final String value;
		final String type;

		type = FileIO.readValue(reader, TypedComponent.TAG_TYPE);
		value = FileIO.readValue(reader, TAG_VALUE);

		constant = GameConstantFactory.getInstance().getConstant(type, value);

		return new KnowItBindingConstant(constant);
	}

	private KnowItBindingRunTime unmarshallRunTimeBinding(
			HierarchicalStreamReader reader) {
		reader.moveDown();
		Collection<String> types = new ArrayList<String>();
		if (reader.getNodeName().equalsIgnoreCase(TypedComponent.TAG_TYPES)) {
			// read all of the types
			while (reader.hasMoreChildren()) {
				types.add(FileIO.readValue(reader, TypedComponent.TAG_TYPE));
			}
		}
		reader.moveUp();
		return new KnowItBindingRunTime(types);
	}

	/**
	 * Unmarshalls the GameObjectBinding and returns a KnowItBindingConstant
	 * returns null if the GameObject failed to read.
	 * 
	 * @param reader
	 * @return
	 */
	private KnowItBindingConstant unmarshallGameObjectBinding(
			HierarchicalStreamReader reader) {
		final String id;
		GameConstant gameObject = null;

		id = reader.getValue();

		// Ew. Gross. - remiller
		final GameModule currentModule = StoryModelConverter.currentModule;

		if (currentModule == null) {
			throw new IllegalStateException(
					"Cannot unmarshall a GameObject binding without a module loaded");
		}

		gameObject = currentModule.getInstanceForObjectIdentifier(id);

		if (gameObject != null) {
			return new KnowItBindingConstant(gameObject);
		} else {
			System.err.println("Binding lookup failed for id " + id
					+ ", assigning null instead.");
			return null;
		}
	}

	private KnowItBindingFunction unmarshallFunctionBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		KnowItBindingFunction binding = new KnowItBindingFunction(null);

		// move down and read as a doIt
		reader.moveDown();

		final ScriptIt scriptIt;
		final KnowItBindingFunction knowItBindingFunction;

		scriptIt = (ScriptIt) context.convertAnother(binding, ScriptIt.class);

		knowItBindingFunction = new KnowItBindingFunction(scriptIt);

		reader.moveUp();

		return knowItBindingFunction;
	}

	private KnowItBindingReference unmarshallReferenceBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		final KnowIt referent;

		KnowItBindingReference binding = new KnowItBindingReference(null);

		// move down and read as a knowIt
		reader.moveDown();

		referent = (KnowIt) context.convertAnother(binding, KnowIt.class);

		reader.moveUp();

		binding = new KnowItBindingReference(referent);

		return binding;
	}

	private KnowItBindingStoryPoint unmarshallStoryPointBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		StoryPoint storyPoint = null;
		final KnowItBindingStoryPoint binding = new KnowItBindingStoryPoint(
				storyPoint);

		// move down and read as a story point
		reader.moveDown();
		storyPoint = (StoryPoint) context.convertAnother(binding,
				StoryPoint.class);
		reader.moveUp();

		return new KnowItBindingStoryPoint(storyPoint);
	}
}
