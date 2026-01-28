package gestion;

import entite.Employe;
import entite.Personne;
import entite.CvEmployee;
import entitebd.EmployeBD;
import entitebd.PersonneBD;
import entitebd.CvEmployeeBD;
import exception.AuthentificationException;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Classe de gestion des employés
 */
public class GestionEmploye {
    private EmployeBD employeBD;
    private PersonneBD personneBD;
    private CvEmployeeBD cvBD;

    public GestionEmploye() {
        this.employeBD = new EmployeBD();
        this.personneBD = new PersonneBD();
        this.cvBD = new CvEmployeeBD();
    }

    /**
     * Ajouter un employé complet (Personne + Employé + CV optionnel)
     */
    public int ajouterEmployeComplet(Employe employe, CvEmployee cv)
            throws SQLException, IllegalArgumentException {

        // Validations
        validerEmploye(employe);

        // Vérifier que la personne n'existe pas déjà
        Personne personneExistante = personneBD.rechercherParNumCarte(employe.getNumCarteIdentite());
        if (personneExistante != null) {
            throw new IllegalArgumentException(
                    "Une personne avec le numéro de carte " + employe.getNumCarteIdentite() + " existe déjà!"
            );
        }

        // Vérifier que le CNSS est unique
        try {
            Employe existant = employeBD.rechercherParId(employe.getNumCNSS());
            if (existant != null) {
                throw new IllegalArgumentException(
                        "Un employé avec le CNSS " + employe.getNumCNSS() + " existe déjà!"
                );
            }
        } catch (SQLException e) {
            // OK, n'existe pas
        }

        // 1. Ajouter la personne
        Personne personne = new Personne();
        personne.setNumCarteIdentite(employe.getNumCarteIdentite());
        personne.setNom(employe.getNom());
        personne.setPrenom(employe.getPrenom());
        personne.setAge(employe.getAge());
        personne.setAdresse(employe.getAdresse());
        personne.setAdresseMail(employe.getAdresseMail());
        personne.setTelephone(employe.getTelephone());

        int numCarteId = personneBD.ajouter(personne);

        if (numCarteId <= 0) {
            throw new SQLException("Échec de l'ajout de la personne");
        }

        // 2. Ajouter l'employé
        try {
            int numCarteEmp = employeBD.ajouter(employe);

            // 3. Ajouter le CV si fourni
            if (cv != null && numCarteEmp > 0) {
                cv.setNumEmployee(numCarteEmp);
                cvBD.ajouter(cv);
            }

            System.out.println(" Employé ajouté avec succès (Carte Emp: " + numCarteEmp + ")");
            return numCarteEmp;

        } catch (SQLException e) {
            // Rollback: supprimer la personne
            personneBD.supprimer(numCarteId);
            throw e;
        }
    }

    /**
     * Modifier un employé
     */
    public boolean modifierEmploye(Employe employe)
            throws SQLException, IllegalArgumentException {

        validerEmploye(employe);

        // Vérifier que l'employé existe
        Employe existant = employeBD.rechercherParId(employe.getNumCarteEmp());
        if (existant == null) {
            throw new IllegalArgumentException(
                    "Employé avec carte #" + employe.getNumCarteEmp() + " introuvable"
            );
        }

        // Modifier l'employé
        boolean empModifie = employeBD.modifier(employe);

        // Modifier les infos personnelles
        if (empModifie) {
            Personne personne = new Personne();
            personne.setNumCarteIdentite(employe.getNumCarteIdentite());
            personne.setNom(employe.getNom());
            personne.setPrenom(employe.getPrenom());
            personne.setAge(employe.getAge());
            personne.setAdresse(employe.getAdresse());
            personne.setAdresseMail(employe.getAdresseMail());
            personne.setTelephone(employe.getTelephone());

            personneBD.modifier(personne);
        }

        return empModifie;
    }

    /**
     * Supprimer un employé
     */
    public boolean supprimerEmploye(int numCarteEmp)
            throws SQLException {

        Employe employe = employeBD.rechercherParId(numCarteEmp);
        if (employe == null) {
            throw new IllegalArgumentException("Employé introuvable");
        }

        // Supprimer le CV s'il existe
        if (cvBD.existe(numCarteEmp)) {
            cvBD.supprimer(numCarteEmp);
        }

        // Supprimer l'employé
        boolean deleted = employeBD.supprimer(numCarteEmp);

        // Supprimer la personne
        if (deleted) {
            personneBD.supprimer(employe.getNumCarteIdentite());
        }

        return deleted;
    }

