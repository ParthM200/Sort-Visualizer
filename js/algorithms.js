// ── Step object shape ─────────────────────────────────────────────────────────
// { type: 'compare'|'swap'|'overwrite'|'sorted'|'pivot'|'done', a?, b?, line }
// `a` and `b` are array indices involved in the step.
// `line` maps to the PSEUDOCODE[algo] array index.

// ── Bubble Sort ───────────────────────────────────────────────────────────────
function* bubbleSort(arr) {
  const n = arr.length;
  for (let i = 0; i < n - 1; i++) {
    for (let j = 0; j < n - i - 1; j++) {
      yield { type: 'compare', a: j, b: j + 1, line: 2 };
      if (arr[j] > arr[j + 1]) {
        [arr[j], arr[j + 1]] = [arr[j + 1], arr[j]];
        yield { type: 'swap', a: j, b: j + 1, line: 3 };
      }
    }
    yield { type: 'sorted', a: n - 1 - i, line: 4 };
  }
  yield { type: 'sorted', a: 0, line: 4 };
  yield { type: 'done' };
}

// ── Insertion Sort ────────────────────────────────────────────────────────────
function* insertionSort(arr) {
  const n = arr.length;
  yield { type: 'sorted', a: 0, line: 0 };
  for (let i = 1; i < n; i++) {
    const key = arr[i];
    let j = i - 1;
    yield { type: 'compare', a: i, b: Math.max(0, j), line: 3 };
    while (j >= 0 && arr[j] > key) {
      arr[j + 1] = arr[j];
      yield { type: 'swap', a: j + 1, b: j, line: 4 };
      j--;
      if (j >= 0) yield { type: 'compare', a: j + 1, b: j, line: 3 };
    }
    arr[j + 1] = key;
    yield { type: 'sorted', a: j + 1, line: 5 };
  }
  yield { type: 'done' };
}

// ── Selection Sort ────────────────────────────────────────────────────────────
function* selectionSort(arr) {
  const n = arr.length;
  for (let i = 0; i < n - 1; i++) {
    let minIdx = i;
    yield { type: 'pivot', a: minIdx, line: 1 };
    for (let j = i + 1; j < n; j++) {
      yield { type: 'compare', a: j, b: minIdx, line: 3 };
      if (arr[j] < arr[minIdx]) {
        minIdx = j;
        yield { type: 'pivot', a: minIdx, line: 4 };
      }
    }
    if (minIdx !== i) {
      [arr[i], arr[minIdx]] = [arr[minIdx], arr[i]];
      yield { type: 'swap', a: i, b: minIdx, line: 5 };
    }
    yield { type: 'sorted', a: i, line: 5 };
  }
  yield { type: 'sorted', a: n - 1, line: 5 };
  yield { type: 'done' };
}

// ── Quick Sort (iterative, avoids call-stack depth limits) ────────────────────
function* quickSort(arr) {
  const stack = [[0, arr.length - 1]];

  while (stack.length > 0) {
    const [lo, hi] = stack.pop();
    if (lo >= hi) {
      if (lo === hi) yield { type: 'sorted', a: lo, line: 7 };
      continue;
    }

    // Partition around arr[hi]
    const pivotVal = arr[hi];
    yield { type: 'pivot', a: hi, line: 1 };
    let i = lo - 1;

    for (let j = lo; j < hi; j++) {
      yield { type: 'compare', a: j, b: hi, line: 4 };
      if (arr[j] <= pivotVal) {
        i++;
        if (i !== j) {
          [arr[i], arr[j]] = [arr[j], arr[i]];
          yield { type: 'swap', a: i, b: j, line: 5 };
        }
      }
    }
    const p = i + 1;
    [arr[p], arr[hi]] = [arr[hi], arr[p]];
    yield { type: 'swap', a: p, b: hi, line: 6 };
    yield { type: 'sorted', a: p, line: 7 };

    stack.push([lo, p - 1]);
    stack.push([p + 1, hi]);
  }
  yield { type: 'done' };
}

