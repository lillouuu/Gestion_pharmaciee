package interfaces.produit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import entite.Medicament;
import entite.StockMedicament;
import entitebd.MedicamentBD;
import entitebd.StockBD;
import gestion.GestionProduit;

public class ModifierMedicamentFrame extends JFrame {
    private JTextField txtSearch;
    private JTable tableMedicaments;
    private DefaultTableModel tableModel;

    // ✅ REFACTORED: Suppression des champs de fournisseur
    private JTextField txtRef, txtNom;
    private JTextArea txtDescription;
    private JButton btnSearch, btnUpdate, btnCancel, btnRefresh, btnManageStock;

    private MedicamentBD medicamentBD;
    private StockBD stockBD;
    private GestionProduit gestionProduit;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private int currentRefMedicament = -1;

    public ModifierMedicamentFrame() {
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        gestionProduit = new GestionProduit();
        initComponents();
        loadMedicaments();
    }

    private void initComponents() {
        setTitle("Modifier un Médicament");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(255, 165, 0));
        topPanel.setPreferredSize(new Dimension(1100, 50));

        JLabel titleLabel = new JLabel("✏ Modifier un médicament");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel de recherche et tableau (gauche)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // Recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Rechercher:"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> searchMedicament());
        searchPanel.add(txtSearch);

