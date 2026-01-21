package entitebd;


import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        try {
            Connection con = ConnectionBD.getConnection();
            if (con != null) {
                System.out.println("✅ Connexion réussie !");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur de connexion : " + e.getMessage());
        }
    }
}
