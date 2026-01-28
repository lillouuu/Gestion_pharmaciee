package entitebd;

import entite.CvEmployee;
import java.sql.*;


public class CvEmployeeBD {

    /**
     * Ajouter un CV
     */
    public boolean ajouter(CvEmployee cv) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "INSERT INTO cv_employee (num_employee, diplome, nb_annee_experience, formation, stage) VALUES (" +
                cv.getNumEmployee() + ", '" +
                cv.getDiplome() + "', " +
                cv.getNbAnneeExperience() + ", '" +
                cv.getFormation() + "', '" +
                cv.getStage() + "')";

        int result = st.executeUpdate(sql);
        st.close();

        if (result > 0) {
            System.out.println("✓ CV ajouté avec succès");
        }
        return result > 0;
    }

    /**
     * Rechercher un CV par numéro d'employé
     */
    public CvEmployee rechercherParNumEmployee(int numEmployee) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM cv_employee WHERE num_employee = " + numEmployee;
        ResultSet rs = st.executeQuery(sql);

        CvEmployee cv = null;
        if (rs.next()) {
            cv = mapResultSetToCv(rs);
        }

        rs.close();
        st.close();
        return cv;
    }

    /**
     * Modifier un CV
     */
    public boolean modifier(CvEmployee cv) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "UPDATE cv_employee SET " +
                "diplome = '" + cv.getDiplome() + "', " +
                "nb_annee_experience = " + cv.getNbAnneeExperience() + ", " +
                "formation = '" + cv.getFormation() + "', " +
                "stage = '" + cv.getStage() + "' " +
                "WHERE num_employee = " + cv.getNumEmployee();

        int result = st.executeUpdate(sql);
        st.close();

        System.out.println("✓ CV modifié");
        return result > 0;
    }

    /**
     * Supprimer un CV
     */
    public boolean supprimer(int numEmployee) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "DELETE FROM cv_employee WHERE num_employee = " + numEmployee;
        int result = st.executeUpdate(sql);

        st.close();
        System.out.println("✓ CV supprimé");
        return result > 0;
    }

    /**
     * Vérifier si un CV existe
     */
    public boolean existe(int numEmployee) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT COUNT(*) as count FROM cv_employee WHERE num_employee = " + numEmployee;
        ResultSet rs = st.executeQuery(sql);

        boolean existe = false;
        if (rs.next()) {
            existe = rs.getInt("count") > 0;
        }

        rs.close();
        st.close();
        return existe;
    }

    /**
     * Mapper ResultSet vers CvEmployee
     */
    private CvEmployee mapResultSetToCv(ResultSet rs) throws SQLException {
        CvEmployee cv = new CvEmployee();
        cv.setNumEmployee(rs.getInt("num_employee"));
        cv.setDiplome(rs.getString("diplome"));
        cv.setNbAnneeExperience(rs.getInt("nb_annee_experience"));
        cv.setFormation(rs.getString("formation"));
        cv.setStage(rs.getString("stage"));
        return cv;
    }
}
