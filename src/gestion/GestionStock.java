package gestion;

import entite.StockMedicament;
import entitebd.StockBD;
import exception.StockInsuffisantException;
import exception.ProduitNonTrouveException;
import java.sql.SQLException;
import java.util.List;


public class GestionStock {
    private StockBD stockBD = new StockBD();


    public void diminuerStock(int refMedicament, int quantite)
            throws SQLException, StockInsuffisantException, ProduitNonTrouveException {

        StockMedicament stock = stockBD.rechercherParRef(refMedicament);

        if (stock == null) {
            throw new ProduitNonTrouveException(refMedicament);
        }

        if (stock.getQuantiteProduit() < quantite) {
            throw new StockInsuffisantException(refMedicament, quantite, stock.getQuantiteProduit());
        }

        int nouvelleQuantite = stock.getQuantiteProduit() - quantite;
        stockBD.mettreAJourQuantite(refMedicament, nouvelleQuantite);


        if (nouvelleQuantite <= stock.getSeuilMin()) {
            System.out.println("⚠️ ALERTE: Stock faible pour médicament ref " + refMedicament +
                    " (Quantité: " + nouvelleQuantite + ", Seuil: " + stock.getSeuilMin() + ")");
        }
    }


    public void augmenterStock(int refMedicament, int quantite)
            throws SQLException, ProduitNonTrouveException {

        StockMedicament stock = stockBD.rechercherParRef(refMedicament);

        if (stock == null) {
            throw new ProduitNonTrouveException(refMedicament);
        }

        int nouvelleQuantite = stock.getQuantiteProduit() + quantite;
        stockBD.mettreAJourQuantite(refMedicament, nouvelleQuantite);

        System.out.println("✓ Stock augmenté pour médicament ref " + refMedicament + " (+" + quantite + " unités)");
    }


    public List<StockMedicament> obtenirAlertes() throws SQLException {
        return stockBD.getProduitsEnAlerte();
    }


    public boolean estEnAlerte(int refMedicament) throws SQLException, ProduitNonTrouveException {
        StockMedicament stock = stockBD.rechercherParRef(refMedicament);

        if (stock == null) {
            throw new ProduitNonTrouveException(refMedicament);
        }

        return stock.Alerte();
    }


    public double calculerValeurTotaleStock() throws SQLException {
        List<StockMedicament> stocks = stockBD.listerTous();
        double valeurTotale = 0.0;

        for (StockMedicament stock : stocks) {
            valeurTotale += stock.getQuantiteProduit() * stock.getPrixAchat();
        }

        return valeurTotale;
    }


    public String genererRapportStock() throws SQLException {
        List<StockMedicament> stocks = stockBD.listerTous();
        List<StockMedicament> alertes = stockBD.getProduitsEnAlerte();

        StringBuilder rapport = new StringBuilder();
        rapport.append("========== RAPPORT D'ÉTAT DU STOCK ==========\n");
        rapport.append("Nombre total de produits: ").append(stocks.size()).append("\n");
        rapport.append("Produits en alerte: ").append(alertes.size()).append("\n");
        rapport.append(String.format("Valeur totale du stock: %.2f DT\n", calculerValeurTotaleStock()));
        rapport.append("\n--- PRODUITS EN ALERTE ---\n");

        if (alertes.isEmpty()) {
            rapport.append("Aucun produit en alerte\n");
        } else {
            for (StockMedicament stock : alertes) {
                rapport.append(String.format("- Ref %d: %d unités (Seuil: %d)\n",
                        stock.getRefMedicament(),
                        stock.getQuantiteProduit(),
                        stock.getSeuilMin()));
            }
        }

        return rapport.toString();
    }
}