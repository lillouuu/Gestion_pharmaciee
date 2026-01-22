package interfaces.employe;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import entite.Employe;
import entite.CvEmployee;
import entitebd.CvEmployeeBD;
import gestion.GestionEmploye;

public class ListerEmployesFrame extends JFrame {
    private JTable tableEmployes;
    private DefaultTableModel tableModel;
    private JButton btnRefresh, btnDetails, btnModifier, btnSupprimer, btnClose;
    private JLabel lblCount, lblStats;
    private GestionEmploye gestionEmploye;
    private CvEmployeeBD cvBD;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public ListerEmployesFrame() {
        gestionEmploye = new GestionEmploye();
        cvBD = new CvEmployeeBD();
        initComponents();
        loadEmployes();
    }

    private void initComponents() {
        setTitle("Liste des Employés");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0, 123, 255));
        topPanel.setPreferredSize(new Dimension(1200, 50));

        JLabel titleLabel = new JLabel("👨‍💼 Liste des employés");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tableau
        String[] columns = {
                "N° Carte", "CNSS", "Nom", "Prénom", "Poste",
                "Salaire", "Téléphone", "Date Recrutement", "Jours/Sem"
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
        tableEmployes.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableEmployes.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableEmployes.getColumnModel().getColumn(3).setPreferredWidth(120);
        tableEmployes.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableEmployes.getColumnModel().getColumn(5).setPreferredWidth(80);
        tableEmployes.getColumnModel().getColumn(6).setPreferredWidth(100);
        tableEmployes.getColumnModel().getColumn(7).setPreferredWidth(100);
        tableEmployes.getColumnModel().getColumn(8).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(tableEmployes);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel stats
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        lblCount = new JLabel("Total: 0 employé(s)");
        lblCount.setFont(new Font("Arial", Font.BOLD, 13));

        lblStats = new JLabel("Masse salariale: 0.00 DT");
        lblStats.setFont(new Font("Arial", Font.BOLD, 13));
        lblStats.setHorizontalAlignment(SwingConstants.RIGHT);

        statsPanel.add(lblCount);
        statsPanel.add(lblStats);

        mainPanel.add(statsPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setPreferredSize(new Dimension(130, 35));
        btnRefresh.setBackground(new Color(0, 123, 255));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadEmployes());

        btnDetails = new JButton("👁 Détails");
        btnDetails.setPreferredSize(new Dimension(130, 35));
        btnDetails.setBackground(new Color(108, 117, 125));
        btnDetails.setForeground(Color.WHITE);
        btnDetails.setFocusPainted(false);
        btnDetails.addActionListener(e -> showDetails());

        btnModifier = new JButton("✏ Modifier");
        btnModifier.setPreferredSize(new Dimension(130, 35));
        btnModifier.setBackground(new Color(255, 165, 0));
        btnModifier.setForeground(Color.WHITE);
        btnModifier.setFocusPainted(false);
        btnModifier.addActionListener(e -> modifierEmploye());

        btnSupprimer = new JButton("🗑 Supprimer");
        btnSupprimer.setPreferredSize(new Dimension(130, 35));
        btnSupprimer.setBackground(new Color(220, 53, 69));
        btnSupprimer.setForeground(Color.WHITE);
        btnSupprimer.setFocusPainted(false);
        btnSupprimer.addActionListener(e -> supprimerEmploye());

        btnClose = new JButton("❌ Fermer");
        btnClose.setPreferredSize(new Dimension(130, 35));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnRefresh);
        btnPanel.add(btnDetails);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);
        btnPanel.add(btnClose);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadEmployes() {
        tableModel.setRowCount(0);

        try {
            List<Employe> employes = gestionEmploye.listerTousEmployes();
            GestionEmploye.StatistiquesEmployes stats = gestionEmploye.obtenirStatistiques();

            for (Employe emp : employes) {
                tableModel.addRow(new Object[]{
                        emp.getNumCarteEmp(),
                        emp.getNumCNSS(),
                        emp.getNom(),
                        emp.getPrenom(),
                        emp.getPoste(),
                        String.format("%.2f DT", emp.getSalaire()),
                        emp.getTelephone(),
                        dateFormat.format(emp.getDateRejoindTravail()),
                        emp.getNbJoursParSemaine()
                });
            }

            lblCount.setText("Total: " + stats.getNbTotal() + " employé(s) - Admins: " + stats.getNbAdmins());
            lblStats.setText(String.format("Masse salariale: %.2f DT | Salaire moyen: %.2f DT",
                    stats.getMasseSalariale(), stats.getSalaireMoyen()));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetails() {
        int selectedRow = tableEmployes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un employé",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            int numCarteEmp = (Integer) tableModel.getValueAt(selectedRow, 0);

            // Rechercher l'employé complet
            List<Employe> employes = gestionEmploye.listerTousEmployes();
            Employe emp = employes.stream()
                    .filter(e -> e.getNumCarteEmp() == numCarteEmp)
                    .findFirst()
                    .orElse(null);

            if (emp == null) return;

            // Rechercher le CV
            CvEmployee cv = gestionEmploye.obtenirCV(numCarteEmp);

            // Créer dialog de détails
            JDialog dialog = new JDialog(this, "Détails Employé #" + numCarteEmp, true);
            dialog.setSize(600, 650);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(10, 10));

            JTextArea txtDetails = new JTextArea();
            txtDetails.setEditable(false);
            txtDetails.setFont(new Font("Monospaced", Font.PLAIN, 12));
            txtDetails.setMargin(new Insets(10, 10, 10, 10));

            StringBuilder details = new StringBuilder();
            details.append("═══════════════════════════════════════════════════\n");
            details.append("              FICHE EMPLOYÉ\n");
            details.append("═══════════════════════════════════════════════════\n\n");

            details.append("👤 INFORMATIONS PERSONNELLES\n");
            details.append("  • N° Carte Identité : ").append(emp.getNumCarteIdentite()).append("\n");
            details.append("  • Nom complet       : ").append(emp.getPrenom()).append(" ").append(emp.getNom()).append("\n");
            details.append("  • Âge               : ").append(emp.getAge()).append(" ans\n");
            details.append("  • Adresse           : ").append(emp.getAdresse()).append("\n");
            details.append("  • Email             : ").append(emp.getAdresseMail()).append("\n");
            details.append("  • Téléphone         : ").append(emp.getTelephone()).append("\n\n");

            details.append("💼 INFORMATIONS PROFESSIONNELLES\n");
            details.append("  • N° Carte Employé  : ").append(emp.getNumCarteEmp()).append("\n");
            details.append("  • N° CNSS           : ").append(emp.getNumCNSS()).append("\n");
            details.append("  • Poste             : ").append(emp.getPoste());
            if (emp.admin()) {
                details.append(" (ADMINISTRATEUR)");
            }
            details.append("\n");
            details.append("  • Salaire           : ").append(String.format("%.2f DT", emp.getSalaire())).append("\n");
            details.append("  • Date recrutement  : ").append(dateFormat.format(emp.getDateRejoindTravail())).append("\n");
            details.append("  • Horaires          : ").append(emp.getHeureDebutTravail())
                    .append(" - ").append(emp.getHeureSortie()).append("\n");
            details.append("  • Jours/semaine     : ").append(emp.getNbJoursParSemaine()).append(" jour(s)\n\n");

            if (cv != null) {
                details.append("📄 CURRICULUM VITAE\n");
                details.append("  • Diplôme           : ").append(cv.getDiplome()).append("\n");
                details.append("  • Expérience        : ").append(cv.getNbAnneeExperience()).append(" an(s)\n");
                details.append("  • Formation         : ").append(cv.getFormation() != null ? cv.getFormation() : "N/A").append("\n");
                details.append("  • Stage             : ").append(cv.getStage() != null ? cv.getStage() : "N/A").append("\n");
            } else {
                details.append("📄 CURRICULUM VITAE\n");
                details.append("  Aucun CV enregistré\n");
            }

            details.append("\n═══════════════════════════════════════════════════\n");

            txtDetails.setText(details.toString());
            txtDetails.setCaretPosition(0);

            JScrollPane scrollPane = new JScrollPane(txtDetails);
            dialog.add(scrollPane, BorderLayout.CENTER);

            JButton btnClose = new JButton("Fermer");
            btnClose.addActionListener(e -> dialog.dispose());
            JPanel btnPanel = new JPanel();
            btnPanel.add(btnClose);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            dialog.setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierEmploye() {
        int selectedRow = tableEmployes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un employé",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int numCarteEmp = (Integer) tableModel.getValueAt(selectedRow, 0);
        ModifierEmployeFrame frame = new ModifierEmployeFrame(numCarteEmp);
        frame.setVisible(true);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                loadEmployes();
            }
        });
    }

    private void supprimerEmploye() {
        int selectedRow = tableEmployes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un employé",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int numCarteEmp = (Integer) tableModel.getValueAt(selectedRow, 0);
        String nom = (String) tableModel.getValueAt(selectedRow, 2);
        String prenom = (String) tableModel.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ ATTENTION ⚠️\n\n" +
                        "Êtes-vous sûr de vouloir supprimer cet employé?\n\n" +
                        "Nom: " + prenom + " " + nom + "\n" +
                        "N° Carte: " + numCarteEmp + "\n\n" +
                        "Cette action supprimera également:\n" +
                        "• Les informations personnelles\n" +
                        "• Le CV (si existant)\n\n" +
                        "Cette action est IRRÉVERSIBLE!",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = gestionEmploye.supprimerEmploye(numCarteEmp);

                if (deleted) {
                    JOptionPane.showMessageDialog(this,
                            "✅ Employé supprimé avec succès!",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadEmployes();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la suppression:\n" + ex.getMessage(),
                        "Erreur BD",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
