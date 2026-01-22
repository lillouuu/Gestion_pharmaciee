package interfaces.rapport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import entitebd.VenteBD;
import gestion.GestionVente;

public class ChiffresAffairesFrame extends JFrame {
    private JTextArea txtRapport;
    private JTable tableVentes;
    private DefaultTableModel tableModel;
    private JButton btnGenerate, btnExport, btnClose;
    private JLabel lblTotal, lblNbVentes;
    private GestionVente gestionVente;
    private VenteBD venteBD;

    public ChiffresAffairesFrame() {
        gestionVente = new GestionVente();
        venteBD = new VenteBD();
        initComponents();
        generateRapport();
    }

    private void initComponents() {
        setTitle("Chiffre d'Affaires");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(40, 167, 69));
        topPanel.setPreferredSize(new Dimension(1000, 50));

        JLabel titleLabel = new JLabel("💵 Rapport Chiffre d'Affaires");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal avec split
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Panel haut - Résumé
        JPanel topSplitPanel = createSummaryPanel();
        splitPane.setTopComponent(topSplitPanel);

        // Panel bas - Détails
        JPanel bottomSplitPanel = createDetailsPanel();
        splitPane.setBottomComponent(bottomSplitPanel);

        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = createButtonPanel();
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Panel statistiques
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistiques"));

        lblNbVentes = new JLabel("Nombre de ventes: 0");
        lblNbVentes.setFont(new Font("Arial", Font.BOLD, 14));
        lblNbVentes.setHorizontalAlignment(SwingConstants.CENTER);

        lblTotal = new JLabel("Chiffre d'affaires: 0.00 DT");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotal.setForeground(new Color(40, 167, 69));
        lblTotal.setHorizontalAlignment(SwingConstants.CENTER);

        statsPanel.add(lblNbVentes);
        statsPanel.add(lblTotal);

        panel.add(statsPanel, BorderLayout.NORTH);

        // Zone de texte pour le rapport
        txtRapport = new JTextArea();
        txtRapport.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtRapport.setEditable(false);
        txtRapport.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(txtRapport);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Détail des ventes"));

        // Tableau
        String[] columns = {"N° Vente", "Date", "Montant", "Client", "Employé"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableVentes = new JTable(tableModel);
        tableVentes.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tableVentes);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnGenerate = new JButton("🔄 Actualiser");
        btnGenerate.setPreferredSize(new Dimension(140, 35));
        btnGenerate.setBackground(new Color(0, 123, 255));
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFocusPainted(false);
        btnGenerate.addActionListener(e -> generateRapport());

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

        panel.add(btnGenerate);
        panel.add(btnExport);
        panel.add(btnClose);

        return panel;
    }

    private void generateRapport() {
        try {
            // Calculer le chiffre d'affaires
            double ca = gestionVente.calculerChiffreAffaires();

            // Récupérer les ventes
            var ventes = venteBD.getAllVentes();

            // Générer le rapport textuel
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════════════\n");
            sb.append("            RAPPORT CHIFFRE D'AFFAIRES\n");
            sb.append("═══════════════════════════════════════════════════════════\n\n");
            sb.append("Date du rapport: ").append(new SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date())).append("\n\n");
            sb.append("📊 STATISTIQUES GÉNÉRALES\n");
            sb.append("  • Nombre total de ventes : ").append(ventes.size()).append("\n");
            sb.append("  • Chiffre d'affaires     : ").append(String.format("%.2f DT", ca)).append("\n");

            if (!ventes.isEmpty()) {
                double moyenne = ca / ventes.size();
                sb.append("  • Montant moyen/vente    : ").append(String.format("%.2f DT", moyenne)).append("\n");
            }

            sb.append("\n═══════════════════════════════════════════════════════════\n");

            txtRapport.setText(sb.toString());
            txtRapport.setCaretPosition(0);

            // Mettre à jour les labels
            lblNbVentes.setText("Nombre de ventes: " + ventes.size());
            lblTotal.setText(String.format("Chiffre d'affaires: %.2f DT", ca));

            // Remplir le tableau
            tableModel.setRowCount(0);
            for (var vente : ventes) {
                tableModel.addRow(new Object[]{
                        vente.getNumVente(),
                        vente.getDateVente(),
                        String.format("%.2f DT", vente.getMontantTotalVente()),
                        "Client #" + vente.getNumClient(),
                        "Emp #" + vente.getNumCarteEmp()
                });
            }

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
        fileChooser.setSelectedFile(new java.io.File("rapport_ca_" + sdf.format(new Date()) + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile());
                writer.write(txtRapport.getText());
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
