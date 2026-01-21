package entite;

public class StockMedicament {
    private int numStock;
    private int refMedicament;
    private int quantiteProduit;
    private double prixAchat;
    private double prixVente;
    private int seuilMin;
    public int getNumStock() {
        return numStock;
    }
    public void setNumStock(int numStock) {
        this.numStock = numStock;
    }
    public int getRefMedicament() {
        return refMedicament;
    }
    public void setRefMedicament(int i) {
        this.refMedicament = i;
    }
    public double getPrixAchat() {
        return prixAchat;
    }
    public void setPrixAchat(double prixAchat) {
        this.prixAchat = prixAchat;
    }
    public int  getQuantiteProduit() {
        return quantiteProduit;
    }
    public void setQuantiteProduit(int quantiteProduit) {
        this.quantiteProduit = quantiteProduit;
    }
    public double getPrixVente() {
        return prixVente;
    }
    public void setPrixVente(double prixVente) {
        this.prixVente = prixVente;
    }
    public int getSeuilMin() {
        return seuilMin;
    }
    public void setSeuilMin(int seuilMin) {
        this.seuilMin = seuilMin;
    }
    public void ajouterStock(double quantite) {
        this.quantiteProduit += quantite;
    }
    public boolean Alerte() {
        return quantiteProduit <= seuilMin;
    }


}