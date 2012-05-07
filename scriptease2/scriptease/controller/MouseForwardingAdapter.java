package scriptease.controller;

import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * MouseForwardingAdapter forwards mouse events on the registered component
 * until an ancestor consumes it.
 * 
 * This seems like something that swing should do regardless...
 * 
 * @author mfchurch
 * 
 */
public class MouseForwardingAdapter extends MouseAdapter {
	// cap for how many parents we check before giving up.
	private final int SAFE_LIMIT = 5;
	private static MouseForwardingAdapter instance;

	public static MouseForwardingAdapter getInstance() {
		if (instance == null)
			instance = new MouseForwardingAdapter();
		return instance;
	}

	private MouseForwardingAdapter() {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		passToParentListener(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		passToParentListener(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		passToParentListener(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		passToParentListener(e);
	}

	private void passToParentListener(MouseEvent e) {
		int i = 0;
		Container parent = e.getComponent().getParent();
		while (parent != null && !e.isConsumed() && i < SAFE_LIMIT) {
			e.setSource(parent);
			// pass the event
			parent.dispatchEvent(e);

			// check higher
			parent = parent.getParent();
			i++;
		}
	}
}
