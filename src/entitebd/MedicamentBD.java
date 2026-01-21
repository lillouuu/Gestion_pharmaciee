package entitebd;

import entite.Medicament;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicamentBD {

    public int ajouter(Medicament medicament) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        // ✅ CORRECTION: Suppression du champ 'prix' qui n'existe pas dans la BD
        String sql = "INSERT INTO medicament (nom, num_fournisseur, descriptio, date_fabrication, date_expiration) VALUES ('" +
                medicament.getNom() + "', " +
                medicament.getNumFournisseur() + ", '" +
                medicament.getDescriptio() + "', '" +
                new java.sql.Date(medicament.getDateFabrication().getTime()) + "', '" +
                new java.sql.Date(medicament.getDateExpiration().getTime()) + "')";

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
        System.out.println("✅ Médicament ajouté !");
        return generatedId;
    }

    public Medicament rechercherParRef(int refMedicament) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM medicament WHERE ref_medicament = " + refMedicament;
        ResultSet rs = st.executeQuery(sql);

        Medicament med = null;
        if (rs.next()) {
            med = mapResultSetToMedicament(rs);
        }

        rs.close();
        st.close();
        return med;
    }

    public List<Medicament> rechercherParNom(String nom) throws SQLException {
        List<Medicament> medicaments = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM medicament WHERE nom LIKE '%" + nom + "%' ORDER BY nom";
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            medicaments.add(mapResultSetToMedicament(rs));
        }

        rs.close();
        st.close();
        return medicaments;
    }

    public List<Medicament> listerTous() throws SQLException {
        List<Medicament> medicaments = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM medicament ORDER BY nom";
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            medicaments.add(mapResultSetToMedicament(rs));
        }

        rs.close();
        st.close();
        return medicaments;
    }

    public List<Medicament> listerParFournisseur(int numFournisseur) throws SQLException {
        List<Medicament> medicaments = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM medicament WHERE num_fournisseur = " + numFournisseur + " ORDER BY nom";
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            medicaments.add(mapResultSetToMedicament(rs));
        }

        rs.close();
        st.close();
        return medicaments;
    }

    public boolean modifier(Medicament medicament) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        // ✅ CORRECTION: Suppression du champ 'prix' et correction des guillemets
        String sql = "UPDATE medicament SET " +
                "nom = '" + medicament.getNom() + "', " +
                "num_fournisseur = " + medicament.getNumFournisseur() + ", " +
                "descriptio = '" + medicament.getDescriptio() + "', " +
                "date_fabrication = '" + new java.sql.Date(medicament.getDateFabrication().getTime()) + "', " +
                "date_expiration = '" + new java.sql.Date(medicament.getDateExpiration().getTime()) + "' " +
                "WHERE ref_medicament = " + medicament.getRefMedicament();

        int result = st.executeUpdate(sql);
        st.close();
        System.out.println("✅ Médicament modifié !");
        return result > 0;
    }

    public boolean supprimer(int refMedicament) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "DELETE FROM medicament WHERE ref_medicament = " + refMedicament;
        int result = st.executeUpdate(sql);

        st.close();
        System.out.println("✅ Médicament supprimé !");
        return result > 0;
    }

    private Medicament mapResultSetToMedicament(ResultSet rs) throws SQLException {
        Medicament med = new Medicament();
        med.setRefMedicament(rs.getInt("ref_medicament"));
        med.setNom(rs.getString("nom"));
        med.setNumFournisseur(rs.getInt("num_fournisseur"));
        med.setDescriptio(rs.getString("descriptio"));
        med.setDateFabrication(rs.getDate("date_fabrication"));
        med.setDateExpiration(rs.getDate("date_expiration"));
        return med;
    }
}