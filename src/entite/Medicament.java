package entite;
import java.util.Date;


public class Medicament {

    private int refMedicament;
    private String nom;
    private int numFournisseur; // numFournisseur
    private String descriptio;
    private Date dateFabrication;
    private Date dateExpiration;
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public int  getRefMedicament() {
        return refMedicament;
    }
    public void setRefMedicament(int refMedicament) {
        this.refMedicament = refMedicament;
    }
    public int getNumFournisseur() {
        return numFournisseur;
    }
    public void setNumFournisseur(int numFournisseur) {
        this.numFournisseur = numFournisseur;
    }

    public String getDescriptio() {
        return descriptio;
    }
    public void setDescriptio(String description) {
        this.descriptio = description;
    }
    public Date getDateFabrication() {
        return dateFabrication;
    }
    public void setDateFabrication(Date dateFabrication) {
        this.dateFabrication = dateFabrication;
    }
    public Date getDateExpiration() {
        return dateExpiration;
    }
    public void setDateExpiration(Date dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
    public boolean estPerime() {
        if (dateExpiration == null) return false;
        return new Date().after(dateExpiration);
    }
}