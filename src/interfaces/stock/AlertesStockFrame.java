package interfaces.stock;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.Medicament;
import entite.StockMedicament;
import entitebd.MedicamentBD;
import gestion.GestionStock;

public class AlertesStockFrame extends JFrame {
    private JTable tableAlertes;
    private DefaultTableModel tableModel;
    private JLabel lblCount, lblValeurTotal;
    private JButton btnRefresh, btnClose, btnCommander;
    private GestionStock gestionStock;
    private MedicamentBD medicamentBD;

    public AlertesStockFrame() {
        gestionStock = new GestionStock();
        medicamentBD = new MedicamentBD();
        initComponents();
        loadAlertes();
    }

    private void initComponents() {
        setTitle("Alertes Stock");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(236, 72, 153));
        topPanel.setPreferredSize(new Dimension(1000, 50));

        JLabel titleLabel = new JLabel("⚠ Alertes de stock faible");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(new Color(255, 243, 205));
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7), 2));

        JLabel lblInfo = new JLabel("⚠ Produits dont la quantité est inférieure ou égale au seuil minimal");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 12));
        lblInfo.setForeground(new Color(133, 100, 4));
        infoPanel.add(lblInfo);

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Tableau
        String[] columns = {"Réf", "Nom", "Quantité actuelle", "Seuil minimal", "À commander", "Prix achat", "Coût estimé"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableAlertes = new JTable(tableModel);
        tableAlertes.setRowHeight(25);
        tableAlertes.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableAlertes.getColumnModel().getColumn(1).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(tableAlertes);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel stats
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        lblCount = new JLabel("Produits en alerte: 0");
        lblCount.setFont(new Font("Arial", Font.BOLD, 13));
        lblCount.setForeground(new Color(236, 72, 153));

        lblValeurTotal = new JLabel("Valeur totale du stock: 0.00 DT");
        lblValeurTotal.setFont(new Font("Arial", Font.BOLD, 13));
        lblValeurTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        statsPanel.add(lblCount);
        statsPanel.add(lblValeurTotal);

        mainPanel.add(statsPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setPreferredSize(new Dimension(150, 35));
        btnRefresh.setBackground(new Color(99, 102, 241));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadAlertes());

        btnCommander = new JButton("📦 Créer commandes");
        btnCommander.setPreferredSize(new Dimension(150, 35));
        btnCommander.setBackground(new Color(255, 165, 0));
        btnCommander.setForeground(Color.WHITE);
        btnCommander.setFocusPainted(false);
        btnCommander.setToolTipText("Créer des commandes pour les produits en alerte");
        btnCommander.addActionListener(e -> creerCommandes());

        btnClose = new JButton("❌ Fermer");
        btnClose.setPreferredSize(new Dimension(150, 35));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnRefresh);
        btnPanel.add(btnCommander);
        btnPanel.add(btnClose);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadAlertes() {
        tableModel.setRowCount(0);

        try {
            List<StockMedicament> alertes = gestionStock.obtenirAlertes();
            double valeurTotale = gestionStock.calculerValeurTotaleStock();

            for (StockMedicament stock : alertes) {
                Medicament med = medicamentBD.rechercherParRef(stock.getRefMedicament());

                if (med != null) {
                    int quantiteActuelle = stock.getQuantiteProduit();
                    int seuilMin = stock.getSeuilMin();
                    int aCommander = (seuilMin * 2) - quantiteActuelle; // Commander pour arriver au double du seuil
                    double coutEstime = aCommander * stock.getPrixAchat();

                    tableModel.addRow(new Object[]{
                            stock.getRefMedicament(),
                            med.getNom(),
                            quantiteActuelle,
                            seuilMin,
                            aCommander,
                            String.format("%.2f DT", stock.getPrixAchat()),
                            String.format("%.2f DT", coutEstime)
                    });
                }
            }

            lblCount.setText("Produits en alerte: " + alertes.size());
            lblValeurTotal.setText(String.format("Valeur totale du stock: %.2f DT", valeurTotale));

            if (alertes.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "✓ Aucune alerte de stock!\nTous les produits sont au-dessus du seuil minimal.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des alertes: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void creerCommandes() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Aucun produit en alerte!",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous créer des commandes pour tous les produits en alerte?\n" +
                        "Nombre de produits: " + tableModel.getRowCount(),
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // TODO: Implémenter la création de commandes
            JOptionPane.showMessageDialog(this,
                    "Fonctionnalité à implémenter:\n" +
                            "Créer des commandes automatiques pour " + tableModel.getRowCount() + " produits",
                    "En développement",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
