package interfaces.employe;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import entite.Employe;
import entite.CvEmployee;
import gestion.GestionEmploye;

public class AjouterEmployeFrame extends JFrame {
    // Infos personnelles
    private JTextField txtNumCarte, txtNom, txtPrenom, txtAge;
    private JTextField txtAdresse, txtEmail, txtTelephone;

    // Infos employé
    private JTextField txtCNSS, txtSalaire, txtMotDePasse;
    private JTextField txtDateRecrutement, txtHeureDebut, txtHeureFin;
    private JSpinner spnJoursParSemaine;
    private JComboBox<String> cmbPoste;

    // CV
    private JTextField txtDiplome, txtExperience;
    private JTextArea txtFormation, txtStage;
    private JCheckBox chkAjouterCV;

    private JButton btnSave, btnCancel;
    private GestionEmploye gestionEmploye;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public AjouterEmployeFrame() {
        gestionEmploye = new GestionEmploye();
        initComponents();
    }

    private void initComponents() {
        setTitle("Ajouter un Employé");
        setSize(900, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(34, 139, 34));
        topPanel.setPreferredSize(new Dimension(900, 50));

        JLabel titleLabel = new JLabel("➕ Ajouter un nouvel employé");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal avec scroll
        JScrollPane scrollPane = new JScrollPane(createFormPanel());
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnSave = new JButton("💾 Enregistrer");
        btnSave.setPreferredSize(new Dimension(150, 35));
        btnSave.setBackground(new Color(34, 139, 34));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveEmploye());

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

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;

        // Section: Informations personnelles
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel lblInfoPerso = new JLabel("👤 Informations personnelles");
        lblInfoPerso.setFont(new Font("Arial", Font.BOLD, 14));
        lblInfoPerso.setForeground(new Color(34, 139, 34));
        panel.add(lblInfoPerso, gbc);
        gbc.gridwidth = 1;

