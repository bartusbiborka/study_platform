package JdbcExample;

public class Main {
    public static void main(String[] args) {


        LoginPage loginPage = new LoginPage(null);

        if (loginPage.authenticatedUser != null) {
            System.out.println("User logat " + loginPage.authenticatedUser);
        } else {
            System.out.println("Logare nereusita.");
        }
    }
}
