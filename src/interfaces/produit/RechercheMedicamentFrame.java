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

public class RechercheMedicamentFrame extends JFrame {
    private JTextField txtNom, txtRef;
    private JComboBox<String> cmbFournisseur;
    private JTable tableResultats;
    private DefaultTableModel tableModel;
    private JButton btnSearch, btnClear, btnClose;
    private JTextArea txtDetails;
    private MedicamentBD medicamentBD;
    private StockBD stockBD;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public RechercheMedicamentFrame() {
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        initComponents();
    }

    private void initComponents() {
        setTitle("Rechercher un Médicament");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0, 123, 255));
        topPanel.setPreferredSize(new Dimension(1100, 50));

        JLabel titleLabel = new JLabel("🔍 Rechercher un médicament");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de recherche
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
                "Critères de recherche",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(0, 123, 255)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Référence
        gbc.gridx = 0; gbc.gridy = 0;
        searchPanel.add(new JLabel("Référence:"), gbc);
        gbc.gridx = 1;
        txtRef = new JTextField(15);
        searchPanel.add(txtRef, gbc);

        // Nom
        gbc.gridx = 2; gbc.gridy = 0;
        searchPanel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 3;
        txtNom = new JTextField(15);
        searchPanel.add(txtNom, gbc);

        // Fournisseur
        gbc.gridx = 0; gbc.gridy = 1;
        searchPanel.add(new JLabel("Fournisseur:"), gbc);
        gbc.gridx = 1;
        // TODO: Charger depuis BD
        cmbFournisseur = new JComboBox<>(new String[]{"Tous", "Fournisseur 1", "Fournisseur 2", "Fournisseur 3"});
        searchPanel.add(cmbFournisseur, gbc);

        // Boutons de recherche
        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 2;
        JPanel searchBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnSearch = new JButton("🔍 Rechercher");
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> performSearch());
        searchBtnPanel.add(btnSearch);

        btnClear = new JButton("🔄 Réinitialiser");
        btnClear.setBackground(new Color(108, 117, 125));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);
        btnClear.addActionListener(e -> clearSearch());
        searchBtnPanel.add(btnClear);

        searchPanel.add(searchBtnPanel, gbc);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Split pane pour résultats et détails
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Panel résultats (tableau)
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Résultats de recherche"));

        String[] columns = {"Réf", "Nom", "Fournisseur", "Prix", "Stock", "Prix Vente", "Date Exp.", "Statut"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableResultats = new JTable(tableModel);
        tableResultats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableResultats.setRowHeight(25);
        tableResultats.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableResultats.getColumnModel().getColumn(1).setPreferredWidth(200);

        tableResultats.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableResultats.getSelectedRow() != -1) {
                showDetails();
            }
        });

        JScrollPane scrollTable = new JScrollPane(tableResultats);
        resultsPanel.add(scrollTable, BorderLayout.CENTER);

        // Panel détails
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Détails du médicament"));

        txtDetails = new JTextArea(8, 50);
        txtDetails.setEditable(false);
        txtDetails.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetails.setBackground(new Color(245, 245, 245));
        JScrollPane scrollDetails = new JScrollPane(txtDetails);
        detailsPanel.add(scrollDetails, BorderLayout.CENTER);

        splitPane.setTopComponent(resultsPanel);
        splitPane.setBottomComponent(detailsPanel);
        splitPane.setDividerLocation(350);

        mainPanel.add(splitPane, BorderLayout.CENTER);

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

        // Actions Enter
        txtRef.addActionListener(e -> performSearch());
        txtNom.addActionListener(e -> performSearch());
    }

    private void performSearch() {
        tableModel.setRowCount(0);
        txtDetails.setText("");

        String ref = txtRef.getText().trim();
        String nom = txtNom.getText().trim();
        int fournisseurIndex = cmbFournisseur.getSelectedIndex();

        if (ref.isEmpty() && nom.isEmpty() && fournisseurIndex == 0) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir au moins un critère de recherche!",
                    "Recherche",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            List<Medicament> medicaments = null;

            // Recherche par référence
            if (!ref.isEmpty()) {
                try {
                    int refMed = Integer.parseInt(ref);
                    Medicament med = medicamentBD.rechercherParRef(refMed);
                    if (med != null) {
                        medicaments = List.of(med);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "La référence doit être un nombre!",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // Recherche par nom
            else if (!nom.isEmpty()) {
                medicaments = medicamentBD.rechercherParNom(nom);
            }
            // Recherche par fournisseur
            else if (fournisseurIndex > 0) {
                medicaments = medicamentBD.listerParFournisseur(fournisseurIndex);
            }

            if (medicaments != null && !medicaments.isEmpty()) {
                for (Medicament med : medicaments) {
                    // Filtrer par fournisseur si spécifié avec nom
                    if (fournisseurIndex > 0 && med.getNumFournisseur() != fournisseurIndex) {
                        continue;
                    }

                    StockMedicament stock = stockBD.rechercherParRef(med.getRefMedicament());
                    String stockQte = stock != null ? String.valueOf(stock.getQuantiteProduit()) : "N/A";
                    String prixVente = stock != null ? String.format("%.2f DT", stock.getPrixVente()) : "N/A";
                    String statut = med.estPerime() ? "⚠ PÉRIMÉ" : "✓ Valide";

                    tableModel.addRow(new Object[]{
                            med.getRefMedicament(),
                            med.getNom(),
                            "Fournisseur " + med.getNumFournisseur(),
                            //String.format("%.2f DT", med.getPrix()),
                            stockQte,
                            prixVente,
                            dateFormat.format(med.getDateExpiration()),
                            statut
                    });
                }

                JLabel lblCount = new JLabel(medicaments.size() + " résultat(s) trouvé(s)");
                lblCount.setFont(new Font("Arial", Font.ITALIC, 11));
            } else {
                JOptionPane.showMessageDialog(this,
                        "Aucun médicament trouvé avec ces critères!",
                        "Résultat",
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

    private void showDetails() {
        int selectedRow = tableResultats.getSelectedRow();
        if (selectedRow == -1) return;

        try {
            int refMed = (int) tableModel.getValueAt(selectedRow, 0);
            Medicament med = medicamentBD.rechercherParRef(refMed);
            StockMedicament stock = stockBD.rechercherParRef(refMed);

            if (med != null) {
                StringBuilder details = new StringBuilder();
                details.append("═══════════════════════════════════════════════════════════════\n");
                details.append("                   INFORMATIONS DÉTAILLÉES\n");
                details.append("═══════════════════════════════════════════════════════════════\n\n");

                details.append("📋 MÉDICAMENT\n");
                details.append("  • Référence       : ").append(med.getRefMedicament()).append("\n");
                details.append("  • Nom             : ").append(med.getNom()).append("\n");
                details.append("  • Fournisseur     : Fournisseur ").append(med.getNumFournisseur()).append("\n");
                //details.append("  • Prix unitaire   : ").append(String.format("%.2f DT", med.getPrix())).append("\n");
                details.append("  • Date fabrication: ").append(dateFormat.format(med.getDateFabrication())).append("\n");
                details.append("  • Date expiration : ").append(dateFormat.format(med.getDateExpiration())).append("\n");
                details.append("  • Description     : ").append(med.getDescriptio() != null ? med.getDescriptio() : "N/A").append("\n\n");

                if (stock != null) {
                    details.append("📦 STOCK\n");
                    details.append("  • Quantité en stock: ").append(stock.getQuantiteProduit()).append("\n");
                    details.append("  • Prix d'achat     : ").append(String.format("%.2f DT", stock.getPrixAchat())).append("\n");
                    details.append("  • Prix de vente    : ").append(String.format("%.2f DT", stock.getPrixVente())).append("\n");
                    details.append("  • Seuil minimal    : ").append(stock.getSeuilMin()).append("\n");
                    details.append("  • Marge bénéficiaire: ").append(String.format("%.2f DT", stock.getPrixVente() - stock.getPrixAchat())).append("\n");

                    if (stock.Alerte()) {
                        details.append("\n  ⚠ ALERTE: Stock faible! (Quantité ≤ seuil minimal)\n");
                    }
                } else {
                    details.append("📦 STOCK\n");
                    details.append("  • Pas d'information de stock disponible\n");
                }

                if (med.estPerime()) {
                    details.append("\n⚠ ATTENTION: Ce médicament est PÉRIMÉ!\n");
                }

                txtDetails.setText(details.toString());
            }
        } catch (SQLException ex) {
            txtDetails.setText("Erreur lors du chargement des détails: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void clearSearch() {
        txtRef.setText("");
        txtNom.setText("");
        cmbFournisseur.setSelectedIndex(0);
        tableModel.setRowCount(0);
        txtDetails.setText("");
        txtRef.requestFocus();
    }
}