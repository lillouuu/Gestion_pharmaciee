package interfaces.produit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import entite.Medicament;
import entite.StockMedicament;
import entitebd.MedicamentBD;
import entitebd.StockBD;

public class ModifierMedicamentFrame extends JFrame {
    private JTextField txtSearch;
    private JTable tableMedicaments;
    private DefaultTableModel tableModel;
    private JTextField txtRef, txtNom, txtPrix, txtDateFab, txtDateExp;
    private JTextField txtQuantite, txtPrixAchat, txtPrixVente, txtSeuilMin;
    private JTextArea txtDescription;
    private JComboBox<String> cmbFournisseur;
    private JButton btnSearch, btnUpdate, btnCancel, btnRefresh;
    private MedicamentBD medicamentBD;
    private StockBD stockBD;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private int currentRefMedicament = -1;

    public ModifierMedicamentFrame() {
        medicamentBD = new MedicamentBD();
        stockBD = new StockBD();
        initComponents();
        loadMedicaments();
    }

    private void initComponents() {
        setTitle("Modifier un Médicament");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(255, 165, 0));
        topPanel.setPreferredSize(new Dimension(1100, 50));

        JLabel titleLabel = new JLabel("✏ Modifier un médicament");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel de recherche et tableau (gauche)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // Recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Rechercher:"));
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> searchMedicament());
        searchPanel.add(txtSearch);

        btnSearch = new JButton("🔍");
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> searchMedicament());
        searchPanel.add(btnSearch);

        btnRefresh = new JButton("🔄");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadMedicaments());
        searchPanel.add(btnRefresh);

        leftPanel.add(searchPanel, BorderLayout.NORTH);

        // Tableau
        String[] columns = {"Réf", "Nom", "Fournisseur", "Stock", "Prix Vente"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableMedicaments = new JTable(tableModel);
        tableMedicaments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMedicaments.setRowHeight(25);
        tableMedicaments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableMedicaments.getSelectedRow() != -1) {
                loadSelectedMedicament();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableMedicaments);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel formulaire (droite)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        JScrollPane scrollForm = new JScrollPane();
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Section Médicament
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblMed = new JLabel("📋 Informations du médicament");
        lblMed.setFont(new Font("Arial", Font.BOLD, 12));
        lblMed.setForeground(new Color(255, 165, 0));
        formPanel.add(lblMed, gbc);
        gbc.gridwidth = 1;

        // Référence (non modifiable)
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Référence:"), gbc);
        gbc.gridx = 1;
        txtRef = new JTextField(15);
        txtRef.setEditable(false);
        txtRef.setBackground(new Color(240, 240, 240));
        formPanel.add(txtRef, gbc);

        // Nom
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Nom *:"), gbc);
        gbc.gridx = 1;
        txtNom = new JTextField(15);
        formPanel.add(txtNom, gbc);

        // Fournisseur
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Fournisseur *:"), gbc);
        gbc.gridx = 1;
        cmbFournisseur = new JComboBox<>(new String[]{"Fournisseur 1", "Fournisseur 2", "Fournisseur 3"});
        formPanel.add(cmbFournisseur, gbc);

        // Prix
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Prix unitaire *:"), gbc);
        gbc.gridx = 1;
        txtPrix = new JTextField(15);
        formPanel.add(txtPrix, gbc);

        // Date fabrication
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Date fab. *:"), gbc);
        gbc.gridx = 1;
        txtDateFab = new JTextField(15);
        formPanel.add(txtDateFab, gbc);

        // Date expiration
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Date exp. *:"), gbc);
        gbc.gridx = 1;
        txtDateExp = new JTextField(15);
        formPanel.add(txtDateExp, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        txtDescription = new JTextArea(3, 15);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        formPanel.add(scrollDesc, gbc);
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Section Stock
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        JLabel lblStock = new JLabel("📦 Gestion du stock");
        lblStock.setFont(new Font("Arial", Font.BOLD, 12));
        lblStock.setForeground(new Color(255, 165, 0));
        formPanel.add(lblStock, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Quantité
        gbc.gridx = 0; gbc.gridy = 10;
        formPanel.add(new JLabel("Quantité *:"), gbc);
        gbc.gridx = 1;
        txtQuantite = new JTextField(15);
        formPanel.add(txtQuantite, gbc);

        // Prix achat
        gbc.gridx = 0; gbc.gridy = 11;
        formPanel.add(new JLabel("Prix achat *:"), gbc);
        gbc.gridx = 1;
        txtPrixAchat = new JTextField(15);
        formPanel.add(txtPrixAchat, gbc);

        // Prix vente
        gbc.gridx = 0; gbc.gridy = 12;
        formPanel.add(new JLabel("Prix vente *:"), gbc);
        gbc.gridx = 1;
        txtPrixVente = new JTextField(15);
        formPanel.add(txtPrixVente, gbc);

        // Seuil
        gbc.gridx = 0; gbc.gridy = 13;
        formPanel.add(new JLabel("Seuil min *:"), gbc);
        gbc.gridx = 1;
        txtSeuilMin = new JTextField(15);
        formPanel.add(txtSeuilMin, gbc);

        scrollForm.setViewportView(formPanel);
        rightPanel.add(scrollForm, BorderLayout.CENTER);

        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        btnUpdate = new JButton("💾 Modifier");
        btnUpdate.setPreferredSize(new Dimension(120, 35));
        btnUpdate.setBackground(new Color(255, 165, 0));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> updateMedicament());

        btnCancel = new JButton("❌ Fermer");
        btnCancel.setPreferredSize(new Dimension(120, 35));
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnUpdate);
        btnPanel.add(btnCancel);

        rightPanel.add(btnPanel, BorderLayout.SOUTH);

        // Ajouter les panels au frame
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(500);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadMedicaments() {
        tableModel.setRowCount(0);
        try {
            List<Medicament> medicaments = medicamentBD.listerTous();
            for (Medicament med : medicaments) {
                StockMedicament stock = stockBD.rechercherParRef(med.getRefMedicament());
                String stockQte = stock != null ? String.valueOf(stock.getQuantiteProduit()) : "0";
                String prixVente = stock != null ? String.format("%.2f DT", stock.getPrixVente()) : "N/A";

                tableModel.addRow(new Object[]{
                        med.getRefMedicament(),
                        med.getNom(),
                        "Fournisseur " + med.getNumFournisseur(),
                        stockQte,
                        prixVente
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchMedicament() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            loadMedicaments();
            return;
        }

        tableModel.setRowCount(0);
        try {
            List<Medicament> medicaments = medicamentBD.rechercherParNom(searchTerm);
            for (Medicament med : medicaments) {
                StockMedicament stock = stockBD.rechercherParRef(med.getRefMedicament());
                String stockQte = stock != null ? String.valueOf(stock.getQuantiteProduit()) : "0";
                String prixVente = stock != null ? String.format("%.2f DT", stock.getPrixVente()) : "N/A";

                tableModel.addRow(new Object[]{
                        med.getRefMedicament(),
                        med.getNom(),
                        "Fournisseur " + med.getNumFournisseur(),
                        stockQte,
                        prixVente
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la recherche: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedMedicament() {
        int selectedRow = tableMedicaments.getSelectedRow();
        if (selectedRow == -1) return;

        try {
            currentRefMedicament = (int) tableModel.getValueAt(selectedRow, 0);
            Medicament med = medicamentBD.rechercherParRef(currentRefMedicament);
            StockMedicament stock = stockBD.rechercherParRef(currentRefMedicament);

            if (med != null) {
                txtRef.setText(String.valueOf(med.getRefMedicament()));
                txtNom.setText(med.getNom());
                cmbFournisseur.setSelectedIndex(med.getNumFournisseur() - 1);
                //txtPrix.setText(String.valueOf(med.getPrix()));
                txtDateFab.setText(dateFormat.format(med.getDateFabrication()));
                txtDateExp.setText(dateFormat.format(med.getDateExpiration()));
                txtDescription.setText(med.getDescriptio() != null ? med.getDescriptio() : "");

                if (stock != null) {
                    txtQuantite.setText(String.valueOf(stock.getQuantiteProduit()));
                    txtPrixAchat.setText(String.valueOf(stock.getPrixAchat()));
                    txtPrixVente.setText(String.valueOf(stock.getPrixVente()));
                    txtSeuilMin.setText(String.valueOf(stock.getSeuilMin()));
                } else {
                    txtQuantite.setText("0");
                    txtPrixAchat.setText("");
                    txtPrixVente.setText("");
                    txtSeuilMin.setText("10");
                }

                btnUpdate.setEnabled(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMedicament() {
        if (currentRefMedicament == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un médicament!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Validation
            double prix = Double.parseDouble(txtPrix.getText().trim());
            int quantite = Integer.parseInt(txtQuantite.getText().trim());
            double prixAchat = Double.parseDouble(txtPrixAchat.getText().trim());
            double prixVente = Double.parseDouble(txtPrixVente.getText().trim());
            int seuilMin = Integer.parseInt(txtSeuilMin.getText().trim());
            Date dateFab = dateFormat.parse(txtDateFab.getText().trim());
            Date dateExp = dateFormat.parse(txtDateExp.getText().trim());

            // Créer l'objet Medicament
            Medicament med = new Medicament();
            med.setRefMedicament(currentRefMedicament);
            med.setNom(txtNom.getText().trim());
            med.setNumFournisseur(cmbFournisseur.getSelectedIndex() + 1);
            //med.setPrix(prix);
            med.setDateFabrication(dateFab);
            med.setDateExpiration(dateExp);
            med.setDescriptio(txtDescription.getText().trim());

            // Mettre à jour le médicament
            boolean medUpdated = medicamentBD.modifier(med);

            // Mettre à jour le stock
            StockMedicament stock = stockBD.rechercherParRef(currentRefMedicament);
            if (stock != null) {
                stock.setQuantiteProduit(quantite);
                stock.setPrixAchat(prixAchat);
                stock.setPrixVente(prixVente);
                stock.setSeuilMin(seuilMin);
                stockBD.modifier(stock);
            } else {
                // Créer le stock s'il n'existe pas
                stock = new StockMedicament();
                stock.setRefMedicament(currentRefMedicament);
                stock.setQuantiteProduit(quantite);
                stock.setPrixAchat(prixAchat);
                stock.setPrixVente(prixVente);
                stock.setSeuilMin(seuilMin);
                stockBD.ajouter(stock);
            }

            if (medUpdated) {
                JOptionPane.showMessageDialog(this,
                        "Médicament modifié avec succès!",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                loadMedicaments();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de saisie: vérifiez les nombres!",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Format de date invalide! (jj/mm/aaaa)",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur BD: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
