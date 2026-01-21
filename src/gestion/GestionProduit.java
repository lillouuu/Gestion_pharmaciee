package gestion;

import entite.Medicament;
import entite.StockMedicament;
import entitebd.MedicamentBD;
import entitebd.StockBD;
import exception.ProduitNonTrouveException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class GestionProduit {
    private MedicamentBD medicamentBD = new MedicamentBD();
    private StockBD stockBD = new StockBD();

    /**
     * Ajouter un médicament avec son stock initial
     * @return La référence du médicament créé
     */
    public int ajouterMedicament(Medicament medicament, StockMedicament stock)
            throws SQLException, IllegalArgumentException {

        // Validations métier
        validerMedicament(medicament);
        validerStock(stock);

        // Vérifier que le médicament n'est pas périmé
        if (medicament.estPerime()) {
            throw new IllegalArgumentException(
                    "Impossible d'ajouter un médicament déjà périmé! Date expiration: " +
                            medicament.getDateExpiration()
            );
        }

        // Ajouter le médicament
        int refMedicament = medicamentBD.ajouter(medicament);

        if (refMedicament > 0) {
            try {
                // Créer le stock associé
                stock.setRefMedicament(refMedicament);
                int numStock = stockBD.ajouter(stock);

                if (numStock > 0) {
                    System.out.println("✓ Médicament et stock créés avec succès (ref: " + refMedicament + ")");
                    return refMedicament;
                } else {
                    // Rollback: supprimer le médicament si le stock n'a pas pu être créé
                    medicamentBD.supprimer(refMedicament);
                    throw new SQLException("Échec de la création du stock");
                }
            } catch (SQLException e) {
                // Rollback en cas d'erreur
                medicamentBD.supprimer(refMedicament);
                throw e;
            }
        }

        throw new SQLException("Échec de la création du médicament");
    }

    /**
     * Modifier un médicament et son stock
     */
    public boolean modifierMedicament(Medicament medicament, StockMedicament stock)
            throws SQLException, ProduitNonTrouveException, IllegalArgumentException {

        // Vérifier que le médicament existe
        Medicament existing = medicamentBD.rechercherParRef(medicament.getRefMedicament());
        if (existing == null) {
            throw new ProduitNonTrouveException(medicament.getRefMedicament());
        }

        // Validations
        validerMedicament(medicament);
        validerStock(stock);

        // Mettre à jour le médicament
        boolean medUpdated = medicamentBD.modifier(medicament);

        if (medUpdated) {
            // Mettre à jour ou créer le stock
            StockMedicament existingStock = stockBD.rechercherParRef(medicament.getRefMedicament());

            if (existingStock != null) {
                stock.setNumStock(existingStock.getNumStock());
                stock.setRefMedicament(medicament.getRefMedicament());
                return stockBD.modifier(stock);
            } else {
                // Créer le stock s'il n'existe pas
                stock.setRefMedicament(medicament.getRefMedicament());
                return stockBD.ajouter(stock) > 0;
            }
        }

        return false;
    }

    /**
     * Supprimer un médicament et son stock
     */
    public boolean supprimerMedicament(int refMedicament)
            throws SQLException, ProduitNonTrouveException {

        // Vérifier que le médicament existe
        Medicament med = medicamentBD.rechercherParRef(refMedicament);
        if (med == null) {
            throw new ProduitNonTrouveException(refMedicament);
        }

        // Vérifier s'il y a du stock
        StockMedicament stock = stockBD.rechercherParRef(refMedicament);
        if (stock != null && stock.getQuantiteProduit() > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer un médicament avec du stock restant! " +
                            "Quantité actuelle: " + stock.getQuantiteProduit()
            );
        }

        // Supprimer d'abord le stock (clé étrangère)
        if (stock != null) {
            stockBD.supprimer(refMedicament);
        }

        // Puis supprimer le médicament
        boolean deleted = medicamentBD.supprimer(refMedicament);

        if (deleted) {
            System.out.println("✓ Médicament supprimé (ref: " + refMedicament + ")");
        }

        return deleted;
    }

    /**
     * Rechercher un médicament par référence avec son stock
     */
    public MedicamentAvecStock rechercherMedicamentComplet(int refMedicament)
            throws SQLException, ProduitNonTrouveException {

        Medicament med = medicamentBD.rechercherParRef(refMedicament);
        if (med == null) {
            throw new ProduitNonTrouveException(refMedicament);
        }

        StockMedicament stock = stockBD.rechercherParRef(refMedicament);
        return new MedicamentAvecStock(med, stock);
    }

    /**
     * Rechercher des médicaments par nom
     */
    public List<Medicament> rechercherParNom(String nom) throws SQLException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de recherche ne peut pas être vide");
        }
        return medicamentBD.rechercherParNom(nom);
    }

    /**
     * Lister tous les médicaments
     */
    public List<Medicament> listerTousMedicaments() throws SQLException {
        return medicamentBD.listerTous();
    }

    /**
     * Lister les médicaments d'un fournisseur
     */
    public List<Medicament> listerParFournisseur(int numFournisseur) throws SQLException {
        if (numFournisseur <= 0) {
            throw new IllegalArgumentException("Numéro de fournisseur invalide");
        }
        return medicamentBD.listerParFournisseur(numFournisseur);
    }

    /**
     * Obtenir les médicaments périmés
     */
    public List<Medicament> obtenirMedicamentsPerimes() throws SQLException {
        List<Medicament> tous = medicamentBD.listerTous();
        return tous.stream()
                .filter(Medicament::estPerime)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Obtenir les médicaments proches de l'expiration (moins de 30 jours)
     */
    public List<Medicament> obtenirMedicamentsProchesExpiration(int joursAvant) throws SQLException {
        List<Medicament> tous = medicamentBD.listerTous();
        Date now = new Date();
        long millisParJour = 24 * 60 * 60 * 1000;
        long seuilMillis = now.getTime() + (joursAvant * millisParJour);

        return tous.stream()
                .filter(med -> {
                    if (med.getDateExpiration() == null) return false;
                    return med.getDateExpiration().getTime() <= seuilMillis
                            && !med.estPerime();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Calculer le nombre total de médicaments
     */
    public int compterMedicaments() throws SQLException {
        return medicamentBD.listerTous().size();
    }

    /**
     * Calculer la valeur totale des médicaments en stock
     */
    public double calculerValeurTotaleMedicaments() throws SQLException {
        List<StockMedicament> stocks = stockBD.listerTous();
        double total = 0.0;

        for (StockMedicament stock : stocks) {
            total += stock.getQuantiteProduit() * stock.getPrixVente();
        }

        return total;
    }

    // ============ MÉTHODES DE VALIDATION PRIVÉES ============

    private void validerMedicament(Medicament med) throws IllegalArgumentException {
        if (med == null) {
            throw new IllegalArgumentException("Le médicament ne peut pas être null");
        }

        if (med.getNom() == null || med.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du médicament est obligatoire");
        }


        if (med.getNumFournisseur() <= 0) {
            throw new IllegalArgumentException("Le fournisseur est obligatoire");
        }

        if (med.getDateFabrication() == null) {
            throw new IllegalArgumentException("La date de fabrication est obligatoire");
        }

        if (med.getDateExpiration() == null) {
            throw new IllegalArgumentException("La date d'expiration est obligatoire");
        }

        if (med.getDateExpiration().before(med.getDateFabrication())) {
            throw new IllegalArgumentException(
                    "La date d'expiration doit être après la date de fabrication"
            );
        }
    }

    private void validerStock(StockMedicament stock) throws IllegalArgumentException {
        if (stock == null) {
            throw new IllegalArgumentException("Le stock ne peut pas être null");
        }

        if (stock.getQuantiteProduit() < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative");
        }

        if (stock.getPrixAchat() <= 0) {
            throw new IllegalArgumentException("Le prix d'achat doit être positif");
        }

        if (stock.getPrixVente() <= 0) {
            throw new IllegalArgumentException("Le prix de vente doit être positif");
        }

        if (stock.getSeuilMin() < 0) {
            throw new IllegalArgumentException("Le seuil minimal ne peut pas être négatif");
        }

        if (stock.getPrixVente() < stock.getPrixAchat()) {
            System.out.println("⚠ Attention: Le prix de vente est inférieur au prix d'achat!");
        }
    }

    // ============ CLASSE INTERNE POUR RETOURNER MÉDICAMENT + STOCK ============

    public static class MedicamentAvecStock {
        private Medicament medicament;
        private StockMedicament stock;

        public MedicamentAvecStock(Medicament medicament, StockMedicament stock) {
            this.medicament = medicament;
            this.stock = stock;
        }

        public Medicament getMedicament() {
            return medicament;
        }

        public StockMedicament getStock() {
            return stock;
        }

        public boolean aStock() {
            return stock != null;
        }

        public int getQuantiteStock() {
            return stock != null ? stock.getQuantiteProduit() : 0;
        }

        public boolean estEnAlerte() {
            return stock != null && stock.Alerte();
        }

        public boolean estPerime() {
            return medicament != null && medicament.estPerime();
        }
    }
}
