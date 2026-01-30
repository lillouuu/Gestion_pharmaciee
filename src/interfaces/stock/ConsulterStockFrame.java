package interfaces.stock;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import entitebd.ConnectionBD;

/**
 * Frame pour consulter le stock avec jointure stock_medicament + medicament
 * Basé sur la requête SQL fournie
 */
public class ConsulterStockFrame extends JFrame {
    private JTable tableStock;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtFilter;
    private JComboBox<String> cmbFilterType;
    private JLabel lblCount, lblValeur, lblAlertes;
    private JButton btnRefresh, btnClose, btnExport;
    private JTextArea txtDetails;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public ConsulterStockFrame() {
        initComponents();
        chargerStock();
    }

    private void initComponents() {
        setTitle("Consultation du Stock - Vue Complète");
        setSize(1400, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(99, 102, 241));
        topPanel.setPreferredSize(new Dimension(1400, 50));

        JLabel titleLabel = new JLabel("👁 Consultation Stock - Vue complète (Stock ⋈ Médicament)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Split pane principal
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Panel gauche - Tableau
        JPanel leftPanel = creerPanelTableau();
        splitPane.setLeftComponent(leftPanel);

        // Panel droit - Détails
        JPanel rightPanel = creerPanelDetails();
        splitPane.setRightComponent(rightPanel);

        splitPane.setDividerLocation(1000);
        add(splitPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setPreferredSize(new Dimension(150, 35));
        btnRefresh.setBackground(new Color(99, 102, 241));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> chargerStock());

        btnExport = new JButton("📄 Exporter CSV");
        btnExport.setPreferredSize(new Dimension(150, 35));
        btnExport.setBackground(new Color(100, 116, 139));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.addActionListener(e -> exporterCSV());

        btnClose = new JButton("❌ Fermer");
        btnClose.setPreferredSize(new Dimension(150, 35));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnRefresh);
        btnPanel.add(btnExport);
        btnPanel.add(btnClose);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel creerPanelTableau() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // Panel filtrage
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtres"));

        filterPanel.add(new JLabel("Type:"));
        cmbFilterType = new JComboBox<>(new String[]{
                "Tous", "En stock", "Stock faible", "Rupture", "Périmés", "Valides"
        });
        cmbFilterType.addActionListener(e -> appliquerFiltre());
        filterPanel.add(cmbFilterType);

        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("Recherche:"));
        txtFilter = new JTextField(20);
        txtFilter.addCaretListener(e -> appliquerFiltreTexte());
        filterPanel.add(txtFilter);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Tableau - Colonnes selon la requête SQL
        String[] columns = {
                "N° Stock", "Réf Med", "Nom Médicament", "Description",
                "Quantité", "Prix Achat", "Prix Vente", "Seuil Min",
                "Date Fab", "Date Exp", "Valeur Stock", "Statut"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 1 || columnIndex == 4 || columnIndex == 7) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        tableStock = new JTable(tableModel);
        tableStock.setAutoCreateRowSorter(true);
        tableStock.setRowHeight(25);
        tableStock.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Ajuster les largeurs de colonnes
        tableStock.getColumnModel().getColumn(0).setPreferredWidth(70);   // N° Stock
        tableStock.getColumnModel().getColumn(1).setPreferredWidth(60);   // Réf Med
        tableStock.getColumnModel().getColumn(2).setPreferredWidth(200);  // Nom
        tableStock.getColumnModel().getColumn(3).setPreferredWidth(150);  // Description
        tableStock.getColumnModel().getColumn(4).setPreferredWidth(70);   // Quantité
        tableStock.getColumnModel().getColumn(5).setPreferredWidth(80);   // Prix Achat
        tableStock.getColumnModel().getColumn(6).setPreferredWidth(80);   // Prix Vente
        tableStock.getColumnModel().getColumn(7).setPreferredWidth(70);   // Seuil
        tableStock.getColumnModel().getColumn(8).setPreferredWidth(80);   // Date Fab
        tableStock.getColumnModel().getColumn(9).setPreferredWidth(80);   // Date Exp
        tableStock.getColumnModel().getColumn(10).setPreferredWidth(90);  // Valeur
        tableStock.getColumnModel().getColumn(11).setPreferredWidth(100); // Statut

