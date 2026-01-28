// ⚠️ IMPORTANT: Cette classe existe déjà dans votre projet !
// Elle est utilisée pour la gestion des clients.
// 
// Voici la version existante que vous devez garder telle quelle :

package entitebd;

import entite.Personne;
import java.sql.*;


public class PersonneBD {

    /**
     * Ajouter une personne
     */
    public int ajouter(Personne personne) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "INSERT INTO personne (num_carte_identite, nom, prenom, age, adresse, adresse_mail, telephone) VALUES (" +
                personne.getNumCarteIdentite() + ", '" +
                personne.getNom() + "', '" +
                personne.getPrenom() + "', " +
                personne.getAge() + ", '" +
                (personne.getAdresse() != null ? personne.getAdresse() : "") + "', '" +
                (personne.getAdresseMail() != null ? personne.getAdresseMail() : "") + "', '" +
                personne.getTelephone() + "')";

        int result = st.executeUpdate(sql);
        st.close();

        if (result > 0) {
            System.out.println("✓ Personne ajoutée");
            return personne.getNumCarteIdentite();
        }

        return -1;
    }

    /**
     * Rechercher une personne par numéro de carte d'identité
     */
    public Personne rechercherParNumCarte(int numCarte) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM personne WHERE num_carte_identite = " + numCarte;
        ResultSet rs = st.executeQuery(sql);

        Personne personne = null;
        if (rs.next()) {
            personne = mapResultSetToPersonne(rs);
        }

        rs.close();
        st.close();
        return personne;
    }

    /**
     * Modifier une personne
     */
    public boolean modifier(Personne personne) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "UPDATE personne SET " +
                "nom = '" + personne.getNom() + "', " +
                "prenom = '" + personne.getPrenom() + "', " +
                "age = " + personne.getAge() + ", " +
                "adresse = '" + (personne.getAdresse() != null ? personne.getAdresse() : "") + "', " +
                "adresse_mail = '" + (personne.getAdresseMail() != null ? personne.getAdresseMail() : "") + "', " +
                "telephone = '" + personne.getTelephone() + "' " +
                "WHERE num_carte_identite = " + personne.getNumCarteIdentite();

        int result = st.executeUpdate(sql);
        st.close();

        System.out.println("✓ Personne modifiée");
        return result > 0;
    }

    /**
     * Supprimer une personne
     */
    public boolean supprimer(int numCarte) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "DELETE FROM personne WHERE num_carte_identite = " + numCarte;
        int result = st.executeUpdate(sql);

        st.close();
        System.out.println("✓ Personne supprimée");
        return result > 0;
    }

    /**
     * Mapper ResultSet vers Personne
     */
    private Personne mapResultSetToPersonne(ResultSet rs) throws SQLException {
        Personne p = new Personne();
        p.setNumCarteIdentite(rs.getInt("num_carte_identite"));
        p.setNom(rs.getString("nom"));
        p.setPrenom(rs.getString("prenom"));
        p.setAge(rs.getInt("age"));
        p.setAdresse(rs.getString("adresse"));
        p.setAdresseMail(rs.getString("adresse_mail"));
        p.setTelephone(rs.getString("telephone"));
        return p;
    }
}

