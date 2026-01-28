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

public class SupprimerMedicamentFrame extends JFrame {
    private JTextField txtSearch;
    private JTable tableMedicaments;
    private DefaultTableModel tableModel;
    private JButton btnSearch, btnDelete, btnCancel, btnRefresh;
    private MedicamentBD medicamentBD;
    private StockBD stockBD;
    private GestionProduit gestionProduit;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public SupprimerMedicamentFrame() {
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        gestionProduit = new GestionProduit();
        initComponents();
        loadMedicaments();
    }

    private void initComponents() {
        setTitle("Supprimer un Médicament");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(220, 53, 69));
        topPanel.setPreferredSize(new Dimension(1100, 50));

        JLabel titleLabel = new JLabel("🗑 Supprimer un médicament");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Rechercher par nom:"));
        txtSearch = new JTextField(25);
        searchPanel.add(txtSearch);

        btnSearch = new JButton("🔍 Rechercher");
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> searchMedicament());
        searchPanel.add(btnSearch);

        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setBackground(new Color(108, 117, 125));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadMedicaments());
        searchPanel.add(btnRefresh);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Tableau - ✅ REFACTORED: Colonnes adaptées aux lots multiples
        String[] columns = {"Réf", "Nom",  "Nb Lots", "Stock Total", "Plus Ancien", "Statut"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableMedicaments = new JTable(tableModel);
        tableMedicaments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMedicaments.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableMedicaments.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableMedicaments.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tableMedicaments);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel d'information
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(255, 243, 205));
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7), 2));
        JLabel lblInfo = new JLabel("⚠ Attention: La suppression d'un médicament supprimera aussi TOUS ses stocks (lots)!");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 12));
        lblInfo.setForeground(new Color(133, 100, 4));
        infoPanel.add(lblInfo);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnDelete = new JButton("🗑 Supprimer");
        btnDelete.setPreferredSize(new Dimension(150, 35));
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> deleteMedicament());

        btnCancel = new JButton("❌ Fermer");
        btnCancel.setPreferredSize(new Dimension(150, 35));
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnDelete);
        btnPanel.add(btnCancel);

        add(btnPanel, BorderLayout.SOUTH);

        // Action sur Enter dans le champ de recherche
        txtSearch.addActionListener(e -> searchMedicament());
    }

    private void loadMedicaments() {
        tableModel.setRowCount(0);
        try {
            List<Medicament> medicaments = medicamentBD.listerTous();
            for (Medicament med : medicaments) {

                List<StockMedicament> stocks = stockBD.getStocksParExpiration(med.getRefMedicament());
                int nbLots = stocks != null ? stocks.size() : 0;
                int stockTotal = 0;
                String plusAncien = "N/A";
                String statut = "✓ OK";

                if (stocks != null && !stocks.isEmpty()) {
                    for (StockMedicament stock : stocks) {
                        stockTotal += stock.getQuantiteProduit();
                        if (stock.estPerime()) {
                            statut = "⚠ PÉRIMÉ";
                        }
                    }

                    StockMedicament premierStock = stocks.get(0);
                    plusAncien = dateFormat.format(premierStock.getDateExpiration());
                }

                tableModel.addRow(new Object[]{
                        med.getRefMedicament(),
                        med.getNom(),
                        nbLots + " lot(s)",
                        stockTotal + " unités",
                        plusAncien,
                        statut
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des médicaments: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
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
                String plusAncien = "N/A";
                String statut = "✓ OK";

                if (stocks != null && !stocks.isEmpty()) {
                    for (StockMedicament stock : stocks) {
                        stockTotal += stock.getQuantiteProduit();

                        if (stock.estPerime()) {
                            statut = "⚠ PÉRIMÉ";
                        }
                    }

                    StockMedicament premierStock = stocks.get(0);
                    plusAncien = dateFormat.format(premierStock.getDateExpiration());
                }

                tableModel.addRow(new Object[]{
                        med.getRefMedicament(),
                        med.getNom(),
                        nbLots + " lot(s)",
                        stockTotal + " unités",
                        plusAncien,
                        statut
                });
            }

            if (medicaments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Aucun médicament trouvé avec le nom: " + searchTerm,
                        "Recherche",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la recherche: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteMedicament() {
        int selectedRow = tableMedicaments.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un médicament à supprimer!",
                    "Sélection requise",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int refMedicament = (int) tableModel.getValueAt(selectedRow, 0);
        String nomMedicament = (String) tableModel.getValueAt(selectedRow, 1);
        String nbLots = (String) tableModel.getValueAt(selectedRow, 2);
        String stockTotal = (String) tableModel.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer ce médicament?\n\n" +
                        "Référence: " + refMedicament + "\n" +
                        "Nom: " + nomMedicament + "\n" +
                        "Nombre de lots: " + nbLots + "\n" +
                        "Stock total: " + stockTotal + "\n\n" +
                        "⚠ Cette action supprimera TOUS les lots de stock!\n" +
                        "⚠ Cette action est irréversible!",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = gestionProduit.supprimerMedicament(refMedicament);

                if (deleted) {
                    JOptionPane.showMessageDialog(this,
                            "✅ Médicament supprimé avec succès!\n" +
                                    "Tous les stocks associés ont également été supprimés.",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadMedicaments();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Erreur lors de la suppression du médicament!",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur: " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

}
