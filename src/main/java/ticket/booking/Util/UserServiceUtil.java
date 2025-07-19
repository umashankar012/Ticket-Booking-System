package ticket.booking.Util;

public class UserServiceUtil {

    public static String hashPassword(String password) {
        // ⚠️ Dummy hash function — replace with real encryption in production
        return "hashed_" + password;
    }

    public static boolean checkPassword(String plain, String hashed) {
        return hashPassword(plain).equals(hashed);
    }
}
