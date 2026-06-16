// ── Constants ─────────────────────────────────────────────────────────────────
const SPEED_MS     = [0, 280, 90, 28, 8, 1];
const SPEED_NAMES  = ['', 'Very Slow', 'Slow', 'Medium', 'Fast', 'Blazing'];
const BAR_NORMAL   = ['#8AA4FF', '#4A6CF7'];
const BAR_COMPARE  = ['#FF8099', '#F06078'];
const BAR_SORTED   = ['#7FFFD4', '#00E5A0'];
const BAR_PIVOT    = ['#FFD860', '#FFB703'];
const BAR_OVERWRITE= ['#C0A9FF', '#9B72FF'];

// ── App State ─────────────────────────────────────────────────────────────────
const app = {
  selectedAlgos: [],
  arraySize:  50,
  speed:       3,
  pattern:    'random',
  originalArr: null,

  leftAlgoKey:  null,
  rightAlgoKey: null,

  left:  null,   // SideState
  right: null,

  running:     false,
  rafId:       null,
  lastTick:    0,
};

function makeSide() {
  return {
    arr:     [],
    sorted:  new Set(),
    gen:     null,
    compareA:  -1, compareB: -1,
    pivot:     -1,
    overwrite: -1,
    line:      -1,
    comparisons: 0,
    swaps:       0,
    startTime:   0,
    elapsed:     0,
    done:        false,
    winner:      false,
  };
}

// ── Welcome Screen ────────────────────────────────────────────────────────────
function initWelcome() {
  const grid = document.getElementById('algo-grid');
  for (const [key, algo] of Object.entries(ALGORITHMS)) {
    const card = document.createElement('div');
    card.className = 'algo-card';
    card.dataset.key = key;
    card.innerHTML = `<div class="algo-card-name">${algo.name}</div>`
                   + `<div class="algo-card-o">${algo.worstO}</div>`;
    card.addEventListener('click', () => toggleAlgo(key));
    grid.appendChild(card);
  }

  const sizeSlider  = document.getElementById('size-slider');
  const speedSlider = document.getElementById('speed-slider');
  sizeSlider.addEventListener('input',  () => {
    app.arraySize = +sizeSlider.value;
    document.getElementById('size-val').textContent = sizeSlider.value;
  });
  speedSlider.addEventListener('input', () => {
    app.speed = +speedSlider.value;
    document.getElementById('speed-val').textContent = SPEED_NAMES[speedSlider.value];
  });

  document.querySelectorAll('.pattern-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.pattern-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      app.pattern = btn.dataset.pattern;
    });
  });

  document.getElementById('visualize-btn').addEventListener('click', launchVisualizer);
}

function toggleAlgo(key) {
  const idx = app.selectedAlgos.indexOf(key);
  if (idx >= 0) {
    app.selectedAlgos.splice(idx, 1);
  } else {
    if (app.selectedAlgos.length >= 2) {
      const old = app.selectedAlgos.shift();
      document.querySelector(`.algo-card[data-key="${old}"]`).classList.remove('selected');
    }
    app.selectedAlgos.push(key);
  }
  document.querySelectorAll('.algo-card').forEach(c =>
    c.classList.toggle('selected', app.selectedAlgos.includes(c.dataset.key))
  );
  const n = app.selectedAlgos.length;
  const hint = document.getElementById('pick-hint');
  hint.textContent = `Select 2 algorithms  (${n} / 2)`;
  hint.className = 'pick-hint' + (n === 2 ? ' full' : '');
  document.getElementById('visualize-btn').disabled = n !== 2;
}

// ── Array Generation ──────────────────────────────────────────────────────────
function generateArray(size, pattern) {
  let arr;
  switch (pattern) {
    case 'reversed':
      arr = Array.from({length: size}, (_, i) => Math.round(100 - (i / (size - 1)) * 90));
      break;
    case 'nearly':
      arr = Array.from({length: size}, (_, i) => Math.round(5 + (i / (size - 1)) * 90));
      for (let k = 0; k < Math.ceil(size * 0.06); k++) {
        const a = Math.floor(Math.random() * size);
        const b = Math.floor(Math.random() * size);
        [arr[a], arr[b]] = [arr[b], arr[a]];
      }
      break;
    case 'few':
      arr = Array.from({length: size}, () => [18, 36, 55, 72, 90][Math.floor(Math.random() * 5)]);
      break;
    default:
      arr = Array.from({length: size}, () => Math.round(Math.random() * 90) + 5);
  }
  return arr;
}

