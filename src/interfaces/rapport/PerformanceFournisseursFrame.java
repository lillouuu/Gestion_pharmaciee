package interfaces.rapport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import entite.Fournisseur;
import entitebd.FournisseurBD;

public class PerformanceFournisseursFrame extends JFrame {
    private JTable tableFournisseurs;
    private DefaultTableModel tableModel;
    private JTextArea txtAnalyse;
    private JButton btnRefresh, btnExport, btnClose;
    private FournisseurBD fournisseurBD;

    public PerformanceFournisseursFrame() {
        fournisseurBD = new FournisseurBD();
        initComponents();
        loadPerformance();
    }

    private void initComponents() {
        setTitle("Performance des Fournisseurs");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0, 123, 255));
        topPanel.setPreferredSize(new Dimension(1000, 50));

        JLabel titleLabel = new JLabel("🏢 Performance des Fournisseurs");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Panel tableau
        JPanel tablePanel = createTablePanel();
        splitPane.setTopComponent(tablePanel);

        // Panel analyse
        JPanel analysePanel = createAnalysePanel();
        splitPane.setBottomComponent(analysePanel);

        splitPane.setDividerLocation(400);
        add(splitPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = createButtonPanel();
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Classement des fournisseurs"));

        // Tableau
        String[] columns = {"Rang", "N°", "Nom", "Évaluation", "Nb Commandes", "Performance", "Statut"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableFournisseurs = new JTable(tableModel);
        tableFournisseurs.setRowHeight(25);
        tableFournisseurs.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableFournisseurs.getColumnModel().getColumn(2).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(tableFournisseurs);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAnalysePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Analyse détaillée"));

        txtAnalyse = new JTextArea();
        txtAnalyse.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtAnalyse.setEditable(false);
        txtAnalyse.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(txtAnalyse);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setPreferredSize(new Dimension(140, 35));
        btnRefresh.setBackground(new Color(0, 123, 255));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadPerformance());

        btnExport = new JButton("💾 Exporter");
        btnExport.setPreferredSize(new Dimension(140, 35));
        btnExport.setBackground(new Color(40, 167, 69));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.addActionListener(e -> exportRapport());

        btnClose = new JButton("❌ Fermer");
        btnClose.setPreferredSize(new Dimension(140, 35));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        panel.add(btnRefresh);
        panel.add(btnExport);
        panel.add(btnClose);

        return panel;
    }

    private void loadPerformance() {
        tableModel.setRowCount(0);

        try {
            List<Fournisseur> fournisseurs = fournisseurBD.listerTous();

            // Données pour l'analyse
            int totalFournisseurs = fournisseurs.size();
            double sommeRates = 0;
            int nbExcellent = 0, nbBon = 0, nbMoyen = 0, nbFaible = 0;

            int rang = 1;
            for (Fournisseur f : fournisseurs) {
                double performance = fournisseurBD.calculerPerformance(f.getNumFournisseur());

                // Nombre de commandes (simulation basée sur la performance)
                int nbCommandes = (int) (performance * 100 / f.getRate());

                // Déterminer le statut
                String statut;
                if (f.getRate() >= 4.0) {
                    statut = "⭐ Excellent";
                    nbExcellent++;
                } else if (f.getRate() >= 3.0) {
                    statut = "✓ Bon";
                    nbBon++;
                } else if (f.getRate() >= 2.0) {
                    statut = "○ Moyen";
                    nbMoyen++;
                } else {
                    statut = "⚠ Faible";
                    nbFaible++;
                }

                sommeRates += f.getRate();

                tableModel.addRow(new Object[]{
                        rang++,
                        f.getNumFournisseur(),
                        f.getNomFournisseur(),
                        String.format("%.1f/5", f.getRate()),
                        nbCommandes,
                        String.format("%.2f%%", performance),
                        statut
                });
            }

            // Générer l'analyse
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════════════\n");
            sb.append("        ANALYSE DE PERFORMANCE DES FOURNISSEURS\n");
            sb.append("═══════════════════════════════════════════════════════════\n\n");
            sb.append("Date: ").append(new SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date())).append("\n\n");

            sb.append("📊 STATISTIQUES GLOBALES\n");
            sb.append("  • Nombre total de fournisseurs : ").append(totalFournisseurs).append("\n");

            if (totalFournisseurs > 0) {
                double moyenneRate = sommeRates / totalFournisseurs;
                sb.append("  • Évaluation moyenne           : ").append(String.format("%.2f/5", moyenneRate)).append("\n");
            }

            sb.append("\n📈 RÉPARTITION PAR CATÉGORIE\n");
            sb.append("  • Excellent (≥4.0)  : ").append(nbExcellent).append(" fournisseur(s)\n");
            sb.append("  • Bon (≥3.0)        : ").append(nbBon).append(" fournisseur(s)\n");
            sb.append("  • Moyen (≥2.0)      : ").append(nbMoyen).append(" fournisseur(s)\n");
            sb.append("  • Faible (<2.0)     : ").append(nbFaible).append(" fournisseur(s)\n");

            sb.append("\n💡 RECOMMANDATIONS\n");
            if (nbFaible > 0) {
                sb.append("  ⚠ Attention: ").append(nbFaible).append(" fournisseur(s) avec évaluation faible\n");
                sb.append("     → Envisager une réévaluation ou changement de fournisseur\n");
            }
            if (nbExcellent > 0) {
                sb.append("  ✓ ").append(nbExcellent).append(" fournisseur(s) excellent(s)\n");
                sb.append("     → Maintenir et renforcer la collaboration\n");
            }

            sb.append("\n═══════════════════════════════════════════════════════════\n");

            txtAnalyse.setText(sb.toString());
            txtAnalyse.setCaretPosition(0);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportRapport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le rapport");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        fileChooser.setSelectedFile(new java.io.File("rapport_fournisseurs_" + sdf.format(new Date()) + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile());
                writer.write(txtAnalyse.getText());
                writer.write("\n\nDÉTAIL DES FOURNISSEURS\n");
                writer.write("═══════════════════════════════════════════════════════════\n\n");

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    writer.write(String.format("Rang %d: %s\n",
                            tableModel.getValueAt(i, 0),
                            tableModel.getValueAt(i, 2)));
                    writer.write(String.format("  Évaluation: %s\n", tableModel.getValueAt(i, 3)));
                    writer.write(String.format("  Performance: %s\n", tableModel.getValueAt(i, 5)));
                    writer.write(String.format("  Statut: %s\n\n", tableModel.getValueAt(i, 6)));
                }

                writer.close();

                JOptionPane.showMessageDialog(this,
                        "✓ Rapport exporté avec succès!",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'export: " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
