package scriptease.gui.SEGraph;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.SEGraphToolBarObserver;
import scriptease.util.FileOp;
import scriptease.util.GUIOp;

/**
 * Builds a ToolBar to edit graphs with. Includes buttons for selecting nodes,
 * adding and deleting nodes, and adding and deleting paths. The ToolBar buttons
 * only set the mode; the graph itself contains the specific actions that should
 * happen.
 * 
 * @author kschenk
 * @author jyuen
 * 
 */
@SuppressWarnings("serial")
public class SEGraphToolBar extends JToolBar {
	private final ObserverManager<SEGraphToolBarObserver> observers;

	private final JToggleButton selectButton;
	private final JToggleButton insertButton;
	private final JToggleButton deleteButton;
	private final JToggleButton connectButton;
	private final JToggleButton disconnectButton;
	private final JToggleButton groupButton;
	private final JToggleButton ungroupButton;

	private Mode mode;

	/**
	 * The current mode of the toolbar.
	 * 
	 * @author kschenk
	 * @author jyuen
	 * 
	 */
	public static enum Mode {
		SELECT, INSERT, DELETE, CONNECT, DISCONNECT, GROUP, UNGROUP;

		private static final String CURSOR_DIRECTORY = "scriptease/resources/icons/buttonicons/";
		private static final String CURSOR_EXTENSION = ".png";

		private String iconName() {
			return this.name().toLowerCase();
		}

		/**
		 * Returns the cursor associated with the mode.
		 * 
		 * @param mode
		 * @return
		 */
		public Cursor getCursor() {
			return GUIOp.createCursor(this.iconName());
		}

		/**
		 * Loads the image for the toolbar button from the path:
		 * "scriptease/resources/icons/buttonicons/actionName.png", where
		 * actionName refers to the name of the icon.
		 * 
		 * All images loaded in this way must be .png files, and must be located
		 * in the buttonicons folder.
		 * 
		 * @param iconName
		 *            The name of the image file being loaded, without the .png
		 *            extension.
		 * 
		 * @return An {@link ImageIcon} for the loaded image, or null if image
		 *         cannot be loaded.
		 */
		public ImageIcon getIcon() {
			try {
				final BufferedImage buttonImage;

				buttonImage = ImageIO.read(FileOp
						.getFileResource(CURSOR_DIRECTORY + this.iconName()
								+ CURSOR_EXTENSION));

				return new ImageIcon(buttonImage);
			} catch (IOException e) {
				UncaughtExceptionHandler handler = Thread
						.getDefaultUncaughtExceptionHandler();
				handler.uncaughtException(Thread.currentThread(),
						new IllegalStateException("Exception " + e
								+ "while adding the icon for "
								+ "ToolBarButton " + this.iconName()));
				return null;
			}
		}
	}

	public SEGraphToolBar() {
		super();
		this.observers = new ObserverManager<SEGraphToolBarObserver>();

		final ButtonGroup buttonGroup = new ButtonGroup();

		this.selectButton = this.buildToggleButton(Mode.SELECT);
		this.insertButton = this.buildToggleButton(Mode.INSERT);
		this.deleteButton = this.buildToggleButton(Mode.DELETE);
		this.connectButton = this.buildToggleButton(Mode.CONNECT);
		this.disconnectButton = this.buildToggleButton(Mode.DISCONNECT);
		this.groupButton = this.buildToggleButton(Mode.GROUP);
		this.ungroupButton = this.buildToggleButton(Mode.UNGROUP);

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setRollover(true);
		this.setFloatable(false);
		this.setBackground(Color.WHITE);

		buttonGroup.add(this.selectButton);
		buttonGroup.add(this.insertButton);
		buttonGroup.add(this.deleteButton);
		buttonGroup.add(this.connectButton);
		buttonGroup.add(this.disconnectButton);
		buttonGroup.add(this.groupButton);
		buttonGroup.add(this.ungroupButton);

		// Sorry about the bizarre loop, but that's the way these work :(
		for (final Enumeration<AbstractButton> buttons = buttonGroup
				.getElements(); buttons.hasMoreElements();) {
			this.add(buttons.nextElement());
		}

		this.setMode(Mode.SELECT);
	}

	/**
	 * Builds a {@link JToggleButton} based on the passed in mode.
	 * 
	 * @param mode
	 * @return
	 */
	private JToggleButton buildToggleButton(final Mode mode) {
		final JToggleButton button = new JToggleButton();

		button.setIcon(mode.getIcon());
		button.setHideActionText(true);
		button.setFocusable(false);

		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SEGraphToolBar.this.setMode(mode);
			}
		});
		return button;
	}

	public void setMode(Mode mode) {
		this.mode = mode;

		final ButtonModel buttonModel;
		if (mode == Mode.SELECT) {
			buttonModel = this.selectButton.getModel();
		} else if (mode == Mode.INSERT) {
			buttonModel = this.insertButton.getModel();
		} else if (mode == Mode.DELETE) {
			buttonModel = this.deleteButton.getModel();
		} else if (mode == Mode.CONNECT) {
			buttonModel = this.connectButton.getModel();
		} else if (mode == Mode.DISCONNECT) {
			buttonModel = this.disconnectButton.getModel();
		} else if (mode == Mode.GROUP) {
			buttonModel = this.groupButton.getModel();
		} else if (mode == Mode.UNGROUP) {
			buttonModel = this.ungroupButton.getModel();
		} else {
			// Handle any strange behaviour by setting this to Select by
			// default.
			this.mode = Mode.SELECT;
			buttonModel = this.selectButton.getModel();
		}

		final ButtonGroup group = ((DefaultButtonModel) buttonModel).getGroup();

		group.setSelected(buttonModel, true);

		this.notifyModeSelection();
	}

	public Mode getMode() {
		return this.mode;
	}

	/**
	 * Adds a {@link SEGraphToolBarObserver} to the toolbar. This observer will
	 * last for the lifetime of the toolbar.
	 * 
	 * @param observer
	 */
	public void addObserver(SEGraphToolBarObserver observer) {
		this.observers.addObserver(this, observer);
	}

	private void notifyModeSelection() {
		for (SEGraphToolBarObserver observer : this.observers.getObservers()) {
			observer.modeChanged(this.mode);
		}
	}
}