package interfaces.stock;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import gestion.GestionStock;

public class RapportStockFrame extends JFrame {
    private JTextArea txtRapport;
    private JButton btnGenerate, btnExport, btnPrint, btnClose;
    private GestionStock gestionStock;

    public RapportStockFrame() {
        gestionStock = new GestionStock();
        initComponents();
        genererRapport();
    }

    private void initComponents() {
        setTitle("Rapport de Stock");
        setSize(800, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(236, 72, 153));
        topPanel.setPreferredSize(new Dimension(800, 50));

        JLabel titleLabel = new JLabel("📊 Rapport d'état du stock");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // En-tête du rapport
        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Informations du rapport"));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        headerPanel.add(new JLabel("📅 Date de génération: " + sdf.format(new Date())));
        headerPanel.add(new JLabel("🏢 Pharmacie - Système de gestion"));
        headerPanel.add(new JLabel("📋 Type: Rapport complet de stock"));

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Zone de texte pour le rapport
        txtRapport = new JTextArea();
        txtRapport.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtRapport.setEditable(false);
        txtRapport.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(txtRapport);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        btnGenerate = new JButton("🔄 Actualiser");
        btnGenerate.setPreferredSize(new Dimension(140, 35));
        btnGenerate.setBackground(new Color(99, 102, 241));
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFocusPainted(false);
        btnGenerate.addActionListener(e -> genererRapport());

        btnExport = new JButton("💾 Exporter");
        btnExport.setPreferredSize(new Dimension(140, 35));
        btnExport.setBackground(new Color(236, 72, 153));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.addActionListener(e -> exporterRapport());

        btnPrint = new JButton("🖨 Imprimer");
        btnPrint.setPreferredSize(new Dimension(140, 35));
        btnPrint.setBackground(new Color(108, 117, 125));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);
        btnPrint.addActionListener(e -> imprimerRapport());

        btnClose = new JButton("❌ Fermer");
        btnClose.setPreferredSize(new Dimension(140, 35));
        btnClose.setBackground(new Color(99, 102, 241));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnGenerate);
        btnPanel.add(btnExport);
        btnPanel.add(btnPrint);
        btnPanel.add(btnClose);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void genererRapport() {
        txtRapport.setText("Génération du rapport en cours...\n");

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return gestionStock.genererRapportStock();
            }

            @Override
            protected void done() {
                try {
                    String rapport = get();
                    txtRapport.setText(rapport);
                    txtRapport.setCaretPosition(0);
                } catch (Exception ex) {
                    txtRapport.setText("❌ Erreur lors de la génération du rapport:\n" + ex.getMessage());
                    JOptionPane.showMessageDialog(RapportStockFrame.this,
                            "Erreur: " + ex.getMessage(),
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void exporterRapport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le rapport");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        fileChooser.setSelectedFile(new java.io.File("rapport_stock_" + sdf.format(new Date()) + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile());
                writer.write("╔════════════════════════════════════════════════════════════════╗\n");
                writer.write("║            RAPPORT DE STOCK - PHARMACIE                        ║\n");
                writer.write("║  Généré le: " + new SimpleDateFormat("dd/MM/yyyy à HH:mm:ss").format(new Date()) + "                             ║\n");
                writer.write("╚════════════════════════════════════════════════════════════════╝\n\n");
                writer.write(txtRapport.getText());
                writer.write("\n\n");
                writer.write("═══════════════════════════════════════════════════════════════\n");
                writer.write("Rapport généré automatiquement par le système de gestion\n");
                writer.write("═══════════════════════════════════════════════════════════════\n");
                writer.close();

                JOptionPane.showMessageDialog(this,
                        "✓ Rapport exporté avec succès!\n" +
                                "Fichier: " + fileChooser.getSelectedFile().getName(),
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

    private void imprimerRapport() {
        try {
            boolean complete = txtRapport.print();
            if (complete) {
                JOptionPane.showMessageDialog(this,
                        "✓ Impression envoyée avec succès!",
                        "Impression",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Impression annulée",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur d'impression: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
