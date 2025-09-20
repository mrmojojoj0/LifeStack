package MyApp;
// import javax.swing.*;

import Authenticator.Login;

public class Main {
        public static void main(String[] args) {
        // SwingUtilities.invokeLater(() -> {
            // if (Files.exists(Paths.get(CREDENTIAL_FILE))) {
            //     Login loginFrame = new Login();
            //     loginFrame.showLogin();
            // } else {
            //     SignUp.showSignup();
            // }
            Login l = new Login();
            l.show();
        // });
    }

}
