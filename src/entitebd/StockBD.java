package entitebd;

import entite.StockMedicament;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockBD {


    public int ajouter(StockMedicament stock) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "INSERT INTO stock_medicament (ref_medicament, quantite_produit, prix_achat, prix_vente, seuil_min) VALUES (" +
                stock.getRefMedicament() + ", " +
                stock.getQuantiteProduit() + ", " +
                stock.getPrixAchat() + ", " +
                stock.getPrixVente() + ", " +
                stock.getSeuilMin() + ")";

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
        System.out.println("Stock ajouté !");
        return generatedId;
    }

    public StockMedicament rechercherParRef(int refMedicament) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM stock_medicament WHERE ref_medicament = " + refMedicament;
        ResultSet rs = st.executeQuery(sql);

        StockMedicament stock = null;
        if (rs.next()) {
            stock = mapResultSetToStock(rs);
        }

        rs.close();
        st.close();
        return stock;
    }

    public StockMedicament rechercherParNumStock(int numStock) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM stock_medicament WHERE num_stock = " + numStock;
        ResultSet rs = st.executeQuery(sql);

        StockMedicament stock = null;
        if (rs.next()) {
            stock = mapResultSetToStock(rs);
        }

        rs.close();
        st.close();
        return stock;
    }


    public boolean mettreAJourQuantite(int refMedicament, int nouvelleQuantite) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "UPDATE stock_medicament SET quantite_produit = " + nouvelleQuantite +
                " WHERE ref_medicament = " + refMedicament;

        int result = st.executeUpdate(sql);
        st.close();
        System.out.println("Quantité mise à jour !");
        return result > 0;
    }


    public boolean modifier(StockMedicament stock) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "UPDATE stock_medicament SET " +
                "ref_medicament = " + stock.getRefMedicament() + ", " +
                "quantite_produit = " + stock.getQuantiteProduit() + ", " +
                "prix_achat = " + stock.getPrixAchat() + ", " +
                "prix_vente = " + stock.getPrixVente() + ", " +
                "seuil_min = " + stock.getSeuilMin() + " " +
                "WHERE num_stock = " + stock.getNumStock();

        int result = st.executeUpdate(sql);
        st.close();
        System.out.println("Stock modifié !");
        return result > 0;
    }

    public List<StockMedicament> getProduitsEnAlerte() throws SQLException {
        List<StockMedicament> stocks = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM stock_medicament WHERE quantite_produit <= seuil_min ORDER BY quantite_produit ASC";
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            stocks.add(mapResultSetToStock(rs));
        }

        rs.close();
        st.close();
        return stocks;
    }

    public List<StockMedicament> listerTous() throws SQLException {
        List<StockMedicament> stocks = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM stock_medicament ORDER BY ref_medicament";
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            stocks.add(mapResultSetToStock(rs));
        }

        rs.close();
        st.close();
        return stocks;
    }



    public boolean supprimer(int refMedicament) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "DELETE FROM stock_medicament WHERE ref_medicament = " + refMedicament;
        int result = st.executeUpdate(sql);

        st.close();
        System.out.println("Stock supprimé !");
        return result > 0;
    }


    public boolean supprimerParNumStock(int numStock) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "DELETE FROM stock_medicament WHERE num_stock = " + numStock;
        int result = st.executeUpdate(sql);

        st.close();
        System.out.println("Stock supprimé !");
        return result > 0;
    }


    private StockMedicament mapResultSetToStock(ResultSet rs) throws SQLException {
        StockMedicament stock = new StockMedicament();
        stock.setNumStock(rs.getInt("num_stock"));
        stock.setRefMedicament(rs.getInt("ref_medicament"));
        stock.setQuantiteProduit(rs.getInt("quantite_produit"));
        stock.setPrixAchat(rs.getDouble("prix_achat"));
        stock.setPrixVente(rs.getDouble("prix_vente"));
        stock.setSeuilMin(rs.getInt("seuil_min"));
        return stock;
    }
}