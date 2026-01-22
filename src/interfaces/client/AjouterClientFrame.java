package interfaces.client;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.Client;
import gestion.GestionClient;

public class AjouterClientFrame extends JFrame {
    private JTextField txtNumCarte, txtNom, txtPrenom, txtAge, txtAdresse, txtEmail, txtTelephone, txtCodeCnam;
    private JButton btnSave, btnCancel;
    private GestionClient gestionClient;

    public AjouterClientFrame() {
        gestionClient = new GestionClient();
        initComponents();
    }

    private void initComponents() {
        setTitle("Ajouter un Client");
        setSize(600, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(34, 139, 34));
        topPanel.setPreferredSize(new Dimension(600, 50));

        JLabel titleLabel = new JLabel("➕ Ajouter un nouveau client");
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

        // Section: Informations personnelles
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblInfo = new JLabel("📋 Informations personnelles");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 14));
        lblInfo.setForeground(new Color(34, 139, 34));
        formPanel.add(lblInfo, gbc);
        gbc.gridwidth = 1;

        // Numéro carte identité
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("N° Carte Identité *:"), gbc);
        gbc.gridx = 1;
        txtNumCarte = new JTextField(20);
        formPanel.add(txtNumCarte, gbc);

        // Nom
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Nom *:"), gbc);
        gbc.gridx = 1;
        txtNom = new JTextField(20);
        formPanel.add(txtNom, gbc);

        // Prénom
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Prénom *:"), gbc);
        gbc.gridx = 1;
        txtPrenom = new JTextField(20);
        formPanel.add(txtPrenom, gbc);

        // Âge
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Âge *:"), gbc);
        gbc.gridx = 1;
        txtAge = new JTextField(20);
        formPanel.add(txtAge, gbc);

        // Téléphone
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Téléphone *:"), gbc);
        gbc.gridx = 1;
        txtTelephone = new JTextField(20);
        formPanel.add(txtTelephone, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        formPanel.add(txtEmail, gbc);

        // Adresse
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Adresse:"), gbc);
        gbc.gridx = 1;
        txtAdresse = new JTextField(20);
        formPanel.add(txtAdresse, gbc);

        // Section: Informations client
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        JLabel lblClient = new JLabel("⭐ Informations fidélité");
        lblClient.setFont(new Font("Arial", Font.BOLD, 14));
        lblClient.setForeground(new Color(34, 139, 34));
        formPanel.add(lblClient, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Code CNAM
        gbc.gridx = 0; gbc.gridy = 9;
        formPanel.add(new JLabel("Code CNAM:"), gbc);
        gbc.gridx = 1;
        txtCodeCnam = new JTextField(20);
        formPanel.add(txtCodeCnam, gbc);

        // Note
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
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
        btnSave.addActionListener(e -> saveClient());

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

    private void saveClient() {
        // Validation
        if (txtNumCarte.getText().trim().isEmpty() || txtNom.getText().trim().isEmpty() ||
                txtPrenom.getText().trim().isEmpty() || txtAge.getText().trim().isEmpty() ||
                txtTelephone.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Veuillez remplir tous les champs obligatoires (*) !",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Créer le client
            Client client = new Client();
            client.setNumCarteIdentite(Integer.parseInt(txtNumCarte.getText().trim()));
            client.setNom(txtNom.getText().trim());
            client.setPrenom(txtPrenom.getText().trim());
            client.setAge(Integer.parseInt(txtAge.getText().trim()));
            client.setTelephone(txtTelephone.getText().trim());
            client.setAdresseMail(txtEmail.getText().trim());
            client.setAdresse(txtAdresse.getText().trim());
            client.setCodeCnam(txtCodeCnam.getText().trim());
            client.setPointFidelite(0); // Nouveau client = 0 points

            // Ajouter
            int numClient = gestionClient.ajouterClient(client);

            JOptionPane.showMessageDialog(this,
                    "✅ Client ajouté avec succès!\n\n" +
                            "N° Client: " + numClient + "\n" +
                            "Nom: " + client.getPrenom() + " " + client.getNom() + "\n" +
                            "Points de fidélité: 0",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

            clearForm();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Le numéro de carte et l'âge doivent être des nombres valides!",
                    "Erreur de saisie",
                    JOptionPane.ERROR_MESSAGE);
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
        txtNumCarte.setText("");
        txtNom.setText("");
        txtPrenom.setText("");
        txtAge.setText("");
        txtTelephone.setText("");
        txtEmail.setText("");
        txtAdresse.setText("");
        txtCodeCnam.setText("");
        txtNumCarte.requestFocus();
    }
}
