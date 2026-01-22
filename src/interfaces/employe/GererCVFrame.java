package interfaces.employe;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.CvEmployee;
import entite.Employe;
import entitebd.EmployeBD;
import gestion.GestionEmploye;

public class GererCVFrame extends JFrame {
    private int numCarteEmp;
    private CvEmployee cv;

    private JTextField txtDiplome, txtExperience;
    private JTextArea txtFormation, txtStage;
    private JLabel lblEmploye;
    private JButton btnSave, btnCancel;

    private GestionEmploye gestionEmploye;
    private EmployeBD employeBD;

    public GererCVFrame(int numCarteEmp) {
        this.numCarteEmp = numCarteEmp;
        this.gestionEmploye = new GestionEmploye();
        this.employeBD = new EmployeBD();
        initComponents();
        loadCV();
    }

    private void initComponents() {
        setTitle("Gérer le CV");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 123, 255));
        topPanel.setPreferredSize(new Dimension(600, 70));

        JLabel titleLabel = new JLabel("📄 Gestion du CV");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        lblEmploye = new JLabel("");
        lblEmploye.setFont(new Font("Arial", Font.PLAIN, 14));
        lblEmploye.setForeground(Color.WHITE);
        lblEmploye.setHorizontalAlignment(SwingConstants.CENTER);
        lblEmploye.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        topPanel.add(lblEmploye, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Panel formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Diplôme
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Diplôme:"), gbc);
        gbc.gridx = 1;
        txtDiplome = new JTextField(25);
        formPanel.add(txtDiplome, gbc);

        // Années d'expérience
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Années d'expérience:"), gbc);
        gbc.gridx = 1;
        txtExperience = new JTextField(25);
        formPanel.add(txtExperience, gbc);

        // Formation
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Formation:"), gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        txtFormation = new JTextArea(4, 25);
        txtFormation.setLineWrap(true);
        txtFormation.setWrapStyleWord(true);
        JScrollPane scrollFormation = new JScrollPane(txtFormation);
        formPanel.add(scrollFormation, gbc);
        gbc.gridheight = 1;

        // Stage
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Stage(s):"), gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        txtStage = new JTextArea(4, 25);
        txtStage.setLineWrap(true);
        txtStage.setWrapStyleWord(true);
        JScrollPane scrollStage = new JScrollPane(txtStage);
        formPanel.add(scrollStage, gbc);
        gbc.gridheight = 1;

        add(formPanel, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnSave = new JButton("💾 Enregistrer");
        btnSave.setPreferredSize(new Dimension(150, 35));
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveCV());

        btnCancel = new JButton("❌ Fermer");
        btnCancel.setPreferredSize(new Dimension(150, 35));
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadCV() {
        try {
            // Charger les infos de l'employé
            Employe employe = employeBD.rechercherParId(numCarteEmp);
            if (employe != null) {
                lblEmploye.setText("Employé: " + employe.getPrenom() + " " + employe.getNom() +
                        " (Carte #" + numCarteEmp + ")");
            }

            // Charger le CV
            cv = gestionEmploye.obtenirCV(numCarteEmp);

            if (cv != null) {
                txtDiplome.setText(cv.getDiplome());
                txtExperience.setText(String.valueOf(cv.getNbAnneeExperience()));
                txtFormation.setText(cv.getFormation());
                txtStage.setText(cv.getStage());
            } else {
                // Créer un nouveau CV
                cv = new CvEmployee();
                cv.setNumEmployee(numCarteEmp);
                txtExperience.setText("0");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveCV() {
        try {
            // Valider et parser l'expérience
            int experience;
            try {
                experience = Integer.parseInt(txtExperience.getText().trim());
                if (experience < 0) {
                    JOptionPane.showMessageDialog(this,
                            "L'expérience ne peut pas être négative!",
                            "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "L'expérience doit être un nombre entier!",
                        "Erreur de saisie",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Mettre à jour l'objet CV
            cv.setDiplome(txtDiplome.getText().trim());
            cv.setNbAnneeExperience(experience);
            cv.setFormation(txtFormation.getText().trim());
            cv.setStage(txtStage.getText().trim());

            // Sauvegarder
            boolean saved = gestionEmploye.ajouterOuModifierCV(numCarteEmp, cv);

            if (saved) {
                JOptionPane.showMessageDialog(this,
                        "✅ CV enregistré avec succès!",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de base de données:\n" + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