// ── Launch Visualizer ─────────────────────────────────────────────────────────
function launchVisualizer() {
  app.leftAlgoKey  = app.selectedAlgos[0];
  app.rightAlgoKey = app.selectedAlgos[1];
  app.originalArr  = generateArray(app.arraySize, app.pattern);

  setupVisualizerUI();
  showScreen('visualizer-screen');
  // Tiny delay so layout is settled before canvas sizing
  setTimeout(() => startVisualization(app.originalArr.slice()), 60);
}

function setupVisualizerUI() {
  const L = ALGORITHMS[app.leftAlgoKey];
  const R = ALGORITHMS[app.rightAlgoKey];

  document.getElementById('viz-vs-label').textContent = `${L.name}  vs  ${R.name}`;
  setEl('left-name',   L.name);   setEl('right-name',   R.name);
  setEl('left-bigo',   L.worstO); setEl('right-bigo',   R.worstO);
  setEl('left-detail', L.detail); setEl('right-detail', R.detail);

  buildPseudo('pseudo-left',  PSEUDOCODE[app.leftAlgoKey]);
  buildPseudo('pseudo-right', PSEUDOCODE[app.rightAlgoKey]);

  document.getElementById('btn-play').onclick    = togglePlay;
  document.getElementById('btn-step').onclick    = stepOnce;
  document.getElementById('btn-restart').onclick = () => startVisualization(app.originalArr.slice());
  document.getElementById('btn-new').onclick     = () => {
    app.originalArr = generateArray(app.arraySize, app.pattern);
    startVisualization(app.originalArr.slice());
  };
  document.getElementById('btn-back').onclick = () => {
    stopAnim();
    showScreen('welcome-screen');
  };
}

function buildPseudo(id, lines) {
  document.getElementById(id).innerHTML =
    lines.map((ln, i) => `<div class="pseudo-line" data-i="${i}">${esc(ln)}</div>`).join('');
}

// ── Start / Reset ─────────────────────────────────────────────────────────────
function startVisualization(arr) {
  stopAnim();

  app.left  = makeSide();
  app.right = makeSide();

  app.left.arr  = arr.slice();
  app.right.arr = arr.slice();

  const t = performance.now();
  app.left.startTime  = t;
  app.right.startTime = t;

  app.left.gen  = ALGORITHMS[app.leftAlgoKey].fn(app.left.arr);
  app.right.gen = ALGORITHMS[app.rightAlgoKey].fn(app.right.arr);

  app.running  = true;
  app.lastTick = t;

  setStatus('left',  'sorting', 'Sorting...');
  setStatus('right', 'sorting', 'Sorting...');
  updateStats('left');
  updateStats('right');

  document.getElementById('btn-play').textContent = '⏸ Pause';

  resizeCanvases();
  app.rafId = requestAnimationFrame(tick);
}

function stopAnim() {
  if (app.rafId) cancelAnimationFrame(app.rafId);
  app.rafId   = null;
  app.running = false;
}

// ── Animation Loop ────────────────────────────────────────────────────────────
function tick(now) {
  const msPerStep = SPEED_MS[app.speed];
  const elapsed   = now - app.lastTick;

  if (app.running && elapsed >= msPerStep) {
    const steps = Math.min(60, Math.max(1, Math.floor(elapsed / msPerStep)));
    for (let s = 0; s < steps; s++) {
      advance('left');
      advance('right');
    }
    app.lastTick = now;
  }

  const t = performance.now();
  if (!app.left.done)  { app.left.elapsed  = t - app.left.startTime; }
  if (!app.right.done) { app.right.elapsed = t - app.right.startTime; }

  updateStats('left');
  updateStats('right');
  checkWinner();

  drawSide('left');
  drawSide('right');

  app.rafId = requestAnimationFrame(tick);
}

