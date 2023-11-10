package Client;

import java.util.ArrayList;
import java.util.Random;

import Agent.DQN;
import Protobuf.Command;

/**
 * The AI class represents the main Artificial Intelligence module for
 * controlling blue and yellow robots in a robotic soccer game.
 * It manages communication, navigation, environment state, and command
 * execution.
 */
public class AI {
    private static final Random random = new Random();
    private Communication communication;
    private EnvironmentState environmentState;
    private EnvironmentState lastState;
    private SharedObject<double[][]> blueVelocity, yellowVelocity;
    private static double acceleration = 4;

    /**
     * Initializes a new instance of the AI class. It sets up the necessary
     * components and shared objects.
     */
    public AI() {
        this.communication = new Communication();
        this.environmentState = new EnvironmentState();
        this.lastState = new EnvironmentState();
        this.blueVelocity = new SharedObject<double[][]>(new double[3][2]);
        this.yellowVelocity = new SharedObject<double[][]>(new double[3][2]);
    }

    /*
     * return a random double from -1 to 1
     */
    private static double getRandomDouble() {
        return -1.0 + (2.0 * random.nextDouble());
    }

    /**
     * Periodically generates and sends commands to the communication module for
     * both blue and yellow robots.
     */
    private void executeCommand() {

        while (true) {
            try {
                Thread.sleep(80);
            } catch (Exception e) {
            }
            try {

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
     * Only used for debugging
     * Periodically prints the current environment state (if needed) for debugging
     * purposes.
     */
    // private void printEnvironmentState() {
    // while (true) {
    // try {
    // Thread.sleep(80);
    // Main.clearTerminal();
    // environmentState.printState();
    // } catch (Exception ex) {

    // }
    // }
    // }

    /*
     * returns the distance of two 2D vectors as a double
     */
    private double distance(Double[] v, Double[] u) {
        double dist = 0;
        for (int i = 0; i < 2; i++) {
            dist += (v[i] - u[i]) * (v[i] - u[i]);
        }
        return Math.sqrt(dist);
    }

    /*
     * return a vector as a Double[], the subtraction of two 2D vectors.
     */
    private Double[] subtract(Double[] v, Double[] u) {
        Double[] ret = { 0.0, 0.0 };
        for (int i = 0; i < 2; i++) {
            ret[i] = v[i] - u[i];
        }
        return ret;
    }

    // private double dotProduct(Double[] v, Double[] u) {
    // double ret = 0;
    // for (int i = 0; i < 2; i++) {
    // ret += v[i] * u[i];
    // }
    // return ret;
    // }

    /*
     * calculate the reward of the iterarion as a -1 to 1 float value to
     * feed the neural network
     */
    private float reward() {
        // Double[] enemyGoal = { environmentState.field.length.Get() / 2, 0.0 };
        Double[] ballPos = {
                environmentState.ball.x.Get(),
                environmentState.ball.y.Get()
        };
        Double[] robotPos = {
                environmentState.blueRobots[0].x.Get(),
                environmentState.blueRobots[0].y.Get()
        };
        Double[] robotBall = subtract(ballPos, robotPos);
        Double[] zero = { 0.0, 0.0 };
        Double[] ballReplacePos = communication.ballReplacePos.Get();
        Double[] ini_pos = subtract(ballReplacePos, communication.robotReplacePos.Get());
        float initial_d = 1000.0f * (float) distance(zero, ini_pos);

        float robot_ball_reward = (float) Main.clamp(1 - distance(robotBall, zero) / initial_d, -1, 1);
        float ball_goal_reward;
        if ((ballPos[0] - ballReplacePos[0] * 1000) > 0) {
            ball_goal_reward = (float) ((ballPos[0] - ballReplacePos[0] * 1000)
                    / (environmentState.field.length.Get() / 2 - ballReplacePos[0] * 1000));
        } else {
            ball_goal_reward = (float) (-(ballPos[0] - ballReplacePos[0] * 1000)
                    / (-environmentState.field.length.Get() / 2 - ballReplacePos[0] * 1000));
        }
        System.out.println(
                "robot to ball reward: " + robot_ball_reward + ", ball to goal reward: " + ball_goal_reward);
        float ret = 0.1f * robot_ball_reward + 0.9f * ball_goal_reward;
        if (environmentState.goalsBlue.Get() > lastState.goalsBlue.Get()) {
            System.out.println("goal blue: ");
            ret = 1;
        } else if (environmentState.goalsYellow.Get() > lastState.goalsYellow.Get()) {
            System.out.println("goal yellow: ");
            ret = -1;
        }
        System.out.println("total reward: " + ret);
        return ret;

        // Double[] lastBallPos = {
        // lastState.ball.x.Get(),
        // lastState.ball.y.Get()
        // };
        // Double[] lastRobotPos = {
        // lastState.blueRobots[0].x.Get(),
        // lastState.blueRobots[0].y.Get()
        // };

        // double angle = environmentState.blueRobots[0].angle.Get();
        // Double[] orientation = { Math.cos(angle), Math.sin(angle) };
        // float angle_reward = (float) (dotProduct(orientation, robotBall) /
        // distance(zero, robotBall));
        // // Main.clearTerminal();

        // float dist_reward = (float) (distance(lastBallPos, lastRobotPos) -
        // distance(ballPos, robotPos));
        // dist_reward *= 0.03;
        // dist_reward = (float) Main.clamp((double) dist_reward, -1, 1);

        // float reward = (angle_reward + 3 * dist_reward) / 4;

        // float aux = lastReward;
        // lastReward = reward;
        // reward -= aux;
        // if (environmentState.field.width.Get() / 2 - Math.abs(robotPos[1]) < 42 ||
        // environmentState.field.length.Get() / 2 - Math.abs(robotPos[0]) < 42)
        // reward = -1;
        // System.out
        // .println(Main.round(angle_reward, 2) + " " + Main.round(dist_reward, 2) + " "
        // + Main.round(reward, 2));
        // return reward;
    }

    /**
     * Initializes the AI module and periodically updates the blue robot's
     * action based on the received environment state. When the timer is up,
     * the neural network collect the reward and the replace signal is triggered
     */
    private void executeAI() {
        long timer = 7000;
        long time = System.currentTimeMillis() - timer;
        boolean replace;
        DQN dqn = new DQN(12, 3, 8, 32, 32, 0.99f, 0.001f);
        double[][] blueVelocity = new double[3][2];
        blueVelocity[0][0] = blueVelocity[0][1] = 0.0;
        // double[][] blueDestiny = new double[3][3];
        boolean first = true;
        while (true) {
            try {
                if (!communication.rx.Get())
                    continue;

                if (environmentState.lastEnv != null)
                    lastState.updateState(environmentState.lastEnv);
                // update current state
                environmentState.updateState(communication.environment.Get());
                replace = communication.replace.Get();
                if (replace && System.currentTimeMillis() - time > 500) {
                    communication.replace.Set(false);
                    time = System.currentTimeMillis();
                } else if (!replace && (System.currentTimeMillis() - time > timer // time is up
                        || environmentState.goalsBlue.Get() != lastState.goalsBlue.Get() // blue goal
                        || environmentState.goalsYellow.Get() != lastState.goalsYellow.Get())) { // yellow goal
                    time = System.currentTimeMillis();
                    double x1, x2;
                    x1 = getRandomDouble() * (environmentState.field.length.Get() / 2 - 40) / 1000;
                    // x2 = (environmentState.field.length.Get() / 2 + 1000 * x1 + 40 + 21) / 2000
                    // + getRandomDouble() * (environmentState.field.length.Get() / 2 - 1000 * x1 -
                    // 40 - 21) / 2000;
                    x2 = getRandomDouble() * (environmentState.field.length.Get() / 2 - 21) / 1000;
                    while (Math.abs(x2 - x1) < 0.061)
                        x2 = getRandomDouble() * (environmentState.field.length.Get() / 2 - 21) / 1000;
                    Double[] robotReplace = { x1, 0.0, 0.0 };
                    // getRandomDouble() * (environmentState.field.width.Get() / 2 - 40) / 1000,
                    // getRandomDouble() * 180
                    Double[] ballReplace = { x2, 0.0 };
                    // getRandomDouble() * (environmentState.field.width.Get() / 2 - 21) / 1000,
                    if (!first) { // ignore first reward
                        // reward and collect
                        float reward = reward();
                        dqn.collect(reward, false);
                    } else
                        first = false;
                    // Set the replace positions and trigger the replace flag to true
                    communication.robotReplacePos.Set(robotReplace);
                    communication.ballReplacePos.Set(ballReplace);
                    communication.replace.Set(true);
                }
                EnvironmentState.Robot robot = environmentState.blueRobots[0];
                EnvironmentState.Ball ball = environmentState.ball;
                float[] state = {
                        robot.x.Get().floatValue(),
                        robot.y.Get().floatValue(),
                        robot.vX.Get().floatValue(),
                        robot.vY.Get().floatValue(),
                        robot.angle.Get().floatValue(),
                        robot.vAngle.Get().floatValue(),
                        ball.x.Get().floatValue(),
                        ball.y.Get().floatValue(),
                        ball.vX.Get().floatValue(),
                        ball.vY.Get().floatValue(),
                        environmentState.goalsBlue.Get().floatValue(),
                        environmentState.goalsYellow.Get().floatValue(),
                };
                int action = dqn.react(state);
                this.blueVelocity.Get();
                try {
                    if (action == 0) {
                        // FORWARD
                        blueVelocity[0][0] = 15;
                        blueVelocity[0][1] = 15;
                        Thread.sleep(200);
                    } else if (action == 1) {
                        // BACKWARD
                        blueVelocity[0][0] = -15;
                        blueVelocity[0][1] = -15;
                        Thread.sleep(200);
                    } else if (action == 2) {
                        // STOP
                        blueVelocity[0][0] = 0;
                        blueVelocity[0][1] = 0;
                        Thread.sleep(100);
                    }
                    /*
                     * else if (action == 3) {
                     * // ACCELERATE LEFT
                     * blueVelocity[0][0] = Main.clamp(blueVelocity[0][0] + acceleration, -20, 20);
                     * blueVelocity[0][1] = Main.clamp(blueVelocity[0][0] - acceleration, -20, 20);
                     * Thread.sleep(100);
                     * } else if (action == 4) {
                     * // ACCELERATE RIGHT
                     * blueVelocity[0][0] = Main.clamp(blueVelocity[0][0] - acceleration, -20, 20);
                     * blueVelocity[0][1] = Main.clamp(blueVelocity[0][0] + acceleration, -20, 20);
                     * Thread.sleep(100);
                     * }
                     */
                    else if (action == 3) {
                        // TURN LEFT
                        blueVelocity[0][0] = Main.clamp(blueVelocity[0][0] + acceleration, -20, 20);
                        blueVelocity[0][1] = Main.clamp(blueVelocity[0][0] - acceleration, -20, 20);
                        Thread.sleep(100);
                    } else if (action == 4) {
                        // TURN RIGHT
                        blueVelocity[0][0] = Main.clamp(blueVelocity[0][0] - acceleration, -20, 20);
                        blueVelocity[0][1] = Main.clamp(blueVelocity[0][0] + acceleration, -20, 20);
                        Thread.sleep(100);
                    }
                } catch (InterruptedException ex) {
                }
                this.blueVelocity.Set(blueVelocity);
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
                new Thread(this::executeAI),
                new Thread(this::executeCommand),
                // new Thread(this::printEnvironmentState),
        };
        for (Thread thread : threads)
            thread.start();
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Thread thread : threads)
            thread.interrupt();
        try {
            for (Thread thread : threads)
                thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
