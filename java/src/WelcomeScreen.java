import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class WelcomeScreen extends JFrame {

    // ── Palette ─────────────────────────────────────────────
    static final Color BG        = new Color(0x0B0C10);
    static final Color CARD      = new Color(0x13151D);
    static final Color BORDER    = new Color(0x252836);
    static final Color BLUE      = new Color(0x4A6CF7);
    static final Color PURPLE    = new Color(0x7B4FEF);
    static final Color GREEN     = new Color(0x00E5A0);
    static final Color RED       = new Color(0xF06078);
    static final Color TEXT_H    = new Color(0xF1F5F9);
    static final Color TEXT_BODY = new Color(0x94A3B8);
    static final Color TEXT_MUTE = new Color(0x4B5563);

    private JSlider sizeSlider;
    private JSlider speedSlider;
    private JLabel  sizeVal;
    private JLabel  speedVal;

    public WelcomeScreen() {
        setTitle("Sort Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(540, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(false);

        setContentPane(new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g2.setColor(BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Dot grid
                g2.setColor(new Color(255, 255, 255, 14));
                for (int x = 20; x < getWidth(); x += 28) {
                    for (int y = 20; y < getHeight(); y += 28) {
                        g2.fillOval(x, y, 2, 2);
                    }
                }
                // Glow blob top-center
                RadialGradientPaint glow = new RadialGradientPaint(
                    getWidth() / 2f, 0,
                    getWidth() * 0.6f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0x4A6CF7, true).darker(), BG}
                );
                // Override with simpler gradient since RadialGradient needs Color not alpha
                g2.setColor(new Color(74, 108, 247, 30));
                g2.fillOval(getWidth()/2 - 200, -120, 400, 280);
                g2.dispose();
            }
        });
        getContentPane().setLayout(new GridBagLayout());

        getContentPane().add(buildCard());
        setVisible(true);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card background
                g2.setColor(CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                // Border
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 20, 20));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(440, 490));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(44, 44, 44, 44));

        // ── Header ───────────────────────────────────────────
        JLabel title = label("Sort Visualizer", 32, Font.BOLD, TEXT_H);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = label("Bubble  ·  Insertion  ·  Side by Side", 13, Font.PLAIN, TEXT_BODY);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        // Gradient accent bar under title
        JPanel accentBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BLUE, getWidth(), 0, PURPLE);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 3, 3));
                g2.dispose();
            }
        };
        accentBar.setOpaque(false);
        accentBar.setMaximumSize(new Dimension(60, 3));
        accentBar.setPreferredSize(new Dimension(60, 3));
        accentBar.setAlignmentX(CENTER_ALIGNMENT);

        // ── Array Size ───────────────────────────────────────
        sizeVal = label("50", 13, Font.PLAIN, BLUE);
        JPanel sizeRow = labelRow("Array Size", sizeVal);

        sizeSlider = slider(10, 100, 50);
        sizeSlider.addChangeListener(e -> sizeVal.setText(sizeSlider.getValue() + ""));

        // ── Speed ────────────────────────────────────────────
        String[] speedNames = {"", "Very Slow", "Slow", "Medium", "Fast", "Very Fast"};
        speedVal = label("Medium", 13, Font.PLAIN, BLUE);
        JPanel speedRow = labelRow("Animation Speed", speedVal);

        speedSlider = slider(1, 5, 3);
        speedSlider.setSnapToTicks(true);
        speedSlider.addChangeListener(e -> speedVal.setText(speedNames[speedSlider.getValue()]));

        // ── Button ───────────────────────────────────────────
        JButton btn = buildButton();
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.addActionListener(e -> launch());

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(sub);
        card.add(Box.createVerticalStrut(14));
        card.add(accentBar);
        card.add(Box.createVerticalStrut(36));
        card.add(sizeRow);
        card.add(Box.createVerticalStrut(10));
        card.add(sizeSlider);
        card.add(Box.createVerticalStrut(30));
        card.add(speedRow);
        card.add(Box.createVerticalStrut(10));
        card.add(speedSlider);
        card.add(Box.createVerticalStrut(40));
        card.add(btn);
        return card;
    }

    private void launch() {
        int size = sizeSlider.getValue();
        int[] delays = {300, 120, 45, 14, 4};
        int delayMs = delays[speedSlider.getValue() - 1];
        int[] array = new int[size];
        for (int i = 0; i < size; i++) array[i] = (int)(Math.random() * 95) + 5;
        dispose();
        new VisualizerFrame(array, delayMs);
    }

    // ── Component helpers ────────────────────────────────────

    private JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(Font.SANS_SERIF, style, size));
        l.setForeground(color);
        return l;
    }

    private JPanel labelRow(String heading, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel left = label(heading, 13, Font.BOLD, TEXT_H);
        row.add(left, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JSlider slider(int min, int max, int val) {
        JSlider s = new JSlider(min, max, val) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int trackY = getHeight() / 2;
                int trackH = 4;
                // Track background
                g2.setColor(BORDER);
                g2.fill(new RoundRectangle2D.Float(0, trackY - trackH/2f, getWidth(), trackH, trackH, trackH));
                // Track fill
                float pct = (getValue() - getMinimum()) / (float)(getMaximum() - getMinimum());
                int fillW = (int)(getWidth() * pct);
                GradientPaint gp = new GradientPaint(0, 0, BLUE, fillW, 0, PURPLE);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, trackY - trackH/2f, fillW, trackH, trackH, trackH));
                // Thumb
                int thumbX = (int)(pct * (getWidth() - 16));
                g2.setColor(Color.WHITE);
                g2.fillOval(thumbX, trackY - 8, 16, 16);
                g2.setColor(BLUE);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(thumbX, trackY - 8, 16, 16);
                g2.dispose();
            }
        };
        s.setOpaque(false);
        s.setFocusable(false);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return s;
    }

    private JButton buildButton() {
        JButton btn = new JButton("Visualize  →") {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Glow under button
                if (hov) {
                    g2.setColor(new Color(74, 108, 247, 50));
                    g2.fill(new RoundRectangle2D.Float(-6, 4, getWidth()+12, getHeight()+4, 20, 20));
                }
                // Gradient fill
                GradientPaint gp = new GradientPaint(0, 0, hov ? BLUE.brighter() : BLUE, getWidth(), 0, PURPLE);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
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
        btn.setPreferredSize(new Dimension(220, 50));
        btn.setMaximumSize(new Dimension(220, 50));
        return btn;
    }
}
