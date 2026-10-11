import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/** Simple correctness tests (no JUnit needed). Run main() - it throws if something is wrong. */
public class Tests {

    static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    static void testStructure(String name, Supplier<IntMap> factory) {
        Random rnd = new Random(1);
        IntMap map = factory.get();

        List<Integer> keys = new ArrayList<>();
        for (int i = 0; i < 500; i++) keys.add(i * 7);      // 500 unique keys
        Collections.shuffle(keys, rnd);

        for (int k : keys) map.insert(k, k * 2);
        check(map.size() == 500, name + ": size after inserts");
        for (int k : keys) check(map.search(k) == k * 2, name + ": search existing");
        check(map.search(-1) == null, name + ": search missing");

        map.insert(keys.get(0), 999);                        // update, not duplicate
        check(map.search(keys.get(0)) == 999, name + ": update value");
        check(map.size() == 500, name + ": size after update");

        for (int i = 0; i < 250; i++) check(map.delete(keys.get(i)), name + ": delete existing");
        check(!map.delete(-1), name + ": delete missing");
        check(map.size() == 250, name + ": size after deletes");
        for (int i = 0; i < 250; i++) check(map.search(keys.get(i)) == null, name + ": deleted gone");
        for (int i = 250; i < 500; i++) check(map.search(keys.get(i)) != null, name + ": rest present");

        System.out.println(name + ": all tests passed");
    }

    public static void main(String[] args) {
        testStructure("BST", BST::new);
        testStructure("HashTable", HashTable::new);
    }
}
