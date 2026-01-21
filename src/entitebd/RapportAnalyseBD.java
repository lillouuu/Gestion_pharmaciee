package entitebd;

import entite.RapportAnalyse;
import java.sql.*;

public class RapportAnalyseBD {

    public void ajouter(RapportAnalyse rapport) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        // ✅ NOTE: L'ID doit être fourni car il n'est pas auto_increment dans la BD
        String sql = "INSERT INTO rapport_analyse (id, chiffre_affaire, num_carte_emp) VALUES (" +
                rapport.getId() + ", " +
                rapport.getChiffreAffaire() + ", " +
                rapport.getNumCarteEmp() + ")";

        st.executeUpdate(sql);
        st.close();
        System.out.println("✅ Rapport d'analyse ajouté");
    }

    public RapportAnalyse getRapportByEmploye(int numCarteEmp) throws SQLException {
        RapportAnalyse rapport = null;
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM rapport_analyse WHERE num_carte_emp = " + numCarteEmp;
        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            rapport = mapResultSetToRapport(rs);
        }

        rs.close();
        st.close();
        return rapport;
    }

    public void modifier(RapportAnalyse rapport) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "UPDATE rapport_analyse SET " +
                "chiffre_affaire = " + rapport.getChiffreAffaire() + ", " +
                "num_carte_emp = " + rapport.getNumCarteEmp() + " " +
                "WHERE id = " + rapport.getId();

        st.executeUpdate(sql);
        st.close();
        System.out.println("✅ Rapport d'analyse modifié");
    }

    private RapportAnalyse mapResultSetToRapport(ResultSet rs) throws SQLException {
        RapportAnalyse rapport = new RapportAnalyse();
        rapport.setId(rs.getInt("id"));
        rapport.setChiffreAffaire(rs.getDouble("chiffre_affaire"));
        rapport.setNumCarteEmp(rs.getInt("num_carte_emp"));
        return rapport;
    }
}
