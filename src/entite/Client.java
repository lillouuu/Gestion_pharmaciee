package entite;
import java.util.Date;

public class Client extends Personne {
    private int numClient;
    private int pointFidelite;
    private Date dernierDateAchat;
    private String codeCnam;
    public int getPointFidelite() {
        return pointFidelite;
    }
    public void setPointFidelite(int pointFidelite) {
        this.pointFidelite = pointFidelite;
    }
    public Date getDernierDateAchat() {
        return dernierDateAchat;
    }
    public void setDernierDateAchat(Date dernierDateAchat) {
        this.dernierDateAchat = dernierDateAchat;
    }
    public String getCodeCnam() {
        return codeCnam;
    }
    public void setCodeCnam(String codeCnam) {
        this.codeCnam = codeCnam;
    }
    public void ajoutPoints (int point) {
        pointFidelite=pointFidelite+point ;//Cette fonction permet l’ajout de certains points en fonction de l’achat effectué

    }
    public void utilisepoints(int point) {
        if (pointFidelite> point) {
            pointFidelite=pointFidelite-point ;
            System.out.println("La soustraction des points a été effectuée avec succès");}
        else {
            System.out.println("Les points de fidélité de ce client sont insuffisants pour effectuer cette opération.");

        }

    }
    public int getNumClient() {
        return numClient;
    }
    public void setNumClient(int numClient) {
        this.numClient = numClient;
    }



}
