package interfaces.client;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.Client;
import gestion.GestionClient;

public class GestionFideliteFrame extends JFrame {
    private JTextField txtNumClient, txtNom, txtPoints, txtPointsAjust;
    private JRadioButton rbAjouter, rbUtiliser;
    private JButton btnSearch, btnAjuster, btnClose;
    private JLabel lblReduction;
    private GestionClient gestionClient;
    private int currentNumClient = -1;

    public GestionFideliteFrame() {
        gestionClient = new GestionClient();
        initComponents();
    }

    private void initComponents() {
        setTitle("Gestion de la Fidélité");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(255, 193, 7));
        topPanel.setPreferredSize(new Dimension(600, 50));

        JLabel titleLabel = new JLabel("⭐ Gestion de la Fidélité Client");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Section recherche
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblSearch = new JLabel("🔍 Rechercher le client");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 13));
        lblSearch.setForeground(new Color(255, 193, 7));
        mainPanel.add(lblSearch, gbc);
        gbc.gridwidth = 1;

        // Numéro client
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("N° Client:"), gbc);
        gbc.gridx = 1;
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        txtNumClient = new JTextField(15);
        txtNumClient.addActionListener(e -> searchClient());
        searchPanel.add(txtNumClient, BorderLayout.CENTER);
        btnSearch = new JButton("🔍");
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> searchClient());
        searchPanel.add(btnSearch, BorderLayout.EAST);
        mainPanel.add(searchPanel, gbc);

        // Nom (lecture seule)
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Nom complet:"), gbc);
        gbc.gridx = 1;
        txtNom = new JTextField(15);
        txtNom.setEditable(false);
        txtNom.setBackground(new Color(240, 240, 240));
        mainPanel.add(txtNom, gbc);

        // Points actuels (lecture seule)
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Points actuels:"), gbc);
        gbc.gridx = 1;
        txtPoints = new JTextField(15);
        txtPoints.setEditable(false);
        txtPoints.setBackground(new Color(240, 240, 240));
        txtPoints.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(txtPoints, gbc);

        // Séparateur
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        mainPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // Section ajustement
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JLabel lblAjust = new JLabel("📊 Ajustement des points");
        lblAjust.setFont(new Font("Arial", Font.BOLD, 13));
        lblAjust.setForeground(new Color(255, 193, 7));
        mainPanel.add(lblAjust, gbc);
        gbc.gridwidth = 1;

        // Type d'opération
        gbc.gridx = 0; gbc.gridy = 6;
        mainPanel.add(new JLabel("Opération:"), gbc);
        gbc.gridx = 1;
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rbAjouter = new JRadioButton("➕ Ajouter", true);
        rbUtiliser = new JRadioButton("➖ Utiliser");
        rbUtiliser.addActionListener(e -> updateReduction());
        rbAjouter.addActionListener(e -> updateReduction());
        ButtonGroup group = new ButtonGroup();
        group.add(rbAjouter);
        group.add(rbUtiliser);
        typePanel.add(rbAjouter);
        typePanel.add(rbUtiliser);
        mainPanel.add(typePanel, gbc);

        // Nombre de points
        gbc.gridx = 0; gbc.gridy = 7;
        mainPanel.add(new JLabel("Nombre de points:"), gbc);
        gbc.gridx = 1;
        txtPointsAjust = new JTextField(15);
        txtPointsAjust.addCaretListener(e -> updateReduction());
        mainPanel.add(txtPointsAjust, gbc);

        // Réduction équivalente
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        lblReduction = new JLabel("");
        lblReduction.setFont(new Font("Arial", Font.ITALIC, 12));
        lblReduction.setForeground(new Color(34, 139, 34));
        lblReduction.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblReduction, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnAjuster = new JButton("✓ Valider");
        btnAjuster.setPreferredSize(new Dimension(120, 35));
        btnAjuster.setBackground(new Color(255, 193, 7));
        btnAjuster.setForeground(Color.WHITE);
        btnAjuster.setFocusPainted(false);
        btnAjuster.setEnabled(false);
        btnAjuster.addActionListener(e -> ajusterPoints());

        btnClose = new JButton("❌ Fermer");
        btnClose.setPreferredSize(new Dimension(120, 35));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnAjuster);
        btnPanel.add(btnClose);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void searchClient() {
        String numStr = txtNumClient.getText().trim();
        if (numStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir un numéro de client!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int numClient = Integer.parseInt(numStr);
            Client client = gestionClient.rechercherParId(numClient);

            if (client == null) {
                JOptionPane.showMessageDialog(this,
                        "Aucun client trouvé avec le numéro: " + numClient,
                        "Recherche",
                        JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                return;
            }

            // Charger les infos
            currentNumClient = numClient;
            txtNom.setText(client.getPrenom() + " " + client.getNom());
            txtPoints.setText(String.valueOf(client.getPointFidelite()));
            btnAjuster.setEnabled(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Le numéro de client doit être un nombre!",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de base de données: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateReduction() {
        try {
            String pointsStr = txtPointsAjust.getText().trim();
            if (!pointsStr.isEmpty()) {
                int points = Integer.parseInt(pointsStr);
                if (rbUtiliser.isSelected()) {
                    double reduction = gestionClient.calculerReduction(points, 0.1);
                    lblReduction.setText("💰 Réduction: " + String.format("%.2f DT", reduction));
                } else {
                    lblReduction.setText("");
                }
            } else {
                lblReduction.setText("");
            }
        } catch (NumberFormatException ex) {
            lblReduction.setText("");
        }
    }

    private void ajusterPoints() {
        if (currentNumClient == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez d'abord rechercher un client!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pointsStr = txtPointsAjust.getText().trim();
        if (pointsStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir un nombre de points!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int points = Integer.parseInt(pointsStr);
            if (points <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Le nombre de points doit être positif!",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String operation = rbAjouter.isSelected() ? "ajouter" : "utiliser";

            // Confirmer
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Confirmer l'opération?\n\n" +
                            "Client: " + txtNom.getText() + "\n" +
                            "Opération: " + operation.toUpperCase() + " " + points + " point(s)\n" +
                            (rbUtiliser.isSelected() ? "Réduction: " + String.format("%.2f DT",
                                    gestionClient.calculerReduction(points, 0.1)) : ""),
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Effectuer l'opération
            if (rbAjouter.isSelected()) {
                gestionClient.ajouterPoints(currentNumClient, points);
            } else {
                gestionClient.utiliserPoints(currentNumClient, points);
            }

            // Actualiser
            searchClient();
            txtPointsAjust.setText("");
            lblReduction.setText("");

            JOptionPane.showMessageDialog(this,
                    "✓ Opération effectuée avec succès!",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Le nombre de points doit être un entier!",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de base de données: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        currentNumClient = -1;
        txtNom.setText("");
        txtPoints.setText("");
        btnAjuster.setEnabled(false);
        lblReduction.setText("");
    }
}