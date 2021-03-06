package scriptease.controller.io.converter.storycomponent.behaviour;

import scriptease.controller.io.XMLAttribute;
import scriptease.model.StoryComponent;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.semodel.librarymodel.LibraryModel;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts the CollaborativeTask {@link CollaborativeTask} to and from xml.
 * 
 * @author jyuen
 */
public class CollaborativeTaskConverter extends TaskConverter {
	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final CollaborativeTask task = (CollaborativeTask) source;

		XMLAttribute.INITIATE.write(writer, task.getInitiatorName());
		XMLAttribute.RESPOND.write(writer, task.getResponderName());

		super.marshal(source, writer, context);

	}

	@Override
	public CollaborativeTask unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {

		final String initiatorName = XMLAttribute.INITIATE.read(reader);
		final String responderName = XMLAttribute.RESPOND.read(reader);

		final CollaborativeTask collaborativeTask = (CollaborativeTask) super
				.unmarshal(reader, context);

		collaborativeTask.setInitiatorName(initiatorName);
		collaborativeTask.setResponderName(responderName);

		return collaborativeTask;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(CollaborativeTask.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context, LibraryModel library) {
		final CollaborativeTask task = new CollaborativeTask(library);
		// remove the default generated initiator and responder containers.
		task.removeStoryChild(task.getInitiatorContainer());
		task.removeStoryChild(task.getResponderContainer());
		return task;
	}
}
