package gestion;

import entitebd.CommandeBD;
import entitebd.VoieCommandeBD;
import entitebd.RapportAnalyseBD;
import entite.Commande;
import entite.VoieCommande;
import entite.RapportAnalyse;
import exception.ProduitNonTrouveException;
import java.sql.SQLException;
import java.util.ArrayList;

public class GestionCommande {
    private CommandeBD commandeBD;
    private VoieCommandeBD voieCommandeBD;
    private GestionStock gestionStock;
    private RapportAnalyseBD rapportBD;

    public GestionCommande() {
        this.commandeBD = new CommandeBD();
        this.voieCommandeBD = new VoieCommandeBD();
        this.gestionStock = new GestionStock();
        this.rapportBD = new RapportAnalyseBD();
    }

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
            // ✅ CORRECTION: Calculer le prix total avant d'ajouter
            ligne.setPrixTotalVoieCommande();
            voieCommandeBD.ajouterLigne(ligne);
        }

        System.out.println("✅ Commande #" + numCommande + " créée avec " + lignes.size() + " lignes");
        return numCommande;
    }

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
            // ✅ CORRECTION: Calculer le prix total avant modification
            ligne.setPrixTotalVoieCommande();
            voieCommandeBD.modifierLigne(ligne);
        }

        double total = calculerTotalCommande(nouvellesLignes);
        commande.setMontantTotalCommande(total);
        commandeBD.modifierCommande(commande);

        System.out.println("✅ Commande #" + numCommande + " modifiée");
    }

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

    public void receptionnerCommande(int numCommande, int numCarteEmp)
            throws SQLException, ProduitNonTrouveException {

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

        ArrayList<VoieCommande> lignes = voieCommandeBD.getLignesParCommande(numCommande);

        for (VoieCommande ligne : lignes) {
            gestionStock.augmenterStock(ligne.getRefMedicament(), ligne.getQuantite());
        }

        commande.setStatut("Reçue");
        commandeBD.modifierCommande(commande);

        mettreAJourChiffreAffaires(commande.getMontantTotalCommande(), numCarteEmp);

        System.out.println("✅ Commande #" + numCommande + " réceptionnée et stock mis à jour");
    }

    public BilanCommande obtenirBilanCommande(int numCommande) throws SQLException {
        Commande commande = commandeBD.getCommandeById(numCommande);
        if (commande == null) {
            throw new IllegalArgumentException("Commande #" + numCommande + " introuvable");
        }

        ArrayList<VoieCommande> lignes = voieCommandeBD.getLignesParCommande(numCommande);
        return new BilanCommande(commande, lignes);
    }

    public ArrayList<Commande> listerToutesCommandes() throws SQLException {
        return commandeBD.afficherToutesCommandes();
    }

    public ArrayList<VoieCommande> getLignesCommande(int numCommande) throws SQLException {
        return voieCommandeBD.getLignesParCommande(numCommande);
    }

    private double calculerTotalCommande(ArrayList<VoieCommande> lignes) {
        double total = 0;
        for (VoieCommande ligne : lignes) {
            total += ligne.calculerTotal();
        }
        return total;
    }

    private void mettreAJourChiffreAffaires(double montant, int numCarteEmp) throws SQLException {
        try {
            RapportAnalyse rapport = rapportBD.getRapportByEmploye(numCarteEmp);
            if (rapport != null) {
                rapport.setChiffreAffaire(rapport.getChiffreAffaire() + montant);
                rapportBD.modifier(rapport);
            } else {
                // ✅ Génération d'un ID unique pour le nouveau rapport
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