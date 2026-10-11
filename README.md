# BST vs Hash Table: Performance Comparison (Java)

Course: Design and Analysis of Algorithms, Astana IT University (SE-2522)
Author: Batyr

Both data structures are implemented from scratch in Java (no `HashMap`, `TreeMap`, etc.).

## Repository structure

```
src/
  IntMap.java      common interface (insert, search, delete, size)
  BST.java         unbalanced binary search tree (iterative)
  HashTable.java   hash table with separate chaining + resizing
  Tests.java       correctness tests
  Benchmark.java   timing experiments (n = 100, 1,000, 10,000)
results/
  results.csv          raw measurements
  benchmark_output.txt console output of the benchmark
README.md          this report
```

## How to run

IntelliJ IDEA: open the folder, mark `src` as Sources Root, run `Tests.main()` and then `Benchmark.main()`.

Command line:

```
mkdir out
javac -d out src/*.java
java -cp out Tests
java -cp out Benchmark
```

## Implementations

**BST.** Every node stores a key, a value and left/right children; smaller keys go left, larger go right.
Insert, search and delete are iterative, so a degenerate tree (height n) does not cause a stack overflow.
Delete handles three cases: leaf, one child, two children (the node is overwritten with its in-order
successor, then the successor is removed).

**Hash table.** An array of buckets; each bucket is a singly linked list (separate chaining).
Index = `hash(key) % capacity`. The hash function is multiplicative (Knuth) with a bit-mix.
When the load factor exceeds 0.75 the capacity is doubled and all entries are rehashed.

## Complexity analysis

n = number of elements, h = height of the BST.

| Operation | BST best | BST average | BST worst | Hash table best | Hash table average | Hash table worst |
|---|---|---|---|---|---|---|
| Insert | O(1) | O(log n) | O(n) | O(1) | O(1) amortized | O(n) |
| Search | O(1) | O(log n) | O(n) | O(1) | O(1) | O(n) |
| Delete | O(1) | O(log n) | O(n) | O(1) | O(1) | O(n) |

**BST.** Cost is O(h): each step goes one level down.
- Best case O(1): the key is at the root (or the new node is attached directly to the root).
- Average O(log n): for random insertion order the height is about log n (measured: 13, 21, 29 for n = 100, 1,000, 10,000).
- Worst case O(n): sorted (or nearly sorted) input makes the tree a chain with h = n.

**Hash table.** The hash gives the bucket directly.
- Best / average O(1): short chains because the hash spreads keys evenly and the load factor is kept below 0.75.
- Worst case O(n): all keys fall into one bucket (very bad hash function or no resizing), so the operation scans one chain of length n.
- Insert is amortized O(1): one insert occasionally triggers a resize that costs O(n), but doubling means this happens rarely.

## Experiments

Setup: Java 21, `System.nanoTime()`, 20 warm-up runs for the JIT compiler (results discarded),
then the median of 7 runs. Keys are unique integers. For each n the structures were tested with
random insertion order and with already-sorted insertion order. Search and delete use a shuffled
order of the same keys (all keys exist). A third configuration, a hash table with a deliberately
bad hash function (`hash(k) = 0`) and no resizing, shows the hash table worst case.

Times in milliseconds for the whole batch of n operations:

| n | Input order | Structure | Insert | Search | Delete |
|---|---|---|---|---|---|
| 100 | random | BST | 0.024 | 0.027 | 0.023 |
| 100 | random | Hash table | 0.007 | 0.008 | 0.004 |
| 100 | sorted | BST | 0.018 | 0.020 | 0.012 |
| 100 | sorted | Hash table | 0.011 | 0.012 | 0.008 |
| 1,000 | random | BST | 0.118 | 0.089 | 0.094 |
| 1,000 | random | Hash table | 0.102 | 0.076 | 0.064 |
| 1,000 | sorted | BST | 0.921 | 1.351 | 0.640 |
| 1,000 | sorted | Hash table | 0.044 | 0.020 | 0.010 |
| 1,000 | any | Hash table (bad hash) | 0.98-1.37 | 0.92-0.93 | 0.46-0.51 |
| 10,000 | random | BST | 1.263 | 0.652 | 0.708 |
| 10,000 | random | Hash table | 0.325 | 0.110 | 0.089 |
| 10,000 | sorted | BST | 108.670 | 129.874 | 94.251 |
| 10,000 | sorted | Hash table | 0.337 | 0.099 | 0.067 |
| 10,000 | any | Hash table (bad hash) | about 90 | about 90 | about 62 |

BST height: random input gives 13 / 21 / 29 for n = 100 / 1,000 / 10,000;
sorted input gives 100 / 1,000 / 10,000 (a chain).

### Observations

1. **Random data.** Both structures are fast. At n = 10,000 the hash table is about 4x faster at insert
   and about 6-8x faster at search and delete than the BST.
2. **Sorted data.** The BST degenerates into a linked list. At n = 10,000 its operations take 94-130 ms,
   while the hash table needs under 1 ms: it is more than 300x faster. The hash table does not
   depend on the input order.
3. **Growth.** For the BST on sorted input, 10x more elements gave about 100x more time (O(n) per operation, O(n^2) per batch).
   For the hash table, 10x more elements gave roughly 10x more time (O(1) per operation).
4. **Bad hash function.** With all keys in one bucket the hash table becomes as slow as the degenerate BST
   (about 90 ms at n = 10,000). Its O(1) depends on a good hash function and resizing.
5. **Small n.** At n = 100 the times are a few microseconds, so the measurements are noisy and the
   difference is not reliable. The structures only separate clearly for larger n.

## Why performance differs in practice

- **Input order vs. hash quality.** BST speed depends on the shape of the tree, which depends on the order of
  insertion. Hash table speed depends on how evenly the hash function distributes keys.
- **Constant factors.** Hash table operations have a cost for hashing and for resizing, which matters for small n.
  A BST has no hashing but needs several comparisons and pointer jumps per operation.
- **Memory access.** BST nodes are separate objects scattered in memory; following many pointers causes cache misses.
  The bucket array of a hash table is contiguous, so reaching a bucket needs one memory access.
- **Ordering.** A BST keeps keys sorted, so it supports in-order traversal, min/max, range queries and successor.
  A hash table cannot do these efficiently.
- **Balancing.** A self-balancing tree (AVL, Red-Black) would keep O(log n) even for sorted input.

## Conclusions

- A hash table is faster for plain lookup by key, and stable regardless of input order, provided the hash function is good and the table resizes.
- An unbalanced BST works for random data but is unsafe for sorted or nearly sorted data.
- Use a hash table when only fast insert/search/delete by key is needed; use a balanced BST when sorted order, range queries, or guaranteed worst-case performance are required.

## Limitations

Measurements were made on one machine with the JVM, so absolute times vary between runs and computers.
The important result is the trend (growth rate and the collapse of the BST on sorted input).
