package scriptease.model.atomic.describeits;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.model.atomic.KnowIt;

/**
 * A node for DescribeIts.
 * 
 * @author kschenk
 * 
 */
public class DescribeItNode implements Cloneable {
	private static final String NEW_DESCRIBEIT_NODE = "New Node";
	private static int describeItNodeCounter = 1;

	private KnowIt knowIt;
	private String name;

	private Set<DescribeItNode> successors;

	public DescribeItNode() {
		this("", null);
	}

	public DescribeItNode(String name) {
		this(name, null);
	}

	public DescribeItNode(String name, KnowIt knowIt) {
		super();
		this.successors = new HashSet<DescribeItNode>();

		if (name.equals("") || name == null) {
			name = NEW_DESCRIBEIT_NODE + " " + describeItNodeCounter++;
		}

		this.setKnowIt(knowIt);
		this.setName(name);

	}

	@Override
	protected DescribeItNode clone() {
		DescribeItNode clone = null;
		try {
			clone = (DescribeItNode) super.clone();

		} catch (CloneNotSupportedException e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), e);
		}

		clone.name = this.name;
		if (this.knowIt != null)
			clone.knowIt = this.knowIt.clone();
		clone.successors = new HashSet<DescribeItNode>();

		for (DescribeItNode successor : this.getSuccessors()) {
			clone.addSuccessor(successor.clone());
		}

		return clone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setKnowIt(KnowIt knowIt) {
		this.knowIt = knowIt;
	}

	public KnowIt getKnowIt() {
		return this.knowIt;
	}

	/**
	 * Gets the immediate successors of the StoryPoint.
	 * 
	 * @return
	 */
	public Collection<DescribeItNode> getSuccessors() {
		return this.successors;
	}

	/**
	 * Adds a successor to the StoryPoint.
	 * 
	 * @param successor
	 */
	public boolean addSuccessor(DescribeItNode successor) {
		if (successor != this && !successor.getSuccessors().contains(this)) {
			if (this.successors.add(successor)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds multiple successors to the StoryPoint.
	 * 
	 * @param successors
	 */
	public void addSuccessors(Collection<DescribeItNode> successors) {
		for (DescribeItNode successor : successors) {
			if (successor != this) {
				this.addSuccessor(successor);
			}
		}
	}

	/**
	 * Removes a successor from the StoryPoint.
	 * 
	 * @param successor
	 */
	public boolean removeSuccessor(DescribeItNode successor) {
		if (this.successors.remove(successor)) {
			return true;
		}
		return false;
	}

	/**
	 * Gets all descendants of the StoryPoint, including the StoryPoint itself.
	 * That is, the successors, the successors of the successors, etc.
	 * 
	 * @return
	 */
	public Set<DescribeItNode> getDescendants() {
		if (this.successors.contains(this)) {
			throw new IllegalStateException(
					"DescribeItNode contains itself as a child!");
		}

		Set<DescribeItNode> descendants;
		descendants = new HashSet<DescribeItNode>();

		descendants.add(this);
		for (DescribeItNode successor : this.successors) {
			descendants.add(successor);
			descendants.addAll(successor.getDescendants());
		}
		return descendants;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DescribeItNode) {
			final DescribeItNode compared;

			compared = (DescribeItNode) obj;

			if (this.knowIt == compared.getKnowIt() && this.knowIt != null)
				return this.knowIt.equals(compared.getKnowIt());
			else
				return this.getName().equals(compared.getName());
		}
		return false;
	}
}