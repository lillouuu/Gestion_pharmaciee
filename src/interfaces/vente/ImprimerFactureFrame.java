package interfaces.vente;

import entite.Vente;
import entite.VoieVente;
import entitebd.VenteBD;
import entitebd.VoieVenteBD;
import entitebd.ClientBD;
import entitebd.MedicamentBD;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImprimerFactureFrame extends JFrame {
    private VenteBD venteBD;
    private VoieVenteBD voieVenteBD;
    private ClientBD clientBD;
    private MedicamentBD medicamentBD;

    private JTextField txtNumVente;
    private JTextArea txtFacture;
    private JButton btnRechercher, btnImprimer, btnExporter;

    public ImprimerFactureFrame() {
        venteBD = new VenteBD();
        voieVenteBD = new VoieVenteBD();
        clientBD = new ClientBD();
        medicamentBD = new MedicamentBD();

        initComponents();
    }

    private void initComponents() {
        setTitle("Imprimer Facture");
        setSize(700, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0X292421));
        topPanel.setPreferredSize(new Dimension(700, 50));

        JLabel titleLabel = new JLabel("🧾 Imprimer une Facture");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Rechercher vente"));

        searchPanel.add(new JLabel("Numéro de vente:"));
        txtNumVente = new JTextField(15);
        txtNumVente.addActionListener(e -> rechercherVente());
        searchPanel.add(txtNumVente);

        btnRechercher = new JButton("🔍 Rechercher");
        btnRechercher.setBackground(new Color(0XBAE0D0));
        btnRechercher.setForeground(Color.WHITE);
        btnRechercher.setFocusPainted(false);
        btnRechercher.addActionListener(e -> rechercherVente());
        searchPanel.add(btnRechercher);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel facturePanel = new JPanel(new BorderLayout());
        facturePanel.setBorder(BorderFactory.createTitledBorder("Facture"));

        txtFacture = new JTextArea();
        txtFacture.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtFacture.setEditable(false);
        txtFacture.setBackground(Color.WHITE);
        txtFacture.setText("Veuillez rechercher une vente...");

        JScrollPane scrollPane = new JScrollPane(txtFacture);
        facturePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(facturePanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        btnImprimer = new JButton("🖨 Imprimer");
        btnImprimer.setPreferredSize(new Dimension(140, 35));
        btnImprimer.setBackground(new Color(100, 137, 99));
        btnImprimer.setForeground(Color.WHITE);
        btnImprimer.setFocusPainted(false);
        btnImprimer.setEnabled(false);
        btnImprimer.addActionListener(e -> imprimerFacture());

        btnExporter = new JButton("💾 Exporter");
        btnExporter.setPreferredSize(new Dimension(140, 35));
        btnExporter.setBackground(new Color(188, 45, 90));
        btnExporter.setForeground(Color.WHITE);
        btnExporter.setFocusPainted(false);
        btnExporter.setEnabled(false);
        btnExporter.addActionListener(e -> exporterFacture());

        JButton btnFermer = new JButton("❌ Fermer");
        btnFermer.setPreferredSize(new Dimension(140, 35));
        btnFermer.setBackground(new Color(111, 10, 126));
        btnFermer.setForeground(Color.WHITE);
        btnFermer.setFocusPainted(false);
        btnFermer.addActionListener(e -> dispose());

        btnPanel.add(btnImprimer);
        btnPanel.add(btnExporter);
        btnPanel.add(btnFermer);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void rechercherVente() {
        String numStr = txtNumVente.getText().trim();
        if (numStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir un numéro de vente",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int numVente = Integer.parseInt(numStr);
            Vente vente = venteBD.getVenteById(numVente);

            if (vente == null) {
                JOptionPane.showMessageDialog(this,
                        "Aucune vente trouvée avec le numéro: " + numVente,
                        "Vente introuvable", JOptionPane.INFORMATION_MESSAGE);
                txtFacture.setText("Vente introuvable.");
                btnImprimer.setEnabled(false);
                btnExporter.setEnabled(false);
                return;
            }

            genererFacture(vente);
            btnImprimer.setEnabled(true);
            btnExporter.setEnabled(true);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Le numéro de vente doit être un nombre",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la recherche: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void genererFacture(Vente vente) throws SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à HH:mm");

        StringBuilder facture = new StringBuilder();
        facture.append("╔════════════════════════════════════════════════════════╗\n");
        facture.append("║              PHARMACIE - FACTURE DE VENTE              ║\n");
        facture.append("╚════════════════════════════════════════════════════════╝\n\n");

        facture.append("Facture N°: ").append(vente.getNumVente()).append("\n");
        facture.append("Date: ").append(vente.getDateVente()).append("\n");
        facture.append("Imprimée le: ").append(sdf.format(new Date())).append("\n\n");

        facture.append("────────────────────────────────────────────────────────\n");
        facture.append("INFORMATIONS CLIENT\n");
        facture.append("────────────────────────────────────────────────────────\n\n");

        if (vente.getNumClient() > 0) {
            try {
                entite.Client client = clientBD.rechercherParId(vente.getNumClient());
                if (client != null) {
                    facture.append("Nom: ").append(client.getNom()).append(" ").append(client.getPrenom()).append("\n");
                    facture.append("Code CNAM: ").append(client.getCodeCnam()).append("\n");
                    facture.append("Téléphone: ").append(client.getTelephone()).append("\n");
                    facture.append("Email: ").append(client.getAdresseMail()).append("\n");
                } else {
                    facture.append("Client N°").append(vente.getNumClient()).append("\n");
                }
            } catch (Exception e) {
                facture.append("Client N°").append(vente.getNumClient()).append("\n");
            }
        } else {
            facture.append("Vente directe (sans client enregistré)\n");
        }

        facture.append("\n────────────────────────────────────────────────────────\n");
        facture.append("DÉTAIL DES PRODUITS\n");
        facture.append("────────────────────────────────────────────────────────\n\n");

        ArrayList<VoieVente> lignes = voieVenteBD.getLignesParVente(vente.getNumVente());

        facture.append(String.format("%-30s %5s %10s %12s\n",
                "Produit", "Qté", "Prix Unit.", "Total"));
        facture.append("────────────────────────────────────────────────────────\n");

        double totalGeneral = 0.0;
        for (VoieVente lv : lignes) {
            try {
                entite.Medicament med = medicamentBD.rechercherParRef(lv.getRefMedicament());
                String nomMed = med != null ? med.getNom() : "Produit #" + lv.getRefMedicament();

                if (nomMed.length() > 28) {
                    nomMed = nomMed.substring(0, 25) + "...";
                }

                facture.append(String.format("%-30s %5d %10.2f %12.2f\n",
                        nomMed,
                        lv.getQuantite(),
                        lv.getPrixUnitaire(),
                        lv.getPrixTotalVoieVente()));

                totalGeneral += lv.getPrixTotalVoieVente();
            } catch (Exception e) {
                facture.append(String.format("Produit #%-22d %5d %10.2f %12.2f\n",
                        lv.getRefMedicament(),
                        lv.getQuantite(),
                        lv.getPrixUnitaire(),
                        lv.getPrixTotalVoieVente()));
            }
        }

        facture.append("────────────────────────────────────────────────────────\n");
        facture.append(String.format("%48s %12.2f DT\n", "MONTANT TOTAL:", vente.getMontantTotalVente()));
        facture.append("════════════════════════════════════════════════════════\n\n");

        if (vente.getNumClient() > 0) {
            int pointsGagnes = (int) (vente.getMontantTotalVente() / 10);
            facture.append("Points fidélité gagnés: ").append(pointsGagnes).append(" points\n\n");
        }

        facture.append("Date limite de retour: ").append(vente.getDateLimRendreProduit()).append("\n\n");

        facture.append("────────────────────────────────────────────────────────\n");
        facture.append("Merci de votre confiance!\n");
        facture.append("════════════════════════════════════════════════════════\n");

        txtFacture.setText(facture.toString());
        txtFacture.setCaretPosition(0);
    }

    private void imprimerFacture() {
        try {
            boolean complete = txtFacture.print();
            if (complete) {
                JOptionPane.showMessageDialog(this,
                        "✓ Impression envoyée avec succès!",
                        "Impression", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Impression annulée",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.awt.print.PrinterException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur d'impression: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exporterFacture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer la facture");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        fileChooser.setSelectedFile(new java.io.File(
                "facture_" + txtNumVente.getText() + "_" + sdf.format(new Date()) + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile());
                writer.write(txtFacture.getText());
                writer.close();

                JOptionPane.showMessageDialog(this,
                        "✓ Facture exportée avec succès!\nFichier: " +
                                fileChooser.getSelectedFile().getName(),
                        "Succès", JOptionPane.INFORMATION_MESSAGE);

            } catch (java.io.IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'export: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}