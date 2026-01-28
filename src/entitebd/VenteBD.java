package entitebd;

import entite.Vente;
import java.sql.*;
import java.util.ArrayList;

public class VenteBD {
// ajouter nouvelle vente
    public void ajouterVente(Vente v) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "INSERT INTO vente (date_vente, date_lim_rendre_produit, montant_total_vente, num_client, num_emp) VALUES ('" +
                v.getDateVente() + "', '" +
                v.getDateLimRendreProduit() + "', " +
                v.getMontantTotalVente() + ", " +
                v.getNumClient() + ", " +
                v.getNumCarteEmp() + ")";

        st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

        ResultSet rs = st.getGeneratedKeys();
        if (rs.next()) {
            v.setNumVente(rs.getInt(1));
        }
        rs.close();
        st.close();

        System.out.println("✅ Vente ajoutée avec ID: " + v.getNumVente());
    }
//modifier vente
    public void modifierVente(Vente v) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        // ✅ CORRECTION: Utilisation de montant_total_vente
        String sql = "UPDATE vente SET " +
                "date_vente = '" + v.getDateVente() + "', " +
                "date_lim_rendre_produit = '" + v.getDateLimRendreProduit() + "', " +
                "montant_total_vente = " + v.getMontantTotalVente() + ", " +
                "num_client = " + v.getNumClient() + ", " +
                "num_emp = " + v.getNumCarteEmp() + " " +
                "WHERE num_vente = " + v.getNumVente();

        st.executeUpdate(sql);
        st.close();
        System.out.println("✅ Vente modifiée");
    }
//modifier montant total
    public void modifierMontantTotal(int numVente, double montantTotal) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        // ✅ CORRECTION: Utilisation de montant_total_vente
        String sql = "UPDATE vente SET montant_total_vente = " + montantTotal + " WHERE num_vente = " + numVente;

        st.executeUpdate(sql);
        st.close();
        System.out.println("✅ Montant total modifié");
    }
// get vente par num
    public Vente getVenteById(int numVente) throws SQLException {
        Vente v = null;
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM vente WHERE num_vente = " + numVente;
        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            v = mapResultSetToVente(rs);
        }

        rs.close();
        st.close();
        return v;
    }
//lister toutes les ventes
    public ArrayList<Vente> getAllVentes() throws SQLException {
        ArrayList<Vente> ventes = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM vente ORDER BY date_vente DESC";
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            ventes.add(mapResultSetToVente(rs));
        }

        rs.close();
        st.close();
        return ventes;
    }

    private Vente mapResultSetToVente(ResultSet rs) throws SQLException {
        Vente v = new Vente();
        v.setNumVente(rs.getInt("num_vente"));
        v.setDateVente(rs.getString("date_vente"));
        v.setDateLimRendreProduit(rs.getString("date_lim_rendre_produit"));
        v.setMontantTotalVente(rs.getDouble("montant_total_vente"));
        v.setNumClient(rs.getInt("num_client"));
        v.setNumEmp(rs.getInt("num_emp"));
        return v;
    }
}

