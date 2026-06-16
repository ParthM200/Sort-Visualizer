import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class VisualizerFrame extends JFrame {

    private static final Color BG       = new Color(0xF8F9FA);
    private static final Color TEXT_DARK = new Color(0x2B2D42);
    private static final Color TEXT_MUTE = new Color(0x8D99AE);
    private static final Color ACCENT   = new Color(0x4361EE);

    private SortPanel bubblePanel;
    private SortPanel insertionPanel;

    private final int[] originalArray;
    private final int   delayMs;

    private Timer tickTimer;

    public VisualizerFrame(int[] array, int delayMs) {
        this.originalArray = array.clone();
        this.delayMs       = delayMs;

        setTitle("Sort Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 660);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);

        buildUI();
        setVisible(true);
        startSort(array);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG);
                g.fillRect(0, 0, getWidth(), getHeight());
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
        bar.setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));

        JLabel title = new JLabel("Sort Visualizer");
        title.setFont(new Font("SF Pro Display", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JLabel sub = new JLabel("Bubble Sort  ·  Insertion Sort  ·  Side by Side");
        sub.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTE);

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

        JPanel area = new JPanel(new GridLayout(1, 2, 16, 0));
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        area.add(bubblePanel);
        area.add(insertionPanel);
        return area;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 0, 20, 0));

        JButton restart  = buildBtn("Restart",   new Color(0x4361EE));
        JButton newArray = buildBtn("New Array",  new Color(0x495057));
        JButton back     = buildBtn("← Settings", new Color(0xADB5BD));

        restart.addActionListener(e -> {
            stopSort();
            startSort(originalArray);
        });

        newArray.addActionListener(e -> {
            stopSort();
            int[] arr = new int[originalArray.length];
            for (int i = 0; i < arr.length; i++) arr[i] = (int)(Math.random() * 95) + 5;
            startSort(arr);
        });

        back.addActionListener(e -> {
            stopSort();
            dispose();
            new WelcomeScreen();
        });

        bar.add(restart);
        bar.add(newArray);
        bar.add(back);
        return bar;
    }

    private void startSort(int[] array) {
        bubblePanel.setArray(array);
        insertionPanel.setArray(array);

        // Tick timer to refresh elapsed time display while sorting
        if (tickTimer != null) tickTimer.stop();
        tickTimer = new Timer(100, e -> {
            bubblePanel.tick();
            insertionPanel.tick();
        });
        tickTimer.start();

        new BubbleSortThread(bubblePanel, delayMs).start();
        new InsertionSortThread(insertionPanel, delayMs).start();
    }

    private void stopSort() {
        if (tickTimer != null) tickTimer.stop();
        // Threads are daemons — they'll stop when we reset the panels
    }

    private JButton buildBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = hovered ? color.darker() : color;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SF Pro Text", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
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