// ── Step Advance ──────────────────────────────────────────────────────────────
function advance(side) {
  const s = app[side];
  if (s.done) return;

  const { value, done } = s.gen.next();

  s.compareA = -1; s.compareB = -1; s.pivot = -1; s.overwrite = -1;

  if (done || !value || value.type === 'done') {
    s.done = true;
    s.line = -1;
    s.elapsed = performance.now() - s.startTime;
    for (let i = 0; i < s.arr.length; i++) s.sorted.add(i);
    setStatus(side, 'done', 'Done ✓');
    setLine(side, -1);
    return;
  }

  s.line = value.line ?? -1;
  setLine(side, s.line);

  switch (value.type) {
    case 'compare':
      s.compareA = value.a; s.compareB = value.b;
      s.comparisons++;
      break;
    case 'swap':
      s.compareA = value.a; s.compareB = value.b;
      s.swaps++;
      break;
    case 'overwrite':
      s.overwrite = value.a;
      s.swaps++;
      break;
    case 'pivot':
      s.pivot = value.a;
      break;
    case 'sorted':
      s.sorted.add(value.a);
      break;
  }
}

// ── Winner check ──────────────────────────────────────────────────────────────
function checkWinner() {
  const L = app.left, R = app.right;
  if (L.done && R.done) return;
  if (L.done && !L.winner && !R.winner) {
    L.winner = true;
    setStatus('left', 'winner', 'Winner 🏆');
  }
  if (R.done && !R.winner && !L.winner) {
    R.winner = true;
    setStatus('right', 'winner', 'Winner 🏆');
  }
}

// ── Controls ──────────────────────────────────────────────────────────────────
function togglePlay() {
  app.running = !app.running;
  document.getElementById('btn-play').textContent = app.running ? '⏸ Pause' : '▶ Play';
}

function stepOnce() {
  if (app.running) {
    app.running = false;
    document.getElementById('btn-play').textContent = '▶ Play';
  }
  advance('left');
  advance('right');
  const t = performance.now();
  if (!app.left.done)  app.left.elapsed  = t - app.left.startTime;
  if (!app.right.done) app.right.elapsed = t - app.right.startTime;
  updateStats('left'); updateStats('right');
  checkWinner();
  drawSide('left'); drawSide('right');
}

// ── Canvas Drawing ────────────────────────────────────────────────────────────
function resizeCanvases() {
  for (const side of ['left', 'right']) {
    const cv   = document.getElementById(`canvas-${side}`);
    const rect = cv.getBoundingClientRect();
    cv.width  = Math.round(rect.width  * devicePixelRatio);
    cv.height = Math.round(rect.height * devicePixelRatio);
  }
}

