package gestion;

import entitebd.CommandeBD;
import entitebd.VoieCommandeBD;
import entitebd.RapportAnalyseBD;
import entite.Commande;
import entite.VoieCommande;
import entite.RapportAnalyse;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Gestion des commandes SANS mise à jour automatique du stock
 * Le stock sera géré manuellement lors de la réception dans l'interface
 */
public class GestionCommande {
    private CommandeBD commandeBD;
    private VoieCommandeBD voieCommandeBD;
    private RapportAnalyseBD rapportBD;

    public GestionCommande() {
        this.commandeBD = new CommandeBD();
        this.voieCommandeBD = new VoieCommandeBD();
        this.rapportBD = new RapportAnalyseBD();
    }

    /**
     * Créer une nouvelle commande
     */
    public int creerCommande(Commande commande, ArrayList<VoieCommande> lignes)
            throws SQLException, IllegalArgumentException {

        if (commande == null) {
            throw new IllegalArgumentException("La commande ne peut pas être null");
        }
        if (lignes == null || lignes.isEmpty()) {
            throw new IllegalArgumentException("Une commande doit avoir au moins une ligne");
        }

        commande.setStatut("En attente");

        double total = calculerTotalCommande(lignes);
        commande.setMontantTotalCommande(total);

        commandeBD.ajouterCommande(commande);
        int numCommande = commande.getNumCommande();

        for (VoieCommande ligne : lignes) {
            ligne.setNumCommande(numCommande);
            ligne.setPrixTotalVoieCommande();
            voieCommandeBD.ajouterLigne(ligne);
        }

        System.out.println("✅ Commande #" + numCommande + " créée avec " + lignes.size() + " lignes");
        return numCommande;
    }

    /**
     * Modifier une commande existante
     */
    public void modifierCommande(int numCommande, ArrayList<VoieCommande> nouvellesLignes)
            throws SQLException, IllegalArgumentException {

        Commande commande = commandeBD.getCommandeById(numCommande);
        if (commande == null) {
            throw new IllegalArgumentException("Commande #" + numCommande + " introuvable");
        }

        if ("Reçue".equals(commande.getStatut()) || "Annulée".equals(commande.getStatut())) {
            throw new IllegalArgumentException("Impossible de modifier une commande " + commande.getStatut());
        }

        for (VoieCommande ligne : nouvellesLignes) {
            ligne.setPrixTotalVoieCommande();
            voieCommandeBD.modifierLigne(ligne);
        }

        double total = calculerTotalCommande(nouvellesLignes);
        commande.setMontantTotalCommande(total);
        commandeBD.modifierCommande(commande);

        System.out.println("✅ Commande #" + numCommande + " modifiée");
    }

    /**
     * Annuler une commande
     */
    public void annulerCommande(int numCommande) throws SQLException {

        Commande commande = commandeBD.getCommandeById(numCommande);
        if (commande == null) {
            throw new IllegalArgumentException("Commande #" + numCommande + " introuvable");
        }

        if ("Reçue".equals(commande.getStatut())) {
            throw new IllegalArgumentException("Impossible d'annuler une commande déjà reçue");
        }

        ArrayList<VoieCommande> lignes = voieCommandeBD.getLignesParCommande(numCommande);
        for (VoieCommande ligne : lignes) {
            voieCommandeBD.supprimerLigne(ligne.getIdLigneCommande());
        }

        commande.setStatut("Annulée");
        commande.setMontantTotalCommande(0);
        commandeBD.modifierCommande(commande);

        System.out.println("✅ Commande #" + numCommande + " annulée");
    }

