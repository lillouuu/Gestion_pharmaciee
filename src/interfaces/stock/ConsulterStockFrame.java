package interfaces.stock;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import entite.Medicament;
import entite.StockMedicament;
import entitebd.MedicamentBD;
import entitebd.StockBD;
import gestion.GestionStock;

public class ConsulterStockFrame extends JFrame {
    private JTable tableStock;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtFilter;
    private JComboBox<String> cmbFilterType;
    private JLabel lblCount, lblValeur, lblAlertes;
    private JButton btnRefresh, btnClose, btnDetails;
    private JTextArea txtDetails;
    private MedicamentBD medicamentBD;
    private StockBD stockBD;
    private GestionStock gestionStock;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public ConsulterStockFrame() {
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        gestionStock = new GestionStock();
        initComponents();
        loadStock();
    }

    private void initComponents() {
        setTitle("Consulter le Stock");
        setSize(1400, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0, 123, 255));
        topPanel.setPreferredSize(new Dimension(1400, 50));

        JLabel titleLabel = new JLabel("👁 Consultation détaillée du stock");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal avec Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Panel gauche - Tableau
        JPanel leftPanel = createTablePanel();
        splitPane.setLeftComponent(leftPanel);

        // Panel droit - Détails
        JPanel rightPanel = createDetailsPanel();
        splitPane.setRightComponent(rightPanel);

        splitPane.setDividerLocation(1000);
        add(splitPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setPreferredSize(new Dimension(150, 35));
        btnRefresh.setBackground(new Color(0, 123, 255));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadStock());

