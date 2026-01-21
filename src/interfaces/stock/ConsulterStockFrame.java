package interfaces.stock;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.Medicament;
import entite.StockMedicament;
import entitebd.MedicamentBD;
import entitebd.StockBD;
import gestion.GestionStock;

public class ConsulterStockFrame extends JFrame {
    private JTable tableStock;
    private DefaultTableModel tableModel;
    private JTextField txtFilter;
    private JComboBox<String> cmbFilterType;
    private JLabel lblCount, lblValeur, lblAlertes;
    private JButton btnRefresh, btnClose;
    private MedicamentBD medicamentBD;
    private StockBD stockBD;
    private GestionStock gestionStock;

    public ConsulterStockFrame() {
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        gestionStock = new GestionStock();
        initComponents();
        loadStock();
    }

    private void initComponents() {
        setTitle("Consulter le Stock");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0, 123, 255));
        topPanel.setPreferredSize(new Dimension(1200, 50));

        JLabel titleLabel = new JLabel("👁 Consultation du stock");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel filtrage
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtres"));

        filterPanel.add(new JLabel("Type:"));
        cmbFilterType = new JComboBox<>(new String[]{
                "Tous", "En stock", "Alertes", "Rupture", "Valeur élevée (> 1000 DT)"
        });
        cmbFilterType.addActionListener(e -> applyFilter());
        filterPanel.add(cmbFilterType);

        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("Recherche:"));
        txtFilter = new JTextField(20);
        txtFilter.addCaretListener(e -> applyFilter());
        filterPanel.add(txtFilter);

        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setBackground(new Color(0, 123, 255));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadStock());
        filterPanel.add(btnRefresh);

        mainPanel.add(filterPanel, BorderLayout.NORTH);

        // Tableau
        String[] columns = {
                "Réf", "Nom", "Quantité", "Seuil", "Prix Achat", "Prix Vente",
                "Valeur Stock", "Marge Unit.", "Statut"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableStock = new JTable(tableModel);
        tableStock.setAutoCreateRowSorter(true);
        tableStock.setRowHeight(25);
        tableStock.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableStock.getColumnModel().getColumn(1).setPreferredWidth(250);
        tableStock.getColumnModel().getColumn(2).setPreferredWidth(70);
        tableStock.getColumnModel().getColumn(3).setPreferredWidth(60);

        JScrollPane scrollPane = new JScrollPane(tableStock);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel statistiques
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        lblCount = new JLabel("Produits: 0");
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

        mainPanel.add(statsPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnClose = new JButton("❌ Fermer");
        btnClose.setPreferredSize(new Dimension(150, 35));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnClose);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadStock() {
        tableModel.setRowCount(0);
        int alertCount = 0;
        double valeurTotale = 0.0;

        try {
            List<StockMedicament> stocks = stockBD.listerTous();

            for (StockMedicament stock : stocks) {
                Medicament med = medicamentBD.rechercherParRef(stock.getRefMedicament());

                if (med != null) {
                    int quantite = stock.getQuantiteProduit();
                    double prixAchat = stock.getPrixAchat();
                    double prixVente = stock.getPrixVente();
                    double valeurStock = quantite * prixAchat;
                    double margeUnit = prixVente - prixAchat;

                    valeurTotale += valeurStock;

                    String statut;
                    if (quantite == 0) {
                        statut = "❌ Rupture";
                        alertCount++;
                    } else if (stock.Alerte()) {
                        statut = "⚠ Alerte";
                        alertCount++;
                    } else {
                        statut = "✓ Normal";
                    }

                    tableModel.addRow(new Object[]{
                            stock.getRefMedicament(),
                            med.getNom(),
                            quantite,
                            stock.getSeuilMin(),
                            String.format("%.2f", prixAchat),
                            String.format("%.2f", prixVente),
                            String.format("%.2f DT", valeurStock),
                            String.format("%.2f DT", margeUnit),
                            statut
                    });
                }
            }

            lblCount.setText("Produits: " + stocks.size());
            lblValeur.setText(String.format("Valeur totale: %.2f DT", valeurTotale));
            lblAlertes.setText("Alertes: " + alertCount);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void applyFilter() {
        // TODO: Implémenter le filtrage
        // Pour l'instant, on recharge tout
        if (txtFilter.getText().trim().isEmpty() && cmbFilterType.getSelectedIndex() == 0) {
            loadStock();
        }
    }
}
