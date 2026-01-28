package interfaces;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import entite.Employe;
import entitebd.EmployeBD;
import exception.AuthentificationException;

public class LoginFrame extends JFrame {
    private JTextField cnssField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JCheckBox showPasswordCheckbox;
    private EmployeBD employeBD;

    // Modern 2026 Color Palette
    private static final Color PRIMARY = new Color(99, 102, 241);      // Indigo
    private static final Color PRIMARY_DARK = new Color(79, 70, 229);  // Darker Indigo
    private static final Color SECONDARY = new Color(236, 72, 153);    // Pink
    private static final Color BACKGROUND = new Color(248, 250, 252);  // Light Gray
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);   // Dark Slate
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139); // Slate Gray
    private static final Color SUCCESS = new Color(34, 197, 94);       // Green
    private static final Color ERROR = new Color(239, 68, 68);         // Red

    public LoginFrame() {
        employeBD = new EmployeBD();
        initComponents();
    }

    private void initComponents() {
        setTitle("Pharmacy Ben Abdallah");
        setSize(500, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with gradient background
        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Login card
        JPanel loginCard = createLoginCard();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        mainPanel.add(loginCard, gbc);

        add(mainPanel);
    }

    private JPanel createLoginCard() {
        JPanel card = new RoundedPanel(30, CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(420, 550));

        // Add shadow effect
        card.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                new EmptyBorder(40, 50, 40, 50)
        ));

        // Logo/Icon
        // Logo/Icon
        JLabel iconLabel = new JLabel("💊");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 35)); // smaller size
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(20)); // increase vertical spacing to move it lower


        // Title
        JLabel titleLabel = new JLabel(" Pharmacy ben Abdallah");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));

        // Subtitle
        JLabel subtitleLabel = new JLabel("Pharmacy Management System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(35));

        // CNSS Field
        JLabel cnssLabel = new JLabel("CNSS Number");
        cnssLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cnssLabel.setForeground(TEXT_PRIMARY);
        cnssLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(cnssLabel);
        card.add(Box.createVerticalStrut(8));

        cnssField = new ModernTextField();
        cnssField.setMaximumSize(new Dimension(320, 45));
        cnssField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(cnssField);
        card.add(Box.createVerticalStrut(20));

        // Password Field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passwordLabel.setForeground(TEXT_PRIMARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passwordLabel);
        card.add(Box.createVerticalStrut(8));

        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
        passwordPanel.setOpaque(false);
        passwordPanel.setMaximumSize(new Dimension(320, 45));
        passwordPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new ModernPasswordField();
        passwordField.setMaximumSize(new Dimension(280, 45));
        passwordPanel.add(passwordField);
        passwordPanel.add(Box.createHorizontalStrut(5));

        JButton togglePasswordBtn = new JButton("👁");
        togglePasswordBtn.setPreferredSize(new Dimension(35, 45));
        togglePasswordBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        togglePasswordBtn.setFocusPainted(false);
        togglePasswordBtn.setBorderPainted(false);
        togglePasswordBtn.setBackground(new Color(241, 245, 249));
        togglePasswordBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        togglePasswordBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == (char) 0) {
                passwordField.setEchoChar('•');
                togglePasswordBtn.setText("👁");
            } else {
                passwordField.setEchoChar((char) 0);
                togglePasswordBtn.setText("🙈");
            }
        });
        passwordPanel.add(togglePasswordBtn);

        card.add(passwordPanel);
        card.add(Box.createVerticalStrut(30));

        // Login Button
        loginButton = new ModernButton("Sign In", PRIMARY);
        loginButton.setMaximumSize(new Dimension(320, 50));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());
        card.add(loginButton);
        card.add(Box.createVerticalStrut(15));

        // Info text
        JLabel infoLabel = new JLabel("Secure pharmacy management platform");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(infoLabel);

        // Enter key support
        passwordField.addActionListener(e -> handleLogin());
        cnssField.addActionListener(e -> passwordField.requestFocus());

        return card;
    }

    private void handleLogin() {
        String cnssText = cnssField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (cnssText.isEmpty() || password.isEmpty()) {
            showModernDialog("Please fill in all fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int cnss;
            try {
                cnss = Integer.parseInt(cnssText);
            } catch (NumberFormatException e) {
                showModernDialog("CNSS number must be a valid number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Show loading state
            loginButton.setEnabled(false);
            loginButton.setText("Signing in...");

            SwingWorker<Employe, Void> worker = new SwingWorker<>() {
                @Override
                protected Employe doInBackground() throws Exception {
                    return employeBD.authentifier(cnss, password);
                }

                @Override
                protected void done() {
                    try {
                        Employe employe = get();
                        if (employe == null) {
                            throw new AuthentificationException("Invalid credentials", String.valueOf(cnss));
                        }

                        // Success animation
                        dispose();
                        new MainFrame(employe).setVisible(true);

                    } catch (Exception ex) {
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In");

                        if (ex.getCause() instanceof AuthentificationException) {
                            showModernDialog("Invalid CNSS number or password", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                        } else {
                            showModernDialog("Database connection error: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            };
            worker.execute();

        } catch (Exception ex) {
            loginButton.setEnabled(true);
            loginButton.setText("Sign In");
            showModernDialog("An unexpected error occurred", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showModernDialog(String message, String title, int messageType) {
        UIManager.put("OptionPane.background", CARD_BG);
        UIManager.put("Panel.background", CARD_BG);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    // Custom Components

    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth(), h = getHeight();

            GradientPaint gp = new GradientPaint(0, 0, new Color(238, 242, 255),
                    0, h, new Color(219, 234, 254));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
    }

    class RoundedPanel extends JPanel {
        private int radius;
        private Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
    }

    class ModernTextField extends JTextField {
        public ModernTextField() {
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 2, true),
                    new EmptyBorder(10, 15, 10, 15)
            ));
            setBackground(new Color(248, 250, 252));
            setForeground(TEXT_PRIMARY);
            setCaretColor(PRIMARY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            super.paintComponent(g);
        }
    }

    class ModernPasswordField extends JPasswordField {
        public ModernPasswordField() {
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 2, true),
                    new EmptyBorder(10, 15, 10, 15)
            ));
            setBackground(new Color(248, 250, 252));
            setForeground(TEXT_PRIMARY);
            setCaretColor(PRIMARY);
            setEchoChar('•');
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            super.paintComponent(g);
        }
    }

    class ModernButton extends JButton {
        private Color baseColor;

        public ModernButton(String text, Color color) {
            super(text);
            this.baseColor = color;
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        baseColor = PRIMARY_DARK;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    baseColor = PRIMARY;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isEnabled()) {
                g2.setColor(baseColor);
            } else {
                g2.setColor(new Color(203, 213, 225));
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            super.paintComponent(g);
        }
    }

    class ShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color[] shadowColors = {
                    new Color(0, 0, 0, 10),
                    new Color(0, 0, 0, 8),
                    new Color(0, 0, 0, 6),
                    new Color(0, 0, 0, 4),
                    new Color(0, 0, 0, 2)
            };

            for (int i = 0; i < shadowColors.length; i++) {
                g2.setColor(shadowColors[i]);
                g2.drawRoundRect(x + i, y + i, width - 2 * i - 1, height - 2 * i - 1, 30, 30);
            }
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(5, 5, 5, 5);
        }
    }

    public static void main(String[] args) {
        /*try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });*/

            try {
                UIManager.put("Button.background", new Color(0x718bbc));
                UIManager.put("Button.foreground", Color.WHITE);
                UIManager.put("Button.font", new Font("Arial", Font.BOLD, 16));
                UIManager.put("Button.border", BorderFactory.createEmptyBorder(10, 20, 10, 20));
                UIManager.put("Button.focus", new Color(0,0,0,0));
            } catch (Exception e) {
                e.printStackTrace();
            }

            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));


    }
}