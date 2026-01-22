package interfaces.fournisseur;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.Fournisseur;
import entitebd.FournisseurBD;

public class SupprimerFournisseurFrame extends JFrame {
    private JTable tableFournisseurs;
    private DefaultTableModel tableModel;
    private JButton btnDelete, btnCancel, btnRefresh;
    private FournisseurBD fournisseurBD;

    public SupprimerFournisseurFrame() {
        fournisseurBD = new FournisseurBD();
        initComponents();
        loadFournisseurs();
    }

    private void initComponents() {
        setTitle("Supprimer un Fournisseur");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(220, 53, 69));
        topPanel.setPreferredSize(new Dimension(900, 50));

        JLabel titleLabel = new JLabel("🗑️ Supprimer un fournisseur");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tableau
        String[] columns = {"N°", "Nom", "Adresse", "Téléphone", "Email", "Évaluation"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableFournisseurs = new JTable(tableModel);
        tableFournisseurs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableFournisseurs.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tableFournisseurs);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel d'avertissement
        JPanel warningPanel = new JPanel();
        warningPanel.setBackground(new Color(255, 243, 205));
        warningPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7), 2));
        JLabel lblWarning = new JLabel("⚠ Attention: La suppression d'un fournisseur supprimera aussi ses médicaments associés!");
        lblWarning.setFont(new Font("Arial", Font.BOLD, 12));
        lblWarning.setForeground(new Color(133, 100, 4));
        warningPanel.add(lblWarning);
        mainPanel.add(warningPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setPreferredSize(new Dimension(150, 35));
        btnRefresh.setBackground(new Color(0, 123, 255));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadFournisseurs());

        btnDelete = new JButton("🗑️ Supprimer");
        btnDelete.setPreferredSize(new Dimension(150, 35));
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> deleteFournisseur());

        btnCancel = new JButton("❌ Fermer");
        btnCancel.setPreferredSize(new Dimension(150, 35));
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnRefresh);
        btnPanel.add(btnDelete);
        btnPanel.add(btnCancel);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadFournisseurs() {
        tableModel.setRowCount(0);
        try {
            List<Fournisseur> fournisseurs = fournisseurBD.listerTous();
            for (Fournisseur f : fournisseurs) {
                tableModel.addRow(new Object[]{
                        f.getNumFournisseur(),
                        f.getNomFournisseur(),
                        f.getAdresse(),
                        f.getTelephone(),
                        f.getAdresseEmail(),
                        String.format("%.1f/5", f.getRate())
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteFournisseur() {
        int selectedRow = tableFournisseurs.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un fournisseur à supprimer!",
                    "Sélection requise",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int numFournisseur = (int) tableModel.getValueAt(selectedRow, 0);
        String nomFournisseur = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer ce fournisseur?\n\n" +
                        "Numéro: " + numFournisseur + "\n" +
                        "Nom: " + nomFournisseur + "\n\n" +
                        "⚠ Cette action est irréversible!",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = fournisseurBD.supprimer(numFournisseur);

                if (deleted) {
                    JOptionPane.showMessageDialog(this,
                            "Fournisseur supprimé avec succès!",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadFournisseurs();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Erreur lors de la suppression!",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur de base de données: " + ex.getMessage(),
                        "Erreur BD",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