        btnClose = new JButton("❌ Fermer");
        btnClose.setPreferredSize(new Dimension(150, 35));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnRefresh);
        btnPanel.add(btnClose);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // Panel filtrage
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtres"));

        filterPanel.add(new JLabel("Type:"));
        cmbFilterType = new JComboBox<>(new String[]{
                "Tous", "En stock", "Alertes", "Rupture", "Même médicament"
        });
        cmbFilterType.addActionListener(e -> applyFilter());
        filterPanel.add(cmbFilterType);

        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("Recherche:"));
        txtFilter = new JTextField(20);
        txtFilter.addCaretListener(e -> applyTextFilter());
        filterPanel.add(txtFilter);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Tableau avec TOUTES les colonnes importantes
        String[] columns = {
                "N° Stock", "Réf Med", "Nom Médicament", "Quantité",
                "Prix Achat", "Prix Vente", "Seuil Min", "Valeur Stock",
                "Date Fab", "Date Exp", "Statut"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 1 || columnIndex == 3 || columnIndex == 6) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        tableStock = new JTable(tableModel);
        tableStock.setAutoCreateRowSorter(true);
        tableStock.setRowHeight(25);
        tableStock.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Tailles des colonnes
        tableStock.getColumnModel().getColumn(0).setPreferredWidth(70);  // N° Stock
        tableStock.getColumnModel().getColumn(1).setPreferredWidth(60);  // Réf Med
        tableStock.getColumnModel().getColumn(2).setPreferredWidth(200); // Nom
        tableStock.getColumnModel().getColumn(3).setPreferredWidth(70);  // Quantité
        tableStock.getColumnModel().getColumn(4).setPreferredWidth(80);  // Prix Achat
        tableStock.getColumnModel().getColumn(5).setPreferredWidth(80);  // Prix Vente
        tableStock.getColumnModel().getColumn(6).setPreferredWidth(70);  // Seuil
        tableStock.getColumnModel().getColumn(7).setPreferredWidth(90);  // Valeur
        tableStock.getColumnModel().getColumn(8).setPreferredWidth(80);  // Date Fab
        tableStock.getColumnModel().getColumn(9).setPreferredWidth(80);  // Date Exp
        tableStock.getColumnModel().getColumn(10).setPreferredWidth(100); // Statut

        // Sorter pour le filtrage
        sorter = new TableRowSorter<>(tableModel);
        tableStock.setRowSorter(sorter);

        // Listener pour afficher les détails
        tableStock.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableStock.getSelectedRow() != -1) {
                showDetails();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableStock);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel statistiques
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        lblCount = new JLabel("Lignes de stock: 0");
        lblCount.setFont(new Font("Arial", Font.BOLD, 13));

        lblValeur = new JLabel("Valeur totale: 0.00 DT");
        lblValeur.setFont(new Font("Arial", Font.BOLD, 13));
        lblValeur.setHorizontalAlignment(SwingConstants.CENTER);

        lblAlertes = new JLabel("Alertes: 0");
        lblAlertes.setFont(new Font("Arial", Font.BOLD, 13));
        lblAlertes.setForeground(new Color(220, 53, 69));
        lblAlertes.setHorizontalAlignment(SwingConstants.RIGHT);

        statsPanel.add(lblCount);
        statsPanel.add(lblValeur);
        statsPanel.add(lblAlertes);

        panel.add(statsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Détails de la ligne de stock"));

        txtDetails = new JTextArea();
        txtDetails.setEditable(false);
        txtDetails.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetails.setBackground(new Color(245, 245, 245));
        txtDetails.setText("Sélectionnez une ligne de stock\npour voir les détails...");

        JScrollPane scrollPane = new JScrollPane(txtDetails);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadStock() {
        tableModel.setRowCount(0);
        int alertCount = 0;
        double valeurTotale = 0.0;

        try {
            // ✅ CHANGEMENT: Récupérer TOUTES les lignes de stock
            List<StockMedicament> stocks = stockBD.listerTous();

            for (StockMedicament stock : stocks) {
                // Récupérer les infos du médicament
                Medicament med = medicamentBD.rechercherParRef(stock.getRefMedicament());

                if (med != null) {
                    int quantite = stock.getQuantiteProduit();
                    double prixAchat = stock.getPrixAchat();
                    double prixVente = stock.getPrixVente();
                    double valeurStock = quantite * prixAchat;

                    valeurTotale += valeurStock;

                    // Déterminer le statut
                    String statut;
                    if (quantite == 0) {
                        statut = "❌ Vide";
                        alertCount++;
                    } else if (stock.Alerte()) {
                        statut = "⚠ Alerte";
                        alertCount++;
                    } else if (med.estPerime()) {
                        statut = "⚠ Périmé";
                        alertCount++;
                    } else {
                        statut = "✓ Normal";
                    }

                    tableModel.addRow(new Object[]{
                            stock.getNumStock(),              // N° Stock
                            stock.getRefMedicament(),         // Réf Med
                            med.getNom(),                     // Nom
                            quantite,                         // Quantité
                            String.format("%.2f DT", prixAchat),
                            String.format("%.2f DT", prixVente),
                            stock.getSeuilMin(),              // Seuil
                            String.format("%.2f DT", valeurStock),
                            dateFormat.format(med.getDateFabrication()),
                            dateFormat.format(med.getDateExpiration()),
                            statut
                    });
                }
            }

            lblCount.setText("Lignes de stock: " + stocks.size());
            lblValeur.setText(String.format("Valeur totale: %.2f DT", valeurTotale));
            lblAlertes.setText("Alertes: " + alertCount);

            if (alertCount > 0) {
                lblAlertes.setForeground(new Color(220, 53, 69));
            } else {
                lblAlertes.setForeground(new Color(40, 167, 69));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showDetails() {
        int selectedRow = tableStock.getSelectedRow();
        if (selectedRow == -1) return;

        try {
            // Récupérer le numéro de stock de la ligne sélectionnée
            int numStock = (Integer) tableModel.getValueAt(selectedRow, 0);
            int refMed = (Integer) tableModel.getValueAt(selectedRow, 1);

            StockMedicament stock = stockBD.rechercherParNumStock(numStock);
            Medicament med = medicamentBD.rechercherParRef(refMed);

            if (stock != null && med != null) {
                StringBuilder details = new StringBuilder();
                details.append("═══════════════════════════════════════\n");
                details.append("     DÉTAILS DE LA LIGNE DE STOCK\n");
                details.append("═══════════════════════════════════════\n\n");

                // Informations Stock
                details.append("📦 LIGNE DE STOCK\n");
                details.append("  • N° Stock        : ").append(stock.getNumStock()).append("\n");
                details.append("  • Quantité        : ").append(stock.getQuantiteProduit()).append(" unité(s)\n");
                details.append("  • Prix d'achat    : ").append(String.format("%.2f DT", stock.getPrixAchat())).append("\n");
                details.append("  • Prix de vente   : ").append(String.format("%.2f DT", stock.getPrixVente())).append("\n");
                details.append("  • Seuil minimal   : ").append(stock.getSeuilMin()).append(" unité(s)\n");
                details.append("  • Valeur stock    : ").append(String.format("%.2f DT",
                        stock.getQuantiteProduit() * stock.getPrixAchat())).append("\n");
                details.append("  • Marge unitaire  : ").append(String.format("%.2f DT",
                        stock.getPrixVente() - stock.getPrixAchat())).append("\n");
                details.append("  • Marge totale    : ").append(String.format("%.2f DT",
                        (stock.getPrixVente() - stock.getPrixAchat()) * stock.getQuantiteProduit())).append("\n\n");

                // Informations Médicament
                details.append("💊 MÉDICAMENT ASSOCIÉ\n");
                details.append("  • Référence       : ").append(med.getRefMedicament()).append("\n");
                details.append("  • Nom             : ").append(med.getNom()).append("\n");
                details.append("  • Fournisseur     : Fournisseur #").append(med.getNumFournisseur()).append("\n");
                details.append("  • Description     : ").append(
                        med.getDescriptio() != null ? med.getDescriptio() : "N/A").append("\n");
                details.append("  • Date fabrication: ").append(dateFormat.format(med.getDateFabrication())).append("\n");
                details.append("  • Date expiration : ").append(dateFormat.format(med.getDateExpiration())).append("\n\n");

                // Alertes et statuts
                details.append("⚠ ALERTES & STATUTS\n");

                if (stock.getQuantiteProduit() == 0) {
                    details.append("  ❌ RUPTURE DE STOCK!\n");
                } else if (stock.Alerte()) {
                    details.append("  ⚠ STOCK FAIBLE!\n");
                    details.append("     Quantité actuelle ≤ Seuil minimal\n");
                }

                if (med.estPerime()) {
                    details.append("  ⚠ MÉDICAMENT PÉRIMÉ!\n");
                    details.append("     Date d'expiration dépassée\n");
                } else {
                    long joursRestants = (med.getDateExpiration().getTime() - System.currentTimeMillis())
                            / (1000 * 60 * 60 * 24);
                    if (joursRestants <= 30) {
                        details.append("  ⚠ EXPIRATION PROCHE!\n");
                        details.append("     ").append(joursRestants).append(" jour(s) restant(s)\n");
                    } else {
                        details.append("  ✓ Statut normal\n");
                    }
                }

                details.append("\n═══════════════════════════════════════\n");

                // Rechercher les autres stocks du même médicament
                details.append("\n📊 AUTRES STOCKS DU MÊME MÉDICAMENT\n");
                List<StockMedicament> autresStocks = stockBD.listerTous();
                int count = 0;
                int totalQte = 0;

                for (StockMedicament s : autresStocks) {
                    if (s.getRefMedicament() == refMed) {
                        count++;
                        totalQte += s.getQuantiteProduit();
                        if (s.getNumStock() != numStock) {
                            details.append("  • Stock #").append(s.getNumStock())
                                    .append(": ").append(s.getQuantiteProduit())
                                    .append(" unités @ ").append(String.format("%.2f DT", s.getPrixAchat()))
                                    .append("\n");
                        }
                    }
                }

                details.append("\n  Total: ").append(count).append(" ligne(s) de stock\n");
                details.append("  Quantité totale: ").append(totalQte).append(" unité(s)\n");
                details.append("═══════════════════════════════════════\n");

                txtDetails.setText(details.toString());
                txtDetails.setCaretPosition(0);
            }

        } catch (SQLException ex) {
            txtDetails.setText("Erreur lors du chargement des détails:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void applyFilter() {
        String filterType = (String) cmbFilterType.getSelectedItem();

        if ("Tous".equals(filterType)) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String statut = entry.getStringValue(10);
                    int quantite = (Integer) entry.getValue(3);

                    switch (filterType) {
                        case "En stock":
                            return quantite > 0 && !statut.contains("Périmé");
                        case "Alertes":
                            return statut.contains("Alerte") || statut.contains("Périmé");
                        case "Rupture":
                            return quantite == 0;
                        case "Même médicament":
                            // Afficher toutes les lignes du même médicament que la ligne sélectionnée
                            int selectedRow = tableStock.getSelectedRow();
                            if (selectedRow >= 0) {
                                int refMed = (Integer) tableModel.getValueAt(
                                        tableStock.convertRowIndexToModel(selectedRow), 1);
                                return (Integer) entry.getValue(1) == refMed;
                            }
                            return true;
                        default:
                            return true;
                    }
                }
            });
        }
    }

    private void applyTextFilter() {
        String text = txtFilter.getText().trim();

        if (text.isEmpty()) {
            applyFilter();
        } else {
            final String filterText = text.toLowerCase();
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    // Recherche dans N° Stock, Réf Med, et Nom
                    String numStock = entry.getStringValue(0).toLowerCase();
                    String refMed = entry.getStringValue(1).toLowerCase();
                    String nom = entry.getStringValue(2).toLowerCase();

                    return numStock.contains(filterText) ||
                            refMed.contains(filterText) ||
                            nom.contains(filterText);
                }
            });
        }
    }
}