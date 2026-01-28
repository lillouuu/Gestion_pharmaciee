package gestion;

import entite.Fournisseur;
import entitebd.FournisseurBD;
import java.sql.SQLException;
import java.util.List;

public class GestionFournisseur {
    private FournisseurBD fournisseurBD;

    public GestionFournisseur() {
        this.fournisseurBD = new FournisseurBD();
    }

    /**
     * Ajouter un fournisseur
     */
    public int ajouterFournisseur(Fournisseur fournisseur) throws SQLException, IllegalArgumentException {
        validerFournisseur(fournisseur);

        int numFournisseur = fournisseurBD.ajouter(fournisseur);

        System.out.println("✓ Fournisseur ajouté avec succès (ID: " + numFournisseur + ")");
        return numFournisseur;
    }

    /**
     * Modifier un fournisseur
     */
    public boolean modifierFournisseur(Fournisseur fournisseur) throws SQLException, IllegalArgumentException {
        validerFournisseur(fournisseur);

        if (fournisseur.getNumFournisseur() <= 0) {
            throw new IllegalArgumentException("Numéro de fournisseur invalide");
        }

        Fournisseur existing = fournisseurBD.rechercherParId(fournisseur.getNumFournisseur());
        if (existing == null) {
            throw new IllegalArgumentException("Fournisseur introuvable: " + fournisseur.getNumFournisseur());
        }

        return fournisseurBD.modifier(fournisseur);
    }

    /**
     * Supprimer un fournisseur
     */
    public boolean supprimerFournisseur(int numFournisseur) throws SQLException {
        if (numFournisseur <= 0) {
            throw new IllegalArgumentException("Numéro de fournisseur invalide");
        }

        Fournisseur fournisseur = fournisseurBD.rechercherParId(numFournisseur);
        if (fournisseur == null) {
            throw new IllegalArgumentException("Fournisseur introuvable: " + numFournisseur);
        }


        return fournisseurBD.supprimer(numFournisseur);
    }

    /**
     * Rechercher un fournisseur par ID
     */
    public Fournisseur rechercherParId(int numFournisseur) throws SQLException {
        return fournisseurBD.rechercherParId(numFournisseur);
    }

    /**
     * Lister tous les fournisseurs
     */
    public List<Fournisseur> listerTous() throws SQLException {
        return fournisseurBD.listerTous();
    }

    /**
     * Calculer la performance d'un fournisseur
     */
    public double calculerPerformance(int numFournisseur) throws SQLException {
        return fournisseurBD.calculerPerformance(numFournisseur);
    }

    /**
     * Évaluer un fournisseur (mettre à jour son taux)
     */
    public boolean evaluerFournisseur(int numFournisseur, double nouveauTaux) throws SQLException {
        if (nouveauTaux < 0 || nouveauTaux > 100) {
            throw new IllegalArgumentException("Le taux doit être entre 0 et 100");
        }

        Fournisseur fournisseur = fournisseurBD.rechercherParId(numFournisseur);
        if (fournisseur == null) {
            throw new IllegalArgumentException("Fournisseur introuvable: " + numFournisseur);
        }

        fournisseur.setRate(nouveauTaux);
        return fournisseurBD.modifier(fournisseur);
    }

    /**
     * Obtenir les meilleurs fournisseurs
     */
    public List<Fournisseur> getTopFournisseurs(int limit) throws SQLException {
        List<Fournisseur> fournisseurs = fournisseurBD.listerTous();

        // Trier par rate décroissant
        fournisseurs.sort((f1, f2) -> Double.compare(f2.getRate(), f1.getRate()));

        if (limit > 0 && fournisseurs.size() > limit) {
            return fournisseurs.subList(0, limit);
        }

        return fournisseurs;
    }

    /**
     * Générer un rapport sur les fournisseurs
     */
    public String genererRapport() throws SQLException {
        List<Fournisseur> fournisseurs = fournisseurBD.listerTous();

        StringBuilder rapport = new StringBuilder();
        rapport.append("═══════════════════════════════════════\n");
        rapport.append("      RAPPORT FOURNISSEURS\n");
        rapport.append("═══════════════════════════════════════\n\n");
        rapport.append("Nombre total de fournisseurs: ").append(fournisseurs.size()).append("\n\n");

        if (!fournisseurs.isEmpty()) {
            double moyenneRate = fournisseurs.stream()
                    .mapToDouble(Fournisseur::getRate)
                    .average()
                    .orElse(0.0);

            rapport.append("Taux moyen: ").append(String.format("%.2f%%", moyenneRate)).append("\n\n");

            rapport.append("TOP 5 FOURNISSEURS:\n");
            List<Fournisseur> topFournisseurs = getTopFournisseurs(5);
            for (int i = 0; i < topFournisseurs.size(); i++) {
                Fournisseur f = topFournisseurs.get(i);
                double perf = fournisseurBD.calculerPerformance(f.getNumFournisseur());
                rapport.append(String.format("%d. %s - Taux: %.1f%% - Performance: %.2f\n",
                        i + 1, f.getNomFournisseur(), f.getRate(), perf));
            }
        }

        rapport.append("\n═══════════════════════════════════════\n");

        return rapport.toString();
    }

    // ============ VALIDATIONS ============

    private void validerFournisseur(Fournisseur fournisseur) throws IllegalArgumentException {
        if (fournisseur == null) {
            throw new IllegalArgumentException("Le fournisseur ne peut pas être null");
        }

        if (fournisseur.getNomFournisseur() == null || fournisseur.getNomFournisseur().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du fournisseur est obligatoire");
        }

        if (fournisseur.getTelephone() == null || fournisseur.getTelephone().trim().isEmpty()) {
            throw new IllegalArgumentException("Le téléphone est obligatoire");
        }

        if (fournisseur.getAdresseEmail() == null || fournisseur.getAdresseEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }

        // Validation basique de l'email
        if (!fournisseur.getAdresseEmail().contains("@")) {
            throw new IllegalArgumentException("Format d'email invalide");
        }

        if (fournisseur.getRate() < 0 || fournisseur.getRate() > 100) {
            throw new IllegalArgumentException("Le taux doit être entre 0 et 100");
        }
    }
}
