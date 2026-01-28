package interfaces.commande;

import entite.*;
import entitebd.StockBD;
import entitebd.MedicamentBD;
import entitebd.FournisseurBD;
import gestion.GestionCommande;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreerCommandeFrame extends JFrame {
    private GestionCommande gestionCommande;
    private MedicamentBD medicamentBD;
    private FournisseurBD fournisseurBD;

    private JComboBox<MedicamentItem> cmbMedicament;
    private JSpinner spnQuantite;
    private JTextField txtPrixUnitaire;
    private JTextField txtRemise;
    private JTextField txtImpots;
    private JTextField txtDateAchat;
    private JTextField txtDateLimite;
    private JComboBox<FournisseurItem> cmbFournisseur;
    private JTextField txtNumCarteEmp;

    private DefaultTableModel tableModel;
    private JTable tableLignes;
    private ArrayList<VoieCommande> lignesCommande;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public CreerCommandeFrame() {
        gestionCommande = new GestionCommande();
        medicamentBD = new MedicamentBD();
        fournisseurBD = new FournisseurBD();
        lignesCommande = new ArrayList<>();

        initComponents();
        chargerMedicaments();
        chargerFournisseurs();
    }

    private void initComponents() {
        setTitle("Créer une Commande");
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel supérieur - Informations commande
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Panel central - Ajout de lignes
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Panel inférieur - Actions
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Informations Commande"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Date d'achat
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Date d'achat:"), gbc);

        gbc.gridx = 1;
        txtDateAchat = new JTextField(LocalDate.now().toString(), 15);
        panel.add(txtDateAchat, gbc);

        // Date limite
        gbc.gridx = 2;
        panel.add(new JLabel("Date limite:"), gbc);

        gbc.gridx = 3;
        txtDateLimite = new JTextField(LocalDate.now().plusDays(30).toString(), 15);
        panel.add(txtDateLimite, gbc);

        // Fournisseur
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Fournisseur *:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3;
        cmbFournisseur = new JComboBox<>();
        panel.add(cmbFournisseur, gbc);
        gbc.gridwidth = 1;

        // Numéro carte employé
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Num Carte Emp:"), gbc);

        gbc.gridx = 1;
        txtNumCarteEmp = new JTextField("1", 15);
        panel.add(txtNumCarteEmp, gbc);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Lignes de Commande"));

        // Panel ajout ligne
        JPanel ajoutPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Médicament avec bouton ajouter
        gbc.gridx = 0; gbc.gridy = 0;
        ajoutPanel.add(new JLabel("Médicament:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JPanel medPanel = new JPanel(new BorderLayout(5, 0));
        cmbMedicament = new JComboBox<>();
        cmbMedicament.setEditable(true);
        cmbMedicament.addActionListener(e -> onMedicamentSelected());
        medPanel.add(cmbMedicament, BorderLayout.CENTER);

        JButton btnNouveauMed = new JButton("➕ Nouveau");
        btnNouveauMed.setToolTipText("Ajouter un nouveau médicament");
        btnNouveauMed.setBackground(new Color(40, 167, 69));
        btnNouveauMed.setForeground(Color.WHITE);
        btnNouveauMed.setFocusPainted(false);
        btnNouveauMed.addActionListener(e -> ajouterNouveauMedicament());
        medPanel.add(btnNouveauMed, BorderLayout.EAST);

        ajoutPanel.add(medPanel, gbc);

        // Quantité
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        ajoutPanel.add(new JLabel("Quantité:"), gbc);

        gbc.gridx = 1;
        spnQuantite = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        ajoutPanel.add(spnQuantite, gbc);

        // Prix unitaire
        gbc.gridx = 2;
        ajoutPanel.add(new JLabel("Prix Unit.:"), gbc);

        gbc.gridx = 3;
        txtPrixUnitaire = new JTextField("0.0", 10);
        ajoutPanel.add(txtPrixUnitaire, gbc);

        // Remise
        gbc.gridx = 0; gbc.gridy = 2;
        ajoutPanel.add(new JLabel("Remise (%):"), gbc);

        gbc.gridx = 1;
        txtRemise = new JTextField("0", 10);
        ajoutPanel.add(txtRemise, gbc);

        // Impôts
        gbc.gridx = 2;
        ajoutPanel.add(new JLabel("Impôts (%):"), gbc);

        gbc.gridx = 3;
        txtImpots = new JTextField("19", 10);
        ajoutPanel.add(txtImpots, gbc);

        // Bouton ajouter
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        JButton btnAjouter = new JButton("➕ Ajouter à la commande");
        btnAjouter.setBackground(new Color(34, 139, 34));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.addActionListener(e -> ajouterLigne());
        ajoutPanel.add(btnAjouter, gbc);

        panel.add(ajoutPanel, BorderLayout.NORTH);

        // Table des lignes
        String[] columns = {"Médicament", "Quantité", "Prix Unit.", "Remise %", "Impôts %", "Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableLignes = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableLignes);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnContinuer = new JButton("➕ Continuer");
        btnContinuer.setBackground(new Color(0, 123, 255));
        btnContinuer.setForeground(Color.WHITE);
        btnContinuer.addActionListener(e -> continuerAjout());

        JButton btnTerminer = new JButton("✅ Terminer et Créer");
        btnTerminer.setBackground(new Color(40, 167, 69));
        btnTerminer.setForeground(Color.WHITE);
        btnTerminer.addActionListener(e -> creerCommande());

        JButton btnAnnuler = new JButton("❌ Annuler");
        btnAnnuler.setBackground(new Color(220, 53, 69));
        btnAnnuler.setForeground(Color.WHITE);
        btnAnnuler.addActionListener(e -> dispose());

        panel.add(btnContinuer);
        panel.add(btnTerminer);
        panel.add(btnAnnuler);

        return panel;
    }

    private void chargerMedicaments() {
        try {
            List<Medicament> medicaments = medicamentBD.listerTous();
            cmbMedicament.removeAllItems();

            for (Medicament med : medicaments) {
                cmbMedicament.addItem(new MedicamentItem(med));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des médicaments: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chargerFournisseurs() {
        try {
            List<Fournisseur> fournisseurs = fournisseurBD.listerTous();
            cmbFournisseur.removeAllItems();

            for (Fournisseur f : fournisseurs) {
                cmbFournisseur.addItem(new FournisseurItem(f));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des fournisseurs: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onMedicamentSelected() {
        MedicamentItem selected = (MedicamentItem) cmbMedicament.getSelectedItem();
        if (selected != null) {
            try {
                StockBD stockBD = new StockBD();
                // ✅ REFACTORED: Obtenir le premier stock (peut y avoir plusieurs lots)
                List<StockMedicament> stocks = stockBD.getStocksParExpiration(selected.getMedicament().getRefMedicament());
                if (stocks != null && !stocks.isEmpty()) {
                    // Prendre le prix d'achat du premier lot disponible
                    txtPrixUnitaire.setText(String.valueOf(stocks.get(0).getPrixAchat()));
                } else {
                    txtPrixUnitaire.setText("0.0");
                }
            } catch (Exception e) {
                txtPrixUnitaire.setText("0.0");
            }
        }
    }

    /**
     * ✅ REFACTORED: Dialogue pour ajouter un nouveau médicament (SANS fournisseur, SANS dates)
     * Les dates seront ajoutées lors de la réception de la commande dans le stock
     */
    private void ajouterNouveauMedicament() {
        String nom = JOptionPane.showInputDialog(
                this,
                "Entrez le nom du médicament :",
                "Nouveau médicament",
                JOptionPane.PLAIN_MESSAGE
        );

        // Annulation ou nom vide
        if (nom == null || nom.trim().isEmpty()) {
            return;
        }

        try {
            // ✅ REFACTORED: Création du médicament SANS fournisseur et SANS dates
            Medicament med = new Medicament();
            med.setNom(nom.trim());
            med.setDescriptio(""); // description vide

            // ✅ Insertion UNIQUEMENT du nom et description
            int refMedicament = medicamentBD.ajouter(med);

            if (refMedicament > 0) {
                med.setRefMedicament(refMedicament);

                // Recharger la liste des médicaments
                chargerMedicaments();

                // Sélectionner automatiquement le nouveau médicament
                for (int i = 0; i < cmbMedicament.getItemCount(); i++) {
                    MedicamentItem item = cmbMedicament.getItemAt(i);
                    if (item.getMedicament().getRefMedicament() == refMedicament) {
                        cmbMedicament.setSelectedIndex(i);
                        break;
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "✅ Médicament ajouté dans la base!\n\n" +
                                "ℹ️ Les stocks seront créés lors de la réception de la commande.\n" +
                                "Les dates de fabrication et d'expiration seront saisies à ce moment.",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'ajout : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void ajouterLigne() {
        try {
            MedicamentItem medItem = (MedicamentItem) cmbMedicament.getSelectedItem();
            if (medItem == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un médicament");
                return;
            }

            int quantite = (Integer) spnQuantite.getValue();
            double prixUnit = Double.parseDouble(txtPrixUnitaire.getText());
            double remise = Double.parseDouble(txtRemise.getText());
            double impots = Double.parseDouble(txtImpots.getText());

            VoieCommande ligne = new VoieCommande(0, medItem.getMedicament().getRefMedicament(),
                    quantite, prixUnit, remise, impots);

            lignesCommande.add(ligne);

            double total = ligne.calculerTotal();
            tableModel.addRow(new Object[]{
                    medItem.getMedicament().getNom(),
                    quantite,
                    prixUnit,
                    remise,
                    impots,
                    String.format("%.2f DT", total)
            });

            spnQuantite.setValue(1);

            JOptionPane.showMessageDialog(this, "Ligne ajoutée avec succès!");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez entrer des valeurs numériques valides",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void continuerAjout() {
        int choix = JOptionPane.showConfirmDialog(this,
                "Voulez-vous ajouter une autre ligne?",
                "Continuer",
                JOptionPane.YES_NO_OPTION);

        if (choix == JOptionPane.NO_OPTION) {
            creerCommande();
        }
    }

    private void creerCommande() {
        if (lignesCommande.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez ajouter au moins une ligne à la commande",
                    "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ✅ REFACTORED: Vérifier qu'un fournisseur est sélectionné
        FournisseurItem fournisseurItem = (FournisseurItem) cmbFournisseur.getSelectedItem();
        if (fournisseurItem == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un fournisseur!",
                    "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Commande commande = new Commande();
            commande.setDateAchat(txtDateAchat.getText());
            commande.setDateLimRendreProduit(txtDateLimite.getText());
            commande.setNumFournisseur(fournisseurItem.getFournisseur().getNumFournisseur());
            commande.setNumCarteEmp(Integer.parseInt(txtNumCarteEmp.getText()));

            int numCommande = gestionCommande.creerCommande(commande, lignesCommande);

            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(numCommande);

            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════\n");
            sb.append("       BILAN DE LA COMMANDE\n");
            sb.append("═══════════════════════════════════════\n\n");
            sb.append("Numéro Commande: #").append(numCommande).append("\n");
            sb.append("Fournisseur: ").append(fournisseurItem.getFournisseur().getNomFournisseur()).append("\n");
            sb.append("Date d'achat: ").append(bilan.getCommande().getDateAchat()).append("\n");
            sb.append("Statut: ").append(bilan.getCommande().getStatut()).append("\n");
            sb.append("Nombre de lignes: ").append(bilan.getNombreLignes()).append("\n");
            sb.append("Total: ").append(String.format("%.2f DT", bilan.getTotal())).append("\n");
            sb.append("\n═══════════════════════════════════════\n");
            sb.append("ℹ️ Les stocks seront créés lors de la réception\n");

            JOptionPane.showMessageDialog(this, sb.toString(),
                    "Commande Créée", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la création: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Numéro de carte employé invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Classe interne pour encapsuler un Medicament dans le JComboBox
     */
    private static class MedicamentItem {
        private Medicament medicament;

        public MedicamentItem(Medicament medicament) {
            this.medicament = medicament;
        }

        public Medicament getMedicament() {
            return medicament;
        }

        @Override
        public String toString() {
            return medicament.getRefMedicament() + " - " + medicament.getNom();
        }
    }

    /**
     * ✅ NEW: Classe interne pour encapsuler un Fournisseur dans le JComboBox
     */
    private static class FournisseurItem {
        private Fournisseur fournisseur;

        public FournisseurItem(Fournisseur fournisseur) {
            this.fournisseur = fournisseur;
        }

        public Fournisseur getFournisseur() {
            return fournisseur;
        }

        @Override
        public String toString() {
            return fournisseur.getNumFournisseur() + " - " + fournisseur.getNomFournisseur() +
                    " (" + fournisseur.getTelephone() + ")";
        }
    }
}