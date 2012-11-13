package io.genericfileformat;

import java.util.ArrayList;
import java.util.List;

/**
 * We never want more than one Journal GFF in our erf file, so this is a
 * singleton class. This is its own class due to specialist methods that we do
 * NOT want inside of a regular GFF.
 * 
 * @author kschenk
 * 
 */
public class GeneratedJournalGFF extends GenericFileFormat {
	private static final String RESREF = "se_genjournal";

	private static final GeneratedJournalGFF instance = new GeneratedJournalGFF();

	private final GffField nameField;
	private final GffField tagField;

	/**
	 * Returns the sole instance of GeneratedJournalGFF.
	 * 
	 * @return
	 */
	public static GeneratedJournalGFF getInstance() {
		return instance;
	}

	/**
	 * Set the name of the Journal category.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.nameField.setData(name);
	}

	/**
	 * Set the tag of the journal category.
	 * 
	 * @param tag
	 */
	public void setTag(String tag) {
		this.tagField.setData(tag);
	}

	private GeneratedJournalGFF() {
		super(RESREF, TYPE_JOURNAL_BP + " ");

		final GffField commentField;

		commentField = new GffField(10, 5, 20);

		// Add labels
		this.labelArray.add("Categories");

		// TODO Anything other than "categories" should be separated, so we can
		// create individual ones. We can have many categories and many entries
		// per category...

		this.labelArray.add("Name");
		this.labelArray.add("XP");
		this.labelArray.add("Priority");
		this.labelArray.add("Picture");
		this.labelArray.add("Comment");
		this.labelArray.add("Tag");
		this.labelArray.add("EntryList");
		this.labelArray.add("ID");
		this.labelArray.add("End");
		this.labelArray.add("Text");

		// Add the three structs
		// Top Level Struct
		this.structArray.add(new GffStruct(-1, 0, 1));
		// JournalCategory struct
		this.structArray.add(new GffStruct(0, 0, 7));
		// JournalEntry struct
		this.structArray.add(new GffStruct(0, 28, 3));

		// Categories Field:
		this.fieldArray.add(new GffField(15, 0, 0));

		// Name Field:
		this.nameField = new GffField(12, 1, 0);
		this.nameField.setBlankCExoLocString();

		this.fieldArray.add(this.nameField);

		// XP Field:
		this.fieldArray.add(new GffField(4, 2, 0));

		// Priority Field:
		this.fieldArray.add(new GffField(4, 3, 4));

		// Picture Field:
		this.fieldArray.add(new GffField(2, 4, 65535));

		// Comment Field:
		this.fieldArray.add(commentField);

		// Tag Field:
		this.tagField = new GffField(10, 6, 24);
		this.fieldArray.add(tagField);

		// EntryList field:
		this.fieldArray.add(new GffField(15, 7, 8));

		/*
		 * Entries
		 * 
		 * Note: we only have one entry per category since we're using custom
		 * tags to update entries instead of the built-in journal system.
		 */

		// ID Field:
		this.fieldArray.add(new GffField(4, 8, 1));

		// End field:
		this.fieldArray.add(new GffField(2, 9, 0));

		// Text field:
		final GffField textField;

		// This is a CExoLoc field.
		textField = new GffField(12, 10, 28);
		textField.setBlankCExoLocString();

		this.fieldArray.add(textField);

		// TODO When we move out categories, we need to set data to
		// "<CUSTOM" + categoryCount + ">" instead
		// TODO Will have to change Tag data to category

		// There is nothing wrong with regenerating these btw

		textField.setData("<CUSTOM10>");
		commentField.setData("Journal generated by ScriptEase 2. Do not "
				+ "touch if you don't want to cause major issues.");
		this.tagField.setData("se_category1");
		this.nameField.setData("Category Test");

		for (long i = 1; i <= 10; i++)
			this.fieldIndicesArray.add(i);

		for (long i = 0; i <= 1; i++) {
			final List<Long> longList;

			longList = new ArrayList<Long>();

			longList.add(i + 1);

			this.listIndicesArray.add(longList);
		}
	}
}
