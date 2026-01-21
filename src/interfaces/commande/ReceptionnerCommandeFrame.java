package interfaces.commande;

import entite.*;
import entitebd.MedicamentBD;
import entitebd.StockBD;
import gestion.GestionCommande;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

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

    public ReceptionnerCommandeFrame() {
        gestionCommande = new GestionCommande();
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        initComponents();
    }

    private void initComponents() {
        setTitle("Réceptionner une Commande");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(40, 167, 69));
        topPanel.setPreferredSize(new Dimension(1000, 50));

        JLabel titleLabel = new JLabel("📦 Réceptionner une Commande");
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

        // Panel lignes avec impact stock
        JPanel lignesPanel = createLignesPanel();
        splitPane.setBottomComponent(lignesPanel);

        splitPane.setDividerLocation(220);
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

        // Numéro commande
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Numéro Commande:"), gbc);

        gbc.gridx = 1;
        txtNumCommande = new JTextField(15);
        txtNumCommande.addActionListener(e -> chargerCommande());
        panel.add(txtNumCommande, gbc);

        // Numéro carte employé
        gbc.gridx = 2;
        panel.add(new JLabel("Num Carte Emp:"), gbc);

        gbc.gridx = 3;
        txtNumCarteEmp = new JTextField(10);
        txtNumCarteEmp.setText("1");
        panel.add(txtNumCarteEmp, gbc);

        // Bouton charger
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

        txtInfoCommande = new JTextArea(9, 40);
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
        panel.setBorder(BorderFactory.createTitledBorder("Détails des Lignes - Impact sur le Stock"));

        String[] columns = {
                "ID", "Médicament", "Quantité Cmdée", "Prix Unit.",
                "Stock Actuel", "Nouveau Stock", "Sous-Total"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableLignes = new JTable(tableModel);
        tableLignes.setRowHeight(25);
        tableLignes.getColumnModel().getColumn(1).setPreferredWidth(200);

        JScrollPane scrollTable = new JScrollPane(tableLignes);
        panel.add(scrollTable, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnReceptionner = new JButton("📦 Réceptionner et Mettre à Jour Stock");
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
            ArrayList<VoieCommande> lignes = bilan.getLignes();

            // Afficher les informations
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════\n");
            sb.append("   INFORMATIONS DE LA COMMANDE\n");
            sb.append("═══════════════════════════════════════\n\n");
            sb.append("Numéro: #").append(commandeActuelle.getNumCommande()).append("\n");
            sb.append("Date: ").append(commandeActuelle.getDateAchat()).append("\n");
            sb.append("Date limite: ").append(commandeActuelle.getDateLimRendreProduit()).append("\n");
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
                sb.append("Elle ne peut PAS être réceptionnée.\n");
                btnReceptionner.setEnabled(false);
            } else {
                sb.append("\n✅ Prête à être réceptionnée.\n");
                btnReceptionner.setEnabled(true);
            }

            txtInfoCommande.setText(sb.toString());

            // Afficher les lignes avec impact stock
            tableModel.setRowCount(0);
            for (VoieCommande ligne : lignes) {
                String nomMed = getNomMedicament(ligne.getRefMedicament());
                int stockActuel = getStockActuel(ligne.getRefMedicament());
                int nouveauStock = stockActuel + ligne.getQuantite();

                tableModel.addRow(new Object[]{
                        ligne.getIdLigneCommande(),
                        nomMed,
                        ligne.getQuantite(),
                        String.format("%.2f DT", ligne.getPrixUnitaire()),
                        stockActuel,
                        nouveauStock + " (+" + ligne.getQuantite() + ")",
                        String.format("%.2f DT", ligne.calculerTotal())
                });
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Numéro de commande invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            txtInfoCommande.setText("Commande introuvable.");
            tableModel.setRowCount(0);
            btnReceptionner.setEnabled(false);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + e.getMessage(),
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

        try {
            int numCarteEmp = Integer.parseInt(txtNumCarteEmp.getText().trim());
            int numCommande = commandeActuelle.getNumCommande();

            // Préparer le résumé des changements
            StringBuilder resumeStock = new StringBuilder();
            resumeStock.append("IMPACT SUR LE STOCK:\n\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String medicament = (String) tableModel.getValueAt(i, 1);
                int quantite = (int) tableModel.getValueAt(i, 2);
                int stockActuel = (int) tableModel.getValueAt(i, 4);

                resumeStock.append("• ").append(medicament).append("\n");
                resumeStock.append("  Stock: ").append(stockActuel)
                        .append(" → ").append(stockActuel + quantite)
                        .append(" (+").append(quantite).append(")\n\n");
            }

            // Demander confirmation
            int confirmation = JOptionPane.showConfirmDialog(this,
                    "📦 RÉCEPTION DE COMMANDE\n\n" +
                            "Commande: #" + numCommande + "\n" +
                            "Total: " + String.format("%.2f DT", commandeActuelle.getMontantTotalCommande()) + "\n" +
                            "Lignes: " + tableModel.getRowCount() + "\n\n" +
                            "Cette action va:\n" +
                            "✅ Mettre à jour le stock pour tous les médicaments\n" +
                            "✅ Changer le statut en 'Reçue'\n" +
                            "✅ Mettre à jour le chiffre d'affaires\n\n" +
                            resumeStock.toString() +
                            "Confirmer la réception?",
                    "Confirmer Réception",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }

            // Réceptionner
            gestionCommande.receptionnerCommande(numCommande, numCarteEmp);

            // Afficher le bilan final
            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(numCommande);

            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════\n");
            sb.append("     COMMANDE RÉCEPTIONNÉE\n");
            sb.append("═══════════════════════════════════════\n\n");
            sb.append("Numéro: #").append(bilan.getCommande().getNumCommande()).append("\n");
            sb.append("Statut: ").append(bilan.getCommande().getStatut()).append("\n");
            sb.append("Total: ").append(String.format("%.2f DT", bilan.getTotal())).append("\n");
            sb.append("Lignes traitées: ").append(bilan.getNombreLignes()).append("\n");
            sb.append("\n✅ Stock mis à jour avec succès\n");
            sb.append("✅ Chiffre d'affaires mis à jour\n");
            sb.append("✅ Statut changé en 'Reçue'\n");
            sb.append("\n═══════════════════════════════════════\n");

            JOptionPane.showMessageDialog(this, sb.toString(),
                    "Réception Réussie", JOptionPane.INFORMATION_MESSAGE);

            // Actualiser l'affichage
            txtInfoCommande.setText(sb.toString());
            btnReceptionner.setEnabled(false);

            // Recharger pour montrer les nouveaux stocks
            chargerCommande();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Numéro de carte employé invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
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
            // Ignorer
        }
        return "Médicament #" + refMedicament;
    }

    private int getStockActuel(int refMedicament) {
        try {
            StockMedicament stock = stockBD.rechercherParRef(refMedicament);
            if (stock != null) {
                return stock.getQuantiteProduit();
            }
        } catch (SQLException e) {
            // Ignorer
        }
        return 0;
    }
}