package interfaces.fournisseur;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.Fournisseur;
import entitebd.FournisseurBD;

public class ModifierFournisseurFrame extends JFrame {
    private JTable tableFournisseurs;
    private DefaultTableModel tableModel;
    private JTextField txtNum, txtNom, txtAdresse, txtTelephone, txtEmail;
    private JSpinner spnRate;
    private JButton btnSearch, btnUpdate, btnCancel, btnRefresh;
    private FournisseurBD fournisseurBD;
    private int currentNumFournisseur = -1;

    public ModifierFournisseurFrame() {
        fournisseurBD = new FournisseurBD();
        initComponents();
        loadFournisseurs();
    }

    private void initComponents() {
        setTitle("Modifier un Fournisseur");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(236, 72, 153));
        topPanel.setPreferredSize(new Dimension(1000, 50));

        JLabel titleLabel = new JLabel("✏️ Modifier un fournisseur");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Panel gauche - Liste
        JPanel leftPanel = createListPanel();
        splitPane.setLeftComponent(leftPanel);

        // Panel droit - Formulaire
        JPanel rightPanel = createFormPanel();
        splitPane.setRightComponent(rightPanel);

        splitPane.setDividerLocation(500);
        add(splitPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = createButtonPanel();
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // Panel recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadFournisseurs());
        searchPanel.add(btnRefresh);

        panel.add(searchPanel, BorderLayout.NORTH);

        // Tableau
        String[] columns = {"N°", "Nom", "Téléphone", "Email", "Évaluation"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableFournisseurs = new JTable(tableModel);
        tableFournisseurs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableFournisseurs.setRowHeight(25);
        tableFournisseurs.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableFournisseurs.getSelectedRow() != -1) {
                loadSelectedFournisseur();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableFournisseurs);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Numéro (non modifiable)
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Numéro:"), gbc);
        gbc.gridx = 1;
        txtNum = new JTextField(15);
        txtNum.setEditable(false);
        txtNum.setBackground(new Color(240, 240, 240));
        panel.add(txtNum, gbc);

        // Nom
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nom *:"), gbc);
        gbc.gridx = 1;
        txtNom = new JTextField(15);
        panel.add(txtNom, gbc);

        // Adresse
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Adresse *:"), gbc);
        gbc.gridx = 1;
        txtAdresse = new JTextField(15);
        panel.add(txtAdresse, gbc);

        // Téléphone
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Téléphone *:"), gbc);
        gbc.gridx = 1;
        txtTelephone = new JTextField(15);
        panel.add(txtTelephone, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Email *:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(15);
        panel.add(txtEmail, gbc);

        // Rate
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Évaluation *:"), gbc);
        gbc.gridx = 1;
        SpinnerNumberModel rateModel = new SpinnerNumberModel(3.0, 0.0, 5.0, 0.5);
        spnRate = new JSpinner(rateModel);
        panel.add(spnRate, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnUpdate = new JButton("💾 Modifier");
        btnUpdate.setPreferredSize(new Dimension(120, 35));
        btnUpdate.setBackground(new Color(236, 72, 153));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> updateFournisseur());

        btnCancel = new JButton("❌ Fermer");
        btnCancel.setPreferredSize(new Dimension(120, 35));
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        panel.add(btnUpdate);
        panel.add(btnCancel);

        return panel;
    }

    private void loadFournisseurs() {
        tableModel.setRowCount(0);
        try {
            List<Fournisseur> fournisseurs = fournisseurBD.listerTous();
            for (Fournisseur f : fournisseurs) {
                tableModel.addRow(new Object[]{
                        f.getNumFournisseur(),
                        f.getNomFournisseur(),
                        f.getTelephone(),
                        f.getAdresseEmail(),
                        String.format("%.1f/5", f.getRate())
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedFournisseur() {
        int selectedRow = tableFournisseurs.getSelectedRow();
        if (selectedRow == -1) return;

        try {
            currentNumFournisseur = (int) tableModel.getValueAt(selectedRow, 0);
            Fournisseur f = fournisseurBD.rechercherParId(currentNumFournisseur);

            if (f != null) {
                txtNum.setText(String.valueOf(f.getNumFournisseur()));
                txtNom.setText(f.getNomFournisseur());
                txtAdresse.setText(f.getAdresse());
                txtTelephone.setText(f.getTelephone());
                txtEmail.setText(f.getAdresseEmail());
                spnRate.setValue(f.getRate());
                btnUpdate.setEnabled(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFournisseur() {
        if (currentNumFournisseur == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un fournisseur!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Fournisseur f = new Fournisseur();
            f.setNumFournisseur(currentNumFournisseur);
            f.setNomFournisseur(txtNom.getText().trim());
            f.setAdresse(txtAdresse.getText().trim());
            f.setTelephone(txtTelephone.getText().trim());
            f.setAdresseEmail(txtEmail.getText().trim());
            f.setRate((Double) spnRate.getValue());

            boolean updated = fournisseurBD.modifier(f);

            if (updated) {
                JOptionPane.showMessageDialog(this,
                        "Fournisseur modifié avec succès!",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                loadFournisseurs();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
