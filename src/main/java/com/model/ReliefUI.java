package com.model;

   import java.util.ArrayList;
   import java.util.Calendar;
   import java.util.GregorianCalendar;
   import java.util.UUID;

   /**
    * Text-based UI for the hurricane relief app.
    */
   public class ReliefUI {

    private static int passed = 0;
    private static int failed = 0;

       /**
        * Loads the shelters and displays each one.
        *
        * @param args not used
        */
       public static void main(String[] args) {
           ArrayList<Shelter> shelters = DataLoader.getCachedShelters();
           System.out.println("Shelters:");
           for (Shelter shelter : shelters) {
               System.out.println(shelter);
           }
            testAccountLoginLogout();
       }

    /**
     * Tests createAccount, login and logout. Uses a unique username each run so a
     * test account saved on a previous run does not make the duplicate check fail.
     */
    private static void testAccountLoginLogout() {
        System.out.println("\nAccount tests:");
        ReliefApplication app = ReliefApplication.getInstance();
 
        String username = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        String password = "testpass123";
 
        // create account
        User created = app.createAccount(username, password, "Test", "User",
                new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime(),
                "1 Test St, Columbia, SC 29201", "test@relief.org", "803-555-0100",
                USERTYPE.VOLUNTEER);
        check("createAccount returns a user", created != null);
 
        User duplicate = app.createAccount(username, password, "Test", "User",
                new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime(),
                "1 Test St, Columbia, SC 29201", "test@relief.org", "803-555-0100",
                USERTYPE.VOLUNTEER);
        check("createAccount rejects a duplicate username", duplicate == null);
 
        // login
        check("not logged in before login", !app.isLoggedIn());
 
        check("login fails with wrong password", app.login(username, "wrongpass") == null);
        check("still not logged in after failed login", !app.isLoggedIn());
 
        check("login fails with unknown username", app.login("nobody_here", password) == null);
 
        User loggedIn = app.login(username, password);
        check("login succeeds with correct password", loggedIn != null);
        check("logged in after login", app.isLoggedIn());
 
        // logout
        check("logout returns true when logged in", app.logout());
        check("not logged in after logout", !app.isLoggedIn());
        check("logout returns false when nobody is logged in", !app.logout());
 
        // login again after logging out
        check("login works again after logout", app.login(username, password) != null);
        app.logout();
 
        System.out.println("\n" + passed + " passed, " + failed + " failed");
    }
 
    private static void check(String description, boolean result) {
        if (result) {
            passed++;
            System.out.println("  PASS: " + description);
        } else {
            failed++;
            System.out.println("  FAIL: " + description);
        }
    }
   }