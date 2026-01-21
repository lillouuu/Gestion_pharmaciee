package entitebd;

import entite.Commande;
import java.sql.*;
import java.util.ArrayList;

public class CommandeBD {

    public void ajouterCommande(Commande c) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "INSERT INTO commande " +
                "(statut, montant_total_commande, date_achat, date_lim_rendre_produit, num_fournisseur, num_carte_emp) VALUES ('" +
                c.getStatut() + "', " +
                c.getMontantTotalCommande() + ", '" +
                c.getDateAchat() + "', '" +
                c.getDateLimRendreProduit() + "', " +
                c.getNumFournisseur() + ", " +
                c.getNumCarteEmp() + ")";

        st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

        ResultSet rs = st.getGeneratedKeys();
        if (rs.next()) {
            c.setNumCommande(rs.getInt(1));
        }
        rs.close();
        st.close();
        System.out.println("✅ Commande ajoutée avec ID: " + c.getNumCommande());
    }

    public Commande getCommandeById(int numCommande) throws SQLException {
        Commande c = null;
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM commande WHERE num_commande = " + numCommande;
        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            c = mapResultSetToCommande(rs);
        }

        rs.close();
        st.close();
        return c;
    }

    public void modifierCommande(Commande c) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        // ✅ CORRECTION: Ajout de la virgule manquante après montant_total_commande
        String sql = "UPDATE commande SET " +
                "statut = '" + c.getStatut() + "', " +
                "montant_total_commande = " + c.getMontantTotalCommande() + ", " +
                "date_achat = '" + c.getDateAchat() + "', " +
                "date_lim_rendre_produit = '" + c.getDateLimRendreProduit() + "', " +
                "num_fournisseur = " + c.getNumFournisseur() + ", " +
                "num_carte_emp = " + c.getNumCarteEmp() + " " +
                "WHERE num_commande = " + c.getNumCommande();

        st.executeUpdate(sql);
        st.close();
        System.out.println("✅ Commande modifiée");
    }

    public void modifierStatut(int numCommande, String statut) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "UPDATE commande SET statut = '" + statut + "' WHERE num_commande = " + numCommande;
        st.executeUpdate(sql);
        st.close();
        System.out.println("✅ Statut de la commande modifié");
    }

    public ArrayList<Commande> afficherToutesCommandes() throws SQLException {
        ArrayList<Commande> commandes = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM commande ORDER BY date_achat DESC";
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            commandes.add(mapResultSetToCommande(rs));
        }

        rs.close();
        st.close();
        return commandes;
    }

    private Commande mapResultSetToCommande(ResultSet rs) throws SQLException {
        Commande c = new Commande();
        c.setNumCommande(rs.getInt("num_commande"));
        c.setStatut(rs.getString("statut"));
        c.setMontantTotalCommande(rs.getDouble("montant_total_commande"));
        c.setDateAchat(rs.getString("date_achat"));
        c.setDateLimRendreProduit(rs.getString("date_lim_rendre_produit"));
        c.setNumFournisseur(rs.getInt("num_fournisseur"));
        c.setNumCarteEmp(rs.getInt("num_carte_emp"));
        return c;
    }
}