package scriptease.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Common list operations such as identity comparisons and equality comparisons
 * 
 * @author mfchurch
 * 
 */
public class ListOp {

	/**
	 * Returns if each element in the given lists are .equal
	 * 
	 * @param <T>
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static <T> boolean equalLists(List<T> list1, List<T> list2) {
		final int size = list1.size();
		boolean equal = (size == list2.size());
		for (int i = 0; i < size; i++) {
			if (equal) {
				final T t1 = list1.get(i);
				final T t2 = list2.get(i);
				equal &= t1.equals(t2);
			} else
				break;
		}
		return equal;
	}

	/**
	 * Returns if each element in the given lists are identity equal (==)
	 * 
	 * @param <T>
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static <T> boolean identityEqualLists(List<T> list1, List<T> list2) {
		final int size = list1.size();
		boolean equal = (size == list2.size());
		for (int i = 0; i < size; i++) {
			if (equal) {
				final T t1 = list1.get(i);
				final T t2 = list2.get(i);
				equal &= (t1 == t2);
			} else
				break;
		}
		return equal;
	}

	/**
	 * Returns if the given collection contains an object .equal to the given
	 * key
	 * 
	 * @param <T>
	 * @param collection
	 * @param key
	 * @return
	 */
	public static <T> boolean equalsContains(Collection<T> collection, T key) {
		return ListOp.equalsRetrieve(collection, key) != null;
	}

	/**
	 * Returns if the given collection of lists contains a list identity equal
	 * (=) to the given list key
	 * 
	 * @param <T>
	 * @param collection
	 * @param key
	 * @return
	 */
	public static <T> boolean identityContains(Collection<List<T>> collection,
			List<T> key) {
		for (List<T> subCollection : collection) {
			if (ListOp.identityEqualLists(subCollection, key))
				return true;
		}
		return false;
	}

	/**
	 * Returns if the given collection contains an object identity equal (==) to
	 * the given key
	 * 
	 * @param <T>
	 * @param collection
	 * @param key
	 * @return
	 */
	public static <T> boolean identityContains(Collection<T> collection, T key) {
		return ListOp.identityEqualsRetrieve(collection, key) != null;
	}

	/**
	 * Returns the object in the given collection which is .equals to the key.
	 * Returns null if none are found
	 * 
	 * @param <T>
	 * @param collection
	 * @param key
	 * @return
	 */
	public static <T> T equalsRetrieve(Collection<T> collection, T key) {
		for (T object : collection)
			if (object.equals(key))
				return object;
		return null;
	}

	/**
	 * Returns the object in the given collection which is identity equal (==)
	 * to the key. Returns null if none are found
	 * 
	 * @param <T>
	 * @param collection
	 * @param key
	 * @return
	 */
	public static <T> T identityEqualsRetrieve(Collection<T> collection, T key) {
		for (T object : collection)
			if (object == key)
				return object;
		return null;
	}

	/**
	 * Creates a list containing the passed in contents.
	 * 
	 * @param contents
	 * @return
	 */
	public static <T> List<T> createList(T... contents) {
		final ArrayList<T> list = new ArrayList<T>();

		if (contents != null)
			for (T content : contents) {
				list.add(content);
			}

		return list;
	}

	/**
	 * Returns the first item in the list. If there are no items, this returns
	 * null.
	 * 
	 * @param collection
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T head(Collection<T> collection) {
		if (collection.isEmpty())
			return null;

		return (T) collection.toArray()[0];
	}

	@SuppressWarnings("unchecked")
	public static <T> Collection<T> tail(Collection<T> collection) {
		if (collection.size() < 2)
			return null;

		return (Collection<T>) createList(Arrays.copyOfRange(
				collection.toArray(), 1, collection.size()));
	}

	@SuppressWarnings("unchecked")
	public static <T> T last(Collection<T> collection) {
		if (collection.isEmpty())
			return null;

		return (T) collection.toArray()[collection.size() - 1];
	}
}
