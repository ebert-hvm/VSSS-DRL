package Client;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

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
        Communication communication = new Communication();
        AI ai = new AI(communication);
        Thread[] threads = {
                new Thread(communication::startReceiving),
                new Thread(communication::startSending),
                new Thread(ai::start)
        };
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Thread thread : threads) {
            thread.interrupt();
        }
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}