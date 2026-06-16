import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SortPanel extends JPanel {

    private static final Color CARD      = new Color(0x13151D);
    private static final Color BORDER    = new Color(0x252836);
    private static final Color GRID_LINE = new Color(255, 255, 255, 10);
    private static final Color BAR_BASE  = new Color(0x4A6CF7);
    private static final Color BAR_TOP   = new Color(0x8AA4FF);
    private static final Color COMPARE   = new Color(0xF06078);
    private static final Color SORTED    = new Color(0x00E5A0);
    private static final Color SORTED_TOP= new Color(0x7FFFD4);
    private static final Color PIVOT     = new Color(0xFFB703);
    private static final Color TEXT_H    = new Color(0xF1F5F9);
    private static final Color TEXT_BODY = new Color(0x94A3B8);
    private static final Color TEXT_MUTE = new Color(0x4B5563);
    private static final Color STAT_BG   = new Color(0x1C1F2E);

    private final String algorithmName;
    private int[]    array;
    private int      compareA    = -1;
    private int      compareB    = -1;
    private int      sortedFrom  = Integer.MAX_VALUE;
    private boolean[] sortedFlags;

    private int     comparisons = 0;
    private int     swaps       = 0;
    private long    startTime   = 0;
    private long    elapsedMs   = 0;
    private boolean running     = false;
    private boolean done        = false;
    private boolean winner      = false; // finished first

    public SortPanel(String name, int[] arr) {
        this.algorithmName = name;
        setArray(arr);
        setOpaque(false);
    }

    public synchronized void setArray(int[] arr) {
        array       = arr.clone();
        sortedFlags = new boolean[arr.length];
        compareA    = -1;
        compareB    = -1;
        sortedFrom  = Integer.MAX_VALUE;
        comparisons = 0;
        swaps       = 0;
        elapsedMs   = 0;
        running     = false;
        done        = false;
        winner      = false;
        repaint();
    }

    public synchronized void highlight(int a, int b)    { compareA = a; compareB = b; repaint(); }
    public synchronized void markSortedFrom(int idx)    { sortedFrom = idx; repaint(); }
    public synchronized void markSortedIndex(int idx)   { if (idx >= 0 && idx < sortedFlags.length) sortedFlags[idx] = true; repaint(); }
    public synchronized void incrementComparisons()     { comparisons++; }
    public synchronized void incrementSwaps()           { swaps++; }

    public synchronized void setRunning(boolean r) {
        running   = r;
        startTime = System.currentTimeMillis();
    }

    public synchronized void setDone() {
        done       = true;
        running    = false;
        elapsedMs  = System.currentTimeMillis() - startTime;
        compareA   = -1;
        compareB   = -1;
        sortedFrom = 0;
        for (int i = 0; i < sortedFlags.length; i++) sortedFlags[i] = true;
        repaint();
    }

    public synchronized void setWinner(boolean w) { winner = w; }
    public synchronized boolean isDone()           { return done; }

    public synchronized void tick() {
        if (running) elapsedMs = System.currentTimeMillis() - startTime;
        repaint();
    }

    public int[] getArray() { return array; }

    // ── Painting ─────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth(), h = getHeight();
        int headerH = 62;
        int statsH  = 88;
        int pad     = 16;
        int chartY  = headerH + pad;
        int chartH  = h - headerH - statsH - pad * 2;

        // ── Card bg + border ─────────────────────────────────
        g2.setColor(CARD);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));
        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(1));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w-1, h-1, 18, 18));

        drawHeader(g2, w, headerH);
        drawChart(g2, pad, chartY, w - pad*2, chartH);
        drawStats(g2, pad, h - statsH - pad/2, w - pad*2, statsH);

        g2.dispose();
    }

    private void drawHeader(Graphics2D g2, int w, int headerH) {
        // Algorithm name
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2.setColor(TEXT_H);
        g2.drawString(algorithmName, 20, 32);

        // Winner badge
        if (winner) {
            drawBadge(g2, w, "WINNER  🏆", new Color(0xFFB703), new Color(0x7A5500));
        } else if (done) {
            drawBadge(g2, w, "Complete ✓", SORTED, new Color(0x004D38));
        } else if (running) {
            drawBadge(g2, w, "Sorting...", COMPARE, new Color(0x5A0018));
        } else {
            drawBadge(g2, w, "Ready", new Color(0x94A3B8), new Color(0x1E2433));
        }

        // Separator line with gradient
        g2.setStroke(new BasicStroke(1));
        GradientPaint sep = new GradientPaint(0, 0, BORDER, w, 0, CARD);
        g2.setPaint(sep);
        g2.drawLine(16, headerH - 6, w - 16, headerH - 6);
    }

    private void drawBadge(Graphics2D g2, int panelW, String text, Color fgColor, Color bgColor) {
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        int sw = g2.getFontMetrics().stringWidth(text);
        int bw = sw + 18, bh = 22, bx = panelW - bw - 16, by = 14;
        g2.setColor(bgColor);
        g2.fill(new RoundRectangle2D.Float(bx, by, bw, bh, bh, bh));
        g2.setColor(fgColor);
        g2.drawString(text, bx + 9, by + bh - 7);
    }

    private void drawChart(Graphics2D g2, int x, int y, int w, int h) {
        // Chart area background with subtle inset
        g2.setColor(new Color(0x0D0F16));
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 8, 8));

        // Grid lines (horizontal, at 25/50/75%)
        g2.setColor(GRID_LINE);
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 6}, 0));
        for (int pct = 25; pct <= 75; pct += 25) {
            int lineY = y + h - (int)(h * pct / 100f);
            g2.drawLine(x + 4, lineY, x + w - 4, lineY);
        }
        g2.setStroke(new BasicStroke(1));

        if (array == null || array.length == 0) return;

        int n = array.length;
        float gap    = n > 60 ? 1f : 2f;
        float totalGap = (n - 1) * gap;
        float barW   = Math.max(1f, (w - totalGap) / (float) n);
        float radius = Math.min(barW / 2f, 3f);

        for (int i = 0; i < n; i++) {
            float barH = (array[i] / 100f) * (h - 6);
            float bx   = x + i * (barW + gap);
            float by   = y + h - barH;

            boolean isSorted   = done || sortedFlags[i] || i >= sortedFrom;
            boolean isCompared = (i == compareA || i == compareB);

            Color topColor, botColor;
            if (isSorted) {
                topColor = SORTED_TOP; botColor = SORTED;
            } else if (isCompared) {
                topColor = COMPARE.brighter(); botColor = COMPARE;
            } else {
                topColor = BAR_TOP; botColor = BAR_BASE;
            }

            // Glow effect for active bars
            if (isCompared && !done) {
                for (int gl = 5; gl >= 1; gl--) {
                    float exp = gl * 1.8f;
                    g2.setColor(new Color(COMPARE.getRed(), COMPARE.getGreen(), COMPARE.getBlue(), gl * 14));
                    g2.fill(new RoundRectangle2D.Float(bx - exp, by - exp/2f, barW + exp*2, barH + exp, radius + exp, radius + exp));
                }
            }
            if (isSorted && done) {
                for (int gl = 3; gl >= 1; gl--) {
                    float exp = gl * 1.2f;
                    g2.setColor(new Color(SORTED.getRed(), SORTED.getGreen(), SORTED.getBlue(), gl * 10));
                    g2.fill(new RoundRectangle2D.Float(bx - exp, by - exp/2f, barW + exp*2, barH + exp, radius + exp, radius + exp));
                }
            }

            // Main bar with gradient
            GradientPaint gp = new GradientPaint(bx, by, topColor, bx, by + barH, botColor);
            g2.setPaint(gp);
            if (barW <= 2) {
                g2.fillRect((int)bx, (int)by, (int)Math.max(1, barW), (int)barH);
            } else {
                g2.fill(new RoundRectangle2D.Float(bx, by, barW, barH, radius, radius));
            }
        }
    }

    private void drawStats(Graphics2D g2, int x, int y, int w, int h) {
        int gap  = 8;
        int sw   = (w - gap * 2) / 3;

        drawStatCard(g2, x,                y, sw, h, "Comparisons", fmt(comparisons), BAR_BASE);
        drawStatCard(g2, x + sw + gap,     y, sw, h, "Swaps",       fmt(swaps),       COMPARE);
        drawStatCard(g2, x + (sw + gap)*2, y, sw, h, "Time",        fmtTime(elapsedMs), SORTED);
    }

    private void drawStatCard(Graphics2D g2, int x, int y, int w, int h, String label, String value, Color accent) {
        // Card bg
        g2.setColor(STAT_BG);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 10, 10));

        // Top accent line
        GradientPaint top = new GradientPaint(x, y, accent, x + w, y, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0));
        g2.setPaint(top);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(x + 8, y + 1, x + w - 8, y + 1);
        g2.setStroke(new BasicStroke(1));

        // Value (monospace, large)
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        g2.setColor(TEXT_H);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(value, x + (w - fm.stringWidth(value)) / 2, y + 40);

        // Label
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g2.setColor(TEXT_BODY);
        fm = g2.getFontMetrics();
        g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + 58);
    }

    private String fmt(int n)        { return n >= 1000 ? String.format("%,d", n) : String.valueOf(n); }
    private String fmtTime(long ms)  { return ms < 1000 ? ms + "ms" : String.format("%.2fs", ms / 1000.0); }
}
