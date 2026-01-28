package entitebd;

import entite.VoieCommande;
import java.sql.*;
import java.util.ArrayList;

public class VoieCommandeBD {
//ajouter ligne de commande
    public void ajouterLigne(VoieCommande lc) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        lc.setPrixTotalVoieCommande();

        String sql = "INSERT INTO voie_commande " +
                "(num_commande, ref_medicament, quantite, prix_unitaire, prix_total_voie_commande, remise, impots_sur_commande) VALUES (" +
                lc.getNumCommande() + ", " +
                lc.getRefMedicament() + ", " +
                lc.getQuantite() + ", " +
                lc.getPrixUnitaire() + ", " +
                lc.getPrixTotalVoieCommande() + ", " +
                lc.getRemise() + ", " +
                lc.getImpotSurCommande() + ")";

        st.executeUpdate(sql);
        st.close();

        System.out.println("✅ Ligne de commande ajoutée");
    }
//modifier ligne
    public void modifierLigne(VoieCommande lc) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();


        lc.setPrixTotalVoieCommande();

        String sql = "UPDATE voie_commande SET " +
                "num_commande = " + lc.getNumCommande() + ", " +
                "ref_medicament = " + lc.getRefMedicament() + ", " +
                "quantite = " + lc.getQuantite() + ", " +
                "prix_unitaire = " + lc.getPrixUnitaire() + ", " +
                "prix_total_voie_commande = " + lc.getPrixTotalVoieCommande() + ", " +
                "remise = " + lc.getRemise() + ", " +
                "impots_sur_commande = " + lc.getImpotSurCommande() + " " +
                "WHERE id_ligne_commande = " + lc.getIdLigneCommande();

        st.executeUpdate(sql);
        st.close();

        System.out.println("✅ Ligne de commande modifiée");
    }

    public void supprimerLigne(int idLigneCommande) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "DELETE FROM voie_commande WHERE id_ligne_commande = " + idLigneCommande;
        st.executeUpdate(sql);
        st.close();

        System.out.println("✅ Ligne de commande supprimée");
    }
//listes tous les voies d'une commande de numCommande dans un arraylist
    public ArrayList<VoieCommande> getLignesParCommande(int numCommande) throws SQLException {
        ArrayList<VoieCommande> lignes = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM voie_commande WHERE num_commande = " + numCommande;
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            VoieCommande lc = mapResultSetToVoieCommande(rs);
            lignes.add(lc);
        }

        rs.close();
        st.close();
        return lignes;
    }
//recupere voie par num
    public VoieCommande getLigneById(int idLigneCommande) throws SQLException {
        VoieCommande lc = null;
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT * FROM voie_commande WHERE id_ligne_commande = " + idLigneCommande;
        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            lc = mapResultSetToVoieCommande(rs);
        }

        rs.close();
        st.close();
        return lc;
    }

    private VoieCommande mapResultSetToVoieCommande(ResultSet rs) throws SQLException {
        VoieCommande lc = new VoieCommande();
        lc.setIdLigneCommande(rs.getInt("id_ligne_commande"));
        lc.setNumCommande(rs.getInt("num_commande"));
        lc.setRefMedicament(rs.getInt("ref_medicament"));
        lc.setQuantite(rs.getInt("quantite"));
        lc.setPrixUnitaire(rs.getDouble("prix_unitaire"));
        lc.setRemise(rs.getDouble("remise"));
        lc.setImpotSurCommande(rs.getDouble("impots_sur_commande"));
        // ✅ Le prix total sera recalculé si nécessaire
        return lc;
    }
}
