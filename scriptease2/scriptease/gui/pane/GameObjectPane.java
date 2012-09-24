package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.controller.observer.library.LibraryManagerEvent;
import scriptease.controller.observer.library.LibraryManagerObserver;
import scriptease.gui.SETree.GameObjectPanelTree;
import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.LibraryManager;
import scriptease.model.PatternModelManager;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.translator.codegenerator.GameObjectPicker;

/**
 * The pane containing game objects, such as objects, sounds, etc. that are used
 * in stories.
 * 
 */
@SuppressWarnings("serial")
public class GameObjectPane extends JPanel implements GameObjectPicker,
		LibraryManagerObserver, PatternModelObserver {
	// Although the default picker will be used, a customPicker can define
	// certain behavior of the default one.
	private JTextField searchField;
	private GameObjectPanelTree tree;

	public GameObjectPane() {
		this.tree = new GameObjectPanelTree();

		// Register for tool tips
		ToolTipManager.sharedInstance().registerComponent(this.tree);

		this.tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		this.tree.setBackground(Color.WHITE);

		// Add the tree to the pane.
		JScrollPane treeScrollPane = new JScrollPane(this.tree,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		treeScrollPane.setBackground(Color.WHITE);
		treeScrollPane.getVerticalScrollBar().setUnitIncrement(16);

		// build the filter
		JComponent filterPane = this.buildFilterPane();

		this.setPreferredSize(new Dimension(this.tree.getPreferredSize().width,
				0));

		SpringLayout pickerPaneLayout = new SpringLayout();

		// Spring filterPane
		pickerPaneLayout.putConstraint(SpringLayout.WEST, filterPane, 5,
				SpringLayout.WEST, this);
		pickerPaneLayout.putConstraint(SpringLayout.NORTH, filterPane, 5,
				SpringLayout.NORTH, this);
		pickerPaneLayout.putConstraint(SpringLayout.EAST, filterPane, -5,
				SpringLayout.EAST, this);
		// Spring pickerTree
		pickerPaneLayout.putConstraint(SpringLayout.WEST, treeScrollPane, 5,
				SpringLayout.WEST, this);
		pickerPaneLayout.putConstraint(SpringLayout.EAST, treeScrollPane, -5,
				SpringLayout.EAST, this);
		pickerPaneLayout.putConstraint(SpringLayout.SOUTH, treeScrollPane, -5,
				SpringLayout.SOUTH, this);
		pickerPaneLayout.putConstraint(SpringLayout.NORTH, treeScrollPane, 5,
				SpringLayout.SOUTH, filterPane);
		this.setLayout(pickerPaneLayout);

		this.add(filterPane);
		this.add(treeScrollPane);

		LibraryManager.getInstance().addLibraryManagerObserver(this);
		PatternModelManager.getInstance().addPatternModelObserver(this);
	}

	// TODO Combine parts of this with configurePane() in LibraryPane.java.
	private JPanel buildFilterPane() {
		final JPanel filterPane;

		this.searchField = new JTextField(20);
		// TODO Search Field does zip, zilch, nada

		filterPane = new JPanel();
		filterPane.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.gray), Il8nResources
				.getString("Search_Filter_"),
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font(
						"SansSerif", Font.PLAIN, 12), Color.black));

		JComponent searchFilterPane = new JPanel();

		// Sets up the type filter.
		searchFilterPane.add(this.searchField);
		// TODO Make types work!
		searchFilterPane.add(new JButton("Types"));
		BoxLayout searchFilterPaneLayout = new BoxLayout(searchFilterPane,
				BoxLayout.X_AXIS);
		searchFilterPane.setLayout(searchFilterPaneLayout);

		// FilterPane Layout
		BoxLayout filterPaneLayout = new BoxLayout(filterPane, BoxLayout.Y_AXIS);
		filterPane.setLayout(filterPaneLayout);
		filterPane.add(searchFilterPane);
		filterPane.setMinimumSize(searchFilterPane.getPreferredSize());

		return filterPane;
	}

	public JPanel getPickerPanel() {
		return this;
	}

	public void onWidgetClicked(KnowItBindingConstant object) {
		// Not used here. The custom picker, if it exists, handles this.
	}

	@Override
	public void onWidgetHovered(BindingWidget widget) {

	}

	@Override
	public void onWidgetUnHovered() {
	}

	/**
	 * This listener checks for when the model is changed. This usually happens
	 * when you load a model, or when you switch them by switching tabs.
	 */
	@Override
	public void modelChanged(PatternModelEvent event) {
		if (event.getEventType() == PatternModelEvent.PATTERN_MODEL_ACTIVATED) {
			this.tree = new GameObjectPanelTree();
			this.buildFilterPane();
		}
	}

	/**
	 * Keep the display of the library up to date with the changes to Libraries.
	 * This listener is important for the Story Component Builder, so that
	 * changes made there will apply to the library view as well.
	 */
	@Override
	public void modelChanged(LibraryManagerEvent event) {
		if (event.getEventType() == LibraryManagerEvent.LIBRARYMODEL_CHANGED) {
			this.tree = new GameObjectPanelTree();
			this.buildFilterPane();
		}
	}
}

/**
 * A TreeCellRenderer for the Game Object panel. This renderer returns the
 * JLabel (or BindingWidget) stored in the given node (<code>value</code> ) of
 * the given <code>tree</code>. The returned JLabel is only used for drawing,
 * and does not receive events, which necessitates a custom TransferHandler for
 * the tree.
 * 
 * @author graves
 */
@SuppressWarnings("serial")
class GameObjectTreeCellRenderer extends DefaultTreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		JComponent cell = (JComponent) ((DefaultMutableTreeNode) value)
				.getUserObject();
		return cell;
	}
}