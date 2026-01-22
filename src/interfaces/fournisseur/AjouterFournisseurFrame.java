package interfaces.fournisseur;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.Fournisseur;
import entitebd.FournisseurBD;

public class AjouterFournisseurFrame extends JFrame {
    private JTextField txtNumFournisseur, txtNom, txtAdresse, txtTelephone, txtEmail;
    private JSpinner spnRate;
    private JButton btnSave, btnCancel;
    private FournisseurBD fournisseurBD;

    public AjouterFournisseurFrame() {
        fournisseurBD = new FournisseurBD();
        initComponents();
    }

    private void initComponents() {
        setTitle("Ajouter un Fournisseur");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(34, 139, 34));
        topPanel.setPreferredSize(new Dimension(600, 50));

        JLabel titleLabel = new JLabel("➕ Ajouter un nouveau fournisseur");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Numéro fournisseur
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Numéro Fournisseur:"), gbc);
        gbc.gridx = 1;
        txtNumFournisseur = new JTextField(20);
        txtNumFournisseur.setToolTipText("Laissez vide pour génération automatique");
        JPanel numPanel = new JPanel(new BorderLayout());
        numPanel.add(txtNumFournisseur, BorderLayout.CENTER);
        JLabel lblAuto = new JLabel("(Auto)");
        lblAuto.setFont(new Font("Arial", Font.ITALIC, 10));
        lblAuto.setForeground(Color.GRAY);
        numPanel.add(lblAuto, BorderLayout.EAST);
        formPanel.add(numPanel, gbc);

        // Nom
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nom *:"), gbc);
        gbc.gridx = 1;
        txtNom = new JTextField(20);
        formPanel.add(txtNom, gbc);

        // Adresse
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Adresse *:"), gbc);
        gbc.gridx = 1;
        txtAdresse = new JTextField(20);
        formPanel.add(txtAdresse, gbc);

        // Téléphone
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Téléphone *:"), gbc);
        gbc.gridx = 1;
        txtTelephone = new JTextField(20);
        formPanel.add(txtTelephone, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Email *:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        formPanel.add(txtEmail, gbc);

        // Rate (évaluation)
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Évaluation (0-5) *:"), gbc);
        gbc.gridx = 1;
        SpinnerNumberModel rateModel = new SpinnerNumberModel(3.0, 0.0, 5.0, 0.5);
        spnRate = new JSpinner(rateModel);
        formPanel.add(spnRate, gbc);

        // Note
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JLabel lblNote = new JLabel("* Champs obligatoires");
        lblNote.setFont(new Font("Arial", Font.ITALIC, 11));
        lblNote.setForeground(Color.GRAY);
        formPanel.add(lblNote, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnSave = new JButton("💾 Enregistrer");
        btnSave.setPreferredSize(new Dimension(150, 35));
        btnSave.setBackground(new Color(34, 139, 34));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveFournisseur());

        btnCancel = new JButton("❌ Annuler");
        btnCancel.setPreferredSize(new Dimension(150, 35));
        btnCancel.setBackground(new Color(220, 53, 69));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void saveFournisseur() {
        // Validation
        if (txtNom.getText().trim().isEmpty() ||
                txtAdresse.getText().trim().isEmpty() ||
                txtTelephone.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Veuillez remplir tous les champs obligatoires!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // ✅ Générer automatiquement un numéro de fournisseur si le champ est vide
            int numFournisseur = 0; // 0 = auto-généré par FournisseurBD

            if (!txtNumFournisseur.getText().trim().isEmpty()) {
                try {
                    numFournisseur = Integer.parseInt(txtNumFournisseur.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Le numéro fournisseur doit être un nombre entier!\nLaissez vide pour génération automatique.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            System.out.println("🔍 DEBUG: Création fournisseur avec num = " + numFournisseur);

            Fournisseur f = new Fournisseur();
            f.setNumFournisseur(numFournisseur); // 0 ou le numéro saisi
            f.setNomFournisseur(txtNom.getText().trim());
            f.setAdresse(txtAdresse.getText().trim());
            f.setTelephone(txtTelephone.getText().trim());
            f.setAdresseEmail(txtEmail.getText().trim());
            f.setRate((Double) spnRate.getValue());

            int result = fournisseurBD.ajouter(f);

            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                        "✅ Fournisseur ajouté avec succès!\n\n" +
                                "Numéro: " + result + "\n" +
                                "Nom: " + txtNom.getText() + "\n" +
                                "Évaluation: " + spnRate.getValue() + "/5",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);

                clearForm();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Erreur lors de l'ajout du fournisseur!\n\nVérifiez les logs pour plus de détails.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Erreur de base de données:\n\n" + ex.getMessage() +
                            "\n\n💡 Conseil: Vérifiez que le numéro fournisseur n'existe pas déjà.",
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void clearForm() {
        txtNumFournisseur.setText("");
        txtNom.setText("");
        txtAdresse.setText("");
        txtTelephone.setText("");
        txtEmail.setText("");
        spnRate.setValue(3.0);
        txtNumFournisseur.requestFocus();
    }
}