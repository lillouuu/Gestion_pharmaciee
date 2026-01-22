package interfaces;
import javax.swing.*;
import java.awt.*;
import entite.Employe;
import interfaces.produit.*;
import interfaces.stock.*;
import interfaces.commande.*;
import interfaces.vente.*;
import interfaces.client.*;
import interfaces.fournisseur.*;
import interfaces.rapport.*;

public class MainFrame extends JFrame {
    private Employe employeConnecte;
    private JTabbedPane tabbedPane;

    public MainFrame(Employe employe) {
        this.employeConnecte = employe;
        initComponents();
    }

    private void initComponents() {
        setTitle("Gestion Pharmacie - " + employeConnecte.getPrenom() + " " + employeConnecte.getNom());
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0x718bbc));
        topPanel.setPreferredSize(new Dimension(1200, 60));

        JLabel welcomeLabel = new JLabel("  Connecté: " + employeConnecte.getPrenom() +
                " " + employeConnecte.getNom() +
                " (" + employeConnecte.getPoste() + ")");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(0xeac4d5));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.setBackground(new Color(0xeac4d5));
        logoutButton.setForeground(new Color(0x718bbc));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> handleLogout());
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 16));

        tabbedPane.addTab("📦 Produits", createProductPanel());
        tabbedPane.addTab("🛒 Ventes", createVentePanel());
        tabbedPane.addTab("👥 Clients", createClientPanel());
        tabbedPane.addTab("📋 Commandes", createCommandePanel());


        if (employeConnecte.admin()) {
            tabbedPane.addTab("📊 Stock", createStockPanel());
            tabbedPane.addTab("👨‍💼 Employés", createEmployePanel());
            tabbedPane.addTab("🏢 Fournisseurs", createFournisseurPanel());
            tabbedPane.addTab("📈 Rapports", createRapportPanel());
        }
        tabbedPane.setBackground(new Color(0xe0d7da));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Module Gestion des Produits", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(0x718bbc));
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        JButton addBtn = new JButton("➕ Ajouter un médicament");
        JButton editBtn = new JButton("✏️ Modifier un médicament");
        JButton deleteBtn = new JButton("🗑️ Supprimer un médicament");
        JButton searchBtn = new JButton("🔍 Rechercher un médicament");

        styleButton(addBtn, new Color(0xa3bccf),new Color(0xe0d7da));
        styleButton(editBtn, new Color(0x718bbc),new Color(0xeac4d5));
        styleButton(deleteBtn, new Color(0xcd7e3),new Color(0x718bbc));
        styleButton(searchBtn, new Color(0xeac4d5),new Color(0xa3bccf));

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(searchBtn);

        panel.add(btnPanel, BorderLayout.CENTER);

        addBtn.addActionListener(e -> {
            AjouterMedicamentFrame frame = new AjouterMedicamentFrame();
            frame.setVisible(true);
        });

        editBtn.addActionListener(e -> {
            ModifierMedicamentFrame frame = new ModifierMedicamentFrame();
            frame.setVisible(true);
        });

        deleteBtn.addActionListener(e -> {
            SupprimerMedicamentFrame frame = new SupprimerMedicamentFrame();
            frame.setVisible(true);
        });

        searchBtn.addActionListener(e -> {
            RechercheMedicamentFrame frame = new RechercheMedicamentFrame();
            frame.setVisible(true);
        });

        return panel;
    }

    private void styleButton(JButton btn, Color color,Color color2) {
        btn.setBackground(color);
        btn.setForeground(color2);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JPanel createVentePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Gestion des Ventes", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(0x718bbc));
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 20, 20));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        JButton btnAdd = new JButton("➕ Nouvelle vente");
        JButton btnHist = new JButton("📜 Historique ventes");
        JButton btnFacture = new JButton("🧾 Imprimer facture");

        styleButton(btnAdd, new Color(0x718bbc),new Color(0xeac4d5));
        styleButton(btnHist, new Color(0xcd7e3),new Color(0x718bbc));
        styleButton(btnFacture, new Color(0xeac4d5),new Color(0xa3bccf));

        btnPanel.add(btnAdd);
        btnPanel.add(btnHist);
        btnPanel.add(btnFacture);

        panel.add(btnPanel, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> {
            NouvelleVenteFrame frame = new NouvelleVenteFrame();
            frame.setVisible(true);
        });

        btnHist.addActionListener(e -> {
            HistoriqueVentesFrame frame = new HistoriqueVentesFrame();
            frame.setVisible(true);
        });

        btnFacture.addActionListener(e -> {
            ImprimerFactureFrame frame = new ImprimerFactureFrame();
            frame.setVisible(true);
        });

        return panel;
    }

    private JPanel createClientPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Gestion des Clients", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(0x718bbc));
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        JButton btnAdd = new JButton("➕ Ajouter client");
        JButton btnSearch = new JButton("🔍 Rechercher client");
        JButton btnHist = new JButton("📜 Historique achats");
        JButton btnFidelite = new JButton("⭐ Fidélité client");

        styleButton(btnAdd, new Color(0xa3bccf),new Color(0xe0d7da));
        styleButton(btnSearch, new Color(0x718bbc),new Color(0xeac4d5));
        styleButton(btnHist, new Color(0xcd7e3),new Color(0x718bbc));
        styleButton(btnFidelite, new Color(0xeac4d5),new Color(0xa3bccf));

        btnPanel.add(btnAdd);
        btnPanel.add(btnSearch);
        btnPanel.add(btnHist);
        btnPanel.add(btnFidelite);

        panel.add(btnPanel, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> {
            AjouterClientFrame frame = new AjouterClientFrame();
            frame.setVisible(true);
        });
        btnSearch.addActionListener(e -> {
            RechercheClientFrame frame = new RechercheClientFrame();
            frame.setVisible(true);
        });
        btnFidelite.addActionListener(e -> {
            GestionFideliteFrame frame = new GestionFideliteFrame();
            frame.setVisible(true);
        });


        btnHist.addActionListener(e -> {
            HistoriqueClientFrame frame = new HistoriqueClientFrame();
            frame.setVisible(true);
        });

        return panel;
    }

    private JPanel createCommandePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Gestion des Commandes", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(0x718bbc));
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(5, 1, 20, 20));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        JButton btnCreate = new JButton("➕ Créer commande");
        JButton btnEdit = new JButton("✏️ Modifier commande");
        JButton btnCancel = new JButton("❌ Annuler commande");
        JButton btnReceive = new JButton("📦 Réceptionner commande");
        JButton btnList = new JButton("📋 Lister commandes");
        JButton btnAlert = new JButton("⚠️ Alertes stock");

        styleButton(btnCreate, new Color(0xa3bccf),new Color(0xe0d7da));
        styleButton(btnEdit, new Color(0x718bbc),new Color(0xeac4d5));
        styleButton(btnCancel, new Color(0xcd7e3),new Color(0x718bbc));
        styleButton(btnReceive, new Color(0xeac4d5),new Color(0xa3bccf));
        styleButton(btnList, new Color(0xe0d7da),new Color(0xa3bccf));
        styleButton(btnAlert, new Color(0x718bbc),new Color(0xeac4d5));

        btnPanel.add(btnCreate);
        btnPanel.add(btnEdit);
        btnPanel.add(btnCancel);
        btnPanel.add(btnReceive);
        btnPanel.add(btnList);
        btnPanel.add(btnAlert);

        panel.add(btnPanel, BorderLayout.CENTER);

        btnCreate.addActionListener(e -> {
            CreerCommandeFrame frame = new CreerCommandeFrame();
            frame.setVisible(true);
        });

        btnEdit.addActionListener(e -> {
            ModifierCommandeFrame frame = new ModifierCommandeFrame();
            frame.setVisible(true);
        });

        btnCancel.addActionListener(e -> {
            AnnulerCommandeFrame frame = new AnnulerCommandeFrame();
            frame.setVisible(true);
        });
        btnAlert.addActionListener(e -> {
            interfaces.stock.AlertesStockFrame frame = new interfaces.stock.AlertesStockFrame();
            frame.setVisible(true);
        });

        btnReceive.addActionListener(e -> {
            ReceptionnerCommandeFrame frame = new ReceptionnerCommandeFrame();
            frame.setVisible(true);
        });

        btnList.addActionListener(e -> {
            ListerCommandesFrame frame = new ListerCommandesFrame();
            frame.setVisible(true);
        });

        return panel;
    }

    private JPanel createStockPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Gestion du Stock", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(0x718bbc));
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        JButton btnView = new JButton("👁️ Consulter stock");

        JButton btnAjust = new JButton("📦 Ajuster stock");
        JButton btnSeuil = new JButton("📉 Modifier seuil minimal");
        JButton btnReport = new JButton("📊 Rapport stock");

        styleButton(btnView,new Color(0xa3bccf),new Color(0xe0d7da));
        styleButton(btnAjust, new Color(0xcd7e3),new Color(0x718bbc));
        styleButton(btnSeuil, new Color(0xeac4d5),new Color(0xa3bccf));
        styleButton(btnReport, new Color(0xe0d7da),new Color(0xa3bccf));

        btnPanel.add(btnView);

        btnPanel.add(btnAjust);
        btnPanel.add(btnSeuil);
        btnPanel.add(btnReport);

        panel.add(btnPanel, BorderLayout.CENTER);

        btnView.addActionListener(e -> {
            ConsulterStockFrame frame = new ConsulterStockFrame();
            frame.setVisible(true);
        });


        btnAjust.addActionListener(e -> {
            interfaces.stock.AjusterStockFrame frame = new interfaces.stock.AjusterStockFrame();
            frame.setVisible(true);
        });

        btnReport.addActionListener(e -> {
            interfaces.stock.RapportStockFrame frame = new interfaces.stock.RapportStockFrame();
            frame.setVisible(true);
        });

        return panel;
    }

    private JPanel createEmployePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Gestion des Employés", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(0x718bbc));
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 20, 20));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        JButton btnAdd = new JButton("➕ Ajouter employé");
        JButton btnList = new JButton("📋 Liste des employés");
        JButton btnSalary = new JButton("💰 Gérer salaires");

        styleButton(btnAdd, new Color(0xa3bccf),new Color(0xe0d7da));
        styleButton(btnSalary, new Color(0x718bbc),new Color(0xeac4d5));
        styleButton(btnList, new Color(0xe0d7da),new Color(0xa3bccf));

        btnPanel.add(btnAdd);
        btnPanel.add(btnList);
        btnPanel.add(btnSalary);

        panel.add(btnPanel, BorderLayout.CENTER);

        // Actions des boutons
        btnAdd.addActionListener(e -> {
            interfaces.employe.AjouterEmployeFrame frame = new interfaces.employe.AjouterEmployeFrame();
            frame.setVisible(true);
        });

        btnList.addActionListener(e -> {
            interfaces.employe.ListerEmployesFrame frame = new interfaces.employe.ListerEmployesFrame();
            frame.setVisible(true);
        });

        btnSalary.addActionListener(e -> {
            interfaces.employe.GererSalaireFrame frame = new interfaces.employe.GererSalaireFrame();
            frame.setVisible(true);
        });

        return panel;
    }

    private JPanel createFournisseurPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Gestion des Fournisseurs", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(0x718bbc));
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        JButton btnAdd = new JButton("➕ Ajouter fournisseur");
        JButton btnEdit = new JButton("✏️ Modifier fournisseur");
        JButton btnDelete = new JButton("🗑️ Supprimer fournisseur");
        JButton btnEval = new JButton("⭐ Évaluer fournisseur");

        styleButton(btnAdd, new Color(0xa3bccf),new Color(0xe0d7da));
        styleButton(btnEdit, new Color(0x718bbc),new Color(0xeac4d5));
        styleButton(btnDelete, new Color(0xcd7e3),new Color(0x718bbc));
        styleButton(btnEval, new Color(0xe0d7da),new Color(0xa3bccf));

        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnEval);

        panel.add(btnPanel, BorderLayout.CENTER);

        // Actions des boutons
        btnAdd.addActionListener(e -> {
            AjouterFournisseurFrame frame = new AjouterFournisseurFrame();
            frame.setVisible(true);
        });

        btnEdit.addActionListener(e -> {
            ModifierFournisseurFrame frame = new ModifierFournisseurFrame();
            frame.setVisible(true);
        });

        btnDelete.addActionListener(e -> {
            SupprimerFournisseurFrame frame = new SupprimerFournisseurFrame();
            frame.setVisible(true);
        });

        btnEval.addActionListener(e -> {
            EvaluerFournisseurFrame frame = new EvaluerFournisseurFrame();
            frame.setVisible(true);
        });

        return panel;
    }

    private JPanel createRapportPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Rapports & Statistiques", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(0x718bbc));
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(5, 1, 20, 20));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        JButton btnStock = new JButton("📦 Rapport stock");
        JButton btnCA = new JButton("💵 Chiffre d'affaires");
        JButton btnPerf = new JButton("🏢 Performance fournisseurs");
        JButton btnTop = new JButton("🏆 Top clients");
        JButton btnProd = new JButton("📊 Ventes par produit");

        styleButton(btnStock,new Color(0xa3bccf),new Color(0xe0d7da));
        styleButton(btnCA, new Color(0x718bbc),new Color(0xeac4d5));
        styleButton(btnPerf, new Color(0xcd7e3),new Color(0x718bbc));
        styleButton(btnTop, new Color(0xeac4d5),new Color(0xa3bccf));
        styleButton(btnProd, new Color(0xe0d7da),new Color(0xa3bccf));

        btnPanel.add(btnStock);
        btnPanel.add(btnCA);
        btnPanel.add(btnPerf);
        btnPanel.add(btnTop);
        btnPanel.add(btnProd);

        panel.add(btnPanel, BorderLayout.CENTER);

        // Actions des boutons
        btnStock.addActionListener(e -> {
            interfaces.stock.RapportStockFrame frame = new interfaces.stock.RapportStockFrame();
            frame.setVisible(true);
        });

        btnCA.addActionListener(e -> {
            ChiffresAffairesFrame frame = new ChiffresAffairesFrame();
            frame.setVisible(true);
        });

        btnPerf.addActionListener(e -> {
            PerformanceFournisseursFrame frame = new PerformanceFournisseursFrame();
            frame.setVisible(true);
        });

        btnTop.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Fonctionnalité 'Top clients' en développement",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        btnProd.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Fonctionnalité 'Ventes par produit' en développement",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment vous déconnecter?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginFrame().setVisible(true);
        }
    }
}