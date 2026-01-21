package interfaces.commande;

import entite.*;
import entitebd.StockBD;
import gestion.GestionCommande;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class ModifierCommandeFrame extends JFrame {
    private GestionCommande gestionCommande;

    private JTextField txtNumCommande;
    private JTable tableLignes;
    private DefaultTableModel tableModel;
    private ArrayList<VoieCommande> lignesCommande;
    private Commande commandeActuelle;

    public ModifierCommandeFrame() {
        gestionCommande = new GestionCommande();
        lignesCommande = new ArrayList<>();

        initComponents();
    }

    private void initComponents() {
        setTitle("Modifier une Commande");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel recherche
        JPanel topPanel = createSearchPanel();
        add(topPanel, BorderLayout.NORTH);

        // Panel lignes
        JPanel centerPanel = createLinesPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel bottomPanel = createButtonPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Rechercher Commande"));

        panel.add(new JLabel("Numéro Commande:"));

        txtNumCommande = new JTextField(15);
        panel.add(txtNumCommande);

        JButton btnCharger = new JButton("🔍 Charger");
        btnCharger.setBackground(new Color(0, 123, 255));
        btnCharger.setForeground(Color.WHITE);
        btnCharger.addActionListener(e -> chargerCommande());
        panel.add(btnCharger);

        return panel;
    }

    private JPanel createLinesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Lignes de la Commande"));

        String[] columns = {"ID Ligne", "Médicament", "Quantité", "Prix Unit.", "Remise %", "Impôts %", "Total"};
        tableModel = new DefaultTableModel(columns, 0);

        tableLignes = new JTable(tableModel);
        tableLignes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tableLignes);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel modification d'une ligne
        JPanel modifPanel = createModificationPanel();
        panel.add(modifPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createModificationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Modifier la ligne sélectionnée"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtQuantite = new JTextField(10);
        JTextField txtPrixUnit = new JTextField(10);
        JTextField txtRemise = new JTextField(10);
        JTextField txtImpots = new JTextField(10);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Quantité:"), gbc);
        gbc.gridx = 1;
        panel.add(txtQuantite, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Prix Unit.:"), gbc);
        gbc.gridx = 3;
        panel.add(txtPrixUnit, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Remise %:"), gbc);
        gbc.gridx = 1;
        panel.add(txtRemise, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Impôts %:"), gbc);
        gbc.gridx = 3;
        panel.add(txtImpots, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        JButton btnModifierLigne = new JButton("✏️ Modifier cette ligne");
        btnModifierLigne.setBackground(new Color(255, 165, 0));
        btnModifierLigne.setForeground(Color.WHITE);
        btnModifierLigne.addActionListener(e -> {
            int selectedRow = tableLignes.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne");
                return;
            }

            try {
                VoieCommande ligne = lignesCommande.get(selectedRow);
                ligne.setQuantite(Integer.parseInt(txtQuantite.getText()));
                ligne.setPrixUnitaire(Double.parseDouble(txtPrixUnit.getText()));
                ligne.setRemise(Double.parseDouble(txtRemise.getText()));
                ligne.setImpotSurCommande(Double.parseDouble(txtImpots.getText()));

                // Mettre à jour la table
                tableModel.setValueAt(ligne.getQuantite(), selectedRow, 2);
                tableModel.setValueAt(ligne.getPrixUnitaire(), selectedRow, 3);
                tableModel.setValueAt(ligne.getRemise(), selectedRow, 4);
                tableModel.setValueAt(ligne.getImpotSurCommande(), selectedRow, 5);
                tableModel.setValueAt(String.format("%.2f DT", ligne.calculerTotal()), selectedRow, 6);

                JOptionPane.showMessageDialog(this, "Ligne modifiée localement. Cliquez sur 'Enregistrer' pour sauvegarder.");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valeurs invalides", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnModifierLigne, gbc);

        // Charger les valeurs quand on sélectionne une ligne
        tableLignes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableLignes.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < lignesCommande.size()) {
                    VoieCommande ligne = lignesCommande.get(selectedRow);
                    txtQuantite.setText(String.valueOf(ligne.getQuantite()));
                    txtPrixUnit.setText(String.valueOf(ligne.getPrixUnitaire()));
                    txtRemise.setText(String.valueOf(ligne.getRemise()));
                    txtImpots.setText(String.valueOf(ligne.getImpotSurCommande()));
                }
            }
        });

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnEnregistrer = new JButton("💾 Enregistrer les modifications");
        btnEnregistrer.setBackground(new Color(40, 167, 69));
        btnEnregistrer.setForeground(Color.WHITE);
        btnEnregistrer.addActionListener(e -> enregistrerModifications());

        JButton btnAnnuler = new JButton("❌ Annuler");
        btnAnnuler.setBackground(new Color(220, 53, 69));
        btnAnnuler.setForeground(Color.WHITE);
        btnAnnuler.addActionListener(e -> dispose());

        panel.add(btnEnregistrer);
        panel.add(btnAnnuler);

        return panel;
    }

    private void chargerCommande() {
        try {
            int numCommande = Integer.parseInt(txtNumCommande.getText());

            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(numCommande);
            commandeActuelle = bilan.getCommande();
            lignesCommande = bilan.getLignes();

            // Vérifier le statut
            if ("Reçue".equals(commandeActuelle.getStatut()) ||
                    "Annulée".equals(commandeActuelle.getStatut())) {
                JOptionPane.showMessageDialog(this,
                        "Impossible de modifier une commande " + commandeActuelle.getStatut(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Afficher les lignes avec prix récupérés depuis le stock
            tableModel.setRowCount(0);
            StockBD stockBD = new StockBD();

            for (VoieCommande ligne : lignesCommande) {
                // Récupérer le nom du médicament
                String nomMed = "Médicament #" + ligne.getRefMedicament();
                try {
                    entitebd.MedicamentBD medBD = new entitebd.MedicamentBD();
                    Medicament med = medBD.rechercherParRef(ligne.getRefMedicament());
                    if (med != null) {
                        nomMed = med.getNom();
                    }
                } catch (Exception e) {
                    // Ignorer
                }

                tableModel.addRow(new Object[]{
                        ligne.getIdLigneCommande(),
                        ligne.getRefMedicament() + " - " + nomMed,
                        ligne.getQuantite(),
                        ligne.getPrixUnitaire(),
                        ligne.getRemise(),
                        ligne.getImpotSurCommande(),
                        String.format("%.2f DT", ligne.calculerTotal())
                });
            }

            JOptionPane.showMessageDialog(this,
                    "Commande #" + numCommande + " chargée\nStatut: " + commandeActuelle.getStatut() +
                            "\nTotal: " + String.format("%.2f DT", bilan.getTotal()));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Numéro de commande invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enregistrerModifications() {
        if (commandeActuelle == null) {
            JOptionPane.showMessageDialog(this, "Veuillez d'abord charger une commande");
            return;
        }

        try {
            gestionCommande.modifierCommande(commandeActuelle.getNumCommande(), lignesCommande);

            // Afficher le nouveau bilan
            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(
                    commandeActuelle.getNumCommande());

            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════\n");
            sb.append("  NOUVEAU BILAN DE LA COMMANDE\n");
            sb.append("═══════════════════════════════\n\n");
            sb.append("Numéro: #").append(bilan.getCommande().getNumCommande()).append("\n");
            sb.append("Statut: ").append(bilan.getCommande().getStatut()).append("\n");
            sb.append("Nouveau Total: ").append(String.format("%.2f DT", bilan.getTotal())).append("\n");
            sb.append("\n═══════════════════════════════\n");

            JOptionPane.showMessageDialog(this, sb.toString(),
                    "Modifications enregistrées", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}