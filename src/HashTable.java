import java.util.function.IntUnaryOperator;

/**
 * Hash table with separate chaining (linked list in every bucket)
 * and dynamic resizing (capacity doubles when load factor > 0.75).
 */
public class HashTable implements IntMap {

    private static class Entry {
        int key;
        int value;
        Entry next;

        Entry(int key, int value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private static final double MAX_LOAD = 0.75;

    private Entry[] buckets;
    private int size;
    private final IntUnaryOperator hashFn;
    private final boolean resizeEnabled;

    /** Normal table: good hash function + resizing. */
    public HashTable() {
        this(8, HashTable::multiplicativeHash, true);
    }

    public HashTable(int capacity, IntUnaryOperator hashFn, boolean resizeEnabled) {
        this.buckets = new Entry[capacity];
        this.hashFn = hashFn;
        this.resizeEnabled = resizeEnabled;
    }

    /** Multiplicative hashing (Knuth) with an extra bit-mix to spread keys. */
    public static int multiplicativeHash(int key) {
        int h = key * 0x9E3779B9;
        return h ^ (h >>> 16);
    }

    private int index(int key, int capacity) {
        return (hashFn.applyAsInt(key) & 0x7fffffff) % capacity;
    }

    @Override
    public void insert(int key, int value) {
        int i = index(key, buckets.length);
        for (Entry e = buckets[i]; e != null; e = e.next) {
            if (e.key == key) {            // key already exists: update value
                e.value = value;
                return;
            }
        }
        buckets[i] = new Entry(key, value, buckets[i]);   // add at head of chain
        size++;
        if (resizeEnabled && size > MAX_LOAD * buckets.length) {
            resize();
        }
    }

    @Override
    public Integer search(int key) {
        for (Entry e = buckets[index(key, buckets.length)]; e != null; e = e.next) {
            if (e.key == key) {
                return e.value;
            }
        }
        return null;
    }

    @Override
    public boolean delete(int key) {
        int i = index(key, buckets.length);
        Entry prev = null;
        for (Entry e = buckets[i]; e != null; prev = e, e = e.next) {
            if (e.key == key) {
                if (prev == null) {
                    buckets[i] = e.next;
                } else {
                    prev.next = e.next;
                }
                size--;
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    private void resize() {
        Entry[] old = buckets;
        Entry[] bigger = new Entry[old.length * 2];
        for (Entry head : old) {
            for (Entry e = head; e != null; ) {
                Entry next = e.next;
                int i = index(e.key, bigger.length);
                e.next = bigger[i];
                bigger[i] = e;
                e = next;
            }
        }
        buckets = bigger;
    }
}
