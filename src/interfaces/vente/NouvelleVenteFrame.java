package interfaces.vente;

import entite.*;
import entitebd.ClientBD;
import entitebd.MedicamentBD;
import entitebd.StockBD;
import gestion.GestionVente;
import gestion.GestionStock;
import exception.StockInsuffisantException;
import exception.ProduitNonTrouveException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ✅ VERSION FEFO INTELLIGENTE - CORRIGÉE
 * - Vérifie le stock TOTAL avant d'ajouter une ligne
 * - Gère automatiquement la répartition sur plusieurs lots
 * - Affiche clairement les lots qui seront utilisés
 * - ✅ REFACTORED: Dates gérées depuis StockMedicament (pas Medicament)
 * - ✅ FIXED: Restauration des quantités réservées en cas d'erreur de validation
 */
public class NouvelleVenteFrame extends JFrame {
    private GestionVente gestionVente;
    private GestionStock gestionStock;
    private ClientBD clientBD;
    private MedicamentBD medicamentBD;
    private StockBD stockBD;

    private JTextField txtCodeCnam, txtNomClient;
    private JLabel lblClientInfo, lblPointsFidelite;
    private JComboBox<MedicamentItem> cmbMedicament;
    private JSpinner spnQuantite;
    private JTextField txtPrixUnitaire;
    private JTextField txtNumCarteEmp, txtDateVente, txtDateLimite;
    private JLabel lblStockInfo;

    private DefaultTableModel tableModel;
    private JTable tableLignes;
    private JLabel lblTotal;
    private ArrayList<VoieVente> lignesVente;
    private Client clientActuel;

    private Map<Integer, String> medicamentNoms;

    // ✅ Tracker pour les quantités déjà ajoutées dans le panier
    private Map<Integer, Integer> quantitesReservees;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public NouvelleVenteFrame() {
        gestionVente = new GestionVente();
        gestionStock = new GestionStock();
        clientBD = new ClientBD();
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        lignesVente = new ArrayList<>();
        medicamentNoms = new HashMap<>();
        quantitesReservees = new HashMap<>();

        initComponents();
        chargerMedicaments();
    }

    private void initComponents() {
        setTitle("🛒 Nouvelle Vente - FEFO Intelligent");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(40, 167, 69));
        topPanel.setPreferredSize(new Dimension(1200, 50));

