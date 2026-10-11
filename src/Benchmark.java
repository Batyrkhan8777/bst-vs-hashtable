import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Measures insert / search / delete time of BST and HashTable
 * for n = 100, 1000, 10000 (random and sorted input).
 * Times are the total for n operations, in milliseconds (median of REPEATS runs).
 */
public class Benchmark {

    static final int[] SIZES = {100, 1_000, 10_000};
    static final int REPEATS = 7;
    static final int WARMUP_RUNS = 20;       // lets the JIT compiler optimise before we measure

    static double[] runOnce(Supplier<IntMap> factory, int[] keys, int[] searchOrder, int[] deleteOrder) {
        IntMap map = factory.get();
        long sink = 0;                       // use results so the JIT cannot remove the loops

        long t0 = System.nanoTime();
        for (int k : keys) map.insert(k, k);
        long t1 = System.nanoTime();
        for (int k : searchOrder) sink += map.search(k);
        long t2 = System.nanoTime();
        for (int k : deleteOrder) sink += map.delete(k) ? 1 : 0;
        long t3 = System.nanoTime();

        if (sink == 42) System.out.print("");
        return new double[]{(t1 - t0) / 1e6, (t2 - t1) / 1e6, (t3 - t2) / 1e6};
    }

    static double median(double[] values) {
        double[] copy = values.clone();
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    static int[] shuffled(int[] keys, Random rnd) {
        List<Integer> list = new ArrayList<>();
        for (int k : keys) list.add(k);
        Collections.shuffle(list, rnd);
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    public static void main(String[] args) throws IOException {
        Random rnd = new Random(42);

        // --- warm-up (results are thrown away) ---
        int[] warmKeys = new int[2000];
        for (int i = 0; i < warmKeys.length; i++) warmKeys[i] = i * 3;
        for (int i = 0; i < WARMUP_RUNS; i++) {
            runOnce(BST::new, warmKeys, shuffled(warmKeys, rnd), shuffled(warmKeys, rnd));
            runOnce(HashTable::new, warmKeys, shuffled(warmKeys, rnd), shuffled(warmKeys, rnd));
        }

        String[] names = {"BST", "HashTable", "HashTable (bad hash, no resize)"};
        List<Supplier<IntMap>> factories = List.of(
                BST::new,
                HashTable::new,
                () -> new HashTable(8, k -> 0, false)      // every key -> bucket 0
        );

        Files.createDirectories(Path.of("results"));
        try (PrintWriter csv = new PrintWriter("results/results.csv")) {
            csv.println("structure,input_order,n,insert_ms,search_ms,delete_ms");

            for (int n : SIZES) {
                // n unique random keys
                List<Integer> pool = new ArrayList<>();
                for (int i = 0; i < n * 10; i++) pool.add(i);
                Collections.shuffle(pool, rnd);
                int[] randomKeys = pool.subList(0, n).stream().mapToInt(Integer::intValue).toArray();
                int[] sortedKeys = randomKeys.clone();
                Arrays.sort(sortedKeys);

                String[] orderNames = {"random", "sorted"};
                int[][] orders = {randomKeys, sortedKeys};

                for (int o = 0; o < 2; o++) {
                    int[] keys = orders[o];
                    int[] searchOrder = shuffled(keys, rnd);
                    int[] deleteOrder = shuffled(keys, rnd);

                    for (int s = 0; s < names.length; s++) {
                        double[] ins = new double[REPEATS];
                        double[] sea = new double[REPEATS];
                        double[] del = new double[REPEATS];
                        for (int r = 0; r < REPEATS; r++) {
                            double[] t = runOnce(factories.get(s), keys, searchOrder, deleteOrder);
                            ins[r] = t[0];
                            sea[r] = t[1];
                            del[r] = t[2];
                        }
                        double mi = median(ins), ms = median(sea), md = median(del);
                        String line = String.format(java.util.Locale.US, "%s,%s,%d,%.3f,%.3f,%.3f",
                                names[s], orderNames[o], n, mi, ms, md);
                        csv.println(line);
                        System.out.println(line);
                    }
                }

                // tree height: random vs sorted input
                BST r = new BST(), sTree = new BST();
                for (int k : randomKeys) r.insert(k, k);
                for (int k : sortedKeys) sTree.insert(k, k);
                System.out.println("n=" + n + " BST height: random=" + r.height() + ", sorted=" + sTree.height());
            }
        }
        System.out.println("Saved results/results.csv");
    }
}
