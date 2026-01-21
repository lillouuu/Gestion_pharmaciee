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

public class ListeMedicamentFrame extends JFrame {
    private JTable tableMedicaments;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtFilter;
    private JComboBox<String> cmbFilterType;
    private JButton btnRefresh, btnExport, btnClose;
    private JLabel lblCount, lblAlerts;
    private MedicamentBD medicamentBD;
    private StockBD stockBD;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public ListeMedicamentFrame() {
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        initComponents();
        loadMedicaments();
    }

    private void initComponents() {
        setTitle("Liste des Médicaments");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(108, 117, 125));
        topPanel.setPreferredSize(new Dimension(1200, 50));

        JLabel titleLabel = new JLabel("📋 Liste complète des médicaments");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de filtrage
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtrage rapide"));

        filterPanel.add(new JLabel("Filtrer par:"));
        cmbFilterType = new JComboBox<>(new String[]{"Tous", "En stock", "Stock faible", "Périmés", "Valides"});
        cmbFilterType.addActionListener(e -> applyFilter());
        filterPanel.add(cmbFilterType);

        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("Recherche:"));
        txtFilter = new JTextField(20);
        txtFilter.addCaretListener(e -> applyTextFilter());
        filterPanel.add(txtFilter);

        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setBackground(new Color(0, 123, 255));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadMedicaments());
        filterPanel.add(btnRefresh);

        mainPanel.add(filterPanel, BorderLayout.NORTH);

        // Tableau
        String[] columns = {
                "Réf", "Nom", "Fournisseur", "Prix Unit.", "Stock",
                "Prix Achat", "Prix Vente", "Seuil Min", "Date Exp.", "Statut"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 4 || columnIndex == 7) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        tableMedicaments = new JTable(tableModel);
        tableMedicaments.setAutoCreateRowSorter(true);
        tableMedicaments.setRowHeight(25);
        tableMedicaments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Tailles des colonnes
        tableMedicaments.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableMedicaments.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableMedicaments.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableMedicaments.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableMedicaments.getColumnModel().getColumn(4).setPreferredWidth(60);
        tableMedicaments.getColumnModel().getColumn(5).setPreferredWidth(80);
        tableMedicaments.getColumnModel().getColumn(6).setPreferredWidth(80);
        tableMedicaments.getColumnModel().getColumn(7).setPreferredWidth(70);
        tableMedicaments.getColumnModel().getColumn(8).setPreferredWidth(90);
        tableMedicaments.getColumnModel().getColumn(9).setPreferredWidth(100);

        // Sorter pour le filtrage
        sorter = new TableRowSorter<>(tableModel);
        tableMedicaments.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tableMedicaments);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel d'informations en bas
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        lblCount = new JLabel("Total: 0 médicament(s)");
        lblCount.setFont(new Font("Arial", Font.BOLD, 12));
        lblCount.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        lblAlerts = new JLabel("Alertes: 0");
        lblAlerts.setFont(new Font("Arial", Font.BOLD, 12));
        lblAlerts.setForeground(new Color(220, 53, 69));
        lblAlerts.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        lblAlerts.setHorizontalAlignment(SwingConstants.RIGHT);

        infoPanel.add(lblCount);
        infoPanel.add(lblAlerts);

        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnExport = new JButton("📄 Exporter (CSV)");
        btnExport.setPreferredSize(new Dimension(150, 35));
        btnExport.setBackground(new Color(40, 167, 69));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.addActionListener(e -> exportToCSV());

        btnClose = new JButton("❌ Fermer");
        btnClose.setPreferredSize(new Dimension(150, 35));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnExport);
        btnPanel.add(btnClose);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadMedicaments() {
        tableModel.setRowCount(0);
        int alertCount = 0;

        try {
            List<Medicament> medicaments = medicamentBD.listerTous();

            for (Medicament med : medicaments) {
                StockMedicament stock = stockBD.rechercherParRef(med.getRefMedicament());

                String stockQte = stock != null ? String.valueOf(stock.getQuantiteProduit()) : "0";
                String prixAchat = stock != null ? String.format("%.2f", stock.getPrixAchat()) : "N/A";
                String prixVente = stock != null ? String.format("%.2f", stock.getPrixVente()) : "N/A";
                String seuilMin = stock != null ? String.valueOf(stock.getSeuilMin()) : "N/A";

                // Déterminer le statut
                String statut;
                if (med.estPerime()) {
                    statut = "⚠ PÉRIMÉ";
                    alertCount++;
                } else if (stock != null && stock.Alerte()) {
                    statut = "⚠ Stock faible";
                    alertCount++;
                } else if (stock == null || stock.getQuantiteProduit() == 0) {
                    statut = "❌ Rupture";
                    alertCount++;
                } else {
                    statut = "✓ Normal";
                }

                tableModel.addRow(new Object[]{
                        med.getRefMedicament(),
                        med.getNom(),
                        "Fournisseur " + med.getNumFournisseur(),
                        //String.format("%.2f", med.getPrix()),
                        stock != null ? stock.getQuantiteProduit() : 0,
                        prixAchat,
                        prixVente,
                        seuilMin,
                        dateFormat.format(med.getDateExpiration()),
                        statut
                });
            }

            lblCount.setText("Total: " + medicaments.size() + " médicament(s)");
            lblAlerts.setText("Alertes: " + alertCount);

            if (alertCount > 0) {
                lblAlerts.setForeground(new Color(220, 53, 69));
            } else {
                lblAlerts.setForeground(new Color(40, 167, 69));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void applyFilter() {
        String filterType = (String) cmbFilterType.getSelectedItem();

        if (filterType.equals("Tous")) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String statut = entry.getStringValue(9);
                    int stock = (Integer) entry.getValue(4);

                    switch (filterType) {
                        case "En stock":
                            return stock > 0 && !statut.contains("PÉRIMÉ");
                        case "Stock faible":
                            return statut.contains("Stock faible");
                        case "Périmés":
                            return statut.contains("PÉRIMÉ");
                        case "Valides":
                            return statut.equals("✓ Normal");
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
            applyFilter(); // Applique le filtre de type uniquement
        } else {
            final String filterText = text.toLowerCase();
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    // Recherche dans nom et fournisseur
                    String nom = entry.getStringValue(1).toLowerCase();
                    String fournisseur = entry.getStringValue(2).toLowerCase();

                    return nom.contains(filterText) || fournisseur.contains(filterText);
                }
            });
        }
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer la liste en CSV");
        fileChooser.setSelectedFile(new java.io.File("medicaments_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile());

                // En-têtes
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.write(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.write(";");
                    }
                }
                writer.write("\n");

                // Données
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        writer.write(value != null ? value.toString() : "");
                        if (j < tableModel.getColumnCount() - 1) {
                            writer.write(";");
                        }
                    }
                    writer.write("\n");
                }

                writer.close();

                JOptionPane.showMessageDialog(this,
                        "Export réussi!\nFichier: " + fileChooser.getSelectedFile().getName(),
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'export: " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}