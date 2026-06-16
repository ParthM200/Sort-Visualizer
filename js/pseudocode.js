// Pseudocode lines per algorithm.
// Line index in these arrays must match the `line` field yielded by each generator.
const PSEUDOCODE = {
  bubble: [
    "for i = 0 to n−2",
    "  for j = 0 to n−i−2",
    "    compare arr[j], arr[j+1]",
    "      swap(arr[j], arr[j+1])",
    "  mark arr[n−1−i] as sorted",
  ],
  insertion: [
    "for i = 1 to n−1",
    "  key ← arr[i]",
    "  j ← i − 1",
    "  while j ≥ 0 and arr[j] > key",
    "    arr[j+1] ← arr[j];  j−−",
    "  arr[j+1] ← key  (sorted)",
  ],
  selection: [
    "for i = 0 to n−1",
    "  minIdx ← i",
    "  for j = i+1 to n−1",
    "    if arr[j] < arr[minIdx]",
    "      minIdx ← j",
    "  swap(arr[i], arr[minIdx])",
  ],
  quick: [
    "quickSort(arr, lo, hi)",
    "  pivot ← arr[hi]",
    "  i ← lo − 1",
    "  for j = lo to hi−1",
    "    if arr[j] ≤ pivot",
    "      i++;  swap(arr[i], arr[j])",
    "  swap(arr[i+1], arr[hi])  ← pivot in place",
    "  recurse on sub-partitions",
  ],
  merge: [
    "mergeSort(arr, l, r)",
    "  mid ← ⌊(l + r) / 2⌋",
    "  mergeSort(arr, l, mid)",
    "  mergeSort(arr, mid+1, r)",
    "  merge(left half, right half)",
    "    compare and pick smaller",
    "    write back to array",
  ],
  heap: [
    "buildMaxHeap(arr)",
    "  for i = n/2−1 downto 0",
    "    heapify(arr, n, i)",
    "for i = n−1 downto 1",
    "  swap(arr[0], arr[i])",
    "  heapify(arr, i, 0)",
  ],
};