        JLabel titleLabel = new JLabel("🛒 Nouvelle Vente (FEFO: First Expired, First Out)");
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
        ajoutPanel.setBorder(BorderFactory.createTitledBorder("Ajouter un produit (FEFO activé)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        ajoutPanel.add(new JLabel("Médicament:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbMedicament = new JComboBox<>();
        cmbMedicament.addActionListener(e -> onMedicamentSelected());
        ajoutPanel.add(cmbMedicament, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        lblStockInfo = new JLabel(" ");
        lblStockInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblStockInfo.setForeground(new Color(0, 100, 200));
        ajoutPanel.add(lblStockInfo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        ajoutPanel.add(new JLabel("Quantité:"), gbc);

        gbc.gridx = 1;
        spnQuantite = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spnQuantite.addChangeListener(e -> updateStockInfoOnQuantityChange());
        ajoutPanel.add(spnQuantite, gbc);

        gbc.gridx = 2;
        ajoutPanel.add(new JLabel("Prix unitaire:"), gbc);

        gbc.gridx = 3;
        txtPrixUnitaire = new JTextField("0.0", 10);
        ajoutPanel.add(txtPrixUnitaire, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        JButton btnAjouter = new JButton("➕ Ajouter à la vente");
        btnAjouter.setBackground(new Color(40, 167, 69));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.setFocusPainted(false);
        btnAjouter.addActionListener(e -> ajouterLigne());
        ajoutPanel.add(btnAjouter, gbc);

        panel.add(ajoutPanel, BorderLayout.NORTH);

        String[] columns = {"Médicament", "Quantité", "Prix Unit.", "Total", "Lots utilisés"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableLignes = new JTable(tableModel);
        tableLignes.setRowHeight(25);
        tableLignes.getColumnModel().getColumn(4).setPreferredWidth(250);
        JScrollPane scrollPane = new JScrollPane(tableLignes);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lignes de vente"));
        panel.add(scrollPane, BorderLayout.CENTER);

        // ✅ Panel pour le total et le bouton supprimer
        JPanel bottomPanelProduit = new JPanel(new BorderLayout());

        // Bouton supprimer à gauche
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSupprimerLigne = new JButton("🗑️ Supprimer la ligne sélectionnée");
        btnSupprimerLigne.setBackground(new Color(220, 53, 69));
        btnSupprimerLigne.setForeground(Color.WHITE);
        btnSupprimerLigne.setFocusPainted(false);
        btnSupprimerLigne.addActionListener(e -> supprimerLigneSelectionnee());
        leftPanel.add(btnSupprimerLigne);
        bottomPanelProduit.add(leftPanel, BorderLayout.WEST);

        // Total à droite
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal = new JLabel("Total: 0.00 DT");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(new Color(40, 167, 69));
        totalPanel.add(lblTotal);
        bottomPanelProduit.add(totalPanel, BorderLayout.EAST);

        panel.add(bottomPanelProduit, BorderLayout.SOUTH);

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
        btnValider.setOpaque(true);
        btnValider.setBorderPainted(false);
        btnValider.setContentAreaFilled(true);
        btnValider.addActionListener(e -> validerVente());

        JButton btnAnnuler = new JButton("❌ Annuler");
        btnAnnuler.setPreferredSize(new Dimension(120, 40));
        btnAnnuler.setBackground(new Color(220, 53, 69));
        btnAnnuler.setForeground(Color.WHITE);
        btnAnnuler.setFocusPainted(false);
        btnAnnuler.setOpaque(true);
        btnAnnuler.setBorderPainted(false);
        btnAnnuler.setContentAreaFilled(true);
        btnAnnuler.addActionListener(e -> dispose());

        panel.add(btnValider);
        panel.add(btnAnnuler);

        return panel;
    }

    /**
     * ✅ REFACTORED: Charger uniquement les médicaments avec stock TOTAL > 0
     * Les vérifications de péremption se font au niveau des STOCKS, pas des médicaments
     */
    private void chargerMedicaments() {
        try {
            List<Medicament> medicaments = medicamentBD.listerTous();
            cmbMedicament.removeAllItems();

            int nbMedicamentsCharges = 0;

            for (Medicament med : medicaments) {
                // ✅ Obtenir tous les stocks pour ce médicament
                List<StockMedicament> stocks = stockBD.getStocksParExpiration(med.getRefMedicament());

                // ✅ Filtrer les stocks périmés
                List<StockMedicament> stocksValides = new ArrayList<>();
                for (StockMedicament stock : stocks) {
                    if (!stock.estPerime()) {
                        stocksValides.add(stock);
                    }
                }

                // ✅ Calculer le stock total valide
                int stockTotal = 0;
                for (StockMedicament stock : stocksValides) {
                    stockTotal += stock.getQuantiteProduit();
                }

                if (stockTotal > 0) {
                    MedicamentItem item = new MedicamentItem(med, stocksValides);
                    cmbMedicament.addItem(item);
                    medicamentNoms.put(med.getRefMedicament(), med.getNom());
                    nbMedicamentsCharges++;

                    System.out.println("✅ " + med.getNom() + " - Stock total: " + stockTotal +
                            " (" + stocksValides.size() + " lot(s) valide(s))");
                }
            }

            System.out.println("📦 " + nbMedicamentsCharges + " médicament(s) chargé(s) avec stock disponible");

            if (nbMedicamentsCharges == 0) {
                JOptionPane.showMessageDialog(this,
                        "Aucun médicament en stock disponible pour la vente.",
                        "Stock vide",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des médicaments: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * ✅ Sélection d'un médicament avec calcul du stock disponible
     */
    private void onMedicamentSelected() {
        MedicamentItem selected = (MedicamentItem) cmbMedicament.getSelectedItem();
        if (selected != null) {
            StockMedicament premierLot = selected.getPremierLot();
            if (premierLot != null) {
                txtPrixUnitaire.setText(String.valueOf(premierLot.getPrixVente()));
            }
            spnQuantite.setValue(1);

            try {
                int refMed = selected.getMedicament().getRefMedicament();
                int stockTotal = gestionStock.obtenirStockTotal(refMed);

                // ✅ Soustraire les quantités déjà réservées dans le panier
                int dejaReserve = quantitesReservees.getOrDefault(refMed, 0);
                int stockDisponible = stockTotal - dejaReserve;

                if (stockDisponible > 0) {
                    ((SpinnerNumberModel) spnQuantite.getModel()).setMaximum(stockDisponible);
                } else {
                    ((SpinnerNumberModel) spnQuantite.getModel()).setMaximum(0);
                }

                System.out.println("📊 " + selected.getMedicament().getNom() +
                        " - Stock total: " + stockTotal +
                        ", Déjà réservé: " + dejaReserve +
                        ", Disponible: " + stockDisponible);

            } catch (SQLException e) {
                System.err.println("❌ Erreur récupération stock: " + e.getMessage());
                ((SpinnerNumberModel) spnQuantite.getModel()).setMaximum(1);
            }

            updateStockInfo(selected, 1);
        }
    }

    private void updateStockInfoOnQuantityChange() {
        MedicamentItem selected = (MedicamentItem) cmbMedicament.getSelectedItem();
        if (selected != null) {
            int quantite = (Integer) spnQuantite.getValue();
            updateStockInfo(selected, quantite);
        }
    }

    /**
     * ✅ REFACTORED: Affichage de la répartition FEFO avec dates depuis StockMedicament
     */
    private void updateStockInfo(MedicamentItem item, int quantiteDemandee) {
        List<StockMedicament> stocks = item.getTousLesLots();
        StringBuilder info = new StringBuilder();

        info.append("📦 FEFO: ");

        // ✅ Calculer le stock disponible (total - déjà réservé)
        int refMed = item.getMedicament().getRefMedicament();
        int dejaReserve = quantitesReservees.getOrDefault(refMed, 0);

        int reste = quantiteDemandee;
        int nbLots = 0;
        int stockTotalDisponible = 0;

        for (StockMedicament stock : stocks) {
            stockTotalDisponible += stock.getQuantiteProduit();
        }

        stockTotalDisponible -= dejaReserve;

        // ✅ Simuler la répartition FEFO
        for (StockMedicament stock : stocks) {
            if (reste <= 0) break;

            int qteDisponible = stock.getQuantiteProduit();
            int qtePrise = Math.min(qteDisponible, reste);

            if (nbLots > 0) info.append(" + ");
            info.append(qtePrise).append(" du lot #").append(stock.getNumStock());

            // ✅ REFACTORED: Utiliser la date d'expiration du STOCK, pas du médicament
            if (stock.getDateExpiration() != null) {
                long joursAvantExpiration = calculerJoursAvantExpiration(stock.getDateExpiration());

                info.append(" (exp: ").append(joursAvantExpiration).append("j)");

                if (joursAvantExpiration < 30) {
                    info.append(" ⚠️");
                }
            }

            reste -= qtePrise;
            nbLots++;
        }

        if (reste > 0) {
            info.append(" ❌ STOCK INSUFFISANT (manque: ").append(reste);
            if (dejaReserve > 0) {
                info.append(", dont ").append(dejaReserve).append(" déjà dans le panier");
            }
            info.append(")");
            lblStockInfo.setForeground(Color.RED);
        } else {
            info.append(" ✅");
            if (dejaReserve > 0) {
                info.append(" (").append(dejaReserve).append(" déjà réservé)");
            }
            lblStockInfo.setForeground(new Color(0, 100, 200));
        }

        lblStockInfo.setText(info.toString());
    }

    private long calculerJoursAvantExpiration(Date dateExpiration) {
        Date maintenant = new Date();
        long differenceMs = dateExpiration.getTime() - maintenant.getTime();
        return TimeUnit.MILLISECONDS.toDays(differenceMs);
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

    /**
     * ✅ CORRECTION CRITIQUE: Vérifier le stock disponible AVANT d'ajouter au panier
     */
    private void ajouterLigne() {
        MedicamentItem medItem = (MedicamentItem) cmbMedicament.getSelectedItem();
        if (medItem == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un médicament");
            return;
        }

        try {
            int quantite = (Integer) spnQuantite.getValue();
            double prixUnit = Double.parseDouble(txtPrixUnitaire.getText());
            int refMed = medItem.getMedicament().getRefMedicament();

            // ✅ Récupérer le stock TOTAL de la BD
            int stockTotal = gestionStock.obtenirStockTotal(refMed);

            // ✅ Soustraire les quantités déjà réservées dans le panier
            int dejaReserve = quantitesReservees.getOrDefault(refMed, 0);
            int stockDisponible = stockTotal - dejaReserve;

            System.out.println("🔍 Vérification stock:");
            System.out.println("   Médicament: " + medItem.getMedicament().getNom());
            System.out.println("   Stock total BD: " + stockTotal);
            System.out.println("   Déjà réservé: " + dejaReserve);
            System.out.println("   Stock disponible: " + stockDisponible);
            System.out.println("   Quantité demandée: " + quantite);

            // ✅ Vérification stricte
            if (quantite > stockDisponible) {
                String message = "Stock insuffisant!\n\n" +
                        "Stock total: " + stockTotal + " unités\n" +
                        "Déjà dans le panier: " + dejaReserve + " unités\n" +
                        "Stock disponible: " + stockDisponible + " unités\n" +
                        "Quantité demandée: " + quantite + " unités\n\n" +
                        "⚠️ Veuillez réduire la quantité.";

                JOptionPane.showMessageDialog(this, message,
                        "Stock épuisé", JOptionPane.ERROR_MESSAGE);

                if (stockDisponible > 0) {
                    spnQuantite.setValue(stockDisponible);
                    ((SpinnerNumberModel) spnQuantite.getModel()).setMaximum(stockDisponible);
                }
                return;
            }

            // ✅ Ajouter la quantité aux réservations
            quantitesReservees.put(refMed, dejaReserve + quantite);

            // ✅ Simuler le FEFO avec les stocks réels
            String lotsUtilises = simulerFEFO(medItem, quantite);

            VoieVente ligne = new VoieVente();
            ligne.setRefMedicament(refMed);
            ligne.setQuantite(quantite);
            ligne.setPrixUnitaire(prixUnit);
            ligne.setPrixTotalVoieVente();

            lignesVente.add(ligne);

            tableModel.addRow(new Object[]{
                    medItem.getMedicament().getNom(),
                    quantite,
                    String.format("%.2f DT", prixUnit),
                    String.format("%.2f DT", ligne.getPrixTotalVoieVente()),
                    lotsUtilises
            });

            calculerTotal();
            spnQuantite.setValue(1);

            // ✅ Mettre à jour l'affichage pour le même médicament
            onMedicamentSelected();

            System.out.println("✅ Ligne ajoutée - Nouvelle réservation: " + quantitesReservees.get(refMed));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Prix unitaire invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la vérification du stock: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * ✅ REFACTORED: Simulation FEFO avec dates depuis StockMedicament
     */
    private String simulerFEFO(MedicamentItem item, int quantite) {
        List<StockMedicament> stocks = item.getTousLesLots();
        StringBuilder result = new StringBuilder();

        int reste = quantite;
        int nbLots = 0;

        for (StockMedicament stock : stocks) {
            if (reste <= 0) break;

            int qtePrise = Math.min(stock.getQuantiteProduit(), reste);

            if (nbLots > 0) result.append(", ");
            result.append(qtePrise).append(" de #").append(stock.getNumStock());

            // ✅ REFACTORED: Utiliser la date d'expiration du STOCK
            if (stock.getDateExpiration() != null) {
                long jours = calculerJoursAvantExpiration(stock.getDateExpiration());
                result.append(" (").append(jours).append("j)");
            }

            reste -= qtePrise;
            nbLots++;
        }

        return result.toString();
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

            // ✅ La méthode enregistrerVente utilise maintenant le FEFO correct
            gestionVente.enregistrerVente(vente, lignesVente);

            StringBuilder facture = new StringBuilder();
            facture.append("═══════════════════════════════════════\n");
            facture.append("        FACTURE DE VENTE (FEFO)\n");
            facture.append("═══════════════════════════════════════\n\n");
            facture.append("N° Vente: #").append(vente.getNumVente()).append("\n");
            facture.append("Date: ").append(vente.getDateVente()).append("\n\n");

            if (clientActuel != null) {
                facture.append("Client: ").append(clientActuel.getNom()).append(" ").append(clientActuel.getPrenom()).append("\n");
                int pointsGagnes = (int) (total / 10);
                facture.append("Points gagnés: ").append(pointsGagnes).append("\n\n");
            }

            facture.append("DÉTAILS:\n");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String nom = (String) tableModel.getValueAt(i, 0);
                int qte = (Integer) tableModel.getValueAt(i, 1);
                String totalLigne = (String) tableModel.getValueAt(i, 3);
                String lots = (String) tableModel.getValueAt(i, 4);

                facture.append("• ").append(nom).append(" x").append(qte);
                facture.append(" = ").append(totalLigne).append("\n");
                facture.append("  Lots: ").append(lots).append("\n");
            }

            facture.append("\nTOTAL: ").append(String.format("%.2f DT", total)).append("\n");
            facture.append("═══════════════════════════════════════\n");
            facture.append("✅ FEFO appliqué: lots les plus proches\n");
            facture.append("   de la date d'expiration vendus en premier\n");

            JOptionPane.showMessageDialog(this, facture.toString(),
                    "Vente réussie", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (StockInsuffisantException e) {
            // ✅ RESTAURER les quantités réservées en cas d'erreur
            restaurerQuantitesReservees();

            JOptionPane.showMessageDialog(this,
                    "❌ ERREUR FEFO - Stock insuffisant:\n\n" +
                            e.getMessage() + "\n\n" +
                            "Le système n'a pas pu répartir la quantité demandée\n" +
                            "sur les lots disponibles selon FEFO.\n\n" +
                            "Vérifiez les quantités dans votre panier.",
                    "Erreur FEFO", JOptionPane.ERROR_MESSAGE);
        } catch (ProduitNonTrouveException e) {
            // ✅ RESTAURER les quantités réservées en cas d'erreur
            restaurerQuantitesReservees();

            JOptionPane.showMessageDialog(this,
                    "Produit non trouvé: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            // ✅ RESTAURER les quantités réservées en cas d'erreur
            restaurerQuantitesReservees();

            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            // ✅ RESTAURER les quantités réservées en cas d'erreur
            restaurerQuantitesReservees();

            JOptionPane.showMessageDialog(this,
                    "Numéro de carte employé invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Restaurer les quantités réservées en cas d'erreur
     * Permet de recalculer les quantités disponibles sans fermer la fenêtre
     */
    private void restaurerQuantitesReservees() {
        System.out.println("⚠️ Erreur de validation - Recalcul des quantités réservées...");

        // Recalculer les quantités réservées depuis le panier actuel
        quantitesReservees.clear();

        for (VoieVente ligne : lignesVente) {
            int refMed = ligne.getRefMedicament();
            int quantite = ligne.getQuantite();
            int quantiteActuelle = quantitesReservees.getOrDefault(refMed, 0);
            quantitesReservees.put(refMed, quantiteActuelle + quantite);
        }

        // Mettre à jour l'affichage si un médicament est sélectionné
        MedicamentItem selected = (MedicamentItem) cmbMedicament.getSelectedItem();
        if (selected != null) {
            onMedicamentSelected();
        }

        System.out.println("✅ Quantités réservées recalculées depuis le panier");
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Supprimer une ligne sélectionnée du panier
     * Restaure automatiquement les quantités réservées
     */
    private void supprimerLigneSelectionnee() {
        int selectedRow = tableLignes.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner une ligne à supprimer",
                    "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmer la suppression
        String nomMedicament = (String) tableModel.getValueAt(selectedRow, 0);
        int quantite = (Integer) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer cette ligne ?\n\n" +
                        "Médicament: " + nomMedicament + "\n" +
                        "Quantité: " + quantite,
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Récupérer la référence du médicament avant de supprimer
        VoieVente ligneASupprimer = lignesVente.get(selectedRow);
        int refMed = ligneASupprimer.getRefMedicament();
        int qteALiberer = ligneASupprimer.getQuantite();

        // Supprimer de la liste et du tableau
        lignesVente.remove(selectedRow);
        tableModel.removeRow(selectedRow);

        // ✅ Libérer les quantités réservées
        int quantiteReservee = quantitesReservees.getOrDefault(refMed, 0);
        int nouvelleReservation = quantiteReservee - qteALiberer;

        if (nouvelleReservation > 0) {
            quantitesReservees.put(refMed, nouvelleReservation);
        } else {
            quantitesReservees.remove(refMed);
        }

        System.out.println("🗑️ Ligne supprimée:");
        System.out.println("   Médicament ref: " + refMed);
        System.out.println("   Quantité libérée: " + qteALiberer);
        System.out.println("   Nouvelle réservation: " + nouvelleReservation);

        // Recalculer le total
        calculerTotal();

        // Mettre à jour l'affichage du stock si le même médicament est sélectionné
        MedicamentItem selected = (MedicamentItem) cmbMedicament.getSelectedItem();
        if (selected != null && selected.getMedicament().getRefMedicament() == refMed) {
            onMedicamentSelected();
        }

        JOptionPane.showMessageDialog(this,
                "Ligne supprimée avec succès!\n" +
                        "Stock disponible mis à jour.",
                "Suppression réussie",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * ✅ Classe interne pour encapsuler un Medicament avec ses stocks
     */
    private class MedicamentItem {
        private Medicament medicament;
        private List<StockMedicament> tousLesLots;

        public MedicamentItem(Medicament medicament, List<StockMedicament> stocks) {
            this.medicament = medicament;
            this.tousLesLots = stocks;
        }

        public Medicament getMedicament() {
            return medicament;
        }

        public List<StockMedicament> getTousLesLots() {
            return tousLesLots;
        }

        public StockMedicament getPremierLot() {
            return tousLesLots.isEmpty() ? null : tousLesLots.get(0);
        }

        public int getStockTotal() {
            int total = 0;
            for (StockMedicament stock : tousLesLots) {
                total += stock.getQuantiteProduit();
            }
            return total;
        }

        @Override
        public String toString() {
            StockMedicament premier = getPremierLot();
            if (premier == null) return medicament.getNom() + " (Stock épuisé)";

            int nbLots = tousLesLots.size();
            int stockTotal = getStockTotal();

            return medicament.getNom() +
                    " - Lot #" + premier.getNumStock() +
                    " (" + premier.getQuantiteProduit() + " unités" +
                    (nbLots > 1 ? " + " + (nbLots - 1) + " autre(s) lot(s)" : "") +
                    ", Total: " + stockTotal +
                    ", Prix: " + String.format("%.2f DT", premier.getPrixVente()) + ")";
        }
    }
}