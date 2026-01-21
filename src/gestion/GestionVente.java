package gestion;

import entitebd.VenteBD;
import entitebd.VoieVenteBD;
import entitebd.ClientBD;
import entite.VoieVente;
import entite.Vente;
import entite.Client;
import exception.StockInsuffisantException;
import exception.ProduitNonTrouveException;
import java.sql.SQLException;
import java.util.ArrayList;



public class GestionVente {
    private VenteBD venteBD = new VenteBD();
    private VoieVenteBD ligneventeBD = new VoieVenteBD();
    private GestionStock gestionStock = new GestionStock();
    private ClientBD clientBD = new ClientBD();


    public void enregistrerVente(Vente v, ArrayList<VoieVente> lignes)
            throws SQLException, StockInsuffisantException, ProduitNonTrouveException {

        for (VoieVente lv : lignes) {

            gestionStock.diminuerStock(lv.getRefMedicament(), lv.getQuantite());
        }

        venteBD.ajouterVente(v);
        int numVente = v.getNumVente();

        for (VoieVente lv : lignes) {
            lv.setNumVente(numVente);
            ligneventeBD.ajouterLigne(lv);
        }


        if (v.getNumClient() > 0) {
            int pointsGagnes = (int) (v.getMontantTotalVente() / 10);

            // Récupérer le client actuel
            Client client = clientBD.rechercherParId(v.getNumClient());
            if (client != null) {
                int nouveauxPoints = client.getPointFidelite() + pointsGagnes;
                clientBD.mettreAJourPoints(v.getNumClient(), nouveauxPoints);
                System.out.println("✓ " + pointsGagnes + " points de fidélité ajoutés au client");
            }
        }

        System.out.println("✓ Vente #" + numVente + " enregistrée avec succès !");
    }


    public ArrayList<Vente> obtenirHistoriqueClient(int numClient) throws SQLException {
        ArrayList<Vente> historique = new ArrayList<>();
        ArrayList<Vente> toutesVentes = venteBD.getAllVentes();

        for (Vente v : toutesVentes) {
            if (v.getNumClient() == numClient) {
                historique.add(v);
            }
        }

        return historique;
    }

    public double calculerChiffreAffaires() throws SQLException {
        ArrayList<Vente> ventes = venteBD.getAllVentes();
        double total = 0.0;

        for (Vente v : ventes) {
            total += v.getMontantTotalVente();
        }

        return total;
    }

    public VoieVenteBD getLigneventeBD() {
        return ligneventeBD;
    }

    public void setLigneventeBD(VoieVenteBD ligneventeBD) {
        this.ligneventeBD = ligneventeBD;
    }
}
