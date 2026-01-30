package interfaces.commande;

import entite.Commande;
import gestion.GestionCommande;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class AnnulerCommandeFrame extends JFrame {
    private GestionCommande gestionCommande;

    private JTextField txtNumCommande;
    private JTextArea txtInfoCommande;

    public AnnulerCommandeFrame() {
        gestionCommande = new GestionCommande();
        initComponents();
    }

    private void initComponents() {
        setTitle("Annuler une Commande");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel supérieur
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Panel central - Informations
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Panel inférieur - Boutons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Rechercher Commande"));

        panel.add(new JLabel("Numéro Commande:"));

        txtNumCommande = new JTextField(15);
        panel.add(txtNumCommande);

        JButton btnRechercher = new JButton("🔍 Rechercher");
        btnRechercher.setBackground(new Color(236, 72, 153));
        btnRechercher.setForeground(Color.WHITE);
        btnRechercher.addActionListener(e -> rechercherCommande());
        panel.add(btnRechercher);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Informations de la Commande"));

        txtInfoCommande = new JTextArea(10, 40);
        txtInfoCommande.setEditable(false);
        txtInfoCommande.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtInfoCommande.setBackground(new Color(245, 245, 245));
        txtInfoCommande.setText("Veuillez rechercher une commande...");

        JScrollPane scrollPane = new JScrollPane(txtInfoCommande);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAnnuler = new JButton("❌ Annuler la Commande");
        btnAnnuler.setBackground(new Color(79, 70, 229));
        btnAnnuler.setForeground(Color.WHITE);
        btnAnnuler.setFont(new Font("Arial", Font.BOLD, 14));
        btnAnnuler.addActionListener(e -> annulerCommande());

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setBackground(new Color(108, 117, 125));
        btnFermer.setForeground(Color.WHITE);
        btnFermer.addActionListener(e -> dispose());

        panel.add(btnAnnuler);
        panel.add(btnFermer);

        return panel;
    }

    private void rechercherCommande() {
        try {
            int numCommande = Integer.parseInt(txtNumCommande.getText());

            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(numCommande);
            Commande commande = bilan.getCommande();

            // Afficher les informations
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════\n");
            sb.append("       INFORMATIONS DE LA COMMANDE\n");
            sb.append("═══════════════════════════════════════\n\n");
            sb.append("Numéro Commande: #").append(commande.getNumCommande()).append("\n");
            sb.append("Date d'achat: ").append(commande.getDateAchat()).append("\n");
            sb.append("Date limite: ").append(commande.getDateLimRendreProduit()).append("\n");
            sb.append("Statut: ").append(commande.getStatut()).append("\n");
            sb.append("Fournisseur: ").append(commande.getNumFournisseur()).append("\n");
            sb.append("Nombre de lignes: ").append(bilan.getNombreLignes()).append("\n");
            sb.append("Total: ").append(String.format("%.2f DT", bilan.getTotal())).append("\n");
            sb.append("\n═══════════════════════════════════════\n");

            // Vérifier si annulable
            if ("Reçue".equals(commande.getStatut())) {
                sb.append("\n⚠️ ATTENTION: Cette commande est déjà reçue.\n");
                sb.append("Elle ne peut pas être annulée.\n");
            } else if ("Annulée".equals(commande.getStatut())) {
                sb.append("\n⚠️ Cette commande est déjà annulée.\n");
            } else {
                sb.append("\n✅ Cette commande peut être annulée.\n");
            }

            txtInfoCommande.setText(sb.toString());

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez entrer un numéro de commande valide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            txtInfoCommande.setText("Commande introuvable.");
        }
    }

    private void annulerCommande() {
        try {
            int numCommande = Integer.parseInt(txtNumCommande.getText());

            // Demander confirmation
            int confirmation = JOptionPane.showConfirmDialog(this,
                    "⚠️ ATTENTION ⚠️\n\n" +
                            "Voulez-vous vraiment annuler la commande #" + numCommande + " ?\n\n" +
                            "Cette action va:\n" +
                            "• Supprimer toutes les lignes de la commande\n" +
                            "• Mettre le statut à 'Annulée'\n" +
                            "• Mettre le total à 0\n\n" +
                            "Cette action est IRRÉVERSIBLE!",
                    "Confirmer l'annulation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }

            // Annuler la commande
            gestionCommande.annulerCommande(numCommande);

            // Afficher le bilan après annulation
            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(numCommande);
            Commande commande = bilan.getCommande();

            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════\n");
            sb.append("       COMMANDE ANNULÉE\n");
            sb.append("═══════════════════════════════════════\n\n");
            sb.append("Numéro: #").append(commande.getNumCommande()).append("\n");
            sb.append("Statut: ").append(commande.getStatut()).append("\n");
            sb.append("Total: ").append(commande.getMontantTotalCommande()).append(" DT").append("\n");
            sb.append("Nombre de lignes: ").append(bilan.getNombreLignes()).append("\n");
            sb.append("\n✅ La commande a été annulée avec succès.\n");
            sb.append("═══════════════════════════════════════\n");

            JOptionPane.showMessageDialog(this, sb.toString(),
                    "Commande Annulée", JOptionPane.INFORMATION_MESSAGE);

            txtInfoCommande.setText(sb.toString());

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez d'abord rechercher une commande valide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'annulation: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
