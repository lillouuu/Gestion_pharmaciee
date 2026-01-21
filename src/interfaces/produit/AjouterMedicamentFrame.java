package interfaces.produit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import entite.Fournisseur;
import entite.Medicament;
import entite.StockMedicament;
import entitebd.FournisseurBD;
import gestion.GestionProduit;

public class AjouterMedicamentFrame extends JFrame {
    // ✅ CORRECTION: Suppression de txtPrix (n'existe pas dans Medicament)
    private JTextField txtNom, txtQuantite, txtPrixAchat, txtPrixVente, txtSeuilMin;
    private JTextField txtDateFab, txtDateExp;
    private JTextArea txtDescription;
    private JComboBox<String> cmbFournisseur;
    private JButton btnSave, btnCancel, btnRefreshFourn;
    private JLabel lblFournisseurInfo;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Map<String, Integer> fournisseurMap = new HashMap<>();
    private FournisseurBD fournisseurBD;
    private GestionProduit gestionProduit;

    public AjouterMedicamentFrame() {
        fournisseurBD = new FournisseurBD();
        gestionProduit = new GestionProduit();
        initComponents();
        loadFournisseurs();
    }

    private void initComponents() {
        setTitle("Ajouter un Médicament");
        setSize(750, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0, 102, 204));
        topPanel.setPreferredSize(new Dimension(750, 50));

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
        lblInfoMed.setForeground(new Color(0, 102, 204));
        formPanel.add(lblInfoMed, gbc);
        gbc.gridwidth = 1;

        // Nom
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nom *:"), gbc);
        gbc.gridx = 1;
        txtNom = new JTextField(25);
        formPanel.add(txtNom, gbc);

        // Fournisseur avec recherche
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Fournisseur *:"), gbc);
        gbc.gridx = 1;

        JPanel fournPanel = new JPanel(new BorderLayout(5, 0));
        cmbFournisseur = new JComboBox<>();
        cmbFournisseur.setEditable(true);