        // Sorter pour filtrage
        sorter = new TableRowSorter<>(tableModel);
        tableStock.setRowSorter(sorter);

        // Listener pour afficher détails
        tableStock.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableStock.getSelectedRow() != -1) {
                afficherDetails();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableStock);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel statistiques
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        lblCount = new JLabel("Lignes: 0");
        lblCount.setFont(new Font("Arial", Font.BOLD, 13));

        lblValeur = new JLabel("Valeur totale: 0.00 DT");
        lblValeur.setFont(new Font("Arial", Font.BOLD, 13));
        lblValeur.setHorizontalAlignment(SwingConstants.CENTER);

        lblAlertes = new JLabel("Alertes: 0");
        lblAlertes.setFont(new Font("Arial", Font.BOLD, 13));
        lblAlertes.setForeground(new Color(236, 72, 153));
        lblAlertes.setHorizontalAlignment(SwingConstants.RIGHT);

        statsPanel.add(lblCount);
        statsPanel.add(lblValeur);
        statsPanel.add(lblAlertes);

        panel.add(statsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel creerPanelDetails() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Détails de la ligne"));

        txtDetails = new JTextArea();
        txtDetails.setEditable(false);
        txtDetails.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetails.setBackground(new Color(245, 245, 245));
        txtDetails.setText("Sélectionnez une ligne\npour voir les détails...");

        JScrollPane scrollPane = new JScrollPane(txtDetails);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Charger les données avec la requête SQL de jointure
     */
    private void chargerStock() {
        tableModel.setRowCount(0);
        int alertCount = 0;
        double valeurTotale = 0.0;

        // Requête SQL exacte fournie
        String sql = "SELECT " +
                "s.num_stock, " +
                "s.quantite_produit, " +
                "s.prix_achat, " +
                "s.prix_vente, " +
                "s.seuil_min, " +
                "m.ref_medicament, " +
                "m.nom AS nom_medicament, " +
                "m.descriptio, " +
                "s.date_fabrication, " +
                "s.date_expiration " +
                "FROM stock_medicament s " +
                "JOIN medicament m ON s.ref_medicament = m.ref_medicament " +
                "ORDER BY s.num_stock";

        try (Connection con = ConnectionBD.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                // Récupérer les données
                int numStock = rs.getInt("num_stock");
                int refMedicament = rs.getInt("ref_medicament");
                String nomMedicament = rs.getString("nom_medicament");
                String description = rs.getString("descriptio");
                int quantite = rs.getInt("quantite_produit");
                double prixAchat = rs.getDouble("prix_achat");
                double prixVente = rs.getDouble("prix_vente");
                int seuilMin = rs.getInt("seuil_min");
                Date dateFab = rs.getDate("date_fabrication");
                Date dateExp = rs.getDate("date_expiration");

                // Calculer valeur stock
                double valeurStock = quantite * prixAchat;
                valeurTotale += valeurStock;

                // Déterminer statut
                String statut;
                boolean alerte = false;

                if (quantite == 0) {
                    statut = "❌ Rupture";
                    alerte = true;
                } else if (quantite <= seuilMin) {
                    statut = "⚠ Stock faible";
                    alerte = true;
                } else if (dateExp != null && new java.util.Date().after(dateExp)) {
                    statut = "⚠ Périmé";
                    alerte = true;
                } else {
                    // Vérifier si proche expiration (30 jours)
                    if (dateExp != null) {
                        long joursRestants = (dateExp.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
                        if (joursRestants <= 30 && joursRestants > 0) {
                            statut = "⚠ Exp. proche";
                            alerte = true;
                        } else {
                            statut = "✓ Normal";
                        }
                    } else {
                        statut = "✓ Normal";
                    }
                }

                if (alerte) alertCount++;

                // Ajouter la ligne au tableau
                tableModel.addRow(new Object[]{
                        numStock,
                        refMedicament,
                        nomMedicament != null ? nomMedicament : "N/A",
                        description != null && !description.isEmpty() ? description : "-",
                        quantite,
                        String.format("%.2f DT", prixAchat),
                        String.format("%.2f DT", prixVente),
                        seuilMin,
                        dateFab != null ? dateFormat.format(dateFab) : "N/A",
                        dateExp != null ? dateFormat.format(dateExp) : "N/A",
                        String.format("%.2f DT", valeurStock),
                        statut
                });
            }

            // Mettre à jour les statistiques
            lblCount.setText("Lignes: " + tableModel.getRowCount());
            lblValeur.setText(String.format("Valeur totale: %.2f DT", valeurTotale));
            lblAlertes.setText("Alertes: " + alertCount);

            if (alertCount > 0) {
                lblAlertes.setForeground(new Color(236, 72, 153));
            } else {
                lblAlertes.setForeground(new Color(100, 116, 139));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement du stock:\n" + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Afficher les détails de la ligne sélectionnée
     */
    private void afficherDetails() {
        int selectedRow = tableStock.getSelectedRow();
        if (selectedRow == -1) return;

        // Convertir l'index de vue en index de modèle (important avec le tri)
        int modelRow = tableStock.convertRowIndexToModel(selectedRow);

        try {
            int numStock = (Integer) tableModel.getValueAt(modelRow, 0);
            int refMed = (Integer) tableModel.getValueAt(modelRow, 1);
            String nom = (String) tableModel.getValueAt(modelRow, 2);
            String desc = (String) tableModel.getValueAt(modelRow, 3);
            int quantite = (Integer) tableModel.getValueAt(modelRow, 4);
            String prixAchat = (String) tableModel.getValueAt(modelRow, 5);
            String prixVente = (String) tableModel.getValueAt(modelRow, 6);
            int seuil = (Integer) tableModel.getValueAt(modelRow, 7);
            String dateFab = (String) tableModel.getValueAt(modelRow, 8);
            String dateExp = (String) tableModel.getValueAt(modelRow, 9);
            String valeur = (String) tableModel.getValueAt(modelRow, 10);
            String statut = (String) tableModel.getValueAt(modelRow, 11);

            // Calculer marge
            double achat = Double.parseDouble(prixAchat.replace(" DT", "").replace(",", "."));
            double vente = Double.parseDouble(prixVente.replace(" DT", "").replace(",", "."));
            double margeUnit = vente - achat;
            double margeTotale = margeUnit * quantite;

            StringBuilder details = new StringBuilder();
            details.append("═══════════════════════════════════════════\n");
            details.append("       DÉTAILS DE LA LIGNE DE STOCK\n");
            details.append("═══════════════════════════════════════════\n\n");

            details.append("📦 INFORMATIONS STOCK\n");
            details.append("  • N° Stock        : ").append(numStock).append("\n");
            details.append("  • Quantité        : ").append(quantite).append(" unité(s)\n");
            details.append("  • Prix d'achat    : ").append(prixAchat).append("\n");
            details.append("  • Prix de vente   : ").append(prixVente).append("\n");
            details.append("  • Seuil minimal   : ").append(seuil).append(" unité(s)\n");
            details.append("  • Valeur du stock : ").append(valeur).append("\n");
            details.append("  • Marge unitaire  : ").append(String.format("%.2f DT", margeUnit)).append("\n");
            details.append("  • Marge totale    : ").append(String.format("%.2f DT", margeTotale)).append("\n\n");

            details.append("💊 INFORMATIONS MÉDICAMENT\n");
            details.append("  • Référence       : ").append(refMed).append("\n");
            details.append("  • Nom             : ").append(nom).append("\n");
            details.append("  • Description     : ").append(desc).append("\n");
            details.append("  • Date fabrication: ").append(dateFab).append("\n");
            details.append("  • Date expiration : ").append(dateExp).append("\n\n");

            details.append("⚠ STATUT & ALERTES\n");
            details.append("  • État            : ").append(statut).append("\n");

            if (quantite == 0) {
                details.append("\n  ❌ ATTENTION: RUPTURE DE STOCK!\n");
            } else if (quantite <= seuil) {
                details.append("\n  ⚠ ALERTE: Stock faible!\n");
                details.append("     À commander: ").append((seuil * 2) - quantite).append(" unités\n");
            }

            if (!"N/A".equals(dateExp)) {
                try {
                    java.util.Date expDate = dateFormat.parse(dateExp);
                    long joursRestants = (expDate.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);

                    if (joursRestants < 0) {
                        details.append("\n  ⚠ MÉDICAMENT PÉRIMÉ!\n");
                    } else if (joursRestants <= 30) {
                        details.append("\n  ⚠ EXPIRATION PROCHE!\n");
                        details.append("     ").append(joursRestants).append(" jour(s) restant(s)\n");
                    }
                } catch (Exception e) {
                    // Ignorer erreur de parsing
                }
            }

            details.append("\n═══════════════════════════════════════════\n");

            // Compter combien de lignes pour le même médicament
            int nbLignesMeme = compterLignesMedicament(refMed);
            if (nbLignesMeme > 1) {
                details.append("\n📊 AUTRES STOCKS DU MÊME MÉDICAMENT\n");
                details.append("  Il existe ").append(nbLignesMeme).append(" ligne(s) de stock\n");
                details.append("  pour ce médicament (Réf #").append(refMed).append(")\n");
            }

            details.append("═══════════════════════════════════════════\n");

            txtDetails.setText(details.toString());
            txtDetails.setCaretPosition(0);

        } catch (Exception ex) {
            txtDetails.setText("Erreur lors de l'affichage des détails:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Compter le nombre de lignes de stock pour un médicament donné
     */
    private int compterLignesMedicament(int refMedicament) {
        int count = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((Integer) tableModel.getValueAt(i, 1) == refMedicament) {
                count++;
            }
        }
        return count;
    }

    /**
     * Appliquer le filtre par type
     */
    private void appliquerFiltre() {
        String filterType = (String) cmbFilterType.getSelectedItem();

        if ("Tous".equals(filterType)) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String statut = entry.getStringValue(11);
                    int quantite = (Integer) entry.getValue(4);

                    switch (filterType) {
                        case "En stock":
                            return quantite > 0 && !statut.contains("Périmé");
                        case "Stock faible":
                            return statut.contains("Stock faible") || statut.contains("Exp. proche");
                        case "Rupture":
                            return quantite == 0;
                        case "Périmés":
                            return statut.contains("Périmé");
                        case "Valides":
                            return statut.equals("✓ Normal");
                        default:
                            return true;
                    }
                }
            });
        }
    }

    /**
     * Appliquer le filtre par texte
     */
    private void appliquerFiltreTexte() {
        String text = txtFilter.getText().trim();

        if (text.isEmpty()) {
            appliquerFiltre();
        } else {
            final String filterText = text.toLowerCase();
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String numStock = entry.getStringValue(0).toLowerCase();
                    String refMed = entry.getStringValue(1).toLowerCase();
                    String nom = entry.getStringValue(2).toLowerCase();
                    String desc = entry.getStringValue(3).toLowerCase();

                    return numStock.contains(filterText) ||
                            refMed.contains(filterText) ||
                            nom.contains(filterText) ||
                            desc.contains(filterText);
                }
            });
        }
    }

    /**
     * Exporter les données en CSV
     */
    private void exporterCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter le stock en CSV");
        fileChooser.setSelectedFile(new java.io.File("stock_complet_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile())) {

                // En-têtes
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.write("\"" + tableModel.getColumnName(i) + "\"");
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.write(";");
                    }
                }
                writer.write("\n");

                // Données
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        writer.write("\"" + (value != null ? value.toString() : "") + "\"");
                        if (j < tableModel.getColumnCount() - 1) {
                            writer.write(";");
                        }
                    }
                    writer.write("\n");
                }

                JOptionPane.showMessageDialog(this,
                        "✓ Export réussi!\n\nFichier: " + fileChooser.getSelectedFile().getName() +
                                "\nLignes exportées: " + tableModel.getRowCount(),
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'export:\n" + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}