    /**
     * Authentifier un employé
     */
    public Employe authentifier(int numCNSS, String motDePasse)
            throws SQLException, AuthentificationException {

        Employe employe = employeBD.authentifier(numCNSS, motDePasse);

        if (employe == null) {
            throw new AuthentificationException(
                    "Identifiants incorrects pour CNSS: " + numCNSS,
                    String.valueOf(numCNSS)
            );
        }

        return employe;
    }

    /**
     * Gérer le salaire d'un employé
     */
    public boolean modifierSalaire(int numCarteEmp, double nouveauSalaire)
            throws SQLException, IllegalArgumentException {

        if (nouveauSalaire <= 0) {
            throw new IllegalArgumentException("Le salaire doit être positif!");
        }

        Employe employe = employeBD.rechercherParId(numCarteEmp);
        if (employe == null) {
            throw new IllegalArgumentException("Employé introuvable");
        }

        employe.setSalaire(nouveauSalaire);
        return employeBD.modifier(employe);
    }

    /**
     * Gérer le CV d'un employé
     */
    public boolean ajouterOuModifierCV(int numCarteEmp, CvEmployee cv)
            throws SQLException {

        cv.setNumEmployee(numCarteEmp);

        if (cvBD.existe(numCarteEmp)) {
            return cvBD.modifier(cv);
        } else {
            return cvBD.ajouter(cv);
        }
    }

    /**
     * Obtenir le CV d'un employé
     */
    public CvEmployee obtenirCV(int numCarteEmp) throws SQLException {
        return cvBD.rechercherParNumEmployee(numCarteEmp);
    }

    /**
     * Lister tous les employés
     */
    public List<Employe> listerTousEmployes() throws SQLException {
        return employeBD.listerTous();
    }

    /**
     * Obtenir les statistiques des employés
     */
    public StatistiquesEmployes obtenirStatistiques() throws SQLException {
        List<Employe> employes = employeBD.listerTous();

        int nbTotal = employes.size();
        int nbAdmins = 0;
        double masseSalariale = 0.0;
        double salaireMoyen = 0.0;

        for (Employe emp : employes) {
            if (emp.admin()) {
                nbAdmins++;
            }
            masseSalariale += emp.getSalaire();
        }

        if (nbTotal > 0) {
            salaireMoyen = masseSalariale / nbTotal;
        }

        return new StatistiquesEmployes(nbTotal, nbAdmins, masseSalariale, salaireMoyen);
    }

    /**
     * Validations
     */
    private void validerEmploye(Employe employe) throws IllegalArgumentException {
        if (employe == null) {
            throw new IllegalArgumentException("L'employé ne peut pas être null");
        }

        if (employe.getNom() == null || employe.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        if (employe.getPrenom() == null || employe.getPrenom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }

        if (employe.getNumCNSS() <= 0) {
            throw new IllegalArgumentException("Le numéro CNSS est obligatoire");
        }

        if (employe.getNumCarteIdentite() <= 0) {
            throw new IllegalArgumentException("Le numéro de carte d'identité est obligatoire");
        }

        if (employe.getSalaire() <= 0) {
            throw new IllegalArgumentException("Le salaire doit être positif");
        }

        if (employe.getMotDePasse() == null || employe.getMotDePasse().length() < 4) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 4 caractères");
        }

        if (employe.getPoste() == null || employe.getPoste().trim().isEmpty()) {
            throw new IllegalArgumentException("Le poste est obligatoire");
        }

        if (employe.getDateRejoindTravail() == null) {
            throw new IllegalArgumentException("La date de recrutement est obligatoire");
        }

        if (employe.getDateRejoindTravail().after(new Date())) {
            throw new IllegalArgumentException("La date de recrutement ne peut pas être dans le futur");
        }

        if (employe.getNbJoursParSemaine() < 1 || employe.getNbJoursParSemaine() > 7) {
            throw new IllegalArgumentException("Le nombre de jours par semaine doit être entre 1 et 7");
        }
    }

    /**
     * Classe interne pour les statistiques
     */
    public static class StatistiquesEmployes {
        private int nbTotal;
        private int nbAdmins;
        private double masseSalariale;
        private double salaireMoyen;

        public StatistiquesEmployes(int nbTotal, int nbAdmins, double masseSalariale, double salaireMoyen) {
            this.nbTotal = nbTotal;
            this.nbAdmins = nbAdmins;
            this.masseSalariale = masseSalariale;
            this.salaireMoyen = salaireMoyen;
        }

        public int getNbTotal() { return nbTotal; }
        public int getNbAdmins() { return nbAdmins; }
        public double getMasseSalariale() { return masseSalariale; }
        public double getSalaireMoyen() { return salaireMoyen; }
    }
}