        JTextField editor = (JTextField) cmbFournisseur.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterFournisseurs(editor.getText());
            }
        });

        fournPanel.add(cmbFournisseur, BorderLayout.CENTER);

        btnRefreshFourn = new JButton("🔄");
        btnRefreshFourn.setPreferredSize(new Dimension(35, 25));
        btnRefreshFourn.setToolTipText("Actualiser la liste");
        btnRefreshFourn.setFocusPainted(false);
        btnRefreshFourn.addActionListener(e -> loadFournisseurs());
        fournPanel.add(btnRefreshFourn, BorderLayout.EAST);

        formPanel.add(fournPanel, gbc);

        // Info fournisseur sélectionné
        gbc.gridx = 1; gbc.gridy = 3;
        lblFournisseurInfo = new JLabel("");
        lblFournisseurInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblFournisseurInfo.setForeground(Color.GRAY);
        formPanel.add(lblFournisseurInfo, gbc);

        cmbFournisseur.addActionListener(e -> updateFournisseurInfo());

        // Date fabrication
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Date fabrication (jj/mm/aaaa) *:"), gbc);
        gbc.gridx = 1;
        txtDateFab = new JTextField(25);
        txtDateFab.setText(dateFormat.format(new Date()));
        formPanel.add(txtDateFab, gbc);

        // Date expiration
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Date expiration (jj/mm/aaaa) *:"), gbc);
        gbc.gridx = 1;
        txtDateExp = new JTextField(25);
        formPanel.add(txtDateExp, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        txtDescription = new JTextArea(3, 25);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        formPanel.add(scrollDesc, gbc);
        gbc.gridheight = 1;

        // Section: Gestion du stock
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 5, 5, 5);
        JLabel lblStock = new JLabel("📦 Gestion du stock");
        lblStock.setFont(new Font("Arial", Font.BOLD, 14));
        lblStock.setForeground(new Color(0, 102, 204));
        formPanel.add(lblStock, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Quantité initiale
        gbc.gridx = 0; gbc.gridy = 9;
        formPanel.add(new JLabel("Quantité initiale *:"), gbc);
        gbc.gridx = 1;
        txtQuantite = new JTextField(25);
        txtQuantite.setText("0");
        formPanel.add(txtQuantite, gbc);

        // Prix d'achat
        gbc.gridx = 0; gbc.gridy = 10;
        formPanel.add(new JLabel("Prix d'achat (DT) *:"), gbc);
        gbc.gridx = 1;
        txtPrixAchat = new JTextField(25);
        formPanel.add(txtPrixAchat, gbc);

        // Prix de vente
        gbc.gridx = 0; gbc.gridy = 11;
        formPanel.add(new JLabel("Prix de vente (DT) *:"), gbc);
        gbc.gridx = 1;
        txtPrixVente = new JTextField(25);
        formPanel.add(txtPrixVente, gbc);

        // Seuil minimal
        gbc.gridx = 0; gbc.gridy = 12;
        formPanel.add(new JLabel("Seuil minimal *:"), gbc);
        gbc.gridx = 1;
        txtSeuilMin = new JTextField(25);
        txtSeuilMin.setText("10");
        formPanel.add(txtSeuilMin, gbc);

        // Note
        gbc.gridx = 0; gbc.gridy = 13; gbc.gridwidth = 2;
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
        btnCancel.setBackground(new Color(220, 53, 69));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadFournisseurs() {
        cmbFournisseur.removeAllItems();
        fournisseurMap.clear();

        try {
            List<Fournisseur> fournisseurs = fournisseurBD.listerTous();

            if (fournisseurs.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Aucun fournisseur trouvé!\nVeuillez d'abord ajouter des fournisseurs.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (Fournisseur f : fournisseurs) {
                String displayText = f.getNomFournisseur() + " (ID: " + f.getNumFournisseur() + ")";
                cmbFournisseur.addItem(displayText);
                fournisseurMap.put(displayText, f.getNumFournisseur());
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des fournisseurs: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterFournisseurs(String searchText) {
        if (searchText.isEmpty()) {
            loadFournisseurs();
            return;
        }

        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cmbFournisseur.getModel();
        model.removeAllElements();

        for (String fournisseur : fournisseurMap.keySet()) {
            if (fournisseur.toLowerCase().contains(searchText.toLowerCase())) {
                model.addElement(fournisseur);
            }
        }
    }

    private void updateFournisseurInfo() {
        String selected = (String) cmbFournisseur.getSelectedItem();
        if (selected != null && fournisseurMap.containsKey(selected)) {
            try {
                Fournisseur f = fournisseurBD.rechercherParId(fournisseurMap.get(selected));
                if (f != null) {
                    lblFournisseurInfo.setText("📞 " + f.getTelephone() + " | 📧 " + f.getAdresseEmail());
                }
            } catch (SQLException ex) {
                lblFournisseurInfo.setText("");
            }
        } else {
            lblFournisseurInfo.setText("");
        }
    }

    private int getSelectedFournisseurId() {
        String selected = (String) cmbFournisseur.getSelectedItem();
        if (selected == null) return -1;

        if (fournisseurMap.containsKey(selected)) {
            return fournisseurMap.get(selected);
        }

        try {
            int start = selected.indexOf("(ID: ") + 5;
            int end = selected.indexOf(")", start);
            if (start > 4 && end > start) {
                return Integer.parseInt(selected.substring(start, end));
            }
        } catch (Exception e) {
            // Ignore
        }

        return -1;
    }

    private void saveMedicament() {
        // ✅ CORRECTION: Validation sans txtPrix
        if (txtNom.getText().trim().isEmpty() ||
                txtDateFab.getText().trim().isEmpty() || txtDateExp.getText().trim().isEmpty() ||
                txtQuantite.getText().trim().isEmpty() || txtPrixAchat.getText().trim().isEmpty() ||
                txtPrixVente.getText().trim().isEmpty() || txtSeuilMin.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Veuillez remplir tous les champs obligatoires (*) !",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int numFournisseur = getSelectedFournisseurId();
        if (numFournisseur == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un fournisseur valide!",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // ✅ CORRECTION: Parser les données sans prix
            int quantite = Integer.parseInt(txtQuantite.getText().trim());
            double prixAchat = Double.parseDouble(txtPrixAchat.getText().trim());
            double prixVente = Double.parseDouble(txtPrixVente.getText().trim());
            int seuilMin = Integer.parseInt(txtSeuilMin.getText().trim());
            Date dateFab = dateFormat.parse(txtDateFab.getText().trim());
            Date dateExp = dateFormat.parse(txtDateExp.getText().trim());

            // Créer les objets
            Medicament med = new Medicament();
            med.setNom(txtNom.getText().trim());
            med.setNumFournisseur(numFournisseur);
            med.setDescriptio(txtDescription.getText().trim());
            med.setDateFabrication(dateFab);
            med.setDateExpiration(dateExp);

            StockMedicament stock = new StockMedicament();
            stock.setQuantiteProduit(quantite);
            stock.setPrixAchat(prixAchat);
            stock.setPrixVente(prixVente);
            stock.setSeuilMin(seuilMin);

            // Utiliser GestionProduit pour ajouter
            int refMedicament = gestionProduit.ajouterMedicament(med, stock);

            JOptionPane.showMessageDialog(this,
                    "✅ Médicament ajouté avec succès!\n\n" +
                            "Référence: " + refMedicament + "\n" +
                            "Nom: " + txtNom.getText() + "\n" +
                            "Fournisseur: " + cmbFournisseur.getSelectedItem() + "\n" +
                            "Stock initial: " + quantite + " unité(s)",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Les prix, quantités et seuil doivent être des nombres valides!",
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

    private void clearForm() {
        txtNom.setText("");
        txtDateFab.setText(dateFormat.format(new Date()));
        txtDateExp.setText("");
        txtDescription.setText("");
        txtQuantite.setText("0");
        txtPrixAchat.setText("");
        txtPrixVente.setText("");
        txtSeuilMin.setText("10");
        if (cmbFournisseur.getItemCount() > 0) {
            cmbFournisseur.setSelectedIndex(0);
        }
        lblFournisseurInfo.setText("");
        txtNom.requestFocus();
    }
}