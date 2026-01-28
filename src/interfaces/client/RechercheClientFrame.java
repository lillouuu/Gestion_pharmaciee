package interfaces.client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.Client;
import gestion.GestionClient;

public class RechercheClientFrame extends JFrame {
    private JTextField txtSearch, txtCodeCnam;
    private JTable tableClients;
    private DefaultTableModel tableModel;
    private JTextArea txtDetails;
    private JButton btnSearch, btnSearchCnam, btnRefresh, btnClose;
    private GestionClient gestionClient;

    public RechercheClientFrame() {
        gestionClient = new GestionClient();
        initComponents();
    }

    private void initComponents() {
        setTitle("Rechercher un Client");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel titre
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0, 123, 255));
        topPanel.setPreferredSize(new Dimension(1200, 50));

        JLabel titleLabel = new JLabel("🔍 Rechercher un client");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel principal avec split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Panel gauche - Recherche et tableau
        JPanel leftPanel = createSearchPanel();
        splitPane.setLeftComponent(leftPanel);

        // Panel droit - Détails
        JPanel rightPanel = createDetailsPanel();
        splitPane.setRightComponent(rightPanel);

        splitPane.setDividerLocation(800);
        add(splitPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        btnClose = new JButton("❌ Fermer");
        btnClose.setPreferredSize(new Dimension(150, 35));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnClose);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // Panel recherche
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Critères de recherche"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Recherche par nom
        gbc.gridx = 0; gbc.gridy = 0;
        searchPanel.add(new JLabel("Nom/Prénom:"), gbc);
        gbc.gridx = 1;
        txtSearch = new JTextField(20);
        txtSearch.addActionListener(e -> searchByName());
        searchPanel.add(txtSearch, gbc);

        gbc.gridx = 2;
        btnSearch = new JButton("Rechercher");
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> searchByName());
        searchPanel.add(btnSearch, gbc);

        // Recherche par code CNAM
        gbc.gridx = 0; gbc.gridy = 1;
        searchPanel.add(new JLabel("Code CNAM:"), gbc);
        gbc.gridx = 1;
        txtCodeCnam = new JTextField(20);
        txtCodeCnam.addActionListener(e -> searchByCnam());
        searchPanel.add(txtCodeCnam, gbc);

        gbc.gridx = 2;
        btnSearchCnam = new JButton("Rechercher");
        btnSearchCnam.setBackground(new Color(0, 123, 255));
        btnSearchCnam.setForeground(Color.WHITE);
        btnSearchCnam.setFocusPainted(false);
        btnSearchCnam.addActionListener(e -> searchByCnam());
        searchPanel.add(btnSearchCnam, gbc);

        // Bouton tout afficher
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        btnRefresh = new JButton("📋 Afficher tous les clients");
        btnRefresh.setBackground(new Color(108, 117, 125));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadAllClients());
        searchPanel.add(btnRefresh, gbc);

        panel.add(searchPanel, BorderLayout.NORTH);

        // Tableau
        String[] columns = {"N° Client", "N° Carte", "Nom", "Prénom", "Téléphone", "Points", "Code CNAM"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableClients = new JTable(tableModel);
        tableClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableClients.setRowHeight(25);
        tableClients.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableClients.getColumnModel().getColumn(2).setPreferredWidth(150);
        tableClients.getColumnModel().getColumn(3).setPreferredWidth(150);

        tableClients.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableClients.getSelectedRow() != -1) {
                showDetails();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableClients);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Détails du client"));

        txtDetails = new JTextArea();
        txtDetails.setEditable(false);
        txtDetails.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetails.setBackground(new Color(245, 245, 245));
        txtDetails.setText("Sélectionnez un client\npour voir les détails...");

        JScrollPane scrollPane = new JScrollPane(txtDetails);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void searchByName() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir un nom ou prénom!",
                    "Recherche",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        txtDetails.setText("");

        try {
            List<Client> clients = gestionClient.rechercherParNom(searchTerm);

            if (clients.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Aucun client trouvé avec: " + searchTerm,
                        "Résultat",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (Client client : clients) {
                tableModel.addRow(new Object[]{
                        client.getNumClient(),
                        client.getNumCarteIdentite(),
                        client.getNom(),
                        client.getPrenom(),
                        client.getTelephone(),
                        client.getPointFidelite(),
                        client.getCodeCnam() != null ? client.getCodeCnam() : "N/A"
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la recherche: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchByCnam() {
        String codeCnam = txtCodeCnam.getText().trim();
        if (codeCnam.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir un code CNAM!",
                    "Recherche",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        txtDetails.setText("");

        try {
            Client client = gestionClient.rechercherParCodeCnam(codeCnam);

            if (client == null) {
                JOptionPane.showMessageDialog(this,
                        "Aucun client trouvé avec le code CNAM: " + codeCnam,
                        "Résultat",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            tableModel.addRow(new Object[]{
                    client.getNumClient(),
                    client.getNumCarteIdentite(),
                    client.getNom(),
                    client.getPrenom(),
                    client.getTelephone(),
                    client.getPointFidelite(),
                    client.getCodeCnam()
            });

            // Sélectionner automatiquement
            tableClients.setRowSelectionInterval(0, 0);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la recherche: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllClients() {
        tableModel.setRowCount(0);
        txtDetails.setText("");

        try {
            List<Client> clients = gestionClient.listerTous();

            for (Client client : clients) {
                tableModel.addRow(new Object[]{
                        client.getNumClient(),
                        client.getNumCarteIdentite(),
                        client.getNom(),
                        client.getPrenom(),
                        client.getTelephone(),
                        client.getPointFidelite(),
                        client.getCodeCnam() != null ? client.getCodeCnam() : "N/A"
                });
            }

            JOptionPane.showMessageDialog(this,
                    clients.size() + " client(s) chargé(s)",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetails() {
        int selectedRow = tableClients.getSelectedRow();
        if (selectedRow == -1) return;

        try {
            int numClient = (Integer) tableModel.getValueAt(selectedRow, 0);
            Client client = gestionClient.rechercherParId(numClient);

            if (client != null) {
                StringBuilder details = new StringBuilder();
                details.append("═══════════════════════════════════════\n");
                details.append("      INFORMATIONS DU CLIENT\n");
                details.append("═══════════════════════════════════════\n\n");

                details.append("📋 IDENTITÉ\n");
                details.append("  • N° Client         : ").append(client.getNumClient()).append("\n");
                details.append("  • N° Carte Identité : ").append(client.getNumCarteIdentite()).append("\n");
                details.append("  • Nom               : ").append(client.getNom()).append("\n");
                details.append("  • Prénom            : ").append(client.getPrenom()).append("\n");
                details.append("  • Âge               : ").append(client.getAge()).append(" ans\n\n");

                details.append("  CONTACT\n");
                details.append("  • Téléphone         : ").append(client.getTelephone()).append("\n");
                details.append("  • Email             : ").append(
                        client.getAdresseMail() != null ? client.getAdresseMail() : "N/A").append("\n");
                details.append("  • Adresse           : ").append(
                        client.getAdresse() != null ? client.getAdresse() : "N/A").append("\n\n");

                details.append("  FIDÉLITÉ\n");
                details.append("  • Points            : ").append(client.getPointFidelite()).append("\n");
                details.append("  • Code CNAM         : ").append(
                        client.getCodeCnam() != null ? client.getCodeCnam() : "N/A").append("\n");

                if (client.getDernierDateAchat() != null) {
                    details.append("  • Dernier achat     : ").append(
                            new java.text.SimpleDateFormat("dd/MM/yyyy").format(client.getDernierDateAchat())).append("\n");
                }

                details.append("\n═══════════════════════════════════════\n");

                txtDetails.setText(details.toString());
                txtDetails.setCaretPosition(0);
            }

        } catch (SQLException ex) {
            txtDetails.setText("Erreur lors du chargement des détails:\n" + ex.getMessage());
        }
    }
}