    /**
     * Marquer une commande comme reçue
     * NOTE: Cette méthode ne met PLUS à jour le stock automatiquement
     * Le stock doit être géré manuellement dans l'interface de réception
     */
    public void marquerCommeRecue(int numCommande, int numCarteEmp)
            throws SQLException {

        Commande commande = commandeBD.getCommandeById(numCommande);
        if (commande == null) {
            throw new IllegalArgumentException("Commande #" + numCommande + " introuvable");
        }

        if ("Reçue".equals(commande.getStatut())) {
            throw new IllegalArgumentException("Commande déjà réceptionnée");
        }
        if ("Annulée".equals(commande.getStatut())) {
            throw new IllegalArgumentException("Impossible de réceptionner une commande annulée");
        }

        // Changer le statut uniquement
        commande.setStatut("Reçue");
        commandeBD.modifierCommande(commande);

        // Mettre à jour le chiffre d'affaires
        mettreAJourChiffreAffaires(commande.getMontantTotalCommande(), numCarteEmp);

        System.out.println("✅ Commande #" + numCommande + " marquée comme reçue");
        System.out.println("⚠️ Note: Le stock doit être mis à jour manuellement dans l'interface");
    }

    /**
     * ANCIENNE MÉTHODE - Conservée pour compatibilité mais DÉCONSEILLÉE
     * @deprecated Utilisez plutôt la réception manuelle dans l'interface
     */
    @Deprecated
    public void receptionnerCommande(int numCommande, int numCarteEmp)
            throws SQLException {
        System.out.println("⚠️ ATTENTION: Utilisation de la méthode dépréciée receptionnerCommande()");
        System.out.println("⚠️ Cette méthode ne met plus à jour le stock automatiquement");
        System.out.println("⚠️ Utilisez l'interface de réception pour gérer le stock manuellement");
        
        marquerCommeRecue(numCommande, numCarteEmp);
    }

    /**
     * Obtenir le bilan d'une commande
     */
    public BilanCommande obtenirBilanCommande(int numCommande) throws SQLException {
        Commande commande = commandeBD.getCommandeById(numCommande);
        if (commande == null) {
            throw new IllegalArgumentException("Commande #" + numCommande + " introuvable");
        }

        ArrayList<VoieCommande> lignes = voieCommandeBD.getLignesParCommande(numCommande);
        return new BilanCommande(commande, lignes);
    }

    /**
     * Lister toutes les commandes
     */
    public ArrayList<Commande> listerToutesCommandes() throws SQLException {
        return commandeBD.afficherToutesCommandes();
    }

    /**
     * Obtenir les lignes d'une commande
     */
    public ArrayList<VoieCommande> getLignesCommande(int numCommande) throws SQLException {
        return voieCommandeBD.getLignesParCommande(numCommande);
    }

    /**
     * Calculer le total d'une commande
     */
    private double calculerTotalCommande(ArrayList<VoieCommande> lignes) {
        double total = 0;
        for (VoieCommande ligne : lignes) {
            total += ligne.calculerTotal();
        }
        return total;
    }

    /**
     * Mettre à jour le chiffre d'affaires
     */
    private void mettreAJourChiffreAffaires(double montant, int numCarteEmp) throws SQLException {
        try {
            RapportAnalyse rapport = rapportBD.getRapportByEmploye(numCarteEmp);
            if (rapport != null) {
                rapport.setChiffreAffaire(rapport.getChiffreAffaire() + montant);
                rapportBD.modifier(rapport);
            } else {
                rapport = new RapportAnalyse();
                rapport.setId((int)(System.currentTimeMillis() % 1000000));
                rapport.setNumCarteEmp(numCarteEmp);
                rapport.setChiffreAffaire(montant);
                rapportBD.ajouter(rapport);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Avertissement: Impossible de mettre à jour le chiffre d'affaires");
            e.printStackTrace();
        }
    }

    /**
     * Classe interne pour le bilan d'une commande
     */
    public static class BilanCommande {
        private Commande commande;
        private ArrayList<VoieCommande> lignes;

        public BilanCommande(Commande commande, ArrayList<VoieCommande> lignes) {
            this.commande = commande;
            this.lignes = lignes;
        }

        public Commande getCommande() {
            return commande;
        }

        public ArrayList<VoieCommande> getLignes() {
            return lignes;
        }

        public double getTotal() {
            return commande.getMontantTotalCommande();
        }

        public int getNombreLignes() {
            return lignes.size();
        }
    }
}
