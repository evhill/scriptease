package scriptease.controller.io.converter.fragment;

import java.util.ArrayList;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.LineFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class LineFragmentConverter implements Converter {

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final LineFragment line = (LineFragment) source;

		// Write Sub Fragments
		context.convertAnother(line.getSubFragments());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final List<AbstractFragment> subFragments = new ArrayList<AbstractFragment>();

		// Read Sub Fragments
		if (reader.hasMoreChildren()) {
			subFragments.addAll((List<AbstractFragment>) context
					.convertAnother(null, ArrayList.class));
		}

		return new LineFragment(subFragments);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(LineFragment.class);
	}
}