        btnSearch = new JButton("🔍");
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> searchMedicament());
        searchPanel.add(btnSearch);

        btnRefresh = new JButton("🔄");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadMedicaments());
        searchPanel.add(btnRefresh);

        leftPanel.add(searchPanel, BorderLayout.NORTH);

        // Tableau - ✅ REFACTORED: Suppression de la colonne Fournisseur
        String[] columns = {"Réf", "Nom", "Nb Lots", "Stock Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableMedicaments = new JTable(tableModel);
        tableMedicaments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMedicaments.setRowHeight(25);
        tableMedicaments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableMedicaments.getSelectedRow() != -1) {
                loadSelectedMedicament();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableMedicaments);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel formulaire (droite)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Section Médicament
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblMed = new JLabel("📋 Informations du médicament");
        lblMed.setFont(new Font("Arial", Font.BOLD, 12));
        lblMed.setForeground(new Color(255, 165, 0));
        formPanel.add(lblMed, gbc);
        gbc.gridwidth = 1;

        // Référence (non modifiable)
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Référence:"), gbc);
        gbc.gridx = 1;
        txtRef = new JTextField(20);
        txtRef.setEditable(false);
        txtRef.setBackground(new Color(240, 240, 240));
        formPanel.add(txtRef, gbc);

        // Nom
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Nom *:"), gbc);
        gbc.gridx = 1;
        txtNom = new JTextField(20);
        formPanel.add(txtNom, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        formPanel.add(scrollDesc, gbc);
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;


        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 1;

        rightPanel.add(formPanel, BorderLayout.CENTER);

        // Boutons
        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 5, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnUpdate = new JButton("💾 Modifier Médicament");
        btnUpdate.setBackground(new Color(255, 165, 0));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> updateMedicament());

        btnManageStock = new JButton("📦 Gérer les Stocks");
        btnManageStock.setBackground(new Color(0, 123, 255));
        btnManageStock.setForeground(Color.WHITE);
        btnManageStock.setFocusPainted(false);
        btnManageStock.setEnabled(false);
        btnManageStock.addActionListener(e -> manageStock());

        btnCancel = new JButton("❌ Fermer");
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnUpdate);
        btnPanel.add(btnManageStock);
        btnPanel.add(new JLabel("")); // Spacer
        btnPanel.add(btnCancel);

        rightPanel.add(btnPanel, BorderLayout.SOUTH);

        // Ajouter les panels au frame
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(600);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadMedicaments() {
        tableModel.setRowCount(0);
        try {
            List<Medicament> medicaments = medicamentBD.listerTous();
            for (Medicament med : medicaments) {
                // ✅ REFACTORED: Obtenir tous les stocks (lots multiples)
                List<StockMedicament> stocks = stockBD.getStocksParExpiration(med.getRefMedicament());
                int nbLots = stocks != null ? stocks.size() : 0;
                int stockTotal = 0;

                if (stocks != null) {
                    for (StockMedicament stock : stocks) {
                        stockTotal += stock.getQuantiteProduit();
                    }
                }

                tableModel.addRow(new Object[]{
                        med.getRefMedicament(),
                        med.getNom(),
                        nbLots + " lot(s)",
                        stockTotal + " unités"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchMedicament() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            loadMedicaments();
            return;
        }

        tableModel.setRowCount(0);
        try {
            List<Medicament> medicaments = medicamentBD.rechercherParNom(searchTerm);
            for (Medicament med : medicaments) {
                List<StockMedicament> stocks = stockBD.getStocksParExpiration(med.getRefMedicament());
                int nbLots = stocks != null ? stocks.size() : 0;
                int stockTotal = 0;

                if (stocks != null) {
                    for (StockMedicament stock : stocks) {
                        stockTotal += stock.getQuantiteProduit();
                    }
                }

                tableModel.addRow(new Object[]{
                        med.getRefMedicament(),
                        med.getNom(),
                        nbLots + " lot(s)",
                        stockTotal + " unités"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la recherche: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedMedicament() {
        int selectedRow = tableMedicaments.getSelectedRow();
        if (selectedRow == -1) return;

        try {
            currentRefMedicament = (int) tableModel.getValueAt(selectedRow, 0);
            Medicament med = medicamentBD.rechercherParRef(currentRefMedicament);

            if (med != null) {
                txtRef.setText(String.valueOf(med.getRefMedicament()));
                txtNom.setText(med.getNom());
                txtDescription.setText(med.getDescriptio() != null ? med.getDescriptio() : "");

                btnUpdate.setEnabled(true);
                btnManageStock.setEnabled(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMedicament() {
        if (currentRefMedicament == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un médicament!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // ✅ REFACTORED: Modifier uniquement les informations du médicament (sans fournisseur)
            Medicament med = new Medicament();
            med.setRefMedicament(currentRefMedicament);
            med.setNom(txtNom.getText().trim());
            med.setDescriptio(txtDescription.getText().trim());

            // Utiliser GestionProduit pour modifier
            boolean updated = gestionProduit.modifierMedicament(med);

            if (updated) {
                JOptionPane.showMessageDialog(this,
                        "✅ Médicament modifié avec succès!\n\n" +
                                "ℹ️ Pour modifier les stocks, utilisez le bouton 'Gérer les Stocks'.",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                loadMedicaments();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void manageStock() {
        if (currentRefMedicament == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un médicament!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Medicament med = medicamentBD.rechercherParRef(currentRefMedicament);
            List<StockMedicament> stocks = stockBD.getStocksParExpiration(currentRefMedicament);

            StringBuilder info = new StringBuilder();
            info.append("═══════════════════════════════════════\n");
            info.append("  GESTION DES STOCKS\n");
            info.append("═══════════════════════════════════════\n\n");
            info.append("Médicament: ").append(med.getNom()).append("\n");
            info.append("Référence: ").append(currentRefMedicament).append("\n\n");

            if (stocks == null || stocks.isEmpty()) {
                info.append("⚠️ Aucun stock disponible.\n");
                info.append("Les stocks seront créés lors de la réception des commandes.\n");
            } else {
                info.append("Nombre de lots: ").append(stocks.size()).append("\n\n");
                info.append("DÉTAILS DES LOTS:\n");
                info.append("─────────────────────────────────────\n");

                for (int i = 0; i < stocks.size(); i++) {
                    StockMedicament stock = stocks.get(i);
                    info.append("\nLot #").append(i + 1).append(" (Stock #").append(stock.getNumStock()).append(")\n");
                    info.append("  Quantité: ").append(stock.getQuantiteProduit()).append(" unités\n");
                    info.append("  Date fab: ").append(dateFormat.format(stock.getDateFabrication())).append("\n");
                    info.append("  Date exp: ").append(dateFormat.format(stock.getDateExpiration())).append("\n");
                    info.append("  Prix achat: ").append(String.format("%.2f DT", stock.getPrixAchat())).append("\n");
                    info.append("  Prix vente: ").append(String.format("%.2f DT", stock.getPrixVente())).append("\n");

                    if (stock.estPerime()) {
                        info.append("  ⚠️ PÉRIMÉ\n");
                    } else if (stock.Alerte()) {
                        info.append("  ⚠️ ALERTE STOCK FAIBLE\n");
                    }
                }
            }

            JOptionPane.showMessageDialog(this,
                    info.toString(),
                    "Stocks - " + med.getNom(),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}