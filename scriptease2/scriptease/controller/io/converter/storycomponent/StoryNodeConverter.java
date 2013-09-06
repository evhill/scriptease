package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.model.complex.StoryNode;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts the StoryNode class to and from XML.
 * 
 * @author jyuen
 */
public abstract class StoryNodeConverter extends ComplexStoryComponentConverter {
	
	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class. 
	public static final String TAG_SUCCESSORS = "Successors";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final StoryNode storyNode = (StoryNode) source;
		super.marshal(source, writer, context);

		writer.startNode(TAG_SUCCESSORS);
		context.convertAnother(storyNode.getSuccessors());
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryNode storyNode = (StoryNode) super.unmarshal(reader,
				context);
		
		final Set<StoryNode> successors = new HashSet<StoryNode>();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(TAG_SUCCESSORS)) {
				successors.addAll((Collection<StoryNode>) context
						.convertAnother(storyNode, ArrayList.class));
			}
			reader.moveUp();
		}

		storyNode.addSuccessors(successors);

		return storyNode;
	}
}
