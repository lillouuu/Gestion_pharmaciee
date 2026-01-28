package interfaces.commande;

import entite.*;
import entitebd.MedicamentBD;
import entitebd.StockBD;
import gestion.GestionCommande;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReceptionnerCommandeFrame extends JFrame {
    private GestionCommande gestionCommande;
    private MedicamentBD medicamentBD;
    private StockBD stockBD;

    private JTextField txtNumCommande;
    private JTextField txtNumCarteEmp;
    private JTextArea txtInfoCommande;
    private JTable tableLignes;
    private DefaultTableModel tableModel;
    private JButton btnReceptionner;

    private Commande commandeActuelle;
    private ArrayList<VoieCommande> lignesCommande;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public ReceptionnerCommandeFrame() {
        gestionCommande = new GestionCommande();
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        lignesCommande = new ArrayList<>();
        initComponents();
    }

    private void initComponents() {
        setTitle("Réceptionner une Commande");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(40, 167, 69));
        topPanel.setPreferredSize(new Dimension(1200, 50));

        JLabel titleLabel = new JLabel("📦 Réceptionner une Commande et Créer Nouveaux Stocks");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel recherche
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Panel infos
        JPanel infoPanel = createInfoPanel();
        splitPane.setTopComponent(infoPanel);

        // Panel lignes avec formulaire de complétion
        JPanel lignesPanel = createLignesPanel();
        splitPane.setBottomComponent(lignesPanel);

        splitPane.setDividerLocation(200);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Rechercher Commande"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Numéro Commande:"), gbc);

        gbc.gridx = 1;
        txtNumCommande = new JTextField(15);
        txtNumCommande.addActionListener(e -> chargerCommande());
        panel.add(txtNumCommande, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Num Carte Emp:"), gbc);

        gbc.gridx = 3;
        txtNumCarteEmp = new JTextField(10);
        txtNumCarteEmp.setText("1");
        panel.add(txtNumCarteEmp, gbc);

        gbc.gridx = 4;
        JButton btnCharger = new JButton("🔍 Charger");
        btnCharger.setBackground(new Color(0, 123, 255));
        btnCharger.setForeground(Color.WHITE);
        btnCharger.setFocusPainted(false);
        btnCharger.addActionListener(e -> chargerCommande());
        panel.add(btnCharger, gbc);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Informations Commande"));

        txtInfoCommande = new JTextArea(8, 40);
        txtInfoCommande.setEditable(false);
        txtInfoCommande.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtInfoCommande.setBackground(new Color(245, 245, 245));
        txtInfoCommande.setText("Veuillez rechercher une commande...");

        JScrollPane scrollInfo = new JScrollPane(txtInfoCommande);
        panel.add(scrollInfo, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLignesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Détails des Lignes - Compléter les informations pour créer nouveaux stocks"));

        String[] columns = {
                "ID", "Médicament", "Qté", "Prix Achat",
                "Date Fab", "Date Exp", "Prix Vente", "Seuil", "✓"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 8) return Boolean.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Seules les colonnes 4,5,6,7 (dates, prix vente, seuil) sont éditables
                return column >= 4 && column <= 7;
            }
        };

        tableLignes = new JTable(tableModel);
        tableLignes.setRowHeight(30);
        tableLignes.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableLignes.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableLignes.getColumnModel().getColumn(8).setPreferredWidth(30);

        // Ajouter un bouton pour marquer comme complété
        tableLignes.getColumnModel().getColumn(8).setCellRenderer(
                new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                                                                   boolean isSelected, boolean hasFocus, int row, int column) {
                        JCheckBox checkBox = new JCheckBox();
                        checkBox.setSelected(value != null && (Boolean) value);
                        checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                        return checkBox;
                    }
                }
        );

        JScrollPane scrollTable = new JScrollPane(tableLignes);
        panel.add(scrollTable, BorderLayout.CENTER);

        // Panel avec boutons d'aide
        JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        helpPanel.setBackground(new Color(255, 243, 205));
        JLabel lblHelp = new JLabel("💡 Double-cliquez sur les cellules pour modifier les dates (jj/mm/aaaa), prix de vente et seuil minimal");
        lblHelp.setFont(new Font("Arial", Font.ITALIC, 11));
        helpPanel.add(lblHelp);

        JButton btnValiderLigne = new JButton("✓ Valider la ligne sélectionnée");
        btnValiderLigne.setBackground(new Color(40, 167, 69));
        btnValiderLigne.setForeground(Color.WHITE);
        btnValiderLigne.addActionListener(e -> validerLigneSelectionnee());
        helpPanel.add(btnValiderLigne);

        panel.add(helpPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnReceptionner = new JButton("📦 Réceptionner et Créer les Nouveaux Stocks");
        btnReceptionner.setBackground(new Color(40, 167, 69));
        btnReceptionner.setForeground(Color.WHITE);
        btnReceptionner.setFont(new Font("Arial", Font.BOLD, 14));
        btnReceptionner.setFocusPainted(false);
        btnReceptionner.setEnabled(false);
        btnReceptionner.addActionListener(e -> receptionnerCommande());

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setBackground(new Color(108, 117, 125));
        btnFermer.setForeground(Color.WHITE);
        btnFermer.setFocusPainted(false);
        btnFermer.addActionListener(e -> dispose());

        panel.add(btnReceptionner);
        panel.add(btnFermer);

        return panel;
    }

    private void chargerCommande() {

        try {
            int numCommande = Integer.parseInt(txtNumCommande.getText().trim());

            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(numCommande);
            commandeActuelle = bilan.getCommande();
            lignesCommande = bilan.getLignes();

            // Afficher les informations
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════\n");
            sb.append("   INFORMATIONS DE LA COMMANDE\n");
            sb.append("═══════════════════════════════════════\n\n");
            sb.append("Numéro: #").append(commandeActuelle.getNumCommande()).append("\n");
            sb.append("Date: ").append(commandeActuelle.getDateAchat()).append("\n");
            sb.append("Statut: ").append(commandeActuelle.getStatut()).append("\n");
            sb.append("Fournisseur: ").append(commandeActuelle.getNumFournisseur()).append("\n");
            sb.append("Total: ").append(String.format("%.2f DT", bilan.getTotal())).append("\n");
            sb.append("\n═══════════════════════════════════════\n");

            // Vérifier si réceptionnable
            if ("Reçue".equals(commandeActuelle.getStatut())) {
                sb.append("\n⚠️ Cette commande est déjà réceptionnée.\n");
                btnReceptionner.setEnabled(false);
            } else if ("Annulée".equals(commandeActuelle.getStatut())) {
                sb.append("\n⚠️ Cette commande est annulée.\n");
                btnReceptionner.setEnabled(false);
            } else {
                sb.append("\n✅ Prête à être réceptionnée.\n");
                sb.append("⚠️ Veuillez compléter les informations pour chaque ligne.\n");
                btnReceptionner.setEnabled(false); // Sera activé après validation des lignes
            }

            txtInfoCommande.setText(sb.toString());
// Vérifier si réceptionnable
            if ("Reçue".equals(commandeActuelle.getStatut())) {
                sb.append("\n⚠️ Cette commande est déjà réceptionnée.\n");
                btnReceptionner.setEnabled(false);
                tableModel.setRowCount(0); // vider les lignes pour éviter modification
                txtInfoCommande.setText(sb.toString());
                return; // ne pas continuer
            } else if ("Annulée".equals(commandeActuelle.getStatut())) {
                sb.append("\n⚠️ Cette commande est annulée.\n");
                btnReceptionner.setEnabled(false);
                tableModel.setRowCount(0);
                txtInfoCommande.setText(sb.toString());
                return;
            }

            // Afficher les lignes avec valeurs par défaut
            tableModel.setRowCount(0);
            for (VoieCommande ligne : lignesCommande) {
                String nomMed = getNomMedicament(ligne.getRefMedicament());

                // Valeurs par défaut
                String dateFab = dateFormat.format(new Date());
                String dateExp = dateFormat.format(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)); // +1 an
                String prixVente = String.format("%.2f", ligne.getPrixUnitaire() * 1.3); // +30% marge par défaut
                String seuil = "10";

                tableModel.addRow(new Object[]{
                        ligne.getIdLigneCommande(),
                        nomMed,
                        ligne.getQuantite(),
                        String.format("%.2f", ligne.getPrixUnitaire()),
                        dateFab,
                        dateExp,
                        prixVente,
                        seuil,
                        false // Non validé
                });
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Numéro de commande invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void validerLigneSelectionnee() {
        int selectedRow = tableLignes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner une ligne",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // Valider les données de la ligne
            String dateFabStr = (String) tableModel.getValueAt(selectedRow, 4);
            String dateExpStr = (String) tableModel.getValueAt(selectedRow, 5);
            String prixVenteStr = (String) tableModel.getValueAt(selectedRow, 6);
            String seuilStr = (String) tableModel.getValueAt(selectedRow, 7);

            // Parser et valider
            Date dateFab = dateFormat.parse(dateFabStr);
            Date dateExp = dateFormat.parse(dateExpStr);
            double prixVente = Double.parseDouble(prixVenteStr);
            int seuil = Integer.parseInt(seuilStr);

            // Vérifications
            if (dateExp.before(dateFab)) {
                JOptionPane.showMessageDialog(this,
                        "La date d'expiration doit être après la date de fabrication!",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (prixVente <= 0 || seuil < 0) {
                JOptionPane.showMessageDialog(this,
                        "Le prix de vente et le seuil doivent être positifs!",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Marquer comme validé
            tableModel.setValueAt(true, selectedRow, 8);

            JOptionPane.showMessageDialog(this,
                    "✓ Ligne validée!",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            // Vérifier si toutes les lignes sont validées
            boolean toutesValidees = true;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (!(Boolean) tableModel.getValueAt(i, 8)) {
                    toutesValidees = false;
                    break;
                }
            }

            if (toutesValidees) {
                btnReceptionner.setEnabled(true);
                JOptionPane.showMessageDialog(this,
                        "✅ Toutes les lignes sont validées!\n" +
                                "Vous pouvez maintenant réceptionner la commande.",
                        "Prêt", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (java.text.ParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Format de date invalide! Utilisez: jj/mm/aaaa",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Valeurs numériques invalides!",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void receptionnerCommande() {
        if (commandeActuelle == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez d'abord charger une commande",
                    "Erreur", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ✅ Nouvelle vérification
        if ("Reçue".equals(commandeActuelle.getStatut())) {
            JOptionPane.showMessageDialog(this,
                    "Cette commande a déjà été réceptionnée !",
                    "Erreur", JOptionPane.WARNING_MESSAGE);
            btnReceptionner.setEnabled(false);
            return;
        }

        // Vérifier que toutes les lignes sont validées
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (!(Boolean) tableModel.getValueAt(i, 8)) {
                JOptionPane.showMessageDialog(this,
                        "Toutes les lignes doivent être validées avant la réception!",
                        "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }


        // Vérifier que toutes les lignes sont validées
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (!(Boolean) tableModel.getValueAt(i, 8)) {
                JOptionPane.showMessageDialog(this,
                        "Toutes les lignes doivent être validées avant la réception!",
                        "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }





        try {
            int numCarteEmp = Integer.parseInt(txtNumCarteEmp.getText().trim());

            // Demander confirmation
            int confirmation = JOptionPane.showConfirmDialog(this,
                    "📦 RÉCEPTION DE COMMANDE\n\n" +
                            "Commande: #" + commandeActuelle.getNumCommande() + "\n" +
                            "Lignes: " + tableModel.getRowCount() + "\n\n" +
                            "Cette action va créer " + tableModel.getRowCount() +
                            " NOUVELLES LIGNES dans le stock.\n" +
                            "Chaque ligne aura ses propres caractéristiques.\n\n" +
                            "Confirmer la réception?",
                    "Confirmer Réception",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }

            // ✅ Créer les nouveaux stocks
            int stocksCrees = 0;
            StringBuilder details = new StringBuilder();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                VoieCommande ligne = lignesCommande.get(i);

                // Récupérer les données du tableau
                Date dateFab = dateFormat.parse((String) tableModel.getValueAt(i, 4));
                Date dateExp = dateFormat.parse((String) tableModel.getValueAt(i, 5));
                double prixVente = Double.parseDouble((String) tableModel.getValueAt(i, 6));
                int seuil = Integer.parseInt((String) tableModel.getValueAt(i, 7));

                // ✅ REFACTORED: Créer un nouveau StockMedicament avec les dates
                StockMedicament nouveauStock = new StockMedicament();
                nouveauStock.setRefMedicament(ligne.getRefMedicament());
                nouveauStock.setQuantiteProduit(ligne.getQuantite());
                nouveauStock.setPrixAchat(ligne.getPrixUnitaire());
                nouveauStock.setPrixVente(prixVente);
                nouveauStock.setSeuilMin(seuil);

                // ✅ DATES MAINTENANT DANS STOCKMEDICAMENT
                nouveauStock.setDateFabrication(dateFab);
                nouveauStock.setDateExpiration(dateExp);

                // ✅ Ajouter à la base de données (NOUVELLE ligne)
                int numStock = stockBD.ajouter(nouveauStock);

                if (numStock > 0) {
                    stocksCrees++;

                    // Récupérer le nom du médicament
                    String nomMed = getNomMedicament(ligne.getRefMedicament());
                    details.append("• ").append(nomMed).append(": ")
                            .append(ligne.getQuantite()).append(" unités (Stock #")
                            .append(numStock).append(")\n")
                            .append("  Fab: ").append(dateFormat.format(dateFab))
                            .append(" | Exp: ").append(dateFormat.format(dateExp)).append("\n");
                }
            }

            // Changer le statut de la commande
            commandeActuelle.setStatut("Reçue");
            new entitebd.CommandeBD().modifierCommande(commandeActuelle);

            // Afficher le résultat
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════\n");
            sb.append("     RÉCEPTION RÉUSSIE\n");
            sb.append("═══════════════════════════════════════\n\n");
            sb.append("Commande: #").append(commandeActuelle.getNumCommande()).append("\n");
            sb.append("Nouvelles lignes de stock créées: ").append(stocksCrees).append("\n\n");
            sb.append("DÉTAILS:\n");
            sb.append(details.toString());
            sb.append("\n✅ Chaque ligne de commande a créé une\n");
            sb.append("   nouvelle entrée dans le stock avec ses\n");
            sb.append("   propres dates de fabrication/expiration.\n");
            sb.append("═══════════════════════════════════════\n");

            JOptionPane.showMessageDialog(this, sb.toString(),
                    "Réception Réussie", JOptionPane.INFORMATION_MESSAGE);

            txtInfoCommande.setText(sb.toString());
            btnReceptionner.setEnabled(false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la réception: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String getNomMedicament(int refMedicament) {
        try {
            Medicament med = medicamentBD.rechercherParRef(refMedicament);
            if (med != null) {
                return med.getNom() + " (#" + refMedicament + ")";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Médicament #" + refMedicament;
    }
}