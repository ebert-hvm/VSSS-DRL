package Client;

import java.util.ArrayList;
import Agent.DQN;
import Protobuf.Command;
import Protobuf.Robot;
import ai.djl.ndarray.NDManager;

/**
 * The AI class represents the main Artificial Intelligence module for
 * controlling blue and yellow robots in a robotic soccer game.
 * It manages communication, navigation, environment state, and command
 * execution.
 */
public class AI {
    private Communication communication;
    private Navigation navigation;
    private EnvironmentState environmentState;
    private SharedObject<double[][]> blueVelocity, yellowVelocity;
    private SharedObject<double[][]> blueDestiny;

    /**
     * Initializes a new instance of the AI class. It sets up the necessary
     * components and shared objects.
     */
    public AI() {
        this.communication = new Communication();
        this.navigation = new Navigation();
        this.environmentState = new EnvironmentState();
        blueVelocity = new SharedObject<double[][]>(new double[3][2]);
        yellowVelocity = new SharedObject<double[][]>(new double[3][2]);
        blueDestiny = new SharedObject<double[][]>(new double[3][3]);
    }

    /**
     * Periodically executes navigation logic to determine blue robot velocities.
     */
    private void executeNavigation() {
        double[][] blueVelocity = new double[3][2];
        while (true) {
            try {
                Thread.sleep(Navigation.framePeriod);
            } catch (Exception ex) {
            }
            blueVelocity[0] = navigation.execute(environmentState, blueDestiny.Get()[0]);
            this.blueVelocity.Set(blueVelocity);
        }
    }

    /**
     * Periodically generates and sends commands to the communication module for
     * both blue and yellow robots.
     */
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

    /**
     * Periodically prints the current environment state (if needed) for debugging
     * purposes.
     */
    private void printEnvironmentState() {
        while (true) {
            try {
                Thread.sleep(80);
                Main.clearTerminal();
                environmentState.printState();
            } catch (Exception ex) {

            }
        }
    }

    /**
     * Initializes the AI module and periodically updates the blue robot's
     * destination based on the received environment state.
     */
    private void startAI() {
        DQN dqn = new DQN(5, 4, 64, 32, 32, 0.99f, 0.001f);
        System.out.println("YABA");
        double[][] blueVelocity = new double[3][2];
        //double[][] blueDestiny = new double[3][3];
        while (true) {
            System.out.println("OOOOOOOOOOOOIII");
            try {
                Thread.sleep(80);
            } catch (Exception e) {
            }
            try {
                if (communication.rx.Get()) {
                    environmentState.updateState(communication.environment.Get());
                }
                EnvironmentState.Robot robot = environmentState.blueRobots[0];
                EnvironmentState.Ball ball = environmentState.ball;
                float[] state = {
                    robot.x.Get().floatValue(),
                    robot.y.Get().floatValue(),
                    robot.angle.Get().floatValue(),
                    ball.x.Get().floatValue(),
                    ball.y.Get().floatValue()
                };
                int action = dqn.react(state);
                this.blueVelocity.Get();
                if(action == 0){
                    blueVelocity[0][0] += 0.1;
                } else if(action == 1){
                    blueVelocity[0][0] -= 0.1;
                } else if(action == 2){
                    blueVelocity[0][1] += 0.1;
                } else if(action == 3){
                    blueVelocity[0][1] -= 0.1;
                }
                this.blueVelocity.Set(blueVelocity);
                dqn.collect(0, false);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    /**
     * Starts the AI module by launching multiple threads for various tasks,
     * including communication, navigation,
     * command execution, and environment state printing.
     */
    public void start() {
        Thread[] threads = {
                new Thread(communication::startReceiving),
                new Thread(communication::startSending),
                new Thread(this::startAI),
                // new Thread(this::printEnvironmentState),
                // new Thread(this::executeNavigation),
                new Thread(this::executeCommand),
        };
        for (Thread thread : threads) thread.start();
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Thread thread : threads) thread.interrupt();
        try {
            for (Thread thread : threads) thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
