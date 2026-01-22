package gestion;

import entite.Client;
import entitebd.ClientBD;
import entitebd.PersonneBD;
import entite.Personne;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class GestionClient {
    private ClientBD clientBD;
    private PersonneBD personneBD;

    public GestionClient() {
        this.clientBD = new ClientBD();
        this.personneBD = new PersonneBD();
    }

    /**
     * Ajouter un client avec ses informations personnelles
     */
    public int ajouterClient(Client client) throws SQLException, IllegalArgumentException {
        // Validations
        validerClient(client);

        // Vérifier si la personne existe déjà avec ce numéro de carte d'identité
        Personne personneExistante = personneBD.rechercherParNumCarte(client.getNumCarteIdentite());

        if (personneExistante == null) {
            // Créer d'abord la personne
            int numCarte = personneBD.ajouter(client);
            client.setNumCarteIdentite(numCarte);
        }

        // Ajouter le client
        int numClient = clientBD.ajouter(client);

        System.out.println("✓ Client ajouté avec succès (ID: " + numClient + ")");
        return numClient;
    }

    /**
     * Modifier un client
     */
    public boolean modifierClient(Client client) throws SQLException, IllegalArgumentException {
        validerClient(client);

        // Mettre à jour les informations personnelles
        personneBD.modifier(client);

        // Mettre à jour les informations spécifiques au client
        return clientBD.mettreAJourPoints(client.getNumClient(), client.getPointFidelite());
    }

    /**
     * Rechercher un client par ID
     */
    public Client rechercherParId(int numClient) throws SQLException {
        return clientBD.rechercherParId(numClient);
    }

    /**
     * Rechercher des clients par nom
     */
    public List<Client> rechercherParNom(String nom) throws SQLException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de recherche ne peut pas être vide");
        }
        return clientBD.rechercherParNom(nom);
    }

    /**
     * Rechercher un client par code CNAM
     */
    public Client rechercherParCodeCnam(String codeCnam) throws SQLException {
        if (codeCnam == null || codeCnam.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code CNAM ne peut pas être vide");
        }
        return clientBD.rechercherParCodeCnam(codeCnam);
    }

    /**
     * Lister tous les clients
     */
    public List<Client> listerTous() throws SQLException {
        return clientBD.listerTous();
    }

    /**
     * Ajouter des points de fidélité
     */
    public void ajouterPoints(int numClient, int points) throws SQLException {
        if (points <= 0) {
            throw new IllegalArgumentException("Le nombre de points doit être positif");
        }

        Client client = clientBD.rechercherParId(numClient);
        if (client == null) {
            throw new IllegalArgumentException("Client introuvable: " + numClient);
        }

        int nouveauxPoints = client.getPointFidelite() + points;
        clientBD.mettreAJourPoints(numClient, nouveauxPoints);

        System.out.println("✓ " + points + " points ajoutés au client #" + numClient);
    }

    /**
     * Utiliser des points de fidélité
     */
    public void utiliserPoints(int numClient, int points) throws SQLException {
        if (points <= 0) {
            throw new IllegalArgumentException("Le nombre de points doit être positif");
        }

        Client client = clientBD.rechercherParId(numClient);
        if (client == null) {
            throw new IllegalArgumentException("Client introuvable: " + numClient);
        }

        if (client.getPointFidelite() < points) {
            throw new IllegalArgumentException(
                    "Points insuffisants! Client a " + client.getPointFidelite() + " points, " +
                            points + " demandés"
            );
        }

        int nouveauxPoints = client.getPointFidelite() - points;
        clientBD.mettreAJourPoints(numClient, nouveauxPoints);

        System.out.println("✓ " + points + " points utilisés pour le client #" + numClient);
    }

    /**
     * Mettre à jour la date du dernier achat
     */
    public void mettreAJourDernierAchat(int numClient, Date dateAchat) throws SQLException {
        Client client = clientBD.rechercherParId(numClient);
        if (client == null) {
            throw new IllegalArgumentException("Client introuvable: " + numClient);
        }

        client.setDernierDateAchat(dateAchat);
        // TODO: Ajouter méthode dans ClientBD pour mettre à jour la date
    }

    /**
     * Calculer la réduction basée sur les points
     */
    public double calculerReduction(int points, double tauxConversion) {
        // Par défaut: 1 point = 0.1 DT de réduction
        if (tauxConversion <= 0) {
            tauxConversion = 0.1;
        }
        return points * tauxConversion;
    }

    /**
     * Obtenir les meilleurs clients (par points)
     */
    public List<Client> getTopClients(int limit) throws SQLException {
        List<Client> clients = clientBD.listerTous();

        // Trier par points de fidélité décroissant
        clients.sort((c1, c2) -> Integer.compare(c2.getPointFidelite(), c1.getPointFidelite()));

        // Limiter le nombre de résultats
        if (limit > 0 && clients.size() > limit) {
            return clients.subList(0, limit);
        }

        return clients;
    }

    /**
     * Générer un rapport sur les clients
     */
    public String genererRapport() throws SQLException {
        List<Client> clients = clientBD.listerTous();

        int totalClients = clients.size();
        int totalPoints = clients.stream().mapToInt(Client::getPointFidelite).sum();
        double moyennePoints = totalClients > 0 ? (double) totalPoints / totalClients : 0;

        StringBuilder rapport = new StringBuilder();
        rapport.append("═══════════════════════════════════════\n");
        rapport.append("       RAPPORT CLIENTÈLE\n");
        rapport.append("═══════════════════════════════════════\n\n");
        rapport.append("Nombre total de clients: ").append(totalClients).append("\n");
        rapport.append("Total points fidélité: ").append(totalPoints).append("\n");
        rapport.append("Moyenne points/client: ").append(String.format("%.1f", moyennePoints)).append("\n\n");

        rapport.append("TOP 5 CLIENTS:\n");
        List<Client> topClients = getTopClients(5);
        for (int i = 0; i < topClients.size(); i++) {
            Client c = topClients.get(i);
            rapport.append(String.format("%d. %s %s - %d points\n",
                    i + 1, c.getPrenom(), c.getNom(), c.getPointFidelite()));
        }

        rapport.append("\n═══════════════════════════════════════\n");

        return rapport.toString();
    }

    // ============ VALIDATIONS ============

    private void validerClient(Client client) throws IllegalArgumentException {
        if (client == null) {
            throw new IllegalArgumentException("Le client ne peut pas être null");
        }

        // Validations de Personne
        if (client.getNom() == null || client.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        if (client.getPrenom() == null || client.getPrenom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }

        if (client.getNumCarteIdentite() <= 0) {
            throw new IllegalArgumentException("Le numéro de carte d'identité est invalide");
        }

        if (client.getTelephone() == null || client.getTelephone().trim().isEmpty()) {
            throw new IllegalArgumentException("Le téléphone est obligatoire");
        }

        // Validations spécifiques au client
        if (client.getPointFidelite() < 0) {
            throw new IllegalArgumentException("Les points de fidélité ne peuvent pas être négatifs");
        }
    }
}