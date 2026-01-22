package interfaces.client;

import entite.Client;
import entite.Vente;
import entite.VoieVente;
import entitebd.ClientBD;
import entitebd.VenteBD;
import entitebd.VoieVenteBD;
import entitebd.MedicamentBD;
import gestion.GestionVente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class HistoriqueClientFrame extends JFrame {
    private ClientBD clientBD;
    private VenteBD venteBD;
    private VoieVenteBD voieVenteBD;
    private MedicamentBD medicamentBD;
    private GestionVente gestionVente;

    private JTextField txtCodeCnam, txtNomClient;
    private JLabel lblInfoClient;
    private JTable tableAchats;
    private DefaultTableModel tableModel;
    private JTextArea txtDetails;
    private JLabel lblStatistiques;

    private Client clientActuel;

    public HistoriqueClientFrame() {
        clientBD = new ClientBD();
        venteBD = new VenteBD();
        voieVenteBD = new VoieVenteBD();
        medicamentBD = new MedicamentBD();
        gestionVente = new GestionVente();

        initComponents();
    }

    private void initComponents() {
        setTitle("Historique d'Achat Client");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0, 123, 255));
        topPanel.setPreferredSize(new Dimension(1200, 50));

        JLabel titleLabel = new JLabel("📜 Historique d'Achat Client");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JPanel tablePanel = createTablePanel();
        splitPane.setTopComponent(tablePanel);

        JPanel detailsPanel = createDetailsPanel();
        splitPane.setBottomComponent(detailsPanel);

        splitPane.setDividerLocation(400);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setBackground(new Color(108, 117, 125));
        btnFermer.setForeground(Color.WHITE);
        btnFermer.setFocusPainted(false);
        btnFermer.addActionListener(e -> dispose());

        btnPanel.add(btnFermer);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Rechercher Client"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Code CNAM:"), gbc);

        gbc.gridx = 1;
        JPanel cnamPanel = new JPanel(new BorderLayout(5, 0));
        txtCodeCnam = new JTextField(15);
        txtCodeCnam.addActionListener(e -> rechercherClient());
        cnamPanel.add(txtCodeCnam, BorderLayout.CENTER);

        JButton btnSearch = new JButton("🔍");
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> rechercherClient());
        cnamPanel.add(btnSearch, BorderLayout.EAST);

        panel.add(cnamPanel, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Nom:"), gbc);

        gbc.gridx = 3;
        txtNomClient = new JTextField(20);
        txtNomClient.setEditable(false);
        txtNomClient.setBackground(new Color(240, 240, 240));
        panel.add(txtNomClient, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4;
        lblInfoClient = new JLabel("");
        lblInfoClient.setFont(new Font("Arial", Font.ITALIC, 12));
        panel.add(lblInfoClient, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Historique des achats"));

        String[] columns = {"N° Vente", "Date", "Montant (DT)", "Nb Produits", "Points gagnés"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableAchats = new JTable(tableModel);
        tableAchats.setRowHeight(25);
        tableAchats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableAchats.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableAchats.getSelectedRow() != -1) {
                afficherDetailsAchat();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableAchats);
        panel.add(scrollPane, BorderLayout.CENTER);

        lblStatistiques = new JLabel("Statistiques: -");
        lblStatistiques.setFont(new Font("Arial", Font.BOLD, 12));
        lblStatistiques.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(lblStatistiques, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Détails de l'achat"));

        txtDetails = new JTextArea();
        txtDetails.setEditable(false);
        txtDetails.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetails.setBackground(new Color(245, 245, 245));
        txtDetails.setText("Sélectionnez un achat pour voir les détails...");

        JScrollPane scrollPane = new JScrollPane(txtDetails);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void rechercherClient() {
        String codeCnam = txtCodeCnam.getText().trim();
        if (codeCnam.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir un code CNAM",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            clientActuel = clientBD.rechercherParCodeCnam(codeCnam);

            if (clientActuel == null) {
                JOptionPane.showMessageDialog(this,
                        "Aucun client trouvé avec ce code CNAM",
                        "Client introuvable", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                return;
            }

            txtNomClient.setText(clientActuel.getNom() + " " + clientActuel.getPrenom());
            lblInfoClient.setText("📧 " + clientActuel.getAdresseMail() +
                    " | 📞 " + clientActuel.getTelephone() +
                    " | ⭐ Points fidélité: " + clientActuel.getPointFidelite());

            chargerHistorique();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la recherche: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chargerHistorique() {
        if (clientActuel == null) return;

        tableModel.setRowCount(0);

        try {
            ArrayList<Vente> achats = gestionVente.obtenirHistoriqueClient(clientActuel.getNumClient());

            double totalDepense = 0.0;
            int totalAchats = achats.size();

            for (Vente v : achats) {
                ArrayList<VoieVente> lignes = voieVenteBD.getLignesParVente(v.getNumVente());
                int nbProduits = 0;
                for (VoieVente lv : lignes) {
                    nbProduits += lv.getQuantite();
                }

                int pointsGagnes = (int) (v.getMontantTotalVente() / 10);
                totalDepense += v.getMontantTotalVente();

                tableModel.addRow(new Object[]{
                        v.getNumVente(),
                        v.getDateVente(),
                        String.format("%.2f", v.getMontantTotalVente()),
                        nbProduits,
                        pointsGagnes
                });
            }

            lblStatistiques.setText(String.format(
                    "Total achats: %d | Dépense totale: %.2f DT | Moyenne par achat: %.2f DT",
                    totalAchats,
                    totalDepense,
                    totalAchats > 0 ? totalDepense / totalAchats : 0.0
            ));

            if (achats.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Ce client n'a pas encore effectué d'achats",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement de l'historique: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void afficherDetailsAchat() {
        int selectedRow = tableAchats.getSelectedRow();
        if (selectedRow == -1) return;

        try {
            int numVente = (Integer) tableModel.getValueAt(selectedRow, 0);
            Vente vente = venteBD.getVenteById(numVente);
            ArrayList<VoieVente> lignes = voieVenteBD.getLignesParVente(numVente);

            if (vente != null) {
                StringBuilder details = new StringBuilder();
                details.append("═══════════════════════════════════════\n");
                details.append("      DÉTAILS DE L'ACHAT #").append(numVente).append("\n");
                details.append("═══════════════════════════════════════\n\n");

                details.append("📅 Date d'achat: ").append(vente.getDateVente()).append("\n");
                details.append("📅 Date limite retour: ").append(vente.getDateLimRendreProduit()).append("\n");
                details.append("👤 Vendu par: Employé #").append(vente.getNumCarteEmp()).append("\n\n");

                details.append("═══════════════════════════════════════\n");
                details.append("PRODUITS ACHETÉS:\n");
                details.append("═══════════════════════════════════════\n\n");

                for (VoieVente lv : lignes) {
                    try {
                        entite.Medicament med = medicamentBD.rechercherParRef(lv.getRefMedicament());
                        String nomMed = med != null ? med.getNom() : "Produit #" + lv.getRefMedicament();

                        details.append("• ").append(nomMed).append("\n");
                        details.append("  Quantité: ").append(lv.getQuantite()).append(" unité(s)\n");
                        details.append("  Prix unitaire: ").append(String.format("%.2f DT", lv.getPrixUnitaire())).append("\n");
                        details.append("  Sous-total: ").append(String.format("%.2f DT", lv.getPrixTotalVoieVente())).append("\n\n");

                    } catch (Exception e) {
                        details.append("• Produit #").append(lv.getRefMedicament()).append("\n");
                        details.append("  Quantité: ").append(lv.getQuantite()).append("\n\n");
                    }
                }

                int pointsGagnes = (int) (vente.getMontantTotalVente() / 10);

                details.append("═══════════════════════════════════════\n");
                details.append("MONTANT TOTAL: ").append(String.format("%.2f DT", vente.getMontantTotalVente())).append("\n");
                details.append("Points fidélité gagnés: ").append(pointsGagnes).append(" points\n");
                details.append("═══════════════════════════════════════\n");

                txtDetails.setText(details.toString());
                txtDetails.setCaretPosition(0);
            }

        } catch (SQLException e) {
            txtDetails.setText("Erreur lors du chargement des détails:\n" + e.getMessage());
        }
    }

    private void clearForm() {
        clientActuel = null;
        txtNomClient.setText("");
        lblInfoClient.setText("");
        tableModel.setRowCount(0);
        lblStatistiques.setText("Statistiques: -");
        txtDetails.setText("Sélectionnez un achat pour voir les détails...");
    }
}
