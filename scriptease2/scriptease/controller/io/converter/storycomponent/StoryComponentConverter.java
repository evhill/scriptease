package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.XMLNode;
import scriptease.model.StoryComponent;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter superclass for all converters that convert StoryComponents.
 * Subclasses are required to implement {@link #buildComponent()}, in which they
 * build the specific instance of StoryComponent that they are responsible for
 * converting. They are also expected to call super.marshal(...) and
 * super.unmarshall(...).
 * 
 * @author remiller
 */
public abstract class StoryComponentConverter implements Converter {
	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class.
	private static final String TAG_NAME = "Name";
	private static final String TAG_LABELS = "Labels";
	private static final String TAG_LABEL = "Label";
	private static final String TAG_VISIBLE = "Visible";
	private static final String TAG_ENABLED = "Enabled";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final StoryComponent comp = (StoryComponent) source;

		// Name
		writer.startNode(TAG_NAME);
		writer.setValue(comp.getDisplayText());
		writer.endNode();

		// Visibility
		writer.startNode(TAG_VISIBLE);
		writer.setValue(comp.isVisible().toString());
		writer.endNode();
		
		// Disability
		writer.startNode(TAG_ENABLED);
		writer.setValue(comp.isEnabled().toString());
		writer.endNode();

		// Labels
		writer.startNode(TAG_LABELS);
		for (String label : comp.getLabels()) {
			writer.startNode(TAG_LABEL);
			writer.setValue(label);
			writer.endNode();
		}
		writer.endNode();
	}

	/**
	 * Unmarshalls A StoryComponent from XML. The exact StoryComponent that is
	 * unmarshalled is determined by the implementation of
	 * {@link #buildComponent(HierarchicalStreamReader, UnmarshallingContext)},
	 * which is called before any generic StoryComponent properties are read in.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryComponent comp;
		final String displayText;
		final String visibility;
		final String enabled;
		final Collection<String> labels = new ArrayList<String>();

		comp = this.buildComponent(reader, context);

		displayText = XMLNode.NAME.readString(reader);
		visibility = XMLNode.VISIBLE.readString(reader);
		enabled = XMLNode.ENABLED.readString(reader);

		// Labels
		reader.moveDown();
		if (!reader.getNodeName().equalsIgnoreCase(TAG_LABELS))
			throw new ConversionException(
					"Failed to read labels for StoryComponent with displayText ["
							+ displayText + "]");

		labels.addAll(XMLNode.LABELS
				.readStringCollection(reader, XMLNode.LABEL));

		reader.moveUp();

		// Actually init the StoryComponent.
		comp.setDisplayText(displayText);
		comp.addLabels(labels);
		comp.setVisible(visibility.equalsIgnoreCase("true"));
		comp.setEnabled(enabled.equalsIgnoreCase("true"));

		return comp;
	}

	/**
	 * Builds an instance of the specific StoryComponent subclass that the
	 * converter is intended to convert. This is used as part of the
	 * unmarshalling process, before all other StoryComponent properties have
	 * been read. The unmarshalling parameters can be used to read any
	 * information that is necessary for constructing the object.
	 * 
	 * @param reader
	 *            the reader to read from
	 * @param context
	 *            the context to read in
	 * 
	 * @return instance a StoryComponent subclass
	 * @see #unmarshal(HierarchicalStreamReader, UnmarshallingContext)
	 */
	protected abstract StoryComponent buildComponent(
			HierarchicalStreamReader reader, UnmarshallingContext context);
}
