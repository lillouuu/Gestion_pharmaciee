package entitebd;

import entite.Fournisseur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FournisseurBD {

    /**
     * Ajouter un fournisseur
     */
    public int ajouter(Fournisseur f) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "INSERT INTO fournisseur (nom_fournisseur, adresse, telephone, adresse_mail, rate) VALUES ('" +
                f.getNomFournisseur() + "', '" +
                f.getAdresse() + "', '" +
                f.getTelephone() + "', '" +
                f.getAdresseEmail() + "', " +
                f.getRate() + ")";

        int result = st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        int generatedId = -1;

        if (result > 0) {
            ResultSet keys = st.getGeneratedKeys();
            if (keys.next()) {
                generatedId = keys.getInt(1);
            }
            keys.close();
        }

        st.close();
        System.out.println("Fournisseur ajouté !");
        return generatedId;
    }

    public Fournisseur rechercherParId(int numFournisseur) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM fournisseur WHERE num_fournisseur = " + numFournisseur;
        ResultSet rs = st.executeQuery(sql);

        Fournisseur f = null;
        if (rs.next()) {
            f = mapResultSetToFournisseur(rs);
        }

        rs.close();
        st.close();
        return f;
    }


    public List<Fournisseur> listerTous() throws SQLException {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM fournisseur ORDER BY nom_fournisseur";
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            fournisseurs.add(mapResultSetToFournisseur(rs));
        }

        rs.close();
        st.close();
        return fournisseurs;
    }

    public boolean modifier(Fournisseur f) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "UPDATE fournisseur SET " +
                "nom_fournisseur = '" + f.getNomFournisseur() + "', " +
                "adresse = '" + f.getAdresse() + "', " +
                "telephone = '" + f.getTelephone() + "', " +
                "adresse_mail = '" + f.getAdresseEmail() + "', " +
                "rate = " + f.getRate() + " " +
                "WHERE num_fournisseur = " + f.getNumFournisseur();

        int result = st.executeUpdate(sql);
        st.close();
        System.out.println("Fournisseur modifié !");
        return result > 0;
    }


    public boolean supprimer(int numFournisseur) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "DELETE FROM fournisseur WHERE num_fournisseur = " + numFournisseur;
        int result = st.executeUpdate(sql);

        st.close();
        System.out.println("Fournisseur supprimé !");
        return result > 0;
    }


    public double calculerPerformance(int numFournisseur) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT COUNT(*) as nb_commandes, f.rate as rate " +
                "FROM commande c JOIN fournisseur f ON c.num_fournisseur = f.num_fournisseur " +
                "WHERE f.num_fournisseur = " + numFournisseur;

        ResultSet rs = st.executeQuery(sql);
        double performance = 0.0;

        if (rs.next()) {
            int nbCommandes = rs.getInt("nb_commandes");
            double rate = rs.getDouble("rate");
            performance = (nbCommandes * rate) / 100.0;
        }

        rs.close();
        st.close();
        return performance;
    }


    private Fournisseur mapResultSetToFournisseur(ResultSet rs) throws SQLException {
        Fournisseur f = new Fournisseur();
        f.setNumFournisseur(rs.getInt("num_fournisseur"));
        f.setNomFournisseur(rs.getString("nom_fournisseur"));
        f.setAdresse(rs.getString("adresse"));
        f.setTelephone(rs.getString("telephone"));
        f.setAdresseEmail(rs.getString("adresse_mail"));
        f.setRate(rs.getFloat("rate"));
        return f;
    }
}