package interfaces.employe;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import entite.Employe;
import entitebd.EmployeBD;
import gestion.GestionEmploye;

public class ModifierEmployeFrame extends JFrame {
    private int numCarteEmp;
    private Employe employe;

    // Champs modifiables
    private JTextField txtNom, txtPrenom, txtAdresse, txtEmail, txtTelephone;
    private JTextField txtSalaire, txtHeureDebut, txtHeureFin;
    private JSpinner spnJoursParSemaine;
    private JComboBox<String> cmbPoste;
    private JPasswordField txtMotDePasse;

    // Champs non modifiables (affichage uniquement)
    private JTextField txtNumCarte, txtCNSS, txtDateRecrutement, txtAge;

    private JButton btnSave, btnCancel, btnCV;
    private GestionEmploye gestionEmploye;
    private EmployeBD employeBD;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public ModifierEmployeFrame(int numCarteEmp) {
        this.numCarteEmp = numCarteEmp;
        this.gestionEmploye = new GestionEmploye();
        this.employeBD = new EmployeBD();
        initComponents();
        loadEmploye();
    }

    private void initComponents() {
        setTitle("Modifier un Employé");
        setSize(700, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(255, 165, 0));
        topPanel.setPreferredSize(new Dimension(700, 50));

        JLabel titleLabel = new JLabel("✏️ Modifier les informations de l'employé");
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
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveEmploye());

        btnCV = new JButton("📄 Gérer CV");
        btnCV.setPreferredSize(new Dimension(150, 35));
        btnCV.setBackground(new Color(0, 123, 255));
        btnCV.setForeground(Color.WHITE);
        btnCV.setFocusPainted(false);
        btnCV.addActionListener(e -> gererCV());

