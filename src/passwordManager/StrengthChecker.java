package passwordManager;

public class StrengthChecker {
    protected static String getPasswordStrength(String password) {
        int score = 0;
        if (password.length() >= 8)
            score++;
        if (password.matches(".*[A-Z].*"))
            score++;
        if (password.matches(".*[a-z].*"))
            score++;
        if (password.matches(".*\\d.*"))
            score++;
        if (password.matches(".*[^a-zA-Z0-9].*"))
            score++;

        if (score <= 2)
            return "Weak";
        else if (score <= 4)
            return "Medium";
        else
            return "Strong";
    }
}
