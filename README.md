# Sort Visualizer

An animated, interactive sort racing tool — pick any two algorithms from six, race them side by side, and watch every comparison and swap happen in real time.

**Live demo:** https://ParthM200.github.io/Sort-Visualizer

## Features

- **6 algorithms:** Bubble, Insertion, Selection, Quick, Merge, Heap Sort
- **Pick any two** to race on the same array simultaneously
- **Live pseudocode panel** — the active line highlights as each operation executes
- **Step-through mode** — pause and advance one operation at a time
- **Array controls** — size (10–150), speed (5 levels), and four array patterns (random, reversed, nearly sorted, few unique values)
- **Live stats** — comparisons, swaps, and elapsed time per algorithm
- **Winner badge** — the faster algorithm gets crowned
- **Big O info** — worst-case complexity badge + best/avg/space detail per algorithm

## Run locally

No build tools or dependencies needed. Just open `index.html` in any browser:

```bash
open index.html
# or serve with Python:
python3 -m http.server 8080
```

## Tech

Pure HTML + CSS + JavaScript. No frameworks, no bundler, no dependencies.

- `js/algorithms.js` — six sorting algorithms as ES6 generator functions; each `yield` is one visualized step
- `js/pseudocode.js` — pseudocode text per algorithm, indexed to match generator line numbers
- `js/app.js` — app controller: animation loop (`requestAnimationFrame`), step-through, canvas rendering, UI state
- `css/style.css` — dark futuristic theme using CSS custom properties
- `java/` — original Java/Swing version (Bubble + Insertion only); archived for reference

## Controls

| Button | Action |
|---|---|
| ⏸ Pause / ▶ Play | Pause or resume the animation |
| ⏭ Step | Advance exactly one operation (auto-pauses) |
| ↺ Restart | Re-run both algorithms on the same array |
| ⬡ New Array | Generate a new array and re-run |
| ← Back | Return to the setup screen |
