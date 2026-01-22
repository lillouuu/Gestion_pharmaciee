package gestion;

import entite.StockMedicament;
import entitebd.StockBD;
import exception.StockInsuffisantException;
import exception.ProduitNonTrouveException;
import java.sql.SQLException;
import java.util.List;


public class GestionStock {
    private StockBD stockBD = new StockBD();

    /**
     * ✅ CORRECTION FEFO: Utilise la méthode retirerQuantite() qui gère automatiquement
     * la répartition sur plusieurs lots selon FEFO
     */
    public void diminuerStock(int refMedicament, int quantite)
            throws SQLException, StockInsuffisantException, ProduitNonTrouveException {

        // ✅ Vérifier d'abord si le médicament existe
        List<StockMedicament> stocks = stockBD.getStocksParExpiration(refMedicament);

        if (stocks == null || stocks.isEmpty()) {
            throw new ProduitNonTrouveException(refMedicament);
        }

        // ✅ Calculer le stock total disponible
        int stockTotal = 0;
        for (StockMedicament stock : stocks) {
            stockTotal += stock.getQuantiteProduit();
        }

        // ✅ Vérifier si le stock total est suffisant
        if (stockTotal < quantite) {
            throw new StockInsuffisantException(refMedicament, quantite, stockTotal);
        }

        // ✅ Utiliser la méthode FEFO qui gère automatiquement la répartition
        try {
            stockBD.retirerQuantite(refMedicament, quantite);
            System.out.println("✓ Stock diminué avec FEFO pour médicament ref " + refMedicament +
                    " (-" + quantite + " unités réparties sur les lots)");

            // ✅ Vérifier les alertes après la diminution
            stocks = stockBD.getStocksParExpiration(refMedicament);
            for (StockMedicament stock : stocks) {
                if (stock.Alerte()) {
                    System.out.println("⚠️ ALERTE: Stock faible pour lot #" + stock.getNumStock() +
                            " du médicament ref " + refMedicament +
                            " (Quantité: " + stock.getQuantiteProduit() +
                            ", Seuil: " + stock.getSeuilMin() + ")");
                }
            }

        } catch (SQLException e) {
            // ✅ Convertir l'exception SQL en StockInsuffisantException si nécessaire
            if (e.getMessage().contains("Stock insuffisant")) {
                throw new StockInsuffisantException(refMedicament, quantite, stockTotal);
            }
            throw e;
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

    /**
     * ✅ NOUVELLE MÉTHODE: Obtenir le stock total disponible pour un médicament
     */
    public int obtenirStockTotal(int refMedicament) throws SQLException {
        List<StockMedicament> stocks = stockBD.getStocksParExpiration(refMedicament);
        int total = 0;

        for (StockMedicament stock : stocks) {
            total += stock.getQuantiteProduit();
        }

        return total;
    }
}