        // Numéro carte identité
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("N° Carte Identité *:"), gbc);
        gbc.gridx = 1;
        txtNumCarte = new JTextField(25);
        panel.add(txtNumCarte, gbc);

        // Nom
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Nom *:"), gbc);
        gbc.gridx = 1;
        txtNom = new JTextField(25);
        panel.add(txtNom, gbc);

        // Prénom
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Prénom *:"), gbc);
        gbc.gridx = 1;
        txtPrenom = new JTextField(25);
        panel.add(txtPrenom, gbc);

        // Âge
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Âge *:"), gbc);
        gbc.gridx = 1;
        txtAge = new JTextField(25);
        panel.add(txtAge, gbc);

        // Adresse
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Adresse:"), gbc);
        gbc.gridx = 1;
        txtAdresse = new JTextField(25);
        panel.add(txtAdresse, gbc);

        // Email
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(25);
        panel.add(txtEmail, gbc);

        // Téléphone
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Téléphone:"), gbc);
        gbc.gridx = 1;
        txtTelephone = new JTextField(25);
        panel.add(txtTelephone, gbc);

        // Séparateur
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 15, 5);
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Section: Informations professionnelles
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel lblInfoPro = new JLabel("💼 Informations professionnelles");
        lblInfoPro.setFont(new Font("Arial", Font.BOLD, 14));
        lblInfoPro.setForeground(new Color(34, 139, 34));
        panel.add(lblInfoPro, gbc);
        gbc.gridwidth = 1;

        // CNSS
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("N° CNSS *:"), gbc);
        gbc.gridx = 1;
        txtCNSS = new JTextField(25);
        panel.add(txtCNSS, gbc);

        // Poste
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Poste *:"), gbc);
        gbc.gridx = 1;
        cmbPoste = new JComboBox<>(new String[]{
                "Pharmacien", "Assistant", "Administrateur", "Caissier", "Préparateur"
        });
        panel.add(cmbPoste, gbc);

        // Salaire
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Salaire (DT) *:"), gbc);
        gbc.gridx = 1;
        txtSalaire = new JTextField(25);
        panel.add(txtSalaire, gbc);

        // Mot de passe
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Mot de passe *:"), gbc);
        gbc.gridx = 1;
        txtMotDePasse = new JPasswordField(25);
        panel.add(txtMotDePasse, gbc);

        // Date recrutement
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Date recrutement (jj/mm/aaaa) *:"), gbc);
        gbc.gridx = 1;
        txtDateRecrutement = new JTextField(25);
        txtDateRecrutement.setText(dateFormat.format(new Date()));
        panel.add(txtDateRecrutement, gbc);

        // Heure début
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Heure début (HH:MM):"), gbc);
        gbc.gridx = 1;
        txtHeureDebut = new JTextField(25);
        txtHeureDebut.setText("08:00");
        panel.add(txtHeureDebut, gbc);

        // Heure fin
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Heure fin (HH:MM):"), gbc);
        gbc.gridx = 1;
        txtHeureFin = new JTextField(25);
        txtHeureFin.setText("17:00");
        panel.add(txtHeureFin, gbc);

        // Jours par semaine
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Jours par semaine *:"), gbc);
        gbc.gridx = 1;
        spnJoursParSemaine = new JSpinner(new SpinnerNumberModel(5, 1, 7, 1));
        panel.add(spnJoursParSemaine, gbc);

        // Séparateur
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 15, 5);
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Section: CV (optionnel)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        chkAjouterCV = new JCheckBox("📄 Ajouter un CV (optionnel)");
        chkAjouterCV.setFont(new Font("Arial", Font.BOLD, 14));
        chkAjouterCV.setForeground(new Color(34, 139, 34));
        chkAjouterCV.addActionListener(e -> toggleCVFields());
        panel.add(chkAjouterCV, gbc);
        gbc.gridwidth = 1;

        // Diplôme
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Diplôme:"), gbc);
        gbc.gridx = 1;
        txtDiplome = new JTextField(25);
        txtDiplome.setEnabled(false);
        panel.add(txtDiplome, gbc);

        // Expérience
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Années d'expérience:"), gbc);
        gbc.gridx = 1;
        txtExperience = new JTextField(25);
        txtExperience.setText("0");
        txtExperience.setEnabled(false);
        panel.add(txtExperience, gbc);

        // Formation
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Formation:"), gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        txtFormation = new JTextArea(3, 25);
        txtFormation.setLineWrap(true);
        txtFormation.setWrapStyleWord(true);
        txtFormation.setEnabled(false);
        JScrollPane scrollFormation = new JScrollPane(txtFormation);
        panel.add(scrollFormation, gbc);
        gbc.gridheight = 1;

        // Stage
        row += 2;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Stage:"), gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        txtStage = new JTextArea(3, 25);
        txtStage.setLineWrap(true);
        txtStage.setWrapStyleWord(true);
        txtStage.setEnabled(false);
        JScrollPane scrollStage = new JScrollPane(txtStage);
        panel.add(scrollStage, gbc);
        gbc.gridheight = 1;

        // Note
        row += 2;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel lblNote = new JLabel("* Champs obligatoires");
        lblNote.setFont(new Font("Arial", Font.ITALIC, 11));
        lblNote.setForeground(Color.GRAY);
        panel.add(lblNote, gbc);

        return panel;
    }

    private void toggleCVFields() {
        boolean enabled = chkAjouterCV.isSelected();
        txtDiplome.setEnabled(enabled);
        txtExperience.setEnabled(enabled);
        txtFormation.setEnabled(enabled);
        txtStage.setEnabled(enabled);
    }

    private void saveEmploye() {
        // Validation des champs obligatoires
        if (!validateFields()) {
            return;
        }

        try {
            // Créer l'objet Employe
            Employe employe = new Employe();

            // Infos personnelles
            employe.setNumCarteIdentite(Integer.parseInt(txtNumCarte.getText().trim()));
            employe.setNom(txtNom.getText().trim());
            employe.setPrenom(txtPrenom.getText().trim());
            employe.setAge(Integer.parseInt(txtAge.getText().trim()));
            employe.setAdresse(txtAdresse.getText().trim());
            employe.setAdresseMail(txtEmail.getText().trim());
            employe.setTelephone(txtTelephone.getText().trim());

            // Infos professionnelles
            employe.setNumCNSS(Integer.parseInt(txtCNSS.getText().trim()));
            employe.setPoste((String) cmbPoste.getSelectedItem());
            employe.setSalaire(Double.parseDouble(txtSalaire.getText().trim()));
            employe.setMotDePasse(txtMotDePasse.getText().trim());
            employe.setDateRejoindTravail(dateFormat.parse(txtDateRecrutement.getText().trim()));
            employe.setHeureDebutTravail(txtHeureDebut.getText().trim());
            employe.setHeureSortie(txtHeureFin.getText().trim());
            employe.setNbJoursParSemaine((Integer) spnJoursParSemaine.getValue());

            // CV (optionnel)
            CvEmployee cv = null;
            if (chkAjouterCV.isSelected()) {
                cv = new CvEmployee();
                cv.setDiplome(txtDiplome.getText().trim());
                cv.setNbAnneeExperience(Integer.parseInt(txtExperience.getText().trim()));
                cv.setFormation(txtFormation.getText().trim());
                cv.setStage(txtStage.getText().trim());
            }

            // Sauvegarder
            int numCarteEmp = gestionEmploye.ajouterEmployeComplet(employe, cv);

            JOptionPane.showMessageDialog(this,
                    "✅ Employé ajouté avec succès!\n\n" +
                            "Carte Employé: " + numCarteEmp + "\n" +
                            "Nom: " + employe.getPrenom() + " " + employe.getNom() + "\n" +
                            "Poste: " + employe.getPoste() + "\n" +
                            "CNSS: " + employe.getNumCNSS(),
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

            clearForm();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de format dans les champs numériques!",
                    "Erreur de saisie",
                    JOptionPane.ERROR_MESSAGE);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Format de date invalide! Utilisez: jj/mm/aaaa",
                    "Erreur de date",
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

    private boolean validateFields() {
        if (txtNumCarte.getText().trim().isEmpty() ||
                txtNom.getText().trim().isEmpty() ||
                txtPrenom.getText().trim().isEmpty() ||
                txtAge.getText().trim().isEmpty() ||
                txtCNSS.getText().trim().isEmpty() ||
                txtSalaire.getText().trim().isEmpty() ||
                txtMotDePasse.getText().trim().isEmpty() ||
                txtDateRecrutement.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Veuillez remplir tous les champs obligatoires (*) !",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearForm() {
        txtNumCarte.setText("");
        txtNom.setText("");
        txtPrenom.setText("");
        txtAge.setText("");
        txtAdresse.setText("");
        txtEmail.setText("");
        txtTelephone.setText("");
        txtCNSS.setText("");
        txtSalaire.setText("");
        txtMotDePasse.setText("");
        txtDateRecrutement.setText(dateFormat.format(new Date()));
        txtHeureDebut.setText("08:00");
        txtHeureFin.setText("17:00");
        spnJoursParSemaine.setValue(5);
        cmbPoste.setSelectedIndex(0);
        chkAjouterCV.setSelected(false);
        txtDiplome.setText("");
        txtExperience.setText("0");
        txtFormation.setText("");
        txtStage.setText("");
        toggleCVFields();
    }
}
