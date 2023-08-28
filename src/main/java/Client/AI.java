package Client;

import java.util.ArrayList;

import Protobuf.Command;

public class AI {
    private Communication communication;
    private Navigation navigation;
    private EnvironmentState environmentState;
    private SharedObject<double[][]> blueVelocity, yellowVelocity;
    private SharedObject<double[][]> blueDestiny, yellowDestiny;

    public AI() {
        this.communication = new Communication();
        this.navigation = new Navigation();
        this.environmentState = new EnvironmentState();
        blueVelocity = new SharedObject<double[][]>(new double[3][2]);
        yellowVelocity = new SharedObject<double[][]>(new double[3][2]);
        blueDestiny = new SharedObject<double[][]>(new double[3][3]);
        yellowDestiny = new SharedObject<double[][]>(new double[3][3]);
    }

    private void executeNavigation() {
        double[][] blueVelocity = new double[3][2];
        while (true) {
            try {
                Thread.sleep(Navigation.framePeriod);
            } catch (Exception ex) {
            }
            blueVelocity[0] = navigation.execute(environmentState, blueDestiny.Get()[0]);
            this.blueVelocity.Set(blueVelocity);
            // yellowVelocity.Set(command[1]);
        }
    }

    private void executeCommand() {
        long time = System.currentTimeMillis() - 5001;
        boolean replace;
        while (true) {
            try {
                Thread.sleep(80);
            } catch (Exception e) {
            }
            try {
                replace = communication.replace.Get();
                if (replace && System.currentTimeMillis() - time > 500) {
                    communication.replace.Set(false);
                    time = System.currentTimeMillis();
                } else if (!replace && System.currentTimeMillis() - time > 5000) {
                    communication.replace.Set(true);
                    time = System.currentTimeMillis();
                }
                ArrayList<Protobuf.Command> commands = new ArrayList<Protobuf.Command>();
                double[][] blueVelocity = this.blueVelocity.Get();
                double[][] yellowVelocity = this.yellowVelocity.Get();
                for (int i = 0; i < 3; i++) {
                    commands.add(Command.newBuilder().setId(i).setYellowteam(false).setWheelLeft(blueVelocity[i][0])
                            .setWheelRight(blueVelocity[i][1]).build());
                    commands.add(Command.newBuilder().setId(i).setYellowteam(true).setWheelLeft(yellowVelocity[i][0])
                            .setWheelRight(yellowVelocity[i][1]).build());
                }
                communication.commands.Set(commands);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private void printEnvironmentState() {
        while (true) {
            try {
                Thread.sleep(80);
                // Main.clearTerminal();
                // environmentState.printState();
            } catch (Exception ex) {

            }
        }
    }

    private void startAI() {
        double[][] blueDestiny = new double[3][3];
        while (true) {
            try {
                Thread.sleep(80);
            } catch (Exception e) {
            }
            try {
                if (communication.rx.Get()) {
                    environmentState.updateState(communication.environment.Get());
                }
                blueDestiny[0][0] = -100;
                blueDestiny[0][1] = 0;
                blueDestiny[0][2] = 90;
                this.blueDestiny.Set(blueDestiny);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    public void start() {
        Thread[] threads = {
                new Thread(communication::startReceiving),
                new Thread(communication::startSending),
                new Thread(this::startAI),
                new Thread(this::printEnvironmentState),
                new Thread(this::executeNavigation),
                new Thread(this::executeCommand),
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
