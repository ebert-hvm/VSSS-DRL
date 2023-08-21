package Client;

public class Main {
    public static void clearTerminal() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            System.out.println("Error clearing terminal: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        MulticastReceiver mcReceiver = new MulticastReceiver();
        mcReceiver.start();
    }
}