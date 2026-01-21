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

public class SupprimerMedicamentFrame extends JFrame {
    private JTextField txtSearch;
    private JTable tableMedicaments;
    private DefaultTableModel tableModel;
    private JButton btnSearch, btnDelete, btnCancel, btnRefresh;
    private MedicamentBD medicamentBD;
    private StockBD stockBD;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public SupprimerMedicamentFrame() {
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        initComponents();
        loadMedicaments();
    }

    private void initComponents() {
        setTitle("Supprimer un Médicament");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(220, 53, 69));
        topPanel.setPreferredSize(new Dimension(1000, 50));

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

        // Tableau
        String[] columns = {"Réf", "Nom", "Fournisseur", "Prix", "Date Fab.", "Date Exp.", "Stock", "Statut"};
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
        JLabel lblInfo = new JLabel("⚠ Attention: La suppression d'un médicament supprimera aussi son stock!");
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
                StockMedicament stock = stockBD.rechercherParRef(med.getRefMedicament());
                String stockQte = stock != null ? String.valueOf(stock.getQuantiteProduit()) : "N/A";
                String statut = med.estPerime() ? "⚠ PÉRIMÉ" : "✓ Valide";

                tableModel.addRow(new Object[]{
                        med.getRefMedicament(),
                        med.getNom(),
                        "Fournisseur " + med.getNumFournisseur(),
                        //String.format("%.2f DT", med.getPrix()),
                        dateFormat.format(med.getDateFabrication()),
                        dateFormat.format(med.getDateExpiration()),
                        stockQte,
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
                StockMedicament stock = stockBD.rechercherParRef(med.getRefMedicament());
                String stockQte = stock != null ? String.valueOf(stock.getQuantiteProduit()) : "N/A";
                String statut = med.estPerime() ? "⚠ PÉRIMÉ" : "✓ Valide";

                tableModel.addRow(new Object[]{
                        med.getRefMedicament(),
                        med.getNom(),
                        "Fournisseur " + med.getNumFournisseur(),
                        //String.format("%.2f DT", med.getPrix()),
                        dateFormat.format(med.getDateFabrication()),
                        dateFormat.format(med.getDateExpiration()),
                        stockQte,
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
        String stock = (String) tableModel.getValueAt(selectedRow, 6);

        // Confirmation
        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer ce médicament?\n\n" +
                        "Référence: " + refMedicament + "\n" +
                        "Nom: " + nomMedicament + "\n" +
                        "Stock actuel: " + stock + "\n\n" +
                        "⚠ Cette action est irréversible!",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Supprimer d'abord le stock (clé étrangère)
                boolean stockDeleted = stockBD.supprimer(refMedicament);

                // Puis supprimer le médicament
                boolean medDeleted = medicamentBD.supprimer(refMedicament);

                if (medDeleted) {
                    JOptionPane.showMessageDialog(this,
                            "Médicament supprimé avec succès!\n" +
                                    (stockDeleted ? "Stock également supprimé." : ""),
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Rafraîchir le tableau
                    loadMedicaments();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Erreur lors de la suppression du médicament!",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur de base de données: " + ex.getMessage(),
                        "Erreur BD",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}