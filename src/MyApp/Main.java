package MyApp;

import Authenticator.Login;

public class Main {
    public static void main(String[] args) {

        // Login constructor will show the appropriate UI (SignUp or Login).
        // Do not call show() on the Login instance here because
        // when SignUp is displayed the Login.frame remains null and
        // calling show() would cause a NullPointerException.
        new Login();

    }

}
