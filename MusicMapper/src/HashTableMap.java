// --== CS400 File Header Information ==--
// Name: Michael Rehani
// Email: mrrehani@wisc.edu
// Notes to Grader: <optional extra notes>

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * 
 * This class implements the hash map abstract data type.
 *
 * @param <KeyType>
 * @param <ValueType>
 */
public class HashTableMap<KeyType, ValueType> implements MapADT<KeyType, ValueType> {

	protected int capacity;
	protected int size;
	protected LinkedList<KeyValuePair<KeyType, ValueType>>[] hashMap;

	/**
	 * Constructor method
	 * 
	 * @param capacity size of the hashMap
	 */
	@SuppressWarnings("unchecked")
	public HashTableMap(int capacity) {
		this.capacity = capacity;
		size = 0;
		hashMap = (LinkedList<KeyValuePair<KeyType, ValueType>>[]) new LinkedList[this.capacity];

	}

	/**
	 * Constructor method
	 * 
	 */
	@SuppressWarnings("unchecked")
	public HashTableMap() { // default capacity = 10
		this.capacity = 10;
		size = 0;
		hashMap = (LinkedList<KeyValuePair<KeyType, ValueType>>[]) new LinkedList[10];
	}

	/**
	 * Adds an item to the hashMap
	 * 
	 * @param key   the value to calculate the hashCode with.
	 * @param value the value to store in the hashMap along with the key
	 * @return true if item was added, false otherwise.
	 */
	@Override
	public boolean put(KeyType key, ValueType value) {

		if (key == null || containsKey(key)) {
			return false;
		}

		int hashCode = Math.abs(key.hashCode()) % capacity;
		// The hashMap, when created, only has null values.
		// So when the hashCode is calculated, we need to create a linkedList for the
		// hashMap at that hashCode.
		if (hashMap[hashCode] == null)
			hashMap[hashCode] = new LinkedList<KeyValuePair<KeyType, ValueType>>();

		hashMap[hashCode].add(new KeyValuePair<KeyType, ValueType>(key, value));
		size++;

		if (((float) size / capacity) >= .85) {
			hashMap = growHashMap();
		}

		return true;

	}

	/**
	 * Helper method to grow the hashMap. Creates a new hashMap and re-hashes all
	 * the items in the old hashMap.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private LinkedList<KeyValuePair<KeyType, ValueType>>[] growHashMap() {
		int newCapacity = capacity * 2;
		LinkedList<KeyValuePair<KeyType, ValueType>>[] largerHashMap = (LinkedList<KeyValuePair<KeyType, ValueType>>[]) new LinkedList[newCapacity];
		for (int i = 0; i < capacity; i++) {
			if (hashMap[i] != null) {
				for (int n = 0; n < hashMap[i].size(); n++) {
					// In this for loop, we rehash everything at the current key into the new,
					// larger array.
					KeyType key = hashMap[i].get(n).key;
					ValueType value = hashMap[i].get(n).value;
					int hashCode = Math.abs(key.hashCode()) % newCapacity;
					if (largerHashMap[hashCode] == null)
						largerHashMap[hashCode] = new LinkedList<KeyValuePair<KeyType, ValueType>>();

					largerHashMap[hashCode].add(new KeyValuePair<KeyType, ValueType>(key, value));
				}
			}
		}
		capacity = newCapacity;
		return largerHashMap;

	}

	/**
	 * Gets the value of the hashMap at the given key
	 * 
	 * @param key the key to use to calculate the hashCode
	 * @return the value at the given key
	 * @throws NoSuchElementException if key not found in the hashMap
	 */
	@Override
	public ValueType get(KeyType key) throws NoSuchElementException {

		int hashCode = Math.abs(key.hashCode()) % capacity;
		if (!containsKey(key) || hashMap[hashCode] == null) {
			throw new NoSuchElementException("Key not in list!");
		}
		for (int i = 0; i < hashMap[hashCode].size(); i++) {
			// hashMap[hashCode] is a linkedList of KeyValuePair objects.
			// We check each KeyValuePair to see if its key is the one we're looking for.
			if (hashMap[hashCode].get(i).key.equals(key)) {
				return hashMap[hashCode].get(i).value;
			}
		}
		throw new NoSuchElementException("Key not in list!");
	}

	@Override
	public int size() {
		return size;
	}

	/**
	 * Checks if the hashMap contains a given key
	 * 
	 * @param key the key to check
	 * @return true if the hashMap contains the key, false otherwise
	 */
	@Override
	public boolean containsKey(KeyType key) {
		for (int i = 0; i < capacity; i++) { // Iterates through each item in the hashMap.
			if (hashMap[i] != null) { // If item is not null (in which case is a linked list), checks
				// each item in the linked list for the item looking for.
				for (int n = 0; n < hashMap[i].size(); n++) {
					// hashMap[hashCode] is a linkedList of KeyValuePair objects.
					// We check each KeyValuePair to see if its key is the one we're looking for.
					if (hashMap[i].get(n).key.equals(key)) {
						return true;
					}
				}

			}
		}
		return false;
	}

	/**
	 * Removes an item from the hashMap
	 * 
	 * @param key the value to calculate the hashCode with.
	 * @return value the value of the item removed if key is found, null otherwise.
	 */
	@Override
	public ValueType remove(KeyType key) {
		int hashCode = Math.abs(key.hashCode()) % capacity;
		ValueType valueRemoved = null; // This will only be changed if the key is found. Otherwise, it will remain
										// null.
		if (hashMap[hashCode] != null) {
			for (int i = 0; i < hashMap[hashCode].size(); i++) {
				if (hashMap[hashCode].get(i).key.equals(key)) {
					valueRemoved = hashMap[hashCode].remove(i).value;
					size--;
				}
			}

		}
		return valueRemoved;

	}

	/**
	 * Clears the hash map.
	 */
	@Override
	public void clear() {
		for (int i = 0; i < capacity; i++) {
			if (hashMap[i] != null) {
				hashMap[i].clear();
			}
		}
		size = 0;
	}
	
	@Override
	public String toString() {
		String toReturn = "{";
		
		for (int i = 0; i < hashMap.length; i++) {
			if (hashMap[i] != null) {
				for (int n = 0; n < hashMap[i].size(); n++) {
					System.out.println(hashMap[i].get(n).key);
					System.out.println(get(hashMap[i].get(n).key));
				}
				
			}
		}
		
		return "hi";
	}

}

/**
 * 
 * This class is used to pair the key and value of each item in the hashMap.
 *
 * @param <KeyType>
 * @param <ValueType>
 */
class KeyValuePair<KeyType, ValueType> {
	protected KeyType key;
	protected ValueType value;

	/**
	 * Constructor method
	 * 
	 * @param key
	 * @param value
	 */
	public KeyValuePair(KeyType key, ValueType value) {
		this.key = key;
		this.value = value;
	}

}
