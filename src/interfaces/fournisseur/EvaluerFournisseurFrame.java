package interfaces.fournisseur;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.Fournisseur;
import entitebd.FournisseurBD;

public class EvaluerFournisseurFrame extends JFrame {
    private JTable tableFournisseurs;
    private DefaultTableModel tableModel;
    private JSlider sliderRate;
    private JLabel lblRateValue, lblPerformance;
    private JTextArea txtCommentaire;
    private JButton btnSave, btnCancel, btnRefresh;
    private FournisseurBD fournisseurBD;
    private int currentNumFournisseur = -1;

    public EvaluerFournisseurFrame() {
        fournisseurBD = new FournisseurBD();
        initComponents();
        loadFournisseurs();
    }

    private void initComponents() {
        setTitle("Évaluer un Fournisseur");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(255, 193, 7));
        topPanel.setPreferredSize(new Dimension(1000, 50));

        JLabel titleLabel = new JLabel("⭐ Évaluer un fournisseur");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Panel gauche - Liste
        JPanel leftPanel = createListPanel();
        splitPane.setLeftComponent(leftPanel);

        // Panel droit - Évaluation
        JPanel rightPanel = createEvaluationPanel();
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

        JLabel lblTitle = new JLabel("📋 Liste des fournisseurs");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(lblTitle, BorderLayout.NORTH);

        // Tableau
        String[] columns = {"N°", "Nom", "Téléphone", "Évaluation", "Performance"};
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

    private JPanel createEvaluationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Titre
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblTitle = new JLabel("⭐ Évaluation du fournisseur");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        // Instructions
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JLabel lblInstruction = new JLabel("Sélectionnez un fournisseur dans la liste pour l'évaluer");
        lblInstruction.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInstruction.setForeground(Color.GRAY);
        panel.add(lblInstruction, gbc);
        gbc.gridwidth = 1;

        // Séparateur
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // Évaluation
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblRate = new JLabel("Note (0-5):");
        lblRate.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblRate, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        sliderRate = new JSlider(0, 50, 25);
        sliderRate.setMajorTickSpacing(10);
        sliderRate.setMinorTickSpacing(5);
        sliderRate.setPaintTicks(true);
        sliderRate.setPaintLabels(false);
        sliderRate.addChangeListener(e -> updateRateLabel());
        panel.add(sliderRate, gbc);
        gbc.gridwidth = 1;

        // Affichage de la note
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        lblRateValue = new JLabel("⭐ 2.5 / 5.0");
        lblRateValue.setFont(new Font("Arial", Font.BOLD, 24));
        lblRateValue.setForeground(new Color(236, 72, 153));
        lblRateValue.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblRateValue, gbc);
        gbc.gridwidth = 1;

        // Performance
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        lblPerformance = new JLabel("Performance: N/A");
        lblPerformance.setFont(new Font("Arial", Font.ITALIC, 12));
        lblPerformance.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblPerformance, gbc);
        gbc.gridwidth = 1;

        // Commentaire
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        JLabel lblComment = new JLabel("Commentaire:");
        lblComment.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblComment, gbc);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtCommentaire = new JTextArea(5, 20);
        txtCommentaire.setLineWrap(true);
        txtCommentaire.setWrapStyleWord(true);
        txtCommentaire.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane scrollComment = new JScrollPane(txtCommentaire);
        panel.add(scrollComment, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setPreferredSize(new Dimension(130, 35));
        btnRefresh.setBackground(new Color(0, 123, 255));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadFournisseurs());

        btnSave = new JButton("💾 Enregistrer");
        btnSave.setPreferredSize(new Dimension(130, 35));
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setEnabled(false);
        btnSave.addActionListener(e -> saveEvaluation());

        btnCancel = new JButton("❌ Fermer");
        btnCancel.setPreferredSize(new Dimension(130, 35));
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        panel.add(btnRefresh);
        panel.add(btnSave);
        panel.add(btnCancel);

        return panel;
    }

    private void loadFournisseurs() {
        tableModel.setRowCount(0);
        try {
            List<Fournisseur> fournisseurs = fournisseurBD.listerTous();
            for (Fournisseur f : fournisseurs) {
                double performance = fournisseurBD.calculerPerformance(f.getNumFournisseur());

                tableModel.addRow(new Object[]{
                        f.getNumFournisseur(),
                        f.getNomFournisseur(),
                        f.getTelephone(),
                        String.format("%.1f/5", f.getRate()),
                        String.format("%.2f%%", performance)
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
                sliderRate.setValue((int) (f.getRate() * 10));
                double performance = fournisseurBD.calculerPerformance(currentNumFournisseur);
                lblPerformance.setText(String.format("Performance: %.2f%%", performance));
                txtCommentaire.setText("");
                btnSave.setEnabled(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRateLabel() {
        double rate = sliderRate.getValue() / 10.0;
        String stars = "⭐".repeat(Math.max(1, (int) Math.round(rate)));
        lblRateValue.setText(String.format("%s %.1f / 5.0", stars, rate));

        // Changer la couleur selon la note
        if (rate >= 4.0) {
            lblRateValue.setForeground(new Color(40, 167, 69));
        } else if (rate >= 3.0) {
            lblRateValue.setForeground(new Color(255, 193, 7));
        } else {
            lblRateValue.setForeground(new Color(220, 53, 69));
        }
    }

    private void saveEvaluation() {
        if (currentNumFournisseur == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un fournisseur!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Fournisseur f = fournisseurBD.rechercherParId(currentNumFournisseur);
            if (f != null) {
                double newRate = sliderRate.getValue() / 10.0;
                f.setRate(newRate);

                boolean updated = fournisseurBD.modifier(f);

                if (updated) {
                    String comment = txtCommentaire.getText().trim();
                    String message = "✅ Évaluation enregistrée!\n\n" +
                            "Fournisseur: " + f.getNomFournisseur() + "\n" +
                            "Note: " + String.format("%.1f/5", newRate);

                    if (!comment.isEmpty()) {
                        message += "\nCommentaire: " + comment;
                    }

                    JOptionPane.showMessageDialog(this,
                            message,
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);

                    loadFournisseurs();
                    txtCommentaire.setText("");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
