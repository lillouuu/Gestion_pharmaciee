package interfaces;

import javax.swing.*;
import java.awt.*;
import entite.Employe;
import entitebd.EmployeBD;
import exception.AuthentificationException;


public class LoginFrame extends JFrame {
    private JTextField cnssField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private EmployeBD employeBD;

    public LoginFrame() {
        employeBD = new EmployeBD();
        initComponents();
    }

    private void initComponents() {
        setTitle("Gestion Pharmacie - Connexion");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);


        JLabel titleLabel = new JLabel("Système de Gestion de Pharmacie");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel cnssLabel = new JLabel("Numéro CNSS:");
        mainPanel.add(cnssLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        cnssField = new JTextField(15);
        mainPanel.add(cnssField, gbc);


        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("Mot de passe:");
        mainPanel.add(passwordLabel, gbc);


        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(15);
        mainPanel.add(passwordField, gbc);


        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton = new JButton("Se connecter");
        loginButton.setBackground(new Color(0, 153, 76));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(150, 35));
        loginButton.addActionListener(e -> handleLogin());
        mainPanel.add(loginButton, gbc);

        add(mainPanel, BorderLayout.CENTER);


        passwordField.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String cnssText = cnssField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (cnssText.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez remplir tous les champs",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {

            int cnss;
            try {
                cnss = Integer.parseInt(cnssText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Le numéro CNSS doit être un nombre valide",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Employe employe = employeBD.authentifier(cnss, password);

            if (employe == null) {
                throw new AuthentificationException("Identifiants incorrects", String.valueOf(cnss));
            }


            JOptionPane.showMessageDialog(this,
                    "Bienvenue " + employe.getPrenom() + " " + employe.getNom() + " !",
                    "Connexion réussie",
                    JOptionPane.INFORMATION_MESSAGE);


            this.dispose();
            new MainFrame(employe).setVisible(true);

        } catch (AuthentificationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erreur d'authentification",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de connexion à la base de données: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}