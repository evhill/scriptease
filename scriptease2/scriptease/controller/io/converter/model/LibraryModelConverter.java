package scriptease.controller.io.converter.model;

import java.util.Collection;

import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.controller.io.XMLNode.XMLNodeData;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Slot;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for Library Models.
 * 
 * @author mfchurch
 * @author kschenk
 */
public class LibraryModelConverter implements Converter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final LibraryModel library = (LibraryModel) source;

		XMLAttribute.NAME.write(writer, library.getTitle());
		XMLAttribute.AUTHOR.write(writer, library.getAuthor());

		XMLNode.TYPES.writeObject(writer, context, library.getGameTypes());
		XMLNode.SLOTS.writeObject(writer, context, library.getSlots(),
				XMLAttribute.DEFAULT_FORMAT, library.getSlotDefaultFormat());

		XMLNode.CAUSES.writeObject(writer, context, library.getCausesCategory()
				.getChildren());
		XMLNode.EFFECTS.writeObject(writer, context, library
				.getEffectsCategory().getChildren());
		XMLNode.DESCRIBEITS.writeObject(writer, context,
				library.getDescribeIts());
		XMLNode.CONTROLITS.writeObject(writer, context, library
				.getControllersCategory().getChildren());
		XMLNode.TYPECONVERTERS.writeObject(writer, context, library
				.getTypeConverter().getConverterDoIts());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final LibraryModel library = new LibraryModel();

		final Collection<GameType> types;
		final XMLNodeData<Collection<Slot>> slots;
		final Collection<CauseIt> causes;
		final Collection<ScriptIt> effects;
		final Collection<DescribeIt> descriptions;
		final Collection<ControlIt> controls;
		final Collection<ScriptIt> typeConvertors;

		System.out.println("Unmarshalling Library Model");

		// Read everything from XML

		library.setTitle(XMLAttribute.NAME.read(reader));
		library.setAuthor(XMLAttribute.AUTHOR.read(reader));

		types = XMLNode.TYPES.readCollection(reader, context, GameType.class);
		slots = XMLNode.SLOTS.readAttributedCollection(reader, context,
				Slot.class, XMLAttribute.DEFAULT_FORMAT);
		causes = XMLNode.CAUSES.readCollection(reader, context, CauseIt.class);
		effects = XMLNode.EFFECTS.readCollection(reader, context,
				ScriptIt.class);
		descriptions = XMLNode.DESCRIBEITS.readCollection(reader, context,
				DescribeIt.class);
		controls = XMLNode.CONTROLITS.readCollection(reader, context,
				ControlIt.class);
		typeConvertors = XMLNode.TYPECONVERTERS.readCollection(reader, context,
				ScriptIt.class);

		// Construct the library

		library.setSlotDefaultFormat(slots
				.getAttribute(XMLAttribute.DEFAULT_FORMAT));
		library.addSlots(slots.getData());

		library.addGameTypes(types);

		library.addAll(causes);
		library.addAll(effects);

		/*
		 * We can't add this as usual since our LibraryModel is still getting
		 * created right here. The add(DescribeIt) method would thus cause a
		 * null pointer exception to be thrown. Besides, we'd be doing things
		 * twice. -kschenk
		 */
		for (DescribeIt describeIt : descriptions) {
			final KnowIt knowIt = library.createKnowItForDescribeIt(describeIt);

			library.add(knowIt);
			library.addDescribeIt(describeIt, knowIt);
		}

		library.addAll(controls);

		library.getTypeConverter().addConverterScriptIts(typeConvertors);

		return library;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(LibraryModel.class);
	}
}
