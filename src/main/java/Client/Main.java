package Client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.InputStream;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static JsonNode parameters;

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