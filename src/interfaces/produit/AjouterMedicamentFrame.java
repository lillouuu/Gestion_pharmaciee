package interfaces.produit;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.Medicament;
import gestion.GestionProduit;

public class AjouterMedicamentFrame extends JFrame {
    // ✅ REFACTORED: Suppression complète des références au fournisseur
    private JTextField txtNom;
    private JTextArea txtDescription;
    private JButton btnSave, btnCancel;

    private GestionProduit gestionProduit;

    public AjouterMedicamentFrame() {
        gestionProduit = new GestionProduit();
        initComponents();
    }

    private void initComponents() {
        setTitle("Ajouter un Médicament");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(236, 72, 153));
        topPanel.setPreferredSize(new Dimension(600, 50));

        JLabel titleLabel = new JLabel("➕ Ajouter un nouveau médicament");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel formulaire avec scroll
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Section: Informations du médicament
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblInfoMed = new JLabel("📋 Informations du médicament");
        lblInfoMed.setFont(new Font("Arial", Font.BOLD, 14));
        lblInfoMed.setForeground(new Color(236, 72, 153));
        formPanel.add(lblInfoMed, gbc);
        gbc.gridwidth = 1;

        // Nom
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nom *:"), gbc);
        gbc.gridx = 1;
        txtNom = new JTextField(25);
        formPanel.add(txtNom, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        txtDescription = new JTextArea(5, 25);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        formPanel.add(scrollDesc, gbc);
        gbc.gridheight = 1;

        // Note importante
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;


        // Note champs obligatoires
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JLabel lblNote = new JLabel("* Champs obligatoires");
        lblNote.setFont(new Font("Arial", Font.ITALIC, 11));
        lblNote.setForeground(Color.GRAY);
        formPanel.add(lblNote, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnSave = new JButton("💾 Enregistrer");
        btnSave.setPreferredSize(new Dimension(150, 35));
        btnSave.setBackground(new Color(34, 139, 34));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveMedicament());

        btnCancel = new JButton("❌ Annuler");
        btnCancel.setPreferredSize(new Dimension(150, 35));
        btnCancel.setBackground(new Color(79, 70, 229));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void saveMedicament() {
        // ✅ REFACTORED: Validation simplifiée - seulement nom
        if (txtNom.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez remplir le nom du médicament!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // ✅ REFACTORED: Créer uniquement le médicament (sans fournisseur, sans stock, sans dates)
            Medicament med = new Medicament();
            med.setNom(txtNom.getText().trim());
            med.setDescriptio(txtDescription.getText().trim());

            // ✅ Utiliser la nouvelle méthode qui ne crée PAS de stock
            int refMedicament = gestionProduit.ajouterMedicament(med);

            JOptionPane.showMessageDialog(this,
                    "✅ Médicament ajouté avec succès!\n\n" +
                            "Référence: " + refMedicament + "\n" +
                            "Nom: " + txtNom.getText() + "\n\n" +
                            "ℹ️ Les stocks seront créés lors de la réception des commandes.",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Validation échouée:\n" + ex.getMessage(),
                    "Erreur de validation",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de base de données:\n" + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtNom.setText("");
        txtDescription.setText("");
        txtNom.requestFocus();
    }
}