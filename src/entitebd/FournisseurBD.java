package entitebd;

import entite.Fournisseur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class FournisseurBD {

  //ajout fournisseur
    public int ajouter(Fournisseur f) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        int numFournisseur = f.getNumFournisseur();

        if (numFournisseur <= 0) {
            // Trouver le prochain ID disponible
            String sqlMax = "SELECT COALESCE(MAX(num_fournisseur), 0) + 1 as next_id FROM fournisseur";
            ResultSet rsMax = st.executeQuery(sqlMax);
            if (rsMax.next()) {
                numFournisseur = rsMax.getInt("next_id");
            }
            rsMax.close();
        }

        String sql = "INSERT INTO fournisseur (num_fournisseur, nom_fournisseur, adresse, telephone, adresse_mail, rate) " +
                "VALUES (" +
                numFournisseur + ", '" +
                escapeSql(f.getNomFournisseur()) + "', '" +
                escapeSql(f.getAdresse()) + "', '" +
                escapeSql(f.getTelephone()) + "', '" +
                escapeSql(f.getAdresseEmail()) + "', " +
                f.getRate() + ")";

        System.out.println("🔍 SQL DEBUG: " + sql);

        int result = st.executeUpdate(sql);
        st.close();

        if (result > 0) {
            System.out.println(" Fournisseur ajouté avec succès! ID: " + numFournisseur);
            return numFournisseur;
        }

        System.err.println(" Échec de l'ajout du fournisseur");
        return -1;
    }

    /**
     * Échapper les caractères SQL dangereux (prévention injection SQL)
     */
    private String escapeSql(String value) {
        if (value == null) return "";
        return value.replace("'", "''");
    }

    /**
     * Rechercher un fournisseur par son ID
     */
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

    /**
     * Lister tous les fournisseurs
     */
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

    /**
     * Modifier un fournisseur
     */
    public boolean modifier(Fournisseur f) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "UPDATE fournisseur SET " +
                "nom_fournisseur = '" + escapeSql(f.getNomFournisseur()) + "', " +
                "adresse = '" + escapeSql(f.getAdresse()) + "', " +
                "telephone = '" + escapeSql(f.getTelephone()) + "', " +
                "adresse_mail = '" + escapeSql(f.getAdresseEmail()) + "', " +
                "rate = " + f.getRate() + " " +
                "WHERE num_fournisseur = " + f.getNumFournisseur();

        int result = st.executeUpdate(sql);
        st.close();

        if (result > 0) {
            System.out.println(" Fournisseur modifié!");
        }
        return result > 0;
    }

    /**
     * Supprimer un fournisseur
     */
    public boolean supprimer(int numFournisseur) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "DELETE FROM fournisseur WHERE num_fournisseur = " + numFournisseur;
        int result = st.executeUpdate(sql);

        st.close();

        if (result > 0) {
            System.out.println("Fournisseur supprimé!");
        }
        return result > 0;
    }

    /**
     * Calculer la performance d'un fournisseur performance = (nbCommandes * rate) / 100.0
     */
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

    /**
     * Mapper un ResultSet vers un objet Fournisseur
     */
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