function drawSide(side) {
  const cv  = document.getElementById(`canvas-${side}`);
  const ctx = cv.getContext('2d');
  const s   = app[side];
  if (!s || !s.arr.length) return;

  const dpr = devicePixelRatio;
  const W   = cv.width / dpr;
  const H   = cv.height / dpr;
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

  // Background
  ctx.fillStyle = '#0D0F16';
  ctx.fillRect(0, 0, W, H);

  // Grid lines
  ctx.save();
  ctx.strokeStyle = 'rgba(255,255,255,0.05)';
  ctx.setLineDash([4, 7]);
  ctx.lineWidth = 1;
  for (const pct of [0.25, 0.5, 0.75]) {
    const y = H * (1 - pct);
    ctx.beginPath(); ctx.moveTo(6, y); ctx.lineTo(W - 6, y); ctx.stroke();
  }
  ctx.restore();

  const arr  = s.arr;
  const n    = arr.length;
  const gap  = n > 90 ? 0.8 : n > 50 ? 1.2 : 1.8;
  const barW = Math.max(1, (W - gap * (n - 1)) / n);
  const r    = Math.min(barW / 2, 3.5);
  const pad  = 4;

  for (let i = 0; i < n; i++) {
    const bx  = i * (barW + gap);
    const bH  = Math.max(2, (arr[i] / 100) * (H - pad));
    const by  = H - bH;

    const isSorted   = s.done || s.sorted.has(i);
    const isCompare  = i === s.compareA || i === s.compareB;
    const isPivot    = i === s.pivot;
    const isOverwrite= i === s.overwrite;

    let [top, bot] = isSorted ? BAR_SORTED
                   : isCompare ? BAR_COMPARE
                   : isPivot    ? BAR_PIVOT
                   : isOverwrite ? BAR_OVERWRITE
                   : BAR_NORMAL;

    // Glow
    if ((isCompare || isPivot || isOverwrite) && !s.done) {
      const gc = isCompare ? 'rgba(240,96,120,'
               : isPivot   ? 'rgba(255,183,3,'
               : 'rgba(155,114,255,';
      for (let g = 5; g >= 1; g--) {
        const e = g * 2.2;
        ctx.fillStyle = gc + (g * 0.055) + ')';
        drawRoundBar(ctx, bx - e, by - e * 0.4, barW + e * 2, bH + e, r + e);
      }
    }
    if (isSorted && s.done) {
      for (let g = 3; g >= 1; g--) {
        const e = g * 1.6;
        ctx.fillStyle = `rgba(0,229,160,${g * 0.045})`;
        drawRoundBar(ctx, bx - e, by - e * 0.4, barW + e * 2, bH + e, r + e);
      }
    }

    // Main bar
    const grad = ctx.createLinearGradient(bx, by, bx, by + bH);
    grad.addColorStop(0, top);
    grad.addColorStop(1, bot);
    ctx.fillStyle = grad;
    drawRoundBar(ctx, bx, by, barW, bH, r);
  }
}

// Rounded-top bar (corners only on top so bars sit flush at bottom)
function drawRoundBar(ctx, x, y, w, h, r) {
  if (w <= 0 || h <= 0) return;
  if (w <= 2) { ctx.fillRect(x, y, Math.max(1, w), h); return; }
  const cr = Math.min(r, w / 2, h / 2);
  ctx.beginPath();
  ctx.moveTo(x + cr, y);
  ctx.lineTo(x + w - cr, y);
  ctx.arcTo(x + w, y,     x + w, y + cr,    cr);
  ctx.lineTo(x + w, y + h);
  ctx.lineTo(x,     y + h);
  ctx.arcTo(x,      y + h, x,     y + h - cr, 0);
  ctx.arcTo(x,      y,     x + cr, y,          cr);
  ctx.closePath();
  ctx.fill();
}

// ── UI Helpers ────────────────────────────────────────────────────────────────
function showScreen(id) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById(id).classList.add('active');
  if (id === 'visualizer-screen') setTimeout(resizeCanvases, 50);
}

function setEl(id, text) { document.getElementById(id).textContent = text; }

function setStatus(side, cls, text) {
  const el = document.getElementById(`${side}-status`);
  el.className = `status-badge ${cls}`;
  el.textContent = text;
}

function setLine(side, idx) {
  const panel = document.getElementById(`pseudo-${side}`);
  panel.querySelectorAll('.pseudo-line').forEach(el => el.classList.remove('active'));
  if (idx >= 0) panel.querySelector(`[data-i="${idx}"]`)?.classList.add('active');
}

function updateStats(side) {
  const s = app[side];
  if (!s) return;
  setEl(`${side}-cmp`,  fmt(s.comparisons));
  setEl(`${side}-swp`,  fmt(s.swaps));
  setEl(`${side}-time`, fmtMs(s.elapsed));
}

function fmt(n)    { return n >= 1000 ? n.toLocaleString() : String(n); }
function fmtMs(ms) { return ms < 1000 ? Math.round(ms) + 'ms' : (ms / 1000).toFixed(2) + 's'; }
function esc(s)    { return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }

// ── Resize ────────────────────────────────────────────────────────────────────
window.addEventListener('resize', resizeCanvases);

// ── Boot ──────────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', initWelcome);
