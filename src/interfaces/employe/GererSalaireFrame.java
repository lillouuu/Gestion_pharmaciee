package interfaces.employe;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.Employe;
import gestion.GestionEmploye;

public class GererSalaireFrame extends JFrame {
    private JTable tableEmployes;
    private DefaultTableModel tableModel;
    private JTextField txtNouveauSalaire, txtPourcentage;
    private JButton btnModifier, btnAugmentation, btnRefresh, btnClose;
    private JLabel lblStats;
    private GestionEmploye gestionEmploye;

    public GererSalaireFrame() {
        gestionEmploye = new GestionEmploye();
        initComponents();
        loadEmployes();
    }

    private void initComponents() {
        setTitle("Gestion des Salaires");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(40, 167, 69));
        topPanel.setPreferredSize(new Dimension(1000, 50));

        JLabel titleLabel = new JLabel("💰 Gestion des salaires");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tableau
        String[] columns = {
                "N° Carte", "Nom", "Prénom", "Poste", "Salaire Actuel",
                "Date Recrutement", "Ancienneté"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableEmployes = new JTable(tableModel);
        tableEmployes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEmployes.setRowHeight(25);
        tableEmployes.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableEmployes.getColumnModel().getColumn(1).setPreferredWidth(120);
        tableEmployes.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableEmployes.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableEmployes.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableEmployes.getColumnModel().getColumn(5).setPreferredWidth(100);
        tableEmployes.getColumnModel().getColumn(6).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(tableEmployes);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel de modification
        JPanel modifPanel = createModificationPanel();
        mainPanel.add(modifPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Panel stats
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        lblStats = new JLabel("");
        lblStats.setFont(new Font("Arial", Font.BOLD, 13));
        statsPanel.add(lblStats);

        add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createModificationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Modifier le salaire"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nouveau salaire
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nouveau salaire (DT):"), gbc);
        gbc.gridx = 1;
        txtNouveauSalaire = new JTextField(15);
        panel.add(txtNouveauSalaire, gbc);

        gbc.gridx = 2;
        btnModifier = new JButton("✏️ Modifier");
        btnModifier.setBackground(new Color(255, 165, 0));
        btnModifier.setForeground(Color.WHITE);
        btnModifier.setFocusPainted(false);
        btnModifier.addActionListener(e -> modifierSalaire());
        panel.add(btnModifier, gbc);

        // Augmentation en pourcentage
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Augmentation (%):"), gbc);
        gbc.gridx = 1;
        txtPourcentage = new JTextField(15);
        panel.add(txtPourcentage, gbc);

        gbc.gridx = 2;
        btnAugmentation = new JButton("📈 Appliquer");
        btnAugmentation.setBackground(new Color(40, 167, 69));
        btnAugmentation.setForeground(Color.WHITE);
        btnAugmentation.setFocusPainted(false);
        btnAugmentation.addActionListener(e -> appliquerAugmentation());
        panel.add(btnAugmentation, gbc);

        // Boutons de contrôle
        gbc.gridx = 3; gbc.gridy = 0; gbc.gridheight = 2;
        JPanel ctrlPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        btnRefresh = new JButton("🔄");
        btnRefresh.setFocusPainted(false);
        btnRefresh.setToolTipText("Actualiser");
        btnRefresh.addActionListener(e -> loadEmployes());
        ctrlPanel.add(btnRefresh);

        btnClose = new JButton("❌");
        btnClose.setFocusPainted(false);
        btnClose.setToolTipText("Fermer");
        btnClose.addActionListener(e -> dispose());
        ctrlPanel.add(btnClose);

        panel.add(ctrlPanel, gbc);

        return panel;
    }

    private void loadEmployes() {
        tableModel.setRowCount(0);

        try {
            List<Employe> employes = gestionEmploye.listerTousEmployes();
            GestionEmploye.StatistiquesEmployes stats = gestionEmploye.obtenirStatistiques();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

            for (Employe emp : employes) {
                // Calculer l'ancienneté en années
                long diffMillis = System.currentTimeMillis() - emp.getDateRejoindTravail().getTime();
                int anciennete = (int) (diffMillis / (1000L * 60 * 60 * 24 * 365));

                tableModel.addRow(new Object[]{
                        emp.getNumCarteEmp(),
                        emp.getNom(),
                        emp.getPrenom(),
                        emp.getPoste(),
                        String.format("%.2f DT", emp.getSalaire()),
                        sdf.format(emp.getDateRejoindTravail()),
                        anciennete + " an(s)"
                });
            }

            lblStats.setText(String.format(
                    "Total: %d employé(s) | Masse salariale: %.2f DT | Salaire moyen: %.2f DT",
                    stats.getNbTotal(), stats.getMasseSalariale(), stats.getSalaireMoyen()
            ));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierSalaire() {
        int selectedRow = tableEmployes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un employé",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String salaireTxt = txtNouveauSalaire.getText().trim();
        if (salaireTxt.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir un nouveau salaire",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int numCarteEmp = (Integer) tableModel.getValueAt(selectedRow, 0);
            String nom = (String) tableModel.getValueAt(selectedRow, 1);
            String prenom = (String) tableModel.getValueAt(selectedRow, 2);
            String salaireActuel = (String) tableModel.getValueAt(selectedRow, 4);

            double nouveauSalaire = Double.parseDouble(salaireTxt);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Modifier le salaire de cet employé?\n\n" +
                            "Nom: " + prenom + " " + nom + "\n" +
                            "Salaire actuel: " + salaireActuel + "\n" +
                            "Nouveau salaire: " + String.format("%.2f DT", nouveauSalaire),
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean updated = gestionEmploye.modifierSalaire(numCarteEmp, nouveauSalaire);

                if (updated) {
                    JOptionPane.showMessageDialog(this,
                            "✅ Salaire modifié avec succès!",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadEmployes();
                    txtNouveauSalaire.setText("");
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Le salaire doit être un nombre valide!",
                    "Erreur de saisie",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de base de données:\n" + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void appliquerAugmentation() {
        int selectedRow = tableEmployes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un employé",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String pctTxt = txtPourcentage.getText().trim();
        if (pctTxt.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir un pourcentage",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int numCarteEmp = (Integer) tableModel.getValueAt(selectedRow, 0);
            String nom = (String) tableModel.getValueAt(selectedRow, 1);
            String prenom = (String) tableModel.getValueAt(selectedRow, 2);
            String salaireActuelStr = (String) tableModel.getValueAt(selectedRow, 4);

            double pourcentage = Double.parseDouble(pctTxt);

            // Extraire le salaire actuel (enlever " DT")
            double salaireActuel = Double.parseDouble(salaireActuelStr.replace(" DT", "").replace(",", "."));
            double nouveauSalaire = salaireActuel * (1 + pourcentage / 100);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Appliquer une augmentation de " + pourcentage + "% ?\n\n" +
                            "Nom: " + prenom + " " + nom + "\n" +
                            "Salaire actuel: " + salaireActuelStr + "\n" +
                            "Nouveau salaire: " + String.format("%.2f DT", nouveauSalaire) + "\n" +
                            "Augmentation: " + String.format("%.2f DT", nouveauSalaire - salaireActuel),
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean updated = gestionEmploye.modifierSalaire(numCarteEmp, nouveauSalaire);

                if (updated) {
                    JOptionPane.showMessageDialog(this,
                            "✅ Augmentation appliquée avec succès!",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadEmployes();
                    txtPourcentage.setText("");
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Le pourcentage doit être un nombre valide!",
                    "Erreur de saisie",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de base de données:\n" + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
