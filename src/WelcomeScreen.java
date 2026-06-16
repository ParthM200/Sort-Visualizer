import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class WelcomeScreen extends JFrame {

    private static final Color BG        = new Color(0xF8F9FA);
    private static final Color ACCENT    = new Color(0x4361EE);
    private static final Color TEXT_DARK = new Color(0x2B2D42);
    private static final Color TEXT_MUTE = new Color(0x8D99AE);
    private static final Color CARD_BG   = Color.WHITE;

    private JSlider sizeSlider;
    private JSlider speedSlider;
    private JLabel  sizeLabel;
    private JLabel  speedLabel;

    public WelcomeScreen() {
        setTitle("Sort Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);

        JPanel card = buildCard();
        root.add(card);

        setContentPane(root);
        setVisible(true);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fill(new RoundRectangle2D.Float(4, 6, getWidth() - 8, getHeight() - 8, 20, 20));
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 20, 20));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 460));
        card.setBorder(BorderFactory.createEmptyBorder(44, 44, 44, 44));

        // Title
        JLabel title = new JLabel("Sort Visualizer");
        title.setFont(new Font("SF Pro Display", Font.BOLD, 30));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Bubble Sort  vs  Insertion Sort");
        subtitle.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTE);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0xE9ECEF));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Array size
        JLabel sizeHead = makeLabel("Array Size", 13, TEXT_DARK, Font.BOLD);
        sizeLabel = makeLabel("50 elements", 12, TEXT_MUTE, Font.PLAIN);
        JPanel sizeRow = labelRow(sizeHead, sizeLabel);

        sizeSlider = new JSlider(10, 100, 50);
        styleSlider(sizeSlider);
        sizeSlider.addChangeListener(e ->
            sizeLabel.setText(sizeSlider.getValue() + " elements"));

        // Speed
        JLabel speedHead = makeLabel("Animation Speed", 13, TEXT_DARK, Font.BOLD);
        speedLabel = makeLabel("Medium", 12, TEXT_MUTE, Font.PLAIN);
        JPanel speedRow = labelRow(speedHead, speedLabel);

        speedSlider = new JSlider(1, 5, 3);
        styleSlider(speedSlider);
        speedSlider.setSnapToTicks(true);
        speedSlider.addChangeListener(e -> {
            String[] labels = {"", "Very Slow", "Slow", "Medium", "Fast", "Very Fast"};
            speedLabel.setText(labels[speedSlider.getValue()]);
        });

        // Visualize button
        JButton btn = buildButton("Visualize →");
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.addActionListener(e -> launch());

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));
        card.add(sep);
        card.add(Box.createVerticalStrut(28));
        card.add(sizeRow);
        card.add(Box.createVerticalStrut(8));
        card.add(sizeSlider);
        card.add(Box.createVerticalStrut(24));
        card.add(speedRow);
        card.add(Box.createVerticalStrut(8));
        card.add(speedSlider);
        card.add(Box.createVerticalStrut(36));
        card.add(btn);

        return card;
    }

    private void launch() {
        int size  = sizeSlider.getValue();
        int speed = speedSlider.getValue();
        // Map 1-5 to delay ms: very slow=300, slow=150, medium=60, fast=20, very fast=5
        int[] delays = {300, 150, 60, 20, 5};
        int delayMs = delays[speed - 1];

        int[] array = generateArray(size);
        dispose();
        new VisualizerFrame(array, delayMs);
    }

    private int[] generateArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = (int)(Math.random() * 95) + 5;
        return arr;
    }

    // ── Helpers ──────────────────────────────────────────────

    private JLabel makeLabel(String text, int size, Color color, int style) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SF Pro Text", style, size));
        l.setForeground(color);
        return l;
    }

    private JPanel labelRow(JLabel left, JLabel right) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private void styleSlider(JSlider s) {
        s.setOpaque(false);
        s.setForeground(ACCENT);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        UIManager.put("Slider.thumbColor", ACCENT);
    }

    private JButton buildButton(String text) {
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
                g2.setColor(hovered ? ACCENT.darker() : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SF Pro Text", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 48));
        btn.setMaximumSize(new Dimension(200, 48));
        return btn;
    }
}
