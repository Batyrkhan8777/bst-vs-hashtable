/** Common interface so the benchmark can treat both structures the same way. */
public interface IntMap {
    void insert(int key, int value);

    /** Returns the stored value, or null if the key is not present. */
    Integer search(int key);

    /** Returns true if the key was found and removed. */
    boolean delete(int key);

    int size();
}
