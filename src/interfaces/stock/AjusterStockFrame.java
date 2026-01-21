package interfaces.stock;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.Medicament;
import entite.StockMedicament;
import entitebd.MedicamentBD;
import entitebd.StockBD;
import gestion.GestionStock;
import exception.ProduitNonTrouveException;
import exception.StockInsuffisantException;

public class AjusterStockFrame extends JFrame {
    private JTextField txtRef, txtNom, txtQuantiteActuelle, txtQuantiteAjust, txtMotif;
    private JRadioButton rbAugmenter, rbDiminuer;
    private JButton btnSearch, btnAjuster, btnCancel;
    private JLabel lblSeuilInfo, lblStatus;
    private GestionStock gestionStock;
    private MedicamentBD medicamentBD;
    private StockBD stockBD;
    private int currentRefMedicament = -1;

    public AjusterStockFrame() {
        gestionStock = new GestionStock();
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        initComponents();
    }

    private void initComponents() {
        setTitle("Ajuster le Stock");
        setSize(600, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(255, 165, 0));
        topPanel.setPreferredSize(new Dimension(600, 50));

        JLabel titleLabel = new JLabel("📦 Ajuster le stock d'un médicament");
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
        JLabel lblSearch = new JLabel("🔍 Rechercher le médicament");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 13));
        lblSearch.setForeground(new Color(255, 165, 0));
        mainPanel.add(lblSearch, gbc);
        gbc.gridwidth = 1;

        // Référence
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Référence:"), gbc);
        gbc.gridx = 1;
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        txtRef = new JTextField(15);
        txtRef.addActionListener(e -> searchMedicament());
        searchPanel.add(txtRef, BorderLayout.CENTER);
        btnSearch = new JButton("🔍");
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> searchMedicament());
        searchPanel.add(btnSearch, BorderLayout.EAST);
        mainPanel.add(searchPanel, gbc);

        // Nom (lecture seule)
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1;
        txtNom = new JTextField(15);
        txtNom.setEditable(false);
        txtNom.setBackground(new Color(240, 240, 240));
        mainPanel.add(txtNom, gbc);

        // Quantité actuelle (lecture seule)
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Stock actuel:"), gbc);
        gbc.gridx = 1;
        txtQuantiteActuelle = new JTextField(15);
        txtQuantiteActuelle.setEditable(false);
        txtQuantiteActuelle.setBackground(new Color(240, 240, 240));
        txtQuantiteActuelle.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(txtQuantiteActuelle, gbc);

        // Info seuil
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        lblSeuilInfo = new JLabel("");
        lblSeuilInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        mainPanel.add(lblSeuilInfo, gbc);
        gbc.gridwidth = 1;

        // Séparateur
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        mainPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // Section ajustement
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JLabel lblAjust = new JLabel("📊 Ajustement");
        lblAjust.setFont(new Font("Arial", Font.BOLD, 13));
        lblAjust.setForeground(new Color(255, 165, 0));
        mainPanel.add(lblAjust, gbc);
        gbc.gridwidth = 1;

        // Type d'ajustement
        gbc.gridx = 0; gbc.gridy = 7;
        mainPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rbAugmenter = new JRadioButton("➕ Augmenter", true);
        rbDiminuer = new JRadioButton("➖ Diminuer");
        ButtonGroup group = new ButtonGroup();
        group.add(rbAugmenter);
        group.add(rbDiminuer);
        typePanel.add(rbAugmenter);
        typePanel.add(rbDiminuer);
        mainPanel.add(typePanel, gbc);

        // Quantité d'ajustement
        gbc.gridx = 0; gbc.gridy = 8;
        mainPanel.add(new JLabel("Quantité:"), gbc);
        gbc.gridx = 1;
        txtQuantiteAjust = new JTextField(15);
        mainPanel.add(txtQuantiteAjust, gbc);

        // Motif
        gbc.gridx = 0; gbc.gridy = 9;
        mainPanel.add(new JLabel("Motif:"), gbc);
        gbc.gridx = 1;
        txtMotif = new JTextField(15);
        txtMotif.setToolTipText("Ex: Réception, Péremption, Inventaire, etc.");
        mainPanel.add(txtMotif, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        lblStatus = new JLabel("");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 12));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblStatus, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnAjuster = new JButton("✓ Ajuster");
        btnAjuster.setPreferredSize(new Dimension(120, 35));
        btnAjuster.setBackground(new Color(255, 165, 0));
        btnAjuster.setForeground(Color.WHITE);
        btnAjuster.setFocusPainted(false);
        btnAjuster.setEnabled(false);
        btnAjuster.addActionListener(e -> ajusterStock());

        btnCancel = new JButton("❌ Annuler");
        btnCancel.setPreferredSize(new Dimension(120, 35));
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnAjuster);
        btnPanel.add(btnCancel);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void searchMedicament() {
        String refStr = txtRef.getText().trim();
        if (refStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir une référence!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int ref = Integer.parseInt(refStr);
            Medicament med = medicamentBD.rechercherParRef(ref);

            if (med == null) {
                JOptionPane.showMessageDialog(this,
                        "Aucun médicament trouvé avec la référence: " + ref,
                        "Recherche",
                        JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                return;
            }

            StockMedicament stock = stockBD.rechercherParRef(ref);
            if (stock == null) {
                JOptionPane.showMessageDialog(this,
                        "Aucune information de stock pour ce médicament!",
                        "Information",
                        JOptionPane.WARNING_MESSAGE);
                clearForm();
                return;
            }

            // Charger les infos
            currentRefMedicament = ref;
            txtNom.setText(med.getNom());
            txtQuantiteActuelle.setText(String.valueOf(stock.getQuantiteProduit()));

            // Info seuil
            String seuilText = "Seuil minimal: " + stock.getSeuilMin();
            if (stock.Alerte()) {
                lblSeuilInfo.setText("⚠ " + seuilText + " - STOCK FAIBLE!");
                lblSeuilInfo.setForeground(new Color(220, 53, 69));
            } else {
                lblSeuilInfo.setText("✓ " + seuilText);
                lblSeuilInfo.setForeground(new Color(40, 167, 69));
            }

            btnAjuster.setEnabled(true);
            lblStatus.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "La référence doit être un nombre!",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de base de données: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajusterStock() {
        if (currentRefMedicament == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez d'abord rechercher un médicament!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qteStr = txtQuantiteAjust.getText().trim();
        if (qteStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir une quantité!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int quantite = Integer.parseInt(qteStr);
            if (quantite <= 0) {
                JOptionPane.showMessageDialog(this,
                        "La quantité doit être positive!",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String motif = txtMotif.getText().trim();
            if (motif.isEmpty()) {
                motif = "Ajustement manuel";
            }

            // Confirmer l'opération
            String action = rbAugmenter.isSelected() ? "augmenter" : "diminuer";
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Confirmer l'ajustement?\n\n" +
                            "Médicament: " + txtNom.getText() + "\n" +
                            "Action: " + action.toUpperCase() + " de " + quantite + " unité(s)\n" +
                            "Motif: " + motif,
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Effectuer l'ajustement
            if (rbAugmenter.isSelected()) {
                gestionStock.augmenterStock(currentRefMedicament, quantite);
                lblStatus.setText("✓ Stock augmenté de " + quantite + " unité(s)");
                lblStatus.setForeground(new Color(40, 167, 69));
            } else {
                gestionStock.diminuerStock(currentRefMedicament, quantite);
                lblStatus.setText("✓ Stock diminué de " + quantite + " unité(s)");
                lblStatus.setForeground(new Color(220, 53, 69));
            }

            // Actualiser l'affichage
            searchMedicament();
            txtQuantiteAjust.setText("");
            txtMotif.setText("");

            JOptionPane.showMessageDialog(this,
                    "✓ Ajustement effectué avec succès!\n" +
                            "Motif: " + motif,
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "La quantité doit être un nombre entier!",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (StockInsuffisantException ex) {
            JOptionPane.showMessageDialog(this,
                    "Stock insuffisant!\n\n" +
                            "Quantité demandée: " + ex.getQuantiteDemandee() + "\n" +
                            "Quantité disponible: " + ex.getQuantiteDisponible(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (ProduitNonTrouveException ex) {
            JOptionPane.showMessageDialog(this,
                    "Produit non trouvé: " + ex.getReference(),
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
        currentRefMedicament = -1;
        txtNom.setText("");
        txtQuantiteActuelle.setText("");
        lblSeuilInfo.setText("");
        lblStatus.setText("");
        btnAjuster.setEnabled(false);
    }
}