        btnCancel = new JButton("❌ Annuler");
        btnCancel.setPreferredSize(new Dimension(150, 35));
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCV);
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

        // Section: Informations non modifiables
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel lblInfoFixe = new JLabel("📋 Informations fixes (non modifiables)");
        lblInfoFixe.setFont(new Font("Arial", Font.BOLD, 14));
        lblInfoFixe.setForeground(new Color(108, 117, 125));
        panel.add(lblInfoFixe, gbc);
        gbc.gridwidth = 1;

        // N° Carte Employé
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("N° Carte Employé:"), gbc);
        gbc.gridx = 1;
        txtNumCarte = new JTextField(20);
        txtNumCarte.setEditable(false);
        txtNumCarte.setBackground(new Color(240, 240, 240));
        panel.add(txtNumCarte, gbc);

        // CNSS
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("N° CNSS:"), gbc);
        gbc.gridx = 1;
        txtCNSS = new JTextField(20);
        txtCNSS.setEditable(false);
        txtCNSS.setBackground(new Color(240, 240, 240));
        panel.add(txtCNSS, gbc);

        // Date recrutement
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Date recrutement:"), gbc);
        gbc.gridx = 1;
        txtDateRecrutement = new JTextField(20);
        txtDateRecrutement.setEditable(false);
        txtDateRecrutement.setBackground(new Color(240, 240, 240));
        panel.add(txtDateRecrutement, gbc);

        // Séparateur
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 15, 5);
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Section: Informations modifiables
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel lblInfoMod = new JLabel("✏️ Informations modifiables");
        lblInfoMod.setFont(new Font("Arial", Font.BOLD, 14));
        lblInfoMod.setForeground(new Color(255, 165, 0));
        panel.add(lblInfoMod, gbc);
        gbc.gridwidth = 1;

        // Nom
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1;
        txtNom = new JTextField(20);
        panel.add(txtNom, gbc);

        // Prénom
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Prénom:"), gbc);
        gbc.gridx = 1;
        txtPrenom = new JTextField(20);
        panel.add(txtPrenom, gbc);

        // Âge
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Âge:"), gbc);
        gbc.gridx = 1;
        txtAge = new JTextField(20);
        panel.add(txtAge, gbc);

        // Adresse
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Adresse:"), gbc);
        gbc.gridx = 1;
        txtAdresse = new JTextField(20);
        panel.add(txtAdresse, gbc);

        // Email
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        panel.add(txtEmail, gbc);

        // Téléphone
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Téléphone:"), gbc);
        gbc.gridx = 1;
        txtTelephone = new JTextField(20);
        panel.add(txtTelephone, gbc);

        // Poste
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Poste:"), gbc);
        gbc.gridx = 1;
        cmbPoste = new JComboBox<>(new String[]{
                "Pharmacien", "Assistant", "Administrateur", "Caissier", "Préparateur"
        });
        panel.add(cmbPoste, gbc);

        // Salaire
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Salaire (DT):"), gbc);
        gbc.gridx = 1;
        txtSalaire = new JTextField(20);
        panel.add(txtSalaire, gbc);

        // Mot de passe
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Nouveau mot de passe:"), gbc);
        gbc.gridx = 1;
        txtMotDePasse = new JPasswordField(20);
        txtMotDePasse.setToolTipText("Laisser vide pour ne pas changer");
        panel.add(txtMotDePasse, gbc);

        // Heure début
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Heure début:"), gbc);
        gbc.gridx = 1;
        txtHeureDebut = new JTextField(20);
        panel.add(txtHeureDebut, gbc);

        // Heure fin
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Heure fin:"), gbc);
        gbc.gridx = 1;
        txtHeureFin = new JTextField(20);
        panel.add(txtHeureFin, gbc);

        // Jours par semaine
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Jours par semaine:"), gbc);
        gbc.gridx = 1;
        spnJoursParSemaine = new JSpinner(new SpinnerNumberModel(5, 1, 7, 1));
        panel.add(spnJoursParSemaine, gbc);

        return panel;
    }

    private void loadEmploye() {
        try {
            employe = employeBD.rechercherParId(numCarteEmp);

            if (employe == null) {
                JOptionPane.showMessageDialog(this,
                        "Employé introuvable!",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            // Champs non modifiables
            txtNumCarte.setText(String.valueOf(employe.getNumCarteEmp()));
            txtCNSS.setText(String.valueOf(employe.getNumCNSS()));
            txtDateRecrutement.setText(dateFormat.format(employe.getDateRejoindTravail()));

            // Champs modifiables
            txtNom.setText(employe.getNom());
            txtPrenom.setText(employe.getPrenom());
            txtAge.setText(String.valueOf(employe.getAge()));
            txtAdresse.setText(employe.getAdresse());
            txtEmail.setText(employe.getAdresseMail());
            txtTelephone.setText(employe.getTelephone());
            cmbPoste.setSelectedItem(employe.getPoste());
            txtSalaire.setText(String.valueOf(employe.getSalaire()));
            txtHeureDebut.setText(employe.getHeureDebutTravail());
            txtHeureFin.setText(employe.getHeureSortie());
            spnJoursParSemaine.setValue(employe.getNbJoursParSemaine());

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void saveEmploye() {
        try {
            // Mettre à jour l'objet employé
            employe.setNom(txtNom.getText().trim());
            employe.setPrenom(txtPrenom.getText().trim());
            employe.setAge(Integer.parseInt(txtAge.getText().trim()));
            employe.setAdresse(txtAdresse.getText().trim());
            employe.setAdresseMail(txtEmail.getText().trim());
            employe.setTelephone(txtTelephone.getText().trim());
            employe.setPoste((String) cmbPoste.getSelectedItem());
            employe.setSalaire(Double.parseDouble(txtSalaire.getText().trim()));
            employe.setHeureDebutTravail(txtHeureDebut.getText().trim());
            employe.setHeureSortie(txtHeureFin.getText().trim());
            employe.setNbJoursParSemaine((Integer) spnJoursParSemaine.getValue());

            // Mettre à jour le mot de passe si fourni
            String newPassword = new String(txtMotDePasse.getPassword());
            if (!newPassword.trim().isEmpty()) {
                employe.setMotDePasse(newPassword.trim());
            }

            // Sauvegarder
            boolean updated = gestionEmploye.modifierEmploye(employe);

            if (updated) {
                JOptionPane.showMessageDialog(this,
                        "✅ Employé modifié avec succès!",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de format dans les champs numériques!",
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

    private void gererCV() {
        GererCVFrame frame = new GererCVFrame(numCarteEmp);
        frame.setVisible(true);
    }
}
