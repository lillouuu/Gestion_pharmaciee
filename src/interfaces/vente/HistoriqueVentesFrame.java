package interfaces.vente;

import entite.Vente;
import entite.VoieVente;
import entitebd.VenteBD;
import entitebd.VoieVenteBD;
import entitebd.ClientBD;
import entitebd.MedicamentBD;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * ✅ ENHANCED: Frame with search by name AND CNAM code
 */
public class HistoriqueVentesFrame extends JFrame {
    private VenteBD venteBD;
    private VoieVenteBD voieVenteBD;
    private ClientBD clientBD;
    private MedicamentBD medicamentBD;

    private JTable tableVentes;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> cmbFiltre;
    private JTextField txtRechercheNom;        // ✅ NEW: Search by name
    private JTextField txtRechercheCodeCnam;   // ✅ NEW: Search by CNAM code
    private JTextArea txtDetails;

    public HistoriqueVentesFrame() {
        venteBD = new VenteBD();
        voieVenteBD = new VoieVenteBD();
        clientBD = new ClientBD();
        medicamentBD = new MedicamentBD();

        initComponents();
        chargerVentes();
    }

    private void initComponents() {
        setTitle("Historique des Ventes");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0XAABDCF));
        topPanel.setPreferredSize(new Dimension(1200, 50));

        JLabel titleLabel = new JLabel("📜 Historique des Ventes");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0X5C7CB7));
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JPanel topSplitPanel = createTablePanel();
        splitPane.setTopComponent(topSplitPanel);

        JPanel bottomSplitPanel = createDetailsPanel();
        splitPane.setBottomComponent(bottomSplitPanel);

        splitPane.setDividerLocation(450);
        add(splitPane, BorderLayout.CENTER);

        // Bottom buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnActualiser = new JButton("🔄 Actualiser");
        btnActualiser.setBackground(new Color(0X7A958F));
        btnActualiser.setForeground(Color.WHITE);
        btnActualiser.setFocusPainted(false);
        btnActualiser.addActionListener(e -> chargerVentes());

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setBackground(new Color(0XCA8E9E));
        btnFermer.setForeground(Color.WHITE);
        btnFermer.setFocusPainted(false);
        btnFermer.addActionListener(e -> dispose());

        btnPanel.add(btnActualiser);
        btnPanel.add(btnFermer);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // ✅ ENHANCED: Filter and search panel
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtres et Recherche"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Time filter
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("Période:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        cmbFiltre = new JComboBox<>(new String[]{
                "Toutes les ventes", "Aujourd'hui", "Cette semaine", "Ce mois"
        });
        cmbFiltre.addActionListener(e -> chargerVentes());
        filterPanel.add(cmbFiltre, gbc);

        // Row 2: Search by name
        gbc.gridx = 0; gbc.gridy = 1;
        filterPanel.add(new JLabel("👤 Nom client:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        txtRechercheNom = new JTextField(20);
        txtRechercheNom.setToolTipText("Rechercher par nom ou prénom du client");
        txtRechercheNom.addCaretListener(e -> appliquerFiltreRecherche());
        filterPanel.add(txtRechercheNom, gbc);

        // Row 3: Search by CNAM code
        gbc.gridx = 0; gbc.gridy = 2;
        filterPanel.add(new JLabel("🔖 Code CNAM:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        txtRechercheCodeCnam = new JTextField(20);
        txtRechercheCodeCnam.setToolTipText("Rechercher par code CNAM");
        txtRechercheCodeCnam.addCaretListener(e -> appliquerFiltreRecherche());
        filterPanel.add(txtRechercheCodeCnam, gbc);

        // Clear button
        gbc.gridx = 2; gbc.gridy = 1; gbc.gridheight = 2;
        JButton btnClearSearch = new JButton("❌ Effacer");
        btnClearSearch.setToolTipText("Effacer les critères de recherche");
        btnClearSearch.addActionListener(e -> {
            txtRechercheNom.setText("");
            txtRechercheCodeCnam.setText("");
            appliquerFiltreRecherche();
        });
        filterPanel.add(btnClearSearch, gbc);
        gbc.gridheight = 1;

        panel.add(filterPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"N° Vente", "Date", "Client", "Code CNAM", "Employé", "Montant (DT)", "Date limite retour"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableVentes = new JTable(tableModel);
        tableVentes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableVentes.setRowHeight(25);

        // ✅ Add sorter for filtering
        sorter = new TableRowSorter<>(tableModel);
        tableVentes.setRowSorter(sorter);

        tableVentes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableVentes.getSelectedRow() != -1) {
                afficherDetails();
            }
        });

        // Adjust column widths
        tableVentes.getColumnModel().getColumn(0).setPreferredWidth(80);  // N° Vente
        tableVentes.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        tableVentes.getColumnModel().getColumn(2).setPreferredWidth(150); // Client
        tableVentes.getColumnModel().getColumn(3).setPreferredWidth(120); // Code CNAM
        tableVentes.getColumnModel().getColumn(4).setPreferredWidth(100); // Employé
        tableVentes.getColumnModel().getColumn(5).setPreferredWidth(100); // Montant
        tableVentes.getColumnModel().getColumn(6).setPreferredWidth(120); // Date limite

        JScrollPane scrollPane = new JScrollPane(tableVentes);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Détails de la vente"));

        txtDetails = new JTextArea();
        txtDetails.setEditable(false);
        txtDetails.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetails.setBackground(new Color(245, 245, 245));
        txtDetails.setText("Sélectionnez une vente pour voir les détails...");

        JScrollPane scrollPane = new JScrollPane(txtDetails);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void chargerVentes() {
        tableModel.setRowCount(0);

        try {
            ArrayList<Vente> ventes = venteBD.getAllVentes();

            for (Vente v : ventes) {
                String nomClient = "Client #" + v.getNumClient();
                String codeCnam = "-";

                try {
                    if (v.getNumClient() > 0) {
                        entite.Client client = clientBD.rechercherParId(v.getNumClient());
                        if (client != null) {
                            nomClient = client.getNom() + " " + client.getPrenom();
                            codeCnam = client.getCodeCnam() != null ? client.getCodeCnam() : "-";
                        }
                    } else {
                        nomClient = "Vente directe";
                        codeCnam = "-";
                    }
                } catch (Exception e) {
                    // Ignorer
                }

                tableModel.addRow(new Object[]{
                        v.getNumVente(),
                        v.getDateVente(),
                        nomClient,
                        codeCnam,  // ✅ NEW: CNAM code column
                        "Employé #" + v.getNumCarteEmp(),
                        String.format("%.2f", v.getMontantTotalVente()),
                        v.getDateLimRendreProduit()
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * ✅ NEW: Apply search filter by name or CNAM code
     */
    private void appliquerFiltreRecherche() {
        String rechercheNom = txtRechercheNom.getText().trim().toLowerCase();
        String rechercheCodeCnam = txtRechercheCodeCnam.getText().trim().toLowerCase();

        if (rechercheNom.isEmpty() && rechercheCodeCnam.isEmpty()) {
            // No filter
            sorter.setRowFilter(null);
        } else {
            // Filter by name OR CNAM code
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String nomClient = entry.getStringValue(2).toLowerCase();  // Column 2: Client
                    String codeCnam = entry.getStringValue(3).toLowerCase();   // Column 3: Code CNAM

                    boolean matchNom = rechercheNom.isEmpty() || nomClient.contains(rechercheNom);
                    boolean matchCodeCnam = rechercheCodeCnam.isEmpty() || codeCnam.contains(rechercheCodeCnam);

                    // Both conditions must match (AND logic)
                    return matchNom && matchCodeCnam;
                }
            });
        }
    }

    private void afficherDetails() {
        int selectedRow = tableVentes.getSelectedRow();
        if (selectedRow == -1) return;

        try {
            // ✅ Convert view row to model row (important with filtering/sorting)
            int modelRow = tableVentes.convertRowIndexToModel(selectedRow);
            int numVente = (Integer) tableModel.getValueAt(modelRow, 0);

            Vente vente = venteBD.getVenteById(numVente);
            ArrayList<VoieVente> lignes = voieVenteBD.getLignesParVente(numVente);

            if (vente != null) {
                StringBuilder details = new StringBuilder();
                details.append("═══════════════════════════════════════\n");
                details.append("        DÉTAILS DE LA VENTE #").append(numVente).append("\n");
                details.append("═══════════════════════════════════════\n\n");

                details.append("📅 Date: ").append(vente.getDateVente()).append("\n");
                details.append("📅 Date limite retour: ").append(vente.getDateLimRendreProduit()).append("\n");
                details.append("👤 Employé: #").append(vente.getNumCarteEmp()).append("\n");

                if (vente.getNumClient() > 0) {
                    try {
                        entite.Client client = clientBD.rechercherParId(vente.getNumClient());
                        if (client != null) {
                            details.append("👥 Client: ").append(client.getNom()).append(" ").append(client.getPrenom()).append("\n");
                            details.append("   Code CNAM: ").append(client.getCodeCnam() != null ? client.getCodeCnam() : "N/A").append("\n");
                            details.append("   Points fidélité actuels: ").append(client.getPointFidelite()).append("\n");
                        }
                    } catch (Exception e) {
                        details.append("👥 Client: #").append(vente.getNumClient()).append("\n");
                    }
                } else {
                    details.append("👥 Client: Vente directe (sans client)\n");
                }

                details.append("\n═══════════════════════════════════════\n");
                details.append("PRODUITS VENDUS:\n");
                details.append("═══════════════════════════════════════\n\n");

                double total = 0.0;
                for (VoieVente lv : lignes) {
                    try {
                        entite.Medicament med = medicamentBD.rechercherParRef(lv.getRefMedicament());
                        String nomMed = med != null ? med.getNom() : "Médicament #" + lv.getRefMedicament();

                        details.append("• ").append(nomMed).append("\n");
                        details.append("  Quantité: ").append(lv.getQuantite()).append("\n");
                        details.append("  Prix unitaire: ").append(String.format("%.2f DT", lv.getPrixUnitaire())).append("\n");
                        details.append("  Sous-total: ").append(String.format("%.2f DT", lv.getPrixTotalVoieVente())).append("\n\n");

                        total += lv.getPrixTotalVoieVente();
                    } catch (Exception e) {
                        details.append("• Médicament #").append(lv.getRefMedicament()).append("\n");
                        details.append("  Quantité: ").append(lv.getQuantite()).append("\n\n");
                    }
                }

                details.append("═══════════════════════════════════════\n");
                details.append("MONTANT TOTAL: ").append(String.format("%.2f DT", vente.getMontantTotalVente())).append("\n");
                details.append("═══════════════════════════════════════\n");

                txtDetails.setText(details.toString());
                txtDetails.setCaretPosition(0);
            }

        } catch (SQLException e) {
            txtDetails.setText("Erreur lors du chargement des détails:\n" + e.getMessage());
        }
    }
}