import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class VisualizerFrame extends JFrame {

    private static final Color BG        = new Color(0x0B0C10);
    private static final Color BLUE      = new Color(0x4A6CF7);
    private static final Color PURPLE    = new Color(0x7B4FEF);
    private static final Color TEXT_H    = new Color(0xF1F5F9);
    private static final Color TEXT_BODY = new Color(0x94A3B8);
    private static final Color BTN_GHOST = new Color(0x1C1F2E);
    private static final Color BORDER    = new Color(0x252836);

    private SortPanel bubblePanel;
    private SortPanel insertionPanel;
    private final int[] originalArray;
    private final int   delayMs;

    private Thread bubbleThread;
    private Thread insertionThread;
    private Timer  tickTimer;

    public VisualizerFrame(int[] array, int delayMs) {
        this.originalArray = array.clone();
        this.delayMs       = delayMs;

        setTitle("Sort Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 680);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);

        buildUI();
        setVisible(true);
        startSort(originalArray.clone());
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle dot grid
                g2.setColor(new Color(255, 255, 255, 8));
                for (int x = 20; x < getWidth(); x += 32)
                    for (int y = 20; y < getHeight(); y += 32)
                        g2.fillOval(x, y, 2, 2);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.add(buildTopBar(),    BorderLayout.NORTH);
        root.add(buildChartArea(), BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(22, 28, 12, 28));

        // Left: title
        JLabel title = new JLabel("Sort Visualizer");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(TEXT_H);

        JLabel sub = new JLabel("Bubble Sort  ·  Insertion Sort  ·  Same Array  ·  Same Speed");
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        sub.setForeground(TEXT_BODY);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    private JPanel buildChartArea() {
        bubblePanel    = new SortPanel("Bubble Sort",    new int[0]);
        insertionPanel = new SortPanel("Insertion Sort", new int[0]);

        JPanel area = new JPanel(new GridLayout(1, 2, 14, 0));
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        area.add(bubblePanel);
        area.add(insertionPanel);
        return area;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 0, 22, 0));

        bar.add(buildBtn("Restart",    BLUE,     false));
        bar.add(buildBtn("New Array",  BTN_GHOST, true));
        bar.add(buildBtn("← Settings", BTN_GHOST, true));

        // Wire actions
        ((JButton) bar.getComponent(0)).addActionListener(e -> {
            stopSort();
            startSort(originalArray.clone());
        });
        ((JButton) bar.getComponent(1)).addActionListener(e -> {
            stopSort();
            int[] arr = new int[originalArray.length];
            for (int i = 0; i < arr.length; i++) arr[i] = (int)(Math.random() * 95) + 5;
            startSort(arr);
        });
        ((JButton) bar.getComponent(2)).addActionListener(e -> {
            stopSort();
            dispose();
            new WelcomeScreen();
        });
        return bar;
    }

    // ── Sort control ─────────────────────────────────────────

    private void startSort(int[] array) {
        bubblePanel.setArray(array);
        insertionPanel.setArray(array);

        if (tickTimer != null) tickTimer.stop();
        tickTimer = new Timer(80, e -> {
            bubblePanel.tick();
            insertionPanel.tick();
            // Check for winner
            checkWinner();
        });
        tickTimer.start();

        bubbleThread    = new BubbleSortThread(bubblePanel, delayMs);
        insertionThread = new InsertionSortThread(insertionPanel, delayMs);
        bubbleThread.start();
        insertionThread.start();
    }

    private void checkWinner() {
        boolean bDone = bubblePanel.isDone();
        boolean iDone = insertionPanel.isDone();
        if (bDone && !iDone) bubblePanel.setWinner(true);
        if (iDone && !bDone) insertionPanel.setWinner(true);
        if (bDone && iDone && tickTimer != null) tickTimer.stop();
    }

    private void stopSort() {
        if (tickTimer != null)      tickTimer.stop();
        if (bubbleThread != null)   bubbleThread.interrupt();
        if (insertionThread != null) insertionThread.interrupt();
    }

    // ── Button builder ───────────────────────────────────────

    private JButton buildBtn(String text, Color baseColor, boolean ghost) {
        JButton btn = new JButton(text) {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (ghost) {
                    g2.setColor(hov ? new Color(0x252836) : baseColor);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(BORDER);
                    g2.setStroke(new BasicStroke(1));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 10, 10));
                    g2.setColor(TEXT_BODY);
                } else {
                    if (hov) {
                        g2.setColor(new Color(74, 108, 247, 45));
                        g2.fill(new RoundRectangle2D.Float(-4, 3, getWidth()+8, getHeight()+2, 14, 14));
                    }
                    GradientPaint gp = new GradientPaint(0, 0, hov ? BLUE.brighter() : BLUE, getWidth(), 0, PURPLE);
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(Color.WHITE);
                }
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 40));
        return btn;
    }
}
