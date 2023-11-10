package Client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.InputStream;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class contains utility methods and the main entry point of the client
 * application.
 */
public class Main {
    /**
     * Clamps a value between a minimum and a maximum.
     *
     * @param value The value to be clamped.
     * @param min   The minimum value to clamp to.
     * @param max   The maximum value to clamp to.
     * @return The clamped value.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Rounds a double value to a specified number of decimal places.
     *
     * @param value  The value to be rounded.
     * @param places The number of decimal places to round to.
     * @return The rounded value.
     * @throws IllegalArgumentException if places is negative.
     */
    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException("Decimal places cannot be negative");

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Stores JSON parameters used by the application.
     */
    public static JsonNode parameters;

    /**
     * Clears the terminal screen based on the operating system.
     * Note: This method may not work on all systems.
     */
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

    /**
     * The main entry point of the client application.
     *
     * @param args Command-line arguments (not used in this application).
     */
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String resourcePath = "parameters.json";
            ClassLoader classLoader = Main.class.getClassLoader();
            URL resourceUrl = classLoader.getResource(resourcePath);
            InputStream inputStream = resourceUrl.openStream();
            parameters = objectMapper.readTree(inputStream);
            AI ai = new AI();
            ai.start();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
