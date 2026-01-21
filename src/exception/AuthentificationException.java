package exception;

/**
 exception levée lors d'un échec d'authentification
 identifiant ou mot de passe incorrect)
 */
public class AuthentificationException extends Exception {
    private String numCNSS;

    public AuthentificationException(String numCNSS) {
        super("Échec de l'authentification pour l'employé avec CNSS: " + numCNSS);
        this.numCNSS = numCNSS;
    }

    public AuthentificationException(String message, String numCNSS) {
        super(message);
        this.numCNSS = numCNSS;
    }

    public String getNumCNSS() {
        return numCNSS;
    }
}
