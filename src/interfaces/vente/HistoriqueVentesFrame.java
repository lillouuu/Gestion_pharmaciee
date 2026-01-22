package interfaces.vente;

import entite.Vente;
import entite.VoieVente;
import entitebd.VenteBD;
import entitebd.VoieVenteBD;
import entitebd.ClientBD;
import entitebd.MedicamentBD;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class HistoriqueVentesFrame extends JFrame {
    private VenteBD venteBD;
    private VoieVenteBD voieVenteBD;
    private ClientBD clientBD;
    private MedicamentBD medicamentBD;

    private JTable tableVentes;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbFiltre;
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

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0, 123, 255));
        topPanel.setPreferredSize(new Dimension(1200, 50));

        JLabel titleLabel = new JLabel("📜 Historique des Ventes");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JPanel topSplitPanel = createTablePanel();
        splitPane.setTopComponent(topSplitPanel);

        JPanel bottomSplitPanel = createDetailsPanel();
        splitPane.setBottomComponent(bottomSplitPanel);

        splitPane.setDividerLocation(450);
        add(splitPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnActualiser = new JButton("🔄 Actualiser");
        btnActualiser.setBackground(new Color(0, 123, 255));
        btnActualiser.setForeground(Color.WHITE);
        btnActualiser.setFocusPainted(false);
        btnActualiser.addActionListener(e -> chargerVentes());

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setBackground(new Color(108, 117, 125));
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

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtres"));

        filterPanel.add(new JLabel("Afficher:"));
        cmbFiltre = new JComboBox<>(new String[]{
                "Toutes les ventes", "Aujourd'hui", "Cette semaine", "Ce mois"
        });
        cmbFiltre.addActionListener(e -> chargerVentes());
        filterPanel.add(cmbFiltre);

        panel.add(filterPanel, BorderLayout.NORTH);

        String[] columns = {"N° Vente", "Date", "Client", "Employé", "Montant (DT)", "Date limite retour"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableVentes = new JTable(tableModel);
        tableVentes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableVentes.setRowHeight(25);
        tableVentes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableVentes.getSelectedRow() != -1) {
                afficherDetails();
            }
        });

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
                try {
                    if (v.getNumClient() > 0) {
                        entite.Client client = clientBD.rechercherParId(v.getNumClient());
                        if (client != null) {
                            nomClient = client.getNom() + " " + client.getPrenom();
                        }
                    } else {
                        nomClient = "Vente directe";
                    }
                } catch (Exception e) {
                    // Ignorer
                }

                tableModel.addRow(new Object[]{
                        v.getNumVente(),
                        v.getDateVente(),
                        nomClient,
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

    private void afficherDetails() {
        int selectedRow = tableVentes.getSelectedRow();
        if (selectedRow == -1) return;

        try {
            int numVente = (Integer) tableModel.getValueAt(selectedRow, 0);
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
                            details.append("   Code CNAM: ").append(client.getCodeCnam()).append("\n");
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