// ── Merge Sort (iterative bottom-up) ─────────────────────────────────────────
function* mergeSort(arr) {
  const n = arr.length;
  const aux = arr.slice();

  for (let width = 1; width < n; width *= 2) {
    for (let lo = 0; lo < n; lo += 2 * width) {
      const mid = Math.min(lo + width - 1, n - 1);
      const hi  = Math.min(lo + 2 * width - 1, n - 1);
      if (mid >= hi) continue;

      for (let k = lo; k <= hi; k++) aux[k] = arr[k];

      let L = lo, R = mid + 1, k = lo;
      while (L <= mid && R <= hi) {
        yield { type: 'compare', a: L, b: R, line: 5 };
        if (aux[L] <= aux[R]) { arr[k] = aux[L++]; }
        else                  { arr[k] = aux[R++]; }
        yield { type: 'overwrite', a: k, line: 6 };
        k++;
      }
      while (L <= mid)  { arr[k] = aux[L++];  yield { type: 'overwrite', a: k, line: 6 }; k++; }
      while (R <= hi)   { arr[k] = aux[R++];  yield { type: 'overwrite', a: k, line: 6 }; k++; }

      for (let s = lo; s <= hi; s++) yield { type: 'sorted', a: s, line: 4 };
    }
  }
  yield { type: 'done' };
}

// ── Heap Sort ─────────────────────────────────────────────────────────────────
function* heapSort(arr) {
  const n = arr.length;

  for (let i = Math.floor(n / 2) - 1; i >= 0; i--) {
    yield* siftDown(arr, n, i);
  }

  for (let i = n - 1; i > 0; i--) {
    [arr[0], arr[i]] = [arr[i], arr[0]];
    yield { type: 'swap', a: 0, b: i, line: 4 };
    yield { type: 'sorted', a: i, line: 4 };
    yield* siftDown(arr, i, 0);
  }
  yield { type: 'sorted', a: 0, line: 5 };
  yield { type: 'done' };
}

function* siftDown(arr, n, i) {
  while (true) {
    let largest = i;
    const l = 2 * i + 1, r = 2 * i + 2;
    if (l < n) { yield { type: 'compare', a: l, b: largest, line: 2 }; if (arr[l] > arr[largest]) largest = l; }
    if (r < n) { yield { type: 'compare', a: r, b: largest, line: 2 }; if (arr[r] > arr[largest]) largest = r; }
    if (largest === i) break;
    [arr[i], arr[largest]] = [arr[largest], arr[i]];
    yield { type: 'swap', a: i, b: largest, line: 2 };
    i = largest;
  }
}

// ── Algorithm registry ────────────────────────────────────────────────────────
const ALGORITHMS = {
  bubble: {
    name: 'Bubble Sort',
    fn: bubbleSort,
    worstO: 'O(n²)',
    detail: 'Best: O(n)  ·  Avg: O(n²)  ·  Space: O(1)',
  },
  insertion: {
    name: 'Insertion Sort',
    fn: insertionSort,
    worstO: 'O(n²)',
    detail: 'Best: O(n)  ·  Avg: O(n²)  ·  Space: O(1)',
  },
  selection: {
    name: 'Selection Sort',
    fn: selectionSort,
    worstO: 'O(n²)',
    detail: 'Best: O(n²)  ·  Avg: O(n²)  ·  Space: O(1)',
  },
  quick: {
    name: 'Quick Sort',
    fn: quickSort,
    worstO: 'O(n²)*',
    detail: 'Best: O(n log n)  ·  Avg: O(n log n)  ·  Space: O(log n)',
  },
  merge: {
    name: 'Merge Sort',
    fn: mergeSort,
    worstO: 'O(n log n)',
    detail: 'Best: O(n log n)  ·  Avg: O(n log n)  ·  Space: O(n)',
  },
  heap: {
    name: 'Heap Sort',
    fn: heapSort,
    worstO: 'O(n log n)',
    detail: 'Best: O(n log n)  ·  Avg: O(n log n)  ·  Space: O(1)',
  },
};
