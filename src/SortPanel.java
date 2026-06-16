import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SortPanel extends JPanel {

    public static final Color BAR_DEFAULT  = new Color(0x4361EE);
    public static final Color BAR_COMPARE  = new Color(0xFF6B6B);
    public static final Color BAR_SORTED   = new Color(0x06D6A0);
    public static final Color BAR_PIVOT    = new Color(0xFFB703);
    private static final Color BG          = new Color(0xF8F9FA);
    private static final Color TEXT_DARK   = new Color(0x2B2D42);
    private static final Color TEXT_MUTE   = new Color(0x8D99AE);
    private static final Color STAT_BG     = new Color(0xEEF0F8);

    private final String algorithmName;
    private int[]  array;
    private int    compareA   = -1;
    private int    compareB   = -1;
    private int    sortedFrom = Integer.MAX_VALUE; // indices >= sortedFrom are sorted (bubble)
    private boolean[] sortedFlags;                 // per-index for insertion

    private int     comparisons = 0;
    private int     swaps       = 0;
    private long    startTime   = 0;
    private long    elapsedMs   = 0;
    private boolean running     = false;
    private boolean done        = false;

    public SortPanel(String algorithmName, int[] array) {
        this.algorithmName = algorithmName;
        setArray(array);
        setOpaque(false);
    }

    public void setArray(int[] arr) {
        this.array      = arr.clone();
        this.sortedFlags = new boolean[arr.length];
        this.compareA   = -1;
        this.compareB   = -1;
        this.sortedFrom = Integer.MAX_VALUE;
        this.comparisons = 0;
        this.swaps       = 0;
        this.elapsedMs   = 0;
        this.running     = false;
        this.done        = false;
        repaint();
    }

    // Called by sort thread on each step
    public synchronized void highlight(int a, int b) {
        compareA = a;
        compareB = b;
        repaint();
    }

    public synchronized void markSortedFrom(int idx) {
        sortedFrom = idx;
        repaint();
    }

    public synchronized void markSortedIndex(int idx) {
        if (idx >= 0 && idx < sortedFlags.length) sortedFlags[idx] = true;
        repaint();
    }

    public synchronized void incrementComparisons() { comparisons++; }
    public synchronized void incrementSwaps()       { swaps++; }

    public synchronized void setRunning(boolean r) {
        running = r;
        if (r) startTime = System.currentTimeMillis();
    }

    public synchronized void setDone() {
        done = true;
        running = false;
        elapsedMs = System.currentTimeMillis() - startTime;
        compareA = -1;
        compareB = -1;
        sortedFrom = 0;
        for (int i = 0; i < sortedFlags.length; i++) sortedFlags[i] = true;
        repaint();
    }

    public synchronized void tick() {
        if (running) elapsedMs = System.currentTimeMillis() - startTime;
        repaint();
    }

    public int[] getArray() { return array; }

    // ── Painting ─────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Panel background
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));

        int headerH = 52;
        int statsH  = 80;
        int chartH  = h - headerH - statsH - 16;
        int chartY  = headerH + 8;

        drawHeader(g2, w, headerH);
        drawBars(g2, 16, chartY, w - 32, chartH);
        drawStats(g2, 0, h - statsH, w, statsH);

        g2.dispose();
    }

    private void drawHeader(Graphics2D g2, int w, int headerH) {
        // Algorithm name
        g2.setFont(new Font("SF Pro Display", Font.BOLD, 17));
        g2.setColor(TEXT_DARK);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(algorithmName, 20, 30);

        // Status badge
        String status;
        Color badgeColor;
        if (done) {
            status = "Complete ✓";
            badgeColor = BAR_SORTED;
        } else if (running) {
            status = "Sorting...";
            badgeColor = BAR_COMPARE;
        } else {
            status = "Ready";
            badgeColor = new Color(0xADB5BD);
        }

        g2.setFont(new Font("SF Pro Text", Font.BOLD, 11));
        int sw = g2.getFontMetrics().stringWidth(status);
        int bw = sw + 16, bh = 22;
        int bx = w - bw - 16, by = 10;
        g2.setColor(badgeColor.brighter().brighter());
        g2.fill(new RoundRectangle2D.Float(bx, by, bw, bh, bh, bh));
        g2.setColor(badgeColor.darker());
        g2.drawString(status, bx + 8, by + bh - 6);

        // Thin separator line
        g2.setColor(new Color(0xE9ECEF));
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(16, headerH - 4, w - 16, headerH - 4);
    }

    private void drawBars(Graphics2D g2, int x, int y, int w, int h) {
        if (array == null || array.length == 0) return;

        int n = array.length;
        int maxVal = 100;

        float totalGap = n > 1 ? (n - 1) * 2f : 0;
        float barW = (w - totalGap) / (float) n;
        if (barW < 1) barW = 1;

        for (int i = 0; i < n; i++) {
            float barH = (array[i] / (float) maxVal) * h;
            float bx = x + i * (barW + 2);
            float by = y + h - barH;

            Color c;
            if (done || sortedFlags[i] || i >= sortedFrom) {
                c = BAR_SORTED;
            } else if (i == compareA || i == compareB) {
                c = BAR_COMPARE;
            } else {
                c = BAR_DEFAULT;
            }

            // Bar body
            g2.setColor(c);
            float radius = Math.min(barW / 2f, 4f);
            g2.fill(new RoundRectangle2D.Float(bx, by, barW, barH, radius, radius));

            // Subtle top shine
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fill(new RoundRectangle2D.Float(bx, by, barW, Math.min(barH, 6), radius, radius));
        }
    }

    private void drawStats(Graphics2D g2, int x, int y, int w, int h) {
        int pad = 16;
        int statW = (w - pad * 2) / 3;

        drawStat(g2, x + pad,                y + 10, statW - 8, "Comparisons", String.valueOf(comparisons), BAR_DEFAULT);
        drawStat(g2, x + pad + statW,        y + 10, statW - 8, "Swaps",       String.valueOf(swaps),       BAR_COMPARE);
        drawStat(g2, x + pad + statW * 2,    y + 10, statW - 8, "Time",        formatTime(elapsedMs),       BAR_SORTED);
    }

    private void drawStat(Graphics2D g2, int x, int y, int w, String label, String value, Color accent) {
        int h = 58;
        // Stat card background
        g2.setColor(STAT_BG);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 10, 10));

        // Accent left bar
        g2.setColor(accent);
        g2.fill(new RoundRectangle2D.Float(x, y, 3, h, 3, 3));

        // Value
        g2.setFont(new Font("SF Pro Display", Font.BOLD, 22));
        g2.setColor(TEXT_DARK);
        g2.drawString(value, x + 12, y + 32);

        // Label
        g2.setFont(new Font("SF Pro Text", Font.PLAIN, 11));
        g2.setColor(TEXT_MUTE);
        g2.drawString(label, x + 12, y + 48);
    }

    private String formatTime(long ms) {
        if (ms < 1000) return ms + "ms";
        return String.format("%.2fs", ms / 1000.0);
    }
}
