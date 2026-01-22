package interfaces.vente;

import entite.*;
import entitebd.ClientBD;
import entitebd.MedicamentBD;
import entitebd.StockBD;
import gestion.GestionVente;
import exception.StockInsuffisantException;
import exception.ProduitNonTrouveException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NouvelleVenteFrame extends JFrame {
    private GestionVente gestionVente;
    private ClientBD clientBD;
    private MedicamentBD medicamentBD;
    private StockBD stockBD;

    private JTextField txtCodeCnam, txtNomClient;
    private JLabel lblClientInfo, lblPointsFidelite;
    private JComboBox<MedicamentItem> cmbMedicament;
    private JSpinner spnQuantite;
    private JTextField txtPrixUnitaire;
    private JTextField txtNumCarteEmp, txtDateVente, txtDateLimite;

    private DefaultTableModel tableModel;
    private JTable tableLignes;
    private JLabel lblTotal;
    private ArrayList<VoieVente> lignesVente;
    private Client clientActuel;

    private Map<Integer, String> medicamentNoms;

    public NouvelleVenteFrame() {
        gestionVente = new GestionVente();
        clientBD = new ClientBD();
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        lignesVente = new ArrayList<>();
        medicamentNoms = new HashMap<>();

        initComponents();
        chargerMedicaments();
    }

    private void initComponents() {
        setTitle("Nouvelle Vente");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(40, 167, 69));
        topPanel.setPreferredSize(new Dimension(1100, 50));

        JLabel titleLabel = new JLabel("🛒 Nouvelle Vente");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        JPanel produitPanel = createProduitPanel();
        mainPanel.add(produitPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Informations Vente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Code CNAM Client:"), gbc);

        gbc.gridx = 1;
        JPanel clientPanel = new JPanel(new BorderLayout(5, 0));
        txtCodeCnam = new JTextField(15);
        txtCodeCnam.addActionListener(e -> rechercherClient());
        clientPanel.add(txtCodeCnam, BorderLayout.CENTER);

        JButton btnSearchClient = new JButton("🔍");
        btnSearchClient.setFocusPainted(false);
        btnSearchClient.addActionListener(e -> rechercherClient());
        clientPanel.add(btnSearchClient, BorderLayout.EAST);

        panel.add(clientPanel, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Nom:"), gbc);

        gbc.gridx = 3;
        txtNomClient = new JTextField(15);
        txtNomClient.setEditable(false);
        txtNomClient.setBackground(new Color(240, 240, 240));
        panel.add(txtNomClient, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        lblClientInfo = new JLabel("");
        lblClientInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        panel.add(lblClientInfo, gbc);

        gbc.gridx = 2; gbc.gridwidth = 2;
        lblPointsFidelite = new JLabel("");
        lblPointsFidelite.setFont(new Font("Arial", Font.BOLD, 12));
        lblPointsFidelite.setForeground(new Color(255, 165, 0));
        panel.add(lblPointsFidelite, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("Date vente:"), gbc);

        gbc.gridx = 1;
        txtDateVente = new JTextField(LocalDate.now().toString(), 15);
        panel.add(txtDateVente, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Date limite retour:"), gbc);

        gbc.gridx = 3;
        txtDateLimite = new JTextField(LocalDate.now().plusDays(7).toString(), 15);
        panel.add(txtDateLimite, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Num Carte Emp:"), gbc);

        gbc.gridx = 1;
        txtNumCarteEmp = new JTextField("5", 15);
        panel.add(txtNumCarteEmp, gbc);

        return panel;
    }

    private JPanel createProduitPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel ajoutPanel = new JPanel(new GridBagLayout());
        ajoutPanel.setBorder(BorderFactory.createTitledBorder("Ajouter un produit"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        ajoutPanel.add(new JLabel("Médicament:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbMedicament = new JComboBox<>();
        cmbMedicament.addActionListener(e -> onMedicamentSelected());
        ajoutPanel.add(cmbMedicament, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        ajoutPanel.add(new JLabel("Quantité:"), gbc);

        gbc.gridx = 1;
        spnQuantite = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        ajoutPanel.add(spnQuantite, gbc);

        gbc.gridx = 2;
        ajoutPanel.add(new JLabel("Prix unitaire:"), gbc);

        gbc.gridx = 3;
        txtPrixUnitaire = new JTextField("0.0", 10);
        ajoutPanel.add(txtPrixUnitaire, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        JButton btnAjouter = new JButton("➕ Ajouter à la vente");
        btnAjouter.setBackground(new Color(40, 167, 69));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.setFocusPainted(false);
        btnAjouter.addActionListener(e -> ajouterLigne());
        ajoutPanel.add(btnAjouter, gbc);

        panel.add(ajoutPanel, BorderLayout.NORTH);

        String[] columns = {"Médicament", "Quantité", "Prix Unit.", "Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableLignes = new JTable(tableModel);
        tableLignes.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(tableLignes);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lignes de vente"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal = new JLabel("Total: 0.00 DT");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(new Color(40, 167, 69));
        totalPanel.add(lblTotal);
        panel.add(totalPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnValider = new JButton("✅ Valider la vente");
        btnValider.setPreferredSize(new Dimension(180, 40));
        btnValider.setBackground(new Color(40, 167, 69));
        btnValider.setForeground(Color.WHITE);
        btnValider.setFont(new Font("Arial", Font.BOLD, 14));
        btnValider.setFocusPainted(false);
        btnValider.addActionListener(e -> validerVente());

        JButton btnAnnuler = new JButton("❌ Annuler");
        btnAnnuler.setPreferredSize(new Dimension(120, 40));
        btnAnnuler.setBackground(new Color(220, 53, 69));
        btnAnnuler.setForeground(Color.WHITE);
        btnAnnuler.setFocusPainted(false);
        btnAnnuler.addActionListener(e -> dispose());

        panel.add(btnValider);
        panel.add(btnAnnuler);

        return panel;
    }

    private void chargerMedicaments() {
        try {
            List<Medicament> medicaments = medicamentBD.listerTous();
            cmbMedicament.removeAllItems();

            for (Medicament med : medicaments) {
                StockMedicament stock = stockBD.rechercherParRef(med.getRefMedicament());
                if (stock != null && stock.getQuantiteProduit() > 0 && !med.estPerime()) {
                    MedicamentItem item = new MedicamentItem(med, stock);
                    cmbMedicament.addItem(item);
                    medicamentNoms.put(med.getRefMedicament(), med.getNom());
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des médicaments: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onMedicamentSelected() {
        MedicamentItem selected = (MedicamentItem) cmbMedicament.getSelectedItem();
        if (selected != null) {
            txtPrixUnitaire.setText(String.valueOf(selected.getStock().getPrixVente()));
            spnQuantite.setValue(1);

            int max = selected.getStock().getQuantiteProduit();
            ((SpinnerNumberModel) spnQuantite.getModel()).setMaximum(max);
        }
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

            if (clientActuel != null) {
                txtNomClient.setText(clientActuel.getNom() + " " + clientActuel.getPrenom());
                lblClientInfo.setText("📧 " + clientActuel.getAdresseMail() + " | 📞 " + clientActuel.getTelephone());
                lblPointsFidelite.setText("⭐ Points fidélité: " + clientActuel.getPointFidelite());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Aucun client trouvé avec ce code CNAM",
                        "Client introuvable", JOptionPane.INFORMATION_MESSAGE);
                txtNomClient.setText("");
                lblClientInfo.setText("");
                lblPointsFidelite.setText("");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la recherche: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouterLigne() {
        MedicamentItem medItem = (MedicamentItem) cmbMedicament.getSelectedItem();
        if (medItem == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un médicament");
            return;
        }

        try {
            int quantite = (Integer) spnQuantite.getValue();
            double prixUnit = Double.parseDouble(txtPrixUnitaire.getText());

            if (quantite > medItem.getStock().getQuantiteProduit()) {
                JOptionPane.showMessageDialog(this,
                        "Stock insuffisant! Stock disponible: " + medItem.getStock().getQuantiteProduit(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            VoieVente ligne = new VoieVente();
            ligne.setRefMedicament(medItem.getMedicament().getRefMedicament());
            ligne.setQuantite(quantite);
            ligne.setPrixUnitaire(prixUnit);
            ligne.setPrixTotalVoieVente();

            lignesVente.add(ligne);

            tableModel.addRow(new Object[]{
                    medItem.getMedicament().getNom(),
                    quantite,
                    String.format("%.2f DT", prixUnit),
                    String.format("%.2f DT", ligne.getPrixTotalVoieVente())
            });

            calculerTotal();
            spnQuantite.setValue(1);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Prix unitaire invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculerTotal() {
        double total = 0.0;
        for (VoieVente ligne : lignesVente) {
            total += ligne.getPrixTotalVoieVente();
        }
        lblTotal.setText(String.format("Total: %.2f DT", total));
    }

    private void validerVente() {
        if (lignesVente.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez ajouter au moins un produit à la vente",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Vente vente = new Vente();
            vente.setDateVente(txtDateVente.getText());
            vente.setDateLimRendreProduit(txtDateLimite.getText());

            double total = 0.0;
            for (VoieVente lv : lignesVente) {
                total += lv.getPrixTotalVoieVente();
            }
            vente.setMontantTotalVente(total);

            if (clientActuel != null) {
                vente.setNumClient(clientActuel.getNumClient());
            } else {
                vente.setNumClient(0);
            }

            vente.setNumEmp(Integer.parseInt(txtNumCarteEmp.getText()));

            gestionVente.enregistrerVente(vente, lignesVente);

            StringBuilder facture = new StringBuilder();
            facture.append("═══════════════════════════════════════\n");
            facture.append("           FACTURE DE VENTE\n");
            facture.append("═══════════════════════════════════════\n\n");
            facture.append("N° Vente: #").append(vente.getNumVente()).append("\n");
            facture.append("Date: ").append(vente.getDateVente()).append("\n\n");

            if (clientActuel != null) {
                facture.append("Client: ").append(clientActuel.getNom()).append(" ").append(clientActuel.getPrenom()).append("\n");
                int pointsGagnes = (int) (total / 10);
                facture.append("Points gagnés: ").append(pointsGagnes).append("\n\n");
            }

            facture.append("DÉTAILS:\n");
            for (int i = 0; i < lignesVente.size(); i++) {
                VoieVente lv = lignesVente.get(i);
                String nomMed = medicamentNoms.get(lv.getRefMedicament());
                facture.append("• ").append(nomMed).append(" x").append(lv.getQuantite());
                facture.append(" = ").append(String.format("%.2f DT", lv.getPrixTotalVoieVente())).append("\n");
            }

            facture.append("\nTOTAL: ").append(String.format("%.2f DT", total)).append("\n");
            facture.append("═══════════════════════════════════════\n");

            JOptionPane.showMessageDialog(this, facture.toString(),
                    "Vente réussie", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (StockInsuffisantException e) {
            JOptionPane.showMessageDialog(this,
                    "Stock insuffisant: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (ProduitNonTrouveException e) {
            JOptionPane.showMessageDialog(this,
                    "Produit non trouvé: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Numéro de carte employé invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class MedicamentItem {
        private Medicament medicament;
        private StockMedicament stock;

        public MedicamentItem(Medicament medicament, StockMedicament stock) {
            this.medicament = medicament;
            this.stock = stock;
        }

        public Medicament getMedicament() {
            return medicament;
        }

        public StockMedicament getStock() {
            return stock;
        }

        @Override
        public String toString() {
            return medicament.getNom() + " (Stock: " + stock.getQuantiteProduit() +
                    ", Prix: " + String.format("%.2f DT", stock.getPrixVente()) + ")";
        }
    }
}