package interfaces.commande;

import entite.Commande;
import entite.VoieCommande;
import gestion.GestionCommande;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class ListerCommandesFrame extends JFrame {
    private GestionCommande gestionCommande;

    private JTable tableCommandes;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbFiltre;

    public ListerCommandesFrame() {
        gestionCommande = new GestionCommande();
        initComponents();
        chargerCommandes("Toutes");
    }

    private void initComponents() {
        setTitle("Historique des Commandes");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel supérieur - Filtres
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Panel central - Table
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Panel inférieur - Boutons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Filtres"));

        panel.add(new JLabel("Filtrer par statut:"));

        cmbFiltre = new JComboBox<>(new String[]{
                "Toutes", "En attente", "Reçue", "Annulée"
        });
        cmbFiltre.addActionListener(e -> chargerCommandes((String) cmbFiltre.getSelectedItem()));
        panel.add(cmbFiltre);

        JButton btnActualiser = new JButton("🔄 Actualiser");
        btnActualiser.setBackground(new Color(0, 123, 255));
        btnActualiser.setForeground(Color.WHITE);
        btnActualiser.addActionListener(e -> chargerCommandes((String) cmbFiltre.getSelectedItem()));
        panel.add(btnActualiser);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Liste des Commandes"));

        String[] columns = {
                "N° Commande", "Date Achat", "Date Limite", "Statut",
                "Fournisseur", "Employé", "Total (DT)"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableCommandes = new JTable(tableModel);
        tableCommandes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Colorer les lignes selon le statut
        tableCommandes.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String statut = (String) table.getValueAt(row, 3);
                    if ("Reçue".equals(statut)) {
                        c.setBackground(new Color(200, 255, 200));
                    } else if ("Annulée".equals(statut)) {
                        c.setBackground(new Color(255, 200, 200));
                    } else if ("En attente".equals(statut)) {
                        c.setBackground(new Color(255, 255, 200));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableCommandes);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnDetails = new JButton("📋 Voir Détails");
        btnDetails.setBackground(new Color(0, 123, 255));
        btnDetails.setForeground(Color.WHITE);
        btnDetails.addActionListener(e -> afficherDetails());

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setBackground(new Color(108, 117, 125));
        btnFermer.setForeground(Color.WHITE);
        btnFermer.addActionListener(e -> dispose());

        panel.add(btnDetails);
        panel.add(btnFermer);

        return panel;
    }

    private void chargerCommandes(String filtre) {
        try {
            ArrayList<Commande> commandes = gestionCommande.listerToutesCommandes();

            tableModel.setRowCount(0);

            for (Commande cmd : commandes) {
                // Appliquer le filtre
                if (!"Toutes".equals(filtre) && !filtre.equals(cmd.getStatut())) {
                    continue;
                }

                tableModel.addRow(new Object[]{
                        cmd.getNumCommande(),
                        cmd.getDateAchat(),
                        cmd.getDateLimRendreProduit(),
                        cmd.getStatut(),
                        cmd.getNumFournisseur(),
                        cmd.getNumCarteEmp(),
                        String.format("%.2f", cmd.getMontantTotalCommande())
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void afficherDetails() {
        int selectedRow = tableCommandes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner une commande",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            int numCommande = (Integer) tableModel.getValueAt(selectedRow, 0);

            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(numCommande);
            Commande cmd = bilan.getCommande();
            ArrayList<VoieCommande> lignes = bilan.getLignes();

            // Créer un dialog pour afficher les détails
            JDialog dialog = new JDialog(this, "Détails Commande #" + numCommande, true);
            dialog.setSize(700, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(10, 10));

            // Informations générales
            JPanel infoPanel = new JPanel(new GridLayout(7, 2, 5, 5));
            infoPanel.setBorder(BorderFactory.createTitledBorder("Informations Générales"));

            infoPanel.add(new JLabel("Numéro:"));
            infoPanel.add(new JLabel(String.valueOf(cmd.getNumCommande())));

            infoPanel.add(new JLabel("Date d'achat:"));
            infoPanel.add(new JLabel(cmd.getDateAchat()));

            infoPanel.add(new JLabel("Date limite:"));
            infoPanel.add(new JLabel(cmd.getDateLimRendreProduit()));

            infoPanel.add(new JLabel("Statut:"));
            JLabel lblStatut = new JLabel(cmd.getStatut());
            lblStatut.setFont(new Font("Arial", Font.BOLD, 12));
            if ("Reçue".equals(cmd.getStatut())) {
                lblStatut.setForeground(new Color(0, 128, 0));
            } else if ("Annulée".equals(cmd.getStatut())) {
                lblStatut.setForeground(new Color(200, 0, 0));
            }
            infoPanel.add(lblStatut);

            infoPanel.add(new JLabel("Fournisseur:"));
            infoPanel.add(new JLabel(String.valueOf(cmd.getNumFournisseur())));

            infoPanel.add(new JLabel("Employé:"));
            infoPanel.add(new JLabel(String.valueOf(cmd.getNumCarteEmp())));

            infoPanel.add(new JLabel("Total:"));
            JLabel lblTotal = new JLabel(String.format("%.2f DT", cmd.getMontantTotalCommande()));
            lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
            infoPanel.add(lblTotal);

            dialog.add(infoPanel, BorderLayout.NORTH);

            // Table des lignes
            String[] columns = {"Médicament", "Quantité", "Prix Unit.", "Remise %", "Impôts %", "Total"};
            DefaultTableModel detailsModel = new DefaultTableModel(columns, 0);

            for (VoieCommande ligne : lignes) {
                // Récupérer le nom du médicament
                String nomMed = "Médicament #" + ligne.getRefMedicament();
                try {
                    entitebd.MedicamentBD medBD = new entitebd.MedicamentBD();
                    entite.Medicament med = medBD.rechercherParRef(ligne.getRefMedicament());
                    if (med != null) {
                        nomMed = med.getNom();
                    }
                } catch (Exception ex) {
                    // Ignorer
                }

                detailsModel.addRow(new Object[]{
                        nomMed,
                        ligne.getQuantite(),
                        String.format("%.2f", ligne.getPrixUnitaire()),
                        String.format("%.1f", ligne.getRemise()),
                        String.format("%.1f", ligne.getImpotSurCommande()),
                        String.format("%.2f DT", ligne.calculerTotal())
                });
            }

            JTable tableDetails = new JTable(detailsModel);
            JScrollPane scrollPane = new JScrollPane(tableDetails);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Lignes de la Commande"));
            dialog.add(scrollPane, BorderLayout.CENTER);

            // Bouton fermer
            JButton btnClose = new JButton("Fermer");
            btnClose.addActionListener(e -> dialog.dispose());
            JPanel btnPanel = new JPanel();
            btnPanel.add(btnClose);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            dialog.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'affichage des détails: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
